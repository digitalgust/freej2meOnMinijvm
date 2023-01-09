/*
 * @(#)ItemStateListener.java	1.13 02/07/24 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.lcdui;

/**
 * This interface is used by applications which need to receive
 * events that indicate changes in the internal
 * state of the interactive items within a {@link Form Form}
 * screen.
 *
 * @see Form#setItemStateListener(ItemStateListener)
 * @since MIDP 1.0
 */
public interface ItemStateListener {

    /**
     * Called when internal state of an <code>Item</code> has been
     * changed by the user.
     * This happens when the user:
     * <UL>
     * <LI>changes the set of selected values in a
     * <code>ChoiceGroup</code>;</LI>
     * <LI>adjusts the value of an interactive <code>Gauge</code>;</LI>
     * <LI>enters or modifies the value in a <code>TextField</code>;</LI>
     * <LI>enters a new date or time in a <code>DateField</code>; and</LI>
     * <LI>{@link Item#notifyStateChanged} was called on an
     * <code>Item</code>.</LI>
     * </UL>
     *
     * <p> It is up to the device to decide when it considers a
     * new value to have been entered into an <code>Item</code>.  For example,
     * implementations of text editing within a <code>TextField</code>
     * vary greatly
     * from device to device. </P>
     *
     * <p>In general, it is not expected that the listener will be called 
     * after every change is made. However, if an item's value
     * has been changed, the listener 
     * will be called to notify the application of the change
     * before it is called for a change on another item, and before a 
     * command is delivered to the <code>Form's</code> 
     * <code>CommandListener</code>. For implementations that have the
     * concept of an input
     * focus, the listener should be called no later than when the focus moves 
     * away from an item whose state has been changed.  The listener
     * should be called only if the item's value has actually been
     * changed.</P>
     *
     * <p> The listener is not called if the application changes
     * the value of an interactive item. </p>
     *
     * @param item the item that was changed
     */
    public void itemStateChanged(Item item);
}
