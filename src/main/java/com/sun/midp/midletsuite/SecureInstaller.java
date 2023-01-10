/*
 * @(#)SecureInstaller.java	1.9 02/09/17 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midletsuite;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import java.lang.String;
import java.lang.IllegalArgumentException;

import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.Connection;
import javax.microedition.io.HttpConnection;

import javax.microedition.pki.CertificateException;

import com.sun.midp.ssl.*;

import com.sun.midp.io.*;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.publickeystore.*;

import com.sun.midp.security.*;

/**
 * This class is able to verify JAD's signed according to the wireless
 * extension specification.
 */
public class SecureInstaller extends Installer {
    /**
     * MIDlet property for the application signature
     */
    private static final String SIG_PROP = "MIDlet-Jar-RSA-SHA1";

    /**
     * MIDlet property for the content provider certificates
     */
    private static final String CERT_PROP =
        "MIDlet-Certificate-";

    /** Set to the issuer of last certificate chain checked. */
    private String lastCa;

    /** Authenticated content provider certificate. */
    private X509Certificate cpCert;

    /**
     * Looks up the domain of a MIDlet suite.
     *
     * @param storageName storage name of a MIDlet suite
     * @param ca CA of an installed suite
     *
     * @return security domain of the MIDlet suite
     */
    protected String getSecurityDomainName(String storageName, String ca) {
        Vector keys;
        String domain;

        /*
         * look up the domain owner, then get the domain from the
         * trusted key store and set the security domain
         */
        try {
            keys = WebPublicKeyStore.getTrustedKeyStore().
                         findKeys(ca);

            domain = ((PublicKeyInfo)keys.elementAt(0)).getDomain();
        } catch (Exception e) {
            domain = "untrusted";
        }

        return domain;
    }

    /**
     * Verifies a Jar. On success set the name of the domain owner in the
     * install state. Post any error back to the server.
     *
     * @param jarStorage System store for applications
     * @param jarFilename name of the jar to read.
     *
     * @exception IOException if any error prevents the reading
     *   of the JAR
     * @exception InvalidJadException if the JAR is not valid or the
     *   provider certificate is missing
     */
    protected void verifyJar(RandomAccessStream jarStorage,
            String jarFilename) throws IOException, InvalidJadException {
        InputStream jarStream;
        String jarSig;

        jarSig = state.getAppProperty(SIG_PROP);
        if (jarSig == null) {
            // no signature to verify
            return;
        }

        findProviderCert(); // This will fill in the cpCert and lastCa fields
        jarStorage.connect(jarFilename, Connector.READ);

        try {
            jarStream = jarStorage.openInputStream();
            try {
                verifyStream(jarStream, jarSig);
                state.ca = lastCa;
            } finally {
                jarStream.close();
            }
        } finally {
            jarStorage.disconnect();
        }
    }

    /**
     * Find the first provider certificate that is signed by a known CA.
     * Set the lastCA field to name of the CA. Set the cpCert field to the
     * provider certificate.
     *
     * @exception InvalidJadException if the JAR is not valid or the
     *   provider certificate is missing or a general certificate error
     */
    private void findProviderCert() throws InvalidJadException {
        int chain;
        int result;

        /*
         * To save memory for applications that do not use PKI,
         * the public keys of the certificate authorities may not
         * have been loaded yet.
         */
        WebPublicKeyStore.loadCertificateAuthorities();

        for (chain = 1; ; chain++) {
            result = checkCertChain(chain); // sets the lastCa and cpCert
            if (result == 1) {
                // we found the good chain
                return;
            }

            if (result == -1) {
                // chain not found, done
                break;
            }
        }

        if (chain == 1) {
            throw new
                InvalidJadException(InvalidJadException.MISSING_PROVIDER_CERT);
        }

        // None of the certificates were issued by a known CA
        throw new
            InvalidJadException(InvalidJadException.UNKNOWN_CA, lastCa);
    }

    /**
     * Check to see if a provider certificate chain is issued by a known
     * CA. Set the lastCA field to name of the CA in any case.
     * Authenticate the chain and set the cpCert field to the provider's
     * certificate if the CA is known.
     *
     * @param chainNum the number of the chain
     *
     * @return 1 if the CA of the chain is known, 0 if not, -1 if the
     *    chain is not found
     *
     * @exception InvalidJadException if something other wrong with a
     *   other than an unknown CA
     */
    private int checkCertChain(int chainNum) throws InvalidJadException {
        int certNum;
        Vector derCerts = new Vector();
        String base64Cert;
        byte[] derCert;

        for (certNum = 1; ; certNum++) {
            base64Cert = state.getAppProperty(CERT_PROP + 
                                              chainNum + "-" + certNum);
            if (base64Cert == null) {
                break;
            }

            try {
                derCert = Base64.decode(base64Cert);
                derCerts.addElement(X509Certificate.generateCertificate(
                    derCert, 0, derCert.length));
            } catch (Exception e) {
                throw new InvalidJadException(
                    InvalidJadException.CORRUPT_PROVIDER_CERT);
            }
        }

        if (certNum == 1) {
            // Chain not found
            return -1;
        }

        try {
            lastCa = X509Certificate.verifyChain(derCerts,
                         X509Certificate.DIGITAL_SIG_KEY_USAGE,
                         X509Certificate.CODE_SIGN_EXT_KEY_USAGE,
                         WebPublicKeyStore.getTrustedKeyStore()).getIssuer();
            cpCert = (X509Certificate)derCerts.elementAt(0);

            // Authenticated
            return 1;
        } catch (CertificateException ce) {
            switch (ce.getReason()) {
            case CertificateException.UNRECOGNIZED_ISSUER:
                lastCa = ce.getCertificate().getIssuer();

                // Issuer not found
                return 0;

            case CertificateException.EXPIRED:
            case CertificateException.NOT_YET_VALID:
                throw new InvalidJadException(
                    InvalidJadException.EXPIRED_PROVIDER_CERT,
                    ce.getCertificate().getSubject());

            case CertificateException.ROOT_CA_EXPIRED:
                throw new InvalidJadException(
                    InvalidJadException.EXPIRED_CA_KEY,
                    ce.getCertificate().getIssuer());
            }

            throw new InvalidJadException(
                InvalidJadException.INVALID_PROVIDER_CERT,
                ce.getCertificate().getSubject());
        }
    }

    /**
     * Common routine that verifies a stream of bytes.
     * The cpCert field must be set before calling.
     *
     * @param stream stream to verify
     * @param base64Signature The base64 encoding of the PKCS v1.5 SHA with
     *        RSA signature of this stream.
     *
     * @exception NullPointerException if the public keystore has not been
     *            established.
     * @exception InvalidJadException the JAR signature is not valid
     * @exception IOException if any error prevents the reading
     *   of the JAR
     */
    private void verifyStream(InputStream stream, String base64Signature)
            throws InvalidJadException, IOException {
        PublicKey cpKey;
        byte[] sig;
        Signature sigVerifier;
        byte[] temp;
        int bytesRead;
        byte[] hash;

        try {
            cpKey = cpCert.getPublicKey();
        } catch (CertificateException e) {
            throw new
                InvalidJadException(InvalidJadException.INVALID_PROVIDER_CERT);
        }

        try {
            sig = Base64.decode(base64Signature);
        } catch (IOException e) {
            throw new
                InvalidJadException(InvalidJadException.CORRUPT_SIGNATURE);
        }

        try {
            // verify the jad signature
            sigVerifier = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1,
                                                false);
            sigVerifier.init(cpKey, Signature.MODE_VERIFY);

            temp = new byte[1024];
            for (; ; ) {
                bytesRead = stream.read(temp);
                if (bytesRead == -1) {
                    break;
                }

                sigVerifier.update(temp, 0, bytesRead);
            }
            
            if (!sigVerifier.verify(null, 0, 0,
                    sig, (short)0, (short)sig.length)) {
                throw new
                    InvalidJadException(InvalidJadException.INVALID_SIGNATURE);
            }
        } catch (CryptoException e) {
            throw new
                InvalidJadException(InvalidJadException.INVALID_SIGNATURE);
        }
    }
}
