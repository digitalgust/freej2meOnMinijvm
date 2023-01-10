/*
 * @(#)SuiteDownloadInfo.java	1.5 02/07/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.dev;

import java.util.Vector;

import java.io.InputStreamReader;
import java.io.IOException;

/**
 * This class represents the Information need to download a MIDlet suite and
 * display it to a user, in a list.
 */
class SuiteDownloadInfo {
    /** URL for the JAD of this suite */
    String url;
    /** label to display to the User for this suite */
    String label;

    /**
     * Read a HTML page and pickout the links for MIDlet suites.
     * A MIDlet suite links end with .jad
     *
     * @param page HTML page to be read
     *
     * @return vector of URL/Label pairs
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    static Vector getDownloadInfoFromPage(InputStreamReader page) 
            throws IOException {
        Vector suites = new Vector();
        SuiteDownloadInfo info;
        
        info = getNextJadLink(page);
        while (info != null) {
            if (info.url.endsWith(".jad") ||
                    info.url.endsWith(".jar")) {
                suites.addElement(info);
            }

            info = getNextJadLink(page);
        }

        return suites;
    }
      
    /**
     * Read a HTML page and pickout next link.
     *
     * @param page HTML page to be read
     *
     * @return URL/Label pair
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static SuiteDownloadInfo getNextJadLink(InputStreamReader page)
            throws IOException {
        String url;
        String label;

        url = getNextUrl(page);
        if (url == null) {
            return null;
        }

        label = getNextLabel(page);
        if (label == null) {
            label = url;
        }

        return new SuiteDownloadInfo(url, label);
    }

    /**
     * Read a HTML page and pickout next href.
     *
     * @param page HTML page to be read
     *
     * @return URL
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static String getNextUrl(InputStreamReader page)
            throws IOException {
        int currentChar;
        StringBuffer url;

        if (!findString(page, "href=\"")) {
            return null;
        }

        url = new StringBuffer();

        currentChar = page.read();
        while (currentChar != '"') {
            if (currentChar == -1) {
                return null;
            }

            url.append((char)currentChar);
            currentChar = page.read();
        }

        if (url.length() == 0) {
            return null;
        }

        return url.toString();
    }

    /**
     * Read a HTML page and pickout the text after the beginning anchor.
     *
     * @param page HTML page to be read
     *
     * @return label
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static String getNextLabel(InputStreamReader page)
            throws IOException {
        int currentChar;
        StringBuffer label;

        if (!findChar(page, '>')) {
            return null;
        }

        label = new StringBuffer();

        currentChar = page.read();
        while (currentChar != '<') {
            if (currentChar == -1) {
                return null;
            }

            label.append((char)currentChar);
            currentChar = page.read();
        }

        if (label.length() == 0) {
            return null;
        }

        return label.toString();
    }

    /**
     * Find the next given string in an HTML page move past it.
     *
     * @param page HTML page to be read
     * @param targetString string to move past
     *
     * @return true if string found, else false
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static boolean findString(InputStreamReader page,
                                      String targetString) throws IOException {
        for (int i = 0; i < targetString.length(); i++) {
            if (!findChar(page, targetString.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Find the next given char in an HTML page move past it.
     *
     * @param page HTML page to be read
     * @param targetChar char to move past
     *
     * @return true if string found, else false
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static boolean findChar(InputStreamReader page, char targetChar)
            throws IOException {
        int currentChar;

        currentChar = page.read();
        while (Character.toLowerCase((char)currentChar) != targetChar) {
            if (currentChar == -1) {
                return false;
            }

            currentChar = page.read();
        }

        return true;
    }

    /**
     * Constructs a SuiteDownloadInfo.
     *
     * @param theUrl URL for this suite
     * @param theLabel label for this suite
     */
    SuiteDownloadInfo(String theUrl, String theLabel) {
        url = theUrl;
        label = theLabel;
    }
}
