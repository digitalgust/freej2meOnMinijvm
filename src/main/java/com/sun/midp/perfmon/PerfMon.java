/*
 * @(#)PerfMon.java	1.16 02/07/24 @(#)
 *
 * Copyright (c) 2001-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.perfmon;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * A class that implements the <code>PerformanceMonitor</code> interface.
 * A performance monitor session consists of a start time [ <code>start</code>
 * method ] and an end time [ <code>end</code> method ]. This class tracks
 * a number of system resources that provides the ability to track
 * a system resource for delta's between the start and end of a session.
 *
 * <p>Each performance monitor session must include a call to the start
 * and end methods in order to measure system resource delta's. Once a
 * proper session call sequence has been made the application can then access
 * the appropriate methods to retrieve the desired system resource delta.
 * Once a call has been made to start method a new session has been started.
 *
 * <p>In order to ensure data integrity the implemenation of the 
 * <code>PerformanceMonitor</code> interface requires the appl using the
 * interface always uses the interface with the proper calling sequence.
 * Below is an example of how an application would use the 
 * <code>PerformanceMonitor</code> interface (and/or implemenation) correctly:
 *
 * <p> This example of code shows how an application might want to use
 * the PerformanceMonitor interface to measure read() I/O resources.
 *
 * <pre>
 *     start = perfmon.start("read()");       // start a perfmon session
 *
 *     for (int i = 0 ; i < len ; i++ ) {
 *         if ((ch = input.read()) != -1){
 *		if (ch <= ' ') ch = ' ';
 *		b.append((char) ch);
 *	    }
 *	}
 *      end = perfmon.end();                  // end a perfmon session
 *
 *      perfmon.report(System.err);           // print standard report format
 *           
 *      String results = 
 *              Long.toString(
 *                  perfmon.getStat((
 *                      PerfMon.INSTRUCTION_COUNTER)));  // get it as string
 *
 *      System.err.println(
 *         "Instruction Counter (string): ["+
 *                 results + "]");                       // print the string
 * </pre>
 */ 

public class PerfMon implements PerformanceMonitor {

    /** the class name of the object being tested - prints on report */
    String     m_classname;
    /** the testname of the application being tested - prints on report */
    String     m_testname;
    /** first time logic flag */
    boolean    m_first_time                    = true;
    /** start time for calling application */
    long       m_start_t;
    /** end time for calling application */
    long       m_end_t;
    /** total time for calling application */
    long       m_delta_t;
    /** all measurement start counters */
    int[]      m_start_count;
    /** all measurement end counters */
    int[]      m_end_count;
    /** all measurement delta counters */
    int[]      m_delta_count;

    /**
     * Construct a PerfMon object.
     */
    public PerfMon() {
	
	this("");
    }

    /**
     * Construct a PerfMon object.
     *
     * @param      classname      the application or classname being tested
     */
    public PerfMon(String classname) {
        
        m_classname = classname;
                
        m_start_count = new int[TOTAL_SYSTEM_PERFMON_COUNTERS];
        m_end_count = new int[TOTAL_SYSTEM_PERFMON_COUNTERS];
        m_delta_count = new int[TOTAL_SYSTEM_PERFMON_COUNTERS];
    }
    
    /**
     * Initializes the performance monitor timer.
     *
     */
    public synchronized void init() {
        
        m_start_t                       = 0;
        m_end_t                         = -1;
	m_testname                      = null;
        
        for (int i = 0; i < TOTAL_SYSTEM_PERFMON_COUNTERS; i++) {	    
	    m_start_count[i] = 0;
	    m_end_count[i] = 0;
	    m_delta_count[i] = 0;
	}
	
    }
    /**
     *  Starts the performance monitor timer and system measurements.
     *
     * @param      testname            current method name
     * @return     start time    (long - return System.currentTimeMillis())
     */
    public synchronized long start(String testname) {

        this.init();
        
        m_testname = testname;
        
        for (int i = 0; i < TOTAL_SYSTEM_PERFMON_COUNTERS; i++) {
	    m_start_count[i] = getStat(i);
        }
        
        m_start_t = System.currentTimeMillis();
        
        return (m_start_t);
    }
    /**
     *  Ends the performance monitor timer and system measurements.
     *
     * @return      end time    (long - return System.currentTimeMillis())
     */
    public synchronized long end() {
        
        for (int i = 0; i < TOTAL_SYSTEM_PERFMON_COUNTERS; i++) {
            m_end_count[i] = getStat(i);
        }
        
        m_end_t = System.currentTimeMillis();
        
        return (m_end_t);
    }
    /**
     *  Reports a standard perfmon output format to a PrintStream 
     *  which includes delta measurements (e.g. elapsed time) between
     *  a specific start and end time.
     *
     * @param      printstream       output stream to print to
     */
    public synchronized void report(PrintStream printstream) {

        Calendar cal = Calendar.getInstance();
        
        TimeZone tz = TimeZone.getTimeZone("GMT");
        cal.setTimeZone(tz);
        
        m_delta_t = m_end_t - m_start_t;
        
        for (int i = 0; i < TOTAL_SYSTEM_PERFMON_COUNTERS; i++) {
            m_delta_count[i] = m_end_count[i] - m_start_count[i];
        }

        if (m_first_time) {
            printstream.println("\n=====================================" +
                                "=====================================" +
                                "=====");
            printstream.println("\t\t\tPerformance Monitor Tracing Report");
            printstream.println("Application Name: ["+m_classname+"]");
            printstream.println("Report Date: ["+toString(cal) + "]");
            printstream.println("=====================================" +
                                "=====================================" +
                                "=====\n");
            m_first_time = false;
        }

        printstream.println("-------------------------------------" +
                            "-------------------------------------" +
                            "-----");
        printstream.println("Performance Monitor - Snapshot Analysis - ");
        printstream.println("TimeStamp: \t\t["+toString(cal)+"]");
        printstream.println("Class Name: \t\t["+m_classname+"]");
        printstream.println("Test Name: \t\t["+m_testname+"]");
        printstream.println("Elapsed Time : \t\t["+m_delta_t+
                           "] (milli-seconds)");
        printstream.println("Classname: \t\t["+m_classname+"]");
        printstream.println("Instruction Count: \t["+
                           m_delta_count[INSTRUCTION_COUNTER]+"]");
        printstream.println("Thread Switch Count: \t["+ 
                           m_delta_count[THREAD_SWITCH_COUNTER]+"]");
        printstream.println("Dynamic Object Count: \t["+ 
                           m_delta_count[DYNAMIC_OBJECT_COUNTER]+"]");
        printstream.println("Dynamic Alloc Count: \t["+ 
                           m_delta_count[DYNAMIC_ALLOC_COUNTER]+"]");
        printstream.println("Dynamic Dealloc Count: \t["+ 
                           m_delta_count[DYNAMIC_DEALLOC_COUNTER]+"]");
        printstream.println("Garbage Collect Count: \t["+
                           m_delta_count[GARBAGE_COLLECTION_COUNTER]+"]");
        printstream.println("GC Deferrals: \t\t["+ 
                           m_delta_count[TOTAL_GC_DEFERRALS]+"]");
        printstream.println("Maximum GC Deferrals: \t["+ 
                           m_delta_count[MAX_GC_DEFERRALS]+"]");
        printstream.println("Garbage Collect Rescans:["+ 
                           m_delta_count[GARBAGE_COLLECTION_RESCANS]+"]");
        printstream.println("-------------------------------------" +
                            "-------------------------------------" +
                            "-----\n");
    }

    /**
     * Formats a Date for output display.
     *
     * @param      cal         calendar object
     * @return                 formatted calendar string format
     */
    static String toString(Calendar cal) {

        int h = cal.get(Calendar.HOUR);
        String hour = h == 0? " (00" : (h < 10? (" (0"+h) : (" ("+h));
        int m = cal.get(Calendar.MINUTE);
        String min = m == 0? ":00" : (m < 10? (":0"+m) : (":"+m));
        int am_pm = cal.get(Calendar.AM_PM);
        return cal.toString()+hour+min+" " + 
            (am_pm == Calendar.PM? "pm)" : "am)");
    }
    
    /**
     * Gets the appropriate perfmon status delta measurement value between
     *  a specific start and end time.
     * 
     * @param    perfmon_id  the perfmon id type (e.g. INSTRUCTION_COUNTER)
     * @return               perfmon stat value (between start and end)
     * @exception IllegalStateException    if start or end equal 0
     * @exception IllegalArgumentException invalid measurement type
     */
    public int getDelta(int perfmon_id)
        throws IllegalStateException, IllegalArgumentException {
        
        if ((m_start_count[INSTRUCTION_COUNTER] == 0) ||
            (m_end_count[INSTRUCTION_COUNTER] == 0))
            {
                
                throw new 
                    IllegalStateException("Invalid start/end sequence.");
            }
        
        if (perfmon_id < INSTRUCTION_COUNTER || 
            perfmon_id > GARBAGE_COLLECTION_RESCANS) {
            
            throw new 
                IllegalArgumentException("Invalid PerfMon measurement type");
        }
        
        return (int) (m_delta_count[perfmon_id] =
                      m_end_count[perfmon_id] -
                      m_start_count[perfmon_id]);       
        
    }
    /**
     * Gets the appropriate and current perfmon stat value.
     *
     * @param     perfmon_id perfmon id (e.g. THREAD_SWITCH_COUNTER).
     * @return                 perfmon status current value
     * @exception IllegalArgumentException if an invalid perfmon id type
     */
    public int getStat(int perfmon_id)
        throws IllegalArgumentException {
        
        if (perfmon_id < INSTRUCTION_COUNTER || 
            perfmon_id > GARBAGE_COLLECTION_RESCANS) {
            
            throw new 
                IllegalArgumentException("Invalid PerfMon measurement type");
        }

        return (int)(sysGetCounter(perfmon_id));

    }

    /**
     * Gets the current perfmon session start time.
     *
     * @return              current perfmon start time in milliseconds
     */
    public long getStartTime() {

        return (m_start_t);
    }
    
    /**
     * Gets the current perfmon session end time.
     *
     * @return              current perfmon end time in milliseconds
     */
    public long getEndTime() {
        return (m_end_t);
    }
    
    /**
     * Gets the current perfmon session elapsed time.
     *
     * @return              current perfmon elapsed time 
     * @exception IllegalStateException  if start or end equal 0
     */
    public long getElapsedTime()
        throws IllegalStateException {

        if ((m_start_count[INSTRUCTION_COUNTER] == 0) ||
            (m_end_count[INSTRUCTION_COUNTER] == 0)) {
            
            throw new 
                IllegalStateException("Invalid start/end sequence.");
        }
        return ((m_delta_t = m_end_t - m_start_t));
    }
    
    /**
     * All native methods are defined here
     * NOTE: functions defined in $(MIDP_WS)/src/share/native/perfmon.c
     *
     * @param     count     performance monitor counter type
     * @return              current system measurement for counter type 
     * @exception IllegalStateException  if start or end equal 0
     */
    static native int sysGetCounter(int count);
    
}
