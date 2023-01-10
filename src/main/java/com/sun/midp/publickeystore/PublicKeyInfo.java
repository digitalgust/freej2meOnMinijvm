/*
 * @(#)PublicKeyInfo.java	1.6 02/09/19 @(#)
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.publickeystore;

import java.io.*;

/** The information that needs to be stored for a public key. */
public class PublicKeyInfo {
    /** Used to tag the owner field in a serialized key. */
    public static final byte OWNER_TAG      = 1;

    /** Used to tag the notBefore field in a serialized key. */
    public static final byte NOT_BEFORE_TAG = 2;

    /** Used to tag the notAfter field in a serialized key. */
    public static final byte NOT_AFTER_TAG  = 3;

    /** Used to tag the modulus field in a serialized key. */
    public static final byte MODULUS_TAG    = 4;

    /** Used to tag the exponent field in a serialized key. */
    public static final byte EXPONENT_TAG   = 5;

    /** Used to gat the domain field in a serialized key. */
    public static final byte DOMAIN_TAG     = 6;

    /** Distinguished Name of the owner. */
    private String owner;

    /** Start of the key's validity period in milliseconds since Jan 1, 1970. */
    private long notBefore;

    /** End of the key's validity period in milliseconds since Jan 1, 1970. */
    private long notAfter;
    
    /** RSA modulus for the public key. */
    private byte[] modulus;
	
    /** RSA exponent for the public key. */
    private byte[] exponent;

    /** Name of the security domain. */
    private String domain;

    /**
     * Deserializes a public key from storage.
     * @param storage what to get the key from
     * @return a full populated PublicKeyInfo object
     * @exception IOException if the key storage was corrupted
     */
    static PublicKeyInfo getKeyFromStorage(InputStorage storage) 
            throws IOException {
        byte[] tag;
        Object value;
        String owner;
        long notBefore;
        long notAfter;
        byte[] modulus;
        byte[] exponent;
        String domain;
        
        tag = new byte[1];

        value = storage.readValue(tag);
        if (value == null) {
            // no more keys
            return null;
        }

        if (tag[0] != OWNER_TAG) {
            throw new IOException("public key storage corrupted");
        }

        owner = (String)value;

        value = storage.readValue(tag);
        if (tag[0] != NOT_BEFORE_TAG) {
            throw new IOException("public key storage corrupted");
        }

        notBefore = ((Long)value).longValue();

        value = storage.readValue(tag);
        if (tag[0] != NOT_AFTER_TAG) {
            throw new IOException("public key storage corrupted");
        }

        notAfter = ((Long)value).longValue();

        value = storage.readValue(tag);
        if (tag[0] != MODULUS_TAG) {
            throw new IOException("public key storage corrupted");
        }

        modulus = (byte[])value;

        value = storage.readValue(tag);
        if (tag[0] != EXPONENT_TAG) {
            throw new IOException("public key storage corrupted");
        }

        exponent = (byte[])value;

        value = storage.readValue(tag);
        if (tag[0] != DOMAIN_TAG) {
            throw new IOException("public key storage corrupted");
        }

        domain = (String)value;

        return new PublicKeyInfo(owner, notBefore, notAfter,
                                 modulus, exponent, domain);
    }

    /**
     * Constructs a PublicKeyInfo object with the specified attributes.
     * This constructor is only used by PublicKeyInfo and its subclasses.
     * @param owner      distinguished name of the owner
     * @param notBefore  start of validity period expressed in milliseconds
     *                   since midnight Jan 1, 1970 UTC 
     * @param notAfter   end of validity period expressed as above
     * @param modulus    modulus associated with the RSA Public Key
     * @param exponent   exponent associated with the RSA Public Key
     * @param domain     security domain of any application authorized
     *                   with the corresponding private key, this can be
     *                   set to null, allowing it to be set later
     */
    public PublicKeyInfo(String owner, long notBefore, long notAfter,
                   byte[] modulus, byte[] exponent, String domain) {
        this.owner = owner;
        this.notBefore = notBefore;
        this.notAfter = notAfter;
        this.modulus = modulus;
        this.exponent = exponent;
        this.domain = domain;
    }

    /**
     * Gets the distinguished name of the key's owner.
     * @return name of key's owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets the start of the key's validity period in
     * milliseconds since Jan 1, 1970.
     * @return start of a key's validity period.
     */
    public long getNotBefore() {
        return notBefore;
    }

    /**
     * Gets the end of the key's validity period in
     * milliseconds since Jan 1, 1970.
     * @return end of a key's validity period.
     */
    public long getNotAfter() {
        return notAfter;
    }
    
    /**
     * Gets RSA modulus of the public key.
     * @return the modulus
     */
    public byte[] getModulus() {
        byte[] retVal = new byte[modulus.length];

	System.arraycopy(modulus, 0, retVal, 0, modulus.length);
        return retVal;
    }
	
    /**
     * Gets RSA exponent of the public key.
     * @return the exponent
     */
    public byte[] getExponent() {
        byte[] retVal = new byte[exponent.length];

	System.arraycopy(exponent, 0, retVal, 0, exponent.length);
        return retVal;
    }

    /**
     * Gets name of the security domain for this key.
     * @return the security domain
     * @see #setDomain
     */
    public String getDomain() {
        if (domain == null) {
            return "untrusted";
        }

        return domain;
    }

    /**
     * Sets the name of the security domain for this key if it does not have
     * a domain.
     * @param domain security domain
     * @see #getDomain
     */
    public void setDomain(String domain) {
        if (domain != null) {
            return;
        }

        this.domain = domain;
    }
}
