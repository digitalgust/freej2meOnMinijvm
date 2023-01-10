/*
 * @(#)RecordStoreFile.java	1.40 02/10/03 @(#)
 *
 * Copyright (c) 2000-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.midp.rms;

import java.io.IOException;

import java.util.Vector;

import javax.microedition.io.Connector;

import com.sun.midp.security.SecurityToken;

import com.sun.midp.midlet.Scheduler;
import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.io.j2me.storage.File;

/**
 * A RecordStoreFile is a file abstraction layer between a 
 * a RecordStore and an underlying persistent storage mechanism.
 * The underlying storage methods are provided by the 
 * RandomAccessStream and File classes.
 *
 * RecordStoreFile confines the namespace of a record store to 
 * the scope of the MIDlet suite of its creating application.
 * It also ensures unicode recordstore names are ascii filesystem safe.
 *
 * The RecordStore class can be implemented directly using the 
 * RandomAccessStream and File classes.  However, 
 * RecordStoreFile served as the java/native code boundry for 
 * RMS in the MIDP 1.0 release.  It exists now for 
 * backwards compatibility with older ports.  
 */
public class RecordStoreFile {
    
    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken;
    
    /** Easily recognize record store files in the file system */
    private static final String dbExtension = ".db";
    
    /** Stream to read/write record store data to */
    private RandomAccessStream recordStream;

    /** 
     * MIDlet suite and vendor specific storage path 
     * used as a unique identifier for a record store
     */
    private String myStoragePath;

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public static void initSecurityToken(SecurityToken token) {
	if (classSecurityToken != null) {
	    return;
	}
	classSecurityToken = token;
    }

    /**
     * Constructs a new RecordStoreFile instance.
     *
     * This process involves a few discrete steps and concludes with
     * with opening a RandomAccessStream that this RecordStoreFile 
     * instance will use for persistant storage.
     *
     * The steps in constructing a RecordStoreFile instance are:
     * <ul>
     *  <li>The storage path for the desired MIDlet suite
     *  is acquired in argument <code>uidPath</code>.  The caller
     *  must get this path using the <code>getUniqueIdPath()</code>
     *  method before calling this constructor.
     * 
     *  <li>This result is then connected with a new 
     *  <code>RandomAccessStrem</code> where record data for this 
     *  instance is stored..
     * </ul>
     *
     * @param uidPath unique identifier path for this
     *        <code>RecordStoreFile</code> object.
     *
     * @exception IOException if there is an error opening the file.
     */
    public RecordStoreFile(String uidPath) 
	throws IOException 
    {
	RandomAccessStream newStream;
	myStoragePath = uidPath;

	newStream = new RandomAccessStream(classSecurityToken);
	newStream.connect(myStoragePath, Connector.READ_WRITE);
	recordStream = newStream;
    }

    /**
     * Returns a storage system unique string for this record store file
     * based on the current vendor and suite of the running MIDlet.
     * <ul>
     *  <li>The native storage path for the desired MIDlet suite
     *  is acquired from the Scheduler.
     * 
     *  <li>The <code>filename</code> arg is converted into an ascii 
     *  equivalent safe to use directly in the underlying
     *  file system and appended to the native storage path.  See the
     *  com.sun.midp.io.j2me.storage.File.unicodeToAsciiFilename() 
     *  method for conversion details.
     *
     *  <li>Finally a ".db" extension is appeded to the file name.
     * <ul>  
     * @param fileName name of the record store
     *
     * @return a unique identifier for this record store file
     */
    public static String getUniqueIdPath(String fileName) {
	return getStoragePath(fileName);
    }
    
    /**
     * Returns a storage system unique string for this record store file
     * based on the vendor, suite, and record store name passed in.
     *
     * @param vendorName name of the vendor of the MIDlet suite
     * @param suiteName name of the MIDlet suite
     * @param fileName name of the record store
     *
     * @return a unique identifier for this record store file
     */
    public static String getUniqueIdPath(String vendorName, String suiteName, 
					 String fileName) {
	return getStoragePath(vendorName, suiteName, fileName);
    }

    /**
     * Get the path of this object when it was created
     *
     * @return the storage path of this <code>RecordStoreFile</code>
     */
    public String getUniqueIdPath() {
	return myStoragePath;
    }
    
    /**
     * Looks to see if the storage file for record store
     * identified by <code>uidPath</code> exists
     *
     * @param uidPath the unique identifier for this record store file
     *
     * @return true if the file exists, false if it does not.
     */
    public static boolean exists(String uidPath) {
	File file;
	file = new File(classSecurityToken);
	return file.exists(uidPath);
    }

    
    /**
     * Removes the storage file for record store <code>filename</code>
     * if it exists.
     *
     * @param uidPath unique identifier path to the file to delete.
     *
     * @return true if successful or false if an IOException occurred
     *         internally.
     */
    public static boolean deleteFile(String uidPath)
    {
	File file;
	file = new File(classSecurityToken);
	try {
	    file.delete(uidPath);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }
    
    /**
     * Sets the position within <code>recordStream</code> to
     * <code>pos</code>.  This will implicitly grow
     * the underlying stream if <code>pos</code> is made greater
     * than the current length of the storage stream.
     *
     * @param pos position within the file to move the current_pos 
     *        pointer to.
     *
     * @exception IOException if there is a problem with the seek.
     */
    public void seek(int pos) throws IOException
    {
	recordStream.setPosition(pos);
    }
    
    /**
     * Write all of <code>buf</code> to <code>recordStream</code>.
     *
     * @param buf buffer to read out of.
     *
     * @exception IOException if a write error occurs.
     */
    public void write(byte[] buf) throws IOException
    {
	write(buf, 0, buf.length);
    }
  
    /**
     * Write <code>buf</code> to <code>recordStream</code>, starting
     * at <code>offset</code> and continuing for <code>numBytes</code>
     * bytes.
     *
     * @param buf buffer to read out of.
     * @param offset starting point write offset, from beginning of buffer.
     * @param numBytes the number of bytes to write.
     *
     * @exception IOException if a write error occurs.
     */
    public void write(byte[] buf, int offset, int numBytes) throws IOException
    {
	recordStream.writeBytes(buf, offset, numBytes);
    }
    
    /**
     * Read up to <code>buf.length</code> into <code>buf</code>.
     *
     * @param buf buffer to read in to.
     *
     * @return the number of bytes read.
     *
     * @exception IOException if a read error occurs.
     */
    public int read(byte[] buf) throws IOException
    {
	return read(buf, 0, buf.length);
    }

    /**
     * Read up to <code>buf.length</code> into <code>buf</code>
     * starting at offset <code>offset</code> in <code>recordStream
     * </code> and continuing for up to <code>numBytes</code> bytes.
     *
     * @param buf buffer to read in to.
     * @param offset starting point read offset, from beginning of buffer.
     * @param numBytes the number of bytes to read.
     *
     * @return the number of bytes read.
     *
     * @exception IOException if a read error occurs.
     */
    public int read(byte[] buf, int offset, int numBytes) throws IOException
    {
	return recordStream.readBytes(buf, offset, numBytes);
    }
  
    /**
     * Disconnect from <code>recordStream</code> if it is
     * non null.  May be called more than once without error.
     *
     * @exception IOException if an error occurs closing 
     *            <code>recordStream</code>.
     */
    public void close() throws IOException
    {
	// close recordStream if it exists
	if (recordStream != null) {
	    recordStream.disconnect();
	    recordStream = null;
	}
    }

    /**
     * Sets the length of this <code>RecordStoreFile</code> 
     * <code>size</code> bytes.  If this file was previously
     * larger than <code>size</code> the extra data is lost.
     *
     * <code>size</code> must be <= the current length of
     * <code>recordStream</code>
     *
     * @param size new size for this file.
     *
     * @exception IOException if an error occurs, or if
     * <code>size</code> is less than zero.
     */

    public void truncate(int size) throws IOException
    {
	if (recordStream != null) {    
	    recordStream.truncate(size);
	}    
    }

    /**
     * Returns an array of the names of record stores owned by the 
     * MIDlet suite. Note that if the MIDlet suite does not 
     * have any record stores, this function will return NULL. 
     *
     * @return an array of record store names. 
     */ 
    public static String[] listRecordStores() {
        return listRecordStoresForSuite(new File(classSecurityToken),
            getStoragePath(null), false);
    }

    /**
     * Returns an array of the names of record stores owned by the 
     * MIDlet suite. Note that if the MIDlet suite does not 
     * have any record stores, this function will return NULL. 
     *
     * @param storage persisent storage access object
     * @param suiteStorageRoot storage root name of the suite
     * @param rawNames if true, raw filenames will be output instead of
     *  processed record store names.
     *
     * @return an array of record store names. 
     */ 
    private static String[] listRecordStoresForSuite(File storage,
                                                     String suiteStorageRoot,
                                                     boolean rawNames) {
	Vector files;
	Vector names;
	String file;
	String asciiName;
	
	files = storage.filenamesThatStartWith(suiteStorageRoot);
	names = new Vector();
	
	// work through list of strings from the directory
	for (int i = 0; i < files.size(); i++) {
	    file = (String)files.elementAt(i);
	    if (file.endsWith(dbExtension)) {
                if (rawNames) {
                    names.addElement(file);
                } else {
                    /*
                     * some or all of the strings in foo may be encoded 
                     * into a system specific format.  decode them before
                     * adding to names.
                     */
                    asciiName = file.substring(suiteStorageRoot.length(), 
					   file.length() - 3);
                    names.addElement(File.asciiFilenameToUnicode(asciiName));
                }
	    }
	}

	if (names.size() == 0) {
	    return null;
	}
	
	String[] rv = new String[names.size()];
	names.copyInto(rv);
	return rv;
    }
    
    /**
     * Remove all the Record Stores for a suite.
     *
     * @param token security token with MIDP internal permisison
     * @param suiteStorageRoot storage root name of the suite
     */ 
    public static void removeRecordStoresForSuite(SecurityToken token,
            String suiteStorageRoot) {

        File storage;
	String[] filenames;
	
	storage = new File(token);
        filenames = listRecordStoresForSuite(storage, suiteStorageRoot, true);
        if (filenames == null) {
            return;
        }

        for (int i = 0; i < filenames.length; i++) {
            try {
                storage.delete(filenames[i]);
            } catch (IOException ioe) {
                // move on to the next suite
            }
        }
    }
    
    /**
     * Returns true if the suite has created at least one record store.
     *
     * @param suiteStorageRoot storage root name of the suite
     *
     * @return true if the suite has at least one record store
     */ 
    public static boolean suiteHasRmsData(String suiteStorageRoot) {
	File storage = new File(classSecurityToken);
	Vector files = storage.filenamesThatStartWith(suiteStorageRoot);

	for (int i = 0; i < files.size(); i++) {
	    String file = (String)files.elementAt(i);
	    if (file.endsWith(dbExtension)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Approximation of remaining space in storage.
     *
     * Usage Warning:  This may be a slow operation if
     * the platform has to look at the size of each file
     * stored in the MIDP memory space and include its size
     * in the total.
     *
     * @return the aproximate space available to grow the
     *         record store in bytes.
     */
    public static int spaceAvailable()
    {
	return new File(classSecurityToken).getBytesAvailableForFiles();
    }

    /** 
     * Given a null argument this helper functions returns a 
     * path to where record stores should be stored for the
     * MIDlet suite of the calling MIDlet process.  If <code>name
     * </code> is non null it returns the full path of that
     * record store in the file system:
     * <storage_path><ascii_converted_'name'>.db
     * 
     * @param name name of target record store, or null if only the
     *        storage path for the current MIDlet suite is desired.
     *
     * @return the relative path to where record store files for the
     *         current MIDlet suite are stored if <code>name</code> 
     *         is null, or the complete system path for storage
     *         of the record store <code>name</code>.
     */
    private static String getStoragePath(String name)
    {
	String str;
	MIDletSuite mSuite;
	StringBuffer path;
	
	mSuite = Scheduler.getScheduler().getMIDletSuite();
	
	// MIDletSuite msuite should not be null.
	str = mSuite.getStorageRoot();
	if (name != null) {
	    path = new StringBuffer(str);
	    // convert the unicode filename into a system acceptable string
	    path.append(File.unicodeToAsciiFilename(name));
	    path.append(dbExtension);
	    str = path.toString();
	}
	return str;
    }
    
    /**
     * If <code>name</code>, <code>suite</code>, and <code>vendor</code> 
     * are all non null, returns the full path of that record store
     * in the file system:
     * <storage_path><ascii_converted_'name'>.db
     *
     * @param vendor vendor of target record store
     * @param suite suite of target record store
     * @param name name of target record store
     *
     * @return the complete system path for storage
     *         of the record store <code>name</code>.
     */
    private static String getStoragePath(String vendor, String suite, 
					 String name) {
	String str = File.getStorageRoot();
	StringBuffer path = new StringBuffer(str);
	if (vendor != null && suite != null) { 
	    path.append(File.unicodeToAsciiFilename(vendor));
	    path.append('_');
	    path.append(File.unicodeToAsciiFilename(suite));
	    path.append('_');
	} 
	if (name != null) {
	    path.append(File.unicodeToAsciiFilename(name));
	    path.append(dbExtension);
	    str = path.toString();
	}
	return str;
    }
}
