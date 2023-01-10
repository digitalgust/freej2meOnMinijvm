/*
 * @(#)BasicText.java	1.72 00/07/20
 * Copyright (c) 1999,2000 Sun Microsystems, Inc. All Rights Reserved.
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


/**
 * PhoneDial allows for UI java code to access the phones native 
 * features. 
 */
public class PhoneDial {

    /**
     * The call methods gets a phone call made.
     * @param phoneNumber the phoneNumber to be called in string format
     * @return true if the service is available
     */
    public static boolean call(String phoneNumber) {
	System.out.println("phoneNumber is call "+phoneNumber);
	// native function would need to handle the actual call
	return true;
    }
    
}
