/*
 * @(#)ImplicitlyTrustedClass.java	1.4 02/07/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.security;

/**
 * Enables optional dynamically loaded implicitly trusted classes to be given
 * security tokens.
 */
public interface ImplicitlyTrustedClass {
    /**
     * Initializes the security token this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class
     */
    public void initSecurityToken(SecurityToken token);
}
