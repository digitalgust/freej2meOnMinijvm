/*
 * @(#)RecordStoreException.java	1.12 02/07/24 @(#)
 *
 * Portiona Copyright (c) 2000-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Copyright 2000 Motorola, Inc. All Rights Reserved.
 * This notice does not imply publication.
 */

package javax.microedition.rms; 

/**
* Thrown to indicate a general exception occurred in a record store operation.
*
* @since MIDP 1.0
*/

public class RecordStoreException
    extends java.lang.Exception
{
    /**
     * Constructs a new <code>RecordStoreException</code> with the
     * specified detail message.
     *
     * @param message the detail message
     */
    public RecordStoreException(String message) {
	super(message);
    } 
    
    /** 
     * Constructs a new <code>RecordStoreException</code> with no detail 
     * message. 
     */ 
    public RecordStoreException() {
    } 
} 
