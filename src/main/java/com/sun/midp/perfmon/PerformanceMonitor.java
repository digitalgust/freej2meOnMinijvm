/*
 * @(#)PerformanceMonitor.java	1.9 02/07/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.perfmon;

import java.io.PrintStream;

/**
 * A class that implements the <code>PerformanceMonitor</code> interface.
 * A performance monitor session consists of a start time [ call <code>start 
 * </code> method ] and an end time [ call <code>end</code> method ]. 
 * This class tracks a number of system resources and provides the 
 * ability to track system resource for delta's (between the start and 
 * end of a session).
 *
 * <p>Each performance monitor session must include a call to first the 
 * <code>start</code> and second the <code>end</code> methods in order 
 * to measure system resource delta's. Once a proper session call sequence 
 * has been made the application can then access the appropriate methods 
 * to retrieve the desired system resource delta.
 * Once a call has been made to <code>start</code> method a new session 
 * has been started. If the application calls <code>end</code> before 
 * <code>start</code> an error will be returned.
 *
 * <p>In order to ensure data integrity the implemenation of the 
 * PerformanceMonitor interface requires the  application using the
 * interface always uses the interface with the proper calling sequence.
 * Below is an example of how an application would use the PerformanceMonitor
 * interface (and/or implemenation) correctly:
 *
 * <p> This example of code shows how an application might want to use
 * the PerformanceMonitor interface to measure read() I/O resources.
 *
 * <pre>
 *     start = perfmon.start("read()");  // start a perfmon session
 *
 *     for (int i = 0 ; i < len ; i++ ) {
 *         if ((ch = input.read()) != -1){
 *		if (ch <= ' ') ch = ' ';
 *		b.append((char) ch);
 *	    }
 *	}
 *      end = perfmon.end();            // end a perfmon session
 *
 *      perfmon.report(System.err);     // print stdout standard report format
 *           
 *      perfmon.write(System.err);      // write key/value pair http-to-servlet
 * </pre>
 *
 */ 

public interface PerformanceMonitor {
        
    /**
    * Specifies instruction counter.
    */
    static final int INSTRUCTION_COUNTER         =  0;
    /**
    * Specifies thread switch counter.
    */
    static final int THREAD_SWITCH_COUNTER       =  1;
    /**
    * Specifies dynamic object counter.
    */
    static final int DYNAMIC_OBJECT_COUNTER      =  2;
    /**
    * Specifies dynamic allocation counter.
    */
    static final int DYNAMIC_ALLOC_COUNTER       =  3;
    /**
    * Specifies dynamic deallocation counter.
    */
    static final int DYNAMIC_DEALLOC_COUNTER     =  4;
    /**
    * Specifies garbage collection counter.
    */
    static final int GARBAGE_COLLECTION_COUNTER  =  5;
    /**
    * Specifies total garbage collection deferrals.
    */
    static final int TOTAL_GC_DEFERRALS          =  6;
    /**
    * Specifies maximum garbage collection deferrals.
    */
    static final int MAX_GC_DEFERRALS            =  7;
    /**
    * Specifies garbage collection rescans.
    */
    static final int GARBAGE_COLLECTION_RESCANS  =  8;
    /**
     * Total number of performance measurement counters.
     */
    static final int TOTAL_SYSTEM_PERFMON_COUNTERS = 9;

    /**
     *  Starts the performance monitor timer and system measurements
     *  and sets (or resets) the start time in milliseconds.
     *
     * @param      testname          current test name
     * @return                       start time in milliseconds
     *                               (long - returns Date.getTime())
     */
    public long start(String testname);

    /**
     *  Ends the performance monitor timer and system measurements
     *  and sets (or resets) the end time in milliseconds.
     *
     * @return                       end time in milliseconds
     *                               (long - returns Date.getTime())
     */
    public long end();

    /**
     *  Reports a standard output format to the PrintStream specified
     *  which includes delta measurements (e.g. elapsed time) between
     *  a specific start and end time.
     *
     * @param      printstream       where to write the output buffer
     */
    public void report(PrintStream printstream);

    /**
     * Gets the current perfmon stat value.
     *
     * @param     perfmon_id perfmon id (e.g. THREAD_SWITCH_COUNTER).
     * @return               perfmon status current (int) value
     * @exception IllegalArgumentException an invalid perfmon id type
     */
    public int getStat(int perfmon_id)  
        throws IllegalArgumentException;

    /**
     *  Gets the appropriate perfmon status delta measurement value between
     *  a specific start and end time. 
     * 
     *
     * @param     perfmon_id performon id (e.g. THREAD_SWITCH_COUNTER).
     * @return               perfmon stat (int) value (between start and end)
     * @exception IllegalStateException    if start or end equal 0
     * @exception IllegalArgumentException invalid measurement type
     */
    public int getDelta(int perfmon_id)  
        throws IllegalStateException, IllegalArgumentException;

    /**
     * Gets the current perfmon session start time in milliseconds.
     *
     * @return                current perfmon start time in milliseconds
     */
    public long getStartTime();

    /**
     * Gets the current perfmon session end time in milliseconds.
     *
     * @return                current perfmon end time in milliseconds
     */
    public long getEndTime();

    /**
     * Gets the current perfmon session end time in milliseconds.
     *
     * @return               current perfmon end time in milliseconds
     * @exception IllegalStateException  if start or end equal 0
     */
    public long getElapsedTime()  
        throws IllegalStateException;
    
}
