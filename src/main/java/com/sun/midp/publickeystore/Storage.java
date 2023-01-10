/*
 * @(#)Storage.java	1.5 02/09/19 @(#)
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.publickeystore;

/** Common constants for marking the type of stored fields. */
public class Storage {

    /**
     * Indicates the current version, increase the version if more types
     * are added.
     */
    public static final byte CURRENT_VERSION = 1;

    /** Indicates the stored field is a byte array. */
    public static final byte BINARY_TYPE = 1;

    /** Indicates the stored field is a String. */
    public static final byte STRING_TYPE = 2;

    /** Indicates the stored field is a long. */
    public static final byte LONG_TYPE = 3;
}
