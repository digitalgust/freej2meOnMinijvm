/*
 * @(#)Main.java	1.62 02/10/03 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.main;

import java.io.IOException;
import java.io.ByteArrayInputStream;

import java.lang.String;

import java.util.Vector;

import com.sun.midp.dev.DevMIDletSuiteImpl;

import com.sun.midp.io.j2me.storage.File;
import com.sun.midp.io.j2me.push.PushRegistryImpl;

import com.sun.midp.lcdui.Resource;

import com.sun.midp.midlet.MIDletInfo;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.Scheduler;

import com.sun.midp.midletsuite.Installer;
import com.sun.midp.midletsuite.InvalidJadException;

import com.sun.midp.security.*;
import com.sun.midp.lcdui.DisplayManagerFactory;

/**
 * The first class loaded in VM by main.c to initialize internal security and
 * perform a MIDP command.
 * <p>
 * This class performs 6 basic commands:
 * <ul>
 * <li>Application installation
 * <li>Application removal
 * <li>Application listing
 * <li>Application execution
 * <li>Graphical Application Management
 * <li>Execute a single MIDlet from the classpath
 * </ul>
 */
public class Main {

    /**
     * Prefix for storage files that classes that do not belong to a suite.
     */
    private static final String DEV_STORAGE_NAME = "run_by_class_storage_";

    /** This class has a different security domain than the MIDlet suite. */
    private static SecurityToken internalSecurityToken;

    /**
     * Initializes internal security, performs the next
     * command indicated in the command state, and sets up the next command
     * to be performed. The command loop is in main.c.
     *
     * When done exitInternal is called with a special constant so
     * main.c knows that this was the last method run, as opposed to the VM
     * aborting.
     * @param args not used, instead a {@link CommandState} object is obtained
     *             using a native method.
     */
    public static void main(String args[]) {
        CommandState state = new CommandState();

	/*
	 * pass resource strings down to the native system menu and
	 * popup choice group methods...
	 */
	initSystemLabels();

        /*
         * We will try to handle any printing at this level, because
         * displaying JAM command line errors is device specific.
         */
        try {
            initializeInternalSecurity();

	    /* Start a inbound connection watcher thread. */
	    new Thread(new PushRegistryImpl()).start();

            restoreCommandState(state);

            // handle any development machine only functions at this level
            switch (state.nextCommand) {
            case CommandProcessor.RUN_CLASS:
                runLocalClass(state);
                state.nextCommand = CommandProcessor.EXIT;
                break;

            case CommandProcessor.MANAGE:
                manage(state);
                break;

            case CommandProcessor.LIST:
            case CommandProcessor.STORAGE_NAMES:
                list(state);
                state.nextCommand = CommandProcessor.EXIT;
                break;

            case CommandProcessor.REMOVE:
                if (DEV_STORAGE_NAME.equals(state.suiteStorageName)) {
                    removeDevStorage(state);
                    state.nextCommand = CommandProcessor.EXIT;
                    break;
                }

                // fall through
            default:
                CommandProcessor.perform(state);
                if (state.status == CommandProcessor.MIDLET_SUITE_NOT_FOUND) {
                    System.out.println("The MIDlet suite was not found.");
                } else if (state.initialCommand == CommandProcessor.INSTALL &&
                        state.status == CommandProcessor.OK) {
                    System.out.println("Storage name: " +
                                       state.suiteStorageName);
                }
            }
        } catch (InvalidJadException ije) {
            System.out.println("** Error installing suite (" +
                               ije.getReason() + "): " + 
                               messageForInvalidJadException(ije));
        } catch (IOException ioe) {
            System.out.println("** Error installing suite: " +
                               ioe.getMessage());
        } catch (ClassNotFoundException ex) {
            if (state.initialCommand == CommandProcessor.MANAGE) {

              state.runExceptionMessage =
                    Resource.getString("The application cannot be launched. " +
                    "One of the application classes appears to be missing. " +
                    "This could be due to a mis-named class. Contact the " +
                    "application provider to resolve the issue.");
            } else {
                System.out.println("MIDlet class(s) not found: " + 
                                   ex.getMessage());
            }
        } catch (InstantiationException ex) {
            if (state.initialCommand == CommandProcessor.MANAGE) {
               state.runExceptionMessage = Resource.getString(
                   "The application cannot be launched. The application " +
                   "may have done an illegal operation. Contact the " +
                   "application provider to resolve the issue.") + "\n\n" +
                   ex.getMessage();
            } else {
                System.out.println(
                    "MIDlet instance(s) could not be created: " + 
                                 ex.getMessage());
            }
        } catch (IllegalAccessException ex) {
            if (state.initialCommand == CommandProcessor.MANAGE) {
                state.runExceptionMessage = Resource.getString(
                   "The application cannot be launched. The application " +
                   "may have done an illegal operation. Contact the " +
                   "application provider to resolve the issue.") + "\n\n" +
                   ex.getMessage();
            } else {
                System.out.println(
                    "MIDlet class(s) could not be accessed: " + 
                    ex.getMessage());
            }
        } catch (OutOfMemoryError ex) {
            if (state.initialCommand == CommandProcessor.MANAGE) {
                state.runExceptionMessage = Resource.getString(
                    "The application has unexpectedly quit because it ran " +
                    "out of memory.");
            } else {
                System.out.println("The MIDlet has run out of memory");
            }
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        } catch (Throwable t) {
            if (state.initialCommand == CommandProcessor.MANAGE) {
               state.runExceptionMessage =
                    Resource.getString("The application has unexpectedly " +
                    " quit. Contact the application provider to resolve " +
                    "the issue.") + "\n\n" + t.getMessage();
            } else {
                System.out.println("Exception caught in main:");
                t.printStackTrace();
                state.nextCommand = CommandProcessor.EXIT;
            }
        }

        saveCommandState(state);

        /*
         * return any non-zero number so the native main can know that
         * this is graceful exit and not the power button on the phone.
         */
        exitInternal(CommandProcessor.MAIN_EXIT);
    }

    /** Initialize the internal security for MIDP */
    private static void initializeInternalSecurity() {
        /*
         * As the first caller to create a token we can pass in null
         * for the security token.
         */
        internalSecurityToken =
            new SecurityToken(null,
               Permissions.forDomain(null, Permissions.INTERNAL_DOMAIN_NAME));

        com.sun.midp.midletsuite.Installer.initSecurityToken(
            internalSecurityToken);
        com.sun.midp.rms.RecordStoreFile.initSecurityToken(
            internalSecurityToken);
        com.sun.midp.publickeystore.WebPublicKeyStore.initSecurityToken(
            internalSecurityToken);
        com.sun.midp.io.j2me.http.Protocol.initSecurityToken(
            internalSecurityToken);
        com.sun.midp.io.j2me.https.Protocol.initSecurityToken(
            internalSecurityToken);
        com.sun.midp.io.j2me.ssl.Protocol.initSecurityToken(
            internalSecurityToken);
        com.sun.midp.io.j2me.datagram.Protocol.initSecurityToken(
            internalSecurityToken);
	DisplayManagerFactory.initSecurityToken(internalSecurityToken);
        com.sun.midp.midlet.MIDletState.initSecurityToken(
            internalSecurityToken);

	try {
            ImplicitlyTrustedClass trustedClass;

	    trustedClass = (ImplicitlyTrustedClass)
		Class.forName("com.sun.midp.io.j2me.push.PushRegistryImpl").
		newInstance();
            trustedClass.initSecurityToken(internalSecurityToken);
        } catch (Exception e) {
            // Push is optional for now
	    e.printStackTrace();
        }
    }

    /**
     * Runs a MIDlet that Manages installed MIDlet Suites.
     *
     * @param state command state to put the status and next command in
     */
    private static void manage(CommandState state) {
        Installer installer;
        MIDletSuite midletSuite;
        String nextMidletSuiteToRun;
        String[] propKeys;
        String[] propValues;

        // we need to get the installer now, before the security level drops
        installer = Installer.getInstaller();

        if (state.runExceptionMessage != null) {
            propKeys = new String[2];
            propValues = new String[2];

            propKeys[1] = "run-message";
            propValues[1] = state.runExceptionMessage;
        } else {
            propKeys = new String[1];
            propValues = new String[1];
        }
            
        // assume a class name of a MIDlet in the classpath
        propKeys[0] = "logo-displayed";
        if (state.logoDisplayed) {
            propValues[0] = "T";
        } else {
            propValues[0] = "F";
        }

        state.logoDisplayed = true;
        state.runExceptionMessage = null;
        try {
            ImplicitlyTrustedClass trustedClass;
            String nameOfManager;

            /*
             * In internal.config an alternate graphical manager
             * can be specified.
             */
            nameOfManager =
            Configuration.getProperty("com.sun.midp.graphicalmanager");
            if (nameOfManager == null) {
                nameOfManager = "com.sun.midp.dev.Manager";
            }

            /* set trusted to false so, the trust icon will not show */
            midletSuite =
                DevMIDletSuiteImpl.create(internalSecurityToken, null,
                nameOfManager, "manager_storage_", propKeys, propValues,
                Permissions.INTERNAL_DOMAIN_NAME, false,
                "Information is arriving for %1. " +
                "Is it OK to launch %1?",
                "%1 needs to start itself to check to see if it has " +
                "received information. Is that OK?");

            if (!Scheduler.getScheduler().schedule(midletSuite)) {
                // shutdown, by push or the power button
                state.nextCommand = CommandProcessor.EXIT;
            }

            // Check to see if we need to run a selected suite next
            nextMidletSuiteToRun = installer.getNextMIDletSuiteToRun();
            if (nextMidletSuiteToRun != null) {
                state.nextCommand = CommandProcessor.RUN;
                state.suiteStorageName = nextMidletSuiteToRun;
                state.midletName = installer.getNextMIDletToRun();
            }

            state.status = CommandProcessor.OK;
            return;
        } catch (Throwable e) {
            state.status = CommandProcessor.ERROR;
            state.nextCommand = CommandProcessor.EXIT;
            e.printStackTrace();
        }
    }

    /**
     * Lists the installed MIDlet Suites.
     *
     * @param state command state to put the status in
     */
    private static void list(CommandState state) {
        Installer installer = Installer.getInstaller();
        String[] appList;
        MIDletSuite midletSuite;
        String temp;
        MIDletInfo midletInfo;
        
        appList = installer.list();
        if ((appList == null) || (appList.length == 0)) {
            System.out.println("** No MIDlet Suites installed on phone");
        } else {
            for (int i = 0; i < appList.length; i++) {
                midletSuite = installer.getMIDletSuite(appList[i]);
                if (midletSuite == null) {
                    System.out.println((i + 1) + ": suite corrupted");
                    continue;
                }

                if (state.nextCommand == CommandProcessor.STORAGE_NAMES) {
                    // just list the storage name, no number
                    System.out.println(appList[i]);
                    continue;
                }

                System.out.println("[" + (i + 1) + "]");
                System.out.println("  Name: " + 
                    midletSuite.getProperty("MIDlet-Name"));
                System.out.println("  Vendor: " + 
                    midletSuite.getProperty("MIDlet-Vendor"));
                System.out.println("  Version: " + 
                    midletSuite.getProperty("MIDlet-Version"));

                temp = midletSuite.getCA();
                if (temp != null) {
                    System.out.println("  Authorized by: " + temp);
                }

                temp = midletSuite.getProperty("MIDlet-Description");
                if (temp != null) {
                    System.out.println("  Description: " + temp);
                }

                System.out.println("  Storage name: " + appList[i]);
                System.out.println("  Size: " +
                    ((midletSuite.getStorageUsed() + 1023) / 1024) + "K");

                System.out.println("  Installed From: " +
                     midletSuite.getDownloadUrl());
                System.out.println("  MIDlets:");

                for (int j = 1; ; j++) {
                    temp = midletSuite.getProperty("MIDlet-" + j);
                    if (temp == null) {
                        break;
                    }

                    midletInfo = new MIDletInfo(temp);
                    System.out.println("    " + midletInfo.name);
                }
            }
        }

        state.status = CommandProcessor.OK;
    }

    /**
     * Run a given MIDlet subclass.
     *
     * @param state command state containing MIDlet's classname.
     */
    private static void runLocalClass(CommandState state) {
        MIDletSuite midletSuite;

        try {
            // assume a class name of a MIDlet in the classpath
            midletSuite = DevMIDletSuiteImpl.create(internalSecurityToken,
                                                 state.descriptorName,
                                                 state.midletClassName,
                                                 DEV_STORAGE_NAME,
                                                 state.securityDomain);

            // if no class name was specified than repeat the selector
            do {
                if (!Scheduler.getScheduler().schedule(midletSuite)) {
                    // shutdown
                    break;
                }
            } while (state.midletClassName == null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the data used by
     * MIDlet that was run directly from the classpath.
     *
     * @param state command state to put the status in
     */
    private static void removeDevStorage(CommandState state) {
        File file;
        String storageRoot;
        Vector files;
        int numberOfFiles;
        
        file = new File(internalSecurityToken);
        storageRoot = File.getStorageRoot() + DEV_STORAGE_NAME;
        files = file.filenamesThatStartWith(storageRoot);
        numberOfFiles = files.size();
        for (int i = 0; i < numberOfFiles; i++) {
            try {
                file.delete((String)files.elementAt(i));
            } catch (IOException ioe) {
                // ignore
            }
        }

        PushRegistryImpl.unregisterConnections(internalSecurityToken,
                                               DEV_STORAGE_NAME);
    }

    /**
     * Returns the associated message for the given exception.
     * This function is here instead of in the exception its self because
     * it not need on devices, it needed only on development platforms that
     * have command line interface.
     * @param ije reason reason code for the exception
     * @return associated message for the given reason
     */
    private static String messageForInvalidJadException(
                                                    InvalidJadException ije) {
        switch (ije.getReason()) {
        case InvalidJadException.MISSING_PROVIDER_CERT:
        case InvalidJadException.MISSING_SUITE_NAME:
        case InvalidJadException.MISSING_VENDOR:
        case InvalidJadException.MISSING_VERSION:
        case InvalidJadException.MISSING_JAR_URL:
        case InvalidJadException.MISSING_JAR_SIZE:
        case InvalidJadException.MISSING_CONFIGURATION:
        case InvalidJadException.MISSING_PROFILE:
            return "A required attribute is missing";

        case InvalidJadException.SUITE_NAME_MISMATCH:
        case InvalidJadException.VERSION_MISMATCH:
        case InvalidJadException.VENDOR_MISMATCH:
            return "A required suite ID attribute in the JAR manifest " +
                "do not match the one in the JAD";

        case InvalidJadException.ATTRIBUTE_MISMATCH:
            return "The value for " + ije.getExtraData() + " in the " +
                "trusted JAR manifest did not match the one in the JAD";

        case InvalidJadException.CORRUPT_PROVIDER_CERT:
            return "The content provider certificate cannot be decoded.";

        case InvalidJadException.UNKNOWN_CA:
            return "The content provider certificate issuer " +
                ije.getExtraData() + " is unknown.";

        case InvalidJadException.INVALID_PROVIDER_CERT:
            return "The signature of the content provider certificate is " +
                  "invalid.";

        case InvalidJadException.CORRUPT_SIGNATURE:
            return "The JAR signature cannot be decoded.";

        case InvalidJadException.INVALID_SIGNATURE:
            return "The signature of the JAR is invalid.";

        case InvalidJadException.UNSUPPORTED_CERT:
            return "The content provider certificate is not a supported " +
                  "version.";

        case InvalidJadException.EXPIRED_PROVIDER_CERT:
            return "The content provider certificate is expired.";

        case InvalidJadException.EXPIRED_CA_KEY:
            return "The public key of " + ije.getExtraData() + " has expired.";

        case InvalidJadException.JAR_SIZE_MISMATCH:
            return "The Jar downloaded was not the size in the JAD";

        case InvalidJadException.OLD_VERSION:
            return "The application is an older version of one that is " +
                "already installed";

        case InvalidJadException.NEW_VERSION:
            return "The application is an newer version of one that is " +
                "already installed";

        case InvalidJadException.INVALID_JAD_URL:
            return "The JAD URL is invalid";

        case InvalidJadException.JAD_SERVER_NOT_FOUND:
            return "JAD server not found";

        case InvalidJadException.JAD_NOT_FOUND:
            return "JAD not found";

        case InvalidJadException.INVALID_JAR_URL:
            return "The JAR URL in the JAD is invalid: " + ije.getExtraData();

        case InvalidJadException.JAR_SERVER_NOT_FOUND:
            return "JAR server not found: " + ije.getExtraData();

        case InvalidJadException.JAR_NOT_FOUND:
            return "JAR not found: " + ije.getExtraData();

        case InvalidJadException.CORRUPT_JAR:
            return "Corrupt JAR, error while reading: " + ije.getExtraData();

        case InvalidJadException.INVALID_JAR_TYPE:
            if (ije.getExtraData() != null) {
                return "JAR did not have the correct media type, it had " +
                   ije.getExtraData();
            }

            return "The server did not have a resource with an acceptable " +
                "media type for the JAR URL. (code 406)";

        case InvalidJadException.INVALID_JAD_TYPE:
            if (ije.getExtraData() != null) {
                String temp = ije.getExtraData();

                if (temp.length() == 0) {
                    return "JAD did not have a media type";
                }

                return "JAD did not have the correct media type, it had " +
                    temp;
            }

            /*
             * Should not happen, since RI does not send the accept field
             * when getting the JAD.
             */
            return "The server did not have a resource with an acceptable " +
                "media type for the JAD URL. (code 406)";

        case InvalidJadException.INVALID_KEY:
            return "The attribute key [" + ije.getExtraData() +
                "] is not in the proper format";

        case InvalidJadException.INVALID_VALUE:
            return "The value for attribute " + ije.getExtraData() +
                " is not in the proper format";

        case InvalidJadException.INSUFFICIENT_STORAGE:
            return "There is insuffient storage to install this suite";

        case InvalidJadException.UNAUTHORIZED:
            return "Authentication required or failed";

        case InvalidJadException.JAD_MOVED:
            return "The JAD to be installed is for an existing suite, " +
                "but not from the same domain as the existing one: " +
                ije.getExtraData();

        case InvalidJadException.CANNOT_AUTH:
            return "Cannot authenticate with the server, unsupported scheme";

        case InvalidJadException.DEVICE_INCOMPATIBLE:
            return "Either the configuration or profile is not supported.";

        case InvalidJadException.ALREADY_INSTALLED:
            return "The JAD matches a version of a suite already installed.";

        case InvalidJadException.AUTHORIZATION_FAILURE:
            return "The suite is not authorized for " + ije.getExtraData();

        case InvalidJadException.PUSH_DUP_FAILURE:
            return "The suite is in confict with another application " +
                "listening for network data on " + ije.getExtraData();

        case InvalidJadException.PUSH_FORMAT_FAILURE:
            return "Push attribute in incorrectly formated: " +
                ije.getExtraData();

        case InvalidJadException.PUSH_PROTO_FAILURE:
            return "Connection in push attribute is not supported: " +
                ije.getExtraData();

        case InvalidJadException.PUSH_CLASS_FAILURE:
            return "The class in push attribute not in a MIDlet-<n> " +
                "attribute: " + ije.getExtraData();

        case InvalidJadException.TRUSTED_OVERWRITE_FAILURE:
            return "Cannot update a trusted suite with an untrusted " +
                "version";
        }

        return ije.getMessage();
    }

    /**
     * Pass the system localized strings for:
     * [1234567890, Menu, Back, Cancel]
     */
    private static void initSystemLabels() {
	String[] sa = new String[4];
	sa[0] = Resource.getString("1234567890");
	sa[1] = Resource.getString("Menu");
	sa[2] = Resource.getString("Back");
	sa[3] = Resource.getString("Cancel");
	
	initSystemLabels(sa);
    }

    /**
     * Pass the system strings for:
     * [1234567890, Menu, Back, Cancel]
     *
     * @param labels localized label strings for the above strings 
     *        (in order) for use by the native system menu and popup 
     *        choice group code
     */
    private static native void initSystemLabels(String[] labels);
	
    /**
     * Save the command state.
     *
     * @param state current command state
     */
    private static native void saveCommandState(CommandState state);

    /**
     * Restore the command state.
     *
     * @param state current command state
     */
    private static native void restoreCommandState(CommandState state);

    /**
     * Exit the VM with an error code. Our private version of Runtime.exit.
     * <p>
     * This is needed because the MIDP version of Runtime.exit cannot tell
     * if it is being called from a MIDlet or not, so it always throws an
     * exception.
     * <p>
     *
     * @param status Status code to return.
     */
    private static native void exitInternal(int status);

    /** This class is not meant to be instatiated */
    private Main() {
    }
}
