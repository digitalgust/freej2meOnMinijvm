/*
 * @(#)JarReader.java	1.10 02/09/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midletsuite;

import java.lang.String;

import java.io.IOException;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import com.sun.midp.io.Util;

/**
 * This class provides a Java API for reading an entry from a Jar file stored
 * on the file system.
 */

class JarReader {
    /**
     * Returns the content of the given entry in the JAR file on the
     * file system given by jarFilePath.
     *
     * @param securityToken token with permission to install software
     * @param jarFilePath file pathname of the JAR file to read. May
     *          be a relative pathname.
     * @param entryName name of the entry to return.
     *
     * @return the content of the given entry in a byte array or null if
     *          the entry was not found
     *
     * @exception IOException if JAR is corrupt or not found
     * @exception IOException if the entry does not exist.
     * @exception SecurityException if the caller does not have permission
     *   to install software.
     */
    static byte[] readJarEntry(SecurityToken securityToken,
                               String jarFilePath, String entryName)
            throws IOException {
        byte[] asciiFilename;
        byte[] asciiEntryName;

        securityToken.checkIfPermissionAllowed(Permissions.AMS);

        asciiFilename = Util.toCString(jarFilePath);
        asciiEntryName = Util.toCString(entryName);

        return readJarEntry0(asciiFilename, asciiEntryName);
    }

    /**
     * Performs the same function as readJarEntry, except file names
     * are passed in localized characters (so that unicode -> "C" string
     * conversion does not need to happen inside native code).
     *
     * @param localJarFilePath file pathname of the JAR file to read. May
     *          be a relative pathname.
     * @param localEntryName name of the entry to return.
     *
     * @return the content of the given entry in a byte array or null if
     *         the entry was not found
     *
     * @exception IOException if JAR is corrupt or not found
     */
    private static native byte[] readJarEntry0(byte[] localJarFilePath, 
                                       byte[] localEntryName)
        throws IOException;
}
