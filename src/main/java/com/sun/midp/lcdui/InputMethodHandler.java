/**
 * @(#)InputMethodHandler.java	1.29 02/09/17 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import com.sun.midp.main.Configuration;

/**
 * The InputMethod handler.
 */
abstract public class InputMethodHandler {

    /** A special no-op key. */
    public static final int KEYCODE_NONE       = -1;

    /** Key code for deleting */
    public static final int KEYCODE_CLEAR      = -2;

    /** Key code for deleting all text */
    public static final int KEYCODE_CLEARALL   = -3;

    /** Key code if the '#' key is pressed in NUMERIC or DECIMAL */
    public static final int KEYCODE_SIGNCHANGE = -4;

    /** Key code if the '#' key is pressed in NUMERIC or DECIMAL */
    public static final int KEYCODE_SHIFT      = -5;


    /** handle for the current input method handler. */
    private static InputMethodHandler thisIM;
    /**
     * get the input method handler.
     * @return the current input method handler.
     */
    public static InputMethodHandler getInputMethodHandler() {
        if (thisIM != null) {
            return thisIM;
        }
        thisIM = getInputMethodHandlerImpl();
        return thisIM;
    }
    /**
     * get the input handler class.
     * @return handle to input method handlet.
     */
    private static InputMethodHandler getInputMethodHandlerImpl() {
        try {
            String n =
		Configuration.getProperty(
                    "com.sun.midp.lcdui.inputMethodHandler");
            if (n != null) {
                Class c = Class.forName(n);
                return (InputMethodHandler) c.newInstance();
            }
            String loc = Configuration.getProperty("microedition.locale");
            if (loc != null) {
                /* 
                 * This only check for the first '-' in the locale, and
                 * convert to '_' for Class.forName() to work.
                 */
                int hyphen;
                if ((hyphen = loc.indexOf('-')) != -1) {
                    StringBuffer tmploc = new StringBuffer(loc);
		    tmploc.setCharAt(hyphen, '_');
		    loc = tmploc.toString();
                }

                String cls
                    = "com.sun.midp.lcdui.i18n.DefaultInputMethodHandler_";
                while (true) {
                    try {
                        Class c = Class.forName(cls + loc);
                        return (InputMethodHandler) c.newInstance();
                    } catch (Throwable t) {}
                    int pos = loc.lastIndexOf('_');
                    if (pos == -1) {
                        break;
                    } else {
                        loc = loc.substring(0, pos);
                    }
                }
            }
            Class c = 
                Class.forName("com.sun.midp.lcdui.DefaultInputMethodHandler");
            return (InputMethodHandler) c.newInstance();
        } catch (Throwable t) {            
            System.out.println(t);          
	    throw new Error("Textbox input method was not initialized.");
        }
    }

    /**
     * Set the InputMethodClient object to the InputMethod.
     * @param imc save the current input method client.
     */
    public abstract void setInputMethodClient(InputMethodClient imc);

    /**
     * Clears the current input handler if it matches the specified one.
     * Any pending keys are first sent to the client.
     *
     * @param imc the client that was set to receive callbacks
     * @return true if the current client was cleared, false otherwise
     */
    public abstract boolean clearInputMethodClient(InputMethodClient imc);

    /**
     * Called when a key is pressed.
     * @param keyCode The key code of the key that was pressed
     * @return the key code of the actual key that was input. This may
     *         depend on the keymap in use. -1 if no immediate decision
     *         could be made, in which case a call back may occur in the future.
     */
    public abstract int keyPressed(int keyCode);

    /**
     * Called when a key is released.
     * @param keyCode The key code of the key that was released
     * @return the key code of the key released
     */
    public abstract int keyReleased(int keyCode);

    /**
     * Called when a key is repeated.
     * @param keyCode The key code of the key that was repeated
     * @return the key code of the key repeated
     */
    public abstract int keyRepeated(int keyCode);

    /**
     * Called when a key is repeated.
     * @param c The key code of the key that was repeated
     * @return the key code of the actual key that was input. This may
     *         depend on the keymap in use. -1 if no immediate decision
     *         could be made, in which case a call back may occur in the future.
     */
    public abstract int keyTyped(char c);

    /**
     * Removes any pending key presses
     */
    public abstract void flush();

    /**
     * Return the supported input modes.
     * @return array of strings of supported input modes.
     */
    public abstract String[] supportedInputModes();

    /**
     * set the constraints, modifiers, and other data structures that
     * depend on them.
     *
     * @param constraints constraints to set. See TextField for values
     * @return true if the constraints were set, false otherwise
     */
    public abstract boolean setConstraints(int constraints);
 
    /**
     * Determine whether the character is considered a symbol
     *
     * @param c character to test
     * @return true if the character is considered a symbol, false otherwise
     */
    public boolean isSymbol(char c) { 
        return false;
    }
  
    /**
     * End the on-going composition. Any keys that are pending are
     * conditionally sent to the client.
     *
     * @param discard true to discard any pending keys
     */
    public abstract void endComposition(boolean discard);
}
