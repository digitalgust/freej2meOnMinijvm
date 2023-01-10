/*
 * @(#)Manager.java	1.71 02/09/11 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.dev;

import javax.microedition.io.*;
import java.util.*;
import java.io.*;

import javax.microedition.midlet.*;

import javax.microedition.lcdui.*;

import javax.microedition.rms.*;

import com.sun.midp.lcdui.DisplayManagerFactory;
import com.sun.midp.lcdui.Resource;

import com.sun.midp.midlet.*;

import com.sun.midp.midletsuite.*;

import com.sun.midp.security.*;

import com.sun.midp.main.Configuration;

import javax.microedition.io.*;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.io.j2me.push.*;

/**
 * The Graphical MIDlet suite manager.
 * <p>
 * Starts with a selector that provides a list of MIDlet suites and
 * a set of commands to perform. It displays the MIDlet names for a suite
 * under the MIDlet suite name, except if there is only one suite then
 * instead of display the suite name, MIDlet-1 name and icon are used.
 * <p>
 * The commands are:</p>
 * <ul>
 * <li><b>Install</b>: Let the user install a suite from a list suites
 * obtained using an HTML URL given by the user. This list is derived by
 * extracting the links with hrefs that are in quotes and end with ".jad" from
 * the HTML page. An href in an extracted link is assumed to be an absolute
 * URL for a MIDP application descriptor.</li>
 * <li><b>Launch</b>: Launch the suite the user selected.
 * <li><b>Remove</b>: Remove the suite (with confirmation) the user selected.
 * </li>
 * <li><b>Update</b>: Update the suite the user selected.</li>
 * <li><b>Info</b>: Show the user general information of the selected suite.
 * <li><b>Settings</b>: Let the user change the manager's settings.
 * </ul>
 */
public class Manager extends MIDlet implements CommandListener {

    /** Translated small copyright string. */
    private static final String SMALL_COPYRIGHT = Resource.getString(
	"Copyright (c) 2000-2002 Sun Microsystems, Inc. All rights reserved.");
    /** Translated long copyright string. */
    private static final String COPYRIGHT = Resource.getString(
	"Copyright (c) 2000-2002 Sun Microsystems, Inc. All rights reserved.\n"
      + "Use is subject to license terms.\n"
      + "Third-party software, including font technology, is copyrighted "
      + "and licensed from Sun suppliers.  Sun, Sun Microsystems, the Sun "
      + "logo, J2ME, the Java Coffee Cup logo, and  Java are trademarks "
      + "or registered trademarks of Sun Microsystems, Inc. in the U.S. "
      + "and other countries.\n"
      + "Federal Acquisitions: Commercial Software - Government Users "
      + "Subject to Standard License Terms and Conditions."
      + "\n\n"  
      + "Copyright (c) 2002 Sun Microsystems, Inc. Tous droits réservés.\n"
      + "Distribué par des licences qui en restreignent l'utilisation.\n"
      + "Le logiciel détenu par des tiers, et qui comprend la technologie "
      + "relative aux polices de caractères, est protégé par un copyright "
      + "et licencié par des fournisseurs de Sun. Sun, Sun Microsystems, "
      + "le logo Sun, J2ME, le logo Java Coffee Cup, et Java sont des "
      + "marques de fabrique ou des marques déposées de Sun Microsystems, "
      + "Inc. aux Etats-Unis et dans d'autres pays.");
    /** Cache of the suite icon. */
    private static Image suiteIcon;
    /** Cache of the empty icon. */
    private static Image emptyIcon;
    /** Cache of the single suite icon. */
    private static Image singleSuiteIcon;
    /** Cache of the Java logo. */
    private static Image javaLogo;
    /** Cache of the home screen graphic. */
    private static Image homeScreenGraphic;
    /** So the static method can know if there is a color display. */
    static boolean colorDisplay;
    /** True until constructed for the first time. */
    private static boolean first = true;

    /** The installer that is being used to install or update a suite. */
    private Installer installer;
    /** List of all the MIDlet suites. */
    private List mlist;
    /** Display for this MIDlet. */
    private Display display;    
    /** Keeps track of when the display last changed, in millseconds. */
    private long lastDisplayChange;
    /** Number of midlets in minfo. */
    private int mcount;
    /** MIDlet suite information, class, name, icon; one per MIDlet suite. */
    private MIDletSuiteInfo[] minfo; 
    /** Currently selected suite. */
    private int selectedSuite;
    /** The application push permission setting. */
    private ChoiceGroup pushChoice;
    /** The application network permission setting. */
    private ChoiceGroup netChoice;
    /** The application network server permission setting. */
    private ChoiceGroup serverChoice;
    /** The application comm port permission setting. */
    private ChoiceGroup commChoice;
    /** Command object for "Apps" command for home screen. */
    private Command appsCmd = new Command(Resource.getString("Apps"), 
                                           Command.SCREEN, 1);
    /** Command object for "Launch". */
    private Command launchCmd = new Command(Resource.getString("Launch"), 
                                           Command.ITEM, 1);
    /** Command object for "Info". */
    private Command infoCmd = new Command(Resource.getString("Info"), 
                                           Command.ITEM, 2);
    /** Command object for "Remove". */
    private Command removeCmd = new Command(Resource.getString("Remove"), 
                                            Command.ITEM, 3);
    /** Command object for "Update". */
    private Command updateCmd = new Command(Resource.getString("Update"), 
                                           Command.ITEM, 4);
    /** Command object for "Application settings". */
    private Command appSettingsCmd = new Command(Resource.getString(
                                      "Application Settings"), 
                                           Command.ITEM, 5);
    /** Command object for "About". */
    private Command aboutCmd = new Command(Resource.getString("About"), 
                                           Command.HELP, 1);
    /** Command object for "OK" command for application settings form. */
    private Command saveAppSettingsCmd = new Command(
                                           Resource.getString("Save"), 
                                           Command.OK, 1);
    /** Command object for "Back" command for back to list. */
    private Command backCmd = new Command(Resource.getString("Back"), 
                                           Command.BACK, 1);
    /** Command object for "Cancel" command for the remove form. */
    private Command cancelCmd = new Command(Resource.getString("Cancel"), 
                                           Command.CANCEL, 1);
    /** Command object for "Remove" command for the remove form. */
    private Command removeOkCmd = new Command(Resource.getString("Remove"), 
                                           Command.SCREEN, 1);

    /**
     * Gets the MIDlet suite icon from storage.
     *
     * @return icon image
     */
    private static Image getSuiteIcon() {
        if (suiteIcon != null) {
            return suiteIcon;
        }

        suiteIcon = GraphicalInstaller.getIconFromStorage(
                        (colorDisplay ? "_suite_8.png" : "_suite_2.png"));
        return suiteIcon;
    }

    /**
     * Gets the empty icon from storage.
     *
     * @return icon image
     */
    private static Image getEmptyIcon() {
        if (emptyIcon != null) {
            return emptyIcon;
        }

        emptyIcon = GraphicalInstaller.getIconFromStorage("_empty.png");
        return emptyIcon;
    }

    /**
     * Gets the single MIDlet suite icon from storage.
     *
     * @return icon image
     */
    private static Image getSingleSuiteIcon() {
        if (singleSuiteIcon != null) {
            return singleSuiteIcon;
        }

        singleSuiteIcon = GraphicalInstaller.getIconFromStorage(
                        (colorDisplay ? "_single8.png" : "_single2.png"));
        return singleSuiteIcon;
    }

    /**
     * Gets the Java logo image from storage.
     *
     * @return icon image
     */
    private static Image getJavaLogo() {
        if (javaLogo != null) {
            return javaLogo;
        }

        javaLogo = GraphicalInstaller.getIconFromStorage(
                       (colorDisplay ? "_logo_8.png" : "_logo_2.png"));
        return javaLogo;
    }

    /**
     * Gets the home screen graphic from storage.
     *
     * @return icon image
     */
    private static Image getHomeScreenGraphic() {
        if (homeScreenGraphic != null) {
            return homeScreenGraphic;
        }

        homeScreenGraphic = GraphicalInstaller.getIconFromStorage(
                       (colorDisplay ? "_home_8.png" : "_home_4.png"));
        return homeScreenGraphic;
    }

    /**
     * Create and initialize a new Manager MIDlet.
     */
    public Manager() {
        installer = Installer.getInstaller();
        display = Display.getDisplay(this);
        colorDisplay = display.isColor();
        mcount = 0;
        minfo = new MIDletSuiteInfo[20];
        readMIDletSuiteInfo();
        first = getAppProperty("logo-displayed").equals("F");
        String runMessage;

        GraphicalInstaller.initSettings();

        setupList();

        if (mcount > 0) {
            mlist.addCommand(infoCmd);
            mlist.addCommand(removeCmd);
            mlist.addCommand(updateCmd);
            mlist.addCommand(appSettingsCmd);
        }

        mlist.addCommand(launchCmd);
        mlist.addCommand(backCmd);
        mlist.addCommand(aboutCmd);
        mlist.setCommandListener(this); // Listen for the selection

        runMessage = getAppProperty("run-message");
        if (runMessage != null) {
            Alert error = new Alert(null);

            error.setString(runMessage);
            display.setCurrent(error, mlist);
        } else if (first) {
            first = false;
            displayHomeScreen();
        } else {
            display.setCurrent(mlist);
        }
    }

    /**
     * Start puts up a List of the MIDlets found in the descriptor file.
     */
    public void startApp() {
    }

    /**
     * Pause; there are no resources that need to be released.
     */
    public void pauseApp() {
    }

    /**
     * Destroy cleans up.
     *
     * @param unconditional is ignored; this object always
     * destroys itself when requested.
     */
    public void destroyApp(boolean unconditional) {
        resetSettings();
    }

    /**
     * Save user settings such as currently selected midlet
     */
    private void resetSettings() {
        GraphicalInstaller.saveSettings(null, "");
    }

    /** Display the home screen. */
    private void displayHomeScreen() {
        Form home = new Form("");

        mlist.setSelectedIndex(0, true);

        home.append(
            new ImageItem(null, getJavaLogo(),
                          ImageItem.LAYOUT_NEWLINE_BEFORE +
                          ImageItem.LAYOUT_CENTER +
                          ImageItem.LAYOUT_NEWLINE_AFTER, null));
        home.append(
            new ImageItem(null, getHomeScreenGraphic(),
                          ImageItem.LAYOUT_NEWLINE_BEFORE +
                          ImageItem.LAYOUT_CENTER +
                          ImageItem.LAYOUT_NEWLINE_AFTER, null));
        home.addCommand(appsCmd);
        home.setCommandListener(this);

        display.setCurrent(home);
    }

    /**
     * Read in and create a MIDletInfo for each MIDlet suite.
     */
    private void readMIDletSuiteInfo() {
        String[] suiteNames;
        MIDletSuite midletSuite;
        int numberOfMidlets;
        String attr;

        suiteNames = installer.list();

        for (int i = 0; i < suiteNames.length; i++) {

            int lowest = i;

            for (int k = i + 1; k < suiteNames.length; k++) {
                if (suiteNames[k].compareTo(suiteNames[lowest]) < 0) {
                    lowest = k;
                }
            }

            try {
                midletSuite = installer.getMIDletSuite(suiteNames[lowest]);
                numberOfMidlets = midletSuite.getNumberOfMIDlets();

                if (numberOfMidlets == 1) {
                    attr = midletSuite.getProperty("MIDlet-1");
                    addMIDletSuite(new MIDletSuiteInfo(suiteNames[lowest],
                                               midletSuite, attr));
                    minfo[mcount - 1].singleMidlet = true;
                    minfo[mcount - 1].icon = getSingleSuiteIcon();
                } else {
                    addMIDletSuite(new MIDletSuiteInfo(suiteNames[lowest],
                                                       midletSuite));
                } 

            } catch (Exception e) {
                // move on to the next suite
            }

            suiteNames[lowest] = suiteNames[i];
        }
    }

    /**
     * Add a MIDlet suite to the list.
     * @param info MIDlet suite information to add to MIDlet
     */
    private void addMIDletSuite(MIDletSuiteInfo info) {
        if (mcount >= minfo.length) {
            MIDletSuiteInfo[] n = new MIDletSuiteInfo[mcount+4];
            System.arraycopy(minfo, 0, n, 0, mcount);
            minfo = n;
        }

        minfo[mcount++] = info;
    }

    /**
     * Open the settings database and retreive the currently selected midlet
     *
     * @return the storagename of the midlet that shoul be hilighted. this
     *          may be null.
     */
    private String getSelectedMIDlet() {
        ByteArrayInputStream bas;
        DataInputStream dis;
        byte[] data;
        RecordStore settings = null;
        String ret = null;
	
        try {

            settings = RecordStore.
                       openRecordStore(GraphicalInstaller.SETTINGS_STORE, 
                                       false);

            /** we should be guaranteed that this is always the case! */
            if (settings.getNumRecords() > 0) {

                data = settings.getRecord(
                           GraphicalInstaller.SELECTED_MIDLET_RECORD_ID);

                if (data != null) {
                    bas = new ByteArrayInputStream(data);
                    dis = new DataInputStream(bas);
                    ret = dis.readUTF();
                }
            }

        } catch (RecordStoreException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        } finally {
            if (settings != null) {
                try {
                    settings.closeRecordStore();
                } catch (RecordStoreException e) {
                    // ignore
                }
            }
        }

        return ret;
    }

    /**
     * Read the set of MIDlet names, icons and classes
     * Fill in the list.
     */
    private void setupList() {
        if (mlist == null) {
            mlist = new List(Resource.getString("Applications"), 
                             Choice.IMPLICIT);

            // Add the installer midlet first
            mlist.append(" " + Resource.getString("Install Application"),
                      getSingleSuiteIcon());
            
            String curMIDlet = getSelectedMIDlet();

            // Add each midlet
            for (int i = 0; i < mcount; i++) {
                mlist.append(" " + minfo[i].displayName, minfo[i].icon);

                if (minfo[i].storageName.equals(curMIDlet)) {
                    // plus one because of the "Install App" option
                    mlist.setSelectedIndex(i + 1, true);
                }
            }
        }
    }
        
    /**
     * Respond to a command issued on any Screen.
     *
     * @param c command activiated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == aboutCmd) {
            Alert a = new Alert(null);
                
            a.setImage(getJavaLogo());
            a.setString(COPYRIGHT);
            a.setTimeout(Alert.FOREVER);

            display.setCurrent(a, mlist);
            return;
        }

        if (s == mlist) {
            if (c == backCmd) {
                displayHomeScreen();
                return;
            }

            // save the selected suite for the removeOk command
            selectedSuite = mlist.getSelectedIndex();

            // The first suite is built-in suite
            selectedSuite--;

            if (c == List.SELECT_COMMAND || c == launchCmd) {


                if (selectedSuite == -1) {
                    // The built-in Install Application MIDlet was selected.
                    installSuite();
                    return;
                }

                launchSuite(minfo[selectedSuite]);
                return;
            }

            if (selectedSuite == -1) {
                // The built-in Install Application MIDlet was selected.
                displayInstallAppWarning();
                return;
            }

            if (c == infoCmd) {
                displaySuiteInfo(minfo[selectedSuite]);
                return;
            }

            if (c == removeCmd) {
                confirmRemove(minfo[selectedSuite]);
                return;
            }

            if (c == updateCmd) {
                updateSuite(minfo[selectedSuite]);
                return;
            }

            if (c == appSettingsCmd) {
                getApplicationSettings(minfo[selectedSuite]);
                return;
            }
        }

        if (c == removeOkCmd) {
            removeSuite(minfo[selectedSuite]);
            return;
        }

        if (c == saveAppSettingsCmd) {
            saveApplicationSettings(minfo[selectedSuite]);
            return;
        }

        if (c == appsCmd || c == backCmd || c == cancelCmd) {
            // goto back to the main list of suites
            display.setCurrent(mlist);
            return;
        }
    }

    /**
     * Lauches a suite.
     *
     * @param suiteInfo information for suite to launch
     */
    private void launchSuite(MIDletSuiteInfo suiteInfo) {
        try {
            // Create an instance of the MIDlet class
            // All other initialization happens in MIDlet constructor
            if (installer.execute(suiteInfo.storageName, null)) {
                /*
                 * Give the new MIDlet the screen by destroy our self,
                 * because we are running in a limited VM and must
                 * restart the VM let the select suite run.
                 */
                destroyApp(false);
                notifyDestroyed();
            } else {
                // Give the new MIDlet the screen by pausing and
                // asking to be resumed.
                notifyPaused();
                resumeRequest();
            }
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(suiteInfo.displayName);
            sb.append("\n");
            sb.append(Resource.getString("Error"));
            sb.append(ex.toString());

            Alert a = new Alert(Resource.getString("Cannot start: "), 
                                sb.toString(), null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            display.setCurrent(a, mlist);
        }
    }

    /**
     * Display the information for a suite.
     *
     * @param suiteInfo information for suite to display
     */
    private void displaySuiteInfo(MIDletSuiteInfo suiteInfo) {
        Form infoForm;
        String name;
        Image icon;
        StringBuffer label = new StringBuffer(40);
        StringBuffer value = new StringBuffer(40);
        Item item;
        String temp;

        try {
            infoForm = new Form(null);

            if (suiteInfo.singleMidlet) {
                name = suiteInfo.displayName;
                icon = suiteInfo.icon;
            } else {
                name = suiteInfo.midletSuite.getProperty(
                          Installer.SUITE_NAME_PROP);
                icon = getSuiteIcon();
            }

            label.append(Resource.getString("Info"));
            label.append(": ");
            label.append(name);
            infoForm.setTitle(label.toString());

            infoForm.append(
                new ImageItem(null, icon, ImageItem.LAYOUT_NEWLINE_BEFORE +
                              ImageItem.LAYOUT_CENTER +
                              ImageItem.LAYOUT_NEWLINE_AFTER, null));

            // round up the size to a Kilobyte
            label.setLength(0);
            label.append(Resource.getString("Size"));
            label.append(": ");
            value.append(
                Integer.toString((suiteInfo.midletSuite.getStorageUsed() +
                    1023) / 1024));
            value.append(" K");
            item = new StringItem(label.toString(), value.toString());
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            label.setLength(0);
            label.append(Resource.getString("Version"));
            label.append(": ");
            item = new StringItem(label.toString(),
                suiteInfo.midletSuite.getProperty(Installer.VERSION_PROP));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            label.setLength(0);

            if (suiteInfo.midletSuite.isTrusted()) {
                temp = "Authorized Vendor";
            } else {
                temp = "Vendor";
            }

            label.append(Resource.getString(temp));
            label.append(": ");
            item = new StringItem(label.toString(),
                suiteInfo.midletSuite.getProperty(Installer.VENDOR_PROP));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            temp = suiteInfo.midletSuite.getProperty(Installer.DESC_PROP);
            if (temp != null) {
                label.setLength(0);
                label.append(Resource.getString("Description"));
                label.append(": ");
                item = new StringItem(label.toString(), temp);
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                infoForm.append(item);
            }

            if (!suiteInfo.singleMidlet) {
                label.setLength(0);
                label.append(Resource.getString("Contents"));
                label.append(":");
                item = new StringItem(label.toString(), "");
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                infoForm.append(item);
                appendMIDletsToForm(suiteInfo.midletSuite, infoForm);
            }

            label.setLength(0);
            label.append(Resource.getString("Website"));
            label.append(": ");
            item = new StringItem(label.toString(),
                                  suiteInfo.midletSuite.getDownloadUrl());
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);


            label.setLength(0);
            label.append(Resource.getString("Advanced"));
            label.append(": ");
            item = new StringItem(label.toString(), "");
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            if (suiteInfo.midletSuite.isTrusted()) {
                infoForm.append(new ImageItem(null,
                    DisplayManagerFactory.getDisplayManager().
                        getTrustedMIDletIcon(), ImageItem.LAYOUT_DEFAULT,
                        null));
                temp = "Trusted";
            } else {
                temp = "Untrusted";
            }

            item = new StringItem(null, Resource.getString(temp));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            temp = suiteInfo.midletSuite.getCA();
            if (temp != null) {
                label.setLength(0);
                label.append(Resource.getString("Authorized by"));
                label.append(": ");
                item = new StringItem(label.toString(), temp);
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                infoForm.append(item);
            }

            temp = PushRegistryImpl.listConnections(
                       suiteInfo.midletSuite.getStorageName(), false);
            if (temp != null) {
                label.setLength(0);
                label.append(Resource.getString("Auto start connections"));
                label.append(": ");
                item = new StringItem(label.toString(), temp);
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                infoForm.append(item);
            }
        } catch (Exception ex) {
            value.setLength(0);
            value.append(suiteInfo.displayName);
            value.append("\n");
            value.append(Resource.getString("Exception"));
            value.append(": ");
            value.append(ex.toString());

            Alert a = new Alert(Resource.getString("Cannot access: "), 
                                value.toString(), null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            display.setCurrent(a, mlist);
            return;
        }

        infoForm.addCommand(backCmd);
        infoForm.setCommandListener(this);
        display.setCurrent(infoForm);
    }

    /**
     * Get the settings for an application.
     *
     * @param suiteInfo information for suite to display
     */
    private void getApplicationSettings(MIDletSuiteInfo suiteInfo) {
        MIDletSuite midletSuite = suiteInfo.midletSuite;
        Form form;
        String name;
        byte[][] ApiPermissions = suiteInfo.getPermissions();
        byte[] maxLevels = ApiPermissions[Permissions.MAX_LEVELS];
        byte[] curLevels = ApiPermissions[Permissions.CUR_LEVELS];
        int maxLevel;
        int permission;
        String[] values = new String[1];

        try {
            form = new Form(null);

            // A push interrupt may have changed the settings.
            suiteInfo.reloadSuite(installer);

            if (suiteInfo.singleMidlet) {
                name = suiteInfo.displayName;
            } else {
                name = suiteInfo.midletSuite.getProperty(
                          Installer.SUITE_NAME_PROP);
            }

            values[0] = name;
            form.setTitle(Resource.getString("Settings for %1:", values));

            pushChoice = newSettingChoice(form,
                             "Can %1 interrupt another application to " +
                             "receive information? The interrupted " +
                             "application will exit.",
                             Permissions.BLANKET,
                             suiteInfo.getPushInterruptSetting(), name);

            /*
             * Get the best user permission level for the group,
             * so when the user saves all of the permissions will
             * have that level.
             */
            maxLevel = maxLevels[Permissions.HTTP];
            permission = curLevels[Permissions.HTTP];
            maxLevel = selectBestUserPermissionLevel(maxLevel,
                       maxLevels[Permissions.HTTPS]);
            permission = selectBestUserPermissionLevel(permission,
                         curLevels[Permissions.HTTPS]);
            maxLevel = selectBestUserPermissionLevel(maxLevel,
                       maxLevels[Permissions.SSL]);
            permission = selectBestUserPermissionLevel(permission,
                         curLevels[Permissions.SSL]);
            maxLevel = selectBestUserPermissionLevel(maxLevel,
                       maxLevels[Permissions.TCP]);
            permission = selectBestUserPermissionLevel(permission,
                         curLevels[Permissions.TCP]);
            maxLevel = selectBestUserPermissionLevel(maxLevel,
                       maxLevels[Permissions.UDP]);
            permission = selectBestUserPermissionLevel(permission,
                         curLevels[Permissions.UDP]);

            netChoice = newSettingChoice(form,
                            "Can %1 use airtime to SEND information? " +
                            "This may cost you money.", maxLevel,
                            permission, name);

            /*
             * Get the best user permission level for the group,
             * so when the user saves all of the permissions will
             * have that level.
             */
            maxLevel = maxLevels[Permissions.TCP_SERVER];
            permission = curLevels[Permissions.TCP_SERVER];
            maxLevel = selectBestUserPermissionLevel(maxLevel,
                       maxLevels[Permissions.UDP_SERVER]);
            permission = selectBestUserPermissionLevel(permission,
                         curLevels[Permissions.UDP_SERVER]);

            serverChoice = newSettingChoice(form,
                            "Can %1 use airtime to RECEIVE information? " +
                            "This may cost you money.", maxLevel,
                            permission, name);
            
            commChoice = newSettingChoice(form,
                         "Can %1 directly connect to a computer to " +
                         "exchange information? This may require a " +
                         "special cable.", maxLevels[Permissions.COMM],
                         curLevels[Permissions.COMM], name);
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(suiteInfo.displayName);
            sb.append("\n");
            sb.append(Resource.getString("Exception"));
            sb.append(": ");
            sb.append(ex.toString());

            Alert a = new Alert(Resource.getString("Cannot access: "), 
                                sb.toString(), null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            display.setCurrent(a, mlist);
            return;
        }

        form.addCommand(saveAppSettingsCmd);
        form.addCommand(backCmd);
        form.setCommandListener(this);
        display.setCurrent(form);
    }

    /**
     * Select the best user permission level of either the current
     * permission or the next permission. Return current if neither
     * permission is a user level.
     *
     * @param current current permission level
     * @param next next permission level
     *
     * @return best user level or the current level
     */
    private int selectBestUserPermissionLevel(int current, int next) {
        if (current == Permissions.BLANKET_GRANTED ||
                current == Permissions.BLANKET) {
            return current;
        }

        if (next == Permissions.BLANKET_GRANTED ||
                next == Permissions.BLANKET) {
            return next;
        }

        if (current == Permissions.SESSION ||
                current == Permissions.DENY_SESSION) {
            return current;
        }

        if (next == Permissions.SESSION ||
                next == Permissions.DENY_SESSION) {
            return next;
        }

        if (current == Permissions.DENY ||
                current == Permissions.USER_DENIED) {
            return current;
        }

        if (next == Permissions.DENY ||
                next == Permissions.USER_DENIED) {
            return next;
        }

        return current;
    }
        
    /**
     * Creates a new choice group in a form if it is user settable,
     * with the 3 preset choices and a initial one set.
     * 
     * @param form Form to put the choice in
     * @param question label for the choice, will be translated
     * @param maxLevel maximum permission level
     * @param level current permission level
     * @param name name of suite
     *
     * @return choice to put in the application settings form,
     *           or null if initValue is -1
     */
    private ChoiceGroup newSettingChoice(Form form, String question,
                                         int maxLevel, int level,
                                         String name) {
        String[] values = {name};
        int initValue;
        ChoiceGroup choice;
        
        switch (level) {
        case Permissions.BLANKET_GRANTED:
        case Permissions.BLANKET:
            initValue = 0;
            break;

        case Permissions.ONE_SHOT:
        case Permissions.SESSION:
        case Permissions.DENY_SESSION:
            initValue = 1;
            break;

        case Permissions.DENY:
        case Permissions.USER_DENIED:
            initValue = 2;
            break;

        default:
            return null;
        }

        choice = new ChoiceGroup(Resource.getString(question, values),
                     Choice.EXCLUSIVE);

        if (maxLevel == Permissions.SESSION ||
                maxLevel == Permissions.ONE_SHOT) {
            initValue--;
        } else {
            choice.append(Resource.getString("Yes, always. Don't ask again."),
                          null);
        }

        choice.append(Resource.getString("Maybe. Ask me each time."), null);
        choice.append(Resource.getString("No. Shutoff %1.", values), null);

        choice.setSelectedIndex(initValue, true);

        choice.setPreferredSize(form.getWidth(), -1);

        form.append(choice);

        return choice;
    }

    /**
     * Save the application settings the user entered.
     *
     * @param suiteInfo information for suite to save the settings for
     */
    private void saveApplicationSettings(MIDletSuiteInfo suiteInfo) {
        MIDletSuite midletSuite = suiteInfo.midletSuite;
        byte[][] ApiPermissions = suiteInfo.getPermissions();
        byte[] maxLevels = ApiPermissions[Permissions.MAX_LEVELS];
        byte[] curLevels = ApiPermissions[Permissions.CUR_LEVELS];
        byte interruptSetting = (byte)getNewPermissionLevel(
                                pushChoice, Permissions.BLANKET,
                                suiteInfo.getPushInterruptSetting());

        suiteInfo.setPushInterruptSetting(interruptSetting);

        curLevels[Permissions.PUSH] = getNewPermissionLevel(pushChoice,
                                      maxLevels[Permissions.PUSH],
                                      curLevels[Permissions.PUSH]);

        curLevels[Permissions.HTTP] = getNewPermissionLevel(netChoice,
                                      maxLevels[Permissions.HTTP],
                                      curLevels[Permissions.HTTP]);
        curLevels[Permissions.HTTPS] = getNewPermissionLevel(netChoice,
                                       maxLevels[Permissions.HTTPS],
                                       curLevels[Permissions.HTTPS]);
        curLevels[Permissions.TCP] = getNewPermissionLevel(netChoice,
                                     maxLevels[Permissions.TCP],
                                     curLevels[Permissions.TCP]);
        curLevels[Permissions.SSL] = getNewPermissionLevel(netChoice,
                                     maxLevels[Permissions.SSL],
                                     curLevels[Permissions.SSL]);
        curLevels[Permissions.UDP] = getNewPermissionLevel(netChoice,
                                     maxLevels[Permissions.UDP],
                                     curLevels[Permissions.UDP]);

        curLevels[Permissions.TCP_SERVER] = getNewPermissionLevel(serverChoice,
                                            maxLevels[Permissions.TCP_SERVER],
                                            curLevels[Permissions.TCP_SERVER]);
        curLevels[Permissions.UDP_SERVER] = getNewPermissionLevel(serverChoice,
                                            maxLevels[Permissions.UDP_SERVER],
                                            curLevels[Permissions.UDP_SERVER]);

        curLevels[Permissions.COMM] = getNewPermissionLevel(commChoice,
                                      maxLevels[Permissions.COMM],
                                      curLevels[Permissions.COMM]);

        try {
            Installer.saveSuiteSettings(
                suiteInfo.midletSuite.getStorageRoot(),
                suiteInfo.getPushInterruptSetting(), ApiPermissions,
                suiteInfo.midletSuite.isTrusted());
            displaySuccessMessage(Resource.getString("Saved!"));
        } catch (Exception ex) {
            Alert a = new Alert(Resource.getString("Exception"), 
                                ex.toString(), null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            display.setCurrent(a, mlist);
        }
    }

    /**
     * Get the choice group index if any and convert it to a new
     * permission. To make this method re-usable, return
     * the current value if the max level is not a user level or
     * if the choice group is null. Also do not return a user level higher
     * than max level.
     *
     * @param choice choice group with the new permission level
     * @param maxLevel maximum level the permission can have
     * @param current current level the permission has
     *
     * @return new level
     */
    byte getNewPermissionLevel(ChoiceGroup choice, int maxLevel,
                               int current) {
        int selected;

        if (maxLevel == Permissions.NEVER ||
                maxLevel == Permissions.ALLOW ||
                choice == null) {
            return (byte)current;
        }

        selected = choice.getSelectedIndex();
        if (choice.size() == 2) {
            // we did not put in the blanket choice, so adjust selected index
            selected++;
        }
        
        switch (choice.getSelectedIndex()) {
        case 0:
            if (maxLevel == Permissions.SESSION) {
                return Permissions.ONE_SHOT;
            }

            if (maxLevel == Permissions.ONE_SHOT) {
                return Permissions.ONE_SHOT;
            }

            return Permissions.BLANKET_GRANTED;

        case 1:
            if (maxLevel == Permissions.ONE_SHOT) {
                return Permissions.ONE_SHOT;
            }

            return Permissions.SESSION;
        }

        return Permissions.USER_DENIED;
    }

    /**
     * Confirm the removal of a suite.
     *
     * @param suiteInfo information for suite to remove
     */
    private void confirmRemove(MIDletSuiteInfo suiteInfo) {
        Form confirmForm;
        StringBuffer temp = new StringBuffer(40);
        Item item;
        String extraConfirmMsg;
        String[] values = new String[1];

        try {
            confirmForm = new Form(null);

            confirmForm.setTitle(Resource.getString("Confirmation"));

            if (suiteInfo.singleMidlet) {
                values[0] = suiteInfo.displayName;
            } else {
                values[0] =
                    suiteInfo.midletSuite.getProperty(
                        Installer.SUITE_NAME_PROP);
            }

            item = new StringItem(null, Resource.getString(
                      "Are you sure you want to remove %1?", values));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            confirmForm.append(item);


            extraConfirmMsg = 
                suiteInfo.midletSuite.getProperty("MIDlet-delete-confirm");
            if (extraConfirmMsg != null) {
                temp.setLength(0);
                temp.append(" \n");
                temp.append(extraConfirmMsg);
                item = new StringItem(null, temp.toString());
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
            }

            if (!suiteInfo.singleMidlet) {
                temp.setLength(0);
                temp.append(Resource.getString("This suite contains"));
                temp.append(": ");
                item = new StringItem(temp.toString(), "");
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
                appendMIDletsToForm(suiteInfo.midletSuite, confirmForm);
            }

            temp.setLength(0);
            temp.append(" \n");
            temp.append(Resource.getString(
                "Once removed, %1 will have to be reinstalled.", values));
            item = new StringItem("", temp.toString());
            confirmForm.append(item);
        } catch (Exception ex) {
            temp.setLength(0);
            temp.append(suiteInfo.displayName);
            temp.append("\n");
            temp.append(Resource.getString("Exception"));
            temp.append(": ");
            temp.append(ex.toString());

            Alert a = new Alert(Resource.getString("Cannot access: "), 
                                temp.toString(), null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            display.setCurrent(a, mlist);
            return;
        }

        confirmForm.addCommand(cancelCmd);
        confirmForm.addCommand(removeOkCmd);
        confirmForm.setCommandListener(this);
        display.setCurrent(confirmForm);
    }

    /**
     * Appends a names of all the MIDlets in a suite to a Form, one per line.
     *
     * @param midletSuite suite of MIDlets
     * @param form form to append to
     */
    private void appendMIDletsToForm(MIDletSuite midletSuite, Form form) {
        int numberOfMidlets;
        MIDletInfo midletInfo;
        StringItem item;

        numberOfMidlets = midletSuite.getNumberOfMIDlets();
        for (int i = 1; i <= numberOfMidlets; i++) {
            midletInfo = new MIDletInfo(
                             midletSuite.getProperty("MIDlet-" + i));

            item = new StringItem(null, midletInfo.name);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            form.append(item);
        }
    }

    /**
     * Remove a suite.
     *
     * @param suiteInfo information for suite to remove
     */
    private void removeSuite(MIDletSuiteInfo suiteInfo) {
        installer.remove(suiteInfo.storageName);
        destroyApp(false);
        notifyDestroyed();
    }

    /**
     * Update a suite.
     *
     * @param suiteInfo information for suite to update
     */
    private void updateSuite(MIDletSuiteInfo suiteInfo) {
        Scheduler.getScheduler().getMIDletSuite().addProperty("storageName",
            suiteInfo.midletSuite.getStorageName());
        installSuite();
    }

    /** Discover and install a suite. */
    private void installSuite() {
        Scheduler.getScheduler().scheduleMIDlet(new GraphicalInstaller());
        destroyApp(false);
        notifyDestroyed();
    }

    /**
     * Alert the user that an action was successful.
     * @param successMessage message to display to user
     */
    private void displaySuccessMessage(String successMessage) {
        Image icon;
        Alert successAlert;

        icon = GraphicalInstaller.getIconFromStorage(
                       (colorDisplay ? "_dukeok8.png" : "_dukeok2.png"));

        successAlert = new Alert(null, successMessage, icon, null);

        successAlert.setTimeout(GraphicalInstaller.ALERT_TIMEOUT);

        // We need to prevent "flashing" on fast development platforms.
        while (System.currentTimeMillis() - lastDisplayChange <
               GraphicalInstaller.ALERT_TIMEOUT);

        display.setCurrent(successAlert, mlist);
        lastDisplayChange = System.currentTimeMillis();
    }

    /**
     * Display the standard warning for Install Application when any
     * command but launch is selected.
     */
    private void displayInstallAppWarning() {
        Alert a = new Alert(null, Resource.getString(
                  "This operation does not apply to Install Application " +
                  "because Install Application is part of the system on " +
                  "this device. Use Install Application to find and " +
                  "install applications."), null, AlertType.INFO);

        a.setTimeout(4000);
        display.setCurrent(a, mlist);
        return;
    }

    /**
     * Simple attribute storage for MIDlet suites
     */
    private class MIDletSuiteInfo {
        /** Midlet suite interface for displaying its info. */
        MIDletSuite midletSuite;
        /** Storage name of the MIDlet suite. */
        String storageName;
        /** Display name of the MIDlet suite. */
        String displayName;
        /** Name of the MIDlet to run. */
        String midletToRun;
        /** Icon for this suite. */
        Image icon;
        /** Is this single MIDlet MIDlet suite. */
        boolean singleMidlet = false;
        /** Holds the updated permissions. */
        private byte[][] permissions;
        /** Holds the updated push interrupt setting. */
        private byte pushInterruptSetting = -1;

        /**
         * Constructs a MIDletSuiteInfo object for a suite.
         *
         * @param theStorageName name the installer has for this suite
         * @param theMidletSuite MIDletSuite object for this suite
         */
        MIDletSuiteInfo(String theStorageName, MIDletSuite theMidletSuite) {
            midletSuite = theMidletSuite;
            displayName = midletSuite.getProperty(Installer.SUITE_NAME_PROP);
            if (displayName == null) {
                displayName = theStorageName;
            }

            icon = getSuiteIcon();
            storageName = theStorageName;
        }

        /**
         * Constructs a MIDletSuiteInfo object for a MIDlet of a suite
         *
         * @param theStorageName name the installer has for this suite
         * @param theMidletSuite MIDletSuite object for this suite
         * @param attr an MIDlet-<n> value for the MIDlet
         */
        MIDletSuiteInfo(String theStorageName, MIDletSuite theMidletSuite,
                        String attr) {
            this(theStorageName, theMidletSuite);

            // use the name from midlet info, if available
            if (attr != null) {
                MIDletInfo midletInfo = new MIDletInfo(attr);
                if (midletInfo.name != null) {
                    displayName = midletInfo.name;
                }
            }
        }

        /**
         * Reloads the suite.
         *
         * @param installer installer to load the midlet suite
         */
        public void reloadSuite(Installer installer) {
            midletSuite = installer.getMIDletSuite(
                            midletSuite.getStorageName());
            permissions = null;
            pushInterruptSetting = -1;
        }

        /**
         * Gets list of permission for this suite.
         *
         * @return permissions from {@link Permissions#}
         */
        public byte[][] getPermissions() {
            if (permissions == null) {
                permissions = midletSuite.getPermissions();
            }

            return permissions;
        }

        /**
         * Sets push setting for interrupting other MIDlets.
         * Reuses the Permissions.
         *
         * @param setting return push setting for interrupting MIDlets the
         *       value will be a permission level from {@link Permissions#}
         */
        public void setPushInterruptSetting(byte setting) {
            pushInterruptSetting = setting;
        }

        /**
         * Gets push setting for interrupting other MIDlets.
         * Reuses the Permissions.
         *
         * @return push setting for interrupting MIDlets the value
         *        will be a permission level from {@link Permissions#}
         */
        public byte getPushInterruptSetting() {
            if (pushInterruptSetting == -1) {
                pushInterruptSetting =
                    (byte)midletSuite.getPushInterruptSetting();
            }

            return pushInterruptSetting;
        }
    }
}
