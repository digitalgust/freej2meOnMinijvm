/*
 * @(#)Resource.java	1.10 02/09/03 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui;

import java.util.Hashtable;
import com.sun.midp.main.Configuration;

/**
 * The Resource class retrieves the RI's locale specific values such
 * as label strings and date formats from its locale specific
 * subclasses.  A subclass of Resource is easily localizable and
 * accompanied with a locale name following an underscore: for
 * example, a German one would be named Resource_de.  In this way, as
 * many related locale-specific classes as needed can be provided.
 * The location of such locale-specific classes is expected to be
 * "com.sun.midp.lcdui.i18n".  
 */
abstract public class Resource {

    /**
     * Returns a localized string for the argument string.
     * @param key an original string in the source code.
     * @return value of named resource.
     */
    public static String getString(String key) {
        String lStr = null;
        if (res != null) {
            if (res.lookup == null) {
                res.loadLookup();
            }
            lStr = (String) res.lookup.get(key);
        }
        return (lStr != null) ? lStr : key;
    }

    /**
     * Returns a localized string for the argument string after substituting
     * values for the "%d" tokens in the localized string, where "d" is 1-9
     * and representing a values 0-8 in an array. The tokens can be in any
     * order in the string. If the localized String is not found
     * the key is used as the localized string. If a "%" is not followed by
     * 1-9 then the "%" is dropped but the next char is put directly into the
     * output string, so "%%" will be "%" in the output and not count as part
     * of a token. Another example would be that "%a" would be just be "a".
     * <p>
     * For example, given "%2 had a little %1." and {"lamb", "Mary"} and there
     * is no localized string for the key, the result would be:
     * <p>
     * <blockquote>"Mary had a little lamb."</blockquote>
     *
     * @param key an original string in the source code with optional
     *            substitution tokens
     * @param values values to substitute for the tokens in the resource
     * @return value of named resource with the tokens substituted
     * @exception ArrayIndexOutOfBoundsException if there are not enough values
     *            to substitute
     */
    public static String getString(String key, String[] values) {
        boolean tokenMarkerFound = false;
        StringBuffer output;
        char currentChar;
        int length;
        String str = getString(key);

        if (str == null) {
            return null;
        }

        length = str.length();
        output = new StringBuffer(length * 2); // try to avoid resizing

        for (int i = 0; i < length; i++) {
            currentChar = str.charAt(i);
  
            if (tokenMarkerFound) {
                if (currentChar < '1' || currentChar > '9') {
                    // covers the "%%" case
                    output.append(currentChar);
                } else {
                    // substitute a value, "1" is index 0 into the value array
                    output.append(values[currentChar - '1']);
                }

                tokenMarkerFound = false;
            } else if (currentChar == '%') {
                tokenMarkerFound = true;
            } else {
                output.append(currentChar);
            }
        }
                
        return output.toString();
    }

    // provided by the subclass
    /**
     * fetch the entire resource contents.
     * @return  array of key value pairs
     */
    abstract protected Object[][] getContents();
    /** handle for the key value lookup table. */
    private Hashtable lookup = null;
    /** 
     * load the lookup table.
     */
    private void loadLookup() {
        if (lookup != null)
            return;

        Object[][] contents = getContents();
        Hashtable tmp = new Hashtable(contents.length);
        for (int i = 0; i < contents.length; ++i) {
            tmp.put(contents[i][0], contents[i][1]);
        }
        lookup = tmp;
    }

    /**
     * Returns a locale-specific formatted date string.  By default,
     * it will return like "Fri, 05 Dec 2000".
     *
     * @param dayOfWeek day of week
     * @param date date
     * @param month month
     * @param year year
     * @return formatted date string
     */
    public static String getDateString(String dayOfWeek, String date, 
                                       String month, String year) {
        String lStr = null;
        if (res != null) {
            if (res.lookup == null) {
                res.loadLookup();
            }
            lStr = res.getLocalizedDateString(dayOfWeek, date, month, year);
        }
        return (lStr != null) ? lStr :
            (dayOfWeek + ", " + date + " " + month + " " + year);
    }

    // provided by the subclass
    /**
     * get the localized version of the date string.
     *
     * @param dayOfWeek named day of week
     * @param date named current date
     * @param month name of month
     * @param year name of year 
     * @return formatted date string
     */
    abstract protected String getLocalizedDateString(String dayOfWeek, 
                                                     String date, 
                                                     String month, 
                                                     String year);

    /**
     * Returns a locale-specific formatted time string.  By default,
     * it will return like "10:05:59 PM".
     *
     * @param hour hour
     * @param min minute
     * @param sec secound
     * @param ampm AM or PM
     * @return formatted time string
     */
    public static String getTimeString(String hour, String min, 
                                       String sec, String ampm) {
        String lStr = null;
        if (res != null) {
            if (res.lookup == null) {
                res.loadLookup();
            }
            lStr = res.getLocalizedTimeString(hour, min, sec, ampm);
        }
        return (lStr != null) ? lStr : 
            (hour + ":" + min + ":" + sec + 
             ((ampm == null) ? "" : (" " + ampm)));
    }

    // provided by the subclass
    /**
     * Returns a locale-specific formatted time string.  By default,
     * it will return like "10:05:59 PM".
     *
     * @param hour hour
     * @param min minute
     * @param sec secound
     * @param ampm AM or PM
     * @return formatted time string
     */
    abstract protected String getLocalizedTimeString(String hour, String min, 
                                                     String sec, String ampm);

    /**
     * Returns a locale-specific formatted date and time string.  By
     * default, it will like return "Fri, 05 Dec 2000 10:05:59 PM".
     *
     * @param dayOfWeek day of week
     * @param date date
     * @param month month
     * @param year year
     * @param hour hour
     * @param min minute
     * @param sec secound
     * @param ampm AM or PM 
     * @return formatted time and date string
     */
    public static String getDateTimeString(String dayOfWeek, String date, 
                                           String month, String year,
                                           String hour, String min, 
                                           String sec, String ampm) {
        String lStr = null;
        if (res != null) {
            if (res.lookup == null) {
                res.loadLookup();
            }
            lStr = res.getLocalizedDateTimeString(dayOfWeek, date, month, year,
                                                  hour, min, sec, ampm);
        }
        return (lStr != null) ? lStr : 
            (dayOfWeek + ", " + date + " " + month + " " + year + " " +
             hour + ":" + min + ":" + sec +
             ((ampm == null) ? "" : (" " + ampm)));
    }

    // provided by the subclass
    /**
     * Returns a locale-specific formatted date and time string.  By
     * default, it will like return "Fri, 05 Dec 2000 10:05:59 PM".
     *
     * @param dayOfWeek day of week
     * @param date date
     * @param month month
     * @param year year
     * @param hour hour
     * @param min minute
     * @param sec secound
     * @param ampm AM or PM 
     * @return formatted time and date string
     */
    abstract protected String getLocalizedDateTimeString(String dayOfWeek, 
                                                         String date, 
                                                         String month, 
                                                         String year,
                                                         String hour, 
                                                         String min, 
                                                         String sec, 
                                                         String ampm);

    /**
     * Returns what the first day of the week is; e.g., Sunday in US,
     * Monday in France.
     * @return numeric value for first day of week
     */
    public static int getFirstDayOfWeek() {
        if (res == null) {
            return java.util.Calendar.SUNDAY;
        }
        return res.getLocalizedFirstDayOfWeek();
    }

    // provided by the subclass
    /**
     * get the localized first day of week.
     *
     * @return numeric localized first day of week.
     */
    abstract protected int getLocalizedFirstDayOfWeek();

    /**
     * Returns whether the AM_PM field comes after the time field or
     * not.  
     * @return true, if AM/PM is after the time field.
     */
    public static boolean isAMPMafterTime() {
        if (res == null) {
            return true;
        }
        return res.isLocalizedAMPMafterTime();
    }

    // provided by the subclass
    /**
     * localized indication of where the AM/PM indicator is placed.
     * @return true, if AM/PM is after the time field.
     */
    abstract protected boolean isLocalizedAMPMafterTime();
    /** local handle to the current Resource structure. */
    static Resource res = null;
    static {
        String cls = "com.sun.midp.lcdui.i18n.Resource";
        String loc = Configuration.getProperty("microedition.locale");
        if (loc != null) {
            /* 
             * This only check for the first '-' in the locale, and
             * convert to '_' for Class.forName() to work.
             */
            int hyphen;
	    if ((hyphen = loc.indexOf('-')) != -1) {
                StringBuffer tmploc = new StringBuffer(loc);
                tmploc.setCharAt(hyphen, '_');
                loc = tmploc.toString();
	    }

            while (true) {
                try {
                    Class c = Class.forName(cls + "_" + loc);
                    res = (Resource) c.newInstance();
                } catch (Throwable t) {}
                if (res == null) {
                    int pos = loc.lastIndexOf('_');
                    if (pos != -1) {
                        loc = loc.substring(0, pos);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
    }
}
