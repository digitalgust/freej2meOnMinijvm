/*
 * @(#)Form.java	1.284 02/11/05 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

/**
 * A <code>Form</code> is a <code>Screen</code> that contains
 * an arbitrary mixture of items: images,
 * read-only text fields, editable text fields, editable date fields, gauges,
 * choice groups, and custom items. In general, any subclass of the
 * {@link Item Item} class may be contained within a form.
 * The implementation handles layout, traversal, and scrolling.
 * The entire contents of the <code>Form</code> scrolls together.
 *
 * <h2>Item Management</h2>
 * <p>
 * The items contained within a <code>Form</code> may be edited
 * using append, delete,
 * insert, and set methods.  <code>Items</code> within a
 * <code>Form</code> are referred to by their
 * indexes, which are consecutive integers in the range from zero to
 * <code>size()-1</code>,
 * with zero referring to the first item and <code>size()-1</code>
 * to the last item.  </p>
 *
 * <p> An item may be placed within at most one
 * <code>Form</code>. If the application
 * attempts to place an item into a <code>Form</code>, and the
 * item is already owned by this
 * or another <code>Form</code>, an
 * <code>IllegalStateException</code> is thrown.
 * The application must
 * remove the item from its currently containing <code>Form</code>
 * before inserting it into
 * the new <code>Form</code>. </p>
 *
 * <p> If the <code>Form</code> is visible on the display when
 * changes to its contents are
 * requested by the application, updates to the display take place as soon
 * as it is feasible for the implementation to do so.
 * Applications need not take any special action to refresh a
 * <code>Form's</code> display
 * after its contents have been modified. </p>
 *
 * <a name="layout"></a>
 * <h2>Layout</h2>
 *
 * <p>Layout policy in <code>Form</code> is organized around
 * rows. Rows are typically
 * related to the width of the screen, respective of margins, scroll bars, and
 * such.  All rows in a particular <code>Form</code> will have the
 * same width.  Rows do not
 * vary in width based on the <code>Items</code> contained within
 * the <code>Form</code>, although they
 * may all change width in certain circumstances, such as when a scroll bar
 * needs to be added or removed. <code>Forms</code> generally do not scroll
 * horizontally.</p>
 *
 * <p><code>Forms</code> grow vertically and scroll vertically as
 * necessary. The height
 * of a <code>Form</code> varies depending upon the number of rows
 * and the height of
 * each row. The height of each row is determined by the items that are
 * positioned on that row. Rows need not all have the same height.
 * Implementations may also vary row heights to provide proper padding or
 * vertical alignment of <code>Item</code> labels.</p>
 *
 * <p>An implementation may choose to lay out <code>Items</code> in a
 * left-to-right or right-to-left direction depending upon the language
 * conventions in use.  The same choice of layout direction must apply to all
 * rows within a particular <code>Form</code>.</p>
 *
 * <p>Prior to the start of the layout algorithm, the
 * <code>Form</code> is considered to
 * have one empty row at the top. The layout algorithm considers each Item
 * in turn, starting at <code>Item</code> zero and proceeding in
 * order through each <code>Item</code>
 * until the last <code>Item</code> in the <code>Form</code>
 * has been processed.
 * If the layout direction (as described above) is left-to-right, the
 * beginning of the row is the left edge of the <code>Form</code>.  If the
 * layout direction is right-to-left, the beginning of the row is the right
 * edge of the <code>Form</code>.  <code>Items</code> are laid out at the
 * beginning of each row, proceeding across each row in the chosen layout
 * direction, packing as many <code>Items</code> onto each row as will fit,
 * unless a condition occurs that causes the packing of a row to be terminated
 * early.
 * A new row is then added, and
 * <code>Items</code> are packed onto it
 * as described above. <code>Items</code> are packed onto rows,
 * and new rows are added
 * below existing rows as necessary until all <code>Items</code>
 * have been processed by
 * the layout algorithm.</p>
 *
 * <p>The layout algorithm has a concept of a <em>current alignment</em>.
 * It can have the value <code>LAYOUT_LEFT</code>,
 * <code>LAYOUT_CENTER</code>, or <code>LAYOUT_RIGHT</code>.
 * The value of the current alignment at the start of the layout algorithm
 * depends upon the layout direction in effect for this <code>Form</code>.  If
 * the layout direction is left-to-right, the initial alignment value must be
 * <code>LAYOUT_LEFT</code>.  If the layout direction is right-to-left, the
 * initial alignment value must be <code>LAYOUT_RIGHT</code>.
 * The current alignment changes when the layout
 * algorithm encounters an <code>Item</code> that has one of the layout
 * directives <code>LAYOUT_LEFT</code>, <code>LAYOUT_CENTER</code>, or
 * <code>LAYOUT_RIGHT</code>.  If none of these directives is present on an
 * <code>Item</code>, the current layout directive does not change.  This
 * rule has the effect of grouping the contents of the
 * <code>Form</code> into sequences of consecutive <code>Items</code>
 * sharing an alignment value.  The alignment value of each <code>Item</code>
 * is maintained internally to the <code>Form</code> and does not affect the
 * <code>Items'</code> layout value as reported by the
 * {@link Item#getLayout Item.getLayout} method.</p>
 *
 * <p>The layout algorithm generally attempts to place an item on the same
 * row as the previous item, unless certain conditions occur that cause a
 * &quot;row break.&quot; When there is a row break, the current item
 * will be placed
 * at the beginning of a new row instead of being placed after
 * the previous item, even if there is room.</p>
 *
 * <p>A row break occurs before an item if any of the following
 * conditions occurs:</p>
 *
 * <ul>
 * <li>the previous item has a row break after it;</li>
 * <li>it has the <code>LAYOUT_NEWLINE_BEFORE</code> directive; or</li>
 * <li>it is a <code>StringItem</code> whose contents starts with
 * &quot;\n&quot;;</li>
 * <li>it is a
 * <code>ChoiceGroup</code>, <code>DateField</code>,
 * <code>Gauge</code>, or a <code>TextField</code>, and the
 * <code>LAYOUT_2</code> directive is not set; or</li>
 * <li>this <code>Item</code> has a <code>LAYOUT_LEFT</code>,
 * <code>LAYOUT_CENTER</code>, or <code>LAYOUT_RIGHT</code> directive
 * that differs from the <code>Form's</code> current alignment.</li>
 * </ul>
 *
 * <p>A row break occurs after an item if any of the following
 * conditions occurs:</p>
 *
 * <ul>
 * <li>it is a <code>StringItem</code> whose contents ends with
 * &quot;\n&quot;; or</li>
 * <li>it has the <code>LAYOUT_NEWLINE_AFTER</code> directive; or</li>
 * <li>it is a
 * <code>ChoiceGroup</code>, <code>DateField</code>,
 * <code>Gauge</code>, or a <code>TextField</code>, and the
 * <code>LAYOUT_2</code> directive is not set.</li>
 * </ul>
 *
 * <p>The presence of the <code>LAYOUT_NEWLINE_BEFORE</code> or
 * <code>LAYOUT_NEWLINE_AFTER</code> directive does not cause
 * an additional row break if there is one already present.  For example,
 * if a <code>LAYOUT_NEWLINE_BEFORE</code> directive appears on a
 * <code>StringItem</code> whose contents starts with &quot;\n&quot;,
 * there is only a single row break.  A similar rule applies with a
 * trailing &quot;\n&quot; and <code>LAYOUT_NEWLINE_AFTER</code>.
 * Also, there is only a single row
 * break if an item has the <code>LAYOUT_NEWLINE_AFTER</code> directive
 * and the next item has the <code>LAYOUT_NEWLINE_BEFORE</code> directive.
 * However, the presence of consecutive &quot;\n&quot; characters,
 * either within a single <code>StringItem</code> or in adjacent
 * <code>StringItems</code>, will cause as many row breaks as there are
 * &quot;\n&quot; characters.  This will cause empty rows to be present.
 * The height of an empty row is determined by the prevailing font height of
 * the <code>StringItem</code> within which the &quot;\n&quot; that ends the
 * row occurs.</p>
 *
 * <p>Implementations may provide additional conditions under which a row
 * break occurs.  For example, an implementation's layout policy may lay out
 * labels specially, implicitly causing a break before every
 * <code>Item</code> that has a
 * label.  Or, as another example, a particular implementation's user
 * interface style may dictate that a DateField item always appears on a row
 * by itself.  In this case, this implementation may cause row breaks to occur
 * both before and after every <code>DateField</code> item.</p>
 *
 * <p>Given two items with adjacent <code>Form</code> indexes, if
 * none of the specified
 * or implementation-specific conditions for a row break between them
 * occurs, and if space permits, these items should be placed on the same
 * row.</p>
 *
 * <p>When packing <code>Items</code> onto a row, the width of the
 * item is compared with
 * the remaining space on the row. For this purpose, the width used is the
 * <code>Item's</code> preferred width, unless the
 * <code>Item</code> has the <code>LAYOUT_SHRINK</code>
 * directive,
 * in which case the <code>Item's</code> minimum width is used. If
 * the <code>Item</code> is too wide
 * to fit in the space remaining on the row, the row is considered to be
 * full, a new row is added beneath this one, and the
 * <code>Item</code> is laid out on
 * this new row.</p>
 *
 * <p>Once the contents of a row have been determined, the space available on
 * the row is distributed by expanding items and by adding space between
 * items. If any items on this row have the
 * <code>LAYOUT_SHRINK</code> directive (that is,
 * they are shrinkable), space is first distributed to these items. Space is
 * distributed to each of these items proportionally to the difference between
 * the each <code>Item's</code> preferred size and its minimum
 * size.  At this stage, no
 * shrinkable item is expanded beyond its preferred width.</p>
 *
 * <p>For example, consider a row that has <code>30</code> pixels
 * of space available and
 * that has two shrinkable items <code>A</code> and
 * <code>B</code>. Item <code>A's</code> preferred size is
 * <code>15</code> and
 * its minimum size is <code>10</code>. Item <code>B's</code>
 * preferred size is <code>30</code> and its minimum
 * size is <code>20</code>. The difference between
 * <code>A's</code> preferred and minimum size is
 * <code>5</code>,
 * and <code>B's</code> difference is <code>10</code>. The
 * <code>30</code> pixels are distributed to these items
 * proportionally to these differences. Therefore, <code>10</code>
 * pixels are
 * distributed to item <code>A</code> and <code>20</code>
 * pixels to item <code>B</code>.</p>
 *
 * <p>If after expanding all the shrinkable items to their preferred widths,
 * there is still space left on the row, this remaining space is distributed
 * equally among the Items that have the
 * <code>LAYOUT_EXPAND</code> directive (the
 * stretchable <code>Items</code>).  The presence of any
 * stretchable items on a row will
 * cause the <code>Items</code> on this row to occupy the full
 * width of the row.</p>
 *
 * <p>If there are no stretchable items on this row, and there is still space
 * available on this row, the <code>Items</code> are packed as tightly as
 * possible and are placed on the row according to the alignment value shared
 * by the <code>Items</code> on this row.  (Since changing the current
 * alignment causes a row break, all <code>Items</code> on the same row must
 * share the same alignment value.)  If the alignment value is
 * <code>LAYOUT_LEFT</code>, the <code>Items</code> are positioned at the left
 * end of the row and the remaining space is placed at the right end of the
 * row.  If the alignment value is <code>LAYOUT_RIGHT</code>, the
 * <code>Items</code> are positioned at the right end of the row and the
 * remaining space is placed at the left end of the row.  If the alignment
 * value is <code>LAYOUT_CENTER</code>, the <code>Items</code> are positioned
 * in the middle of the row such that the remaining space on the row is
 * divided evenly between the left and right ends of the row.</p>
 *
 * <p>Given the set of items on a particular row, the heights of these
 * <code>Items</code> are inspected.  For each <code>Item</code>, the height
 * that is used is the preferred height, unless the <code>Item</code> has the
 * <code>LAYOUT_VSHRINK</code> directive, in which case the
 * <code>Item's</code> minimum height is used.
 * The height of the tallest
 * <code>Item</code> determines the
 * height of the row.  <code>Items</code> that have the
 * <code>LAYOUT_VSHRINK</code> directive are expanded to their preferred
 * height or to the height of the row, whichever is smaller.
 * <code>Items</code> that are still shorter than the
 * row height and that
 * have the <code>LAYOUT_VEXPAND</code> directive will expand to
 * the height of the row.
 * The <code>LAYOUT_VEXPAND</code> directive on an item will never
 * increase the height
 * of a row.</p>
 *
 * <p>Remaining <code>Items</code> shorter than the row height
 * will be positioned
 * vertically within the row using the <code>LAYOUT_TOP</code>,
 * <code>LAYOUT_BOTTOM</code>, and
 * <code>LAYOUT_VCENTER</code> directives.  If no vertical layout directive is
 * specified, the item must be aligned along the bottom of the row.</p>
 *
 * <p><code>StringItems</code> are treated specially in the above
 * algorithm.  If the
 * contents of a <code>StringItem</code> (its string value,
 * exclusive of its label) contain
 * a newline character (&quot;\n&quot;), the string should be split at
 * that point and
 * the remainder laid out starting on the next row.</p>
 *
 * <p>If one or both dimensions of the preferred size of
 * a <code>StringItem</code> have been locked, the <code>StringItem</code>
 * is wrapped to fit that width and height and is treated as a
 * rectangle whose minimum and preferred width and height are the width and
 * height of this rectangle. In this case, the
 * <code>LAYOUT_SHRINK</code>, <code>LAYOUT_EXPAND</code>,
 * and <code>LAYOUT_VEXPAND</code> directives are ignored.</p>
 *
 * <p>If both dimensions of the preferred size of a <code>StringItem</code>
 * are unlocked, the text from the <code>StringItem</code> may be wrapped
 * across multiple rows.  At the point in the layout algorithm where the width
 * of the <code>Item</code> is compared to the remaining space on the row, as
 * much text is taken from the beginning of the <code>StringItem</code> as
 * will fit onto the current row.  The contents of this row are then
 * positioned according to the current alignment value.  The remainder of the
 * text in the <code>StringItem</code> is line-wrapped to the full width of as
 * many new rows as are necessary to accommodate the text.  Each full row is
 * positioned according to the current alignment value.  The last line of the
 * text might leave space available on its row.  If there is no row break
 * following this <code>StringItem</code>, subsequent <code>Items</code> are
 * packed into the remaining space and the contents of the row are positioned
 * according to the current alignment value.  This rule has the effect of
 * displaying the contents of a <code>StringItem</code> as a paragraph of text
 * set flush-left, flush-right, or centered, depending upon whether the
 * current alignment value is <code>LAYOUT_LEFT</code>,
 * <code>LAYOUT_RIGHT</code>, or <code>LAYOUT_CENTER</code>, respectively.
 * The preferred width and height of a <code>StringItem</code> wrapped across
 * multiple rows, as reported by the
 * {@link Item#getPreferredWidth Item.getPreferredWidth} and
 * {@link Item#getPreferredHeight Item.getPreferredHeight}
 * methods, describe the width and height of the bounding rectangle of the
 * wrapped text.</p>
 *
 * <p><code>ImageItems</code> are also treated specially by the above
 * algorithm.  The foregoing rules concerning the horizontal alignment value
 * and the <code>LAYOUT_LEFT</code>, <code>LAYOUT_RIGHT</code>, and
 * <code>LAYOUT_CENTER</code> directives, apply to <code>ImageItems</code>
 * only when the <code>LAYOUT_2</code> directive is also present on that item.
 * If the <code>LAYOUT_2</code> directive is not present on an
 * <code>ImageItem</code>, the behavior of the <code>LAYOUT_LEFT</code>,
 * <code>LAYOUT_RIGHT</code>, and <code>LAYOUT_CENTER</code> directives is
 * implementation-specific.</p>
 *
 * <p>A <code>Form's</code> layout is recomputed automatically as
 * necessary.  This may
 * occur because of a change in an <code>Item's</code> size caused
 * by a change in its
 * contents or because of a request by the application to change the Item's
 * preferred size.  It may also occur if an <code>Item's</code>
 * layout directives are
 * changed by the application.  The application does not need to perform
 * any specific action to cause the <code>Form's</code> layout to
 * be updated.</p>
 *
 * <h2><a NAME="linebreak">Line Breaks and Wrapping</a></h2>
 *
 * <p>For all cases where text is wrapped,
 * line breaks must occur at each newline character
 * (<code>'\n'</code> = Unicode <code>'U+000A'</code>).  
 * If space does not permit
 * the full text to be displayed it is truncated at line breaks.
 * If there are no suitable line breaks, it is recommended that
 * implementations break text at word boundaries.
 * If there are no word boundaries, it is recommended that
 * implementations break text at character boundaries. </p>
 *
 * <p>Labels that contain line breaks may be truncated at the line
 * break and cause the rest of the label not to be shown.</p>
 *
 * <h2>User Interaction</h2>
 *
 * <p> When a <code>Form</code> is present on the display the user
 * can interact
 * with it and its <code>Items</code> indefinitely (for instance,
 * traversing from <code>Item</code>
 * to <code>Item</code>
 * and possibly
 * scrolling). These traversing and scrolling operations do not cause
 * application-visible events. The system notifies
 * the application when the user modifies the state of an interactive
 * <code>Item</code>
 * contained within the <code>Form</code>.  This notification is
 * accomplished by calling the
 * {@link ItemStateListener#itemStateChanged itemStateChanged()}
 * method of the listener declared to the <code>Form</code> with the
 * {@link #setItemStateListener setItemStateListener()} method. </p>
 *
 * <p> As with other <code>Displayable</code> objects, a
 * <code>Form</code> can declare
 * {@link Command commands} and declare a command listener with the
 * {@link Displayable#setCommandListener setCommandListener()} method.
 * {@link CommandListener CommandListener}
 * objects are distinct from
 * {@link ItemStateListener ItemStateListener} objects, and they are declared
 * and invoked separately. </p>
 *
 * <h2>Notes for Application Developers</h2>
 *
 * <UL>
 * <LI>Although this class allows creation of arbitrary combination of
 * components
 * the application developers should keep the small screen size in mind.
 * <code>Form</code> is designed to contain a <em>small number of
 * closely related</em>
 * UI elements. </LI>
 *
 * <LI>If the number of items does not fit on the screen, the
 * implementation may choose to make it scrollable or to fold some components
 * so that a separate screen appears when the element is edited.</LI>
 * </UL>
 *
 * <p>
 * </p>
 *
 * @see Item
 * @since MIDP 1.0
 */

public class Form extends Screen {

// ************************************************************
//  public member variables
// ************************************************************

// ************************************************************
//  protected member variables
// ************************************************************

// ************************************************************
//  package private member variables
// ************************************************************

    /**
     * A boolean declaring whether the contents of the viewport
     * can be traversed using the horizontal traversal keys,
     * ie, left and right
     */
    final boolean TRAVERSE_HORIZONTAL = true;

    /**
     * A boolean declaring whether the contents of the viewport
     * can be traversed using the vertical traversal keys,
     * ie, up and down
     */
    final boolean TRAVERSE_VERTICAL = true;

    /** The spacing, in pixels, between cells */
    static final int CELL_SPACING = 4;
//    static final int CELL_SPACING = 6;

    /** A static identifying a one pixel box for a traversal indicator */
    static final int ONE_PIXEL_BOX = 0;

    /** A static identifying a traversal indicator using triangles */
    static final int TRIANGLE_CORNERS = 1;

    /** A static holding the type of traversal indicator to draw */
    static final int TRAVERSE_INDICATOR = ONE_PIXEL_BOX;
//    static final int TRAVERSE_INDICATOR = TRIANGLE_CORNERS;

    /** A static holding the color of the traversal indicator */
//    static final int TRAVERSE_INDICATOR_COLOR = 0;
    static final int TRAVERSE_INDICATOR_COLOR = Item.DARK_GRAY_COLOR;

    /** A bit mask to capture the horizontal layout directive of an item */
    static final int LAYOUT_HMASK = 0x03;

    /** A bit mask to capture the vertical layout directive of an item */
    static final int LAYOUT_VMASK = 0x30;

// ************************************************************
//  private member variables
// ************************************************************

    /**
     * A Form is always in one of 2 modes:
     *  1. traversing the Form
     *  2. traversing within an Item
     *
     * These 2 modes map to the values:
     *  FORM_TRAVERSE
     *  ITEM_TRAVERSE
     */
    private int formMode;

    /**
     * A value indicating this Form is in "form traverse" mode
     */
    private static final int FORM_TRAVERSE = 0;

    /**
     * A value indicating this Form is in "item traverse" mode
     */
    private static final int ITEM_TRAVERSE = 2;

    /** The item index which has the traversal focus */
    private int traverseIndex = -1;

    /**
     * The traversal indicator should only be shown when the current
     * traversal item is actually interactive. indicateTraverse is
     * true when the traversal indicator should be drawn around the
     * current traversal item.
     */
    private boolean indicateTraverse = true;

    /**
     * Items must have their show/hide notify methods called when
     * they come into and go out of view. This essentially only
     * happens on scrolling, however their method is called in
     * paint(). This flag tells us wether we should be calling
     * the method in the paint routine, and is set in the scroll
     * routine
     */
    private boolean validateVisibility = true;

    /**
     * 'viewable' contains the dimensions and location of the child
     * viewable object within the viewport
     */
    private int[] viewable;

    /**
     * When a Form calls an Item's traverse() method, it passes in
     * an in-out int[] that represents the Item's internal traversal
     * bounds. This gets cached in the visRect variable
     */
    private int[] visRect;

    /**
     * This is the rate at wich the internal array of Items grows if
     * it gets filled up
     */
    private static final int GROW_SIZE = 4;

    /** Array of Items that were added to this form. */
    private Item items[];

    /** The number of actual Items added is numOfItems. */
    private int  numOfItems; // = 0;

    /** itemStateListener that has to be notified of any state changes */
    private ItemStateListener itemStateListener;

    /** 
     * true if a callPointerPressed event has occured without
     * a corresponding callPointerReleased. false otherwise
     */
    private boolean pointerPressed;

// ************************************************************
//  Static initializer, constructor
// ************************************************************

    /**
     * Creates a new, empty <code>Form</code>.
     *
     * @param title the <code>Form's</code> title, or
     * <code>null</code> for no title
     */
    public Form(String title) {
        this(title, null);
    }

    /**
     * Creates a new <code>Form</code> with the specified
     * contents. This is identical to
     * creating an empty <code>Form</code> and then using a set of
     * <code>append</code>
     * methods.  The
     * items array may be <code>null</code>, in which case the
     * <code>Form</code> is created empty.  If
     * the items array is non-null, each element must be a valid
     * <code>Item</code> not
     * already contained within another <code>Form</code>.
     *
     * @param title the <code>Form's</code> title string
     * @param items the array of items to be placed in the
     * <code>Form</code>, or <code>null</code> if there are no
     * items
     * @throws IllegalStateException if one of the items is already owned by
     * another container
     * @throws NullPointerException if an element of the items array is
     * <code>null</code>
     */
    public Form(String title, Item[] items) {
        super(title);

        synchronized (Display.LCDUILock) {

            // Initialize the in-out rect for Item traversal
            visRect = new int[4];

            if (items == null) {
                this.items = new Item[GROW_SIZE];
                // numOfItems was initialized to 0
                // so there is no need to update it
                return;

            } else {
                this.items = new Item[items.length > GROW_SIZE ?
                                      items.length : GROW_SIZE];
            }

            // We have to check all items first so that some
            // items would not be added to a form that was not
            // instanciated
            for (int i = 0; i < items.length; i++) {
                // NullPointerException will be thrown by
                // items[i].getOwner() if items[i] == null;
                if (items[i].getOwner() != null) {
                    throw new IllegalStateException();
                }
            }

            numOfItems = items.length;

            for (int i = 0; i < numOfItems; i++) {
                items[i].setOwner(this);
                this.items[i] = items[i];
            }

        } // synchronized
    }

// ************************************************************
//  public methods
// ************************************************************

    /**
     * Adds an <code>Item</code> into the <code>Form</code>.  The newly
     * added <code>Item</code> becomes the last <code>Item</code> in the
     * <code>Form</code>, and the size of the <code>Form</code> grows
     * by one.
     *
     * @param item the {@link Item Item} to be added.
     * @return the assigned index of the <code>Item</code>
     * @throws IllegalStateException if the item is already owned by
     * a container
     * @throws NullPointerException if item is <code>null</code>
     */
    public int append(Item item) {
        synchronized (Display.LCDUILock) {
            // NullPointerException will be thrown
            // by item.getOwner() if item == null
            if (item.getOwner() != null) {
                throw new IllegalStateException();
            }

            return insertImpl(numOfItems, item);
        }
    }

    /**
     * Adds an item consisting of one <code>String</code> to the
     * <code>Form</code>. The effect of
     * this method is identical to 
     *
     * <p> <code>
     * append(new StringItem(null, str))
     * </code> </p>
     *
     * @param str the <code>String</code> to be added
     * @return the assigned index of the <code>Item</code>
     * @throws NullPointerException if str is <code>null</code>
     */
    public int append(String str) {
        if (str == null) {
            throw new NullPointerException();
        }

        synchronized (Display.LCDUILock) {
            return insertImpl(numOfItems, new StringItem(null, str));
        }
    }
    
    /**
     * Adds an item consisting of one <code>Image</code> to the
     * <code>Form</code>. The effect of
     * this method is identical to 
     *
     * <p> <code>
     * append(new ImageItem(null, img, ImageItem.LAYOUT_DEFAULT, null))
     * </code> </p>
     *
     * @param img the image to be added
     * @return the assigned index of the <code>Item</code>
     * @throws NullPointerException if <code>img</code> is <code>null</code>
     */
    public int append(Image img) {
        if (img == null) {
            throw new NullPointerException();
        }

        synchronized (Display.LCDUILock) {
            return insertImpl(numOfItems,
                new ImageItem(null, img, ImageItem.LAYOUT_DEFAULT, null));
        }
    }

    /**
     * Inserts an item into the <code>Form</code> just prior to
     * the item specified.
     * The size of the <code>Form</code> grows by one.  The
     * <code>itemNum</code> parameter must be
     * within the range <code>[0..size()]</code>, inclusive.
     * The index of the last item is <code>size()-1</code>, and 
     * so there is actually no item whose index is
     * <code>size()</code>. If this value
     * is used for <code>itemNum</code>, the new item is inserted
     * immediately after
     * the last item. In this case, the effect is identical to
     * {@link #append(Item) append(Item)}. 
     *
     * <p> The semantics are otherwise identical to
     * {@link #append(Item) append(Item)}. </p>
     *
     * @param itemNum the index where insertion is to occur
     * @param item the item to be inserted
     * @throws IndexOutOfBoundsException if <code>itemNum</code> is invalid
     * @throws IllegalStateException if the item is already owned by
     * a container
     * @throws NullPointerException if <code>item</code> is
     * <code>null</code>
     */
    public void insert(int itemNum, Item item) {
        synchronized (Display.LCDUILock) {
            // NullPointerException will be thrown
            // by item.getOwner() if item == null
            if (item.getOwner() != null) {
                throw new IllegalStateException();
            }

            if (itemNum < 0 || itemNum > numOfItems) {
                throw new IndexOutOfBoundsException();
            }
            insertImpl(itemNum, item);
        }
    }

    /**
     * Deletes the <code>Item</code> referenced by
     * <code>itemNum</code>. The size of the <code>Form</code>
     * shrinks by one. It is legal to delete all items from a
     * <code>Form</code>.
     * The <code>itemNum</code> parameter must be 
     * within the range <code>[0..size()-1]</code>, inclusive. 
     *
     * @param itemNum the index of the item to be deleted
     * @throws IndexOutOfBoundsException if <code>itemNum</code> is invalid
     */
    public void delete(int itemNum) {
        synchronized (Display.LCDUILock) {
            if (itemNum < 0 || itemNum >= numOfItems) {
                throw new IndexOutOfBoundsException();
            }

            Item deletedItem = items[itemNum];

            deletedItem.setOwner(null);

            numOfItems--;

            if (traverseIndex == itemNum) {
                formMode = FORM_TRAVERSE;
            }

            if (traverseIndex > itemNum || traverseIndex == numOfItems) {
                traverseIndex--;
            }

            if (itemNum < numOfItems) {
                System.arraycopy(items, itemNum + 1, items, itemNum,
                                 numOfItems - itemNum);
            }

            // Delete reference to the last item 
            // that was left after array copy
            items[numOfItems] = null;

            // The Form is clear; reset its state
            if (numOfItems == 0 && items.length > GROW_SIZE) {
                items = new Item[GROW_SIZE];                 // start fresh
            }

            invalidate(null);
        } // synchronized
    }

    /**
     * Deletes all the items from this <code>Form</code>, leaving
     * it with zero items.
     * This method does nothing if the <code>Form</code> is already empty.
     *
     * @since MIDP 2.0
     */
    public void deleteAll() {
        synchronized (Display.LCDUILock) {
            if (numOfItems == 0) {
                return;
            }

            for (int x = 0; x < numOfItems; x++) {
                items[x].setOwner(null);
                items[x] = null;
            }
            if (items.length > GROW_SIZE) {
                items = new Item[GROW_SIZE];                     // start fresh
            }

            // Reset form state
            numOfItems = 0;
            formMode = FORM_TRAVERSE;
            traverseIndex = -1;

            invalidate(null);
        }
    }

    /**
     * Sets the item referenced by <code>itemNum</code> to the
     * specified item,
     * replacing the previous item. The previous item is removed
     * from this <code>Form</code>.
     * The <code>itemNum</code> parameter must be 
     * within the range <code>[0..size()-1]</code>, inclusive. 
     *
     * <p>The end result is equal to
     * <code>insert(n, item); delete(n+1);</code><br>
     * although the implementation may optimize the repainting
     * and usage of the array that stores the items. <P>
     *
     * @param itemNum the index of the item to be replaced
     * @param item the new item to be placed in the <code>Form</code>
     *
     * @throws IndexOutOfBoundsException if <code>itemNum</code> is invalid
     * @throws IllegalStateException if the item is already owned by
     * a container
     * @throws NullPointerException if <code>item</code> is 
     * <code>null</code>
     */
    public void set(int itemNum, Item item) {
        synchronized (Display.LCDUILock) {
            // NullPointerException will be thrown
            // by item.getOwner() if item == null
            if (item.getOwner() != null) {
                throw new IllegalStateException();
            }

            if (itemNum < 0 || itemNum >= numOfItems) {
                throw new IndexOutOfBoundsException();
            }

            setImpl(itemNum, item);
        }
    }
     
    /**
     * Gets the item at given position.  The contents of the
     * <code>Form</code> are left
     * unchanged.
     * The <code>itemNum</code> parameter must be 
     * within the range <code>[0..size()-1]</code>, inclusive. 
     *
     * @param itemNum the index of item
     *
     * @return the item at the given position
     *
     * @throws IndexOutOfBoundsException if <code>itemNum</code> is invalid
     */
    public Item get(int itemNum) {
        synchronized (Display.LCDUILock) {
            if (itemNum < 0 || itemNum >= numOfItems) {
                throw new IndexOutOfBoundsException();
            }

            return items[itemNum];
        }
    }

    /**
     * Sets the <code>ItemStateListener</code> for the
     * <code>Form</code>, replacing any previous
     * <code>ItemStateListener</code>. If
     * <code>iListener</code> is <code>null</code>, simply
     * removes the previous <code>ItemStateListener</code>.
     * @param iListener the new listener, or <code>null</code> to remove it
     */
    public void setItemStateListener(ItemStateListener iListener) {
        synchronized (Display.LCDUILock) {
            itemStateListener = iListener;
        }
    }

    /**
     * Gets the number of items in the <code>Form</code>.
     * @return the number of items
     */
    public int size() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return numOfItems;
    }

    /**
     * Returns the width in pixels of the displayable area available for items.
     * The value may depend on how the device uses the screen and may be
     * affected by the presence or absence of the ticker, title, or commands.
     * The <code>Items</code> of the <code>Form</code> are
     * laid out to fit within this width.
     * @return the width of the <code>Form</code> in pixels
     * @since MIDP 2.0
     */
    public int getWidth() {
        return viewport[WIDTH] - CELL_SPACING - CELL_SPACING;
    } 

    /**
     * Returns the height in pixels of the displayable area available
     * for items.
     * This value is the height of the form that can be displayed without
     * scrolling.
     * The value may depend on how the device uses the screen and may be
     * affected by the presence or absence of the ticker, title, or commands.
     * @return the height of the displayable area of the 
     * <code>Form</code> in pixels
     * @since MIDP 2.0
     */
    public int getHeight() {
        return viewport[HEIGHT] - CELL_SPACING - CELL_SPACING;
    }

// ************************************************************
//  protected methods
// ************************************************************

// ************************************************************
//  package private methods
// ************************************************************

    /**
     * Called to commit any pending user interaction for the current item.
     * Override the no-op in Displayable.
     */
    void commitPendingInteraction() {
        Item curItem = getCurrentItem();
        if (curItem != null) {
            curItem.commitPendingInteraction();
        }
    }

    /**
     * Set the current traversal location to the given Item.
     * This call has no effect if the given Item is the
     * current traversal item, or if the given Item is not
     * part of this Form.
     *
     * @param i the Item to make the current traversal item
     */
    void setCurrentItem(Item i) {
        // SYNC NOTE: This method is called from Display which holds
        // LCDUILock around the call

        if (i == null || i.owner != this) {
            return;
        }

        if (traverseIndex != -1 && items[traverseIndex] == i) {
            return;
        }

        for (int x = 0; x < numOfItems; x++) {
            if (items[x] == i) {
                setTraverseIndex(CustomItem.NONE, traverseIndex, x);
                ((Screen)paintDelegate).resetToTop = false;
                return;
            }
        }
    }

    /**
     * notify this Form it is being shown on the given Display
     *
     * @param d the Display showing this Form
     */
    void callShowNotify(Display d) {
        super.callShowNotify(d);

        synchronized (Display.LCDUILock) {
            // Whenever this Form is shown, update the layout
            layout();

            // In most cases, we reset the form to the top
            // The cast is safe because paintDelegate is always
            // either a List, TextBox, Alert or this Form itself.
            if (((Screen)paintDelegate).resetToTop) {
                traverseIndex = -1;
                view[Y] = 0;
                view[X] = 0;
            } else {
                ((Screen)paintDelegate).resetToTop = true;
            }
        }

        // We issue a default traverse when a Form is initially
        // shown to traverse to the first item in the Form
        traverse(CustomItem.NONE);
    }

    /**
     * notify this Form it is being hidden on the given Display
     *
     * @param d the Display hiding this Form
     */
    void callHideNotify(Display d) {
        super.callHideNotify(d);

        // SYNC NOTE: Rather than make a complete copy of the set
        // of items on this form, we'll simply catch any exception
        // that occurs and move on. The only problem that could occur
        // would be items being deleted from the Form, which would
        // mean the application was removing items from the Form
        // while it was technically still visible.
        if (traverseIndex != -1) {
            try {
                items[traverseIndex].callTraverseOut();
            } catch (Throwable t) {
                // Simply swallow the error and move on
            }
        }

        // We need to loop through our Items and call hideNotify
        // on those that were visible
        for (int x = 0; x < numOfItems; x++) {
            try {
                if (items[x].visible) {
                    items[x].callHideNotify();
                }
            } catch (Throwable t) {
                // Simply swallow the error and move on
            }
        }
    }

    /**
     * Handle a key press
     *
     * @param keyCode the key code of the key which was pressed
     */
    void callKeyPressed(int keyCode) {
        Item i = null;

        synchronized (Display.LCDUILock) {
            if (numOfItems == 0 || traverseIndex < 0) {
                return;
            }

            i = items[traverseIndex];
        } // synchronized


        // SYNC NOTE: formMode can only change as a result of a
        // traversal, which can only occur serially on the event
        // thread, so its safe to use it outside of the lock

        if (keyCode == Display.KEYCODE_UP
            || keyCode == Display.KEYCODE_DOWN
            || keyCode == Display.KEYCODE_LEFT
            || keyCode == Display.KEYCODE_RIGHT) {
            traverse(Display.getGameAction(keyCode));
            return;
        }

        // SYNC NOTE: callKeyPressed may result in a call to the
        // application, so we make sure we do this outside of the
        // LCDUILock
        if (i != null) {
            // pass the keypress onto the current item
            i.callKeyPressed(keyCode);
        }
    }

    /**
     * Handle a key release event
     *
     * @param keyCode the key which was released
     */
    void callKeyReleased(int keyCode) {
        Item i = null;

        synchronized (Display.LCDUILock) {
            if (numOfItems == 0 || traverseIndex < 0) {
                return;
            }

            i = items[traverseIndex];
        }

        // SYNC NOTE: this call may result in a call to the
        // application, so we make sure we do this outside of the
        // LCDUILock
        i.callKeyReleased(keyCode);
    }

    /**
     * Handle a key repeat
     *
     * @param keyCode the key code of the key which was repeated
     */
    void callKeyRepeated(int keyCode) {
        if (keyCode == Display.KEYCODE_UP
            || keyCode == Display.KEYCODE_DOWN
            || keyCode == Display.KEYCODE_LEFT
            || keyCode == Display.KEYCODE_RIGHT) {
            traverse(Display.getGameAction(keyCode));
        }
    }

    /**
     * Handle a typed key from a keyboard
     *
     * @param c The char typed from the keyboard
     */
    void callKeyTyped(char c) {
        Item i = null;

        synchronized (Display.LCDUILock) {
            if (numOfItems == 0 || traverseIndex < 0) {
                return;
            }

            i = items[traverseIndex];
        }

        // SYNC NOTE: this call may result in a call to the
        // application, so we make sure we do this outside of the
        // LCDUILock
        i.callKeyTyped(c);
    }

    /**
     * Handle a pointer pressed event
     *
     * @param x The x coordinate of the press
     * @param y The y coordinate of the press
     */
    void callPointerPressed(int x, int y) {
        Item i = null;

        synchronized (Display.LCDUILock) {
            if (numOfItems == 0 || traverseIndex < 0) {
                return;
            }

            i = items[traverseIndex];

            x = (x - viewport[X] + view[X]) - i.bounds[X];
            y = (y - viewport[Y] + view[Y]) - i.bounds[Y];

            if (x < 0 
                || x > i.bounds[WIDTH]
                || y < 0
                || y > i.bounds[HEIGHT]) {
                return;
            }

            pointerPressed = true;
        }

        // SYNC NOTE: this call may result in a call to the
        // application, so we make sure we do this outside of the
        // LCDUILock
        i.callPointerPressed(x, y);
    }

    /**
     * Handle a pointer released event
     *
     * @param x The x coordinate of the release
     * @param y The y coordinate of the release
     */
    void callPointerReleased(int x, int y) {
        Item i = null;

        synchronized (Display.LCDUILock) {
            if (numOfItems == 0 || traverseIndex < 0 || !pointerPressed) {
                return;
            }

            i = items[traverseIndex];

            x = (x - viewport[X] + view[X]) - i.bounds[X];
            y = (y - viewport[Y] + view[Y]) - i.bounds[Y];

            pointerPressed = false;

        }

        // SYNC NOTE: this call may result in a call to the
        // application, so we make sure we do this outside of the
        // LCDUILock
        i.callPointerReleased(x, y);
    }

    /**
     * Handle a pointer dragged event
     *
     * @param x The x coordinate of the drag
     * @param y The y coordinate of the drag
     */
    void callPointerDragged(int x, int y) {
        Item i = null;

        synchronized (Display.LCDUILock) {
            if (numOfItems == 0 || traverseIndex < 0 || !pointerPressed) {
                return;
            }

            i = items[traverseIndex];

            x = (x - viewport[X] + view[X]) - i.bounds[X];
            y = (y - viewport[Y] + view[Y]) - i.bounds[Y];

        }

        // SYNC NOTE: this call may result in a call to the
        // application, so we make sure we do this outside of the
        // LCDUILock
        i.callPointerDragged(x, y);
    }

    /**
     * The generic traverse method. This method determines the
     * current "mode" of traversal and delegates the traverse
     * to the appropriate handler, either formTraverse(),
     * itemTraverse()
     *
     * @param dir the direction of "traversal"
     */
    void traverse(int dir) {
        // SYNC NOTE: formMode can only ever change as a result of a
        // traverse, which means its single-threaded (on the event thread)
        switch (formMode) {
        case FORM_TRAVERSE:
            try {
                formTraverse(dir);
            } catch (Throwable t) {
                // Swallow the error and move on
            }
            break;
        case ITEM_TRAVERSE:
            try {
                if (!itemTraverse(dir)) {
                    formTraverse(dir);
                }
            } catch (Throwable t) {
                // Swallow the error and move on
            }
            break;
        }
    }

    /**
     * Traverse this Form in the given direction
     *
     * @param dir the direction of traversal
     */
    void formTraverse(int dir) {
        // SYNC NOTE: rather than copy datasets all the time, we simply
        // protect ourselves from unexpected errors using a try/catch in
        // the traverse() method itself. The only problem that could occur
        // would be if the application added or removed Items on the fly
        // while the user was traversing. This could lead to an error
        // condition, but it would be rectified immediately by the
        // pending validation caused by the append/delete. This seems
        // preferable to wasting a lot of heap or employing complicating
        // locking behavior

        if (numOfItems == 0) {
            return;
        }

        // if that is the initial traverse in the form,
        // do it first
        if (dir == CustomItem.NONE) {
            setTraverseIndex(dir, traverseIndex, 
                             traverseIndex == -1 ? 0 : traverseIndex);

            return;
        }

        // If we need to scroll to fit the current item,
        // don't do a traverse, just return
        if (traverseIndex >= 0 &&
            !items[traverseIndex].shouldSkipTraverse() &&
            scrollForBounds(dir, items[traverseIndex].bounds)) {              
                validateVisibility = true;
                repaintContents();

                return;
        }

        int bendDir = dir;
        // We know we must do a "traversal". We always
        // perform a "direction bend" to achieve our
        // desired traversal semantics
        if (dir == Canvas.DOWN) {
            bendDir = Canvas.RIGHT;
        } else if (dir == Canvas.UP) {
            bendDir = Canvas.LEFT;
        }

        // Find new traverseIndex 
        int oldIndex, newIndex = traverseIndex;
            
        do {

            oldIndex = newIndex;

            // Traverse to the next Item on the Form in the
            // given direction
            switch (bendDir) {
            case Canvas.UP:
                int ni1 = findNearestNeighborUp();
                if (ni1 != -1) {
                    newIndex = ni1;
                }
                break;
            case Canvas.DOWN:
                int ni2 = findNearestNeighborDown();
                if (ni2 != -1) {
                    newIndex = ni2;
                }
                break;
            case Canvas.LEFT:
                if (newIndex > 0) {
                    newIndex--;
                }
                break;
            case Canvas.RIGHT:
                if (newIndex < (numOfItems - 1)) {
                    newIndex++;
                }
                break;
            }

            // If we can't traverse any further in the given 
            // direction, return 
            if (oldIndex == newIndex) {
                return;
            }

        } while (items[newIndex].shouldSkipTraverse());

        setTraverseIndex(dir, traverseIndex, newIndex);
    }

    /**
     * Sets traverseIndex on this Form
     * 
     * @param dir the direction of traversal
     * @param oldIndex the old traverseIndex
     * @param newIndex the new traverseIndex to be set
     */
    void setTraverseIndex(int dir, int oldIndex, int newIndex) {
        // SYNC NOTE: rather than copy datasets all the time, we simply
        // protect ourselves from unexpected errors using a try/catch in
        // the traverse() method itself. The only problem that could occur
        // would be if the application added or removed Items on the fly
        // while the user was traversing. This could lead to an error
        // condition, but it would be rectified immediately by the
        // pending validation caused by the append/delete. This seems
        // preferable to wasting a lot of heap or employing complicating
        // locking behavior

        if (dir == CustomItem.NONE) {
            // Make sure that initial traverseIndex is 
            // not pointing to an item that is not traversable
            int newTraveseIndex = newIndex;

            // find first item with index from traverseIndex till 
            // numOfItems-1 that is traversable
            boolean allSkipped = false;
            while (!allSkipped && items[newIndex].shouldSkipTraverse()) {
                newIndex++;
                if (newIndex == numOfItems) {
                    allSkipped = true;
                }
            }

            // if all items with index from traverseIndex till numOfItems-1 
            // were skipped, we need to check if any of the items with index
            // from traverseIndex-1 till 0 that is traversable
            if (allSkipped) {
                if (newTraveseIndex > 0) {
                    newIndex = newTraveseIndex - 1;
                    while (items[newIndex].shouldSkipTraverse()) {
                        newIndex--;
                        if (newIndex == -1) {
                            return;
                        }
                    } 
                } else {
                    return;
                }
            }
        }

        formMode = FORM_TRAVERSE;

        traverseIndex = newIndex;

        if (oldIndex >= 0 && oldIndex < numOfItems) {
            items[oldIndex].callTraverseOut();
        }

        // setTraverseIndex is called from setCurrentItem.
        // It is possible that there was no layout done all.
        // In that case just return and layout will be done
        // when this form is first shown
        if (items[traverseIndex].bounds == null) {
            return;
        }

        if (dir == CustomItem.NONE) {
            dir = newIndex >= oldIndex ? Canvas.DOWN : Canvas.UP;
        }

        // If the newly traversed-to Item is outside the
        // viewport, we change our mode to FORM_SCROLL
        // so that subsequent 'traverses' will scroll the viewport
        // and bring the Item into view
        scrollForTraversal(dir, items[traverseIndex].bounds);
        itemTraverse(dir);
        validateVisibility = true;
        repaintContents();
        updateCommandSet();
        // FIX ME: improve the painting, a full repaintContents() should not
        // always be necessary
    }

    /**
     * Perform an internal traverse on the currently traversed-to
     * Item in the given direction.
     *
     * @param dir the direction of traversal
     * @return true if this item performed internal traversal
     */
    boolean itemTraverse(int dir) {
        // SYNC NOTE: rather than copy datasets all the time, we simply
        // protect ourselves from unexpected errors using a try/catch in
        // the traverse() method itself. The only problem that could occur
        // would be if the application added or removed Items on the fly
        // while the user was traversing. This could lead to an error
        // condition, but it would be rectified immediately by the
        // pending validation caused by the append/delete. This seems
        // preferable to wasting a lot of heap or employing complicating
        // locking behavior

        if (traverseIndex == -1) {
            return false;
        }

        // If we need to scroll to fit the current internal traversal
        // bounds, just return
        if (formMode == ITEM_TRAVERSE && scrollForBounds(dir, visRect)) {
            validateVisibility = true;
            repaintContents();
            return true;
        }

        visRect[X] = visRect[Y] = 0;
        visRect[WIDTH] = items[traverseIndex].bounds[WIDTH];
        visRect[HEIGHT] = items[traverseIndex].bounds[HEIGHT];

        if (items[traverseIndex].callTraverse(dir, viewport[WIDTH],
                                              viewport[HEIGHT], visRect)) {

            // Since visRect is sent to the Item in its own coordinate
            // space, we translate it back into the overall Form's
            // coordinate space
            visRect[X] += items[traverseIndex].bounds[X];
            visRect[Y] += items[traverseIndex].bounds[Y];

            formMode = ITEM_TRAVERSE;

            if (scrollForTraversal(dir, visRect)) {
                validateVisibility = true;
                repaintContents();
            }

            return true;

        } else {
            visRect[X] += items[traverseIndex].bounds[X];
            visRect[Y] += items[traverseIndex].bounds[Y];
            return false;
        }
    }

    /**
     * Find the nearest neighbor to the current traversed-to Item
     * moving upward
     *
     * @return the index of the nearest neighbor up
     */
    int findNearestNeighborUp() {
        // SYNC NOTE: see traverse()

        if (traverseIndex == -1) {
            return 0;
        }

        int a1 = items[traverseIndex].bounds[X];
        int b1 = items[traverseIndex].bounds[Y];
        int a2 = a1 + items[traverseIndex].bounds[WIDTH];
        int b2 = b1 + items[traverseIndex].bounds[HEIGHT];

        b1--;
        b2--;

        int x1, y1, x2, y2;

        while (b1 >= 0) {
            for (int i = traverseIndex - 1; i >= 0; i--) {
                x1 = items[i].bounds[X];
                y1 = items[i].bounds[Y];
                x2 = x1 + items[i].bounds[WIDTH];
                y2 = y1 + items[i].bounds[HEIGHT];

                x1 = (a1 > x1) ? a1: x1;
                y1 = (b1 > y1) ? b1: y1;
                x2 = (a2 < x2) ? a2: x2;
                y2 = (b2 < y2) ? b2: y2;

                if (x2 >= x1 & y2 >= y1) {
                    return i;
                }
            }

            b1 = b1 - CELL_SPACING;
        }

        return -1;
    }

    /**
     * Find the nearest neighbor to the current traversed-to Item
     * moving downward
     *
     * @return the index of the nearest neighbor down
     */
    int findNearestNeighborDown() {
        // SYNC NOTE: see traverse()

        if (traverseIndex == -1) {
            return 0;
        }

        int a1 = items[traverseIndex].bounds[X];
        int b1 = items[traverseIndex].bounds[Y];
        int a2 = a1 + items[traverseIndex].bounds[WIDTH];
        int b2 = b1 + items[traverseIndex].bounds[HEIGHT];

        b2++;

        int x1, y1, x2, y2;
        int greatestY = -1;

        while (true) {
            for (int i = traverseIndex + 1; i < numOfItems; i++) {
                x1 = items[i].bounds[X];
                y1 = items[i].bounds[Y];
                x2 = x1 + items[i].bounds[WIDTH];
                y2 = y1 + items[i].bounds[HEIGHT];

                if (y2 > greatestY) {
                    greatestY = y2;
                }

                x1 = (a1 > x1) ? a1: x1;
                y1 = (b1 > y1) ? b1: y1;
                x2 = (a2 < x2) ? a2: x2;
                y2 = (b2 < y2) ? b2: y2;

                if (x2 >= x1 & y2 >= y1) {
                    return i;
                }
            }

            b2 = b2 + CELL_SPACING;

            if (b2 > greatestY) {
                break;
            }
        }

        return -1;
    }

    /**
     * Scroll the viewport to fit the bounds after an intial traversal
     * has been made
     *
     * @param dir the direction of traversal into the Item
     * @param bounds the bounding box of the traversal location
     * @return True if the viewport had to be scrolled to fit the item
     */
    boolean scrollForBounds(int dir, int[] bounds) {
        // SYNC NOTE: see traverse()

        // We can just short circuit scrolling all together if
        // we know our view is smaller than the viewport
        if (view[HEIGHT] < viewport[HEIGHT]) {
            return false;
        }

        switch (dir) {
            case Canvas.UP:
                if (bounds[Y] >= view[Y]) {
                    return false;
                } else {
                    view[Y] -= Screen.CONTENT_HEIGHT;
                    if (view[Y] < 0) {
                        view[Y] = 0;
                    }
                    return true;
                }
            case Canvas.DOWN:
                if (bounds[Y] + bounds[HEIGHT]  + CELL_SPACING <=
                        view[Y] + viewport[HEIGHT]) {
                    return false;
                } else {
                    view[Y] += Screen.CONTENT_HEIGHT;
                    if (view[Y] > view[HEIGHT] - viewport[HEIGHT]) {
                        view[Y] = view[HEIGHT] - viewport[HEIGHT];
                    }
                    return true;
                }
            case Canvas.LEFT:
                // we don't scroll horizontally just yet
                break;
            case Canvas.RIGHT:
                // we don't scroll horizontally just yet
                break;
        }
        return false;
    }

    /**
     * Scroll the viewport to fit the item when initially
     * traversing to the Item (or within the Item) in the given direction.
     *
     * @param dir the direction of traversal into the Item
     * @param bounds the bounding box of the traversal location
     * @return True if the viewport had to be scrolled to fit the item
     */
    boolean scrollForTraversal(int dir, int[] bounds) {
        // SYNC NOTE: see traverse()

        // We can just short circuit scrolling all together if
        // we know our view is smaller than the viewport
        if (view[HEIGHT] < viewport[HEIGHT]) {
            if (view[Y] > 0) {
                view[Y] = 0;
                return true;
            }
            return false;
        }

        // If the bounds are fully in our view, just return false
        if (bounds[Y] > view[Y] &&
                (bounds[Y] + bounds[HEIGHT] < view[Y] + viewport[HEIGHT])) {
            return false;
        } else {
            if (SCROLLS_VERTICAL) {
                if (bounds[HEIGHT] > viewport[HEIGHT]) {
                    if (dir == Canvas.DOWN || dir == Canvas.LEFT ||
                            dir == CustomItem.NONE) {
                        view[Y] = bounds[Y] - CELL_SPACING;
                    } else if (dir == Canvas.UP || dir == Canvas.RIGHT) {
                        view[Y] = bounds[Y] + bounds[HEIGHT] + CELL_SPACING -
                            viewport[HEIGHT];
                    }
                } else {
                    if (dir == Canvas.DOWN || dir == Canvas.LEFT ||
                            dir == CustomItem.NONE) {
                        view[Y] = bounds[Y] + bounds[HEIGHT] + CELL_SPACING -
                            viewport[HEIGHT];
                    } else if (dir == Canvas.UP || dir == Canvas.RIGHT) {
                        view[Y] = bounds[Y] - CELL_SPACING;
                    }
                }

                if ((view[Y] + viewport[HEIGHT]) > view[HEIGHT]) {
                    view[Y] = view[HEIGHT] - viewport[HEIGHT];
                }
                if (view[Y] < 0) {
                    view[Y] = 0;
                }
                return true;
            } else if (SCROLLS_HORIZONTAL) {
                // Not supported
            }
        }

        return false;
    }

    /**
     * Layout the contents of this Form, and call super.layout() to layout
     * the contents of the parent Screen or Displayable
     */
    void layout() {
        // SYNC NOTE: layout() is always called from within a hold
        // on LCDUILock

        super.layout();

        // If we don't have any Items, just return
        if (numOfItems == 0) {
            return;
        }

        // The index of the first Item in the horizontal row
        int rowStart = 0;

        // The 'viewable' represents the viewpane. It starts out life the
        // size of the viewport, but gets whittled down as each Item gets
        // laid out and occupies space in it. It effectively keeps a running
        // total of what space is available due to the Items which have
        // already been laid out
        if (viewable == null) {
            viewable = new int[4];
        }

        // We only allow space for the traversal indicator if we
        // have more than one item - because we only draw the traversal
        // indicator if we have more than one item to traverse to.
        // View's width is set to the maximum allowable width,
        // while view's height is initialized with initial padding and
        // and grows when new row is added.
        if (numOfItems > 1) {
            viewable[X] = CELL_SPACING;
            viewable[Y] = CELL_SPACING;
            viewable[WIDTH] = viewport[WIDTH] - CELL_SPACING;
            viewable[HEIGHT] = viewport[HEIGHT] - CELL_SPACING;

            view[WIDTH] = viewable[WIDTH] - CELL_SPACING;
            view[HEIGHT] = CELL_SPACING;

        } else {
            viewable[X] = 1;
            viewable[Y] = 1;
            viewable[WIDTH] = viewport[WIDTH] - 1;
            viewable[HEIGHT] = viewport[HEIGHT] - 1;

            view[WIDTH] = viewable[WIDTH] - 1;
            view[HEIGHT] = 1;
        }

        // A running variable which maintains the height of the
        // tallest item on a line
        int lineHeight = 0;
        int pW, pH;

        // We loop through all the Items. NTS: we may be able to improve
        // this given we know which Item called invalidate()f;
        for (int index = 0; index < numOfItems; index++) {
            
            // If the Item can be shrunken, get its minimum width,
            // and its preferred if it is not
            if (items[index].shouldHShrink()) {
                pW = items[index].callMinimumWidth();
            } else {
                if (items[index].lockedWidth != -1) {
                    pW = items[index].lockedWidth;
                } else {
                    // if height is locked default preferred width
                    // will be used, otherwise width will be calculated
                    // based on lockedHeight
                    pW = items[index].callPreferredWidth(
                                      items[index].lockedHeight);
                }
            }
        
            // We have a notion of the maximum allowable width an Item can
            // have, so we enforce it first here:
            if (!SCROLLS_HORIZONTAL && (pW > view[WIDTH])) {
                pW = view[WIDTH];
            }

            // We use a separate boolean here to check for each case of
            // requiring a new line (rather than the if() from hell)
            boolean newLine = (index > 0 && items[index - 1].equateNLA());
            newLine = (newLine || items[index].equateNLB());
            newLine = (newLine || (pW > (viewable[WIDTH] - CELL_SPACING)));

            // We linebreak if there is an existing row
            if (newLine && (lineHeight > 0)) {

                // First, handle current row's layout directives
                try {
                    lineHeight = layoutRowHorizontal(rowStart, index - 1, 
                                                     viewable[WIDTH],
                                                     lineHeight);
                    layoutRowVertical(rowStart, index - 1, lineHeight);
                    if (numOfItems > 1) {
                        view[HEIGHT] += lineHeight + CELL_SPACING;
                    } else {
                        view[HEIGHT] += lineHeight + 1;
                    }
                } catch (Throwable t) {
                    Display.handleThrowable(t);
                }

                // Then, reset the viewable, lineHeight, and rowStart
                viewable[X] = CELL_SPACING;
                viewable[Y] = view[HEIGHT];
                viewable[WIDTH] = viewport[WIDTH] - CELL_SPACING;
                viewable[HEIGHT] -= (lineHeight + CELL_SPACING);

                lineHeight = 0;
                rowStart = index;
            }

            pH = getItemHeight(index, pW);

            // If the Item has never been laid out before, instantiate
            // its bounds[]
            if (items[index].bounds == null) {
                items[index].bounds = new int[4];
            }

            // If the Item is changing size, set the flag so that callPaint()
            // will call the Item's sizeChanged() method before painting
            if (items[index].bounds[WIDTH] != pW ||
                items[index].bounds[HEIGHT] != pH) {
                items[index].sizeChanged = true;
            }

            // We assign the item a bounds which is its pixel location,
            // width, and height in coordinates which represent offsets
            // of the viewport origin (that is, are in the viewport
            // coordinate space)
            items[index].bounds[X] = viewable[X];
            items[index].bounds[Y] = viewable[Y];
            items[index].bounds[WIDTH] = pW;
            items[index].bounds[HEIGHT] = pH;

            /*
            System.err.println("index: " +index);
            System.err.print("\t" + items[index].bounds[X]);
            System.err.print(", " + items[index].bounds[Y]);
            System.err.print(", " + items[index].bounds[WIDTH]);
            System.err.println(", " + items[index].bounds[HEIGHT]);
            */

            // If this Item is currently the tallest on the line, its
            // height becomes our prevailing lineheight
            if (pH > lineHeight) {
                lineHeight = pH;
            }

            // We whittle down the viewpane by the Item's dimensions,
            // effectively maintaining how much space is left for the
            // remaining Items, IFF the item has some positive width
            if (pW > 0) {
                viewable[WIDTH] -= (pW + CELL_SPACING);
                viewable[X]     += (pW + CELL_SPACING);
            }

        } // for

        // Before we quit, layout the last row we did in the loop
        try {
            lineHeight = layoutRowHorizontal(rowStart, numOfItems-1, 
                                             viewable[WIDTH], lineHeight);
            layoutRowVertical(rowStart, numOfItems-1, lineHeight);
            if (numOfItems > 1) {
                view[HEIGHT] += lineHeight + CELL_SPACING;
            } else {
                view[HEIGHT] += lineHeight + 1;
            }
        } catch (Throwable t) {
            Display.handleThrowable(t);
        }
    }

    /**
     * Paint the contents of this Form
     *
     * @param g the Graphics object to paint on
     * @param target the target Object of this repaint
     */
    void callPaint(Graphics g, Object target) {
        super.callPaint(g, target);

        /*
        System.err.println("Form:Clip: " +
            g.getClipX() + "," + g.getClipY() + "," +
            g.getClipWidth() + "," + g.getClipHeight());

        System.err.println("numOfItems: " + numOfItems);
        */

        // SYNC NOTE: We cannot hold any lock around a call into
        // the application's paint() routine. Rather than copy
        // the dataset and expend heap space, we simply protect
        // this operation with try/catch. The only error condition
        // would be if an insert/append/delete occurred in the middle
        // of painting. This error condition would be quickly remedied
        // by the pending validation of that change which causes a
        // repaint automatically

        try {
            if (numOfItems == 0) {
                return;
            }

            int clip[] = new int[4];
            clip[X] = g.getClipX();
            clip[Y] = g.getClipY();
            clip[WIDTH] = g.getClipWidth();
            clip[HEIGHT] = g.getClipHeight();

            // If the clip is an area above our viewport, just return
            if (clip[Y] + clip[HEIGHT] <= viewport[Y]) {
                return;
            }

            if (SCROLLS_VERTICAL) {
                setVerticalScroll();
            }

            if (SCROLLS_HORIZONTAL) {
                // setHorizontalScroll();
            }

            if (target instanceof Item) {
                if (((Item)target).owner == this) {
                    paintItem((Item)target, g, clip);
                }
            } else {
                for (int i = 0; i < numOfItems; i++) {
                    paintItem(items[i], g, clip);
                }
            }
        } catch (Throwable t) {
            // Swallow the error and continue
        }
    }

    /**
     * Paint an item
     *
     * @param item the Item to paint
     * @param g the Graphics object to paint to
     * @param clip the original graphics clip to restore
     */
    void paintItem(Item item, Graphics g, int[] clip) {
        // SYNC NOTE: see callPaint()

        // NOTE: Its possible, that an Item is in an invalid state
        // during a requested repaint. Its ok to simply return,
        // because it means there is a validation event coming on
        // the event thread. When the form re-validates, the Item
        // will be given a proper bounds and will be repainted
        if (item.bounds == null) {
            return;
        }

        int tX = item.bounds[X] + viewport[X] - view[X];
        int tY = item.bounds[Y] + viewport[Y] - view[Y];

        // If we're already beyond the clip, quit looping, as long
        // as we're not validating the visibility of Items after a
        // scroll (calling show/hideNotify())
        if (((tY + item.bounds[HEIGHT] < clip[Y]) || 
             (tY > (clip[Y] + clip[HEIGHT]))) && 
            !validateVisibility) {
            return;
        }

        // Clip the dirty region to only include the item
        g.clipRect(tX, tY, item.bounds[WIDTH], item.bounds[HEIGHT]);

        // If the Item is inside the clip, go ahead and paint it
        if (g.getClipWidth() > 0 && g.getClipHeight() > 0) {

            if (validateVisibility && !item.visible) {
                item.callShowNotify();
            }

            if (item.sizeChanged) {
                item.callSizeChanged(
                    item.bounds[WIDTH], item.bounds[HEIGHT]);
                item.sizeChanged = false;
            }

            // Translate into the Item's coordinate space
            g.translate(tX, tY);

            // NYI: call showNotify() on the Item first

            // We translate the Graphics into the Item's
            // coordinate space
            item.callPaint(g, item.bounds[WIDTH], item.bounds[HEIGHT]);

            // We don't need to do a translate because we are
            // doing a reset() below that will negate it

        } else if (validateVisibility && item.visible) {
            item.callHideNotify();
        }

        // Restore the clip to its original context so
        // future clipRect() calls will have the correct intersection
        g.reset(clip[X], clip[Y],
                clip[X] + clip[WIDTH], clip[Y] + clip[HEIGHT]);

        // Paint the traversal indicator
        if (traverseIndex >= 0 && numOfItems > 1 &&
            item == items[traverseIndex] && indicateTraverse) {

            g.clipRect(tX - CELL_SPACING, tY - CELL_SPACING,
                       item.bounds[WIDTH] + (2 * CELL_SPACING),
                       item.bounds[HEIGHT] + (2 * CELL_SPACING));
            paintTraversalIndicator(g, tX, tY);
            g.setClip(clip[X], clip[Y], clip[WIDTH], clip[HEIGHT]);
        }
    }

    /**
     * Paint the traversal indicator. The width/height are obtained from
     * the current traversal item's bounds.
     *
     * @param g the Graphics to paint on
     * @param x the x orign coordinate to paint the traversal indicator
     * @param y the y origin coordinate to paint the traversal indicator
     */
    void paintTraversalIndicator(Graphics g, int x, int y) {
        // SYNC NOTE: see callPaint()

        // NTS: This may need to special case StringItem?
        g.setColor(TRAVERSE_INDICATOR_COLOR);
        if (TRAVERSE_INDICATOR == ONE_PIXEL_BOX) {
            g.drawRect(x - (CELL_SPACING - 1), y - (CELL_SPACING - 1),
                    items[traverseIndex].bounds[WIDTH] + (CELL_SPACING + 1),
                    items[traverseIndex].bounds[HEIGHT] + (CELL_SPACING + 1));
        } else if (TRAVERSE_INDICATOR == TRIANGLE_CORNERS) {
            g.fillTriangle(items[traverseIndex].bounds[WIDTH] + x,
                           y,
                           items[traverseIndex].bounds[WIDTH] + x,
                           y - (CELL_SPACING),
                           items[traverseIndex].bounds[WIDTH] + x +
                                (CELL_SPACING),
                           y);
            g.fillTriangle(x,
                           items[traverseIndex].bounds[HEIGHT] + y,
                           x,
                           items[traverseIndex].bounds[HEIGHT] + y +
                                (CELL_SPACING - 1),
                           x - (CELL_SPACING - 1),
                           items[traverseIndex].bounds[HEIGHT] + y);
            /*
            This code block puts the triangles at the upper left
            and lower right corners
            g.fillTriangle(x, y,
                           x, y - (CELL_SPACING),
                           x - (CELL_SPACING), y);
            g.fillTriangle(items[traverseIndex].bounds[WIDTH] + x,
                           items[traverseIndex].bounds[HEIGHT] + y,
                           items[traverseIndex].bounds[WIDTH] + x +
                                (CELL_SPACING - 1),
                           items[traverseIndex].bounds[HEIGHT] + y,
                           items[traverseIndex].bounds[WIDTH] + x,
                           items[traverseIndex].bounds[HEIGHT] + y +
                                (CELL_SPACING - 1));
            */
        }
        g.setColor(Display.FG_COLOR);
    }

    /**
     * Retrieve the ItemStateListener for this Form.
     * NOTE: calls to this method should only occur from within
     * a lock on LCDUILock.
     *
     * @return ItemStateListener The ItemStateListener of this Form,
     *                           null if there isn't one set
     */
    ItemStateListener getItemStateListener() {
        return itemStateListener;
    }

    /**
     * Gets item currently in focus. This is will be only applicable to
     * Form. The rest of the subclasses will return null.
     * @return the item currently in focus in this Displayable;
     *          if there are no items in focus, null is returned
     */
    Item getCurrentItem() {
        // SYNC NOTE: getCurrentItem is always called from within
        // a hold on LCDUILock
        return traverseIndex < 0 ? null : items[traverseIndex];
    }

    /**
     * Re-validate the contents of this Form, possibly due to an
     * individual item
     *
     * @param item the Item causing the invalidation (may be null)
     */
    void callInvalidate(Item item) {
        if (!paintDelegate.isShown()) {
            return;
        }

        synchronized (Display.LCDUILock) {
            // If an Item is now invalid, we layout the Form, repaint it,
            // and do a traverse to the current item with the "NONE"
            // direction.
            layout();
        }

        repaintContents();
        traverse(CustomItem.NONE);
    }

    /**
     * Used by the event handler to notify the ItemStateListener
     * of a change in the given item
     *
     * @param item the Item which state was changed
     */
    void callItemStateChanged(Item item) {
        // get a copy of the object reference to ItemStateListener
        ItemStateListener isl = itemStateListener;
        if (isl == null || item == null) {
            return;
        }

        // Protect from any unexpected application exceptions
        try {
            // SYNC NOTE: We lock on calloutLock around any calls
            // into application code
            synchronized (Display.calloutLock) {
                isl.itemStateChanged(item);
            }
        } catch (Throwable thr) {
            Display.handleThrowable(thr);
        }
    }

// ************************************************************
//  private methods
// ************************************************************

    /**
     * Get item's height based on the width
     *
     * @param index the index of the item which height is being calculated 
     * @param pW the width set for the item
     * @return the height of the item
     */
    private int getItemHeight(int index, int pW) {
        // SYNC NOTE: protected by the lock around calls to layout()

        int pH;

        // If the Item can be shrunken, we use its minimum height,
        // and its preferred height if it is not
        if (items[index].shouldVShrink()) {
            pH = items[index].callMinimumHeight();
        } else {
            pH = items[index].lockedHeight;
            if (pH == -1) {
                pH = items[index].callPreferredHeight(pW);
            }
        }
        
        // If we can't scroll vertically, clip the item's height
        // if it can't fit in the view. We would also enforce a
        // notion of a "maximum vertical height" here if we had one
        if (!SCROLLS_VERTICAL &&
            (pH > (viewport[HEIGHT] - CELL_SPACING - CELL_SPACING))) {
            pH = viewport[HEIGHT] - CELL_SPACING - CELL_SPACING;
        }
    
        return pH;
    }

    /**
     * After the contents of a row have been determined, layout the
     * items on that row, taking into account the individual items'
     * horizontally oriented layout directives.
     *
     * @param rowStart the index of the first row element
     * @param rowEnd the index of the last row element
     * @param hSpace the amount of empty space in pixels in this row before 
     *              inflation
     * @param rowHeight the old row height
     * @return the new rowHeight for this row after all of the inflations
     */
    private int layoutRowHorizontal(int rowStart, int rowEnd,
                                    int hSpace, int rowHeight) {
        // SYNC NOTE: protected by the lock around calls to layout()

        hSpace = inflateHShrinkables(rowStart, rowEnd, hSpace);
        hSpace = inflateHExpandables(rowStart, rowEnd, hSpace);


        // if any of the items were inflated we have to recalculate
        // the new row height for this row
        rowHeight = 0;
        for (int i = rowStart; i <= rowEnd; i++) {
            if (rowHeight < items[i].bounds[HEIGHT]) {
                rowHeight = items[i].bounds[HEIGHT];
            }
        }

        if (hSpace == 0) {
            return rowHeight;
        }

        // layout the group of items with LAYOUT_RIGHT layout
        for (; rowStart <= rowEnd; rowEnd--) {

            int layout = items[rowEnd].callGetLayout();
            if ((layout & LAYOUT_HMASK) != Item.LAYOUT_RIGHT) {
                break;
            }

            items[rowEnd].bounds[X] += hSpace;
        }
 
        int hLayout = 0;

        // skip the group of items with LAYOUT_LEFT layout;
        // layout the group of items with LAYOUT_CENTER layout by
        // shifting  x bound to the right by half the amount of the empty space

        hSpace = hSpace/2;

        for (; rowStart <= rowEnd; rowStart++) {

            hLayout = items[rowStart].callGetLayout() & LAYOUT_HMASK;
            if (hLayout == Item.LAYOUT_LEFT) {
                continue;
            } else if (hLayout != Item.LAYOUT_CENTER) {
                break;
            }

            items[rowStart].bounds[X] += hSpace;
        }
   
        return rowHeight;
    }

    /**
     * Inflate all the horizontally 'shrinkable' items on a row
     *
     * @param rowStart the index of the first row element
     * @param rowEnd the index of the last row element
     * @param space the amount of empty space left in pixels in this row
     * @return the amount of empty space on this row after shinkage
     */
    private int inflateHShrinkables(int rowStart, int rowEnd, int space) {
        // SYNC NOTE: protected by the lock around calls to layout()

        if (space == 0) {
            return 0;
        }

        // To inflate shrinkables, we make a first pass gathering
        // the smallest proportion (the baseline)
        int baseline = Integer.MAX_VALUE;
        int pW, prop = 0;

        for (int i = rowStart; i <= rowEnd; i++) {
            if (items[i].shouldHShrink()) {
                pW = items[i].lockedWidth;
                if (pW == -1) {
                    pW = items[i].callPreferredWidth(items[i].lockedHeight);
                }
                prop = pW - items[i].getMinimumWidth();
                if (prop > 0 && prop < baseline) {
                    baseline = prop;
                }
            }
        }

        // If there are no shrinkables, return;
        if (baseline == Integer.MAX_VALUE) {
            return space;
        }

        prop = 0;

        // Now we loop again, adding up all the proportions so
        // we can find the adder
        for (int i = rowStart; i <= rowEnd; i++) {
            if (items[i].shouldHShrink()) {
                pW = items[i].lockedWidth;
                if (pW == -1) {
                    pW = items[i].callPreferredWidth(items[i].lockedHeight);
                }
                prop += ((pW - items[i].getMinimumWidth()) / baseline);
            }
        }


        // Now we compute the adder, and add it to each of the
        // shrinkables, times its proportion
        int adder = space / prop;

        for (int i = rowStart; i <= rowEnd; i++) {
            if (items[i].shouldHShrink()) {
                pW = items[i].lockedWidth;
                if (pW == -1) {
                    pW = items[i].callPreferredWidth(items[i].lockedHeight);
                }
                space = pW - items[i].getMinimumWidth();

                // The proportionate amount of space to add
                prop = adder * (space / baseline);

                // We only enlarge the item to its preferred width at
                // a maximum
                if (space > prop) {
                    space = prop;
                }

                items[i].bounds[WIDTH] += space;
                items[i].bounds[HEIGHT] = 
                    getItemHeight(i, items[i].bounds[WIDTH]);

                // Now we have to shift the rest of the elements on the line
                for (int j = i + 1; j <= rowEnd; j++) {
                    items[j].bounds[X] += space;
                }
            }
        }

        // NTS: If an item only enlarges to its preferred size, it throws
        // off the algorithm a bit in that there will be empty space left
        // over. Shouldn't really matter though

        space = viewport[WIDTH] - CELL_SPACING -
                (items[rowEnd].bounds[X] + items[rowEnd].bounds[WIDTH]);

        return space;
    }

    /**
    * Inflate all the horizontally 'expandable' items on a row
    *
    * @param rowStart the index of the first row element
    * @param rowEnd the index of the last row element
    * @param space the amount of empty space on this row
    * @return the amount of empty space after expansion
    */
    private int inflateHExpandables(int rowStart, int rowEnd, int space) {
        // SYNC NOTE: protected by the lock around calls to layout()

        if (space == 0) {
            return 0;
        }

        int numExp = 0;
        // We first loop through and count the expandables
        for (int i = rowStart; i <= rowEnd; i++) {
            if (items[i].shouldHExpand()) {
                numExp++;
            }
        }

        if (numExp == 0 || space < numExp) {
            return space;
        }

        space = space / numExp;

        // We then add the same amount to each Expandable
        for (int i = rowStart; i <= rowEnd; i++) {
            if (items[i].shouldHExpand()) {
                items[i].bounds[WIDTH] += space;
                items[i].bounds[HEIGHT] = 
                    getItemHeight(i, items[i].bounds[WIDTH]); 

                // We right shift each subsequent item on the row
                for (int j = i + 1; j <= rowEnd; j++) {
                    items[j].bounds[X] += space;
                }
            }
        }

        space = viewport[WIDTH] - CELL_SPACING -
                (items[rowEnd].bounds[X] + items[rowEnd].bounds[WIDTH]);

        return space;
    }

    /**
     * After the contents of a row have been determined, layout the
     * items on that row, taking into account the individual items'
     * vertically oriented layout directives.
     *
     * @param rowStart the index of the first row element
     * @param rowEnd the index of the last row element
     * @param lineHeight the overall height in pixels of the line
     */
    private void layoutRowVertical(int rowStart, int rowEnd, int lineHeight) {
        // SYNC NOTE: protected by the lock around calls to layout()

        int space = 0;
        int pH = 0;

        for (int i = rowStart; i <= rowEnd; i++) {

            // Items that have the LAYOUT_VSHRINK  directive are expanded 
            // to their preferred  height or to the height of the row, 
            // whichever is smaller. Items that are still shorter than 
            // the row height and that have the LAYOUT_VEXPAND directive 
            // will expand to the height of the row.
            if (items[i].shouldVExpand()) {
                items[i].bounds[HEIGHT] = lineHeight;

            } else if (items[i].shouldVShrink()) {
                pH = items[i].lockedHeight;
                if (pH == -1) {
                    pH = items[i].callPreferredHeight(items[i].bounds[WIDTH]);
                }
                if (pH > lineHeight) {
                    pH = lineHeight;
                }
                items[i].bounds[HEIGHT] = pH;
            }

            // By default, layout() puts the Item at the top so we simply
            // add on to the height
            switch (items[i].callGetLayout() & LAYOUT_VMASK) {
                case Item.LAYOUT_VCENTER:
                    space = lineHeight - items[i].bounds[HEIGHT];
                    if (space > 0) {
                        items[i].bounds[Y] += (space / 2);
                    }
                    break;
                case Item.LAYOUT_BOTTOM:
                    space = lineHeight - items[i].bounds[HEIGHT];
                    if (space > 0) {
                        items[i].bounds[Y] += space;
                    }
                    break;
                case Item.LAYOUT_TOP:
                default:
                    // by default, layout() puts the Item at the top
                    break;
            }
        }
    }

    /**
     * Insert an Item into this Form
     *
     * @param itemNum The index into the Item array to insert this Item
     * @param item The Item to insert
     * @return int The index at which the newly inserted Item can be found
     */
    private int insertImpl(int itemNum, Item item) {

        if (traverseIndex >= itemNum || traverseIndex == -1) {
            traverseIndex++;
        }

        if (items.length == numOfItems) {
            Item newItems[] = new Item[numOfItems + GROW_SIZE];
            System.arraycopy(items, 0, newItems, 0, itemNum);
            System.arraycopy(items, itemNum, newItems, itemNum + 1,
                             numOfItems - itemNum);
            items = newItems;
        } else {
            // if we're not appending
            if (itemNum != numOfItems) {
                System.arraycopy(items, itemNum, items, itemNum + 1,
                                 numOfItems - itemNum);
            }
        }

        numOfItems++;

        //
        // the arraycopy copied the reference to the item at this
        // spot. if we call setImpl without setting the index to null
        // setImpl will set the items owner to null.
        //
        items[itemNum] = null;

        setImpl(itemNum, item);
        return itemNum;
    }

    /**
     * Set a specific Item index to be a new Item
     *
     * @param itemNum The Item index to change
     * @param item The new Item to set
     */
    private void setImpl(int itemNum, Item item) {
        Item oldItem = items[itemNum];
        if (oldItem != null) {
            oldItem.setOwner(null);
        }

        item.setOwner(this);

        items[itemNum] = item;

        invalidate(null);
    }
}

