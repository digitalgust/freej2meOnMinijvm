/*
 * @(#)Permissions.java	1.29 02/08/15 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.security;

import javax.microedition.io.Connector;

import java.io.InputStream;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import com.sun.midp.io.Util;
import com.sun.midp.io.j2me.storage.File;
import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.midletsuite.InvalidJadException;

/**
 * This class is a standard list of permissions that 
 * a suite can do and is used by all internal security
 * features. This class also builds a list of permission for each
 * security domain. This only class that would need to be updated in order to
 * add a new security domain.
 */
public final class Permissions {

    /** Name of the security policy file. */
    public static final String POLICY_FILENAME = "_policy.txt";

    /** Name of the internal domain. (all actions allowed) */
    public static final String INTERNAL_DOMAIN_NAME = "internal";

    /** Name of the untrusted domain. */
    public static final String UNTRUSTED_DOMAIN_NAME = "untrusted";

    /** Names to compare to JAD and policy file entries. */
    private static String[] names = {
        "com.sun.midp",
        "com.sun.midp.midletsuite.ams",
        "javax.microedition.io.Connector.http",
        "javax.microedition.io.Connector.socket",
        "javax.microedition.io.Connector.https",
        "javax.microedition.io.Connector.ssl",
        "javax.microedition.io.Connector.serversocket",
        "javax.microedition.io.Connector.datagram",
        "javax.microedition.io.Connector.datagramreceiver",
        "javax.microedition.io.Connector.comm",
        "javax.microedition.io.PushRegistry",
    };

    /** Common permission dialog title for client protocols. */
    static final String CLIENT_DIALOG_TITLE = "OK to Send Information?";

    /** Common permission question for client protocols. */
    static final String CLIENT_PERMISSION_QUESTION =
        "%1 wants to send information. This will require the use of " +
        "airtime which may cost you money. Is this OK? (%3)";

    /** Common permission dialog title for server protocols. */
    static final String SERVER_DIALOG_TITLE = "OK to Receive Information?";

    /** Common permission question for server protocols. */
    static final String SERVER_PERMISSION_QUESTION =
        "%1 wants to receive information. This will require the use of " +
        "airtime which may cost you money. Is this OK? (%3)";

    /**
     * Questions to use for the user permission form.
     * Any %1 in the question will be replaced with the suite name and
     * any %2 will be replaced with the resource name.
     */
    private static String[] questions = {
        "com.sun.midp",
        "com.sun.midp.midletsuite.ams",
        CLIENT_PERMISSION_QUESTION,
        CLIENT_PERMISSION_QUESTION,
        CLIENT_PERMISSION_QUESTION,
        CLIENT_PERMISSION_QUESTION,
        SERVER_PERMISSION_QUESTION,
        CLIENT_PERMISSION_QUESTION,
        SERVER_PERMISSION_QUESTION,
        "%1 wants to directly connect to a computer to exchange " +
        "information. Is that OK? (%3)",
        "To work properly, %1 will need to start itself periodically " +
        "to receive information. If there is already an application " +
        "running, %1 will interrupt and that application will exit. " +
        "Is that OK?",
    };

    /** Titles use for the user permission form. */
    private static String[] titles = {
        "com.sun.midp",
        "com.sun.midp.midletsuite.ams",
        CLIENT_DIALOG_TITLE,
        CLIENT_DIALOG_TITLE,
        CLIENT_DIALOG_TITLE,
        CLIENT_DIALOG_TITLE,
        SERVER_DIALOG_TITLE,
        CLIENT_DIALOG_TITLE,
        SERVER_DIALOG_TITLE,
        "OK to Connect?",
        "OK to Start-up Periodically?",
    };

    /**
     * The maximum levels are held in the first element of the permissions
     * array.
     */
    public static final int MAX_LEVELS = 0;
    /**
     * The current levels are held in the first element of the permissions
     * array.
     */
    public static final int CUR_LEVELS = 1;

    /** com.sun.midp permission ID. */
    public static final int MIDP = 0;
    /** com.sun.midp.midletsuite.ams permission ID. */
    public static final int AMS = 1;
    /** javax.microedition.io.Connector.http permission ID. */
    public static final int HTTP = 2;
    /** javax.microedition.io.Connector.socket permission ID. */
    public static final int TCP = 3;
    /** javax.microedition.io.Connector.https permission ID. */
    public static final int HTTPS = 4;
    /** javax.microedition.io.Connector.ssl permission ID. */
    public static final int SSL = 5;
    /** javax.microedition.io.Connector.serversocket permission ID. */
    public static final int TCP_SERVER = 6;
    /** javax.microedition.io.Connector.datagram permission ID. */
    public static final int UDP = 7;
    /** javax.microedition.io.Connector.datagramreceiver permission ID. */
    public static final int UDP_SERVER = 8;
    /** javax.microedition.io.Connector.comm permission ID. */
    public static final int COMM = 9;
    /** javax.microedition.io.PushRegistry permission ID. */
    public static final int PUSH = 10;
    /** Number of permissions. */
    public static final int NUMBER_OF_PERMISSIONS = 11;

    /** Never allowed an permission. */
    public static final byte NEVER = 0;
    /** Allow an permission with out asking the user. */
    public static final byte ALLOW = 1;
    /** Allow permission until the the user changes it in the settings form. */
    public static final byte BLANKET_GRANTED = 2;
    /** Allow a permission after asking the user once. */
    public static final byte BLANKET = 4;
    /** Allow an permission after asking the user once a session. */
    public static final byte SESSION = 8;
    /** Allow an permission after asking the user every use. */
    public static final byte ONE_SHOT = 16;
    /** Denied by the user, until next session. */
    public static final byte DENY_SESSION = 32;
    /** Ask the user to Deny by default. */
    public static final byte DENY = 64;
    /** Deny by the user, until the user changes it in the settings form. */
    public static final byte USER_DENIED = -128;

    /** Table to save all permissions; keyed by the domain */
    private static Hashtable permissionsTable = null;

    /**
     * Get the name of a permission.
     *
     * @param permission permission number
     *
     * @return permission name
     *
     * @exception SecurityException if the permission is invalid
     */
    public static String getName(int permission) {
        if (permission < 0 || permission >= names.length) {
            throw new SecurityException(SecurityToken.STD_EX_MSG);
        }

        return names[permission];
    }

    /**
     * Get the dialog title for a permission.
     *
     * @param permission permission number
     *
     * @return permission dialog title
     *
     * @exception SecurityException if the permission is invalid
     */
    public static String getTitle(int permission) {
        if (permission < 0 || permission >= titles.length) {
            throw new SecurityException(SecurityToken.STD_EX_MSG);
        }

        return titles[permission];
    }

    /**
     * Get the question for a permission.
     *
     * @param permission permission number
     *
     * @return permission question
     *
     * @exception SecurityException if the permission is invalid
     */
    public static String getQuestion(int permission) {
        if (permission < 0 || permission >= questions.length) {
            throw new SecurityException(SecurityToken.STD_EX_MSG);
        }

        return questions[permission];
    }

    /**
     * Create a list of permission groups a domain is permitted to perform.
     *
     * @param token security token of the calling class, can be null for
     *              built-in classes.
     * @param name name of domain
     *
     * @return 2 arrays, the first containing the maxium level for each
     *     permission, the second containing the default or starting level
     *     for each permission supported
     */
    public static byte[][] forDomain(SecurityToken token, String name) {
        byte [] maximums = new byte[NUMBER_OF_PERMISSIONS];
        byte [] defaults = new byte[NUMBER_OF_PERMISSIONS];
        byte[][] permissions = {maximums, defaults};

        if (INTERNAL_DOMAIN_NAME.equals(name)) {
            for (int i = 0; i < maximums.length; i++) {
                maximums[i] = ALLOW;
                defaults[i] = ALLOW;
            }

            return permissions;
        }

        /*
         * Get permissions from the permissions file
         */
        if (getPermissions(token, name, maximums, defaults)) {
            return permissions;
        }            

        // unknown is the same as untrusted
        if (getPermissions(token, "untrusted", maximums, defaults)) {
            return permissions;
        }

        throw new SecurityException("untrusted domain is not configured");
    }

    /**
     * Create an empty list of permission groups.
     *
     * @return array containing the empty permission groups
     */
    public static byte[] getEmptySet() {
        byte[] permissions = new byte[NUMBER_OF_PERMISSIONS];

        clearPerms(permissions);  // Set permissions to default values
        return permissions;
    }

    /**
     * Expand all alias names in the given API list to their constituent
     * APIs.
     *
     * @param apiList a list of APIs that may or may not contain alias
     *                names.
     * @param aliasTable a table that contains all known aliases.
     *
     * @return Vector a list of APIs with all aliases fully expanded.
     */
    private static Vector expandAlias(Vector apiList, Hashtable aliasTable) {
        boolean aliasMatch = false;
        Vector  returnList;
        int     aliasIdx;
        int     apiIdx;

        /* Exit if there are no APIs or aliases defined */
        if ((aliasTable == null) || (apiList == null)) {
            return apiList;
        }

        /* We will have at leave apiList.size() elements in the return list */
        returnList = new Vector(apiList.size());

        /* Check every API entry to see if it's an alias */
        for (apiIdx = 0; apiIdx < apiList.size(); apiIdx++) {
            String apiName   = (String)apiList.elementAt(apiIdx);

            /* If the API name contains a period, it cannot be an alias */
            if (apiName.indexOf('.') == -1) {
                Enumeration e  = aliasTable.keys();

                while (e.hasMoreElements()) {
                    String aliasName = (String)e.nextElement();

                    if (apiName.equals(aliasName)) {
                        Vector aliasVector =
                            (Vector)aliasTable.get(aliasName);
                        
                        // Add all API names contained in the alias
                        for (int i = 0; i < aliasVector.size(); i++) {
                            returnList.addElement(aliasVector.elementAt(i));
                        }

                        aliasMatch = true;
                        
                        break; // Can only match one alias name per apiName
                    }
                }
            }

            if (aliasMatch) {
                aliasMatch = false;
                continue;           // Do not add apiName if it is an alias
            }

            /* Did not match an alias name; this must be a real API name */
            returnList.addElement(apiName);
        }

        return returnList;
    }

    /**
     * Clear the permissions list by setting all permissions to
     * Permissions.DENY.
     *
     * @param perms a permission array to clear.
     */
    private static void clearPerms(byte[] perms) {
        // Assume perms array is non-null
        for (int i = 0; i < perms.length; i++) {
            perms[i] = Permissions.NEVER;          // This is default perm
        }
    }

    /**
     * Find the given permission name in the global names list.
     *
     * @param apiName the name of the API to find.
     *
     * @return int the index into global names list.
     *
     * @exception IllegalArgumentException if apiName is not found in
     * the global names list
     */
    private static int getPermIndex(String apiName) {
        int nameIdx;

        for (nameIdx = 0; nameIdx < names.length; nameIdx++) {
            if (names[nameIdx].equals(apiName)) {
                return nameIdx;
            }
        }

        // Abort processing
        throw new IllegalArgumentException("bad API name: " + apiName);
    }

    /**
     * Set the default and highest permission level for the given
     * API(s). The API list must only include real API names and
     * not alias names.
     *
     * @param perms the permission array to set
     * @param apiList a list of APIs to set
     * @param highestLevel the highest permission level for every API in
     * apiList
     * @param defaultLevel the default permission level for every API in
     * apiList
     */
    private static void setPerms(byte[] perms, Vector apiList, 
                                 byte highestLevel, byte defaultLevel) {
        int apiIdx;

        for (apiIdx = 0; apiIdx < apiList.size(); apiIdx++) {
            int permIdx;

            permIdx = getPermIndex((String)apiList.elementAt(apiIdx)) * 2;
            perms[permIdx]   = highestLevel;
            perms[permIdx+1] = defaultLevel;
        }
    }

    /**
     * Convert the string permission name to the byte constant value.
     *
     * @param permString the permission string to convert
     *
     * @return byte the permission constant value
     *
     * @exception IllegalArgumentException if permString is not one of
     * pre-defined permission values
     */
    private static byte getPermFromString(String permString) {
        /* Do not check for 'null'; we should throw an NPE */
        if ("allow".equals(permString)) {
            return Permissions.ALLOW;
        } else if ("blanket".equals(permString)) {
            return Permissions.BLANKET;
        } else if ("session".equals(permString)) {
            return Permissions.SESSION;
        } else if ("oneshot".equals(permString)) {
            return Permissions.ONE_SHOT;
        } else {
            // Abort processing
            throw new IllegalArgumentException("bad perm level: " +
                                               permString);
        }
    }

    /**
     * Read the permissions file into the global permissions table.
     *
     * @param token security token of the calling class
     *
     * @exception InvalidJadException if there was any trouble reading
     * or parsing the permissions file.
     */
    private static void readPermissionsTable(SecurityToken token)
            throws InvalidJadException {
        RandomAccessStream storage;
        InputStream permIS;
        
        try {
            storage = new RandomAccessStream(token);
            storage.connect(File.getStorageRoot() + POLICY_FILENAME,
                            Connector.READ);
            permIS = storage.openInputStream();
        } catch (Exception e) {
            throw new InvalidJadException(InvalidJadException.JAD_NOT_FOUND);
        }
        
        try {
            PermissionProperties pp = new PermissionProperties();
            byte[]    newPerms      = new byte[NUMBER_OF_PERMISSIONS*2];
            String    currentDomain = null;
            Hashtable aliasTable    = null;
            String    propertyValue;
            String    propertyKey;
            
            pp.load(permIS);
            clearPerms(newPerms);
            
            for (int i = 0; i < pp.size(); i++) {
                propertyKey   = pp.getKeyAt(i);
                propertyValue = pp.getValueAt(i);
                
                if ("alias".equals(propertyKey)) {
                    String aliasName;
                    String aliasValue;
                    int    nameIdx;
                    
                    nameIdx    = propertyValue.indexOf(' ');
                    aliasName  = propertyValue.substring(0, nameIdx);
                    aliasValue = propertyValue.substring(nameIdx + 1, 
                                     propertyValue.length());
                    if (aliasTable == null) {
                        aliasTable = new Hashtable();
                    }
                    aliasTable.put(aliasName, 
                                   Util.getCommaSeparatedValues(aliasValue));
                } else if ("domain".equals(propertyKey)) {
                    if (permissionsTable == null) {
                        permissionsTable = new Hashtable();
                    }

                    if (currentDomain != null) {
                        permissionsTable.put(currentDomain, newPerms);

                        // hash tables do not copy values
                        newPerms = new byte[NUMBER_OF_PERMISSIONS*2];
                        clearPerms(newPerms);
                    }

                    currentDomain = propertyValue;
                } else if ("allow".equals(propertyKey)) {
                    Vector apiNames;
                    
                    apiNames = Util.getCommaSeparatedValues(propertyValue);
                    apiNames = expandAlias(apiNames, aliasTable);

                    setPerms(newPerms, apiNames, 
                             Permissions.ALLOW, Permissions.ALLOW);
                } else {
                    /* 
                     * Must be a user permission level. If it is some
                     * other string, getPermFromString() will throw
                     * an IllegalArgumentException and abort processing
                     * of the policy file
                     */
                    byte perm;
                    byte defaultPerm;
                    int  defaultPermIdx;
                    if ((defaultPermIdx = propertyKey.indexOf('(')) != -1) {
                        String permString = 
                            propertyKey.substring(0, defaultPermIdx);
                        String defaultPermString = 
                            propertyKey.substring(defaultPermIdx + 1, 
                                                  propertyKey.indexOf(')'));
                        
                        perm = getPermFromString(permString);
                        defaultPerm = getPermFromString(defaultPermString);
                    } else {
                        perm = getPermFromString(propertyKey);
                        defaultPerm = Permissions.DENY;
                    }
                    
                    Vector apiNames;
                    
                    apiNames = Util.getCommaSeparatedValues(propertyValue);
                    apiNames = expandAlias(apiNames, aliasTable);

                    setPerms(newPerms, apiNames, perm, defaultPerm);
                }
            }
            if (permissionsTable == null) {
                permissionsTable = new Hashtable();
            }
            if (currentDomain != null) {
                permissionsTable.put(currentDomain, newPerms);
            }
        } catch (Exception e) {
            System.out.println("Corrupt policy file");
            e.printStackTrace();
            permissionsTable = null; // Do not save half-processed permissions
            throw new InvalidJadException(InvalidJadException.INVALID_KEY);
        } finally {
            try {
                storage.disconnect();
            } catch (Exception e) {
                // nothing we can do.
            }
        }
    }

    /**
     * Get the list of permissions and defaults for a domain.
     *
     * @param token security token of the calling class
     * @param domain name of domain
     * @param permissions array to hold the permissions
     * @param defaults array to hold the defaults for the user query
     *
     * @return true if the domain was found, otherwise false
     */
    private static boolean getPermissions(SecurityToken token, String domain,
            byte[] permissions, byte[] defaults) {

        if (permissionsTable == null) {
            // We have not read the policy file yet..
            try {
                readPermissionsTable(token);
            } catch (InvalidJadException ije) {
                return false;
            }
        }

        byte[] permList = (byte[])permissionsTable.get(domain);

        if (permList != null) {
            // Copy permissions from permission table
            for (int idx = 0; idx < NUMBER_OF_PERMISSIONS; idx++) {
                int permIdx = idx * 2;
                
                permissions[idx] = permList[permIdx];
                defaults[idx]    = permList[permIdx+1];
            }
            
            return true;
        }

        return false;
    }
}

