/*
 * @(#)SecurityToken.java	1.24 02/10/14 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.security;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import com.sun.midp.lcdui.*;

/**
 * Contains methods to get various security state information of the currently
 * running MIDlet suite.
 */
public final class SecurityToken {
    /** The default blanket level answer. */
    private static final String BLANKET_ANSWER =
        "Yes, always. Don't ask again.";

    /** The default session level answer. */
    private static final String SESSION_ANSWER =
        "This time. Ask me next time.";

    /** The default cancel cancel answer. */
    private static final String CANCEL_ANSWER =
        "Not this time. Ask me next time.";

    /** The default deny level answer. */
    private static final String DENY_ANSWER = "No. Shut off %1.";

    /** The standard security exception message. */
    public static final String STD_EX_MSG = "Application not authorized " +
                                            "to access the restricted API";

    /** Enables the first domain be constructed without a domain. */
    private static boolean firstCaller = true;

    /** Permission list. */
    private byte permissions[];

    /**
     * A flag for each permission, True if permission has been asked
     * this session.
     */
    private boolean permissionAsked[];

    /** Maximum permission level list. */
    private byte maxPermissionLevels[];

    /**
     * Creates a security domain with a list of permitted actions or no list
     * to indicate all actions. The caller must be have permission for
     * <code>Permissions.MIDP</code> or be the first caller of
     * the method for this instance of the VM.
     * @param securityToken security token of the caller, can be null for
     *                       the first caller
     * @param ApiPermissions for the token, can be null
     * @exception SecurityException if caller is not permitted to call this
     *            method
     */
    public SecurityToken(SecurityToken securityToken,
            byte[][] ApiPermissions) {
        // assume the JAM is the first caller
        if (firstCaller) {
            firstCaller = false;
        } else {
            securityToken.checkIfPermissionAllowed(Permissions.MIDP);
        }

        maxPermissionLevels = ApiPermissions[Permissions.MAX_LEVELS];

        permissions = ApiPermissions[Permissions.CUR_LEVELS];

        permissionAsked = new boolean[permissions.length];

    }

    /** Creates a security domain for preempting, all permitted actions. */
    private SecurityToken() {}

    /**
     * Get the status of the specified permission.
     * If no API on the device defines the specific permission 
     * requested then it must be reported as denied.
     * If the status of the permission is not known because it might
     * require a user interaction then it should be reported as unknown.
     *
     * @param permission to check if denied, allowed, or unknown.
     * @return 0 if the permission is denied; 1 if the permission is allowed;
     * 	-1 if the status is unknown
     */
    public int checkPermission(String permission) {
        boolean found = false;
        int i;

        synchronized (this) {
            for (i = 0; i < Permissions.NUMBER_OF_PERMISSIONS; i++) {
                if (Permissions.getName(i).equals(permission)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                // report denied
                return 0;
            }

            switch (permissions[i]) {
            case Permissions.ALLOW:
            case Permissions.BLANKET_GRANTED:
                // report allowed
                return 1;
                
            case Permissions.SESSION:
                if (permissionAsked[i]) {
                    return 1;
                }

                // fall through
            case Permissions.BLANKET:
            case Permissions.ONE_SHOT:
            case Permissions.DENY:
                // report unknown
                return -1;

            case Permissions.DENY_SESSION:
                if (!permissionAsked[i]) {
                    return -1;
                }
            }

            // report denied
            return 0;
        }
    }

    /**
     * Check to see the suite has the ALLOW level for specific permission.
     * This is used for by internal APIs that only provide access to
     * trusted system applications.
     *
     * @param permission permission ID from com.sun.midp.security.Permissions
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     */
    public void checkIfPermissionAllowed(int permission) {
        checkIfPermissionAllowed(permission, STD_EX_MSG);
    }

    /**
     * Check to see the suite has the ALLOW level for specific permission.
     * This is used for by internal APIs that only provide access to
     * trusted system applications.
     *
     * @param permission permission ID from com.sun.midp.security.Permissions
     * @param exceptionMsg message if a security exception is thrown
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     */
    public void checkIfPermissionAllowed(int permission, String exceptionMsg) {
        if (permissions == null) {
            /* totally trusted, all permission allowed */
            return;
        }

        if (permission >= 0 && permission < permissions.length &&
            (permissions[permission] == Permissions.ALLOW)) {
            return;
        }

        // this method do not ask the user
        throw new SecurityException(exceptionMsg);
    }

    /**
     * Check for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     * <p>
     * The title, and question strings will be translated,
     * if a string resource is available.
     * Since the strings can have sustitution token in them, if there is a
     * "%" it must changed to "%%". If a string has a %1, the app parameter
     * will be substituted for it. If a string has a "%2, the resource
     * parameter will be substituted for it. If a string has a %3, the
     * extraValue parameter will be substituted for it.
     *
     * @param permission ID of the permission to check for,
     *      the ID must be from
     *      {@link com.sun.midp.security.Permissions}
     * @param title title of the dialog
     * @param question question to ask user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public void checkForPermission(int permission, String title,
            String question, String app, String resource, String extraValue)
            throws InterruptedException {

        checkForPermission(permission, title, question, app, resource, 
             extraValue, 0, SESSION_ANSWER, CANCEL_ANSWER, STD_EX_MSG);
    }

    /**
     * Check for permission and throw an exception if not allowed.
     * May block to ask the user a question.
     * <p>
     * The title, question, and answer strings will be translated,
     * if a string resource is available.
     * Since the strings can have sustitution token in them, if there is a
     * "%" it must changed to "%%". If a string has a %1, the app parameter
     * will be substituted for it. If a string has a "%2, the resource
     * parameter will be substituted for it. If a string has a %3, the
     * extraValue parameter will be substituted for it.
     *
     * @param permission ID of the permission to check for,
     *      the ID must be from
     *      {@link com.sun.midp.security.Permissions}
     * @param title title of the dialog
     * @param question question to ask user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     * @param skip permission to skip, SESSION, DENY,
     *        or any other for skip none, multiple permissions can be added
     *        together
     * @param sessionAnswer text for the session answer or one shot if
     *        the maximum permission level is one shot
     * @param cancelAnswer text for the cancel answer
     * @param exceptionMsg message if a security exception is thrown
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public void checkForPermission(int permission, String title,
            String question, String app, String resource, String extraValue,
            int skip, String sessionAnswer, String cancelAnswer,
            String exceptionMsg) throws InterruptedException {
        if (permissions == null) {
            /* totally trusted, all permissions allowed */
            return;
        }

        synchronized (this) {
            if (permission >= 0 && permission < permissions.length) {
                switch (permissions[permission]) {
                case Permissions.SESSION:
                case Permissions.DENY_SESSION:
                    if (permissionAsked[permission]) {
                        break;
                    }

                    // fall through
                case Permissions.ONE_SHOT:
                case Permissions.BLANKET:
                case Permissions.DENY:
                    permissions[permission] =
                        (byte)askUserForPermission(new SecurityToken(), title,
                        question, app, resource, extraValue,
                        maxPermissionLevels[permission],
                        permissions[permission], skip, BLANKET_ANSWER,
                        sessionAnswer, cancelAnswer, DENY_ANSWER);
                    
                    permissionAsked[permission] = true;
                }

                switch (permissions[permission]) {
                case Permissions.BLANKET:
                    // do not ask again
                    permissions[permission] = Permissions.BLANKET_GRANTED;

                    // fall through
                case Permissions.ALLOW:
                case Permissions.BLANKET_GRANTED:
                case Permissions.SESSION:
                case Permissions.ONE_SHOT:
                    return;

                case Permissions.DENY:
                    // do not ask again
                    permissions[permission] = Permissions.USER_DENIED;

                case Permissions.NEVER:
                    // fail do not ask again
                    break;

                default:
                    // fail but ask again, so switch back to DENY session
                    permissions[permission] = (byte)Permissions.DENY_SESSION;
                }
            }

            throw new SecurityException(exceptionMsg);
        }
    }

    /**
     * Ask the user permission, selecting the max level, default level.
     *
     * @param token security token with the permission to peempt the
     *        foreground display
     * @param title title of the dialog
     * @param question question to ask user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     * @param maximumLevel maximum permission level to display
     * @param defaultLevel default permission level, DENY_SESSION,
     *         or CANCELLED for "Not Now". If the level is greater than the
     *        maximum level, maximum level will be used.
     *
     * @return new permission level or -1 if user cancelled
     *
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public static int askUserForPermission(SecurityToken token, String title,
            String question, String app, String resource, String extraValue,
            int maximumLevel, int defaultLevel) throws InterruptedException {

        PermissionDialog dialog =
            new PermissionDialog(token, title, question, app, resource,
                extraValue, maximumLevel, defaultLevel, 0, BLANKET_ANSWER,
                SESSION_ANSWER, CANCEL_ANSWER, DENY_ANSWER);
        
        return dialog.waitForAnswer();
    }

    /**
     * Ask the user permission and wait for the answer.
     *
     * <p>
     * The title, question, and answer strings will be translated,
     * if a string resource is available.
     * Since the strings can have sustitution token in them, if there is a
     * "%" it must changed to "%%". If a string has a %1, the app parameter
     * will be substituted for it. If a string has a "%2, the resource
     * parameter will be substituted for it. If a string has a %3, the
     * extraValue parameter will be substituted for it.
     *
     * @param token security token with the permission to peempt the
     *        foreground display
     * @param title title of the dialog
     * @param question question to ask user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     * @param maximumLevel maximum permission level to display
     * @param defaultLevel default permission level, DENY_SESSION,
     *         or CANCELLED for "Not Now". If the level is greater than the
     *        maximum level, maximum level will be used.
     * @param skip permission to skip, SESSION, DENY,
     *        or any other for skip none, multiple permissions can be added
     *        together
     * @param blanketAnswer text for the blanket answer
     * @param sessionAnswer text for the session answer or one shot if
     *        the maximum permission level is one shot
     * @param cancelAnswer text for the cancel answer
     * @param denyAnswer text for the deny answer
     *
     * @return new permission level or -1 if user cancelled
     *
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public static int askUserForPermission(SecurityToken token, String title,
            String question, String app, String resource, String extraValue,
            int maximumLevel, int defaultLevel, int skip,
            String blanketAnswer, String sessionAnswer, String cancelAnswer,
            String denyAnswer) throws InterruptedException {

        PermissionDialog dialog =
            new PermissionDialog(token, title, question, app, resource,
                extraValue, maximumLevel, defaultLevel, skip, blanketAnswer,
                sessionAnswer, cancelAnswer, denyAnswer);
        
        return dialog.waitForAnswer();
    }
}

/** Implements security permission dialog. */
class PermissionDialog implements CommandListener, MIDletEventListener {
    /** Answer that indicates that the dialog was cancelled. */
    static final int CANCELLED = -1;

    /** Caches the display manager reference. */
    private DisplayManager displayManager =
        DisplayManagerFactory.getDisplayManager();

    /** This item will have the user's choice. */
    private RadioButtonSet choice;

    /** Command object for "OK" command. */
    private Command okCmd = new Command(Resource.getString("OK"), 
                                           Command.OK, 1);

    /** Holds the preempt token so the form can end. */
    private Object preemptToken;
    
    /** Holds the answer to the security question. */
    private int answer = CANCELLED;

    /**
     * Construct permission dialog with a different answer for cancel.
     * <p>
     * The title, question, and answer strings will be translated,
     * if a string resource is available.
     * Since the strings can have sustitution token in them, if there is a
     * "%" it must changed to "%%". If a string has a %1, the app parameter
     * will be substituted for it. If a string has a "%2, the resource
     * parameter will be substituted for it. If a string has a %3, the
     * extraValue parameter will be substituted for it.
     *
     * @param token security token with the permission to peempt the
     *        foreground display
     * @param title title of the dialog
     * @param question question to ask user
     * @param app name of the application to insert into a string
     *        can be null if no %1 a string
     * @param resource string to insert into a string,
     *        can be null if no %2 in a string
     * @param extraValue string to insert into a string,
     *        can be null if no %3 in a string
     * @param maximumLevel maximum permission level to display
     * @param defaultLevel default permission level, DENY_SESSION,
     *         or CANCELLED for "Not Now". If the level is greater than the
     *        maximum level, maximum level will be used.
     * @param skip permission to skip, SESSION, DENY,
     *        or any other for skip none, multiple permissions can be added
     *        together
     * @param blanketAnswer text for the blanket answer
     * @param sessionAnswer text for the session answer or one shot if
     *        the maximum permission level is one shot
     * @param cancelAnswer text for the cancel answer
     * @param denyAnswer text for the deny answer
     *
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    PermissionDialog(SecurityToken token, String title, String question,
            String app, String resource, String extraValue, int maximumLevel,
            int defaultLevel, int skip, String blanketAnswer,
            String sessionAnswer, String cancelAnswer, String denyAnswer)
            throws InterruptedException {
        String[] substitutions = {app, resource, extraValue};
        Form form = new Form(Resource.getString(title, substitutions));

        choice = new RadioButtonSet(5,
                     Resource.getString(question, substitutions));

        switch (maximumLevel) {
        case Permissions.BLANKET:
            choice.append(Resource.getString(blanketAnswer, substitutions),
                null, Permissions.BLANKET);
        case Permissions.SESSION:
            if ((skip & Permissions.SESSION) != Permissions.SESSION) {
                choice.append(Resource.getString(sessionAnswer, substitutions),
                    null, Permissions.SESSION);
            }

            break;

        case Permissions.ONE_SHOT:
            choice.append(Resource.getString(sessionAnswer, substitutions),
                null, Permissions.ONE_SHOT);
        }

        choice.append(Resource.getString(cancelAnswer, substitutions), null,
                      CANCELLED);

        if ((skip & Permissions.DENY) != Permissions.DENY) {
            choice.append(Resource.getString(denyAnswer, substitutions),
                null, Permissions.DENY);
        }

        switch (defaultLevel) {
        case Permissions.BLANKET:
        case Permissions.SESSION:
        case Permissions.ONE_SHOT:
        case Permissions.DENY:
            break;

        case Permissions.DENY_SESSION:
            // adjust for internal purposes
        default:
            defaultLevel = CANCELLED;
        }

        try {
            choice.setDefaultButton(defaultLevel);
        } catch (IndexOutOfBoundsException e) {
            choice.setDefaultButton(CANCELLED);
        }

        choice.setPreferredSize(form.getWidth(), -1);

        form.append(choice);
        form.addCommand(okCmd);
        form.setCommandListener(this);
        preemptToken = displayManager.preemptDisplay(token, this, form, true);
    }

    /**
     * Waits for the user's answer.
     *
     * @return user's answer
     */
    int waitForAnswer() {
        synchronized (this) {
            if (preemptToken == null) {
                return CANCELLED;
            }

            try {
                wait();
            } catch (Throwable t) {
                return CANCELLED;
            }

            return answer;
        }
    }

    /**
     * Sets the user's answer and notifies waitForAnswer and
     * ends the form.
     *
     * @param theAnswer user's answer or CANCEL if system cancelled the
     *        screen
     */
    private void setAnswer(int theAnswer) {
        synchronized (this) {
            answer = theAnswer;
            displayManager.donePreempting(preemptToken);
            notify();
        }

    }

    /**
     * Respond to a command issued on security question form.
     *
     * @param c command activiated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == okCmd) {
            setAnswer(choice.getSelectedButton());
        }

    }

    /**
     * Pause the current foreground MIDlet and return to the
     * AMS or "selector" to possibly run another MIDlet in the
     * currently active suite.
     * <p>
     * This is not apply to the security dialog.
     *
     * @param midlet midlet that the event applies to
     */
    public void pauseMIDlet(MIDlet midlet) {}
 
    /**
     * Start the currently suspended state. This is not apply to
     * the security dialog.
     *
     * @param midlet midlet that the event applies to
     */
    public void startMIDlet(MIDlet midlet) {}
 
    /**
     * Destroy the MIDlet given midlet.
     * <p>
     * This is not apply to the security dialog.
     *
     * @param midlet midlet that the event applies to
     */
    public void destroyMIDlet(MIDlet midlet) {
    }
}

/**
 * A <code>RadioButtonSet</code> is a group radio buttons intended to be
 * placed within a <code>Form</code>. However the radio buttons can be
 * accessed by a assigned ID instead of by index. This lets the calling
 * code be the same when dealing with dynamic sets.
 */
class RadioButtonSet extends ChoiceGroup {
    /** Keeps track of the button IDs. */
    private int[] ids;

    /**
     * Creates a new, empty <code>RadioButtonSet</code>, specifying its
     * title.
     *
     * @param maxButtons maximum number of buttons the set could have
     * @param label the item's label (see {@link Item Item})
     */
    RadioButtonSet(int maxButtons, String label) {
        super(label, Choice.EXCLUSIVE);
        ids = new int[maxButtons];
    }

    /**
     * Appends choice to the set.
     *
     * @param stringPart the string part of the element to be added
     * @param imagePart the image part of the element to be added, or
     * <code>null</code> if there is no image part
     * @param id ID for the radio button
     *
     * @throws IllegalArgumentException if the image is mutable
     * @throws NullPointerException if <code>stringPart</code> is
     * <code>null</code>
     * @throws IndexOutOfBoundsException this call would exceed the maximum
     *         number of buttons for this set
     */
    public void append(String stringPart, Image imagePart, int id) {
        ids[append(stringPart, imagePart)] = id;
    }

    /**
     * Set the default button.
     *
     * @param id ID of default button
     *
     * @throws IndexOutOfBoundsException if <code>id</code> is invalid
     */
    public void setDefaultButton(int id) { 
        setSelectedIndex(indexFor(id), true);
    }

    /**
     * Returns the ID of the selected radio button.
     *
     * @return ID of selected element
     */
    public int getSelectedButton() { 
        return ids[getSelectedIndex()];
    }

    /**
     * Find the index for an ID.
     *
     * @param id button id
     *
     * @return index for a button
     *
     * @exception IndexOutOfBoundsException If no element exists with that ID
     */
    private int indexFor(int id) {
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == id) {
                return i;
            }
        }

        throw new IndexOutOfBoundsException();
    }
}
