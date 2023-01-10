/*
 * @(#)NetworkConnectionBase.java	1.12 02/08/15 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.io;

/**
 * Base class for Network Connection protocols.
 * This class allows one to initialize the network, if necessary,
 * before any networking code is called.
 */
public abstract class NetworkConnectionBase extends
         BufferedConnectionAdapter { 

    /**
     * This is so not StreamConnection classes can intialize the
     * network if they are loaded first.
     */
    public static void initializeNativeNetwork() {
        /*
         * This method just has to be a reference to
         * get this class loaded and cause the
         * class initializer to initialize the network.
         */
    }

    /**
     * Initialize any posible native networking code.
     */
    private static native void initializeInternal();

    /**
     * This will make sure the network is initialized once and only once
     * per VM instance.
     */
    static { 
        initializeInternal();
    }

    /** Socket object used by native code. */
    protected int handle;

    /** Private variable the native code uses. */
    private int iocb;

    /**
     * Initializes the connection.
     *
     * @param sizeOfBuffer size of the internal buffer or 0 for the default
     *                     size
     */
    protected NetworkConnectionBase(int sizeOfBuffer) {
        super(sizeOfBuffer);
    }
}


