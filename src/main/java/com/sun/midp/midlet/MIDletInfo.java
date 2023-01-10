/*
 * @(#)MIDletInfo.java	1.6 02/07/24 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midlet;

import java.util.Vector;

import com.sun.midp.io.Util;

/**
 * Simple attribute storage for MIDlets in the descriptor/manifest.
 */
public class MIDletInfo {
    /** The name of the MIDlet. */
    public String name;
    /** The icon of the MIDlet. */
    public String icon;
    /** The main class for the MIDlet. */
    public String classname;

    /**
    * Parses out the name, icon and classname. 
    * @param attr contains the name, icon and classname line to be 
    * parsed
    */
    public MIDletInfo(String attr) {
        Vector args;

        if (attr == null) {
            return;
        }

        args = Util.getCommaSeparatedValues(attr);
        if (args.size() > 0) {
            name = (String)args.elementAt(0);
            if (args.size() > 1) {
                icon = (String)args.elementAt(1);
                if (icon.length() == 0) {
                    icon = null;
                }

                if (args.size() > 2) {
                    classname = (String)args.elementAt(2);
                    if (classname.length() == 0) {
                        classname = null;
                    }
                }
            }
        }
    }

    /**
    * Container class to hold information about the current MIDlet.
    * @param name the name of the MIDlet from descriptor file or
    * manifest
    * @param icon the icon to display when the user selects the MIDlet 
    * from a list
    * @param classname the main class for this MIDlet
    */
    public MIDletInfo(String name, String icon, String classname) {
        this.name = name;
        this.icon = icon;
        this.classname = classname;
    }
}
