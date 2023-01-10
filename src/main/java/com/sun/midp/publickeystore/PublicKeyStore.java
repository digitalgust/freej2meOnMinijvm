/*
 * @(#)PublicKeyStore.java	1.5 02/07/24 @(#)
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.publickeystore;

import java.io.*;
import java.util.*;

/**
 * A read-only public keystore for use with MIDP.
 */
public class PublicKeyStore {

    /** Holds the all the keys as {@link PublicKeyInfo} objects. */
    private Vector keyList = null;

    /**
     * Constructor for subclasses.
     */
    protected PublicKeyStore() {
    };

    /**
     * Constructs a read-only keystore from a serialized keystore created
     * by {@link PublicKeyStoreBuilder}.
     * @param in stream to read a keystore serialized by
     *        {@link PublicKeyStoreBuilder#serialize(OutputStream)} from
     * @exception IOException if the key storage was corrupted
     */
    public PublicKeyStore(InputStream in) throws IOException {
        initPublicKeyStore(in, new Vector());
    }

    /**
     * Lets this class work with a writeable key list of a subclass.
     * This is needed because we cannot make the key list in this
     * class protected for security reasons. This method will only
     * work if the PublicKeyStore has not been initialized.
     * @param sharedKeyList key list of a subclass
     */
    protected void initPublicKeyStore(Vector sharedKeyList) {
        if (keyList != null) {
            return;
        }

        keyList = sharedKeyList;
    }

    /**
     * Lets this class work with a writeable key list of a subclass and
     * initialized that key list from a serialized key list.
     * This is needed because we cannot make the key list in this
     * class protected for security reasons. This method will only
     * work if the PublicKeyStore has not been initialized. 
     * @param sharedKeyList key list of a subclass
     * @param in stream to read the serialized keystore
     * @exception IOException if the key storage was corrupted
     */
    protected void initPublicKeyStore(InputStream in, Vector sharedKeyList)
            throws IOException {
        InputStorage storage = new InputStorage(in);
        PublicKeyInfo keyInfo;

        if (keyList != null) {
            return;
        }

        keyList = sharedKeyList;
        for (;;) {
            keyInfo = PublicKeyInfo.getKeyFromStorage(storage);
            if (keyInfo == null)
                return;
            
            keyList.addElement(keyInfo);
        }
    }

    /**
     * Gets a by number from the keystore. 0 is the first key.
     *
     * @param number number of key
     *
     * @return public key information of the key
     *
     * @exception  ArrayIndexOutOfBoundsException  if an invalid number was
     *             given.
     */
    public synchronized PublicKeyInfo getKey(int number) {
        return (PublicKeyInfo)keyList.elementAt(number);
    }

    /**
     * Finds a CAs Public keys based on the distinguished name.
     *
     * @param owner distinguished name of keys' owner
     * @return public key information of the keys
     */
    public synchronized Vector findKeys(String owner) {
        PublicKeyInfo keyInfo;
        Vector keys = null;
  
        for (int i = 0; i < keyList.size(); i++) {
            keyInfo = (PublicKeyInfo)keyList.elementAt(i);
	    if (keyInfo.getOwner().compareTo(owner) == 0) {
                if (keys == null) {
                    keys = new Vector();
                }

                keys.addElement(keyInfo);
            }
	}

        return keys;
    }

    /**
     * Gets the number of keys in the store.
     * @return number of keys in the keystore
     */
    public synchronized int numberOfKeys() {
        return keyList.size();
    }
}
