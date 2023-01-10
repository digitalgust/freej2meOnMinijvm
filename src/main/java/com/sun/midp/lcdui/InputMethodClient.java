/*
 * @(#)InputMethodClient.java	1.16 02/09/17 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;

/**
 * A public interface for the InputMethod client.
 */
public interface InputMethodClient {

    /**
     * Set which input handler to use
     *
     * @param imh input handler to use
     */
    public void setInputMethodHandler(InputMethodHandler imh);

    /**
     * Set which input mode to use
     *
     * @param inputMode a String identifying the input mode
     */
    public void setInputMode(String inputMode);

    /**
     * Returns the current input mode
     *
     * @return current input mode
     */
    public String getInputMode();

    /**
     * Sets which modes are allowed
     *
     * @param allowedModes array of strings specifying allowed modes
     */
    public void setAllowedModes(String[] allowedModes);

    /**
     * Returns the allowed modes
     *
     * @return the allowed modes
     */
    public String[] getAllowedModes();

    /**
     * Returns the current input constraints
     *
     * @return the current input constraints
     */
    public int getConstraints();

    /**
     * Set the current input constraints
     *
     * @param constraints the constraints to set
     * @return true if the constraints were set correctly
     */ 
    public boolean setConstraints(int constraints);

    /**
     * Returns the current display
     *
     * @return the current display
     */
    public Display getDisplay();

    /**
     * Tell the input client that it should set the displayable to be 
     * the current display. This is called when returning 
     * from the symbol table
     *
     * @param displayable displayable to be used
     * @param display display to be used
     */
    public void setCurrent(Displayable displayable, Display display);
    
    /**
     * Asks the client to determine if the next 
     * character would start a new word
     *
     * @return true is the next character would begin a new word
     */
    public boolean isNewWord();

    /**
     * Asks the client to determine if the next 
     * character would start a new sentence
     *
     * @return true is the next character would begin a new sentence
     */
    public boolean isNewSentence();

    /**
     * Asks the client to determine if the next 
     * character would be the first input to the client
     *
     * @return true is the next character would be the first input 
     */
    public boolean isNewInputEntry();

    //
    // call backs from the InputMethodHandler
    //

    /**
     * Call back from the input handler when a key has been committed
     *
     * @param keyCode key code of the key committed
     */
    public void keyEntered(int keyCode);

    /**
     * Call back from the input handler to inform the client that
     * the input mode has changed and that may want to show that
     * information to the user
     *
     * @param mode new input mode
     */
    public void showInputMode(int mode);

}
