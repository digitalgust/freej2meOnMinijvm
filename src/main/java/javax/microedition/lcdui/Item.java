/*
 * @(#)Item.java	1.148 02/10/14 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.Text;

/**
 * A superclass for components that can be added to a {@link Form
 * Form}. All <code>Item</code> objects have a label field,
 * which is a string that is
 * attached to the item. The label is typically displayed near the component
 * when it is displayed within a screen.  The label should be positioned on
 * the same horizontal row as the item or
 * directly above the item.  The implementation should attempt to distinguish
 * label strings from other textual content, possibly by displaying the label
 * in a different font, aligning it to a different margin, or appending a
 * colon to it if it is placed on the same line as other string content.
 * If the screen is scrolling, the implementation should try
 * to keep the label visible at the same time as the <code>Item</code>.
 *
 * <p>In some cases,
 * when the user attempts to interact with an <code>Item</code>,
 * the system will switch to
 * a system-generated screen where the actual interaction takes place. If
 * this occurs, the label will generally be carried along and displayed within
 * this new screen in order to provide the user with some context for the
 * operation. For this reason it is recommended that applications supply a
 * label to all interactive Item objects. However, this is not required, and
 * a <code>null</code> value for a label is legal and specifies
 * the absence of a label.
 * </p>
 *
 * <h3>Item Layout</h3>
 *
 * <p>An <code>Item's</code> layout within its container is
 * influenced through layout directives:</p>
 *
 * <ul>
 * <li> <code>LAYOUT_DEFAULT</code> </li>
 * <li> <code>LAYOUT_LEFT</code> </li>
 * <li> <code>LAYOUT_RIGHT</code> </li>
 * <li> <code>LAYOUT_CENTER</code> </li>
 * <li> <code>LAYOUT_TOP</code> </li>
 * <li> <code>LAYOUT_BOTTOM</code> </li>
 * <li> <code>LAYOUT_VCENTER</code> </li>
 * <li> <code>LAYOUT_NEWLINE_BEFORE</code> </li>
 * <li> <code>LAYOUT_NEWLINE_AFTER</code> </li>
 * <li> <code>LAYOUT_SHRINK</code> </li>
 * <li> <code>LAYOUT_VSHRINK</code> </li>
 * <li> <code>LAYOUT_EXPAND</code> </li>
 * <li> <code>LAYOUT_VEXPAND</code> </li>
 * <li> <code>LAYOUT_2</code> </li>
 * </ul>
 *
 * <p>The <code>LAYOUT_DEFAULT</code> directive indicates
 * that the container's default
 * layout policy is to be used for this item.
 * <code>LAYOUT_DEFAULT</code> has the value
 * zero and has no effect when combined with other layout directives.  It is
 * useful within programs in order to document the programmer's intent.</p>
 *
 * <p>The <code>LAYOUT_LEFT</code>, <code>LAYOUT_RIGHT</code>, and
 * <code>LAYOUT_CENTER</code> directives indicate
 * horizontal alignment and are mutually exclusive.  Similarly, the
 * <code>LAYOUT_TOP</code>, <code>LAYOUT_BOTTOM</code>, and
 * <code>LAYOUT_VCENTER</code> directives indicate vertical
 * alignment and are mutually exclusive.</p>
 *
 * <p>A horizontal alignment directive, a vertical alignment directive, and
 * any combination of other layout directives may be combined using the
 * bit-wise <code>OR</code> operator (<code>|</code>) to compose a
 * layout directive value.  Such a value
 * is used as the parameter to the {@link #setLayout} method and is the return
 * value from the {@link #getLayout} method.</p>
 *
 * <p>Some directives have no defined behavior in some contexts.  A layout
 * directive is ignored if its behavior is not defined for the particular
 * context within which the <code>Item</code> resides.</p>
 *
 * <p>A complete specification of the layout of <code>Items</code>
 * within a <code>Form</code> is given
 * <a href="Form.html#layout">here</a>.</p>
 *
 * <a name="sizes"></a>
 * <h3>Item Sizes</h3>
 *
 * <p><code>Items</code> have two explicit size concepts: the <em>minimum</em>
 * size and the
 * <em>preferred</em> size.  Both the minimum and the preferred sizes refer to
 * the total area of the <code>Item</code>, which includes space for the
 * <code>Item's</code> contents,
 * the <code>Item's</code> label, as well as other space that is
 * significant to the layout
 * policy.  These sizes do not include space that is not significant for
 * layout purposes.  For example, if the addition of a label to an
 * <code>Item</code> would
 * cause other <code>Items</code> to move in order to make room,
 * then the space occupied by
 * this label is significant to layout and is counted as part of
 * the <code>Item's</code>
 * minimum and preferred sizes.  However, if an implementation were to place
 * the label in a margin area reserved exclusively for labels, this would not
 * affect the layout of neighboring <code>Items</code>.
 * In this case, the space occupied
 * by the label would not be considered part of the minimum and preferred
 * sizes.</p>
 *
 * <p>The minimum size is the smallest size at which the
 * <code>Item</code> can function and
 * display its contents, though perhaps not optimally.  The minimum size
 * may be recomputed whenever the <code>Item's</code> contents changes.</p>
 *
 * <p>The preferred size is generally a size based on the
 * <code>Item's</code> contents and
 * is the smallest size at which no information is clipped and text wrapping
 * (if any) is kept to a tolerable minimum.  The preferred size may be
 * recomputed whenever the <code>Item's</code> contents changes.
 * The application can
 * <em>lock</em> the preferred width or preferred height (or both) by
 * supplying specific values for parameters to the {@link #setPreferredSize
 * setPreferredSize} method.  The manner in which an
 * <code>Item</code> fits its contents
 * within an application-specified preferred size is implementation-specific.
 * However, it is recommended that textual content be word-wrapped to fit the
 * preferred size set by the application.  The application can <em>unlock</em>
 * either or both dimensions by supplying the value <code>-1</code>
 * for parameters to the <code>setPreferredSize</code> method.</p>
 *
 * <p>When an <code>Item</code> is created, both the preferred width
 * and height are
 * unlocked.  In this state, the implementation computes the preferred width
 * and height based on the <code>Item's</code> contents, possibly
 * including other relevant
 * factors such as the <code>Item's</code> graphic design and the
 * screen dimensions.
 * After having locked either the preferred width or height, the application
 * can restore the initial, unlocked state by calling
 * <code>setPreferredSize(-1,&nbsp;-1)</code>.</p>
 *
 * <p>The application can lock one dimension of the preferred size and leave
 * the other unlocked.  This causes the system to compute an appropriate value
 * for the unlocked dimension based on arranging the contents to fit the
 * locked dimension.  If the contents changes, the size on the unlocked
 * dimension is recomputed to reflect the new contents, but the size on the
 * locked dimension remains unchanged.  For example, if the application called
 * <code>setPreferredSize(50,&nbsp;-1)</code>, the preferred width would be
 * locked at <code>50</code> pixels and the preferred height would
 * be computed based on the
 * <code>Item's</code> contents.  Similarly, if the application called
 * <code>setPreferredSize(-1,&nbsp;60)</code>, the preferred height would be
 * locked at <code>60</code> pixels and the preferred width would be
 * computed based on the
 * <code>Item's</code> contents.  This feature is particularly useful
 * for <code>Items</code> with
 * textual content that can be line wrapped.</p>
 *
 * <p>The application can also lock both the preferred width and height to
 * specific values.  The <code>Item's</code> contents are truncated or padded
 * as necessary to honor this request.  For <code>Items</code> containing
 * text, the text should be wrapped to the specified width, and any truncation
 * should occur at the end of the text.</p>
 *
 * <p><code>Items</code> also have an implicit maximum size provided by the
 * implementation.  The maximum width is typically based on the width of the
 * screen space available to a <code>Form</code>.  Since <code>Forms</code>
 * can scroll vertically, the maximum height should typically not be based on
 * the height of the available screen space.</p>
 *
 * <p>If the application attempts to lock a preferred size dimension to a
 * value smaller than the minimum or larger than the maximum, the
 * implementation may disregard the requested value and instead use either the
 * minimum or maximum as appropriate.  If this occurs, the actual values used
 * must be visible to the application via the values returned from the
 * {@link #getPreferredWidth getPreferredWidth} and
 * {@link #getPreferredHeight getPreferredHeight} methods.
 * </p>
 *
 * <h3>Commands</h3>
 *
 * <p>A <code>Command</code> is said to be present on an <code>Item</code>
 * if the <code>Command</code> has been
 * added to this <code>Item</code> with a prior call to {@link #addCommand}
 * or {@link #setDefaultCommand} and if
 * the <code>Command</code> has not been removed with a subsequent call to
 * {@link #removeCommand}.  <code>Commands</code> present on an
 * item should have a command
 * type of <code>ITEM</code>.  However, it is not an error for a
 * command whose type is
 * other than <code>ITEM</code> to be added to an item.
 * For purposes of presentation and
 * placement within its user interface, the implementation is allowed to
 * treat a command's items as if they were of type <code>ITEM</code>. </p>
 *
 * <p><code>Items</code> may have a <em>default</em> <code>Command</code>.
 * This state is
 * controlled by the {@link #setDefaultCommand} method.  The default
 * <code>Command</code> is eligible to be bound to a special
 * platform-dependent user
 * gesture.  The implementation chooses which gesture is the most
 * appropriate to initiate the default command on that particular
 * <code>Item</code>.
 * For example, on a device that has a dedicated selection key, pressing
 * this key might invoke the item's default command.  Or, on a
 * stylus-based device, tapping on the <code>Item</code> might
 * invoke its default
 * command.  Even if it can be invoked through a special gesture, the
 * default command should also be invokable in the same fashion as
 * other item commands.</p>
 *
 * <p>It is possible that on some devices there is no special gesture
 * suitable for invoking the default command on an item.  In this case
 * the default command must be accessible to the user in the same
 * fashion as other item commands.  The implementation may use the state
 * of a command being the default in deciding where to place the command
 * in its user interface.</p>
 *
 * <p>It is possible for an <code>Item</code> not to have a default command.
 * In this
 * case, the implementation may bind its special user gesture (if any)
 * for another purpose, such as for displaying a menu of commands.  The
 * default state of an <code>Item</code> is not to have a default command.
 * An <code>Item</code>
 * may be set to have no default <code>Command</code> by removing it from
 * the <code>Item</code> or
 * by passing <code>null</code> to the <code>setDefaultCommand()</code>
 * method.</p>
 *
 * <p>The same command may occur on more than one
 * <code>Item</code> and also on more than
 * one <code>Displayable</code>.  If this situation occurs, the user
 * must be provided with
 * distinct gestures to invoke that command on each <code>Item</code> or
 * <code>Displayable</code> on
 * which it occurs, while those <code>Items</code> or <code>Displayables</code>
 * are visible on the
 * display.  When the user invokes the command, the listener
 * (<code>CommandListener</code>
 * or <code>ItemCommandListener</code> as appropriate) of just the
 * object on which the
 * command was invoked will be called.</p>
 *
 * <p>Adding commands to an <code>Item</code> may affect its appearance, the
 * way it is laid out, and the traversal behavior.  For example, the presence
 * of commands on an <code>Item</code> may cause row breaks to occur, or it
 * may cause additional graphical elements (such as a menu icon) to appear.
 * In particular, if a <code>StringItem</code> whose appearance mode is
 * <code>PLAIN</code> (see below) is given one or more <code>Commands</code>,
 * the implementation is allowed to treat it as if it had a different
 * appearance mode.</p>
 *
 * <a name="appearance"></a>
 * <h3>Appearance Modes</h3>
 *
 * <p>The <code>StringItem</code> and <code>ImageItem</code> classes have an
 * <em>appearance mode</em> attribute that can be set in their constructors.
 * This attribute can have one of the values {@link #PLAIN PLAIN},
 * {@link #HYPERLINK HYPERLINK}, or {@link #BUTTON BUTTON}.
 * An appearance mode of <code>PLAIN</code> is typically used
 * for non-interactive
 * display of textual or graphical material.  The appearance
 * mode values do not have any side effects on the interactivity of the item.
 * In order to be interactive, the item must have one or more
 * <code>Commands</code>
 * (preferably with a default command assigned), and it must have a
 * <code>CommandListener</code> that receives notification of
 * <code>Command</code> invocations.  The
 * appearance mode values also do not have any effect on the semantics of
 * <code>Command</code> invocation on the item.  For example,
 * setting the appearance mode
 * of a <code>StringItem</code> to be <code>HYPERLINK</code>
 * requests that the implementation display
 * the string contents as if they were a hyperlink in a browser.  It is the
 * application's responsibility to attach a <code>Command</code>
 * and a listener to the
 * <code>StringItem</code> that provide behaviors that the user
 * would expect from invoking
 * an operation on a hyperlink, such as loading the referent of the link or
 * adding the link to the user's set of bookmarks.</p>
 *
 * <p>Setting the appearance mode of an <code>Item</code> to be other than
 * <code>PLAIN</code> may affect its minimum, preferred, and maximum sizes, as
 * well as the way it is laid out.  For example, a <code>StringItem</code>
 * with an appearance mode of <code>BUTTON</code> should not be wrapped across
 * rows.  (However, a <code>StringItem</code> with an appearance mode of
 * <code>HYPERLINK</code> should be wrapped the same way as if its appearance
 * mode is <code>PLAIN</code>.)</p>
 *
 * <p>A <code>StringItem</code> or <code>ImageItem</code>
 * in <code>BUTTON</code> mode can be used to create a
 * button-based user interface.  This can easily lead to applications that are
 * inconvenient to use.  For example, in a traversal-based system, users must
 * navigate to a button before they can invoke any commands on it.  If buttons
 * are spread across a long <code>Form</code>, users may be required
 * to perform a
 * considerable amount of navigation in order to discover all the available
 * commands.  Furthermore, invoking a command from a button at the
 * other end of the <code>Form</code> can be quite cumbersome.
 * Traversal-based systems
 * often provide a means of invoking commands from anywhere (such as from a
 * menu), without the need to traverse to a particular item.  Instead of
 * adding a command to a button and placing that button into a
 * <code>Form</code>, it would
 * often be more appropriate and convenient for users if that command were
 * added directly to the <code>Form</code>.  Buttons should be used
 * only in cases where
 * direct user interaction with the item's string or image contents is
 * essential to the user's understanding of the commands that can be invoked
 * from that item.</p>
 *
 * <h3>Default State</h3>
 *
 * <p>Unless otherwise specified by a subclass, the default state of newly
 * created <code>Items</code> is as follows:</p>
 *
 * <ul>
 * <li>the <code>Item</code> is not contained within
 * (&quot;owned by&quot;) any container;</li>
 * <li>there are no <code>Commands</code> present;</li>
 * <li>the default <code>Command</code> is <code>null</code>;</li>
 * <li>the <code>ItemCommandListener</code> is <code>null</code>;</li>
 * <li>the layout directive value is <code>LAYOUT_DEFAULT</code>; and</li>
 * <li>both the preferred width and preferred height are unlocked.</li>
 * </ul>
 *
 * @since MIDP 1.0
 */

abstract public class Item {

// ************************************************************
//  public member variables
// ************************************************************

    /**
     * A layout directive indicating that this <code>Item</code> 
     * should follow the default layout policy of its container.
     *
     * <P>Value <code>0</code> is assigned to <code>LAYOUT_DEFAULT</code>.</P>
     *
     * @since MIDP 2.0
     */
    public final static int LAYOUT_DEFAULT = 0;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * left-aligned layout.
     *
     * <P>Value <code>1</code> is assigned to <code>LAYOUT_LEFT</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_LEFT = 1;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * right-aligned layout.
     *
     * <P>Value <code>2</code> is assigned to <code>LAYOUT_RIGHT</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_RIGHT = 2;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * horizontally centered layout.
     *
     * <P>Value <code>3</code> is assigned to <code>LAYOUT_CENTER</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_CENTER = 3;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * top-aligned layout.
     *
     * <P>Value <code>0x10</code> is assigned to <code>LAYOUT_TOP</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_TOP = 0x10;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * bottom-aligned layout.
     *
     * <P>Value <code>0x20</code> is assigned to <code>LAYOUT_BOTTOM</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_BOTTOM = 0x20;

    /**
     * A layout directive indicating that this <code>Item</code> should have a
     * vertically centered layout.
     *
     * <P>Value <code>0x30</code> is assigned to 
     * <code>LAYOUT_VCENTER</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_VCENTER = 0x30;

    /**
     * A layout directive indicating that this <code>Item</code> 
     * should be placed at the beginning of a new line or row.
     *
     * <P>Value <code>0x100</code> is assigned to 
     * <code>LAYOUT_NEWLINE_BEFORE</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_NEWLINE_BEFORE = 0x100;

    /**
     * A layout directive indicating that this <code>Item</code>
     * should the last on its line or row, and that the next
     * <code>Item</code> (if any) in the container
     * should be placed on a new line or row.
     *
     * <P>Value <code>0x200</code> is assigned to 
     * <code>LAYOUT_NEWLINE_AFTER</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_NEWLINE_AFTER = 0x200;

    /**
     * A layout directive indicating that this <code>Item's</code>
     * width may be reduced to its minimum width.
     *
     *<P>Value <code>0x400</code> is assigned to <code>LAYOUT_SHRINK</code></P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_SHRINK = 0x400;

    /**
     * A layout directive indicating that this <code>Item's</code> 
     * width may be increased to fill available space.
     *
     *<P>Value <code>0x800</code> is assigned to <code>LAYOUT_EXPAND</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_EXPAND = 0x800;

    /**
     * A layout directive indicating that this <code>Item's</code>
     * height may be reduced to its minimum height.
     *
     * <P>Value <code>0x1000</code> is assigned to
     * <code>LAYOUT_VSHRINK</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_VSHRINK = 0x1000;

    /**
     * A layout directive indicating that this <code>Item's</code> 
     * height may be increased to fill available space.
     *
     * <P>Value <code>0x2000</code> is assigned to 
     * <code>LAYOUT_VEXPAND</code>.</P>
     * @since MIDP 2.0
     */
    public final static int LAYOUT_VEXPAND = 0x2000;

    /**
     * A layout directive indicating that new MIDP 2.0 layout
     * rules are in effect for this <code>Item</code>.  If this
     * bit is clear, indicates that MIDP 1.0 layout behavior
     * applies to this <code>Item</code>.
     *
     * <P>Value <code>0x4000</code> is assigned to
     * <code>LAYOUT_2</code>.</P>
     * 
     * @since MIDP 2.0
     */
    public static final int LAYOUT_2 = 0x4000;

    /**
     * An appearance mode value indicating that the <code>Item</code> is to have
     * a normal appearance.
     *
     * <P>Value <code>0</code> is assigned to <code>PLAIN</code>.</P>
     * @since MIDP 2.0
     */
    public final static int PLAIN = 0;

    /**
     * An appearance mode value indicating that the <code>Item</code>
     * is to appear as a hyperlink.
     * <P>Value <code>1</code> is assigned to <code>HYPERLINK</code>.</P>
     * @since MIDP 2.0
     */
    public final static int HYPERLINK = 1;

    /**
     * An appearance mode value indicating that the <code>Item</code>
     * is to appear as a button.
     * <P>Value <code>2</code> is assigned to <code>BUTTON</code>.</P>
     * @since MIDP 2.0
     */
    public final static int BUTTON = 2;

// ************************************************************
//  protected member variables
// ************************************************************

// ************************************************************
//  package private member variables
// ************************************************************

    /** bounds[] array index to x coordinate */
    final static int X      = Displayable.X;

    /** bounds[] array index to y coordinate */
    final static int Y      = Displayable.Y;

    /** bounds[] array index to width */
    final static int WIDTH  = Displayable.WIDTH;

    /** bounds[] array index to height */
    final static int HEIGHT = Displayable.HEIGHT;

    /** internal bitmask representing a valid layout mask */
    final static int VALID_LAYOUT;

    /**
     * An array of 4 elements, describing the x, y, width, height
     * of this Item's bounds in the viewport coordinate space. If
     * its null, it means the Item is currently not in the viewport
     */
    int[] bounds;

    /**
     * A flag indicating this Item has the input focus. This is
     * maintained by the Item superclass for easy use by subclass
     * code.
     */
    boolean hasFocus;

    /**
     * A flag indicating this Item is currently (at least partially)
     * visible on the Form it is on. This is maintained by the Item
     * superclass for easy use by subclass code.
     */
    boolean visible;

    /**
     * A flag indicating the size of this Item has changed in a
     * subsequent layout operation
     */
    boolean sizeChanged;

    /**
     * commandListener that has to be notified of when ITEM command is
     * activated
     */
    ItemCommandListener commandListener; // = null;

    /** The label of this Item */
    String label;

    /**
     * The owner Screen for this Item
     */
    Screen owner;     // = null

    /**
     * The layout type of this Item
     */
    int layout;       // = 0 ; LAYOUT_DEFAULT = 0

    /**
     * This is a default Command which represents the callback
     * to a selection event.
     */
    Command defaultCommand;

    /** The number of Commands added to this Item */
    int numCommands;

    /** The locked width of this Item, -1 by default */
    int lockedWidth = -1;

    /** The locked height of this Item, -1 by default */
    int lockedHeight = -1;

    /**
     * A flag signaling how labels are painted. If true,
     * labels are only painted bold on traverse, else they
     * are always painted bold
     */
    final static boolean LABEL_BOLD_ON_TRAVERSE = false;

    /**
     * Padding used between Button Border and ImageItem
     */
    final static int BUTTON_PAD = 2;

    /**
     * Button Border is 3 pixel wide and 3 pixel high
     */
    final static int BUTTON_BORDER = 3;

    /** Button border color for light gray border */
    static final int LIGHT_GRAY_COLOR = 0x00AFAFAF; // light gray

    /** Button border color for dark gray border */
    static final int DARK_GRAY_COLOR = 0x00606060; // dark gray

    /** The font to render item labels with focus */
    final static Font LABEL_FONT = Font.getFont(
        Screen.CONTENT_FONT.getFace(),
        Font.STYLE_BOLD,
        Screen.CONTENT_FONT.getSize());

    /** 2 pixel padding between the label and the item's contents */
    final static int LABEL_PAD = 2;

    /** The height of the label (maximum of 1 line */
    final static int LABEL_HEIGHT = LABEL_FONT.getHeight() + LABEL_PAD;

    /** Maximum width available to any Item */
    final static int DEFAULT_WIDTH = 
        Display.WIDTH - Form.CELL_SPACING - Form.CELL_SPACING;

// ************************************************************
//  private member variables
// ************************************************************

    /** An array of Commands added to this Item */
    private Command commands[];

// ************************************************************
//  Static initializer, constructor
// ************************************************************

    static {
        VALID_LAYOUT =
            Item.LAYOUT_DEFAULT |
            Item.LAYOUT_LEFT |
            Item.LAYOUT_RIGHT |
            Item.LAYOUT_CENTER |
            Item.LAYOUT_TOP |
            Item.LAYOUT_BOTTOM |
            Item.LAYOUT_VCENTER |
            Item.LAYOUT_SHRINK |
            Item.LAYOUT_EXPAND |
            Item.LAYOUT_VSHRINK |
            Item.LAYOUT_VEXPAND |
            Item.LAYOUT_NEWLINE_BEFORE |
            Item.LAYOUT_NEWLINE_AFTER |
            Item.LAYOUT_2;
    }

    /**
     * Creates a new item with a given label.
     *
     * @param label the label string; null is allowed
     */
    Item(String label) {
        // SYNC NOTE: probably safe, but since subclasses can't lock
        // around their call to super(), we'll lock it here
        synchronized (Display.LCDUILock) {
            this.label = label;
        }
    }

// ************************************************************
//  public methods
// ************************************************************

    /**
     * Sets the label of the <code>Item</code>. If <code>label</code>
     * is <code>null</code>, specifies that this item has no label.
     * 
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within  an <code>Alert</code>.</p>
     * 
     * @param label the label string
     * @throws IllegalStateException if this <code>Item</code> is contained 
     * within an <code>Alert</code>
     * @see #getLabel
     */
    public void setLabel(String label) {
        synchronized (Display.LCDUILock) {
            this.label = label;
            invalidate();
        }
    }
    
    /**
     * Gets the label of this <code>Item</code> object.
     * @return the label string
     * @see #setLabel
     */
    public String getLabel() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return label;
    }

    /**
     * Gets the layout directives used for placing the item.
     * @return a combination of layout directive values
     * @since MIDP 2.0
     * @see #setLayout
     */
    public int getLayout() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return layout;
    }

    /**
     * Sets the layout directives for this item.
     *
     * <p>It is illegal to call this method if this <code>Item</code> 
     * is contained within an <code>Alert</code>.</p>
     * 
     * @param layout a combination of layout directive values for this item
     * @throws IllegalArgumentException if the value of layout is not a
     * bit-wise OR combination of layout directives
     * @throws IllegalStateException if this <code>Item</code> is
     * contained within an <code>Alert</code>
     * @since MIDP 2.0
     * @see #getLayout
     */
    public void setLayout(int layout) {
        synchronized (Display.LCDUILock) {
            setLayoutImpl(layout);
            invalidate();
        }
    }

    /**
     * Adds a context sensitive <code>Command</code> to the item.
     * The semantic type of
     * <code>Command</code> should be <code>ITEM</code>. The implementation
     * will present the command
     * only when the item is active, for example, highlighted.
     * <p>
     * If the added command is already in the item (tested by comparing the
     * object references), the method has no effect. If the item is
     * actually visible on the display, and this call affects the set of
     * visible commands, the implementation should update the display as soon
     * as it is feasible to do so.
     *
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within an <code>Alert</code>.</p>
     *
     * @param cmd the command to be added
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     * @throws NullPointerException if cmd is <code>null</code>
     * @since MIDP 2.0
     */
    public void addCommand(Command cmd) {
        synchronized (Display.LCDUILock) {
            addCommandImpl(cmd);
        }
        }

    /**
     * Removes the context sensitive command from item. If the command is not
     * in the <code>Item</code> (tested by comparing the object references),
     * the method has
     * no effect. If the <code>Item</code> is actually visible on the display, 
     * and this  call
     * affects the set of visible commands, the implementation should update
     * the display as soon as it is feasible to do so.
     *
     *
     * If the command to be removed happens to be the default command,
     * the command is removed and the default command on this Item is
     * set to <code>null</code>.
     *
     * The following code:
     * <CODE> <pre>
     *     // Command c is the default command on Item item
     *     item.removeCommand(c);
     * </pre> </CODE>
     * is equivalent to the following code:
     * <CODE> <pre>
     *     // Command c is the default command on Item item
     *     item.setDefaultCommand(null);
     *     item.removeCommand(c);
     * </pre> </CODE>
     *
     *
     * @param cmd the command to be removed
     * @since MIDP 2.0
     */
    public void removeCommand(Command cmd) {
        synchronized (Display.LCDUILock) {
            removeCommandImpl(cmd);
        }
    }

    /**
     * Sets a listener for <code>Commands</code> to this <code>Item</code>,
     * replacing any previous
     * <code>ItemCommandListener</code>. A <code>null</code> reference
     * is allowed and has the effect of
     * removing any existing listener.
     *
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within an <code>Alert</code>.</p>
     *
     * @param l the new listener, or <code>null</code>.
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     * @since MIDP 2.0
     */
    public void setItemCommandListener(ItemCommandListener l) {
        synchronized (Display.LCDUILock) {
            commandListener = l;
        }
    }

    /**
     * Gets the preferred width of this <code>Item</code>.  
     * If the application has locked
     * the width to a specific value, this method returns that value.
     * Otherwise, the return value is computed based on the 
     * <code>Item's</code> contents,
     * possibly with respect to the <code>Item's</code> preferred height 
     * if it is locked.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.
     *
     * @return the preferred width of the Item
     * @see #getPreferredHeight
     * @see #setPreferredSize
     * @since MIDP 2.0
     */
    public int getPreferredWidth() {
        synchronized (Display.LCDUILock) {
            return (lockedWidth != -1) ? lockedWidth :
                        callPreferredWidth(lockedHeight);
        }
    }

    /**
     * Gets the preferred height of this <code>Item</code>.  
     * If the application has locked
     * the height to a specific value, this method returns that value.
     * Otherwise, the return value is computed based on the 
     * <code>Item's</code> contents,
     * possibly with respect to the <code>Item's</code> preferred 
     * width if it is locked.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.
     *
     * @return the preferred height of the <code>Item</code>
     * @see #getPreferredWidth
     * @see #setPreferredSize
     * @since MIDP 2.0
     */
    public int getPreferredHeight() {
        synchronized (Display.LCDUILock) {
            return (lockedHeight != -1) ? lockedHeight :
                        callPreferredHeight(lockedWidth);
        }
    }

    /**
     * Sets the preferred width and height for this <code>Item</code>.
     * Values for width and height less than <code>-1</code> are illegal.
     * If the width is between zero and the minimum width, inclusive,
     * the minimum width is used instead.
     * If the height is between zero and the minimum height, inclusive,
     * the minimum height is used instead.
     *
     * <p>Supplying a width or height value greater than the minimum width or 
     * height <em>locks</em> that dimension to the supplied
     * value.  The implementation may silently enforce a maximum dimension for 
     * an <code>Item</code> based on factors such as the screen size. 
     * Supplying a value of
     * <code>-1</code> for the width or height unlocks that dimension.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.</p>
     * 
     * <p>It is illegal to call this method if this <code>Item</code> 
     * is contained within  an <code>Alert</code>.</p>
     * 
     * @param width the value to which the width should be locked, or
     * <code>-1</code> to unlock
     * @param height the value to which the height should be locked, or 
     * <code>-1</code> to unlock
     * @throws IllegalArgumentException if width or height is less than 
     * <code>-1</code>
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     * @see #getPreferredHeight
     * @see #getPreferredWidth
     * @since MIDP 2.0
     */
    public void setPreferredSize(int width, int height) {
        if (width < -1 || height < -1) {
            throw new IllegalArgumentException();
        }

        synchronized (Display.LCDUILock) {
            if (owner != null && owner instanceof Alert) {
                throw new IllegalStateException();
            }

            int minWidth  = getMinimumWidth();
            int minHeight = getMinimumHeight();

            this.lockedWidth  = (width != -1 && width < minWidth) 
                              ? minWidth 
                              : width;

            this.lockedHeight = (height != -1 && height < minHeight) 
                              ? minHeight 
                              : height;

            if (visible) {
                if (lockedWidth != bounds[WIDTH] ||
                        lockedHeight != bounds[HEIGHT]) {
                    invalidate();
                }
            }
        } // synchronized
    }

    /**
     * Gets the minimum width for this <code>Item</code>.  This is a width
     * at which the item can function and display its contents,
     * though perhaps not optimally.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.
     * 
     * @return the minimum width of the item
     * @since MIDP 2.0
     */
    public int getMinimumWidth() {
        synchronized (Display.LCDUILock) {
            return callMinimumWidth();
        }
    }

    /**
     * Gets the minimum height for this <code>Item</code>.  This is a height
     * at which the item can function and display its contents,
     * though perhaps not optimally.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.
     *
     * @return the minimum height of the item
     * @since MIDP 2.0
     */
    public int getMinimumHeight() {
        synchronized (Display.LCDUILock) {
            return callMinimumHeight();
        }
    }

    /**
     * Sets default <code>Command</code> for this <code>Item</code>.  
     * If the <code>Item</code> previously had a
     * default <code>Command</code>, that <code>Command</code> 
     * is no longer the default, but it
     * remains present on the <code>Item</code>.
     *
     * <p>If not <code>null</code>, the <code>Command</code> object
     * passed becomes the default <code>Command</code>
     * for this <code>Item</code>.  If the <code>Command</code> object
     * passed is not currently present
     * on this <code>Item</code>, it is added as if {@link #addCommand}
     * had been called
     * before it is made the default <code>Command</code>.</p>
     *
     * <p>If <code>null</code> is passed, the <code>Item</code> is set to
     * have no default <code>Command</code>.
     * The previous default <code>Command</code>, if any, remains present
     * on the <code>Item</code>.
     * </p>
     *
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within  an <code>Alert</code>.</p>
     * 
     * @param cmd the command to be used as this <code>Item's</code> default
     * <code>Command</code>, or <code>null</code> if there is to 
     * be no default command
     *
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     * @since MIDP 2.0
     */
    public void setDefaultCommand(Command cmd) {
        if (cmd != null) {
            addCommand(cmd);
        }
        defaultCommand = cmd;
    }

    /**
     * Causes this <code>Item's</code> containing <code>Form</code> to notify
     * the <code>Item's</code> {@link ItemStateListener}.
     * The application calls this method to inform the
     * listener on the <code>Item</code> that the <code>Item's</code>
     * state has been changed in
     * response to an action.  Even though this method simply causes a call
     * to another part of the application, this mechanism is useful for
     * decoupling the implementation of an <code>Item</code> (in particular, the
     * implementation of a <code>CustomItem</code>, though this also applies to
     * subclasses of other items) from the consumer of the item.
     *
     * <p>If an edit was performed by invoking a separate screen, and the
     * editor now wishes to &quot;return&quot; to the form which contained the
     * selected <code>Item</code>, the preferred method is
     * <code>Display.setCurrent(Item)</code>
     * instead of <code>Display.setCurrent(Displayable)</code>,
     * because it allows the
     * <code>Form</code> to restore focus to the <code>Item</code>
     * that initially invoked the editor.</p>
     *
     * <p>In order to make sure that the documented behavior of
     * <code>ItemStateListener</code> is maintained, it is up to the caller
     * (application) to guarantee that this function is
     * not called unless:</p>
     *
     * <ul>
     * <li>the <code>Item's</code> value has actually been changed, and</li>
     * <li>the change was the result of a user action (an &quot;edit&quot;)
     * and NOT as a result of state change via calls to
     * <code>Item's</code> APIs </li>
     * </ul>
     *
     * <p>The call to <code>ItemStateListener.itemStateChanged</code>
     * may be delayed in order to be serialized with the event stream.
     * The <code>notifyStateChanged</code> method does not block awaiting
     * the completion of the <code>itemStateChanged</code> method.</p>
     *
     * @throws IllegalStateException if the <code>Item</code> is not owned
     * by a <code>Form</code>
     * @since MIDP 2.0
     */
    public void notifyStateChanged() { 
        // get a copy of the object reference to item's owning form
        Screen owner = this.owner;

        if (owner == null || !(owner instanceof Form)) {
            throw new IllegalStateException();
        }

        owner.itemStateChanged(this);
    }

// ************************************************************
//  protected methods
// ************************************************************

// ************************************************************
//  package private methods
// ************************************************************

    /**
     * Called to commit any pending user interaction for the item
     */
    void commitPendingInteraction() { }
        
    /**
     * Return the height of the label for this Item. If null,
     * returns 0, otherwise, returns LABEL_HEIGHT - which is
     * one line height, multi-line labels are currently disabled
     * by this method
     *
     * @param w the width available for the label (-1 means as wide
     *          as possible)
     * @return the height for the label
     */
    int getLabelHeight(int w) {
        if (label == null || label.length() == 0) {
            return 0;
        } 

        if (w == -1) {
            w = DEFAULT_WIDTH;
        }
        return Text.getHeightForWidth(label, LABEL_FONT, w, 0);
    }

    /**
     * Return the width of the label for this Item. If null,
     * returns 0, otherwise, returns the width in pixels of the
     * current label using LABEL_FONT.
     *
     * @return the width for the label
     */
    int getLabelWidth() {
        if (label == null || label.length() == 0) {
            return 0;
        }
        
        return Text.getWidestLineWidth(label.toCharArray(), 0, DEFAULT_WIDTH,
                                       LABEL_FONT);
    }

    /**
     * Get the preferred width of this Item
     *
     * @param height the tentative content height in pixels, or -1 if a
     * tentative height has not been computed
     * @return the preferred width
     */
    abstract int callPreferredWidth(int height);

    /**
     * Get the preferred height of this Item
     *
     * @param width the tentative content width in pixels, or -1 if a
     * tentative width has not been computed
     * @return the preferred height
     */
    abstract int callPreferredHeight(int width);

    /**
     * Get the minimum width of this Item
     *
     * @return the minimum width
     */
    abstract int callMinimumWidth();

    /**
     * Get the minimum height of this Item
     *
     * @return the minimum height
     */
    abstract int callMinimumHeight();

    /**
     * Determine if this Item should horizontally shrink
     *
     * @return true if it should horizontally shrink
     */
    boolean shouldHShrink() {
        return ((layout & LAYOUT_SHRINK) == LAYOUT_SHRINK);
    }

    /**
     * Determine if this Item should horizontally expand
     *
     * @return true if it should horizontally expand
     */
    boolean shouldHExpand() {
        return ((layout & LAYOUT_EXPAND) == LAYOUT_EXPAND);
    }

    /**
     * Determine if this Item should vertically shrink
     *
     * @return true if it should vertically shrink
     */
    boolean shouldVShrink() {
        return ((layout & LAYOUT_VSHRINK) == LAYOUT_VSHRINK);
    }

    /**
     * Determine if this Item should vertically expand
     *
     * @return true if it should vertically expand
     */
    boolean shouldVExpand() {
        return ((layout & LAYOUT_VEXPAND) == LAYOUT_VEXPAND);
    }

    /**
     * Determine if this Item should have a newline after it
     *
     * @return true if it should have a newline after
     */
    boolean equateNLA() {
        return ((layout & LAYOUT_NEWLINE_AFTER) == LAYOUT_NEWLINE_AFTER);
    }

    /**
     * Determine if this Item should have a newline before it
     *
     * @return true if it should have a newline before
     */
    boolean equateNLB() {
        return ((layout & LAYOUT_NEWLINE_BEFORE) == LAYOUT_NEWLINE_BEFORE);
    }

    /**
     * Determine if this Item should not be traversed to
     *
     * @return true if this Item should not be traversed to
     */
    boolean shouldSkipTraverse() {
        return false;
    }

    /**
     * Paint the content of this Item
     *
     * @param g the Graphics object to be used for rendering the item
     * @param w current width of the item in pixels
     * @param h current height of the item in pixels
     */
    abstract void callPaint(Graphics g, int w, int h);

    /**
     * Called by subclasses to repaint this entire Item's bounds
     */
    void repaint() {
        if (bounds != null) {
            repaint(0, 0, bounds[WIDTH], bounds[HEIGHT]);
        }
    }

    /**
     * Called by subclasses to repaint a portion of this Item's bounds
     *
     * @param x the x coordinate of the origin of the dirty region
     * @param y the y coordinate of the origin of the dirty region
     * @param w the width of the dirty region
     * @param h the height of the dirty region
     */
    void repaint(int x, int y, int w, int h) {

        if (owner != null) {

            if (x < 0) {
                x = 0;
            } else if (x > bounds[WIDTH]) {
                return;
            }

            if (y < 0) {
                y = 0;
            } else if (y > bounds[HEIGHT]) {
                return;
            }

            if (w < 0) {
                w = 0;
            } else if (w > bounds[WIDTH]) {
                w = bounds[WIDTH];
            }

            if (h < 0) {
                h = 0;
            } else if (h > bounds[HEIGHT]) {
                h = bounds[HEIGHT];
            }
            owner.repaintItem(this, x, y, w, h);
        }
    }

    /**
     * Called by subclasses to paint this Item's label
     *
     * @param g the graphics to draw to
     * @param width the allowable width for the label
     * @return the vertical offset of the label, if it was painted, 0
     *         if it was not
     */
    int paintLabel(Graphics g, int width) {
        int labelHeight = getLabelHeight(width);
        if (LABEL_BOLD_ON_TRAVERSE) {
            Text.paint(label, hasFocus ? LABEL_FONT: Screen.CONTENT_FONT,
                    g, width, labelHeight, 0, Text.NORMAL, null);
        } else {
            Text.paint(label, LABEL_FONT,
                    g, width, labelHeight, 0, Text.NORMAL, null);
        }
        return labelHeight;
    }

    /**
     * Called by the system to indicate the size available to this Item
     * has changed
     *
     * @param w the new width of the item's content area
     * @param h the new height of the item's content area
     */
    void callSizeChanged(int w, int h) { }

    /**
     * Called by subclass code to indicate to the system that it has
     * either modified its size requirements or performed an internal
     * traversal
     */
    void invalidate() {
        synchronized (Display.LCDUILock) {
            if (owner != null) {
                owner.invalidate(this);
            }
        }
    }

    /**
     * Called by the system
     *
     * <p>The default implementation of the traverse() method always returns
     * false.</p>
     *
     * @param dir the direction of traversal
     * @param viewportWidth the width of the container's viewport
     * @param viewportHeight the height of the container's viewport
     * @param visRect_inout passes the visible rectangle into the method, and
     * returns the updated traversal rectangle from the method
     * @return true if internal traversal had occurred, false if traversal
     * should proceed out
     *
     * @see #getInteractionModes
     * @see #traverseOut
     * @see #TRAVERSE_HORIZONTAL
     * @see #TRAVERSE_VERTICAL
     */
    boolean callTraverse(int dir, int viewportWidth, int viewportHeight,
                         int[] visRect_inout) {

        hasFocus = true;
        return false;
    }

    /**
     * Called by the system to indicate traversal has left this Item
     *
     * @see #getInteractionModes
     * @see #traverse
     * @see #TRAVERSE_HORIZONTAL
     * @see #TRAVERSE_VERTICAL
     */
    void callTraverseOut() {
        hasFocus = false;
    }

    /**
     * Called by the system to signal a key press
     *
     * @param keyCode the key code of the key that has been pressed
     * @see #getInteractionModes
     */
    void callKeyPressed(int keyCode) { }

    /**
     * Called by the system to signal a key typed
     *
     * @param c The character entered from the QWERTY keyboard
     */
    void callKeyTyped(char c) { }

    /**
     * Called by the system to signal a key release
     *
     * @param keyCode the key code of the key that has been released
     * @see #getInteractionModes
     */
    void callKeyReleased(int keyCode) { }

    /**
     * Called by the system to signal a key repeat
     *
     * @param keyCode the key code of the key that has been repeated
     * @see #getInteractionModes
     */
    void callKeyRepeated(int keyCode) { }

    /**
     * Called by the system to signal a pointer press
     *
     * @param x the x coordinate of the pointer down
     * @param y the y coordinate of the pointer down
     *
     * @see #getInteractionModes
     */
    void callPointerPressed(int x, int y) { }

    /**
     * Called by the system to signal a pointer release
     *
     * @param x the x coordinate of the pointer up
     * @param y the x coordinate of the pointer up
     *
     * @see #getInteractionModes
     */
    void callPointerReleased(int x, int y) {}

    /**
     * Called by the system to signal a pointer drag
     *
     * @param x the x coordinate of the pointer drag
     * @param y the x coordinate of the pointer drag
     *
     * @see #getInteractionModes
     */
    void callPointerDragged(int x, int y) { }

    /**
     * Called by the system to notify this Item it is being shown
     *
     * <p>The default implementation of this method updates
     * the 'visible' state
     */
    void callShowNotify() {
        this.visible = true;
    }

    /**
     * Called by the system to notify this Item it is being hidden
     *
     * <p>The default implementation of this method updates
     * the 'visible' state
     */
    void callHideNotify() {
        this.visible = false;
    }

    /**
     * Draw a Button Border around this Item when in Button Mode
     *
     * @param g The Graphics context to paint to
     * @param x  x co-ordinate of the Button Border
     * @param y  y co-ordinate of the Button Border
     * @param w  width of the Button Border
     * @param h height of the Button Border
     * @param hasFocus A flag indicating this Item has focus or not
     */
    void drawButtonBorder(Graphics g, int x, int y, int w, int h,
                          boolean hasFocus) {
        g.setColor(hasFocus ? DARK_GRAY_COLOR : LIGHT_GRAY_COLOR);
        g.fillRect(x, y, w, BUTTON_BORDER);
        g.fillRect(x, y, BUTTON_BORDER, h);

        g.setColor(hasFocus ? LIGHT_GRAY_COLOR : DARK_GRAY_COLOR);

        g.fillTriangle(x, y + h,
            x + BUTTON_BORDER,
            y + h - BUTTON_BORDER,
            x + BUTTON_BORDER,
            y + h);

        g.fillRect(x + BUTTON_BORDER,
            y + h - BUTTON_BORDER,
            w - BUTTON_BORDER,
            BUTTON_BORDER);

        g.fillTriangle(x + w, y,
            x + w - BUTTON_BORDER,
            y + BUTTON_BORDER,
            x + w,
            y + BUTTON_BORDER);

        g.fillRect(x + w - BUTTON_BORDER,
            y + BUTTON_BORDER,
            BUTTON_BORDER, h - BUTTON_BORDER);

        g.setColor(Display.FG_COLOR);
    }

    /**
     *  Adds a context sensitive Command to the item.
     * @param cmd the command to be removed
     */
    void addCommandImpl(Command cmd) {
        // LCDUI Lock must be acquired
        // prior to calling this method.
        if (cmd == null) {
            throw new NullPointerException();
        }

        for (int i = 0; i < numCommands; ++i) {
            if (commands[i] == cmd) {
                return;
            }
        }

        if ((commands == null) || (numCommands == commands.length)) {
            Command[] newCommands = new Command[numCommands + 4];
            if (commands != null) {
                System.arraycopy(commands, 0, newCommands, 0,
                                 numCommands);
            }
            commands = newCommands;
        }

        commands[numCommands] = cmd;
        ++numCommands;

        if (owner != null) {
            owner.updateCommandSet();
        }
    }

    /**
     * Removes the context sensitive command from item.
     * @param cmd the command to be removed
     */
    void removeCommandImpl(Command cmd) {
        // LCDUI Lock must be acquired
        // prior to calling this method.
        for (int i = 0; i < numCommands; ++i) {
            if (commands[i] == cmd) {
                commands[i] = commands[--numCommands];
                commands[numCommands] = null;

                if (cmd == defaultCommand) {
                    defaultCommand = null;
                }

                if (owner != null) {
                    owner.updateCommandSet();
                }

                break;
            }
        }
    }

    /**
     * Gets the set of Commands that have been added to this Item
     *
     * @return Command[] The array of Commands added to this Item
     */
    Command[] getCommands() {
        return commands;
    }

    /**
     * Gets the number of commands that have been added to this Item
     *
     * @return int The number of commands that have been added to this
     *             Item
     */
    int getCommandCount() {
        return numCommands;
    }

    /**
     * Gets ItemCommandListener for this Item
     * @return ItemCommandListener The ItemCommandListener for this Item
     */ 
    ItemCommandListener getItemCommandListener() {
        return commandListener;
    }

    /**
     * Get the owner of this Item
     *
     * @return Screen The owner of this Item
     */
    final Screen getOwner() {
        return owner;
    }

    /**
     * Set the Screen owner of this Item
     *
     * @param owner The Screen containing this Item
     */
    void setOwner(Screen owner) {
        synchronized (Display.LCDUILock) {
            if (this.owner != null && owner != null) {
                throw new IllegalStateException();
            }
            this.owner = owner;
        }
    }

    /**
     * Set the layout type of this Item
     *
     * @param layout The layout type.
     */
    void setLayoutImpl(int layout) {

        if ((layout & ~VALID_LAYOUT) != 0) {
            throw new IllegalArgumentException();
        }

        this.layout = layout;
    }

    /**
     * Get the effective layout type of this Item
     *
     * @return layout The translated layout type.
     */
    int callGetLayout() {
        int l = this.layout;
        if (l == LAYOUT_DEFAULT) {
            return LAYOUT_TOP | LAYOUT_LEFT;
        } else {
            return l;
        }
    }
}
