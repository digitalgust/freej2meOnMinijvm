/*
 * @(#)DefaultInputMethodHandler_ja.java	1.23 02/07/24 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui.i18n;

import com.sun.midp.lcdui.DefaultInputMethodHandler;
import com.sun.midp.lcdui.InputMethodClient;

/**
 * The Japanese implementation of the of the InputMethodHandler interface.
 * @author unattributed
 * @version 1.23 07/24/02
 */
public class DefaultInputMethodHandler_ja extends DefaultInputMethodHandler {

    /** Hiragana Input Method. */
    protected static final int IM_HIRAGANA      = IM_SYMBOL + 1;

    /** Kana Input Method. */
    protected static final int IM_HWKATAKANA    = IM_SYMBOL + 2;

    /** The Halfwidth Katakana character set symbol table. */
    protected char hwKatakanaKeyMap[][] = {
        /*   wa        wo        wn */ 
        {'\uff9c', '\uff66', '\uff9d',
         /* DAKU       PU      KUTEN */
         '\uff9e', '\uff9f', '\uff61',
         /* SEP       LBRA      RBRA     TOUTEN */
         '\uff65', '\uff62', '\uff63', '\uff64'},
        /*    A       I       U       E       O  */
        {'\uff71', '\uff72', '\uff73', '\uff74', '\uff75',
         /*    a       i       u       e       o  */
         '\uff67', '\uff68', '\uff69', '\uff6a', '\uff6b'},
        /*   KA      KI      KU      KE      KO */
        {'\uff76', '\uff77', '\uff78', '\uff79', '\uff7a'},
        /*   SA      SI      SU      SE      SO */
        {'\uff7b', '\uff7c', '\uff7d', '\uff7e', '\uff7f'},
        /*   TA      TI      TU      TE      TO      tu */
        {'\uff80', '\uff81', '\uff82', '\uff83', '\uff84', '\uff6f'},
        /*   NA      NI      NU      NE      NO */
        {'\uff85', '\uff86', '\uff87', '\uff88', '\uff89'},
        /*   HA      HI      HU      HE      HO */
        {'\uff8a', '\uff8b', '\uff8c', '\uff8d', '\uff8e'},
        /*   MA      MI      MU      ME      MO */
        {'\uff8f', '\uff90', '\uff91', '\uff92', '\uff93'},
        /*   YA      YU      YO      ya      yu      yo */
        {'\uff94', '\uff95', '\uff96', '\uff6c', '\uff6d', '\uff6e'},
        /*   RA      RI      RU      RE      RO */
        {'\uff97', '\uff98', '\uff99', '\uff9a', '\uff9b'},
        /* null */
        {'\0'},
        /* SPACE */
        {' '}
    };

    /** The Hiragana character set symbol table. */
    protected char hiraganaKeyMap[][] = {
        /*   WA      WO      WN  */
        {'\u308f', '\u3092', '\u3093',
        /*   SP  TOUTEN   KUTEN   QUEST  EXCLAM */
         '\u3000', '\u3001', '\u3002', '\uff1f', '\uff01',
        /*  SEP   COLON  SEMICOL QUOTEDBL   YEN */
         '\u30fb', '\uff1a', '\uff1b', '\u201d', '\uffe5',
        /* AMPERSAND ASTERISK */
         '\uff06', '\uff0a'},
        /*    A       I       U       E       O  */
        {'\u3042', '\u3044', '\u3046', '\u3048', '\u304a',
         /*   a       i       u       e       o */
         '\u3041', '\u3043', '\u3045', '\u3047', '\u3049'},
        /*   KA      KI      KU      KE      KO */
        {'\u304b', '\u304d', '\u304f', '\u3051', '\u3053',
         /*   GA      GI      GU      GE      GO */
         '\u304c', '\u304e', '\u3050', '\u3052', '\u3054'},
        /*   SA      SI      SU      SE      SO */
        {'\u3055', '\u3057', '\u3059', '\u305b', '\u305d',
         /*   ZA      ZI      ZU      ZE      ZO */
         '\u3056', '\u3058', '\u305a', '\u305c', '\u305e'},
        /*   TA      TI      TU      TE      TO      tu */
        {'\u305f', '\u3061', '\u3064', '\u3066', '\u3068', '\u3063',
         /*   DA      DI      DU      DE      DO */
         '\u3060', '\u3062', '\u3065', '\u3067', '\u3069'},
        /*   NA      NI      NU      NE      NO */
        {'\u306a', '\u306b', '\u306c', '\u306d', '\u306e'},
        /*   HA      HI      HU      HE      HO */
        {'\u306f', '\u3072', '\u3075', '\u3078', '\u307b',
         /*   BA      BI      BU      BE      BO */
         '\u3070', '\u3073', '\u3076', '\u3079', '\u307c',
         /*   PA      PI      PU      PE      PO */
         '\u3071', '\u3074', '\u3077', '\u307a', '\u307d'},
        /*   MA      MI      ME      ME      MO */
        {'\u307e', '\u307f', '\u3080', '\u3081', '\u3082'},
        /*   YA      YU      YO      ya      yu      yo */
        {'\u3084', '\u3086', '\u3088', '\u3083', '\u3085', '\u3087'},
        /*   RA      RI      RU      RE      RO */
        {'\u3089', '\u308a', '\u308b', '\u308c', '\u308d'},
        /* null */
        {'\0'},
        /* SPACE */
        {'\u3000'}
    };

    // Japanese specific symbol table characters
    // protected char jaSymbolTableChars[] = {
    //     '|',  '\uff62', '\uff63', '\uff64',  '{',  '}', 
    // 
    //     '_',	'$',	'(',	')',	'\\', '\uff65', 
    // 
    //     '~',	'"',	'\'',	'/',	'&',  '\uff61',
    // 
    //     '*',	'@',	'.',	'?',	'!',  '`', 
    // 
    //     '#',	'-',	',',	':',	';',  '^', 
    // 
    //     '%',	'=',	'+',	'<',	'>'};

    /** Character Subset values for additional Japanese input modes. */
    protected String jaCharSubset[] = {
        "UCB_HIRAGANA",
        "IS_HALFWIDTH_KATAKANA",
        "UCB_BASIC_LATIN",
        "IS_LATIN",
        "IS_LATIN_DIGITS",
        "MIDP_UPPERCASE_LATIN",
        "MIDP_LOWERCASE_LATIN",        
        "LATIN",
        "LATIN_DIGITS",
    };
    /**
     * values for default supported input modes.
     */
    protected int jaSupportedInputModes[] = {
        IM_HIRAGANA,
        IM_HWKATAKANA,
        IM_ROMAN_CAPS,
        IM_ROMAN_SMALL,
        IM_NUMERIC,
        IM_SYMBOL,
    };


    /** The Japanese input mode conversion table. */
    protected Object jaInputModeConvTable[][] = {
        {"UCB_HIRAGANA",          new Integer(IM_HIRAGANA)},
        {"IS_HALFWIDTH_KATAKANA", new Integer(IM_HWKATAKANA)},
        {"UCB_BASIC_LATIN",       new Integer(IM_ROMAN_CAPS)},
        {"UCB_BASIC_LATIN",       new Integer(IM_ROMAN_SMALL)},
        {"UCB_BASIC_LATIN",       new Integer(IM_NUMERIC)},
        {"UCB_BASIC_LATIN",       new Integer(IM_SYMBOL)},
        {"IS_LATIN",              new Integer(IM_ROMAN_CAPS)},
        {"IS_LATIN",              new Integer(IM_ROMAN_SMALL)},
        {"IS_LATIN",              new Integer(IM_NUMERIC)},
        {"IS_LATIN",              new Integer(IM_SYMBOL)},
        {"IS_LATIN_DIGITS",       new Integer(IM_NUMERIC)},
        {"MIDP_UPPERCASE_LATIN",  new Integer(IM_ROMAN_CAPS)},
        {"MIDP_LOWERCASE_LATIN",  new Integer(IM_ROMAN_SMALL)},
        {"LATIN",                 new Integer(IM_ROMAN_CAPS)},
        {"LATIN",                 new Integer(IM_ROMAN_SMALL)},
        {"LATIN_DIGITS",          new Integer(IM_NUMERIC)},
        {"LATIN",                 new Integer(IM_SYMBOL)},
    };

    /**
     * Constructs a new DefaultInputMethodHandler_ja object
     * to handle japanese input modes.
     */
    public DefaultInputMethodHandler_ja() {
        NUM_INPUT_MODES = 7; // override the super class value
        allowedModes = new int[NUM_INPUT_MODES];
        super.supportedCharSubset = jaCharSubset;
        super.supportedInputModes = jaSupportedInputModes;
        super.inputModeConvTable = jaInputModeConvTable;
        // Japanese specific symbol table
        // super.symbolTableChars = jaSymbolTableChars;
        // super.defaultSymbolCursorPos = 20;
    }

    /**
     * Set the focus of this handler.
     * Overrides DefaultInputMethodHandler.setFocus
     * The difference here is that after SYM mode, it returns to HIRAGANA
     * mode (as opposed to abc mode.
     *
     * @param focus If True, this handler should take focus
     */
/*
    public void setFocus(boolean focus) {
        //
        // Note that setFocus() is always called when Display is
        // changed.  
        //
        if (imc == null) {
            return;
        }
        if (focus) {
            if (hasSymbolTable) {
                if (bufLen > 0) {
                    handleCharInput();
                }
                setInputMode(nextInputMode());
                hasSymbolTable = false;
            } else {
                setInputMode(defaultMode);
            }
        } else {
            if (!hasSymbolTable) {
                setInputMode(IM_NONE);
            }
        }
    }
*/

    /**
     * Set the input method client.
     * @param imc The Input method to use.
     */
    public void setInputMethodClient(InputMethodClient imc) {
        super.setInputMethodClient(imc);

        if (capWord || capSentence) {
            // INITIAL_CAPS_WORD or INITIAL_CAPS_SENTENCE is not supported for
            // Japanese input method.
            capWord = capSentence = false;

            // Need to re-do the input mode and constraint after setting
            // capWord and capSentence to false.
            // setConstraint();
            // inputMode = defaultMode;
        }

        // initial the keyMap
        // setKeyMap(inputMode); 
    }

    /** 
     * Set the appropriate keypad mapping according to the input mode.
     * Override super class to add hiragana and katakana support.
     *
     * @param mode The new input mode to use
     * @param constraints the constraints to use
     * @return true if keyMap is set.
     */
    protected boolean setKeyMap(int mode, int constraints) {
        if (super.setKeyMap(mode, constraints)) {
            return true;
        }

        switch (mode) {
        case IM_HWKATAKANA:
            keyMap = hwKatakanaKeyMap;
            break;
        case IM_HIRAGANA:
            keyMap = hiraganaKeyMap;
            break;
        default:
            return false;
        }
        
        imc.showInputMode(mode);

        return true;
    } 
}
