/*
 * @(#)RecordStoreNotOpenException.java	1.11 02/07/24 @(#)
 *
 * Portiona Copyright (c) 2000-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Copyright 2000 Motorola, Inc. All Rights Reserved.
 * This notice does not imply publication.
 */

package javax.microedition.rms; 

/**
* Thrown to indicate that an operation was attempted on a closed record store.
*
* @since MIDP 1.0
*/

public class RecordStoreNotOpenException
    extends RecordStoreException
{
    /**
     * Constructs a new <code>RecordStoreNotOpenException</code> with the
     * specified detail message.
     *
     * @param message the detail message
     */
    public RecordStoreNotOpenException(String message) {
	super(message);
    } 
    
    /** 
     * Constructs a new <code>RecordStoreNotOpenException</code> with no detail 
     * message. 
     */ 
    public RecordStoreNotOpenException() {
    } 
}
