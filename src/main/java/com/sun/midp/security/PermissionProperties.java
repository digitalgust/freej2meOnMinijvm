/*
 * @(#)PermissionProperties.java	1.6 02/09/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.security;

import com.sun.midp.midletsuite.ManifestProperties;
import com.sun.midp.midletsuite.InvalidJadException;

/**
 * Handles the properties in a permissions file. The format of the file
 * is very similar to that of a manifest file. A line that begins with
 * a space is a continuation of the previous line, and the first space
 * ignored. To save code this parser uses lets its super class parse
 * the key and value of property once it has read the entire line,
 * including contuation lines. This means that this parser is lax on
 * reporting illegal characters.
 * <p>
 * The BNF for the manifest syntax included below is extended as follows:
 * <pre>
 *   policy_file = 1*(directive)
 *   directive = (domain_def | alias_def) [newlines]
 *   domain_def = "domain:" *WS domain_id *WS newlines
 *                1*permission
 *   domain_id = 1*&lt;any Unicode char and continuation, but not newline&gt;
 *
 *   permission = permision_level ":" api_names newlines
 *   api_names: *WS alias_or_name *(*WS "," *WS alias_or_name) *WS
 *   alias_or_name = alias_ref | api_name
 *   alias_ref = &lt;alias_name&gt;  ; alias_name must be from a previous
 *                                   ; alias_def in the same policy_file
 *   permission_level = allow | user_permission_levels
 *   user_permision_levels = highest_level ["(" default_level ")"]
 *   highest_level = user_permission_level
 *   default_level = user_permision_level ; cannot be greater the highest_level
 *   user_permission_level = blanket | session | oneshot 
 *
 *   allow = "allow" ; allow access without asking the user.
 *   blanket = "blanket" ; Allow access, do not ask again.
 *                       ; Include session and oneshot when asking.
 *   session = "session" ; Allow access, ask again at next MIDlet suite
 *                       ; startup. Include oneshot when asking.
 *   oneshot = "oneshot" ; Allow access, ask again at next use.
 *                       ; If no default provided, default is to deny access.
 *
 *   alias_def = "alias:" *WS alias_name *WS alias_api_names
 *   alias_api_names =  api_name
 *                      *(*WS "," *WS api_name) *WS newlines
 *   alias_name = java_name
 *   api_name = java_class_name
 *
 *   WS = continuation | SP | HT
 *   continuation = newline SP
 *   newlines = 1*newline  ; allow blank lines to be ignored
 *   newline = CR LF | LF | CR &lt;not followed by LF&gt;
 *   CR = &lt;Unicode carriage return (0x000D)&gt;
 *   LF = &lt;Unicode linefeed (0x000A)&gt;
 *   SP = &lt;Unicode space (0x0020)&gt;
 *   HT = &lt;Unicode horizontal-tab (0x0009)&gt;
 *
 *   java_name = 1*&lt;characters allowed in a java_class_name except for
 *               "."&gt;
 *   java_class_name = 1*&lt;characters allowed in a Java class name&gt;
 * </pre>
 */
public class PermissionProperties extends ManifestProperties {

    /**
     * Store key:value pair.
     *
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to <tt>key</tt>.
     * @see #getProperty
     */
    protected void putProperty(String key, String value) {
	// We call 'addProperty()' because it is valid for a permission
	// file to contain more than one key of the same name.
	addProperty(key, value);
    }

    /**
     * Check to see if all the chars in the key of a property are valid.
     *
     * @param key key to check
     *
     * @return false if a character is not valid for a key
     */
    protected boolean checkKeyChars(String key) {
        char[] temp = key.toCharArray();
        int len = temp.length;

        for (int i = 0; i < len; i++) {
            char current = temp[i];

            if (current >= 'A' && current <= 'Z') {
                continue;
            }

            if (current >= 'a' && current <= 'z') {
                continue;
            }

            if (current >= '0' && current <= '9') {
                continue;
            }

            if (i > 0 && (current == '-' || current == '_')) {
                continue;
            }

            if (i > 0 && (current == '(' || current == ')')) {
                continue;
            }

            return false;
        }

        return true;
    }
}
