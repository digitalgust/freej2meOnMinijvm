/*
 * @(#)Selector.java	1.46 02/09/11 @(#)
 *
 * Copyright (c) 1999-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.midlet;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;
import com.sun.midp.lcdui.Resource;

/**
 * Selector provides a simple user interface to select MIDlets to run.  
 * It extracts the list of MIDlets from the attributes in the 
 * descriptor file and presents them to the user using the MIDlet-&lt;n&gt; name
 * and icon if any. When the user selects a MIDlet an instance
 * of the class indicated by MIDlet-&lt;n&gt; classname is created.
 */
public class Selector extends MIDlet implements CommandListener, Runnable {
    /**
     * The List of all the MIDlets.
     */
    private List mlist;         
    /**
     * The Display.
     */
    private Display display;    
    /**
     * Number of midlets in minfo.
     */
    private int mcount;
    /**
     * should this MIDlet exit after launching another MIDlet
     */
    private boolean exitAfterLaunch = true;
    /**
     * MIDlet information, class, name, icon; one per MIDlet.
     */
    private MIDletInfo[] minfo; 
    /**
     * the Command object to exit back to the MIDlet Suite Manager
     */
    private Command backCmd = new Command(Resource.getString("Back"),
                                          Command.BACK, 2);
    /**
     * the Command object for "Launch".
     */
    private Command launchCmd = new Command(Resource.getString("Launch"), 
                                           Command.ITEM, 1);
    /**
     * Index of the selected MIDlet, starts at -1 for non-selected.
     */
    private int selectedMidlet = -1;

    /**
     * Create and initialize a new Selector MIDlet.
     * The Display is retreived and the list of MIDlets read
     * from the descriptor file.
     */
    public Selector() {
        this(true);
    }

    /**
     * Create and initialize a new Selector MIDlet.
     * The Display is retreived and the list of MIDlets read
     * from the descriptor file.
     *
     * @param exitFlag set this to true if the selector should exit after
     *        launching a MIDlet.
     */
    protected Selector(boolean exitFlag) {
        exitAfterLaunch = exitFlag;
        display = Display.getDisplay(this); // TBD: is this value value here?
        mcount = 0;
        minfo = new MIDletInfo[20];
        readMIDletInfo();
    }

    /**
     * Start puts up a List of the MIDlets found in the descriptor file.
     */
    public void startApp() {
        setupList();
        mlist.addCommand(launchCmd);

        if (exitAfterLaunch) {
            mlist.addCommand(backCmd);
        }

        mlist.setCommandListener(this); // Listen for the selection

        display.setCurrent(mlist);
    }

    /**
     * Pause; there are no resources that need to be released.
     */
    public void pauseApp() {
    }

    /**
     * Destroy cleans up.
     * The only resource used is in the objects that will be
     * reclaimed by the garbage collector.
     * @param unconditional is ignored; the Selector always
     * destroys itself when requested.
     */
    public void destroyApp(boolean unconditional) {
    }

    /**
     * Respond to a command issued on any Screen.
     * The commands on list is Select and About.
     * Select triggers the creation of the MIDlet of the same name.
     * About puts up the copyright notice.
     *
     * @param c command activiated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
	if ((s == mlist && c == List.SELECT_COMMAND) || (c == launchCmd)) {
            synchronized (this) {
                if (selectedMidlet != -1) {
                    // the previous selected MIDlet is being launched
                    return;
                }

                selectedMidlet = mlist.getSelectedIndex();
            }

            new Thread(this).start();
	} else if (c == backCmd) {
	    destroyApp(false);
	    notifyDestroyed();
            return;
        } 
    }

    /**
     * Launch a the select MIDlet.
     */
    public void run() {
        Scheduler scheduler = Scheduler.getScheduler();
        String classname = minfo[selectedMidlet].classname;

        try {
            scheduler.register(MIDletState.createMIDlet(classname));

            if (exitAfterLaunch) {
                // exit
                destroyApp(false);
                notifyDestroyed();
                return;
            }

            // Give the new MIDlet the screen by setting current to null
            display.setCurrent(null);

            // let another MIDlet be selected after MIDlet ends
            selectedMidlet = -1;
            return;
        } catch (Exception ex) {
            StringBuffer sb = new StringBuffer()
                .append(minfo[selectedMidlet].name)
                .append(", ")
                .append(classname)
                .append("\n")
                .append(Resource.getString("Exception"))
                .append(": ")
                .append(ex.toString());

            Alert a = new Alert(Resource.getString("Cannot start: "), 
                                sb.toString(), null, null);
            System.out.println("Unable to create MIDlet " + classname);
            ex.printStackTrace();
            display.setCurrent(a, mlist);

            // let another MIDlet be selected after the alert
            selectedMidlet = -1;
            return;
        }
    }

    /**
     * Read the set of MIDlet names, icons and classes
     * Fill in the list.
     */
    private void setupList() {
        if (mlist == null) {
            mlist = new List(Resource.getString("Select one to launch:"), 
                             Choice.IMPLICIT);

	    // Add each midlet
	    for (int i = 0; i < mcount; i++) {
		Image icon = null;
		if (minfo[i].icon != null) {
		    try { 
		        icon = Image.createImage(minfo[i].icon);
		    } catch (java.io.IOException noImage) {
			// TBD: use a default ICON of the app has none.
		    }
		}
		mlist.append(" " + minfo[i].name, icon);
	    }
	}
    }
        
    /**
     * Read in and create a MIDletInfor for each MIDlet-&lt;n&gt;
     */
    private void readMIDletInfo() {
        for (int n = 1; n < 100; n++) {
            String nth = "MIDlet-"+ n;
            String attr = getAppProperty(nth);
            if (attr == null || attr.length() == 0)
                break;

            addMIDlet(new MIDletInfo(attr));
        }
    }

    /**
     * Add a MIDlet to the list.
     * @param info MIDlet information to add to MIDlet
     */
    private void addMIDlet(MIDletInfo info) {
        if (mcount >= minfo.length) {
            MIDletInfo[] n = new MIDletInfo[mcount+4];
            System.arraycopy(minfo, 0, n, 0, mcount);
            minfo = n;
        }
        minfo[mcount++] = info;
    }
}



