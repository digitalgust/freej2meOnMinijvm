/*
 * @(#)ChoiceGroup.java	1.211 02/10/15 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;
import com.sun.midp.lcdui.Text;
import com.sun.midp.lcdui.*;

/**
 * A <code>ChoiceGroup</code> is a group of selectable elements intended to be
 * placed within a
 * {@link Form}. The group may be created with a mode that requires a
 * single choice to be made or that allows multiple choices. The
 * implementation is responsible for providing the graphical representation of
 * these modes and must provide visually different graphics for different
 * modes. For example, it might use &quot;radio buttons&quot; for the
 * single choice
 * mode and &quot;check boxes&quot; for the multiple choice mode.
 *
 * <p> <strong>Note:</strong> most of the essential methods have been
 * specified in the {@link Choice Choice} interface.</p>
 * @since MIDP 1.0
 */

public class ChoiceGroup extends Item implements Choice {
    /**
     * If a List is using this ChoiceGroup, it will set isList to true
     */
    boolean isList;

    /**
     * The type of this ChoiceGroup
     */
    private int choiceType;

    /**
     * The string fit policy for this ChoiceGroup
     * '0' by default, which is Choice.TEXT_WRAP_DEFAULT
     */
    private int fitPolicy;
    /*
     * NOTE:  If this is a POPUP choice group, regardless of
     * the set fit policy the system will behave as though
     * fitPolicy == Choice.TEXT_WRAP_OFF.  Popup choice elements
     * will never wrap, and may be truncated.
     */

    /**
     * The number of elements in this ChoiceGroup
     */
    private int numOfEls;

    /**
     * The currently selected index of this ChoiceGroup (-1 by default)
     */
    private int selectedIndex = -1;

    /**
     * The currently highlighted index of this ChoiceGroup (-1 by default)
     */
    private int hilightedIndex = -1;

    /**
     * The array of selected elements of this ChoiceGroup (in the case
     * of a multi-select type)
     */
    private boolean selEls[];

    /**
     * The array containing the String parts of each element
     */
    private String[] stringEls;

    /**
     * The array containing the Image parts of each element (null unless
     * there are Image parts)
     */
    private Image[] imageEls;

    /**
     * The array containing mutable Image parts of each element (null unless
     * there are mutable Image parts)
     */
    private Image[] mutableImageEls;

    /**
     * The array containing the Font of each element (null if no setFont()
     * method was ever called). If fontEls is non-null, only the elements
     * which were set by setFont() are non-null.
     */
    private Font[] fontEls;

    /**
     * The array containing the individual heights of each element, based
     * on the preferred layout width
     */
    private int[] elHeights;

    /**
     * The cachedWidth is the width used to calculate the height of each
     * element. If a different width is passed into paint(), we need
     * to recalculate the heights of the elements.
     */
    private int cachedWidth;

    /**
     * By default, a ChoiceGroup is 80 pixels wide
     */
    private static final int DEFAULT_WIDTH = 80;

    /**
     * The state of the popup ChoiceGroup (false by default)
     */
    private boolean popUpOpen;

    /**
     * A flag indicating if traversal has occurred into this
     * CG on a prior callTraverse. Its reset to false again
     * in callTraverseOut().
     */
    private boolean traversedIn;

    /**
     * The DisplayManager object handling the display events.
     * Used to suspend/resume display updates when an open
     * popup choice group is on screen.
     */
    private DisplayManager displayManager;

    /**
     * Max width of requested popup menu
     */
    private int maxPopupWidth = DEFAULT_WIDTH;

    /**
     * Creates a new, empty <code>ChoiceGroup</code>, specifying its
     * title and its type.
     * The type must be one of <code>EXCLUSIVE</code>,
     * <code>MULTIPLE</code>, or <code>POPUP</code>. The
     * <code>IMPLICIT</code>
     * choice type is not allowed within a <code>ChoiceGroup</code>.
     *
     * @param label the item's label (see {@link Item Item})
     * @param choiceType <code>EXCLUSIVE</code>, <code>MULTIPLE</code>, 
     * or <code>POPUP</code>
     * @throws IllegalArgumentException if <code>choiceType</code> is not one of
     * <code>EXCLUSIVE</code>, <code>MULTIPLE</code>, or <code>POPUP</code>
     * @see Choice#EXCLUSIVE
     * @see Choice#MULTIPLE
     * @see Choice#IMPLICIT
     * @see Choice#POPUP
     */
    public ChoiceGroup(String label, int choiceType) {
        this(label, choiceType, new String[] {}, null);
    }
    
    /**
     * Creates a new <code>ChoiceGroup</code>, specifying its title,
     * the type of the
     * <code>ChoiceGroup</code>, and an array of <code>Strings</code>
     * and <code>Images</code> to be used as its
     * initial contents. 
     *
     * <p>The type must be one of <code>EXCLUSIVE</code>,
     * <code>MULTIPLE</code>, or <code>POPUP</code>.  The
     * <code>IMPLICIT</code>
     * type is not allowed for <code>ChoiceGroup</code>.</p>
     *
     * <p>The <code>stringElements</code> array must be non-null and
     * every array element
     * must also be non-null.  The length of the
     * <code>stringElements</code> array
     * determines the number of elements in the <code>ChoiceGroup</code>.  The 
     * <code>imageElements</code> array
     * may be <code>null</code> to indicate that the
     * <code>ChoiceGroup</code> elements have no images.
     * If the
     * <code>imageElements</code> array is non-null, it must be the
     * same length as the
     * <code>stringElements</code> array.  Individual elements of the
     * <code>imageElements</code> array
     * may be <code>null</code> in order to indicate the absence of an
     * image for the
     * corresponding <code>ChoiceGroup</code> element.  Non-null elements
     * of the
     * <code>imageElements</code> array may refer to mutable or
     * immutable images.</p>
     * 
     * @param label the item's label (see {@link Item Item})
     * @param choiceType <code>EXCLUSIVE</code>, <code>MULTIPLE</code>,
     * or <code>POPUP</code>
     * @param stringElements set of strings specifying the string parts of the 
     * <code>ChoiceGroup</code> elements
     * @param imageElements set of images specifying the image parts of
     * the <code>ChoiceGroup</code> elements
     *
     * @throws NullPointerException if <code>stringElements</code> 
     * is <code>null</code>
     * @throws NullPointerException if the <code>stringElements</code>
     * array contains
     * any <code>null</code> elements
     * @throws IllegalArgumentException if the <code>imageElements</code>
     * array is non-null 
     * and has a different length from the <code>stringElements</code> array
     * @throws IllegalArgumentException if <code>choiceType</code> is not one of
     * <code>EXCLUSIVE</code>, <code>MULTIPLE</code>, or <code>POPUP</code>
     * 
     * @see Choice#EXCLUSIVE
     * @see Choice#MULTIPLE
     * @see Choice#IMPLICIT
     * @see Choice#POPUP
     */
    public ChoiceGroup(String label, int choiceType, 
                       String[] stringElements, Image[] imageElements) {

        this(label, choiceType, stringElements, imageElements, false);
    }
    
    /**
     * Special constructor used by List
     *
     * @param label the item's label (see {@link Item Item})
     * @param choiceType EXCLUSIVE or MULTIPLE
     * @param stringElements set of strings specifying the string parts of the
     * ChoiceGroup elements
     * @param imageElements set of images specifying the image parts of
     * the ChoiceGroup elements
     * @param implicitAllowed Flag to allow implicit selection
     *
     * @throws NullPointerException if stringElements is null
     * @throws NullPointerException if the stringElements array contains
     * any null elements
     * @throws IllegalArgumentException if the imageElements array is non-null
     * and has a different length from the stringElements array
     * @throws IllegalArgumentException if choiceType is neither
     * EXCLUSIVE nor MULTIPLE
     * @throws IllegalArgumentException if any image in the imageElements
     * array is mutable
     *
     * @see Choice#EXCLUSIVE
     * @see Choice#MULTIPLE
     * @see Choice#IMPLICIT
     */
    ChoiceGroup(String label, int choiceType, String[] stringElements,
            Image[] imageElements, boolean implicitAllowed) {

        super(label);
        
        if (displayManager == null) {
            displayManager = DisplayManagerFactory.getDisplayManager();
        }

        if (!((choiceType == Choice.MULTIPLE) ||
                (choiceType == Choice.EXCLUSIVE) ||
                ((choiceType == Choice.IMPLICIT) && implicitAllowed) ||
                (choiceType == Choice.POPUP))) {
            throw new IllegalArgumentException();
        }

        // If stringElements is null NullPointerException will be thrown
        // as expected
        for (int x = 0; x < stringElements.length; x++) {
            if (stringElements[x] == null) {
                throw new NullPointerException();
            }
        }

        if (imageElements != null) {
            if (stringElements.length != imageElements.length) {
                throw new IllegalArgumentException();
            }
        }

        synchronized (Display.LCDUILock) {
            this.choiceType = choiceType;
            numOfEls = stringElements.length;

            switch (choiceType) {
                case Choice.MULTIPLE:
                    selEls = new boolean[numOfEls];
                    for (int i = 0; i < numOfEls; i++) {
                        selEls[i] = false;
                    }
                    break;
                case Choice.POPUP:
                case Choice.IMPLICIT:
                case Choice.EXCLUSIVE:
                    if (numOfEls > 0) {
                        selectedIndex = 0;
                    }
                    break;
            }

            stringEls = new String[numOfEls];
            System.arraycopy(stringElements, 0, stringEls, 0, numOfEls);

            if (imageElements != null) {
                imageEls        = new Image[numOfEls];
                mutableImageEls = new Image[numOfEls];

                // Need to check each and every Image to see if it's mutable
                for (int i = 0; i < numOfEls; i++) {
                    if (imageElements[i] != null &&
                        imageElements[i].isMutable()) {
                        // Save original, mutable Image
                        mutableImageEls[i] = imageElements[i];
                        // Create a snapshot for display
                        imageEls[i] = Image.createImage(imageElements[i]);
                    } else {
                        // Save the immutable image for display
                        imageEls[i] = imageElements[i];
                    }
                }
            }
            hilightedIndex = 0;
        } // synchronized
    }

    /**
     * Returns the number of elements in the <code>ChoiceGroup</code>.
     * @return the number of elements in the <code>ChoiceGroup</code>
     */
    public int size() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return numOfEls;
    }

    /**
     * Gets the <code>String</code> part of the element referenced by
     * <code>elementNum</code>.
     * 
     * @param elementNum the index of the element to be queried
     * @return the string part of the element
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getImage(int)
     */
    public String getString(int elementNum) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);
            return stringEls[elementNum];
        }
    }

    /**
     * Gets the <code>Image</code> part of the element referenced by
     * <code>elementNum</code>.
     *
     * @param elementNum the number of the element to be queried
     * @return the image part of the element, or null if there is no image
     * @throws IndexOutOfBoundsException if elementNum is invalid
     * @see #getString(int)
     */
    public Image getImage(int elementNum) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);
            if (imageEls != null) {
                if (mutableImageEls[elementNum] != null) {
                    // This element has a mutable Image
                    return mutableImageEls[elementNum];
                }
                // This element does not have a mutable Image
                return imageEls[elementNum];
            }
            // This element does not have any Image
            return null;
        }
    }

    /**
     * Appends an element to the <code>ChoiceGroup</code>.
     * 
     * @param stringPart the string part of the element to be added
     * @param imagePart the image part of the element to be added, or
     * <code>null</code> if there is no image part
     * @return the assigned index of the element
     * @throws NullPointerException if <code>stringPart</code> is
     * <code>null</code>
     */
    public int append(String stringPart, Image imagePart) {
        int returnVal = -1;

        synchronized (Display.LCDUILock) {
            checkNull(stringPart, imagePart);
            returnVal = insertImpl(numOfEls, stringPart, imagePart);
        }
        return returnVal;
    }

    /**
     * Inserts an element into the <code>ChoiceGroup</code> just prior to
     * the element specified.
     * 
     * @param elementNum the index of the element where insertion is to occur
     * @param stringPart the string part of the element to be inserted
     * @param imagePart the image part of the element to be inserted,
     * or <code>null</code> if there is no image part
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @throws NullPointerException if <code>stringPart</code>
     * is <code>null</code>
     */
    public void insert(int elementNum, String stringPart,
                       Image imagePart) {
        
        synchronized (Display.LCDUILock) {
            if (elementNum < 0 || elementNum > numOfEls) {
                throw new IndexOutOfBoundsException();
            }
            checkNull(stringPart, imagePart);
            insertImpl(elementNum, stringPart, imagePart);
        }
    }

    /**
     * Deletes the element referenced by <code>elementNum</code>.
     * 
     * @param elementNum the index of the element to be deleted
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     */
    public void delete(int elementNum) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);

            if (elementNum != numOfEls - 1) {

                System.arraycopy(stringEls, elementNum + 1, stringEls,
                                 elementNum, numOfEls - elementNum - 1);

                if (imageEls != null) {
                    // Delete Image snapshot
                    System.arraycopy(imageEls, elementNum + 1, imageEls,
                                     elementNum, numOfEls - elementNum - 1);
                    // Delete mutable Image
                    System.arraycopy(mutableImageEls, elementNum + 1,
                                     mutableImageEls, elementNum,
                                     numOfEls - elementNum - 1);
                }

                if (fontEls != null) {
                    System.arraycopy(fontEls, elementNum + 1, fontEls,
                                     elementNum, numOfEls - elementNum - 1);
                }

                if (choiceType == ChoiceGroup.MULTIPLE) {
                    System.arraycopy(selEls, elementNum + 1, selEls,
                                     elementNum, numOfEls - elementNum - 1);
                }
            }

            if (choiceType == ChoiceGroup.MULTIPLE) {
                selEls[numOfEls - 1] = false;
            }

            --numOfEls;

            stringEls[numOfEls] = null;
            if (imageEls != null) {
                imageEls[numOfEls] = null;
                mutableImageEls[numOfEls] = null;
            }

            if (fontEls != null) {
                fontEls[numOfEls] = null;
            }

            // layouts[--numOfEls] = null;

            if (numOfEls == 0) {
                hilightedIndex = selectedIndex = -1;
            } else {
                // adjust hilighted index
                if (elementNum < hilightedIndex) {
                    hilightedIndex--;
                } else if (elementNum == hilightedIndex &&
                        hilightedIndex == numOfEls) {
                    hilightedIndex = numOfEls - 1;
                }

                // adjust selected index if choiceGroup is not MULTIPLE
                if (choiceType != ChoiceGroup.MULTIPLE) {
                    if (elementNum < selectedIndex) {
                        selectedIndex--;
                    } else if (elementNum == selectedIndex &&
                            selectedIndex == numOfEls) {
                        selectedIndex = numOfEls - 1;
                    }
                }
            }
            invalidate();
        } // synchronized
    }

    /**
     * Deletes all elements from this <code>ChoiceGroup</code>.
     */
    public void deleteAll() {
        synchronized (Display.LCDUILock) {
            for (int x = 0; x < numOfEls; x++) {
                stringEls[x] = null;
                if (imageEls != null) {
                    imageEls[x] = null;
                    mutableImageEls[x] = null;
                }
                if (fontEls != null) {
                    fontEls[x] = null;
                }
            }

            numOfEls = 0;
            hilightedIndex = selectedIndex = -1;
            invalidate();
        }
    }

    /**
     * Sets the <code>String</code> and <code>Image</code> parts of the
     * element referenced by <code>elementNum</code>,
     * replacing the previous contents of the element.
     * 
     * @param elementNum the index of the element to be set
     * @param stringPart the string part of the new element
     * @param imagePart the image part of the element, or <code>null</code>
     * if there is no image part
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @throws NullPointerException if <code>stringPart</code> is
     * <code>null</code>
     */
    public void set(int elementNum, String stringPart, Image imagePart) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);
            checkNull(stringPart, imagePart);
            setImpl(elementNum, stringPart, imagePart);
        }
    }

    /**
     * Gets a boolean value indicating whether this element is selected.
     * 
     * @param elementNum the index of the element to be queried
     *
     * @return selection state of the element
     * 
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     */
    public boolean isSelected(int elementNum) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);
            return (choiceType == Choice.MULTIPLE ? selEls[elementNum] :
                                            (selectedIndex == elementNum));
        }
    }

    /**
     * Returns the index number of an element in the
     * <code>ChoiceGroup</code> that is
     * selected. For <code>ChoiceGroup</code> objects of type
     * <code>EXCLUSIVE</code> and <code>POPUP</code>
     * there is at most one element selected, so
     * this method is useful for determining the user's choice.
     * Returns <code>-1</code> if
     * there are no elements in the <code>ChoiceGroup</code>. 
     *
     * <p>For <code>ChoiceGroup</code> objects of type
     * <code>MULTIPLE</code>, this always
     * returns <code>-1</code> because no
     * single value can in general represent the state of such a
     * <code>ChoiceGroup</code>.
     * To get the complete state of a <code>MULTIPLE</code>
     * <code>Choice</code>, see {@link
     * #getSelectedFlags getSelectedFlags}.</p>
     *
     * @return index of selected element, or <code>-1</code> if none
     * @see #setSelectedIndex
     */
    public int getSelectedIndex() { 
        // SYNC NOTE: return of atomic value, no locking necessary
        return selectedIndex;
    }

    /**
     * Queries the state of a <code>ChoiceGroup</code> and returns the state of 
     * all elements in the
     * boolean array
     * <code>selectedArray_return</code>. <strong>Note:</strong> this
     * is a result parameter.
     * It must be at least as long as the size
     * of the <code>ChoiceGroup</code> as returned by <code>size()</code>.
     * If the array is longer, the extra
     * elements are set to <code>false</code>.
     * 
     * <p>For <code>ChoiceGroup</code> objects of type
     * <code>MULTIPLE</code>, any
     * number of elements may be selected and set to true in the result
     * array.  For <code>ChoiceGroup</code> objects of type
     * <code>EXCLUSIVE</code> and <code>POPUP</code>
     * exactly one element will be selected, unless there are
     * zero elements in the <code>ChoiceGroup</code>. </p>
     *
     * @return the number of selected elements in the <code>ChoiceGroup</code>
     *
     * @param selectedArray_return array to contain the results
     * @throws IllegalArgumentException if <code>selectedArray_return</code>
     * is shorter than the size of the <code>ChoiceGroup</code>
     * @throws NullPointerException if <code>selectedArray_return</code>
     * is null
     * @see #setSelectedFlags
     */
    public int getSelectedFlags(boolean[] selectedArray_return) {
        synchronized (Display.LCDUILock) {
            checkFlag(selectedArray_return);
            int selectedNum = 0;
            if (choiceType == Choice.MULTIPLE) {
                System.arraycopy(selEls, 0, selectedArray_return, 0, numOfEls);
                for (int i = 0; i < numOfEls; i++) {
                    if (selEls[i]) selectedNum++;
                }
                for (int i = numOfEls; i < selectedArray_return.length; i++) {
                    selectedArray_return[i] = false;
                }
            } else {
                for (int i = 0; i < selectedArray_return.length; i++) {
                    selectedArray_return[i] = false;
                }
                if (selectedIndex != -1) {
                    selectedArray_return[selectedIndex] = true;
                    selectedNum = 1;
                }
            }
            return selectedNum;
        }
    }

    /**
     * For <code>ChoiceGroup</code> objects of type
     * <code>MULTIPLE</code>, this simply sets an
     * individual element's selected state. 
     *
     * <P>For <code>ChoiceGroup</code> objects of type
     * <code>EXCLUSIVE</code> and <code>POPUP</code>, this can be used only to
     * select an element.  That is, the <code> selected </code> parameter must
     * be <code> true </code>. When an element is selected, the previously
     * selected element is deselected. If <code> selected </code> is <code>
     * false </code>, this call is ignored.</P>
     *
     * <p>For both list types, the <code>elementNum</code> parameter
     * must be within
     * the range
     * <code>[0..size()-1]</code>, inclusive. </p>
     * 
     * @param elementNum the number of the element. Indexing of the 
     * elements is zero-based
     * @param selected the new state of the element <code>true=selected</code>, 
     * <code>false=not</code> selected
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getSelectedIndex
     */
    public void setSelectedIndex(int elementNum, boolean selected) {
        synchronized (Display.LCDUILock) {
            setSelectedIndexImpl(elementNum, selected);
        } // synchronized
    }

    /**
     * Attempts to set the selected state of every element in the 
     * <code>ChoiceGroup</code>. The array
     * must be at least as long as the size of the
     * <code>ChoiceGroup</code>. If the array is
     * longer, the additional values are ignored. <p>
     *
     * For <code>ChoiceGroup</code> objects of type
     * <code>MULTIPLE</code>, this sets the selected
     * state of every
     * element in the <code>Choice</code>. An arbitrary number of
     * elements may be selected.
     * <p>
     *
     * For <code>ChoiceGroup</code> objects of type
     * <code>EXCLUSIVE</code> and <code>POPUP</code>, exactly one array
     * element must have the value <code>true</code>. If no element is
     * <code>true</code>,
     * the first element
     * in the <code>Choice</code> will be selected. If two or more
     * elements are <code>true</code>, the
     * implementation will choose the first <code>true</code> element
     * and select it. <p>
     *
     * @param selectedArray an array in which the method collect the 
     * selection status
     * @throws IllegalArgumentException if <code>selectedArray</code> 
     * is shorter than the size of the <code>ChoiceGroup</code>
     * @throws NullPointerException if the <code>selectedArray</code> 
     * is <code>null</code>
     * @see #getSelectedFlags
     */
    public void setSelectedFlags(boolean[] selectedArray) {
        synchronized (Display.LCDUILock) {
            checkFlag(selectedArray);
            if (numOfEls == 0) {
                return;
            }
            if (choiceType == Choice.MULTIPLE) {
                System.arraycopy(selectedArray, 0, selEls, 0, numOfEls);
            } else {
                int i = 0;
                for (; i < numOfEls; i++) {
                    if (selectedArray[i]) {
                        break;
                    }
                }
                if (i == numOfEls) {
                    i = 0;
                }
                setSelectedIndexImpl(i, true);
            }
            repaint();
        } // synchronized
    }

    /**
     * Sets the application's preferred policy for fitting
     * <code>Choice</code> element
     * contents to the available screen space. The set policy applies for all
     * elements of the <code>Choice</code> object.  Valid values are
     * {@link #TEXT_WRAP_DEFAULT}, {@link #TEXT_WRAP_ON},
     * and {@link #TEXT_WRAP_OFF}. Fit policy is a hint, and the
     * implementation may disregard the application's preferred policy.
     *
     * @param fitPolicy preferred content fit policy for choice elements
     * @throws IllegalArgumentException if <code>fitPolicy</code> is invalid
     * @see #getFitPolicy
     * @since MIDP 2.0
     */
    public void setFitPolicy(int fitPolicy) {
        if (fitPolicy < TEXT_WRAP_DEFAULT || fitPolicy > TEXT_WRAP_OFF) {
            throw new IllegalArgumentException();
        }
        synchronized (Display.LCDUILock) {
            if (this.fitPolicy != fitPolicy) {
                this.fitPolicy = fitPolicy;
                invalidate();
            }
        }
    }

    /**
     * Gets the application's preferred policy for fitting
     * <code>Choice</code> element
     * contents to the available screen space.  The value returned is the 
     * policy that had been set by the application, even if that value had 
     * been disregarded by the implementation.
     *
     * @return one of {@link #TEXT_WRAP_DEFAULT}, {@link #TEXT_WRAP_ON}, or
     * {@link #TEXT_WRAP_OFF}
     * @see #setFitPolicy
     * @since MIDP 2.0
     */
    public int getFitPolicy() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return fitPolicy;
    }

    /**
     * Sets the application's preferred font for
     * rendering the specified element of this <code>Choice</code>.
     * An element's font is a hint, and the implementation may disregard
     * the application's preferred font.
     *
     * <p> The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()-1]</code>, inclusive.</p>
     *
     * <p> The <code>font</code> parameter must be a valid <code>Font</code>
     * object or <code>null</code>. If the <code>font</code> parameter is
     * <code>null</code>, the implementation must use its default font
     * to render the element.</p>
     *
     * @param elementNum the index of the element, starting from zero
     * @param font the preferred font to use to render the element
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #getFont
     * @since MIDP 2.0
     */
    public void setFont(int elementNum, Font font) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);
            if (fontEls == null) {
                fontEls = new Font[numOfEls];
            }
            fontEls[elementNum] = font;
        }
    }

    /**
     * Gets the application's preferred font for
     * rendering the specified element of this <code>Choice</code>. The
     * value returned is the font that had been set by the application,
     * even if that value had been disregarded by the implementation.
     * If no font had been set by the application, or if the application
     * explicitly set the font to <code>null</code>, the value is the default
     * font chosen by the implementation.
     *
     * <p> The <code>elementNum</code> parameter must be within the range
     * <code>[0..size()-1]</code>, inclusive.</p>
     *
     * @param elementNum the index of the element, starting from zero
     * @return the preferred font to use to render the element
     * @throws IndexOutOfBoundsException if <code>elementNum</code> is invalid
     * @see #setFont(int elementNum, Font font)
     * @since MIDP 2.0
     */
    public Font getFont(int elementNum) {
        synchronized (Display.LCDUILock) {
            checkIndex(elementNum);
            if (fontEls != null && fontEls[elementNum] != null) {
                return fontEls[elementNum];
            } else {
                return Screen.CONTENT_FONT;
            }
        }
    }

// ***********************************************************
//  package private
// ***********************************************************

    /**
     * Determine if this Item should have a newline after it
     *
     * @return true if it should have a newline after
     */
    boolean equateNLA() {
        if (super.equateNLA()) {
            return true;
        }
        return ((layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }
               

    /**
     * Determine if this Item should have a newline before it
     *
     * @return true if it should have a newline before
     */
    boolean equateNLB() {
        if (super.equateNLB()) {
            return true;
        }

        return ((layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }

    /**
     * Get the minimum width of this Item
     *
     * @return the minimum width
     */
    int callMinimumWidth() {

        int lW = getLabelWidth();
	
        if (lW == 0 && numOfEls == 0) {
            return 0;
        }
	
        if ((layout == Item.LAYOUT_DEFAULT) || (equateNLB() && equateNLA())) {
            if (owner != null) {
                return ((Form)owner).getWidth();
            }
            return Item.DEFAULT_WIDTH;
        }
	
        // Find the widest element and then base our width on that
        int textOffset = 0;
        int w = 0;
        int maxElWidth = 0;
        Font fnt;
	
        int grWidth = 0;
        if (choiceType == Choice.EXCLUSIVE) {
            grWidth = RD_WIDTH + LABEL_PAD;
        } else if (choiceType == Choice.MULTIPLE) {
            grWidth = CKBX_WIDTH + LABEL_PAD;
        }
       
        for (int i = 0; i < numOfEls; i++) {
            w = (imageEls == null || imageEls[i] == null) ? 
                 0 : PREFERRED_IMG_W + LABEL_PAD;

            if (stringEls[i] != null && stringEls[i].length() > 0) {

                textOffset = w;

                if (fontEls != null && fontEls[i] != null) {
                    fnt = fontEls[i];
                } else {
                    fnt = Screen.CONTENT_FONT;
                }

		if (choiceType != Choice.POPUP) {
		    w = Text.getWidestLineWidth(stringEls[i].toCharArray(), 
						textOffset, 
						DEFAULT_WIDTH - grWidth, 
						fnt);
		} else {
		    w = Text.getWidestLineWidth(stringEls[i].toCharArray(),
						textOffset, 
						DEFAULT_WIDTH * 2, fnt);
		}			

                if (w > maxElWidth) {
                    maxElWidth = w;
                }
            }
        }
        
        w = lW;

        if (choiceType == Choice.POPUP) {

            if (w > 0) {
                w += LABEL_PAD;
            }

            w += POPUP_AR_WIDTH + LABEL_PAD + maxElWidth;

	    maxPopupWidth = maxElWidth; 
            return (w > Item.DEFAULT_WIDTH ? Item.DEFAULT_WIDTH : w);
        } 

        if (w  < grWidth + maxElWidth) {
            // minimum required width is the maximum of the label width and
            // of the widest element width
            w = grWidth + maxElWidth;
        }
        return (w < DEFAULT_WIDTH ? w : DEFAULT_WIDTH);
    }

    /**
     * Get the preferred width of this Item
     *
     * @param h the desired height for this CG
     * @return the preferred width
     */
    int callPreferredWidth(int h) {
        // If we're a List, we want to be as wide as possible
        if (isList) {
            return 500;
        }

        return callMinimumWidth();
    }

    /**
     * Get the minimum height of this Item
     *
     * @return the minimum height
     */
    int callMinimumHeight() {
        return callPreferredHeight(callMinimumWidth());
    }

    /**
     * Get the preferred height of this Item
     *
     * @param width the desired width for this CG
     * @return the preferred height
     */
    int callPreferredHeight(int width) {
        if (width == -1) {
            width = callPreferredWidth(-1);
        }

        // empty ChoiceGroup is not shown
        if (width == 0) {
            return 0;
        }

        int lH = getLabelHeight(width);

        if (choiceType == Choice.EXCLUSIVE) {
            width -= (RD_WIDTH + LABEL_PAD);
        } else if (choiceType == Choice.MULTIPLE) {
            width -= (CKBX_WIDTH + LABEL_PAD);
        }

        int eHeight = calculateElementHeight(width);

        if (lH == 0) {
            // empty ChoiceGroup is not shown
            if (eHeight == 0) {
                return 0;
            }
        } else {
            lH += LABEL_PAD;
        }

        if (choiceType == Choice.POPUP) {
            if (eHeight > 0) {
                eHeight = elHeights[selectedIndex == -1 ? 0 : selectedIndex];
            }
             
            if (lH <= LABEL_HEIGHT) {
                // single line label
                return (eHeight > LABEL_HEIGHT - LABEL_PAD ? 
                        eHeight : LABEL_HEIGHT);
            }
        }
        return lH + eHeight;
    }

    /**
     * Paint this ChoiceGroup
     *
     * @param g the Graphics to paint to
     * @param w the width to paint
     * @param h the height to paint
     */
    void callPaint(Graphics g, int w, int h) {
        int labelHeight = super.paintLabel(g, w);

        // If its an empty ChoiceGroup, just return
        if (numOfEls == 0 && choiceType != Choice.POPUP) {
            return;
        }
        int translatedX = 0;
        int translatedY = 0;

        if (choiceType == Choice.POPUP) {
            // paint closed state of the popup
            
            if (labelHeight > LABEL_HEIGHT) {
                // translatedX = 0;
                translatedY = labelHeight;
            } else {
                translatedX = getLabelWidth();
                translatedX = (translatedX > 0) ? translatedX + LABEL_PAD : 0;
                // translatedY = 0;
            }

            g.drawImage(POPUP_ARROW_IMG, translatedX, translatedY,
                        Graphics.LEFT | Graphics.TOP);

            if (numOfEls == 0) {
                return;
            }

            translatedX += (POPUP_AR_WIDTH + LABEL_PAD);

            if (imageEls != null && imageEls[selectedIndex] != null) {
                int iX = g.getClipX();
                int iY = g.getClipY();
                int iW = g.getClipWidth();
                int iH = g.getClipHeight();

                g.clipRect(translatedX, translatedY, 
                             PREFERRED_IMG_W, PREFERRED_IMG_H);
                g.drawImage(imageEls[selectedIndex], translatedX, translatedY,
                            Graphics.LEFT | Graphics.TOP);
                g.setClip(iX, iY, iW, iH);
            }

            int elWidth = w;
            if (elWidth != cachedWidth) {
                calculateElementHeight(elWidth);
            }
            elWidth -= POPUP_AR_WIDTH + LABEL_PAD;

            Font fnt;
            if (fontEls != null && fontEls[selectedIndex] != null) {
                fnt = fontEls[selectedIndex];
            } else {
                fnt = Screen.CONTENT_FONT;
            }

            g.translate(translatedX, translatedY);

            int textOffset = 0;
            if (imageEls != null && imageEls[selectedIndex] != null) {
                textOffset = PREFERRED_IMG_W + LABEL_PAD;              
            }

            if (hasFocus) {
                // draw the hilight after drawing the label
                g.fillRect(textOffset, 0,
                           g.getClipWidth() - textOffset,
                           elHeights[selectedIndex]);
                // If there was an offset, we need to fill in the 
                // hilight box under the element's image 

                if (textOffset != 0 && elHeights[selectedIndex] > textOffset) {
                    g.fillRect(0, textOffset, textOffset, 
                               elHeights[selectedIndex] - textOffset); 
                } 
                Text.paint(stringEls[selectedIndex],
                           fnt, g, elWidth,
                           elHeights[selectedIndex], textOffset,
                           (Text.INVERT | Text.TRUNCATE), null);
            } else {
                Text.paint(stringEls[selectedIndex],
                           fnt, g, elWidth,
                           elHeights[selectedIndex], textOffset, 
                           (Text.NORMAL | Text.TRUNCATE), null);
            }
            g.translate(-translatedX, -translatedY);
        } else {
            translatedY = labelHeight;
            if (labelHeight > 0) {
                translatedY += LABEL_PAD;
            }
            g.translate(0, translatedY);
            paintElements(g, w);
            g.translate(0, -translatedY);
        }
    }

    /**
     * Handle traversal within this ChoiceGroup
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the viewport
     * @param viewportHeight the height of the viewport
     * @param visRect the in/out rectangle for the internal traversal location
     * @return True if traversal occurred within this ChoiceGroup
     */
    boolean callTraverse(int dir, int viewportWidth, int viewportHeight,
                         int[] visRect) {

        super.callTraverse(dir, viewportWidth, viewportHeight, visRect);

        // If we have no elements, or if the user pressed left/right,
        // don't bother with the visRect and just return false
        if (numOfEls == 0) {
            return false;
        }

        // If we are a closed popup, don't bother with the visRect
        // and return true on the initial traverse, false on subsequent
        // traverses
        if ((choiceType == Choice.POPUP) && !popUpOpen) {
            if (!traversedIn) {
                traversedIn = true;
                return true;
            } else {
                return false;
            }
        }

        int lh = getLabelHeight(visRect[WIDTH]);
        visRect[Y] = (lh > 0) ? lh + LABEL_PAD : 0;
        for (int i = 0; i < hilightedIndex; i++) {
            visRect[Y] += elHeights[i];
        }
        visRect[HEIGHT] = elHeights[hilightedIndex];

        if (!traversedIn) {
            traversedIn = true;
        } else {
            if (dir == Canvas.UP) {
                if (hilightedIndex > 0) {
                    hilightedIndex--;
                    visRect[Y] -= elHeights[hilightedIndex];
                } else {
                    return popUpOpen;
                }
            } else if (dir == Canvas.DOWN) {
                if (hilightedIndex < (numOfEls - 1)) {
                    visRect[Y] += elHeights[hilightedIndex];
                    hilightedIndex++;
                } else {
                    return popUpOpen;
                }
            } else {
                return popUpOpen;
            }
        }
        visRect[HEIGHT] = elHeights[hilightedIndex];
        if (choiceType == Choice.IMPLICIT) {
            selectedIndex = hilightedIndex;
            // FIX ME: notify listener
        }
        repaint();
        return true;
    }

    /**
     * Traverse out of this ChoiceGroup
     */
    void callTraverseOut() {
        super.callTraverseOut();
        traversedIn = false;
    }

    /**
     * Handle a key press event
     *
     * @param keyCode the key which was pressed
     */
    void callKeyPressed(int keyCode) {
        if (keyCode != Display.KEYCODE_SELECT
            || numOfEls == 0) {
            return;
        }
        switch (choiceType) {
        case Choice.POPUP:
            if (!popUpOpen) {
                displayManager.suspendPainting();
                int lW = getLabelWidth();
                boolean tickerflag = false;
                boolean titleflag = false;
                if (owner.getTicker() != null) {
                    tickerflag = true;
                }
                if (owner.getTitle() != null) {
                    titleflag = true;
                }
                updatePopupElements(stringEls, imageEls, numOfEls,
                                    selectedIndex,
                                    bounds[X] + owner.viewport[X] - 
                                    owner.view[X] + lW,
                                    bounds[Y] + owner.viewport[Y] -
                                    owner.view[Y], 
                                    owner.viewport[WIDTH],
                                    owner.viewport[HEIGHT], 
				    maxPopupWidth,
                                    tickerflag, titleflag);
                popUpOpen = !popUpOpen;
            } else {
                displayManager.resumePainting();
                popUpOpen = !popUpOpen;
                int selected = getPopupSelection();
                if (selected >= 0) {
                    hilightedIndex = selected;
                    setSelectedIndexImpl(hilightedIndex, true);
                    owner.itemStateChanged(this);
                }
                invalidate();
            }
            break;
        case Choice.EXCLUSIVE:
            if (hilightedIndex == selectedIndex) {
                return;
            }
            setSelectedIndexImpl(hilightedIndex, true);
            owner.itemStateChanged(this);
            break;
        case Choice.MULTIPLE:
            setSelectedIndexImpl(hilightedIndex, !selEls[hilightedIndex]);
            owner.itemStateChanged(this);
            break;
        default:
            break;
        }
    }

    /**
     * Get the type of this ChoiceGroup
     *
     * @return The type of this ChoiceGroup, ie IMPLICIT, EXPLICIT, etc.
     */
    int getType() {
        return choiceType;
    }

    /**
     * Set the selected state of the given element index
     *
     * @param elementNum the index of the element to select
     * @param selected true if the element should be selected
     */
    void setSelectedIndexImpl(int elementNum, boolean selected) {
        checkIndex(elementNum);

        switch (choiceType) {
            case Choice.IMPLICIT:
                // hilight changes as a result of selection only
                // if it is an implicit choice
                if (!selected) {
                    return;
                }
                selectedIndex = elementNum;
                hilightedIndex = elementNum;
                break;
            case Choice.POPUP:
                /* fall through */
            case Choice.EXCLUSIVE:
                // selected item cannot be deselected
                if (selectedIndex == elementNum || !selected) {
                    return;
                }
                selectedIndex = elementNum;
                break;
            case Choice.MULTIPLE:
                selEls[elementNum] = selected;
                break;
        }
        repaint();
    }

    /**
     * Determine if Form should not traverse to this ChoiceGroup
     *
     * @return true if Form should not traverse to this ChoiceGroup
     */
    boolean shouldSkipTraverse() {
        if ((label == null || label.equals("")) && (numOfEls == 0)) {
            return true;
        }
        return false;
    }

// ***********************************************************
//  private
// ***********************************************************

    /**
     * Set a particular element of this ChoiceGroup
     *
     * @param elementNum The index of the element to establish
     * @param stringPart The string part of the element to establish
     * @param imagePart The image part of the element to establish
     */
    private void setImpl(int elementNum,
                         String stringPart, Image imagePart) {

        stringEls[elementNum] = stringPart;

        // Create Image storage, if needed
        if ((imagePart != null) && (imageEls == null)) {
            imageEls        = new Image[stringEls.length];
            mutableImageEls = new Image[stringEls.length];
        }

        if (imageEls != null) {
            if ((imagePart != null) && imagePart.isMutable()) {
                // Adding a mutable Image; save it and create a
                // snapshot for display purposes
                mutableImageEls[elementNum] = imagePart;
                imageEls[elementNum] = Image.createImage(imagePart);
            } else {
                // We get here if imagePart is 'null' or if imagePart
                // is not mutable. In either case, the result is the
                // same; set the displayable image to imagePart
                // (whether an Image or 'null', it is the same) and
                // set the mutableImage to 'null'
                mutableImageEls[elementNum] = null;
                imageEls[elementNum] = imagePart;
            }
        }
        invalidate();
    }

    /**
     * Insert a particular element of this ChoiceGroup
     *
     * @param elementNum The index to insert the element
     * @param stringPart The string part of the element to insert
     * @param imagePart The image part of the element to insert
     * @return int  The index of the newly inserted element
     */
    private int insertImpl(int elementNum, String stringPart,
                           Image imagePart) {

        if (numOfEls == stringEls.length) {

            String[] newStrings = new String[stringEls.length + 4];
            System.arraycopy(stringEls, 0, newStrings, 0, elementNum);
            System.arraycopy(stringEls, elementNum, newStrings,
                             elementNum + 1, numOfEls - elementNum);
            stringEls = newStrings;

            if (imageEls != null) {
                Image[] newImages = new Image[imageEls.length + 4];
                Image[] newMutableImages = new Image[imageEls.length + 4];
                System.arraycopy(imageEls, 0, newImages, 0, elementNum);
                System.arraycopy(imageEls, elementNum, newImages,
                                 elementNum + 1, numOfEls - elementNum);
                System.arraycopy(mutableImageEls, 0, newMutableImages, 
                                 0, elementNum);
                System.arraycopy(mutableImageEls, elementNum, newMutableImages,
                                 elementNum + 1, numOfEls - elementNum);
                imageEls = newImages;
                mutableImageEls = newMutableImages;
            }

            if (fontEls != null) {
                Font[] newFonts = new Font[fontEls.length + 4];
                System.arraycopy(fontEls, 0, newFonts, 0, elementNum);
                System.arraycopy(fontEls, elementNum, newFonts,
                                 elementNum + 1, numOfEls - elementNum);
            }

        } else {

            System.arraycopy(stringEls, elementNum, stringEls, elementNum + 1,
                             numOfEls - elementNum);

            if (imageEls != null) {
                System.arraycopy(imageEls, elementNum, imageEls,
                                 elementNum + 1, numOfEls - elementNum);
                System.arraycopy(mutableImageEls, elementNum, mutableImageEls,
                                 elementNum + 1, numOfEls - elementNum);
            }

            if (fontEls != null) {
                System.arraycopy(fontEls, elementNum, fontEls,
                                 elementNum + 1, numOfEls - elementNum);
            }
        }

        if (choiceType == Choice.MULTIPLE) {
            if (selEls.length == numOfEls) {
                boolean newSelEls[] = new boolean[numOfEls + 4];
                System.arraycopy(selEls, 0, newSelEls, 0, elementNum);
                System.arraycopy(selEls, elementNum, newSelEls, elementNum + 1,
                                 numOfEls - elementNum);
                selEls = newSelEls;
            } else {
                System.arraycopy(selEls, elementNum, selEls, elementNum + 1,
                                 numOfEls - elementNum);
            }
            selEls[elementNum] = false;
        }

        stringEls[elementNum] = null;
        if (imageEls != null) {
            imageEls[elementNum] = null;
            mutableImageEls[elementNum] = null;
        }
        if (fontEls != null) {
            fontEls[elementNum] = null;
        }

        numOfEls++;

        if (choiceType != Choice.MULTIPLE &&
                (elementNum < selectedIndex || selectedIndex == -1)) {
            selectedIndex++;
            hilightedIndex = selectedIndex;
        } else if (elementNum < hilightedIndex || hilightedIndex == -1) {
            hilightedIndex++;
        }

        setImpl(elementNum, stringPart, imagePart);

        return elementNum;
    }

    /**
     * Check the validity of a given element index
     *
     * @param elementNum The index to check
     * @throws IndexOutOfBoundsException If no element exists at the
     *                                   that index
     */
    private void checkIndex(int elementNum) {
        if (elementNum < 0 || elementNum >= numOfEls) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Check the given values for null
     *
     * @param stringPart The string part of the element
     * @param imagePart The image part of the element
     * @throws NullPointerException If the string part is null
     */
    private void checkNull(String stringPart, Image imagePart) {
        if (stringPart == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Check the validity of the selection array
     *
     * @param flag  The array of boolean flags representing the
     *              selected state of the elements
     * @throws NullPointerException If the flag array is null
     * @throws IllegalArgumentException If the flag array is not
     *                                  the same size as the element array
     */
    private void checkFlag(boolean[] flag) {
        if (flag == null) {
            throw new NullPointerException();
        }

        if (flag.length < numOfEls) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Paint this ChoiceGroup
     *
     * @param g the Graphics to paint to
     * @param w the width to paint
     */
    private void paintElements(Graphics g, int w) {
        int elWidth = w;
        int textOffset;
        Image img;
        Font fnt;
        int translatedX = 0;
        int translatedY = 0;

        if (choiceType == Choice.EXCLUSIVE) {
            translatedX = RD_WIDTH + LABEL_PAD;
            elWidth -= translatedX;
        } else if (choiceType == Choice.MULTIPLE) {
            translatedX = CKBX_WIDTH + LABEL_PAD;
            elWidth -= translatedX;
        }

        if ((elWidth != cachedWidth) || (elHeights.length != numOfEls)) {
            calculateElementHeight(elWidth);
        }

        // start for
        for (int i = 0; i < numOfEls; i++) {

            if (choiceType == Choice.EXCLUSIVE) {
                img = (i == selectedIndex) ? RD_ON_IMG : RD_OFF_IMG;
            } else if (choiceType == Choice.MULTIPLE) {
                img = (selEls[i]) ? CKBX_ON_IMG : CKBX_OFF_IMG;
            } else {
                img = null;
            }

            if (img != null) {
                g.drawImage(img, 0, 0,
                            Graphics.LEFT | Graphics.TOP);
                // elWidth = w - translatedX;
                g.translate(translatedX, 0);
            }

            textOffset = 0;

            if (imageEls != null && imageEls[i] != null) {
                int iX = g.getClipX();
                int iY = g.getClipY();
                int iW = g.getClipWidth();
                int iH = g.getClipHeight();

                g.clipRect(0, 0, PREFERRED_IMG_W, PREFERRED_IMG_H);
                g.drawImage(imageEls[i], 0, 0,
                            Graphics.LEFT | Graphics.TOP);
                g.setClip(iX, iY, iW, iH);
                textOffset = PREFERRED_IMG_W + LABEL_PAD;
            }

            if (fontEls != null && fontEls[i] != null) {
                fnt = fontEls[i];
            } else {
                fnt = Screen.CONTENT_FONT;
            }

            if (i == hilightedIndex && hasFocus) {
                g.fillRect(textOffset, 0,
                           g.getClipWidth() - translatedX - textOffset,
                           elHeights[i]);
                // If there was an offset, we need to fill in the 
                // hilight box under the element's image 
                if (textOffset != 0 && elHeights[i] > textOffset) { 
                    g.fillRect(0, textOffset, textOffset, 
                               elHeights[i] - textOffset); 
                } 

                Text.paint(stringEls[i], fnt, g, elWidth,
                     elHeights[i], textOffset,
                     ((fitPolicy == Choice.TEXT_WRAP_OFF)
                     ? (Text.INVERT | Text.TRUNCATE) : Text.INVERT), null);
            } else {
                Text.paint(stringEls[i], fnt, g, elWidth,
                     elHeights[i], textOffset,
                     ((fitPolicy == Choice.TEXT_WRAP_OFF)
                     ? (Text.NORMAL | Text.TRUNCATE) : Text.NORMAL), null);
            }

            if (img != null) {
                g.translate(-translatedX, 0);
            }

            g.translate(0, elHeights[i]);
            translatedY += elHeights[i];

        } // end for

        g.translate(0, -translatedY);
    }

    /**
     * Get the total element height of this CGroup
     *
     * @param width the desired width for this CG
     * @return the total element height
     */
    private int calculateElementHeight(int width) {

        // we cache the width we calculated the heights for
        cachedWidth = width;

        int eHeight = 0;
        if (elHeights == null || elHeights.length < numOfEls) {
            elHeights = new int[numOfEls];
        }
        int textOffset = 0;

        Font fnt;

        for (int x = 0; x < numOfEls; x++) {
            if (imageEls == null || imageEls[x] == null) {
                textOffset = 0;
            } else {
                textOffset = PREFERRED_IMG_W + LABEL_PAD;
            }

            if (fontEls == null || fontEls[x] == null) {
                fnt = Screen.CONTENT_FONT;
            } else {
                fnt = fontEls[x];
            }

            if (fitPolicy == TEXT_WRAP_OFF || choiceType == Choice.POPUP) {
                elHeights[x] = fnt.getHeight();
            } else {
                elHeights[x] = Text.getHeightForWidth(stringEls[x], fnt,
                                                      width, textOffset);
            }
            eHeight += elHeights[x];
        }
        return eHeight;
    }

    /**
     * Updates the native data structures used to draw
     * a Choice.POPUP type choice group menu.
     *
     * @param strings array of string elements for the choice group
     * @param images array of image elements for the choice group
     *        (may be null if no choice elements contain images)
     * @param numElements number of elements in the choice group
     * @param selectedElem initial element to draw in selected (hilighted)state
     * @param xPos x coordinate of where the open choice group should attempt 
     *        to position itself.  (the upper right corner of the closed choice
     *        group arrow)
     * @param yPos y coordinate of where the open choice group should attempt to
     *        position itself.
     * @param vWidth width of the viewport
     * @param vHeight height of the viewport
     * @param maxWidth width of widest choice group element
     * @param tickerFlag true if owner has a ticker showing
     * @param titleFlag true if owner has a title showing
     */
    private native void updatePopupElements(String[] strings,
                                            Image[] images,
                                            int numElements,
                                            int selectedElem,
                                            int xPos, int yPos,
                                            int vWidth, int vHeight,
                                            int maxWidth, boolean tickerFlag,
                                            boolean titleFlag);
    /**
     * When an open popup choice group closes, this method asks the native
     * data structure for the newly selected choice element.
     *
     * @return index of selected choice element, or <code>-1</code> in 
     *         if the popup choice menu was canceled out of
     */
    private native int getPopupSelection();

    /**
     * The default height for the popup window for a popup choice,
     * 130 pixels by default
     */
    private static final int PU_WIN_HEIGHT = 130;
 
    /**
     * Image to represent an unselected checkbox
     */
    private static final Image CKBX_OFF_IMG;
    /**
     * Image to represent a selected checkbox
     */
    private static final Image CKBX_ON_IMG;
    /**
     * Width of a checkbox image (both selected and unselected)
     */
    private static final int CKBX_WIDTH = 10;
    /**
     * Height of a checkbox image (both selected and unselected)
     */
    private static final int CKBX_HEIGHT = 11;
    /**
     * Image to represent an unselected radio button
     */
    private static final Image RD_OFF_IMG;
    /**
     * Image to represent a selected radio button
     */
    private static final Image RD_ON_IMG;
    /**
     * Width of a radio button image (both selected and unselected)
     */
    private static final int RD_WIDTH = 11;
    /**
     * Height of a radio button image (both selected and unselected)
     */
    private static final int RD_HEIGHT = 11;
    /**
     * Image to represent a popup arrow
     */
    private static final Image POPUP_ARROW_IMG;
    /**
     * Width of the popup arrow image
     */
    private static final int POPUP_AR_WIDTH = 11;
    /**
     * Height of popup arrow image
     */
    private static final int POPUP_AR_HEIGHT = 11;
    /**
     * The preferred image width for an image as part of an element of
     * a choice (12 pixels).  
     */
    static final int PREFERRED_IMG_W = 12;
    /**
     * The preferred image height for an image as part of an element of
     * a choice (12 pixels).
     */
    static final int PREFERRED_IMG_H = 12;

    static {
        /*
         * Initialize the icons necessary for the various modes.
         */
        CKBX_OFF_IMG =
            ImmutableImage.createIcon("checkbox_off.png");
        CKBX_ON_IMG  =
            ImmutableImage.createIcon("checkbox_on.png");
        RD_OFF_IMG   =
            ImmutableImage.createIcon("radio_off.png");
        RD_ON_IMG    =
            ImmutableImage.createIcon("radio_on.png");
        POPUP_ARROW_IMG = 
            ImmutableImage.createIcon("popup_arrow.png");
    }
}

