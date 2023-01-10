/*
 * @(#)DisplayDeviceAccess.java	1.2 02/07/18 @(#)
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.sun.midp.lcdui;

import java.util.*;
import javax.microedition.lcdui.Display;

/**
 * Class that allows Display to access 
 * Device specific calls in the native code
 * like vibrate and flashBacklight.
 */

public class DisplayDeviceAccess {

    /** 
     * The Timer to service TimerTasks. 
     */
    private static Timer timerService = new Timer();

    /** 
     * A TimerTask. 
     */
    private TimerTask task = null;

    /**
     * Requests a flashing effect for the device's backlight.
     * @param duration the number of milliseconds the backlight should be 
     * flashed, or zero if the flashing should be stopped
     *
     * @return true if the backlight can be controlled
     */
    public boolean flashBacklight(int duration) {
	if (duration < 0) {
	    throw new IllegalArgumentException();
	}

	if (duration == 0) {
	    showBacklight(false);
	    cancelTimer();
	}

	// start timer
	setTimer(duration);
	return showBacklight(true);
    }

    /**
     * Set a new timer.
     * @param duration the number of milliseconds the vibrator should be run
     */
    private void setTimer(int duration) {

        cancelTimer();
        try {
            task = new TimerClient();
            // execute for the duration in milli seconds
	    timerService.schedule(task, duration);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            cancelTimer();
        }
    }

    /**
     * Cancel any running Timer.
     */
    private void cancelTimer() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Inner class TimerTask
     */
    class TimerClient extends TimerTask {
        /**
         * Simply turn off backlight
         */
        public final void run() {
	    showBacklight(false);
        }
    }

    /**
     * show Backlight
     * @param on true to turn on the backlight and false to turn it off
     * @return true, if backlight was shown.
     */
    private native boolean showBacklight(boolean on);
}
