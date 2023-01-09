/*
 * @(#)TextBox.java	1.220 02/10/14 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.InputMethodClient;
import com.sun.midp.lcdui.InputMethodHandler;
import com.sun.midp.lcdui.EventHandler;

/**
 * The <code>TextBox</code> class is a <code>Screen</code> that allows
 * the user to enter and edit
 * text.
 *
 * <p>A <code>TextBox</code> has a maximum size, which is the maximum
 * number of characters
 * that can be stored in the object at any time (its capacity). This limit is
 * enforced when the <code>TextBox</code> instance is constructed,
 * when the user is editing text within the <code>TextBox</code>, as well as
 * when the application program calls methods on the
 * <code>TextBox</code> that modify its
 * contents. The maximum size is the maximum stored capacity and is unrelated
 * to the number of characters that may be displayed at any given time.
 * The number of characters displayed and their arrangement into rows and
 * columns are determined by the device. </p>
 *
 * <p>The implementation may place a boundary on the maximum size, and the 
 * maximum size actually assigned may be smaller than the application had 
 * requested.  The value actually assigned will be reflected in the value
 * returned by {@link #getMaxSize() getMaxSize()}.  A defensively-written 
 * application should compare this value to the maximum size requested and be 
 * prepared to handle cases where they differ.</p>
 *
 * <p>The text contained within a <code>TextBox</code> may be more
 * than can be displayed at
 * one time. If this is the case, the implementation will let the user scroll
 * to view and edit any part of the text. This scrolling occurs transparently
 * to the application. </p>
 *
 * <p>If the constraints are set to {@link TextField#ANY}
 * The text may contain <A HREF="Form.html#linebreak">line breaks</A>.
 * The display of the text must break accordingly and the user must be
 * able to enter line break characters. </p>
 *
 * <p><code>TextBox</code> has the concept of 
 * <em>input constraints</em> that is identical to
 * <code>TextField</code>. The <code>constraints</code> parameters of
 * methods within the
 * <code>TextBox</code> class use constants defined in the {@link
 * TextField TextField}
 * class. See the description of
 * <a href="TextField.html#constraints">input constraints</a>
 * in the <code>TextField</code> class for the definition of these
 * constants.  <code>TextBox</code> also has the same notions as
 * <code>TextField</code> of the <em>actual contents</em> and the
 * <em>displayed contents</em>, described in the same section.
 * </p>
 *
 * <p><code>TextBox</code> also has the concept of <em>input
 * modes</em> that is identical
 * to <code>TextField</code>. See the description of <a
 * href="TextField.html#modes">input
 * modes</a> in the <code>TextField</code> class for more details.
 * 
 * @since MIDP 1.0
 */

public class TextBox extends Screen {

    /** internal form */
    private Form form;

    /** text field object to put on the form */
    private TextField textField;

    /**
     * Creates a new <code>TextBox</code> object with the given title
     * string, initial
     * contents, maximum size in characters, and constraints.
     * If the text parameter is <code>null</code>, the
     * <code>TextBox</code> is created empty.
     * The <code>maxSize</code> parameter must be greater than zero.
     * An <code>IllegalArgumentException</code> is thrown if the
     * length of the initial contents string exceeds <code>maxSize</code>.
     * However,
     * the implementation may assign a maximum size smaller than the 
     * application had requested.  If this occurs, and if the length of the 
     * contents exceeds the newly assigned maximum size, the contents are 
     * truncated from the end in order to fit, and no exception is thrown.
     *
     * @param title the title text to be shown with the display
     * @param text the initial contents of the text editing area,
     * <code>null</code> may be used to
     * indicate no initial content
     * @param maxSize the maximum capacity in characters. The implementation
     * may limit
     * boundary maximum capacity and the actually assigned capacity may
     * me smaller than requested. A defensive application will test the
     * actually given
     * capacity with {@link #getMaxSize()}.
     * @param constraints see <a href="TextField.html#constraints">input
     * constraints</a>
     *
     * @throws IllegalArgumentException if <code>maxSize</code> is zero or less
     * @throws IllegalArgumentException if the <code>constraints</code>
     * parameter is invalid
     * @throws IllegalArgumentException if <code>text</code> is illegal
     * for the specified constraints
     * @throws IllegalArgumentException if the length of the string exceeds
     * the requested maximum capacity
     */
    public TextBox(String title, String text, int maxSize, int constraints) {
        super(title);

        synchronized (Display.LCDUILock) {
         
            form = new Form(title);
            form.paintDelegate = this;

            if ((TextField.UNEDITABLE & constraints) == TextField.UNEDITABLE) {
                form.paintBorder = BORDER_GRAY;
                // cursorEnabled = false;
            } else {
                form.paintBorder = BORDER_SOLID;
                // cursorEnabled is default to be true;
            }

            textField = new TextField(null, text, maxSize, constraints);
            textField.setBorder(false);

            form.append(textField);
            
        }
    }

    /**
     * Gets the contents of the <code>TextBox</code> as a string value.
     *
     * @return the current contents
     * @see #setString
     */
    public String getString() {
        return textField.getString();
    }

    /**
     * Sets the contents of the <code>TextBox</code> as a string
     * value, replacing the previous contents.
     *
     * @param text the new value of the <code>TextBox</code>, or
     * <code>null</code> if the <code>TextBox</code> is to 
     * be made empty
     * @throws IllegalArgumentException if <code>text</code>
     * is illegal for the current 
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws IllegalArgumentException if the text would exceed the current
     * maximum capacity
     * @see #getString
     */
    public void setString(String text) {
        textField.setString(text);
    }
    
    /**
     * Copies the contents of the <code>TextBox</code> into a
     * character array starting at
     * index zero. Array elements beyond the characters copied are left
     * unchanged.
     *
     * @param data the character array to receive the value
     * @return the number of characters copied
     * @throws ArrayIndexOutOfBoundsException if the array is too short for the
     * contents
     * @throws NullPointerException if <code>data</code> is <code>null</code>
     * @see #setChars
     */
    public int getChars(char[] data) {
        return textField.getChars(data);
    }

    /**
     * Sets the contents of the <code>TextBox</code> from a character
     * array, replacing the
     * previous contents. Characters are copied from the region of the
     * <code>data</code> array
     * starting at array index <code>offset</code> and running for
     * <code>length</code> characters.
     * If the data array is <code>null</code>, the <code>TextBox</code>
     * is set to be empty and the other parameters are ignored.
     *
     * <p>The <code>offset</code> and <code>length</code> parameters must
     * specify a valid range of characters within
     * the character array <code>data</code>.
     * The <code>offset</code> parameter must be within the
     * range <code>[0..(data.length)]</code>, inclusive.
     * The <code>length</code> parameter
     * must be a non-negative integer such that
     * <code>(offset + length) &lt;= data.length</code>.</p>
     * 
     * @param data the source of the character data
     * @param offset the beginning of the region of characters to copy
     * @param length the number of characters to copy
     * @throws ArrayIndexOutOfBoundsException if <code>offset</code>
     * and <code>length</code> do not specify
     * a valid range within the data array
     * @throws IllegalArgumentException if <code>data</code>
     * is illegal for the current 
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws IllegalArgumentException if the text would exceed the current
     * maximum capacity
     * @see #getChars
     */
    public void setChars(char[] data, int offset, int length) {
        textField.setChars(data, offset, length);
    }

    /**
     * Inserts a string into the contents of the <code>TextBox</code>.
     * The string is
     * inserted just prior to the character indicated by the
     * <code>position</code> parameter, where zero specifies the first
     * character of the contents of the <code>TextBox</code>.  If
     * <code>position</code> is
     * less than or equal to zero, the insertion occurs at the beginning of
     * the contents, thus effecting a prepend operation.  If
     * <code>position</code> is greater than or equal to the current size of
     * the contents, the insertion occurs immediately after the end of the
     * contents, thus effecting an append operation.  For example,
     * <code>text.insert(s, text.size())</code> always appends the string 
     * <code>s</code> to the current contents.
     * 
     * <p>The current size of the contents is increased by the number of
     * inserted characters. The resulting string must fit within the current
     * maximum capacity. </p>
     *
     * <p>If the application needs to simulate typing of characters it can
     * determining the location of the current insertion point
     * (&quot;caret&quot;)
     * using the with {@link #getCaretPosition() getCaretPosition()} method.  
     * For example,
     * <code>text.insert(s, text.getCaretPosition())</code> inserts the string 
     * <code>s</code> at the current caret position.</p>
     *
     * @param src the <code>String</code> to be inserted
     * @param position the position at which insertion is to occur
     * 
     * @throws IllegalArgumentException if the resulting contents
     * would be illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws IllegalArgumentException if the insertion would
     * exceed the current
     * maximum capacity
     * @throws NullPointerException if <code>src</code> is <code>null</code>
     */
    public void insert(String src, int position)  {
        textField.insert(src, position);
    }

    /**
     * Inserts a subrange of an array of characters into the contents of
     * the <code>TextBox</code>.  The <code>offset</code> and
     * <code>length</code> parameters indicate the subrange of
     * the data array to be used for insertion. Behavior is otherwise
     * identical to {@link #insert(String, int) insert(String, int)}. 
     *
     * <p>The <code>offset</code> and <code>length</code> parameters must
     * specify a valid range of characters within
     * the character array <code>data</code>.
     * The <code>offset</code> parameter must be within the
     * range <code>[0..(data.length)]</code>, inclusive.
     * The <code>length</code> parameter
     * must be a non-negative integer such that
     * <code>(offset + length) &lt;= data.length</code>.</p>
     * 
     * @param data the source of the character data
     * @param offset the beginning of the region of characters to copy
     * @param length the number of characters to copy
     * @param position the position at which insertion is to occur
     * 
     * @throws ArrayIndexOutOfBoundsException if <code>offset</code>
     * and <code>length</code> do not
     * specify a valid range within the data array
     * @throws IllegalArgumentException if the resulting contents
     * would be illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws IllegalArgumentException if the insertion would
     * exceed the current
     * maximum capacity
     * @throws NullPointerException if <code>data</code> is <code>null</code>
     */
    public void insert(char[] data, int offset, int length, int position)  {
        textField.insert(data, offset, length, position);
    }

    /**
     * Deletes characters from the <code>TextBox</code>.
     *
     * <p>The <code>offset</code> and <code>length</code> parameters must
     * specify a valid range of characters within
     * the contents of the <code>TextBox</code>.
     * The <code>offset</code> parameter must be within the
     * range <code>[0..(size())]</code>, inclusive.
     * The <code>length</code> parameter
     * must be a non-negative integer such that
     * <code>(offset + length) &lt;= size()</code>.</p>
     * 
     * @param offset the beginning of the region to be deleted
     * @param length the number of characters to be deleted
     *
     * @throws IllegalArgumentException if the resulting contents
     * would be illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws StringIndexOutOfBoundsException if <code>offset</code>
     * and <code>length</code> do not
     * specify a valid range within the contents of the <code>TextBox</code>
     */
    public void delete(int offset, int length) {
        textField.delete(offset, length);
    }

    /**
     * Returns the maximum size (number of characters) that can be
     * stored in this <code>TextBox</code>.
     * @return the maximum size in characters
     * @see #setMaxSize
     */
    public int getMaxSize() {
        return textField.getMaxSize();
    }

    /**
     * Sets the maximum size (number of characters) that can be
     * contained in this
     * <code>TextBox</code>. If the current contents of the
     * <code>TextBox</code> are larger than
     * <code>maxSize</code>, the contents are truncated to fit.
     *
     * @param maxSize the new maximum size
     *
     * @return assigned maximum capacity - may be smaller than requested.
     * @throws IllegalArgumentException if <code>maxSize</code> is zero or less.
     * @throws IllegalArgumentException if the contents
     * after truncation would be illegal for the current 
     * <a href="TextField.html#constraints">input constraints</a>
     * @see #getMaxSize
     */
    public int setMaxSize(int maxSize)  {
        return textField.setMaxSize(maxSize);
    }

    /**
     * Gets the number of characters that are currently stored in this
     * <code>TextBox</code>.
     * @return the number of characters
     */
    public int size() {
        // returns a value relative to the display text including formatting
        return textField.size();
    }

    /**
     * Gets the current input position.  For some UIs this may block and ask
     * the user for the intended caret position, and on other UIs this may
     * simply return the current caret position.
     * 
     * @return the current caret position, <code>0</code> if at the beginning
     */
    public int getCaretPosition() {
        // returns a value relative to the flat input text
        return textField.getCaretPosition();
    }

    /**
     * Sets the input constraints of the <code>TextBox</code>. If the
     * current contents
     * of the <code>TextBox</code> do not match the new constraints,
     * the contents are
     * set to empty.
     *
     * @param constraints see
     * <a href="TextField.html#constraints">input constraints</a>
     *
     * @throws IllegalArgumentException if the value of the constraints
     * parameter is invalid
     * @see #getConstraints
     */
    public void setConstraints(int constraints) {
        textField.setConstraints(constraints);
    }

    /**
     * Gets the current input constraints of the <code>TextBox</code>.
     *
     * @return the current constraints value (see
     * <a href="TextField.html#constraints">input constraints</a>)
     * @see #setConstraints
     */
    public int getConstraints() {
        return textField.getConstraints();
    }

    /**
     * Sets a hint to the implementation as to the input mode that should be
     * used when the user initiates editing of this
     * <code>TextBox</code>. The
     * <code>characterSubset</code> parameter names a subset of Unicode
     * characters that is used by the implementation to choose an initial
     * input mode.  If <code>null</code> is passed, the implementation should
     * choose a default input mode.
     *
     * <p>See <a href="TextField#modes">Input Modes</a> for a full 
     * explanation of input modes. </p>
     *
     * @param characterSubset a string naming a Unicode character subset,
     * or <code>null</code>
     *
     * @since MIDP 2.0
     */
    public void setInitialInputMode(String characterSubset) {
        textField.setInitialInputMode(characterSubset);
    }

    /**
     * Sets the title of the <code>Displayable</code>. If
     * <code>null</code> is given,
     * removes the title.
     *
     * <P>If the <code>Displayable</code> is actually visible on
     * the display,
     * the implementation should update
     * the display as soon as it is feasible to do so.</P>
     *
     * <P>The existence of a title  may affect the size
     * of the area available for <code>Displayable</code> content.
     * If the application adds, removes, or sets the title text at runtime,
     * this can dynamically change the size of the content area.
     * This is most important to be aware of when using the
     * <code>Canvas</code> class.</p>
     *
     * @param s the new title, or <code>null</code> for no title
     * @since MIDP 2.0
     * @see #getTitle
     */
    public void setTitle(String s) {
        super.setTitle(s);

        // We override this method from Displayable to set the title
        // on our internal Form which is doing all our rendering

        form.setTitle(s);
    }

    /**
     * Sets a ticker for use with this <code>Displayable</code>,
     * replacing any
     * previous ticker.
     * If <code>null</code>, removes the ticker object
     * from this <code>Displayable</code>. The same ticker may be shared by
     * several <code>Displayable</code>
     * objects within an application. This is done by calling
     * <code>setTicker()</code>
     * with the same <code>Ticker</code> object on several
     * different <code>Displayable</code> objects.
     * If the <code>Displayable</code> is actually visible on the display,
     * the implementation should update
     * the display as soon as it is feasible to do so.
     *
     * <p>The existence of a ticker may affect the size
     * of the area available for <code>Displayable's</code> contents.
     * If the application adds, removes, or sets the ticker text at runtime,
     * this can dynamically change the size of the content area.
     * This is most important to be aware of when using the
     * <code>Canvas</code> class.
     *
     * @param ticker the ticker object used on this screen
     * @since MIDP 2.0
     * @see #getTicker
     */
    public void setTicker(Ticker ticker) {
        super.setTicker(ticker);

        // We override this method from Displayable to set the ticker
        // on our internal Form which is doing all our rendering

        form.setTicker(ticker);
    }

    /*
     * package private
     */

    /**
     * Called to commit any pending user interaction for the textfield.
     * Override the no-op in Displayable.
     */
    void commitPendingInteraction() {
            textField.commitPendingInteraction();
    }

    /**
     * Notify this Displayable it is being shown on the given Display
     *
     * @param d the Display showing this Displayable
     */
    void callShowNotify(Display d) {
        super.callShowNotify(d);
        form.callShowNotify(d);
    }

    /**
     * Notify this Displayable it is being hidden on the given Display
     *
     * @param d the Display hiding this Displayable
     */
    void callHideNotify(Display d) {
        super.callHideNotify(d);
        form.callHideNotify(d);
    }

    /**
     * Called by the event thread to invalidate the contents
     * of this TextBox
     *
     * @param src the Item which may have caused the invalidation
     */
    void callInvalidate(Item src) {
        form.callInvalidate(src);
    }

    /**
     * Called by the event thread to notify an ItemStateChangeListener
     * that an item has changed
     *
     * @param src the Item which has changed
     */
    void callItemStateChanged(Item src) {
        form.callItemStateChanged(src);
    }

    /**
     * Handle a key pressed event
     *
     * @param keyCode The key that was pressed
     */
    void callKeyPressed(int keyCode) {
        form.callKeyPressed(keyCode);
    }

    /**
     * Handle a key released event
     *
     * @param keyCode The key that was pressed
     */
    void callKeyReleased(int keyCode) {
        form.callKeyReleased(keyCode);
    }


    /**
     * Handle a key repeated event
     *
     * @param keyCode The key that was keyRepeated
     */
    void callKeyRepeated(int keyCode) {
        form.callKeyRepeated(keyCode);
    }

    /**
     * Handle a key typed event
     *
     * @param keyCode The key that was pressed
     */
    void callKeyTyped(char keyCode) {
        form.callKeyTyped(keyCode);
    }

    /**
     * Paint the content of this TextBox
     *
     * @param g The Graphics object to paint to
     * @param target the target Object of this repaint
     */
    void callPaint(Graphics g, Object target) {
        super.callPaint(g, target);
        form.callPaint(g, target);
    }

} // class TextBox



