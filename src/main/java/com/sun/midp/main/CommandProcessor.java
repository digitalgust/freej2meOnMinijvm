/*
 * @(#)CommandProcessor.java	1.27 02/10/03 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.main;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.lang.String;

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.Scheduler;

import com.sun.midp.midletsuite.Installer;
import com.sun.midp.midletsuite.InvalidJadException;

/**
 * Performs basic MIDlet Suite management commands.
 * <p>
 * The <code>Main</code> class performs four basic operations:
 * <ul>
 * <li>Application installation
 * <li>Application removal
 * <li>Application listing
 * <li>Application execution
 * </ul>
 * <p>
 * Together, these operations are refered to as the Java Application
 * Manager or <em>JAM</em>.
 *
 */
public class CommandProcessor {
    /** return value from Main, so we know that Main exited normally */
    static final int MAIN_EXIT = 2001;

    /** status for success */
    static final int OK = 0;

    /** status when an error occurs */
    static final int ERROR = -1;

    /** status when the MIDlet suite is not found */
    static final int MIDLET_SUITE_NOT_FOUND = -2;

    /** command code for exit */
    static final int EXIT        = 1;

    /** command code for Graphical MIDlet Suite Manager in a dev environment */
    static final int MANAGE      = 2;

    /** command code for installing a suite */
    static final int INSTALL     = 3;

    /** command code for installing and running a suite */
    static final int INSTALL_RUN = 4;

    /** command code for running a suite */
    static final int RUN         = 5;

    /** command code for removing a suite */
    static final int REMOVE      = 6;

    /** command code for listing all installed suites in dev environment */
    static final int LIST        = 7;

    /** command code for running a class directly */
    static final int RUN_CLASS   = 8;

    /** command code for listing the only the storage names of suites  */
    static final int STORAGE_NAMES = 9;

    /** whether-or-not we are peforming a command */
    private static boolean performing = false;

    /** Application Installer */
    private static Installer installer = null;

    /**
     * Perform the specified command.
     * <p>
     * The operation is specified in the initialCommand field of the
     * command state. The nextCommand state will be set to next command or
     * EXIT. The status will also be set.
     * <p>
     * The command state also contains the arguments needed a command.
     * <pre>
     * Command     Argument(s)
     * ----------- -----------
     * install     suiteURL
     * remove      suiteStorageName
     * run         suiteStorageName and optionally (midletName and runOnce)
     * install-run suiteURL and optionally (midletName, autotest, and runOnce)
     * </pre>
     * <p>
     * The <code>list</code> command displays a list of the currently
     * installed applications.
     * <p>
     * The <code>install</code> command will install the given application.
     * The URL used for installation must point to a valid application
     * descriptor file. Only HTTP URLs are accepted. If any errors were
     * encountered during installtion, the status will be ERROR or
     * MIDLET_SUITE_NOT_FOUND. if forceOverwrite in the command state is
     * set MIDlet suite will be forcibly installed. See
     * <a href="Installer.html#installJad(java.lang.String, boolean)">
     * Installer.installJad()</a> for a description.
     * <p>
     * The <code>remove</code> command will remove the given application
     * suite or storage used when running single class of local descriptor.
     * If you specify the special keywork <code>all</code> as the
     * suite name, <em>all</em> application suites will be removed.
     * <p>
     * The <code>run</code> command will execute the given MIDlet from
     * a suite, or if not MIDlet is given, let the user select one.
     * If runOnce in the command state is the next command will be
     * remove.
     * <p>
     * The <code>install-run</code> command performs the <code>install</code>
     * and then the <code>run</code> command. If autotest and runOnce are
     * set in the command state, then after removing the suite the next
     * command will be install. The autotest cycle is broken when the 
     * suite at the URL is not found.
     *
     * @param state command state containing the command to perform and
     *      any arguments that the command may require.
     *
     * @exception IOException is thrown, if an error occurs fetching the 
     *              descriptor or jar file
     * @exception InvalidJadException is thrown, if an error is detected in the 
     *                  descriptor file
     * @exception ClassNotFoundException is thrown, if the desginated MIDlet is 
     *                    not  found in the downloaded bundle
     * @exception InstantiationException is thrown, installer can not be 
     *                    constructed
     * @exception IllegalAccessException is thrown,if the caller does not have 
     *              sufficient authority  to perorm the requested 
     *              operation
     */
    public static void perform(CommandState state) throws IOException,
            InvalidJadException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {

        String nextMidletSuiteToRun;

        // don't run twice, a sneaky untrusted MIDlet may try this
        if (performing) {
            return;
        }

        performing = true;

        // we need to get the installer now, before the security level drops
        if (installer == null) {
            installer = Installer.getInstaller();
        }

        dispatch(state);

        nextMidletSuiteToRun = installer.getNextMIDletSuiteToRun();
        if (nextMidletSuiteToRun != null) {
            state.nextCommand = RUN;
            state.suiteStorageName = nextMidletSuiteToRun;

            // the run method should clear this
            state.midletName = installer.getNextMIDletToRun();
        }

        performing = false;
    }

    /**
     * Dispatches the command for processing.
     *
     * @param state command state containing the command to perform and
     *      any arguments that the command may require.
     */
    private static void dispatch(CommandState state) throws IOException,
            InvalidJadException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        int currentCommand;

        currentCommand = state.nextCommand;
        state.nextCommand = EXIT;

        if (currentCommand == INSTALL || currentCommand == INSTALL_RUN) {
            install(state);
        } else if (currentCommand == RUN) {
            // keep the Manager around if that was initial command
            if (state.initialCommand == MANAGE) {
                state.nextCommand = MANAGE;
            }

            run(state);
        } else if (currentCommand == REMOVE) {
            remove(state);
        }
    }

    /**
     * Installs a MIDlet Suite.
     *
     * @param state command state containing the URL of the suite to install
     *
     * @exception IllegalArgumentException if the JAD URL in the state is
     *   not valid
     */
    private static void install(CommandState state) throws IOException,
            InvalidJadException {
        try {
            state.status = ERROR;

            if (state.securityDomain != null) {
                installer.setUnsignedSecurityDomain(state.securityDomain);
            }

            // save the suite name for "run" command
            state.suiteStorageName = installer.installJad(state.suiteURL,
                                            state.forceOverwrite,
                                            state.removeRMS, null);
            state.status = OK;

            // this may be a one of a set of commands to execute
            if (state.initialCommand == INSTALL_RUN) {
                state.nextCommand = RUN;
            }

            return;
        } catch (InvalidJadException ije) {
            if (ije.getReason() == InvalidJadException.INVALID_JAD_URL) {
                state.status = MIDLET_SUITE_NOT_FOUND;
                throw new IllegalArgumentException("The JAD URL is not valid");
            }

            if (ije.getReason() == InvalidJadException.JAD_SERVER_NOT_FOUND ||
                ije.getReason() == InvalidJadException.JAD_NOT_FOUND) {

                state.status = MIDLET_SUITE_NOT_FOUND;

                if (state.autotest && state.suiteStorageName != null) {
                    // This end of the tests, remove the last test installed
                    state.nextCommand = REMOVE;
                }

                return;
            }

            if (ije.getReason() == InvalidJadException.INVALID_JAD_TYPE) {
                // media type of JAD was wrong, it could be a JAR
                String mediaType = (String)ije.getExtraData();

                if (Installer.JAR_MT_1.equals(mediaType) ||
                        Installer.JAR_MT_2.equals(mediaType)) {
                    // re-run as a JAR only install
                    state.suiteStorageName =
                        installer.installJar(state.suiteURL,
                                             state.forceOverwrite,
                                             state.removeRMS, null);
                    state.status = OK;

                    // this may be a one of a set of commands to execute
                    if (state.initialCommand == INSTALL_RUN) {
                        state.nextCommand = RUN;
                    }

                    return;
                }
            }

            throw ije;
        } catch (IllegalArgumentException iae) {
            // the suite was not found be case of an illegal argument
            state.status = MIDLET_SUITE_NOT_FOUND;
            throw iae;
        } finally {
            if (state.status == ERROR) {
                // during autotest mode, only exit if we cannot get the suite
                if (state.autotest) {
                    state.nextCommand = INSTALL;
                }
            }
        }
    }

    /**
     * Runs a MIDlet suite.
     *
     * @param state command state containing the name of the suite to run
     */
    private static void run(CommandState state) throws
        ClassNotFoundException, InstantiationException,
            IllegalAccessException {

        MIDletSuite midletSuite;

        // Note: the install command creates the suite storage names

        state.status = ERROR;

        if (state.suiteStorageName == null) {
            throw new IllegalArgumentException("The storage name for the " +
                "MIDlet suite was not given");
        }

        if (state.midletName != null || state.midletNumber == null) {
            String temp = state.midletName;

            /*
             * When autotesting push, both the number and name may both be set
             * remove the launched suite name for the next test.
             */
            state.midletName = null;

            midletSuite = installer.getMIDletSuite(state.suiteStorageName,
                                                   temp);

        } else {
            int midletNumber;

            try {
                midletNumber = Integer.parseInt(state.midletNumber);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("The value for " +
                    "MIDlet number was not formatted properly");
            }

            midletSuite = installer.getMIDletSuite(state.suiteStorageName,
                                                   midletNumber);
        }

        if (midletSuite == null) {
            state.status = MIDLET_SUITE_NOT_FOUND;
            return;
        }

        // remove any run once suites
        if (state.runOnce) {
            state.nextCommand = REMOVE;
        } else if (state.autotest) {
            state.nextCommand = INSTALL;
        }

        // run MIDlets til there are none
        if (!Scheduler.getScheduler().schedule(midletSuite)) {
            // The system is shutting down
            state.nextCommand = EXIT;
        }

        state.status = OK;
    }

    /**
     * Removes a MIDlet Suite.
     *
     * @param state command state containing the name of the suite to remove
     *
     * @exception IllegalArgumentException if storage name in the state is
     *    null
     */
    private static void remove(CommandState state) {
        state.status = ERROR;

        if (state.suiteStorageName == null) {
            throw new IllegalArgumentException("No suite specified");
        }
        
        if (state.suiteStorageName.equals("all")) {
            String suite[] = installer.list();
            for (int i = 0; i < suite.length; i++) {
                installer.remove(suite[i]);
            }
        } else {
            installer.remove(state.suiteStorageName);
        }

        state.status = OK;

        return;
    }

    /** This class is not meant to be instatiated */
    private CommandProcessor() {
    }
}
