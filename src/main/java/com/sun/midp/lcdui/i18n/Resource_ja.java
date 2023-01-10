/*
 * @(#)Resource_ja.java	1.14 02/07/31 @(#)
 *
 * Copyright (c) 2000-2001 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.lcdui.i18n;

/**
 * The Japanese localization of Resouce.
 */
public class Resource_ja extends com.sun.midp.lcdui.Resource {
    /**
     * DO NOT TOUCH.
     * @return Japanese translated words.
     */
    public Object[][] getContents() {
        return contents;
    }

    /**
     * Translate the English word(s) or sentence in the left
     * paranthese and put the translation into the right side.
     */
    static final Object[][] contents = {
        /* Alert */
        {"Done", "\u7d42\u4e86"},

        /* DateField, TextField */
        {"Back", "\u623b\u308b"},
        {"Save", "\u4fdd\u5b58"},

        /* DateField */
        {"noon", "\u6b63\u5348"},
        {"PM", "\u5348\u5f8c"},
        {"mid.", "\u5348\u524d"},
        {"AM", "\u5348\u524d"},
        {"<date>", "<\u65e5\u4ed8>"},
        {"<time>", "<\u6642\u9593>"},
        {"<date/time>", "<\u65e5\u4ed8\u30fb\u6642\u9593>"},
        {"Sun", "\u65e5"},
        {"Mon", "\u6708"},
        {"Tue", "\u706b"},
        {"Wed", "\u6c34"},
        {"Thu", "\u6728"},
        {"Fri", "\u91d1"},
        {"Sat", "\u571f"},
        {"January", "1\u6708"},
        {"February", "2\u6708"},
        {"March", "3\u6708"},
        {"April", "4\u6708"},
        {"May", "5\u6708"},
        {"June", "6\u6708"},
        {"July", "7\u6708"},
        {"August", "8\u6708"},
        {"September", "9\u6708"},
        {"October", "10\u6708"},
        {"November", "11\u6708"},
        {"December", "12\u6708"},
        {"Jan", "1\u6708"},
        {"Feb", "2\u6708"},
        {"Mar", "3\u6708"},
        {"Apr", "4\u6708"},
        {"May", "5\u6708"},
        {"Jun", "6\u6708"},
        {"Jul", "7\u6708"},
        {"Aug", "8\u6708"},
        {"Sep", "9\u6708"},
        {"Oct", "10\u6708"},
        {"Nov", "11\u6708"},
        {"Dec", "12\u6708"},


        /* Selector */
        {"About", "\u60c5\u5831"},
        {"Copyright (c) 2000-2001 Sun Microsystems, Inc. All rights reserved.",
	 "Copyright (c) 2000-2001 Sun Microsystems, Inc. All rights reserved."},
        {"Copyright (c) 2000-2001 Sun Microsystems, Inc. "
	 + "All rights reserved.\nUse is subject to license terms.\n"
	 + "Third-party software, including font technology, is copyrighted "
	 + "and licensed from Sun suppliers.  Sun, Sun Microsystems, the Sun "
	 + "logo, J2ME, the Java Coffee Cup logo, and  Java are trademarks "
	 + "or registered trademarks of Sun Microsystems, Inc. in the U.S. "
	 + "and other countries.\n"
	 + "Federal Acquisitions: Commercial Software - Government Users "
	 + "Subject to Standard License Terms and Conditions.\n\n"  
	 + "Copyright (c) 2002 Sun Microsystems, Inc. Tous droits réservés.\n"
	 + "Distribué par des licences qui en restreignent l'utilisation.\n"
	 + "Le logiciel détenu par des tiers, et qui comprend la technologie "
	 + "relative aux polices de caractères, est protégé par un copyright "
	 + "et licencié par des fournisseurs de Sun. Sun, Sun Microsystems, "
	 + "le logo Sun, J2ME, le logo Java Coffee Cup, et Java sont des "
	 + "marques de fabrique ou des marques déposées de Sun Microsystems, "
	 + "Inc. aux Etats-Unis et dans d'autres pays.",
	 "Copyright (c) 2000-2001 Sun Microsystems, Inc. "
	 + "All rights reserved.\nUse is subject to license terms.\n"
	 + "Third-party software, including font technology, is copyrighted "
	 + "and licensed from Sun suppliers.  Sun, Sun Microsystems, the Sun "
	 + "logo, J2ME, the Java Coffee Cup logo, and  Java are trademarks "
	 + "or registered trademarks of Sun Microsystems, Inc. in the U.S. "
	 + "and other countries.\n"
	 + "Federal Acquisitions: Commercial Software - Government Users "
	 + "Subject to Standard License Terms and Conditions.\n\n"  
	 + "Copyright (c) 2002 Sun Microsystems, Inc. Tous droits réservés.\n"
	 + "Distribué par des licences qui en restreignent l'utilisation.\n"
	 + "Le logiciel détenu par des tiers, et qui comprend la technologie "
	 + "relative aux polices de caractères, est protégé par un copyright "
	 + "et licencié par des fournisseurs de Sun. Sun, Sun Microsystems, "
	 + "le logo Sun, J2ME, le logo Java Coffee Cup, et Java sont des "
	 + "marques de fabrique ou des marques déposées de Sun Microsystems, "
	 + "Inc. aux Etats-Unis et dans d'autres pays."},
        {"About Wireless Profile", 
	 "\u30ef\u30a4\u30e4\u30ec\u30b9\u30d7\u30ed\u30d5\u30a1\u30a4"
	 + "\u30eb\u306b\u3064\u3044\u3066"},
        {"Choose One:", "\uff11\u3064\u3092\u9078\u629e"},
        {"Can not start: ", 
	 "\u30b9\u30bf\u30fc\u30c8\u3067\u304d\u307e\u305b\u3093: "}, 
        {"Exception: ", "\u4f8b\u5916: "},

        // Image files for splash screens.  keep these intact unless
        // you really need to change these
        {"/icons/JavaPowered-8.png", "/icons/JavaPowered-8.png"},
        {"/icons/JavaPowered-2.png", "/icons/JavaPowered-2.png"},
    };


    /**
     * Overrides Resource.getLocalizedDateString.
     * Returns the localized date string value.
     * @param dayOfWeek a String representing the day of the week.
     * @param date      a String representing the date.
     * @param month     a String representing the month.
     * @param year      a String representing the year.
     * @return a formatted date string that is suited for the target
     * language.
     * In English, this will return:
     *     "Fri, 05 Dec 2000" 
     */
    public String getLocalizedDateString(String dayOfWeek, String date, 
                                         String month, String year) {
        return year + "\u5e74" + getString(month) +  date  + "\u65e5" + 
            "(" + getString(dayOfWeek) + ")";
    }

    /**
     * Overrides Resource.getLocalizedTimeString.
     * Returns the localized time string value.
     * @param hour a String representing the hour.
     * @param min  a String representing the minute.
     * @param sec  a String representing the second.
     * @param ampm a String representing am or pm. 
     *               Note that ampm can be null.
     * @return a formatted time string that is suited for the target
     * language.
     * In English, this will return;
     *     "10:05:59 PM"
     *
     */
    public String getLocalizedTimeString(String hour, String min, 
                                         String sec, String ampm) {
        return ((ampm == null) ? "" : getString(ampm)) + 
            hour + "\u6642" +
            min  + "\u5206" +
            sec  + "\u79d2";
    }

    /**
     * Overrides Resource.getLocalizedDateTimeString.
     * Returns the localized date time string value.
     * @param dayOfWeek a String representing the day of the week.
     * @param date      a String representing the date.
     * @param month     a String representing the month.
     * @param year      a String representing the year.
     * @param hour a String representing the hour.
     * @param min  a String representing the minute.
     * @param sec  a String representing the second.
     * @param ampm a String representing am or pm.
     *               Note that ampm can be null. 
     * @return a formatted date and time string that is suited for the. 
     * target language. 
     * In English, this will return:
     *     "Fri, 05 Dec 2000 10:05:59 PM"
     */
    public String getLocalizedDateTimeString(String dayOfWeek, String date, 
                                             String month, String year,
                                             String hour, String min, 
                                             String sec, String ampm) {
        return year + "\u5e74" + month + "\u6708" +  date  + "\u65e5" + 
            "(" + getString(dayOfWeek) + ")" + 
            ((ampm == null) ? "" : getString(ampm)) + 
            hour + "\u6642" +
            min  + "\u5206" +
            sec  + "\u79d2";
    }

    /**
     * Returns the localized string for the first day of the week.
     * @return what the first day of the week is; e.g., Sunday in US,
     * Monday in France.
     */
    public int getLocalizedFirstDayOfWeek() {
        return java.util.Calendar.SUNDAY;
    }

    /**
     * Returns whether AM_PM field comes after the time field or
     * not in this locale.
     * @return whether the AM_PM field comes after the time field or
     * not.  
     */
    public boolean isLocalizedAMPMafterTime() {
        return false;
    }
}
