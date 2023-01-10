/*
 * @(#)DefaultInputMethodHandler.java	1.75 02/09/17 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import java.util.Vector;
import java.util.TimerTask;
import java.util.Timer;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;

/**
 * A Default implementation of the InputMethodHandler interface.
 */
public class DefaultInputMethodHandler extends InputMethodHandler {

    /** the client that receives callbacks */
    protected InputMethodClient imc = null;

    /*
     * IDs for supported input modes
     */
    /** The 'none' input mode. */
    protected static final int IM_NONE         = 0;
    /** The 'roman caps' input mode. */
    protected static final int IM_ROMAN_CAPS   = 1;
    /** The 'lowercase roman' input mode. */
    protected static final int IM_ROMAN_SMALL  = 2;
    /** The 'numeric' input mode. */
    protected static final int IM_NUMERIC      = 3;
    /** The 'symbol' input mode. */
    protected static final int IM_SYMBOL       = 4;
    /** Number of supported input modes. */
    protected static int NUM_INPUT_MODES = 5;

    /** the constraints in use */
    protected int currentConstraints;

    /** the modifiers in use */
    protected int currentModifiers;

    /**
     * Default input mode.
     */
    protected int defaultMode;
    /**
     * The number of allowed input modes.
     */
    protected int allowedModesNum;
    /**
     * The set of allowed input modes.
     */
    protected int allowedModes[] = new int[NUM_INPUT_MODES];
    /**
     * Current input mode.
     */
    protected int inputMode;
    /**
     * Old input mode.
     */
    protected int oldInputMode;
    /**
     * boolean to determine if the user had accessed the symbol table
     * via the quick access method of holding SHIFT
     */
    protected boolean quickSymbolAccess = false;

    /**
     * Constraints allow a symbol table to be used
     */
    protected boolean canUseSymbolTable = true;

    /**
     * This is the "last" input mode before jumping into the SYM mode
     * (by holding down the mode shift key for 1 sec). This last mode 
     * tells the input method to switch back to this mode after handling
     * an character from the symbol table.
     */
    protected int lastInputMode;
    /**
     * Character Subset values for supported input modes.
     */
    protected String supportedCharSubset[] = {
        "UCB_BASIC_LATIN",
        "IS_LATIN",
        "IS_LATIN_DIGITS",
        "MIDP_UPPERCASE_LATIN",
        "MIDP_LOWERCASE_LATIN",
        "LATIN",
        "LATIN_DIGITS",
    };
    /**
     * values for supported input modes.
     */
    protected int supportedInputModes[] = {
        IM_ROMAN_CAPS,
        IM_ROMAN_SMALL,
        IM_NUMERIC,
        IM_SYMBOL,
    };

    /**
     * Conversion table between character subsets and input modes.
     */
    protected Object inputModeConvTable[][] = {
        {"UCB_BASIC_LATIN",      new Integer(IM_ROMAN_CAPS)},
        {"UCB_BASIC_LATIN",      new Integer(IM_ROMAN_SMALL)},
        {"UCB_BASIC_LATIN",      new Integer(IM_NUMERIC)},
        {"UCB_BASIC_LATIN",      new Integer(IM_SYMBOL)},
        {"IS_LATIN",             new Integer(IM_ROMAN_CAPS)},
        {"IS_LATIN",             new Integer(IM_ROMAN_SMALL)},
        {"IS_LATIN",             new Integer(IM_NUMERIC)},
        {"IS_LATIN",             new Integer(IM_SYMBOL)},
        {"IS_LATIN_DIGITS",      new Integer(IM_NUMERIC)},
        {"MIDP_UPPERCASE_LATIN", new Integer(IM_ROMAN_CAPS)},
        {"MIDP_LOWERCASE_LATIN", new Integer(IM_ROMAN_SMALL)},
        {"LATIN",                new Integer(IM_ROMAN_CAPS)},
        {"LATIN",                new Integer(IM_ROMAN_SMALL)},
        {"LATIN_DIGITS",         new Integer(IM_NUMERIC)},
        {"LATIN",                new Integer(IM_SYMBOL)},
    };

    /**
     * mode and state variables to support INITIAL_CAPS_WORD/SENTENCE
     * Refer to State Transition Diagram in handleCapMode().
     */
   
    /** cap word mode variable */
    protected boolean capWord;
    /** cap sentence mode variable */
    protected boolean capSentence;

    // Key definition for key maps

    /** Unknown key. */
    protected static final int KEY_UNKNOWN  = -1;
    /** The '0' key. */
    protected static final int KEY_NUM0   = 0;
    /** The '1' key. */
    protected static final int KEY_NUM1   = 1;
    /** The '2' key. */
    protected static final int KEY_NUM2   = 2;
    /** The '3' key. */
    protected static final int KEY_NUM3   = 3;
    /** The '4' key. */
    protected static final int KEY_NUM4   = 4;
    /** The '5' key. */
    protected static final int KEY_NUM5   = 5;
    /** The '6' key. */
    protected static final int KEY_NUM6   = 6;
    /** The '7' key. */
    protected static final int KEY_NUM7   = 7;
    /** The '8' key. */
    protected static final int KEY_NUM8   = 8;
    /** The '9' key. */
    protected static final int KEY_NUM9   = 9;
    /** The '*' key. */
    protected static final int KEY_STAR   = 10;
    /** The '#' key. */
    protected static final int KEY_POUND  = 11;
    /** The 'CLR' key. */
    protected static final int KEY_CLEAR  = 100;

    /**
     * The uppercase roman key map.
     */
    protected char upperRomanKeyMap[][] = {
	{'0'},
	{'1'},
	{'A', 'B', 'C', '2'},
	{'D', 'E', 'F', '3'},
	{'G', 'H', 'I', '4'},
	{'J', 'K', 'L', '5'},
	{'M', 'N', 'O', '6'},
	{'P', 'Q', 'R', 'S', '7'},
	{'T', 'U', 'V', '8'},
	{'W', 'X', 'Y', 'Z', '9'},
	{'\0'},
	{' '}
    };

    /**
     * The lowercase roman key map.
     */
    protected char lowerRomanKeyMap[][] = {
	{'0'},
	{'1'},
	{'a', 'b', 'c', '2'},
	{'d', 'e', 'f', '3'},
	{'g', 'h', 'i', '4'},
	{'j', 'k', 'l', '5'},
	{'m', 'n', 'o', '6'},
	{'p', 'q', 'r', 's', '7'},
	{'t', 'u', 'v', '8'},
	{'w', 'x', 'y', 'z', '9'},
	{'\0'},
	{' '}
    };

    /**
     * The numeric key map.
     * Includes 0-9, *, and space.
     */
    protected char numericKeyMap[][] = {
	{'0'}, 
        {'1'}, 
        {'2'}, 
        {'3'}, 
        {'4'}, 
        {'5'},
	{'6'}, 
        {'7'}, 
        {'8'}, 
        {'9'},
	{'*'},
	{' '}
    };

    /**
     * The decimal key map.
     * Includes 0-9, and period.
     */
    protected char decimalKeyMap[][] = {
        {'0'}, 
        {'1'}, 
        {'2'}, 
        {'3'}, 
        {'4'}, 
        {'5'},
        {'6'}, 
        {'7'}, 
        {'8'}, 
        {'9'},
        {'.'},
        {' '}
    };

    /**
     * The phone numeric key map.
     * Includes 0-9, *, #, +.
     */
    protected char phoneNumericKeyMap[][] = {
	{'0'}, 
        {'1'}, 
        {'2'}, 
        {'3'}, 
        {'4'}, 
        {'5'},
	{'6'}, 
        {'7'}, 
        {'8'}, 
        {'9'},
	{'*'},
	{'#', '+'}
    };

    /**
     * The number of symbol_table is designed to be 25 for 5x5 matrix,
     * starting the selection at 12.  But if you have more, the total
     * must be under 36 for 6x6 matrix.  
     */
    protected char symbolTableChars[] = {
        '_',	'$',	'(',	')',	'\\',

        '~',	'"',	'\'',	'/',	'&',

        '*',	'@',	'.',	'?',	'!',

        '#',	'-',	',',	':',	';',

        '%',	'=',	'+',	'<',	'>'};

    /**
     * Symbol table.
     */
    protected SymbolTable st = new SymbolTable();

    /**
     * Current input table that can be one of the previous maps.
     */
    protected char keyMap[][] = upperRomanKeyMap;

    /**
     * Sets the client that will receive callbacks 
     *
     * @param imc the client to receive callbacks
     */
    public synchronized void setInputMethodClient(InputMethodClient imc) {
        //
        // we don't want keys to hang over onto someone else's turf
        //

        cancelTimer(); 

        ignoreNextKeyRelease = true;
        this.imc = imc;

        lastKey = KEYCODE_NONE;
        lastKeyIndex = KEY_UNKNOWN;
        charIndex = 0;

        if (imc != null) {
            setConstraints(imc.getConstraints());
        }
    }

    /**
     * Clears the current input handler if it matches the specified one.
     *
     * @param imc the client that was set to receive callbacks
     * @return true if the current client was cleared, false otherwise
     */
    public synchronized boolean clearInputMethodClient(InputMethodClient imc) {
        if (this.imc == imc) {
            //
            // send any pending keys to the client before we clear it
            //
            endComposition(false);
            setKeyMap(IM_NONE, currentConstraints);
            this.imc = null;
            return true;
        }
        return false;
    }

    /**
     * Translate the given key code to its index in the key map.
     *
     * @param keyCode The key code of the key pressed
     * @return int The index of the given key code in the key map
     */
    protected int getKeyMapIndex(int keyCode) {
        switch (keyCode) {
            case Canvas.KEY_NUM0:
                return KEY_NUM0;
            case Canvas.KEY_NUM1:
                return KEY_NUM1;
            case Canvas.KEY_NUM2:
                return KEY_NUM2;
            case Canvas.KEY_NUM3:
                return KEY_NUM3;
            case Canvas.KEY_NUM4:
                return KEY_NUM4;
            case Canvas.KEY_NUM5:
                return KEY_NUM5;
            case Canvas.KEY_NUM6:
                return KEY_NUM6;
            case Canvas.KEY_NUM7:
                return KEY_NUM7;
            case Canvas.KEY_NUM8:
                return KEY_NUM8;
            case Canvas.KEY_NUM9:
                return KEY_NUM9;
            case Canvas.KEY_STAR:
                return KEY_STAR;
            case Canvas.KEY_POUND:
                return KEY_POUND;
            case -8:
                return KEY_CLEAR;
            default:
                return KEY_UNKNOWN;
            }
    }

    /**
     * Handle a key pressed event.
     * Overrides InputMethodHandler.keyPressed.
     *
     * @param keyCode The code of the key that was pressed
     * @return int Returns the character that was entered according to
     *             to the current InputMode and constraints, or -1
     *             if the keyCode was not recognized or will be handled
     *             with a call back
     */
    public synchronized int keyPressed(int keyCode) {

        cancelTimer();

        //
        // a new key press means a fresh start so we'll initialize some
        // variables before we begin
        //
        ignoreNextKeyRelease = false;
        quickSymbolAccess = false;


        int idx = getKeyMapIndex(keyCode);

        //
        // the user has pressed another key so commit the previous one
        //
        if (idx != lastKeyIndex) {
            endComposition(false);
        }

        //
        // remember this key index for next time
        //
        lastKeyIndex = idx;


        if (idx == KEY_UNKNOWN) {
            lastKey = KEYCODE_NONE;
            return KEYCODE_NONE;
        }


        //
        // if the user holds the CLEAR key we need to tell the
        // input client
        //
        if (idx == KEY_CLEAR) {
            lastKey = KEYCODE_CLEAR;
            setTimer(TM_CLEAR_BUFFER, 1500);
            return KEYCODE_NONE;
        }


        //
        // these flags will only be set if the user has not tried to 
        // change modes on their own
        //
        if (capWord || capSentence) {
            if (imc.isNewWord() || imc.isNewSentence()) {
                oldInputMode = IM_ROMAN_CAPS;
                inputMode = IM_ROMAN_CAPS;
            } else {
                oldInputMode = IM_ROMAN_SMALL;
                inputMode = IM_ROMAN_SMALL;
            }
            setKeyMap(inputMode, currentConstraints);
   
        } 
      
        lastKey = keyMap[idx][charIndex];
        charIndex = (charIndex + 1) % keyMap[idx].length;

        // System.err.println("keyPressed: lastKey=" + lastKey);

        //
        // if the user holds the star key in these constraints then
        // we need to tell the input client
        //
        if (idx == KEY_STAR && canUseSymbolTable) {
            setTimer(TM_INPUT_MODE, 1000);
            lastKey = KEYCODE_SHIFT;
            return KEYCODE_NONE;
        }

        if (idx == KEY_POUND &&
             (currentConstraints == TextField.NUMERIC   ||
              currentConstraints == TextField.DECIMAL)) {

            lastKey = KEYCODE_SIGNCHANGE;
            endComposition(false);
            return KEYCODE_NONE;
        }

        //
        // if a key has more than one choice we need to cycle through them
        //
        if (keyMap[idx].length > 1) {
            setTimer(TM_IN_COMPOSING, 1600);
        } else {
            endComposition(false);
        }

        return lastKey;
    }

    /**
     * Handle a key released event.
     * Overrides InputMethodHandler.keyReleased.
     *
     * @param keyCode The code of the key that was released
     * @return boolean If True, this handler has handled the key release
     */
    public synchronized int keyReleased(int keyCode) {

        if (ignoreNextKeyRelease) {
            ignoreNextKeyRelease = false;
            lastKey = KEYCODE_NONE;
            lastKeyIndex = KEY_UNKNOWN;
            return lastKey;
        }

        int idx = getKeyMapIndex(keyCode);

        switch (idx) {
            case KEY_UNKNOWN:
                return KEYCODE_NONE;
   
            case KEY_STAR:
                if (timerType == TM_INPUT_MODE) {
                    cancelTimer();
                    if (canUseSymbolTable) {
                        lastKey = KEYCODE_NONE;
                        lastKeyIndex = KEY_UNKNOWN;
                        switchToNextInputMode(false);
                    } else {
                        endComposition(false);
                    }
                }
                break;
            case KEY_CLEAR:
                if (timerType == TM_CLEAR_BUFFER) {
                    cancelTimer();
                    endComposition(false);
                }
                break;
        }

        return lastKey;
    }

    /**
     * Handle a key repeated event.
     * Overrides InputMethodHandler.keyRepeated.
     *
     * @param keyCode The code of the key that was repeated
     * @return boolean If True, this handler has handled the key repeat
     */
    public synchronized int keyRepeated(int keyCode) {
        return keyPressed(keyCode);
    }

     
    /** 
     * Handle a typed key
     *
     * @param c character that was typed
     * @return int Returns the character that was entered according to
     *             to the current InputMode and constraints, or -1
     *             if the keyCode was not recognized or will be handled
     *             with a call back
     */
    public synchronized int keyTyped(char c) {

        cancelTimer();


        ignoreNextKeyRelease = false;
        quickSymbolAccess = false;

        endComposition(false);

        if ((c == 8) || (c == 127)) {
            lastKey = KEYCODE_CLEAR;
        } else {
            lastKey = (int)c;
        }

        endComposition(false);

        return KEYCODE_NONE;
    }

    /**
     * Removes any pending key presses
     */
    public void flush() {
        cancelTimer(); 
        ignoreNextKeyRelease = true;
        lastKey = KEYCODE_NONE;
        lastKeyIndex = KEY_UNKNOWN;
        charIndex = 0;
    }
    
    /**
     * End the on-going composition. Any keys that are pending are
     * conditionally sent to the client.
     *
     * @param discard true to discard any pending keys
     */
    public synchronized void endComposition(boolean discard) {

        if (lastKey == KEYCODE_NONE) {
            return;
        }

        cancelTimer();

        if (!discard && imc != null) {
            imc.keyEntered(lastKey);

            if (imc.isNewInputEntry()) {

                capWord     =
                    (currentModifiers & TextField.INITIAL_CAPS_WORD) ==
                    TextField.INITIAL_CAPS_WORD;

                capSentence =
                    (currentModifiers & TextField.INITIAL_CAPS_SENTENCE) ==
                    TextField.INITIAL_CAPS_SENTENCE;

            }
        }

        lastKey = KEYCODE_NONE;
        lastKeyIndex = KEY_UNKNOWN;
        charIndex = 0;
    }

    /**
     * Get the list of supported input modes of this handler.
     * Overrides InputMethodHandler.supportedInputModes.
     *
     * @return String[] The array of supported input modes
     */
    public String[] supportedInputModes() {
        return supportedCharSubset;
    }

    /** The '0' Timer. */
    protected final static int TM_NONE         = 0;
    /** The input mode timer. */
    protected final static int TM_INPUT_MODE   = 1;
    /** The 'in composition' timer. */
    protected final static int TM_IN_COMPOSING = 2;
    /** The 'clear buffer' timer. */
    protected final static int TM_CLEAR_BUFFER = 3;
    /** The type of timer to set. */
    protected int timerType;

    /**
     * Set a new timer.
     *
     * @param type The type of Timer to set
     * @param delay The length of delay for the Timer
     */
    protected void setTimer(int type, long delay) {
        if (type != timerType) {
            if (type == TM_IN_COMPOSING) {
            }
            timerType = type;
        }
        cancelTimer();
        try {
            timerClient = new TimerClient();
            timerService.schedule(timerClient, delay);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            cancelTimer();
        }
    }

    /** The Timer to service TimerTasks. */
    protected Timer timerService = new Timer();
    /** A TimerTask. */
    protected TimerTask timerClient = null;

    /**
     * A special TimerTask class
     */
    class TimerClient extends TimerTask {
        /**
         * Simply calls timerWentOff()
         */
        public final void run() {
            timerWentOff();
        }
    }

    /**
     * Cancel any running Timer.
     */
    protected synchronized void cancelTimer() {
        if (timerType != TM_NONE && timerClient != null) {
            timerClient.cancel();
            timerClient = null;
            timerType = TM_NONE;
        }
    }

    /**
     * Called by the TimerTask when a Timer executes.
     */
    protected synchronized void timerWentOff() {

        switch (timerType) {
            case TM_INPUT_MODE:
                quickSymbolAccess = true;

                ignoreNextKeyRelease = true;
                endComposition(true);

                oldInputMode = inputMode;
                st.invokeSYM();

                break;
            case TM_CLEAR_BUFFER:
                lastKey = KEYCODE_CLEARALL;
                // fall through
            case TM_IN_COMPOSING:
                endComposition(false);
                ignoreNextKeyRelease = true;
                break;
        }
    }

    /** The last key and last position of the input. */
    protected int lastKey;

    /** lastKey index into the key map */
    protected int lastKeyIndex;

    /** last character index into the key map at the */
    protected int charIndex;

    /** ignore the next keyRelease event */
    protected boolean ignoreNextKeyRelease = false;

    /**
     * Switch to the next available input mode. 
     * Sets capWord and capSentence to false
     *
     * @param fromSymTable true if this is being called from 
     *                     the symbol table class. false otherwise
     */
    protected void switchToNextInputMode(boolean fromSymTable) {

        if (fromSymTable && quickSymbolAccess) {
            inputMode = oldInputMode;
        } else if (fromSymTable) {
            oldInputMode = defaultMode;
            inputMode = defaultMode;
        } else {
            int n = 0;

            // get the current input mode
            while (n < allowedModesNum) {
                if (allowedModes[n] == inputMode) {
                    break;
                }
                n++;
            }
            n = (n + 1) % allowedModesNum;
            oldInputMode = inputMode;
            inputMode = allowedModes[n];

            capWord = capSentence = false;

        }

        setKeyMap(inputMode, currentConstraints);
        
    }

    /** 
     * Set the appropriate keypad mapping according to the input mode
     *
     * @param mode The new input mode to use
     * @param constraints the constraints to use
     * @return true if keyMap is set.
     */
    protected boolean setKeyMap(int mode, int constraints) {
        switch (mode) {
            case IM_ROMAN_CAPS:
                keyMap = upperRomanKeyMap;
                break;
            case IM_ROMAN_SMALL:
                keyMap = lowerRomanKeyMap;
                break;
            case IM_NUMERIC:
                keyMap = (constraints == TextField.PHONENUMBER) 
                       ? phoneNumericKeyMap
                       : (constraints == TextField.DECIMAL 
                           ? decimalKeyMap 
                           : numericKeyMap);

                break;
            case IM_SYMBOL:
                st.invokeSYM();
                break;
            case IM_NONE:
                break;
            default:
                return false;
        }
   
        imc.showInputMode(mode);

        return true;
    }
        

    /**
     * Set the internal constraint and modifier variables. Determine
     * the best inputMode and keyMap to use for the given constraints
     *
     * @param constraints Constraints to use
     * @return true if the constraints were set correctly
     */
    public synchronized boolean setConstraints(int constraints) {

        boolean ret = false;

        currentConstraints = constraints & TextField.CONSTRAINT_MASK;
        currentModifiers   = constraints & ~TextField.CONSTRAINT_MASK;

        canUseSymbolTable = (currentConstraints == TextField.URL) ||
                            (currentConstraints == TextField.EMAILADDR) ||
                            (currentConstraints == TextField.ANY);

        // if (constraints != currentConstraints) 
        {
            ret = buildInputModes(currentConstraints, currentModifiers,
                                  imc.getInputMode(), 
                                  imc.getAllowedModes());

        }

        // currentConstraints = constraints;
        setKeyMap(inputMode, currentConstraints);

        return ret;
    }

    /**
     * Set the input mode.
     *
     * @param constraints The constraints
     * @param modifiers The modifiers
     * @param dMode the default mode, null to choose one
     * @param aMode array of allowed modes, or null to use all
     * @return boolean True, if the mode was valid
     */
    protected boolean buildInputModes(int constraints, int modifiers,
                                       String dMode, String[] aMode) {

        boolean ret = true;

        capWord = capSentence = false;

        if (constraints == TextField.NUMERIC ||
            constraints == TextField.DECIMAL ||
            constraints == TextField.PHONENUMBER) {

            defaultMode = IM_NUMERIC;
            allowedModesNum = 1;
            allowedModes[0] = IM_NUMERIC;
        } 
        else if (constraints == TextField.EMAILADDR ||
                 constraints == TextField.URL) {

            defaultMode = IM_ROMAN_SMALL;
            allowedModesNum = 4;
            allowedModes[0] = IM_ROMAN_SMALL;
            allowedModes[1] = IM_ROMAN_CAPS;
            allowedModes[2] = IM_NUMERIC;
            allowedModes[3] = IM_SYMBOL;
        } 
        else {
            if (constraints != TextField.ANY) {
                dMode = null;
                aMode = null;
            }

            /* 
             * The mappings from character subsets to input modes are not
             * unique. So, we use this vector to make sure we do not end up
             * with the same input mode multiple times.
             */
            Vector uniqueModes = new Vector(NUM_INPUT_MODES);

            if (dMode == null && aMode == null) {
                allowedModesNum = supportedInputModes.length;
                System.arraycopy(supportedInputModes, 0, 
                                 allowedModes, 0, allowedModesNum);
                defaultMode = allowedModes[0];
            } else {
                if (dMode == null && aMode != null) {
                    dMode = aMode[0];
                }
                if (dMode != null && aMode == null) {
                    aMode = new String[supportedCharSubset.length];
                    System.arraycopy(supportedCharSubset, 0, 
                                     aMode, 0, supportedCharSubset.length);
                }

                /* get the default initial mode based on the character subset */
                for (int i = 0; i < inputModeConvTable.length; i++) {
                    if (((String)inputModeConvTable[i][0]).
                        equals(dMode)) {
                        defaultMode = 
                            ((Integer)inputModeConvTable[i][1]).intValue();
                        break;
                    }
                }

                /* 
                 * get all the allowed input modes based on the character 
                 * subset 
                 */
                for (int j = 0; j < inputModeConvTable.length; j++) {
                    for (int i = 0; i < aMode.length; i++) {
                        Integer imode = (Integer)inputModeConvTable[i][1];
                        if (((String)inputModeConvTable[j][0]).equals(aMode[i]) 
                            && uniqueModes.indexOf(imode) == -1) {
                            uniqueModes.addElement(imode);
                        }
                    }
                }

                allowedModesNum = uniqueModes.size();
                for (int i = 0; i < allowedModesNum; i++) {
                   allowedModes[i] = 
                       ((Integer)uniqueModes.elementAt(i)).intValue();
                }

                if (defaultMode == IM_NONE) {
                    defaultMode = allowedModes[0];
                }
                /*
                  temp comment out this code. Not sure the purpose of this
                  code. We do not want to swap the input mode sequence.
                  -tylee 060302

                } else {
                    if (defaultMode != allowedModes[0]) {
                        for (i = 0; i < allowedModesNum; i++) {
                            if (defaultMode == allowedModes[i]) {
                                break;
                            }
                        }
                        int t = allowedModes[0];
                        allowedModes[i] = t;
                        allowedModes[0] = defaultMode;
                    }
                }
               */

            }

            capWord     = 
                (modifiers & TextField.INITIAL_CAPS_WORD) == 
                TextField.INITIAL_CAPS_WORD;

            capSentence = 
                (modifiers & TextField.INITIAL_CAPS_SENTENCE) == 
                TextField.INITIAL_CAPS_SENTENCE;

            if (capWord || capSentence) {
                defaultMode = IM_ROMAN_CAPS;
            }
        }

        oldInputMode = defaultMode;
        inputMode = defaultMode;

        return ret;
    }

    /**
     * Determine if the given character is considered a symbol
     *
     * @param c character to check
     * @return true if the character is a symbol, false otherwise
     */
    public boolean isSymbol(char c) {
        for (int i = 0; i < symbolTableChars.length; i++) {
            if (symbolTableChars[i] == c) {
                return true;
            }
        }
        return false;
    }
 
    /**
     * A special Canvas to display a symbol table.
     */
    protected class SymbolTable extends Canvas {
        /** The margin size */
        private final int MARGIN = 1;
        /** The margin size */
        private final int DMARGIN = 2;
        /** Cell size */
        private int cc;
        /** Height margin */
        private int hmargin;
        /** Width margin */
        private int wmargin;
        /** Margin for the cursor */
        private int margin;
        /** Window x position */
        private int wx;
        /** Window y position */
        private int wy;
        /** Window width */
        private int ww;
        /** Window height */
        private int wh;
        /** Number of columns */
        private int cols;
        /** Number of rows */
        private int rows;
        /** Current cursor position */
        private int pos;
        /** New cursor position */
        private int newpos;
        /** Font */
        private Font font;
        /** Flag */
        private boolean firstTime = true;
        /** The current Display object */
        private Display currentDisplay;
        /** The previous Displayable */
        private Displayable previousScreen;


        /** default location to start the cursor */
        protected int defaultSymbolCursorPos = 12;

        /** temporary holder of the imc that was being used */
        InputMethodClient tmpimc;


        /**
         * Initialize the symbol table
         */
        void init() {
            if (symbolTableChars.length <= 25) {
                rows = cols = 5;
            } else {
                rows = 6;
                cols = 6;
            }

            int w = getWidth() / cols;
            int h = getHeight() / rows;

            cc = (w > h) ? h : w;

            int cw = 0, ch = 0;
            int[] fs = {Font.SIZE_LARGE, Font.SIZE_MEDIUM, Font.SIZE_SMALL};
            for (int i = 0; i < fs.length; i++) {
                font = Font.getFont(Font.FACE_SYSTEM,
                                    Font.STYLE_BOLD,
                                    fs[i]);
                cw = font.charWidth('M');
                ch = font.getHeight();
                if (cw <= cc && ch <= cc) {
                    break;
                }
            }

            ww = cols * cc;
            wh = rows * cc;

            wx = (getWidth() - ww) / 2;
            wy = getHeight() - wh;

            hmargin = (cc - ch) / 2;
            wmargin = cc / 2;
            margin = hmargin + 2;
        }

        /**
         * Invoke this symbol table.
         */
        public void invokeSYM() {
            if (font == null) {
                init();
            }

            tmpimc = imc;
            currentDisplay = imc.getDisplay();
            previousScreen = currentDisplay.getCurrent();
            currentDisplay.setCurrent(this);
        }

        /**
         * Notify this symbol table its being shown on the screen.
         * Overrides Canvas.showNotify.
         */
        protected void showNotify() {
            pos = newpos = defaultSymbolCursorPos;
        }

        /**
         * Notify this symbol table its being hidden.
         * Overrides Canvas.hideNotify.
         */
        protected void hideNotify() {
            firstTime = true;
        }

        /**
         * Paint this symbol table.
         * Overrides Canvas.paint.
         *
         * @param g The Graphics object to paint to
         */
        protected void paint(Graphics g) {
            if (firstTime) {
                paintPanel(g);
                firstTime = false;
            }
            showCursor(g, pos, false);
            showCursor(g, pos = newpos, true);
        }

        /**
         * Paint the symbol table panel
         *
         * @param g The Graphics object to paint to
         */
        void paintPanel(Graphics g) {
            g.setFont(font);

            g.setGrayScale(255);
            g.setClip(0, 0, getWidth(), getHeight());
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setGrayScale(0);
            g.drawRect(wx+1, wy+1, ww-2, wh-2);

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int i = r * cols + c;
                    if (i == symbolTableChars.length) {
                        break;
                    }
                    drawChar(g, symbolTableChars[i], r, c, false);
                }
            }
        }

        /**
         * Draw a character
         *
         * @param g The Graphics object to paint to
         * @param c The character to draw
         * @param row The row the character is in
         * @param col The column the character is in
         * @param reverse A flag to draw the character in inverse
         */
        void drawChar(Graphics g, char c, int row, int col, boolean reverse) {
            int h = font.charWidth(c);
            int y = wy + row * cc + hmargin;
            int x = wx + col * cc + wmargin;

            g.setFont(font);

            if (reverse) {
                g.setGrayScale(255);
            } else {
                g.setGrayScale(0);
            }

            g.drawChar(c, x, y, Graphics.HCENTER | Graphics.TOP);
        }

        /**
         * Show the cursor of this symbol table
         *
         * @param g The Graphics object to paint to
         * @param pos The position of the cursor
         * @param show A flag indicating the visibility of the cursor
         */
        void showCursor(Graphics g, int pos, boolean show) {
            int row = pos / cols;
            int col = pos % cols;
            int y = wy + row * cc;
            int x = wx + col * cc;

            if (show) {
                g.setGrayScale(0);
            } else {
                g.setGrayScale(255);
            }
            g.fillRect(x+margin, y+margin, cc-margin-1, cc-margin-1);

            drawChar(g, symbolTableChars[pos], row, col, show);
        }

        /**
         * Handle a key press event on this symbol table.
         * Overrides Canvas.keyPressed.
         *
         * @param keyCode The key that was pressed
         */
        protected void keyPressed(int keyCode) {
            if ((keyCode > 0) && (((char)keyCode) == '*')) {

                tmpimc.setCurrent(previousScreen, currentDisplay);

                currentDisplay.callSerially(
                    new Runnable() {
                        public void run() {
                            endComposition(true);
                            ignoreNextKeyRelease = true;
                            switchToNextInputMode(true);
                        }
                    });

            } else {
                switch (getGameAction(keyCode)) {
                case Canvas.RIGHT:
                    if ((pos + 1) < symbolTableChars.length) {
                        newpos = pos + 1;
                        repaint();
                    }
                    break;

                case Canvas.LEFT:
                    if (pos > 0) {
                        newpos = pos - 1;
                        repaint();
                    }
                    break;

                case Canvas.UP: {
                    int p = pos - cols;
                    if (p >= 0) {
                        newpos = p;
                        repaint();
                    }
                    break;
                }

                case Canvas.DOWN: {
                    int p = pos + cols;
                    if (p < symbolTableChars.length) {
                        newpos = p;
                        repaint();
                    }
                    break;
                }

                case Canvas.FIRE:

                    tmpimc.setCurrent(previousScreen, currentDisplay);

                    currentDisplay.callSerially(
                        new Runnable() {
                            public void run() {
                                lastKey = symbolTableChars[pos];
                                endComposition(false);
                                ignoreNextKeyRelease = true;
                                switchToNextInputMode(true);
                            }
                        });

                    break;
                }
            }
        }

    }
}

