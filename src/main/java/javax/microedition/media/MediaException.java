/*
 * @(#)MediaException.java	1.8 02/07/24 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.media;

/**
 * A <code>MediaException</code> indicates an unexpected error
 * condition in a method.
 *
 */

public class MediaException extends Exception {

    /**
     * Constructs a <code>MediaException</code> with <code>null</code>
     * as its error detail message.
     */
    public MediaException() {
	super();
    }
    
    /**
     * Constructs a <code>MediaException</code> with the specified detail
     * message. The error message string <code>s</code> can later be
     * retrieved by the 
     * <code>{@link java.lang.Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param reason the detail message.
     */
    public MediaException(String reason) {
	super(reason);
    }
}
