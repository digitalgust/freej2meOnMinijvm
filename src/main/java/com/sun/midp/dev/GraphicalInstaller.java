/*
 * @(#)GraphicalInstaller.java	1.22 02/09/12 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.dev;

import java.io.*;

import java.util.*;

import javax.microedition.io.*;

import javax.microedition.lcdui.*;

import javax.microedition.midlet.*;

import javax.microedition.rms.*;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.lcdui.Resource;

import com.sun.midp.midlet.*;

import com.sun.midp.midletsuite.*;

import com.sun.midp.security.*;

/**
 * The Graphical MIDlet suite manager.
 * <p>
 * Let the user install a suite from a list of suites
 * obtained using an HTML URL given by the user. This list is derived by
 * extracting the links with hrefs that are in quotes and end with ".jad" from
 * the HTML page. An href in an extracted link is assumed to be an absolute
 * URL for a MIDP application descriptor.
 */
public class GraphicalInstaller extends MIDlet implements CommandListener {

    /** Standard timeout for alerts. */
    static final int ALERT_TIMEOUT = 1250;
    /** settings database */
    static final String SETTINGS_STORE = "settings";
    /** record id of selected midlet */
    static final int URL_RECORD_ID = 1;
    /** record is of the last installed midlet */
    static final int SELECTED_MIDLET_RECORD_ID = 2;


    /** So the static method can know if there is a color display. */
    static boolean colorDisplay;

    /** The installer that is being used to install or update a suite. */
    private Installer installer;
    /** Display for this MIDlet. */
    private Display display;    
    /** Contains the default URL for the install list. */
    private String defaultInstallListUrl = "http://";
    /** Contains the URL the user typed in. */
    private TextBox urlTextBox;
    /** Form obtain a password and a username. */
    private Form passwordForm;
    /** Contains the username for installing. */
    private TextField usernameField;
    /** Contains the password for installing. */
    private TextField passwordField;
    /** Background installer that holds state for the current install. */
    private BackgroundInstaller backgroundInstaller;
    /** Displays the progress of the install. */
    private Form progressForm;
    /** Gauge for progress form index. */
    private int progressGaugeIndex;
    /** URL for progress form index. */
    private int progressUrlIndex;
    /** Keeps track of when the display last changed, in millseconds. */
    private long lastDisplayChange;
    /** What to display to the user when the current action is cancelled. */
    private String cancelledMessage;
    /** What to display to the user when the current action is finishing. */
    private String finishingMessage;
    /** Displays a list of suites to install to the user. */
    private List installListBox;
    /** Contains a list of suites to install. */
    private Vector installList;

    /** Command object for "Back" command in the URL form. */
    private Command endCmd = new Command(Resource.getString("Back"), 
                                         Command.BACK, 1);
    /** Command object for URL screen to go and discover available suites. */
    private Command discoverCmd =
        new Command(Resource.getString("Go"), Command.SCREEN, 1);
    /** Command object for URL screen to save the URL for suites. */
    private Command saveCmd =
        new Command(Resource.getString("Save"), Command.SCREEN, 2);

    /** Command object for "Back" command in the suite list form. */
    private Command backCmd = new Command(Resource.getString("Back"), 
                                           Command.BACK, 1);
    /** Command object for "Install" command in the suite list form . */
    private Command installCmd = new Command(
        Resource.getString("Install"), Command.ITEM, 1);

    /** Command object for "Stop" command for progress form. */
    private Command stopCmd = new Command(Resource.getString("Stop"), 
                                           Command.STOP, 1);

    /** Command object for "Cancel" command for the confirm form. */
    private Command cancelCmd = new Command(
                                           Resource.getString("Cancel"), 
                                           Command.CANCEL, 1);
    /** Command object for "Install" command for the confirm download form. */
    private Command continueCmd =
        new Command(Resource.getString("Install"), Command.OK, 1);
    /** Command object for "Next" command for password form. */
    private Command nextCmd = new Command(Resource.getString("Next"), 
                                           Command.OK, 1);
    /** Command object for "continue" command for warning form. */
    private Command okCmd = new Command(Resource.getString("Continue"), 
                                           Command.OK, 1);
    /** Command object for "OK" command for exception form. */
    private Command exceptionCmd = new Command(Resource.getString("OK"), 
                                           Command.OK, 1);
    /** Command object for "Yes" command for keep RMS form. */
    private Command keepRMSCmd = new Command(Resource.getString("Yes"), 
                                           Command.OK, 1);
    /** Command object for "No" command for keep RMS form. */
    private Command removeRMSCmd = new Command(Resource.getString("No"), 
                                           Command.CANCEL, 1);

    /**
     * Gets an icon from storage.
     *
     * @param iconName name without a path
     * @return icon image
     */
    static Image getIconFromStorage(String iconName) {
        String iconFilename;
        RandomAccessStream stream;
        byte[] rawPng;

        iconFilename = File.getStorageRoot() + iconName;
        stream = new RandomAccessStream();
        try { 
            stream.connect(iconFilename, Connector.READ);
            rawPng = new byte[stream.getSizeOf()];
            stream.readBytes(rawPng, 0, rawPng.length);
            stream.disconnect();
            return Image.createImage(rawPng, 0, rawPng.length);
        } catch (java.io.IOException noImage) {
        }

        return null;
    }

    /**
     * Translate an InvalidJadException into a message for the user.
     *
     * @param exception exception to translate
     * @param name name of the MIDlet suite to insert into the message
     * @param vendor vendor of the MIDlet suite to insert into the message,
     *        can be null
     * @param version version of the MIDlet suite to insert into the message,
     *        can be null
     * @param jadUrl URL of a JAD, can be null
     *
     * @return message to display to the user
     */
    private static String translateJadException(
            InvalidJadException exception, String name, String vendor,
            String version, String jadUrl) {
        String[] values = {name, vendor, version, jadUrl,
                           exception.getExtraData()};
        String key;

        switch (exception.getReason()) {
        case InvalidJadException.OLD_VERSION:
            key = "An OLDER version of %1 has been found. " +
                  "Continuing will replace the version installed on your " +
                  "phone.\n\n" +
                  "Currently installed: %5\n" +
                  "Version found: %3\n" +
                  "Vendor: %2\n" +
                  "Website: %4\n\n" +
                  "Would you like to continue?";
            break;

        case InvalidJadException.ALREADY_INSTALLED:
            key = "This version of %1 is already installed on your phone. " +
                  "Continuing will replace the version installed on your " +
                  "phone.\n\n" +
                  "Currently installed: %5\n" +
                  "Version found: %3\n" +
                  "Vendor: %2\n" +
                  "Website: %4\n\n" +
                  "Would you like to continue?";
            break;

        case InvalidJadException.NEW_VERSION:
            key = "A NEWER version of %1 is has been found. " +
                  "Continuing will replace the version installed on your " +
                  "phone.\n\n" +
                  "Currently installed: %5\n" +
                  "Latest available: %3\n" +
                  "Vendor: %2\n" +
                  "Website: %4\n\n" +
                  "Would you like to continue?";
            break;

        case InvalidJadException.JAD_SERVER_NOT_FOUND:
        case InvalidJadException.JAD_NOT_FOUND:
        case InvalidJadException.INVALID_JAD_URL:
            key = "%1 cannot be found at this URL. " +
                  "Contact the application provider for more information.";
            break;

        case InvalidJadException.INVALID_JAD_TYPE:
            key = "The application file (.jad) for %1 does not appear to " +
                  "be the correct type. Contact the application provider " +
                  "for more information.";
            break;

        case InvalidJadException.MISSING_PROVIDER_CERT:
        case InvalidJadException.MISSING_SUITE_NAME:
        case InvalidJadException.MISSING_VENDOR:
        case InvalidJadException.MISSING_VERSION:
        case InvalidJadException.MISSING_JAR_URL:
        case InvalidJadException.MISSING_JAR_SIZE:
            key = "%1 cannot be installed because critical information is " +
                  "missing from the application file (.jad).";
            break;

        case InvalidJadException.MISSING_CONFIGURATION:
        case InvalidJadException.MISSING_PROFILE:
            key = "%1 cannot be installed because critical information is " +
                  "missing from the application file (.jar).";
            break;

        case InvalidJadException.INVALID_KEY:
        case InvalidJadException.INVALID_VALUE:
        case InvalidJadException.INVALID_VERSION:
        case InvalidJadException.PUSH_FORMAT_FAILURE:
        case InvalidJadException.PUSH_CLASS_FAILURE:
            key = "%1 cannot be installed because critical " + 
                  "information is not formatted correctly or is invalid. " +
                  "Contact your application provider to correct this " +
                  "situation.";
            break;

        case InvalidJadException.DEVICE_INCOMPATIBLE:
            key = "%1 is not designed to work with this device and cannot " +
                  "be installed.";
            break;

        case InvalidJadException.JAD_MOVED:
            key = "The new version of %1 is not from the same provider " +
                  "as the old version. The download URLs do not match. " +
                  "Do you want to install the new version?" +
                  "\n\nOld URL: %5\nNew URL: %4";
            break;

        case InvalidJadException.INSUFFICIENT_STORAGE:
            key = "There is not enough room to install %1 (%5K is needed " +
                  "for installation.). Try removing other items to free " +
                  "up space.";
            break;

        case InvalidJadException.JAR_SERVER_NOT_FOUND:
        case InvalidJadException.JAR_NOT_FOUND:
        case InvalidJadException.INVALID_JAR_URL:
            key = "The application file (.jar) for %1 cannot be found at " +
                  "its URL. Contact the application provider for more " +
                  "information.";
            break;

        case InvalidJadException.INVALID_JAR_TYPE:
            key = "The application file (.jar) for %1 does not appear to " +
                  "be the correct type. Contact the application provider " +
                  "for more information.";
            break;

        case InvalidJadException.SUITE_NAME_MISMATCH:
        case InvalidJadException.VERSION_MISMATCH:
        case InvalidJadException.VENDOR_MISMATCH:
        case InvalidJadException.JAR_SIZE_MISMATCH:
        case InvalidJadException.ATTRIBUTE_MISMATCH:
            key = "%1 cannot be installed because critical information " +
                  "between the website and the application file does not " +
                  "match.";
            break;

        case InvalidJadException.CORRUPT_JAR:
            key = "The application file (.jar) for %1 appears to be " +
                  "corrupt. Contact the application provider " +
                  "for more information.";
            break;

        case InvalidJadException.CANNOT_AUTH:
            key = "The website has requested to authenticate the user " +
                  "in way that this device does not support.";
            break;

        case InvalidJadException.CORRUPT_PROVIDER_CERT:
        case InvalidJadException.INVALID_PROVIDER_CERT:
        case InvalidJadException.CORRUPT_SIGNATURE:
        case InvalidJadException.INVALID_SIGNATURE:
        case InvalidJadException.UNSUPPORTED_CERT:
            key = "%1 cannnot be installed because the application files " +
                  "cannot verified. Contact your application " +
                  "provider to correct this situation.";
            break;

        case InvalidJadException.UNKNOWN_CA:
            key = "%1 cannot be installed. The system does recognize who " +
                  "is trying to authorize the application. Contact your " +
                  "service provider to correct this situation. \n\n%5";
            break;

        case InvalidJadException.EXPIRED_PROVIDER_CERT:
            key = "%1 cannot be installed. The trusted certificate used to " +
                  "authorize the application has expired. Contact your " +
                  "application provider to correct this situation.";
            break;

        case InvalidJadException.EXPIRED_CA_KEY:
            key = "%1 cannot be installed. The public key used for " +
                  "authorization has expired. Contact your service " +
                  "provider to correct this situation.";
            break;

        case InvalidJadException.AUTHORIZATION_FAILURE:
            key = "%1 cannot be installed because it does not have " +
                  "permission to the operation it requires.";
            break;

        case InvalidJadException.PUSH_DUP_FAILURE:
            key = "%1 cannot be installed. %1 is requires the use of a " +
                  "particular network resource to listen for network " +
                  "information. This resource is in use by another " + 
                  "application. Try removing the other application and " +
                  "re-installing.";
            break;

        case InvalidJadException.PUSH_PROTO_FAILURE:
            key = "%1 cannot be installed. %1 is requires the use of a " +
                  "particular network resource to listen for network " +
                  "information. This network resource is not supported on " +
                  "this device.";
            break;

        case InvalidJadException.TRUSTED_OVERWRITE_FAILURE:
            key = "The new version of %1, cannot be installed. " +
                  "The old version of %1 is authorized by %5. " +
                  "The new version is not authorized. " +
                  "Authorized applications cannot be replaced by " +
                  "unauthorized applications.";
            break;

        default:
            return exception.getMessage();
        }

        return Resource.getString(key, values);
    }

    /**
     * Create and initialize a new graphical installer MIDlet.
     * The Display is retreived and the list of MIDlet will be retrived or
     * update a currently installed suite.
     */
    GraphicalInstaller() {
        String storageName;

        installer = Installer.getInstaller();
        display = Display.getDisplay(this);
        colorDisplay = display.isColor();

        initSettings();
        restoreSettings();
        
        storageName = getAppProperty("storageName");
        if (storageName != null) {
            updateSuite(storageName);
            return;
        }

        // get the URL of a list of suites to install
        getUrl();
    }

    /**
     * Start.
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
        if (installer != null) {
            installer.stopInstalling();
        }

        /* The backgroundInstaller could be waiting for the user. */
        cancelBackgroundInstall();
    }

    /**
     * Respond to a command issued on any Screen.
     *
     * @param c command activiated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == discoverCmd) {
            // user wants to discover the suites that can be installed
            discoverSuitesToInstall(urlTextBox.getString());
        } else if (s == installListBox &&
                  (c == List.SELECT_COMMAND || c == installCmd)) {
            installSuite(installListBox.getSelectedIndex());
        } else if (c == backCmd) {
            display.setCurrent(urlTextBox);
        } else if (c == nextCmd) {
            // the user has entered a username and password
            resumeInstallWithPassword();
        } else if (c == okCmd) {
            resumeInstallAfterWarning();
        } else if (c == continueCmd) {
            startJarDownload();
        } else if (c == saveCmd) {
            saveURLSetting();
        } else if (c == keepRMSCmd) {
            setKeepRMSAnswer(true);
        } else if (c == removeRMSCmd) {
            setKeepRMSAnswer(false);
        } else if (c == stopCmd) {
            if (installer != null) {
                /*
                 * BackgroundInstaller may be displaying
                 * the "Finishing" message
                 *
                 * also we need to prevent the BackgroundInstaller from
                 * re-displaying the list before the cancelled message is
                 * displayed
                 */
                synchronized (this) {
                    if (installer.stopInstalling()) {
                        displayCancelledMessage(cancelledMessage);
                    }
                }
            } else {
                // goto back to the manager midlet
                notifyDestroyed();
            }
        } else if (c == cancelCmd) {
            displayCancelledMessage(cancelledMessage);
            cancelBackgroundInstall();
        } else if (c == endCmd || c == Alert.DISMISS_COMMAND) {
            // goto back to the manager midlet
            notifyDestroyed();
        }
    }

    /**
     * Get the settings the Manager saved for the user.
     */
    private void restoreSettings() {
        ByteArrayInputStream bas;
        DataInputStream dis;
        byte[] data;
        RecordStore settings = null;
        
        try {
            settings = RecordStore.openRecordStore(SETTINGS_STORE, false);

            data = settings.getRecord(1);
            if (data != null) {
                bas = new ByteArrayInputStream(data);
                dis = new DataInputStream(bas);
                defaultInstallListUrl = dis.readUTF();
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
    }

    /**
     * Initialize the settings database if it doesn't exist. This may create
     * two entries. The first will be for the download url, the second will
     * be for storing the storagename of the currently selected midlet
     */
    static void initSettings() {
        try {
            RecordStore settings = RecordStore.
                                   openRecordStore(SETTINGS_STORE, true);

            if (settings.getNumRecords() == 0) {
                // space for a URL
                settings.addRecord(null, 0, 0);

                // space for current MIDlet Suite name
                settings.addRecord(null, 0, 0);
            }

            settings.closeRecordStore();

        } catch (Exception e) {}
    }

   
    /**
     * Save the settings the user entered.
     *
     * @param url the url to save
     * @param curMidlet the storagename of the currently selected midlet
     * @return the Exception that may have been thrown, or null
     */
    static Exception saveSettings(String url, String curMidlet) {

        Exception ret = null;

        try {
            String temp;
            ByteArrayOutputStream bas;
            DataOutputStream dos;
            byte[] data;
            RecordStore settings;

            bas = new ByteArrayOutputStream();
            dos = new DataOutputStream(bas);
            settings = RecordStore.openRecordStore(SETTINGS_STORE, false);

            if (url != null) {
                dos.writeUTF(url);
                data = bas.toByteArray();
                settings.setRecord(URL_RECORD_ID, data, 0, data.length);
            }

            if (curMidlet != null) {
                bas.reset();

                dos.writeUTF(curMidlet);
                data = bas.toByteArray();
                settings.setRecord(SELECTED_MIDLET_RECORD_ID,
                                   data, 0, data.length);
            }

            settings.closeRecordStore();
            dos.close();
        } catch (Exception e) {
            ret = e;
        }

        return ret;
    }

    /**
     * Save the URL setting the user entered in to the urlTextBox.
     */
    private void saveURLSetting() {
        String temp;
        Exception ex;

        temp = urlTextBox.getString();

        ex = saveSettings(temp, null);
        if (ex != null) {
            displayException(Resource.getString("Exception"), ex.toString());
            return;
        }

        defaultInstallListUrl = temp;

        displaySuccessMessage(Resource.getString("Saved!"));
    }

    /**
     * Update a suite.
     *
     * @param storageName storage name of the suite to update
     */
    private void updateSuite(String storageName) {
        MIDletSuite midletSuite = installer.getMIDletSuite(storageName);
        MIDletInfo midletInfo;
        String name;

        if (midletSuite.getNumberOfMIDlets() == 1) {
            midletInfo = new MIDletInfo(midletSuite.getProperty("MIDlet-1"));
            name = midletInfo.name;
        } else {
            name = midletSuite.getProperty(Installer.SUITE_NAME_PROP);
        }

        cancelledMessage = Resource.getString("Update cancelled.");
        finishingMessage = Resource.getString("Finishing update.");
        installSuiteCommon("Updating", name,
            midletSuite.getDownloadUrl(),
            name + Resource.getString(" was successfully updated"),
            true);
    }

    /**
     * Let the user select a suite to install. The suites that are listed
     * are the links on a web page that end with .jad.
     *
     * @param url where to get the list of suites to install.
     */
    private void discoverSuitesToInstall(String url) {
        new Thread(new BackgroundInstallListGetter(this, url)).start();
    }

    /**
     * Install a suite.
     *
     * @param selectedSuite index into the installList
     */
    private void installSuite(int selectedSuite) {
        SuiteDownloadInfo suite;

        suite = (SuiteDownloadInfo)installList.elementAt(selectedSuite);
        cancelledMessage = Resource.getString("Installation cancelled.");
        finishingMessage = Resource.getString("Finishing installation.");
        installSuiteCommon("Installing", suite.label, suite.url,
            suite.label + Resource.getString(" was successfully installed"),
            false);
    }

    /**
     * Common helper method to install or update a suite.
     *
     * @param action action to put in the form's title
     * @param name name to in the form's title
     * @param url URL of a JAD
     * @param successMessage message to display to user upon success
     * @param updateFlag if true the current suite is being updated
     */
    private void installSuiteCommon(String action, String name, String url,
            String successMessage, boolean updateFlag) {
        try {
            createProgressForm(action, name, url, 0, "Connecting...");
            backgroundInstaller = new BackgroundInstaller(this, url, name,
                                      successMessage, updateFlag);
            new Thread(backgroundInstaller).start();
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();

            sb.append(name);
            sb.append("\n");
            sb.append(Resource.getString("Error"));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString("Cannot access: "), 
                            sb.toString());
        }
    }

    /**
     * Create and display the progress form to the user with the stop action.
     *
     * @param action action to put in the form's title
     * @param name name to in the form's title
     * @param url URL of a JAD
     * @param size 0 if unknown, else size of object to download in K bytes
     * @param gaugeLabel label for progress gauge
     */
    private void createProgressForm(String action, String name,
                                    String url, int size, String gaugeLabel) {
        Form installForm;

        // display the JAR progress form
        installForm = displayProgressForm(action, name, url, size,
                                            gaugeLabel);
        installForm.addCommand(stopCmd);
        installForm.setCommandListener(this);
    }

    /**
     * Display the connecting form to the user, let call set actions.
     *
     * @param action action to put in the form's title
     * @param name name to in the form's title
     * @param url URL of a JAD
     * @param size 0 if unknown, else size of object to download in K bytes
     * @param gaugeLabel label for progress gauge
     *
     * @return displayed form
     */
    private Form displayProgressForm(String action, String name,
            String url, int size, String gaugeLabel) {
        Gauge progressGauge;
        StringItem urlItem;

        progressForm = new Form(null);

        progressForm.setTitle(Resource.getString(action) + " " + name);

        if (size <= 0) {
            progressGauge = new Gauge(Resource.getString(gaugeLabel), 
				      false, Gauge.INDEFINITE,
				      Gauge.CONTINUOUS_RUNNING);
        } else {
            progressGauge = new Gauge(Resource.getString(gaugeLabel), 
				      false, size, 0);
        }

        progressGaugeIndex = progressForm.append(progressGauge);

        if (url == null) {
            urlItem = new StringItem("", "");
        } else {
            urlItem =
                new StringItem(Resource.getString("Website") + ": ", url);
        }

        progressUrlIndex = progressForm.append(urlItem);

        display.setCurrent(progressForm);
        lastDisplayChange = System.currentTimeMillis();

        return progressForm;
    }

    /** Cancel an install (if there is one) waiting for user input. */
    private void cancelBackgroundInstall() {
        if (backgroundInstaller != null) {
            backgroundInstaller.continueInstall = false;

            synchronized (backgroundInstaller) {
                backgroundInstaller.notify();
            }
        }
    }

    /**
     * Ask the user for the URL.
     */
    private void getUrl() {
        try {
            if (urlTextBox == null) {
                urlTextBox = new TextBox(Resource.getString(
                                         "Enter a website to Install From:"),
                                         defaultInstallListUrl, 1024,
                                         TextField.ANY);
                urlTextBox.addCommand(endCmd);
                urlTextBox.addCommand(saveCmd);
                urlTextBox.addCommand(discoverCmd);
                urlTextBox.setCommandListener(this);
            }

            display.setCurrent(urlTextBox);
        } catch (Exception ex) {
            displayException(Resource.getString("Exception"), 
                             ex.toString());
        }
    }

    /**
     * Update the status form.
     *
     * @param status current status of the install.
     * @param state current state of the install.
     */
    private void updateStatus(int status, InstallState state) {
        if (status == Installer.DOWNLOADING_JAD) {
            updateProgressForm("", 0,
               "Downloading the application description file...");
            return;
        }

        if (status == Installer.DOWNLOADING_JAR) {
            updateProgressForm(state.getJarUrl(), state.getJarSize(),
                               "Downloading the application file...");
            return;
        }

        if (status == Installer.DOWNLOADED_1K_OF_JAR &&
                state.getJarSize() > 0) {
            Gauge progressGauge = (Gauge)progressForm.get(progressGaugeIndex);
            progressGauge.setValue(progressGauge.getValue() + 1);
            return;
        }

        if (status == Installer.VERIFYING_SUITE) {
            updateProgressForm(null, 0, "Verifying the application...");
            return;
        }

        if (status == Installer.STORING_SUITE) {
            displaySuccessMessage(finishingMessage);
            return;
        }
    }

    /**
     * Update URL and gauge of the progress form.
     *
     * @param url new URL, null to remove, "" to not change
     * @param size 0 if unknown, else size of object to download in K bytes
     * @param gaugeLabel label for progress gauge
     */
    private void updateProgressForm(String url, int size, String gaugeLabel) {
        Gauge oldProgressGauge;
        Gauge progressGauge;
        StringItem urlItem;

        // We need to prevent "flashing" on fast development platforms.
        while (System.currentTimeMillis() - lastDisplayChange < ALERT_TIMEOUT);

        if (size <= 0) {
            progressGauge = new Gauge(Resource.getString(gaugeLabel), 
				      false, Gauge.INDEFINITE, 
				      Gauge.CONTINUOUS_RUNNING);
        } else {
            progressGauge = new Gauge(Resource.getString(gaugeLabel), 
				      false, size, 0);
        }

        oldProgressGauge = (Gauge)progressForm.get(progressGaugeIndex);
        progressForm.set(progressGaugeIndex, progressGauge);

        // this ends the background thread of gauge.
        oldProgressGauge.setValue(Gauge.CONTINUOUS_IDLE);

        if (url == null) {
            urlItem = new StringItem("", "");
            progressForm.set(progressUrlIndex, urlItem);
        } else if (url.length() != 0) {
            urlItem =
                new StringItem(Resource.getString("Website") + ": ", url);
            progressForm.set(progressUrlIndex, urlItem);
        }

        lastDisplayChange = System.currentTimeMillis();
    }

    /**
     * Give the user a chance to act on warning during an installation.
     *
     * @param name name of the MIDlet suite to insert into the message
     * @param vendor vendor of the MIDlet suite to insert into the message,
     *        can be null
     * @param version version of the MIDlet suite to insert into the message,
     *        can be null
     * @param jadUrl URL of a JAD, can be null
     * @param e last exception from the installer
     */
    private void warnUser(String name, String vendor, String version,
                          String jadUrl, InvalidJadException e) {
        Form warningForm;

        warningForm = new Form(null);
        warningForm.setTitle(Resource.getString("Warning"));
        warningForm.append(translateJadException(e, name, vendor, version,
                                                 jadUrl)); 
        warningForm.addCommand(cancelCmd);
        warningForm.addCommand(okCmd);
        warningForm.setCommandListener(this);
        display.setCurrent(warningForm);
    }

    /**
     * Resume the install after a the user overrides a warning.
     */
    private void resumeInstallAfterWarning() {
        // redisplay the progress form
        display.setCurrent(progressForm);

        backgroundInstaller.continueInstall = true;
        synchronized (backgroundInstaller) {
            backgroundInstaller.notify();
        }
    }

    /**
     * Ask for a username and password.
     */
    private void getUsernameAndPassword() {
        getUsernameAndPasswordCommon("");
    }

    /**
     * Ask for proxy username and password.
     */
    private void getProxyUsernameAndPassword() {
        getUsernameAndPasswordCommon("Firewall Authentication");
    }

    /**
     * Ask a username and password.
     *
     * @param title title of the password form
     */
    private void getUsernameAndPasswordCommon(String title) {
        if (passwordForm == null) {
            passwordForm = new Form(null);

            usernameField = new TextField(
                            Resource.getString("Please Enter ID"), null, 40,
                            TextField.ANY);
            passwordForm.append(usernameField);

            passwordField = new TextField(
                            Resource.getString("Password"), null, 40,
                            TextField.PASSWORD);
            passwordForm.append(passwordField);
            passwordForm.addCommand(cancelCmd);
            passwordForm.addCommand(nextCmd);
            passwordForm.setCommandListener(this);
        }

        passwordForm.setTitle(Resource.getString(title));
        passwordField.setString("");
        display.setCurrent(passwordForm);
    }

    /**
     * Resume the install of the suite with a password and username.
     */
    private void resumeInstallWithPassword() {
        String username;
        String password;


        username = usernameField.getString();
        password = passwordField.getString();
        if (username == null || username.length() == 0) {
            Alert a = new Alert(Resource.getString("Error"), 
                             Resource.getString(
                             "The ID has not been entered."),
                             null, AlertType.ERROR);
            a.setTimeout(ALERT_TIMEOUT);
            display.setCurrent(a, passwordForm);
            return;
        }

        if (password == null || password.length() == 0) {
            Alert a = new Alert(Resource.getString("Error"), 
                                Resource.getString(
                                "The password has not been entered."),
                                null, AlertType.ERROR);
            a.setTimeout(ALERT_TIMEOUT);
            display.setCurrent(a, passwordForm);
            return;
        }

        // redisplay the progress form
        display.setCurrent(progressForm);

        if (backgroundInstaller.proxyAuth) {
            backgroundInstaller.installState.setProxyUsername(username);
            backgroundInstaller.installState.setProxyPassword(password);
        } else {
            backgroundInstaller.installState.setUsername(username);
            backgroundInstaller.installState.setPassword(password);
        }

        backgroundInstaller.continueInstall = true;
        synchronized (backgroundInstaller) {
            backgroundInstaller.notify();
        }
    }

    /**
     * Confirm the JAR download with the user.
     *
     * @param state current state of the install.
     */
    private void displayDownloadConfirmation(InstallState state) {
        Form infoForm;
        StringItem item;
        String name;
        String desc;
        StringBuffer label = new StringBuffer(40);
        StringBuffer value = new StringBuffer(40);
        String[] values = new String[1];

        name = state.getAppProperty(Installer.SUITE_NAME_PROP);

        try {
            infoForm = new Form(null);

            infoForm.setTitle(Resource.getString("Confirmation"));

            values[0] = name;
            item = new StringItem(null, Resource.getString(
                "Are you sure you want to install \"%1\"?", values));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            // round up the size to a Kilobyte
            label.append(Resource.getString("Size"));
            label.append(": ");
            value.setLength(0);
            value.append(state.getJarSize());
            value.append(" K");
            item = new StringItem(label.toString(), value.toString());
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            label.setLength(0);
            label.append(Resource.getString("Version"));
            label.append(": ");
            value.setLength(0);
            item = new StringItem(label.toString(),
                       state.getAppProperty(Installer.VERSION_PROP));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            label.setLength(0);
            label.append(Resource.getString("Vendor"));
            label.append(": ");
            item = new StringItem(label.toString(),
                      state.getAppProperty(Installer.VENDOR_PROP));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            desc = state.getAppProperty(Installer.DESC_PROP);
            if (desc != null) {
                label.setLength(0);
                label.append(Resource.getString("Description"));
                label.append(": ");
                item = new StringItem(label.toString(), desc);
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                infoForm.append(item);
            }

            label.setLength(0);
            label.append(Resource.getString("Website"));
            label.append(": ");
            infoForm.append(new StringItem(label.toString(),
                                           state.getJarUrl()));

            infoForm.addCommand(continueCmd);
            infoForm.addCommand(cancelCmd);
            infoForm.setCommandListener(this);

            // We need to prevent "flashing" on fast development platforms.
            while (System.currentTimeMillis() - lastDisplayChange <
                   ALERT_TIMEOUT);

            display.setCurrent(infoForm);
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(name);
            sb.append("\n");
            sb.append(Resource.getString("Exception"));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString("Cannot access: "), 
                                sb.toString());
        }
    }

    /**
     * Ask the user during an update if they want to keep the old RMS data.
     *
     * @param state current state of the install.
     */
    private void displayKeepRMSForm(InstallState state) {
        Form infoForm;
        String name;
        String desc;
        StringBuffer label = new StringBuffer(40);
        StringBuffer value = new StringBuffer(40);
        String[] values = new String[1];

        name = state.getAppProperty(Installer.SUITE_NAME_PROP);

        try {
            infoForm = new Form(null);

            infoForm.setTitle(Resource.getString("Confirmation"));

            values[0] = name;
            value.append(Resource.getString("Do you want the new version " +
                "of %1 to be able to use the information stored by the " +
                "old version of %1?", values));
            infoForm.append(value.toString());

            infoForm.addCommand(keepRMSCmd);
            infoForm.addCommand(removeRMSCmd);
            infoForm.setCommandListener(this);

            // We need to prevent "flashing" on fast development platforms.
            while (System.currentTimeMillis() - lastDisplayChange <
                   ALERT_TIMEOUT);

            display.setCurrent(infoForm);
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(name);
            sb.append("\n");
            sb.append(Resource.getString("Exception"));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString("Cannot access: "), 
                                sb.toString());
        }
    }

    /**
     * Resume the install to start the JAR download.
     */
    private void startJarDownload() {
        updateProgressForm(backgroundInstaller.url, 0, "Connecting...");

        // redisplay the progress form
        display.setCurrent(progressForm);

        backgroundInstaller.continueInstall = true;
        synchronized (backgroundInstaller) {
            backgroundInstaller.notify();
        }
    }

    /** Confirm the JAR only download with the user. */
    private void displayJarOnlyDownloadConfirmation() {
        Form infoForm;
        StringItem item;
        StringBuffer label = new StringBuffer(40);
        StringBuffer value = new StringBuffer(40);
        String[] values = new String[1];

        try {
            infoForm = new Form(null);

            infoForm.setTitle(Resource.getString("Confirmation"));

            values[0] = backgroundInstaller.name;
            item = new StringItem(null, Resource.getString(
                       "Are you sure you want to install \"%1\"?", values));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            label.append(Resource.getString("Website"));
            label.append(": ");
            item = new StringItem(label.toString(), backgroundInstaller.url);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            infoForm.append(item);

            value.append(" \n");
            value.append(Resource.getString("There is no further " +
                "information available for the application."));
            infoForm.append(new StringItem(null, value.toString()));

            infoForm.addCommand(continueCmd);
            infoForm.addCommand(cancelCmd);
            infoForm.setCommandListener(this);

            // We need to prevent "flashing" on fast development platforms.
            while (System.currentTimeMillis() - lastDisplayChange <
                   ALERT_TIMEOUT);

            display.setCurrent(infoForm);
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer();
            
            sb.append(backgroundInstaller.name);
            sb.append("\n");
            sb.append(Resource.getString("Exception"));
            sb.append(": ");
            sb.append(ex.toString());
            displayException(Resource.getString("Cannot access: "), 
                             sb.toString());
        }
    }

    /**
     * Tell the background installer to keep the RMS data.
     *
     * @param keepRMS set to true to mean the user answered yes
     */
    private void setKeepRMSAnswer(boolean keepRMS) {
        // redisplay the progress form
        display.setCurrent(progressForm);

        // We need to prevent "flashing" on fast development platforms.
        while (System.currentTimeMillis() - lastDisplayChange <
               ALERT_TIMEOUT);

        backgroundInstaller.continueInstall = keepRMS;
        synchronized (backgroundInstaller) {
            backgroundInstaller.notify();
        }
    }

    /**
     * Alert the user that an action was successful.
     *
     * @param successMessage message to display to user
     */
    private void displaySuccessMessage(String successMessage) {
        Image icon;
        Alert successAlert;

        icon = getIconFromStorage(
                       (colorDisplay ? "_dukeok8.png" : "_dukeok2.png"));

        successAlert = new Alert(null, successMessage, icon, null);

        successAlert.setTimeout(ALERT_TIMEOUT);

        // We need to prevent "flashing" on fast development platforms.
        while (System.currentTimeMillis() - lastDisplayChange <
               ALERT_TIMEOUT);

        lastDisplayChange = System.currentTimeMillis();
        display.setCurrent(successAlert);
    }

    /**
     * Alert the user that an action was canceled. The backgroundInstaller
     * will hide the message.
     * @param message message to display to user
     */
    private void displayCancelledMessage(String message) {
        Form form;
        Image icon;

        form = new Form(null);

        icon = getIconFromStorage(
                       (colorDisplay ? "_ack_8.png" : "_ack_2.png"));
        form.append(new ImageItem(null, icon, ImageItem.LAYOUT_CENTER +
                                     ImageItem.LAYOUT_NEWLINE_BEFORE +
                                     ImageItem.LAYOUT_NEWLINE_AFTER, null));

        form.append(message);

        display.setCurrent(form);
        lastDisplayChange = System.currentTimeMillis();
    }

    /**
     * Display an exception to the user, with a done command.
     *
     * @param title exception form's title
     * @param message exception message
     */
    private void displayException(String title, String message) {
        Alert a = new Alert(title, message, null, AlertType.ERROR);

        a.setTimeout(Alert.FOREVER);
        a.setCommandListener(this);

        display.setCurrent(a);
    }

    /** A class to get the install list in a background thread. */
    private class BackgroundInstallListGetter implements Runnable {
        /** Parent installer. */
        private GraphicalInstaller parent;
        /** URL of the list. */
        private String url;

        /**
         * Construct a BackgroundInstallListGetter.
         *
         * @param theParent parent installer of this object
         * @param theUrl where to get the list of suites to install.
         */
        private BackgroundInstallListGetter(GraphicalInstaller theParent,
                                            String theUrl) {
            parent = theParent;
            url = theUrl;
        }

        /**
         * Get the list of suites for the user to install.
         * The suites that are listed
         * are the links on a web page that end with .jad.
         */
        public void run() {
            StreamConnection conn = null;
            InputStreamReader in = null;
            String errorMessage;
            long startTime;

            startTime = System.currentTimeMillis();

            try {
                parent.displayProgressForm("Getting Install List",
                                             "", url, 0, "Connecting...");
                conn = (StreamConnection)Connector.open(url, Connector.READ);
                in = new InputStreamReader(conn.openInputStream());
                try {
                    parent.updateProgressForm("", 0, "Downloading...");
                    parent.installList =
                        SuiteDownloadInfo.getDownloadInfoFromPage(in);
                    
                    if (parent.installList.size() > 0) {
                        parent.installListBox = new List(Resource.getString(
                                                "Select one to install:"),
                                                Choice.IMPLICIT);

                        // Add each suite
                        for (int i = 0; i < parent.installList.size(); i++) {
                            SuiteDownloadInfo suite =
                                (SuiteDownloadInfo)installList.elementAt(i);
                            parent.installListBox.append(suite.label,
                                                         (Image)null);
                        }

                        parent.installListBox.addCommand(backCmd);
                        parent.installListBox.addCommand(installCmd);
                        parent.installListBox.setCommandListener(parent);

                        /*
                         * We need to prevent "flashing" on fast development
                         * platforms. 
                         */
                        while (System.currentTimeMillis() -
                            parent.lastDisplayChange < 
                            parent.ALERT_TIMEOUT);

                        parent.display.setCurrent(parent.installListBox);
                        return;
                    }

                    errorMessage = Resource.getString(
                        "No MIDlet Suites found. " +
                        "Check the URL to make sure it is correct.");
                } catch (IllegalArgumentException ex) {
                    errorMessage = Resource.getString(
                        "The URL is not formatted correctly.");
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                }
            } catch (Exception ex) {
                errorMessage = Resource.getString(
                    "The connection failed. Please check the website " +
                    "URL and try again.");
            } finally {
                if (parent.progressForm != null) {
                    // end the background thread of progress gauge.
                    Gauge progressGauge = (Gauge)parent.progressForm.get(
                                          parent.progressGaugeIndex);
                    progressGauge.setValue(Gauge.CONTINUOUS_IDLE);
                }

                try {
                    conn.close();
                    in.close();
                } catch (Exception e) {
                    // ignore
                }
            }

            Alert a = new Alert(Resource.getString("Error"), 
                                errorMessage, null, AlertType.ERROR);
            a.setTimeout(Alert.FOREVER);
            parent.display.setCurrent(a, parent.urlTextBox);
        }
    }

    /** A class to install a suite in a background thread. */
    private class BackgroundInstaller implements Runnable, InstallListener {
        /** Parent installer. */
        private GraphicalInstaller parent;
        /** URL to install from. */
        private String url; 
        /** Name of MIDlet suite. */
        private String name;
        /**
         * Message for the user after the current install completes
         * successfully.
         */
        private String successMessage;
        /** Flag to update the current suite. */
        private boolean update;
        /** State of the install. */
        InstallState installState;
        /** Signals that the user wants the install to continue. */
        boolean continueInstall;
        /** Signals that the suite only has JAR, no JAD. */
        boolean jarOnly;
        /** Signals that a proxyAuth is needed. */
        boolean proxyAuth;

        /**
         * Construct a BackgroundInstaller.
         *
         * @param theParent parent installer of this object
         * @param theJadUrl where to get the JAD.
         * @param theName name of the MIDlet suite
         * @param theSuccessMessage message to display to user upon success
         * @param updateFlag if true the current suite should be
         *                      overwritten without asking the user.
         */
        private BackgroundInstaller(GraphicalInstaller theParent,
                String theJadUrl, String theName, String theSuccessMessage,
                boolean updateFlag) {
            parent = theParent;
            url = theJadUrl;
            name = theName;
            successMessage = theSuccessMessage;
            update = updateFlag;
        }

        /**
         * Run the installer.
         */
        public void run() {
            try {
                String lastInstalledMIDletName;

                if (jarOnly) {
                    lastInstalledMIDletName = 
                        parent.installer.installJar(url, false, false, this);
                } else {
                    lastInstalledMIDletName =  
                        parent.installer.installJad(url, false, false, this);
                }

                // Let the manager know what suite was installed
                GraphicalInstaller.saveSettings(null, lastInstalledMIDletName);

                parent.displaySuccessMessage(successMessage);

                /*
                 * We need to prevent "flashing" on fast development
                 * platforms.
                 */
                while ((System.currentTimeMillis() -
                        parent.lastDisplayChange) < parent.ALERT_TIMEOUT);

                parent.notifyDestroyed();
                return;
            } catch (Throwable ex) {
                String title;
                String msg;

                if (parent.installer != null &&
                        parent.installer.wasStopped()) {
                    displayListAfterCancelMessage();
                    return;
                }

                if (ex instanceof InvalidJadException) {
                    InvalidJadException ije = (InvalidJadException)ex;

                    if (ije.getReason() ==
                            InvalidJadException.INVALID_JAD_TYPE) {
                        // media type of JAD was wrong, it could be a JAR
                        String mediaType = (String)ije.getExtraData();

                        if (Installer.JAR_MT_1.equals(mediaType) ||
                                Installer.JAR_MT_2.equals(mediaType)) {
                            // re-run as a JAR only install
                            if (confirmJarOnlyDownload()) {
                                jarOnly = true;
                                installState = null;
                                run();
                                return;
                            }

                            displayListAfterCancelMessage();
                            return;
                        }
                    }

                    msg = translateJadException((InvalidJadException)ex,
                                                name, null, null, url);
                } else if (ex instanceof IOException) {
                    msg = Resource.getString(
                          "The connection dropped and the installation " +
                          "did not complete. Please try installing again.");
                } else {
                    msg = ex.getMessage();
                }

                title = Resource.getString("Install Error");
                if (parent.installListBox == null) {
                    // go back to the app list
                    displayException(title, msg); 
                    return;
                }

                Alert a = new Alert(title, msg, null, AlertType.ERROR);
                a.setTimeout(Alert.FOREVER);
                parent.display.setCurrent(a, parent.installListBox);
            } finally {
                if (parent.progressForm != null) {
                    // end the background thread of progress gauge.
                    Gauge progressGauge = (Gauge)parent.progressForm.get(
                                          parent.progressGaugeIndex);
                    progressGauge.setValue(Gauge.CONTINUOUS_IDLE);
                }
            }
        }

        /**
         * Called with the current state of the install so the user can be
         * asked to override the warning. Calls the parent to display the
         * warning to the user and then waits on the state object for
         * user's response.
         *
         * @param state current state of the install.
         *
         * @return true if the user wants to continue,
         *         false to stop the install
         */
        public boolean warnUser(InstallState state) {
            installState = state;

            InvalidJadException e = installState.getLastException();

            switch (e.getReason()) {
            case InvalidJadException.UNAUTHORIZED:
                proxyAuth = false;
                parent.getUsernameAndPassword();
                break;

            case InvalidJadException.PROXY_AUTH:
                proxyAuth = true;
                parent.getProxyUsernameAndPassword();
                break;

            case InvalidJadException.OLD_VERSION:
            case InvalidJadException.ALREADY_INSTALLED:
            case InvalidJadException.NEW_VERSION:
                // this is now an update
                update = true;

                // fall through
            default:
                parent.warnUser(name,
                    state.getAppProperty(Installer.VENDOR_PROP),
                    state.getAppProperty(Installer.VERSION_PROP),
                    url, e);
            }

            return waitForUser();
        }

        /**
         * Called with the current state of the install so the user can be
         * asked to confirm the jar download.
         * If false is returned, the an I/O exception thrown and
         * {@link Installer#wasStopped()} will return true if called.
         *
         * @param state current state of the install.
         *
         * @return true if the user wants to continue, false to stop the
         *         install
         */
        public boolean confirmJarDownload(InstallState state) {
            if (update) {
                // this an update, no need to confirm.
                return true;
            }

            installState = state;

            url = state.getJarUrl();

            parent.displayDownloadConfirmation(state);
            return waitForUser();
        }

        /**
         * Called with the current state of the install so the user can be
         * asked to confirm if the RMS data should be kept for new version of
         * an updated suite.
         *
         * @param state current state of the install.
         *
         * @return true if the user wants to keep the RMS data for the next
         * suite
         */
        public boolean keepRMS(InstallState state) {
            installState = state;

            parent.displayKeepRMSForm(state);
            return waitForUser();
        }

        /**
         * Called with the current state of the install so the user can be
         * asked to confirm the jar only download.
         *
         * @return true if the user wants to continue, false to stop the
         *         install
         */
        private boolean confirmJarOnlyDownload() {
            if (update) {
                // this an update, no need to confirm.
                return true;
            }

            parent.displayJarOnlyDownloadConfirmation();
            return waitForUser();
        }

        /**
         * Wait for the user to respond to current dialog.
         *
         * @return true if the user wants to continue, false to stop the
         *         install
         */
        private boolean waitForUser() {    
            boolean temp;

            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }

            installState = null;

            temp = continueInstall;
            continueInstall = false;

            return temp;
        }

        /**
         * Called with the current status of the install.
         * Changes the status alert box text based on the status.
         *
         * @param status current status of the install.
         * @param state current state of the install.
         */
        public void updateStatus(int status, InstallState state) {
            parent.updateStatus(status, state);
        }

        /**
         * Wait for the cancel message to be displayed to prevent flashing
         * and then display the list of suites.
         */
        private void displayListAfterCancelMessage() {
            // wait for the parent to display "cancelled"
            synchronized (parent) {
                /*
                 * We need to prevent "flashing" on fast
                 * development platforms.
                 */
                while ((System.currentTimeMillis() -
                        parent.lastDisplayChange) < parent.ALERT_TIMEOUT);

                if (parent.installListBox == null) {
                    // go back to app list
                    parent.notifyDestroyed();
                    return;
                }

                parent.display.setCurrent(parent.installListBox);
            }
        }
    }
}
