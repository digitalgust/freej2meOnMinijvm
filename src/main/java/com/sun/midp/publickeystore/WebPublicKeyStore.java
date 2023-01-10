/*
 * @(#)WebPublicKeyStore.java	1.12 02/09/19 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.publickeystore;

import java.io.*;
import java.util.*;
 
import javax.microedition.io.*;

import com.sun.midp.ssl.*;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.security.*;

/**
 * A public keystore that can used with SSL.
 * To work with SSL this class implements the SSL
 * {@link PublicKeyStore} interface.
 */
public class WebPublicKeyStore extends PublicKeyStore implements CertStore {
    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken;

    /** keystore this package uses for verifying descriptors */
    private static WebPublicKeyStore trustedKeyStore;

    /**
     * Initializes the security domain for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    static public void initSecurityToken(SecurityToken token) {
        if (classSecurityToken == null) {
            classSecurityToken = token;
        }
    }
        
    /**
     * Load the certificate authorities for the MIDP RI from storage
     * into the SSL keystore.
     */
    public static void loadCertificateAuthorities() {
        RandomAccessStream storage;
        InputStream tks;
        WebPublicKeyStore ks;

        if (trustedKeyStore != null) {
            return;
        }

        try {
            storage = new RandomAccessStream(classSecurityToken);
            storage.connect(File.getStorageRoot() + "_main.ks",
                            Connector.READ);
            tks = storage.openInputStream();
        } catch (Exception e) {
            System.out.println("Could not open the trusted key store, " +
                               "cannot authenticate HTTPS servers");
            return;
        }

        try {
            ks = new com.sun.midp.publickeystore.WebPublicKeyStore(tks);
        } catch (Exception e) {
            System.out.println("Corrupt key store file, " +
                               "cannot authenticate HTTPS servers");
            e.printStackTrace();
            return;
        } finally {
            try {
                storage.disconnect();
            } catch (Exception e) {
                // nothing we can do.
            }
        }

        WebPublicKeyStore.setTrustedKeyStore(ks);
    }

    /**
     * Establish the given keystore as the system trusted keystore.
     * This is a one-shot method, it will only set the trusted keystore
     * it there is no keystore set. For security purposes only
     * read-only PublicKeyStores should be set.
     * @param keyStore keystore to be the system trusted keystore
     * @see #getTrustedKeyStore
     */
    private static void setTrustedKeyStore(WebPublicKeyStore keyStore) {
        if (trustedKeyStore != null) {
            return;
        }

        trustedKeyStore = keyStore;

        SSLStreamConnection.setTrustedCertStore(keyStore);
        SSLStreamConnection.lockTrustedCertStore();     
    }

    /**
     * Provides the keystore of resident public keys for
     * security domain owners and other CA's.
     * @return keystore of domain owner and CA keys
     * @see #setTrustedKeyStore
     */
    public static WebPublicKeyStore getTrustedKeyStore() {
        return trustedKeyStore;
    }

    /**
     * Constructs an extendable keystore from a serialized keystore created
     * by {@link PublicKeyStoreBuilder}.
     * @param in stream to read a keystore serialized by
     *        {@link PublicKeyStoreBuilder#serialize(OutputStream)} from
     * @exception IOException if the key storage was corrupted
     */
    public WebPublicKeyStore(InputStream in) throws IOException {
        super(in);
    }

    /**
     * Returns the certificate(s) corresponding to a 
     * subject name string.
     * 
     * @param subjectName subject name of the certificate in printable form.
     *
     * @return corresponding certificates or null (if not found)
     */ 
    public X509Certificate[] getCertificates(String subjectName) {
        Vector keys;
        X509Certificate[] certs;

        keys = findKeys(subjectName);
        if (keys == null) {
            return null;
        }

        certs = new X509Certificate[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            certs[i] = createCertificate((PublicKeyInfo)keys.elementAt(i));
        }

        return certs;
    }

    /**
     * Creates an {@link X509Certificate} using the given public key
     * information.
     * @param keyInfo key information
     * @return X509 certificate
     */
    public static X509Certificate createCertificate(PublicKeyInfo keyInfo) {
        if (keyInfo == null) {
            return null;
        }

	try {
	    X509Certificate cert;

	    cert = new X509Certificate((byte)1, // fixed at version 1
                                new byte[0],
                                keyInfo.getOwner(),
                                keyInfo.getOwner(), // issuer same as subject
                                keyInfo.getNotBefore(),
                                keyInfo.getNotAfter(),
                                keyInfo.getModulus(),
                                keyInfo.getExponent(),
                                null, // we don't use finger prints
                                0);
	    return cert;
	} catch (Exception e) {
	    return null;
	}
    }
}
