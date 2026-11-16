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
package com.ibm.realtime.synth.utils;

public class Debug {

	public static boolean DEBUG_MASTER_SWITCH = false;
	public static boolean DEBUG_SHOW_ERRORS = false;

	public static final void debug(String s) {
		if (DEBUG_MASTER_SWITCH) out(s);
	}

	public static final void debugNoNewLine(String s) {
		if (DEBUG_MASTER_SWITCH) outNoNewLine(s);
	}

	public static final void debug(Throwable e) {
		if (DEBUG_MASTER_SWITCH) {
			e.printStackTrace();
			out(e.toString());
		}
	}

	//private static final long startTime = System.nanoTime();
	
	public static final void out(String s) {
		//System.out.println(""+((System.nanoTime()-startTime)/1000000)+" "+s);
		//System.out.println(s);
		//System.out.flush();
		System.out.println(s);
	}

	public static final void outNoNewLine(String s) {
		System.out.print(s);
		System.out.flush();
	}

	public static final void error(String s) {
		if (DEBUG_SHOW_ERRORS) {
			out(s);
		}
	}

	public static final void error(Throwable e) {
		if (DEBUG_SHOW_ERRORS) {
			e.printStackTrace();
			out(e.getClass().getSimpleName().toString()+": "+e.toString());
		}
	}

	/**
	 * Format a floating point number with 5 decimals.
	 * 
	 * @param d the number to convert to string
	 * @return the string with the number d with 3 decimals.
	 */
	public static final String format5(double d) {
		return "" + (Math.rint(d * 100000.0) / 100000.0);
	}

	/**
	 * Format a floating point number with 3 decimals.
	 * 
	 * @param d the number to convert to string
	 * @return the string with the number d with 3 decimals.
	 */
	public static final String format3(double d) {
		return "" + (Math.rint(d * 1000.0) / 1000.0);
	}

	/**
	 * Format a floating point number with 2 decimals.
	 * 
	 * @param d the number to convert to string
	 * @return the string with the number d with 3 decimals.
	 */
	public static final String format2(double d) {
		return "" + (Math.rint(d * 100.0) / 100.0);
	}

	/**
	 * Format a floating point number with 1 decimal.
	 * 
	 * @param d the number to convert to string
	 * @return the string with the number d with 1 decimals.
	 */
	public static final String format1(double d) {
		return "" + (Math.rint(d * 10.0) / 10.0);
	}
	
	public static final String hexString(long l, int digits) {
		String res = Long.toHexString(l).toUpperCase();
		while (res.length() < digits) res="0"+res;
		return res;
	}
	
	/**
	 * @return a string with the byte size with a suitable unit, bytes, KB, etc. 
	 */
	public static String getFriendlyByteSize(long bytes) {
		long threshold = 2 << 10; 
		if (bytes < 4*threshold) {
			return Long.toString(bytes)+" bytes";
		} 
		threshold = threshold << 10; 
		if (bytes < 4*threshold) {
			return format2(bytes / 1024.0)+" KB";
		}
		threshold = threshold << 10; 
		if (bytes < 4*threshold) {
			return format2(bytes / (1024.0*1024.0))+" MB";
		}
		threshold = threshold << 10; 
		if (bytes < 4*threshold) {
			return format2(bytes / (1024.0*1024.0*1024.0))+" GB";
		}
		return format2(bytes / (1024.0*1024.0*1024.0*1024.0))+" TB";
	}
}
