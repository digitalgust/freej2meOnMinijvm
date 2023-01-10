/*
 * @(#)PersistentSelector.java	1.1 02/09/11 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.dev;

import com.sun.midp.midlet.Selector;

/**
 * This class is Selector that will not exit after launching the selected
 * MIDlet, so the user can pick another MIDlet after the selected MIDlet ends.
 */
public class PersistentSelector extends Selector {
    /**
     * Create and initialize a new Persistent Selector MIDlet.
     */
    public PersistentSelector() {
        super(false);
    }
}

