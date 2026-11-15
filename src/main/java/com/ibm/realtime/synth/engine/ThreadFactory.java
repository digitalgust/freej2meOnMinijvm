/*
 * (C) Copyright IBM Corp. 2005, 2008
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ibm.realtime.synth.engine;

import static com.ibm.realtime.synth.utils.Debug.*;

/**
 * A centralized class for creation of threads. If available, real time threads
 * will be used.
 * 
 * @author florian
 */
public class ThreadFactory {

	protected static boolean useRTSJ = false;

	public static boolean DEBUG_THREAD_FACTORY = false;

	private static final boolean USE_PRIORITY = true;

	private static boolean bHasRealtimeThread = false;
	private static boolean bCouldSetRealtimeThreadPriority = false;
	
	
	/**
	 * Create a thread that excutes the given runner. If priority is larger than
	 * 7, a realtime thread is tried to be used. The thread immediately starts
	 * execution.
	 * 
	 * @param runner the Runnable to run in the new thread
	 * @param name the name of the thread, usually for debugging purposes only
	 * @param priority the priority from 0 (lowest) to 28 (highest)
	 * @return the thread object
	 */
	public static Thread createThread(Runnable runner, String name, int priority) {
		Thread t = createJavaThread(runner, name, priority);
		t.start();
		return t;
	}

	/**
	 * Given a priority from 0 to 28, return a priority level suitable for
	 * Thread.setPriority.
	 * 
	 * @param priority the priority in the range 0..28
	 * @return the Java Thread priority
	 */
	private static int priority2javaPriority(int priority) {
		int javaPrio;
		if (priority >= 28) {
			javaPrio = Thread.MAX_PRIORITY;
		} else if (priority <= 0) {
			javaPrio = Thread.MIN_PRIORITY;
		} else {
			javaPrio = Thread.MIN_PRIORITY
					+ (((Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) * priority) / 28);
		}
		return javaPrio;
	}

	private static Thread createJavaThread(Runnable runner, String name,
			int priority) {
		Thread res = new Thread(runner);
		res.setName(name);
		res.setDaemon(true);
		int javaPriority = priority2javaPriority(priority);
		res.setPriority(javaPriority);
		if (DEBUG_THREAD_FACTORY) {
			debug("Created NON-RTSJ thread '" + name + "' logical priority "
					+ priority + "; Java priority " + javaPriority);
		}
		return res;
	}

	public static void setUseRTSJ(boolean value) { useRTSJ = false; }

	public static boolean couldSetRealtimeThreadPriority() { return false; }

	public static boolean hasRealtimeThread() { return false; }

	/**
	 * Create a realtime thread.
	 * 
	 * @param priority 0..28
	 * @return the real time thread, or null on error
	 */
}
