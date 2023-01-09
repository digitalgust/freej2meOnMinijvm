/*
 * @(#)RecordStore.java	1.69 02/10/03 @(#)
 *
 * Portiona Copyright (c) 2000-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Copyright 2000 Motorola, Inc. All Rights Reserved.
 * This notice does not imply publication.
 */

package javax.microedition.rms; 

import com.sun.midp.rms.RecordStoreFile;

/**
 * A class representing a record store. A record store consists of a
 * collection of records which will remain persistent across multiple
 * invocations of the MIDlet. The platform is responsible for
 * making its best effort to maintain the integrity of the
 * MIDlet's record stores throughout the normal use of the
 * platform, including reboots, battery changes, etc.
 *
 * <p>Record stores are created in platform-dependent locations, which
 * are not exposed to the MIDlets. The naming space for record stores
 * is controlled at the MIDlet suite granularity. MIDlets within a
 * MIDlet suite are allowed to create multiple record stores, as long
 * as they are each given different names. When a MIDlet suite is
 * removed from a platform all the record stores associated with its
 * MIDlets will also be removed. MIDlets within a MIDlet suite can
 * access each other's record stores directly. New APIs in MIDP 2.0
 * allow for the explicit sharing of record stores if the MIDlet
 * creating the RecordStore chooses to give such permission.</p>
 *
 * <p> Sharing is accomplished through the ability to name a
 * RecordStore created by another MIDlet suite.</p>
 *
 * <P> RecordStores are uniquely named using the unique name of the
 * MIDlet suite plus the name of the RecordStore. MIDlet suites are
 * identified by the MIDlet-Vendor and MIDlet-Name attributes from the
 * application descriptor.</p>
 *
 * <p> Access controls are defined when RecordStores to be shared are
 * created. Access controls are enforced when RecordStores are
 * opened. The access modes allow private use or shareable
 * with any other MIDlet suite.</p>
 *
 * <p>Record store names are case sensitive and may consist of any
 * combination of between one and 32 Unicode characters
 * inclusive. Record store names must be unique within the scope of a
 * given MIDlet suite. In other words, MIDlets within a MIDlet suite
 * are not allowed to create more than one record store with the same
 * name, however a MIDlet in one MIDlet suite is allowed to have a
 * record store with the same name as a MIDlet in another MIDlet
 * suite. In that case, the record stores are still distinct and
 * separate.</p>
 *
 * <p>No locking operations are provided in this API. Record store
 * implementations ensure that all individual record store operations
 * are atomic, synchronous, and serialized, so no corruption will
 * occur with multiple accesses. However, if a MIDlet uses multiple
 * threads to access a record store, it is the MIDlet's responsibility
 * to coordinate this access or unintended consequences may result.
 * Similarly, if a platform performs transparent synchronization of a
 * record store, it is the platform's responsibility to enforce
 * exclusive access to the record store between the MIDlet and
 * synchronization engine.</p>
 *
 * <p>Records are uniquely identified within a given record store by
 * their recordId, which is an integer value. This recordId is used as
 * the primary key for the records. The first record created in a
 * record store will have recordId equal to one (1). Each subsequent
 * record added to a RecordStore will be assigned a recordId one
 * greater than the record added before it. That is, if two records
 * are added to a record store, and the first has a recordId of 'n',
 * the next will have a recordId of 'n + 1'. MIDlets can create other
 * sequences of the records in the RecordStore by using the
 * <code>RecordEnumeration</code> class.</p>
 *
 * <p>This record store uses long integers for time/date stamps, in
 * the format used by System.currentTimeMillis(). The record store is
 * time stamped with the last time it was modified. The record store
 * also maintains a <em>version</em> number, which is an integer that
 * is incremented for each operation that modifies the contents of the
 * RecordStore.  These are useful for synchronization engines as well
 * as other things.</p>
 *
 * @since MIDP 1.0
 */

public class RecordStore 
{
    /*  
     * RecordStore Constructors 
     */

    /**
     * MIDlets must use <code>openRecordStore()</code> to get 
     * a <code>RecordStore</code> object. If this constructor 
     * is not declared (as private scope), Javadoc (and Java) 
     * will assume a public constructor. 
     */ 
    private RecordStore() {
    }

    /**
     * Deletes the named record store. MIDlet suites are only allowed
     * to delete their own record stores. If the named record store is
     * open (by a MIDlet in this suite or a MIDlet in a different
     * MIDlet suite) when this method is called, a
     * RecordStoreException will be thrown.  If the named record store
     * does not exist a RecordStoreNotFoundException will be
     * thrown. Calling this method does NOT result in recordDeleted
     * calls to any registered listeners of this RecordStore.
     *
     * @param recordStoreName the MIDlet suite unique record store to
     * 		delete
     *
     * @exception RecordStoreException if a record store-related
     * 		exception occurred
     * @exception RecordStoreNotFoundException if the record store
     * 		could not be found
     */
    public static void deleteRecordStore(String recordStoreName) 
        throws RecordStoreException, RecordStoreNotFoundException 
    {
	String uidPath = RecordStoreFile.getUniqueIdPath(recordStoreName);
        // Check the record store cache for a db with the same name
	synchronized (dbCacheLock) {
	    RecordStore db;
	    for (int n = 0; n < dbCache.size(); n++) {
		db = (RecordStore)dbCache.elementAt(n);
		if (db.uniqueIdPath.equals(uidPath)) {
		    // cannot delete an open record store
		    throw new RecordStoreException("deleteRecordStore error:"
						   + " record store is"
						   + " still open");
		}
	    }
	    // this record store is not currently open
	    if (RecordStoreFile.exists(uidPath)) {
		boolean success = RecordStoreFile.deleteFile(uidPath);
		if (!success) {
		    throw new RecordStoreException("deleteRecordStore " +
						   "failed");
		}
	    } else {
	        throw new RecordStoreNotFoundException("deleteRecordStore " +
						       "error: file " +
						       "not found"); 
	    }
	}
    }
    
    /**
     * Open (and possibly create) a record store associated with the
     * given MIDlet suite. If this method is called by a MIDlet when
     * the record store is already open by a MIDlet in the MIDlet suite,
     * this method returns a reference to the same RecordStore object.
     *
     * @param recordStoreName the MIDlet suite unique name for the
     *          record store, consisting of between one and 32 Unicode
     *          characters inclusive.
     * @param createIfNecessary if true, the record store will be
     *		created if necessary
     *
     * @return <code>RecordStore</code> object for the record store
     *
     * @exception RecordStoreException if a record store-related
     *		exception occurred
     * @exception RecordStoreNotFoundException if the record store
     *		could not be found
     * @exception RecordStoreFullException if the operation cannot be
     *		completed because the record store is full
     * @exception IllegalArgumentException if
     *          recordStoreName is invalid
     */
    public static RecordStore openRecordStore(String recordStoreName, 
					      boolean createIfNecessary) 
        throws RecordStoreException, RecordStoreFullException, 
        RecordStoreNotFoundException 
    {
	String uidPath = RecordStoreFile.getUniqueIdPath(recordStoreName);
	synchronized (dbCacheLock) {

	    if (recordStoreName.length() > 32 ||
		recordStoreName.length() == 0) {
		throw new IllegalArgumentException();
	    }

	    // Cache record store objects and ensure that there is only
	    // one record store object in memory for any given record
	    // store file. This is good for memory use. This is NOT safe
	    // in the situation where multiple VM's may be executing code
	    // concurrently. In that case, you have to sync things through
	    // file locking or something similar.
	    
	    // Check the record store cache for a db with the same name
	    RecordStore db;	    
	    for (int n = 0; n < dbCache.size(); n++) {
		db = (RecordStore)dbCache.elementAt(n);
		if (db.uniqueIdPath.equals(uidPath)) {
		    db.opencount++;  // times rs has been opened
		    return db;  // return ref to cached record store
		}
	    }
	    
	    /*
	     * Record store not found in cache, so create it.
	     */
	    db = new RecordStore(uidPath, recordStoreName, createIfNecessary);

	    /*
	     * Now add the new record store to the cache
	     */
	    db.opencount = 1;
	    dbCache.addElement(db);
	    return db;
	}
    }

    /**
     * Open (and possibly create) a record store that can be shared
     * with other MIDlet suites. The RecordStore is owned by the
     * current MIDlet suite. The authorization mode is set when the
     * record store is created, as follows:
     *
     * <ul>
     * <li><code>AUTHMODE_PRIVATE</code> - Only allows the MIDlet
     *          suite that created the RecordStore to access it. This
     *          case behaves identically to
     *          <code>openRecordStore(recordStoreName,
     *          createIfNecessary)</code>.</li>
     * <li><code>AUTHMODE_ANY</code> - Allows any MIDlet to access the
     *          RecordStore. Note that this makes your recordStore
     *          accessible by any other MIDlet on the device. This
     *          could have privacy and security issues depending on
     *          the data being shared. Please use carefully.</li>
     * </ul>
     *
     * <p>The owning MIDlet suite may always access the RecordStore and
     * always has access to write and update the store.</p>
     *
     * <p> If this method is called by a MIDlet when the record store
     * is already open by a MIDlet in the MIDlet suite, this method
     * returns a reference to the same RecordStore object.</p>
     *
     * @param recordStoreName the MIDlet suite unique name for the
     *          record store, consisting of between one and 32 Unicode
     *          characters inclusive.
     * @param createIfNecessary if true, the record store will be
     * 		created if necessary
     * @param authmode the mode under which to check or create access.
     * 		Must be one of AUTHMODE_PRIVATE or AUTHMODE_ANY.
     *		This argument is ignored if the RecordStore exists.
     * @param writable true if the RecordStore is to be writable by
     *		other MIDlet suites that are granted access.
     *		This argument is ignored if the RecordStore exists.
     *
     * @return <code>RecordStore</code> object for the record store
     *
     * @exception RecordStoreException if a record store-related
     * 		exception occurred
     * @exception RecordStoreNotFoundException if the record store
     * 		could not be found
     * @exception RecordStoreFullException if the operation
     *		cannot be completed because the record store is full
     * @exception IllegalArgumentException if authmode or
     *          recordStoreName is invalid
     * @since MIDP 2.0
     */
    public static RecordStore openRecordStore(String recordStoreName,
					      boolean createIfNecessary,
					      int authmode,
					      boolean writable)
	throws RecordStoreException, RecordStoreFullException,
               RecordStoreNotFoundException
    {
	RecordStore rs = RecordStore.openRecordStore(recordStoreName, 
						     createIfNecessary);
	rs.setMode(authmode, writable);
	return rs;
    }
    
    /**
     * Open a record store associated with the named MIDlet suite.
     * The MIDlet suite is identified by MIDlet vendor and MIDlet
     * name.  Access is granted only if the authorization mode of the
     * RecordStore allows access by the current MIDlet suite.  Access
     * is limited by the authorization mode set when the record store
     * was created:
     *
     * <ul>
     * <li><code>AUTHMODE_PRIVATE</code> - Succeeds only if vendorName
     * 		and suiteName identify the current MIDlet suite; this
     * 		case behaves identically to
     *		<code>openRecordStore(recordStoreName,
     *		createIfNecessary)</code>.</li>
     * <li><code>AUTHMODE_ANY</code> - Always succeeds.
     *          Note that this makes your recordStore
     *          accessible by any other MIDlet on the device. This
     *          could have privacy and security issues depending on
     *          the data being shared. Please use carefully.
     *		Untrusted MIDlet suites are allowed to share data but
     *		this is not recommended. The authenticity of the
     *		origin of untrusted MIDlet suites cannot be verified
     *		so shared data may be used unscrupulously.</li>
     * </ul>
     *
     * <p> If this method is called by a MIDlet when the record store
     * is already open by a MIDlet in the MIDlet suite, this method
     * returns a reference to the same RecordStore object.</p>
     *
     * <p> If a MIDlet calls this method to open a record store from
     * its own suite, the behavior is identical to calling:
     * <code>{@link #openRecordStore(String, boolean)
     * openRecordStore(recordStoreName, false)}</code></p>
     *
     * @param recordStoreName the MIDlet suite unique name for the
     *          record store, consisting of between one and 32 Unicode
     *          characters inclusive.
     * @param vendorName the vendor of the owning MIDlet suite
     * @param suiteName the name of the MIDlet suite
     *
     * @return <code>RecordStore</code> object for the record store
     *
     * @exception RecordStoreException if a record store-related
     *		exception occurred
     * @exception RecordStoreNotFoundException if the record store
     * 		could not be found
     * @exception SecurityException if this MIDlet Suite is not
     *  	allowed to open the specified RecordStore.
     * @exception IllegalArgumentException if recordStoreName is
     *          invalid
     * @since MIDP 2.0
     */
    public static RecordStore openRecordStore(String recordStoreName,
					      String vendorName,
					      String suiteName)
	throws RecordStoreException, RecordStoreNotFoundException
    {
	if (vendorName == null || suiteName == null) {
	    throw new IllegalArgumentException("vendorName and " +
					       "suiteName must be " +
					       "non null");
	}
	synchronized (dbCacheLock) {
	    
	    if (recordStoreName.length() > 32 ||
		recordStoreName.length() == 0) {
		throw new IllegalArgumentException();
	    }
	    
	    // Cache record store objects and ensure that there is only
	    // one record store object in memory for any given record
	    // store file. This is good for memory use. This is NOT safe
	    // in the situation where multiple VM's may be executing code
	    // concurrently. In that case, you have to sync things through
	    // file locking or something similar.
	    
	    // Check the record store cache for a db with the same name
	    RecordStore db;	   
	    String uidPath = RecordStoreFile.getUniqueIdPath(vendorName, 
							     suiteName, 
							     recordStoreName);
	    for (int n = 0; n < dbCache.size(); n++) {
		db = (RecordStore)dbCache.elementAt(n);
		if (db.uniqueIdPath.equals(uidPath)) {
		    if (db.checkOwner() == false && 
			db.dbAuthMode == AUTHMODE_PRIVATE) {
			throw new SecurityException();
		    } else {
			db.opencount++;  // times rs has been opened
			return db;  // return ref to cached record store
		    }
		}
	    }
	    /*
	     * Record store not found in cache, so create it.
	     */
	    db = new RecordStore(uidPath, recordStoreName, false);
	    
	    /*
	     * Now add the new record store to the cache
	     */
	    db.opencount = 1;
	    dbCache.addElement(db);
	    
	    if (db.checkOwner() == false && 
		db.dbAuthMode == AUTHMODE_PRIVATE) {
		db.closeRecordStore();
		throw new SecurityException();
	    } else {
		return db;
	    }
	}
    }

    /**
     * Authorization to allow access only to the current MIDlet
     * suite. AUTHMODE_PRIVATE has a value of 0.
     */
    public final static int AUTHMODE_PRIVATE = 0;

    /**
     * Authorization to allow access to any MIDlet
     * suites. AUTHMODE_ANY has a value of 1.
     */
    public final static int AUTHMODE_ANY = 1;

    /**
     * Internal indicator for AUTHMODE_ANY with read only access
     * AUTHMODE_ANY_RO has a value of 2.
     */
    private final static int AUTHMODE_ANY_RO = 2;

    /**
     * Changes the access mode for this RecordStore. The authorization
     * mode choices are:
     *
     * <ul>
     * <li><code>AUTHMODE_PRIVATE</code> - Only allows the MIDlet
     *          suite that created the RecordStore to access it. This
     *          case behaves identically to
     *          <code>openRecordStore(recordStoreName,
     *          createIfNecessary)</code>.</li>
     * <li><code>AUTHMODE_ANY</code> - Allows any MIDlet to access the
     *          RecordStore. Note that this makes your recordStore
     *          accessible by any other MIDlet on the device. This
     *          could have privacy and security issues depending on
     *          the data being shared. Please use carefully.</li>
     * </ul>
     *
     * <p>The owning MIDlet suite may always access the RecordStore and
     * always has access to write and update the store. Only the
     * owning MIDlet suite can change the mode of a RecordStore.</p>
     *
     * @param authmode the mode under which to check or create access.
     * 		Must be one of AUTHMODE_PRIVATE or AUTHMODE_ANY.
     * @param writable true if the RecordStore is to be writable by
     *		other MIDlet suites that are granted access
     *
     * @exception RecordStoreException if a record store-related
     *		exception occurred
     * @exception SecurityException if this MIDlet Suite is not
     *		allowed to change the mode of the RecordStore
     * @exception IllegalArgumentException if authmode is invalid
     * @since MIDP 2.0
     */
    public void setMode(int authmode,
			boolean writable)
    throws RecordStoreException
    {
	synchronized (rsLock) {
	    if (checkOwner() == false) {
		throw new SecurityException();
	    } else if (authmode != AUTHMODE_PRIVATE &&
		       authmode != AUTHMODE_ANY) {
		throw new IllegalArgumentException();
	    } else {
		dbAuthMode = authmode;
		if ((authmode == AUTHMODE_ANY) && (writable == false)) {
		    dbAuthMode = AUTHMODE_ANY_RO;
		}
	    }
	    // flush record store header here
	    storeDBState();
	}
    }

    /**
     * This method is called when the MIDlet requests to have the
     * record store closed. Note that the record store will not
     * actually be closed until closeRecordStore() is called as many
     * times as openRecordStore() was called. In other words, the
     * MIDlet needs to make a balanced number of close calls as open
     * calls before the record store is closed.
     *
     * <p>When the record store is closed, all listeners are removed
     * and all RecordEnumerations associated with it become invalid.
     * If the MIDlet attempts to perform
     * operations on the RecordStore object after it has been closed,
     * the methods will throw a RecordStoreNotOpenException.
     *
     * @exception RecordStoreNotOpenException if the record store is
     *		not open
     * @exception RecordStoreException if a different record
     *		store-related exception occurred
     */
    public void closeRecordStore()
	throws RecordStoreNotOpenException, RecordStoreException 
    {
	synchronized (rsLock) {
	    synchronized (dbCacheLock) {
		checkOpen();
		/*
		 * Find the record store within the record store cache.
		 * A linear seagrch is OK assuming there won't be many 
		 * concurrently open record stores.
		 */
		RecordStore db = null;
		for (int n = 0; n < dbCache.size(); n++) {
		    db = (RecordStore)dbCache.elementAt(n);
		    if (db == this) {
			db.opencount--;
			break;
		    }
		}
		if (db.opencount <= 0) {  // free stuff - final close
		    dbCache.removeElement(db);
		    try {
			// closing now...no need to listen
			if (!recordListener.isEmpty()) {
			    recordListener.removeAllElements();
			}			
			// close native fd 
			if (dbFirstFreeBlockOffset != 0) {
			    compactRecords();  // compact before close
			    // truncate file to compacted size
			    dbraf.truncate(dbDataEnd);
			} 
			dbraf.close();
		    } catch (java.io.IOException ioe) {
			throw new RecordStoreException("error closing .db " +
						       "file");
		    } finally {
			dbraf = null;
			recHeadCache = null;
		    }
		}
	    }
	}
    }

    /**
     * Returns an array of the names of record stores owned by the
     * MIDlet suite. Note that if the MIDlet suite does not
     * have any record stores, this function will return null.
     *
     * The order of RecordStore names returned is implementation
     * dependent.
     *
     * @return array of the names of record stores owned by the
     * MIDlet suite. Note that if the MIDlet suite does not
     * have any record stores, this function will return null.
     */
    public static String[] listRecordStores() 
    {
        // static calls synchronize on dbCacheLock
        synchronized (dbCacheLock) {
	  String[] returnstrings = RecordStoreFile.listRecordStores();
	  return returnstrings;
	}
    }

    /**
     * Returns the name of this RecordStore.
     *
     * @return the name of this RecordStore
     *
     * @exception RecordStoreNotOpenException if the record store is not open
     */
    public String getName() 
	throws RecordStoreNotOpenException 
    {
	checkOpen();
	return recordStoreName;
    }

    /**
     * Each time a record store is modified (by
     * <code>addRecord</code>, <code>setRecord</code>, or
     * <code>deleteRecord</code> methods) its <em>version</em> is
     * incremented. This can be used by MIDlets to quickly tell if
     * anything has been modified.
     *
     * The initial version number is implementation dependent.
     * The increment is a positive integer greater than 0.
     * The version number increases only when the RecordStore is updated.
     *
     * The increment value need not be constant and may vary with each
     * update.
     *
     * @return the current record store version
     *
     * @exception RecordStoreNotOpenException if the record store is
     *            not open
     */
    public int getVersion() 
	throws RecordStoreNotOpenException 
    {
	checkOpen();
	return dbVersion;
    }

    /**
     * Returns the number of records currently in the record store.
     *
     * @return the number of records currently in the record store
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     */
    public int getNumRecords() 
	throws RecordStoreNotOpenException 
    {
	checkOpen();
	return dbNumLiveRecords;
    }
    
    /**
     * Returns the amount of space, in bytes, that the record store
     * occupies. The size returned includes any overhead associated
     * with the implementation, such as the data structures
     * used to hold the state of the record store, etc.
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     *
     * @return the size of the record store in bytes
     */
    public int getSize() 
	throws RecordStoreNotOpenException 
    {
	checkOpen();
	// return the file size of the record store file
	return dbDataEnd;
    }

    /**
     * Returns the amount of additional room (in bytes) available for
     * this record store to grow. Note that this is not necessarily
     * the amount of extra MIDlet-level data which can be stored,
     * as implementations may store additional data structures with
     * each record to support integration with native applications,
     * synchronization, etc.
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     *
     * @return the amount of additional room (in bytes) available for
     *          this record store to grow
     */
    public int getSizeAvailable() 
	throws RecordStoreNotOpenException 
    {
	checkOpen();
	int rv = RecordStoreFile.spaceAvailable() - 
	    DB_BLOCK_SIZE - DB_RECORD_HEADER_LENGTH;
	return (rv < 0) ? 0 : rv;
    }
    
    /**
     * Returns the last time the record store was modified, in the
     * format used by System.currentTimeMillis().
     *
     * @return the last time the record store was modified, in the
     * 		format used by System.currentTimeMillis()
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     */
    public long getLastModified() 
	throws RecordStoreNotOpenException 
    {
	checkOpen();

	return dbLastModified;
    }

    /**
     * Adds the specified RecordListener. If the specified listener
     * is already registered, it will not be added a second time.
     * When a record store is closed, all listeners are removed.
     *
     * @param listener the RecordChangedListener
     * @see #removeRecordListener
     */
    public void addRecordListener(RecordListener listener) 
    {
	synchronized (rsLock) {
	    if (!recordListener.contains(listener)) {
		recordListener.addElement(listener);
	    }
	}
    }

    /**
     * Removes the specified RecordListener. If the specified listener
     * is not registered, this method does nothing.
     *
     * @param listener the RecordChangedListener
     * @see #addRecordListener
     */
    public void removeRecordListener(RecordListener listener) 
    {
	synchronized (rsLock) {
	    recordListener.removeElement(listener);
	}
    }
    
    /**
     * Returns the recordId of the next record to be added to the
     * record store. This can be useful for setting up pseudo-relational
     * relationships. That is, if you have two or more
     * record stores whose records need to refer to one another, you can
     * predetermine the recordIds of the records that will be created
     * in one record store, before populating the fields and allocating
     * the record in another record store. Note that the recordId returned
     * is only valid while the record store remains open and until a call
     * to <code>addRecord()</code>.
     *
     * @return the recordId of the next record to be added to the
     *          record store
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception RecordStoreException if a different record
     *		store-related exception occurred
     */
    public int getNextRecordID() 
	throws RecordStoreNotOpenException, RecordStoreException 
    {
	checkOpen();
	return dbNextRecordID;
    }

    /**
     * Adds a new record to the record store. The recordId for this
     * new record is returned. This is a blocking atomic operation.
     * The record is written to persistent storage before the
     * method returns.
     *
     * @param data the data to be stored in this record. If the record
     * 		is to have zero-length data (no data), this parameter may be
     * 		null.
     * @param offset the index into the data buffer of the first
     * 		relevant byte for this record
     * @param numBytes the number of bytes of the data buffer to use
     * 		for this record (may be zero)
     *
     * @return the recordId for the new record
     *
     * @exception RecordStoreNotOpenException if the record store is
     * 		not open
     * @exception RecordStoreException if a different record
     * 		store-related exception occurred
     * @exception RecordStoreFullException if the operation cannot be
     * 		completed because the record store has no more room
     * @exception SecurityException if the MIDlet has read-only access
     * 		to the RecordStore
     */
    public int addRecord(byte[] data, int offset, int numBytes) 
        throws RecordStoreNotOpenException, RecordStoreException, 
	RecordStoreFullException
    {
	synchronized (rsLock) {
	    checkOpen();
	    if (!checkWritable()) {
		throw new SecurityException();
	    }
	    if ((data == null) && (numBytes > 0)) {
		throw new NullPointerException("illegal arguments: null " +
					       "data,  numBytes > 0");
	    }
	    // get recordId for new record, update db's dbNextRecordID
	    int id = dbNextRecordID++;

	    /*
	     * Find the offset where this record should be stored and
	     * seek to that location in the file. allocateNewRecordStorage()
	     * allocates the space for this record.
	     */	    
	    RecordHeader rh = allocateNewRecordStorage(id, numBytes);
	    try {
		if (data != null) {
		    rh.write(data, offset);
		}
	    } catch (java.io.IOException ioe) {
		throw new RecordStoreException("error writing new record " 
					       + "data");
	    }
	    
	    // Update the state changes to the db file.
	    dbNumLiveRecords++;
	    dbVersion++;
	    storeDBState();
	    
	    // tell listeners a record has been added
	    notifyRecordAddedListeners(id);
	    
	    // Return the new record id
	    return id;
	}
    }
    
    /**
     * The record is deleted from the record store. The recordId for
     * this record is NOT reused.
     *
     * @param recordId the ID of the record to delete
     *
     * @exception RecordStoreNotOpenException if the record store is
     * 		not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     * 		exception occurs
     * @exception SecurityException if the MIDlet has read-only access
     * 		to the RecordStore
     */
    public void deleteRecord(int recordId) 
	throws RecordStoreNotOpenException, InvalidRecordIDException, 
	    RecordStoreException 
    {
	synchronized (rsLock) {
	    checkOpen();
	    if (!checkWritable()) {
		throw new SecurityException();
	    }
	    RecordHeader rh = null; // record header	    
	    try {
		rh = findRecord(recordId, false);
		freeRecord(rh); // calls rh.store		
		recHeadCache.invalidate(rh.id);
	    } catch (java.io.IOException ioe) {
		throw new RecordStoreException("error updating file after" + 
					       " record deletion");
	    }	    
	    // update database header info and sync to file
	    dbNumLiveRecords--;
	    dbVersion++;
	    storeDBState();
	    // tell listeners a record has been deleted
	    notifyRecordDeletedListeners(recordId);
	}
    }

    /**
     * Returns the size (in bytes) of the MIDlet data available
     * in the given record.
     *
     * @param recordId the ID of the record to use in this operation
     *
     * @return the size (in bytes) of the MIDlet data available
     *          in the given record
     *
     * @exception RecordStoreNotOpenException if the record store is
     * 		not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     * 		exception occurs
     */
    public int getRecordSize(int recordId) 
	throws RecordStoreNotOpenException, InvalidRecordIDException, 
	    RecordStoreException 
    {
	synchronized (rsLock) {
	    checkOpen();
	    try {
		// throws InvalidRecordIDException
		RecordHeader rh = findRecord(recordId, true);
		return (rh.dataLenOrNextFree);
	    } catch (java.io.IOException ioe) {
	        throw new RecordStoreException("error reading record data");
	    }
	}
    }
    
    /**
     * Returns the data stored in the given record.
     *
     * @param recordId the ID of the record to use in this operation
     * @param buffer the byte array in which to copy the data
     * @param offset the index into the buffer in which to start copying
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *          exception occurs
     * @exception ArrayIndexOutOfBoundsException if the record is
     *          larger than the buffer supplied
     *
     * @return the number of bytes copied into the buffer, starting at
     *          index <code>offset</code>
     * @see #setRecord
     */
    public int getRecord(int recordId, byte[] buffer, int offset) 
	throws RecordStoreNotOpenException, InvalidRecordIDException, 
	RecordStoreException
    {
	synchronized (rsLock) {
	    checkOpen();
	    
	    RecordHeader rh;
	    try {
		// throws InvalidRecordIDException
		rh = findRecord(recordId, true);
		rh.read(buffer, offset);
	    } catch (java.io.IOException ioe) {
		throw new RecordStoreException("error reading record data");
	    }
	    return rh.dataLenOrNextFree;
	}
    }
    
    /**
     * Returns a copy of the data stored in the given record.
     *
     * @param recordId the ID of the record to use in this operation
     *
     * @exception RecordStoreNotOpenException if the record store is
     *		not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     *		exception occurs
     *
     * @return the data stored in the given record. Note that if the
     * 		record has no data, this method will return null.
     * @see #setRecord
     */
    public byte[] getRecord(int recordId) 
	throws RecordStoreNotOpenException, InvalidRecordIDException, 
	    RecordStoreException 
    {
	synchronized (rsLock) {
	    checkOpen();

	    int size = 0;
	    byte[] data = null;
	    try {
		// throws InvalidRecordIDException
		RecordHeader rh = findRecord(recordId, true); 
		if (rh.dataLenOrNextFree == 0) {
		    return null;
		}
		data = new byte[rh.dataLenOrNextFree];
		rh.read(data, 0);
	    } catch (java.io.IOException ioe) {
		throw new RecordStoreException("error reading record data");
	    } 
	    return data;
	}
    }

    /**
     * Sets the data in the given record to that passed in. After
     * this method returns, a call to <code>getRecord(int recordId)</code>
     * will return an array of numBytes size containing the data
     * supplied here.
     *
     * @param recordId the ID of the record to use in this operation
     * @param newData the new data to store in the record
     * @param offset the index into the data buffer of the first
     * 		relevant byte for this record
     * @param numBytes the number of bytes of the data buffer to use
     * 		for this record
     *
     * @exception RecordStoreNotOpenException if the record store is
     * 		not open
     * @exception InvalidRecordIDException if the recordId is invalid
     * @exception RecordStoreException if a general record store
     * 		exception occurs
     * @exception RecordStoreFullException if the operation cannot be
     * 		completed because the record store has no more room
     * @exception SecurityException if the MIDlet has read-only access
     * 		to the RecordStore
     * @see #getRecord
     */
    public void setRecord(int recordId, byte[] newData, 
			  int offset, int numBytes) 
	throws RecordStoreNotOpenException, InvalidRecordIDException, 
	    RecordStoreException, RecordStoreFullException 
    {
	synchronized (rsLock) {
	    checkOpen();
	    if (!checkWritable()) {
		throw new SecurityException();
	    }
	    
	    if ((newData == null) && (numBytes > 0)) {
		throw new NullPointerException();
	    }
	    
	    RecordHeader rh = null;
	    RecordHeader newrh = null;
	    
	    try {
		rh = findRecord(recordId, false); // throws InvalidRIDException
	    } catch (java.io.IOException ioe) {
		throw new RecordStoreException("error finding record data");
	    }
	    /*
	     * The size of the data and space allocated to the
	     * current record is known here, as is the new size.
	     * Determine if the new data will fit, or if this 
	     * record will have to be stored elsewhere.
	     */
	    if (numBytes <= rh.blockSize - DB_RECORD_HEADER_LENGTH) {
		/*
		 * The new data should fit within the existing record
		 * location in the file. Store the new data and
		 * patch up the record's header fields.
		 */
		int allocSize = getAllocSize(numBytes);
		if (rh.blockSize - allocSize >= 
		    DB_BLOCK_SIZE + DB_RECORD_HEADER_LENGTH) {
		    splitRecord(rh, allocSize); // sets rh.blockSize
	        }
		rh.dataLenOrNextFree = numBytes;		
		try {
		    rh.store(); // write the new record header
		    recHeadCache.insert(rh);  // add to cache
		
		    if (newData != null) {
			rh.write(newData, offset);
		    }
		} catch (java.io.IOException ioe) {
		    throw new RecordStoreException("error writing record" +
						   " data");
		}
	    } else {
		/*
		 * The new data is longer than the old data.  It needs to
		 * be relocated to elsewhere within <code>dbfile</code>. 
		 * Search the free list to see if there's a space where to
		 * store it.  Otherwise append it to the end of the file.
		 */

		freeRecord(rh); // calls rh.store()

		newrh = allocateNewRecordStorage(recordId, numBytes);

		try {
		    if (newData != null) {
			newrh.write(newData, offset);
			// NOTE: I/O exception leaves space allocated & unused
		    }
		} catch (java.io.IOException ioe) {
		    throw new RecordStoreException("error moving record " +
						   "data");
		}
	    }
	    
	    // update database header info and sync to file
	    dbVersion++;
	    storeDBState();
	    notifyRecordChangedListeners(recordId);
	}
    }

    /**
     * Returns an enumeration for traversing a set of records in the
     * record store in an optionally specified order.<p>
     *
     * The filter, if non-null, will be used to determine what
     * subset of the record store records will be used.<p>
     *
     * The comparator, if non-null, will be used to determine the
     * order in which the records are returned.<p>
     *
     * If both the filter and comparator is null, the enumeration
     * will traverse all records in the record store in an undefined
     * order. This is the most efficient way to traverse all of the
     * records in a record store.  If a filter is used with a null
     * comparator, the enumeration will traverse the filtered records
     * in an undefined order.
     *
     * The first call to <code>RecordEnumeration.nextRecord()</code>
     * returns the record data from the first record in the sequence.
     * Subsequent calls to <code>RecordEnumeration.nextRecord()</code>
     * return the next consecutive record's data. To return the record
     * data from the previous consecutive from any
     * given point in the enumeration, call <code>previousRecord()</code>.
     * On the other hand, if after creation the first call is to
     * <code>previousRecord()</code>, the record data of the last element
     * of the enumeration will be returned. Each subsequent call to
     * <code>previousRecord()</code> will step backwards through the
     * sequence.
     *
     * @param filter if non-null, will be used to determine what
     *          subset of the record store records will be used
     * @param comparator if non-null, will be used to determine the
     *          order in which the records are returned
     * @param keepUpdated if true, the enumerator will keep its enumeration
     *          current with any changes in the records of the record
     *          store. Use with caution as there are possible
     *          performance consequences. If false the enumeration
     *          will not be kept current and may return recordIds for
     *          records that have been deleted or miss records that
     *          are added later. It may also return records out of
     *          order that have been modified after the enumeration
     *          was built. Note that any changes to records in the
     *          record store are accurately reflected when the record
     *          is later retrieved, either directly or through the
     *          enumeration. The thing that is risked by setting this
     *          parameter false is the filtering and sorting order of
     *          the enumeration when records are modified, added, or
     *          deleted.
     *
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     *
     * @see RecordEnumeration#rebuild
     *
     * @return an enumeration for traversing a set of records in the
     *          record store in an optionally specified order
     */
    public RecordEnumeration enumerateRecords(RecordFilter filter, 
					      RecordComparator comparator, 
					      boolean keepUpdated) 
	throws RecordStoreNotOpenException 
    {
	checkOpen();
	return new RecordEnumerationImpl(this, filter, 
					 comparator, keepUpdated);
    }

    /*
     * Private Memory Management Methods
     */

    /**
     * Find the record header for a record <code>recordId</code>.
     *
     * @param recordId the id of the desired record header.
     * @param addToCache true if this record should be added to cache 
     *        if found.
     *
     * @return the record header for the given record, or null if 
     *         the record cannot be found.
     */
    private RecordHeader findRecord(int recordId, boolean addToCache)
	throws InvalidRecordIDException, java.io.IOException
    {
	RecordHeader rh;
	int offset;
	
	int cur_offset = dbFirstRecordOffset;
	// if no records exist, throw an exception
	if (cur_offset == 0) {
	    throw new InvalidRecordIDException();
	}
	
	// look for the record in the cache
	rh = recHeadCache.get(recordId);
	if (rh != null) {
	    return rh;
	}
	
	/*
	 * requested record header is NOT in cache...
	 * search through the linked list of records
	 * in the file. 
	 */
	rh = new RecordHeader();	
	while (cur_offset != 0) {
	    rh.load(cur_offset);
	    if (rh.id == recordId) {
		break;
	    } else {
		cur_offset = rh.nextOffset;
	    }
	} 
	
	if (cur_offset == 0) { 
	    // hit the end of the linked list w/o finding record.
	    throw new InvalidRecordIDException();
	}	
	if (addToCache) {
	    recHeadCache.insert(rh);
	}
	return rh;
    }
    

    /**
     * Return the block allocation size for a record with <code>numBytes
     * </code> data bytes.  This includes space for the record header
     * and is a multiple of <code>DB_BLOCK_SIZE</code>.
     *
     * @param numBytes number of data bytes that will be stored in record
     *
     * @return the amount of space to allocate for this record.
     */
    private int getAllocSize(int numBytes) {
	int rv;
	int pad;
	rv = DB_RECORD_HEADER_LENGTH + numBytes;
	pad = DB_BLOCK_SIZE - (rv % DB_BLOCK_SIZE);
	if (pad != DB_BLOCK_SIZE) {
	    rv += pad;
	}
	return rv;
    }



    /**
     * Returns a new record header for record <code>id</code> large
     * enough to hold <code>dataSize</code> bytes of record data.
     *
     * Picks a free block using a first fit strategy that is 
     * large enough for a record header and the associated
     * record data.  The block will be a multiple of DB_BLOCK_SIZE.
     *
     * @param id the record id to assign to the returned record header.
     * @param dataSize length of record data that will be 
     *        stored with this record.
     *
     * @return a new record header 
     *         the record store's backing file.
     */

    private RecordHeader allocateNewRecordStorage(int id, int dataSize)
	throws RecordStoreException, RecordStoreFullException
    {
	int allocSize = getAllocSize(dataSize);
	boolean foundBlock = false;

	/*
	 * Traverse the free block linked list in the file, looking
	 * for the first fit
	 */
	RecordHeader block = new RecordHeader();
	try {
	    int offset = dbFirstFreeBlockOffset;
	    while (offset != 0) {
		block.load(offset);
		// If block is big enough, use it.
		if (block.blockSize >= allocSize) {
		    foundBlock = true;
		    break; // use this free block
		}
		offset = block.dataLenOrNextFree; // next free block
	    }
	} catch (java.io.IOException ioe) {
	    throw new RecordStoreException("error finding first fit block");
	}
	
	if (foundBlock == false) {
	    
	    /*
	     * No free block was found that would hold this record, so
	     * find the last (biggest offset) record in the file and
	     * append this record after it.
	     */
	    
	    // Is there room to grow the file?
	    if (RecordStoreFile.spaceAvailable() < allocSize) {
		throw new RecordStoreFullException();
	    }
	    
	    block = new RecordHeader(dbDataEnd, id, 
				     dbFirstRecordOffset,
				     allocSize, dataSize);
	    try {
		block.store();
	    } catch (java.io.IOException ioe) {
		throw new RecordStoreException("error writing "+
					       "new record data"); 
	    }
	    dbFirstRecordOffset = dbDataEnd;
	    dbDataEnd += allocSize;
	} else { 
	    // block is where the new record should be stored
	    if (block.id != -1) {
		throw new RecordStoreException("ALLOC ERR " + block.id +
					       " is not a free block!");
	    }
	
	    removeFreeBlock(block);  // remove from free block list	    
	    
	    block.id = id;
	    if (block.blockSize - allocSize >=
		DB_BLOCK_SIZE + DB_RECORD_HEADER_LENGTH) {
		splitRecord(block, allocSize); // sets block.blockSize
	    }
	    block.dataLenOrNextFree = dataSize;
	    try {
		block.store(); 
	    } catch (java.io.IOException ioe) {
		throw new RecordStoreException("error writing free block "+
					       "after alloc"); 
	    }
	}
	// add new record to cache
	recHeadCache.insert(block);
	return block;
    }


    /**
     * Splits a free block off the tail end of a large
     * record block which contains extra free space.
     * After calling this method, the caller must call 
     * <code>storeDBState</code>.
     *
     * On return, <code>recHead.blockSize</code> will contain
     * allocSize.
     *
     * @param recHead the current record header
     * @param allocSize the size that <code>recHEad</code>
     *        will have as its <code>blockSize</code> variable
     *        when the call returns.
     *
     * @exception RecordStoreException if there is an error
     *            splitting the record
     */
    private void splitRecord(RecordHeader recHead, int allocSize)
	throws RecordStoreException
    {
	RecordHeader newfb;	
	int extraSpace = recHead.blockSize - allocSize;
	int oldBlockSize = recHead.blockSize;
	recHead.blockSize = allocSize;
	
	// only split records inside the linked list
	if (recHead.offset != dbFirstRecordOffset) {
	    int fboffset = recHead.offset + allocSize;
	    newfb = new RecordHeader(fboffset, -1, recHead.offset,
				     extraSpace, 0);
	    try {
		freeRecord(newfb); // write new free block to disk
		RecordHeader prh = new RecordHeader(recHead.offset +
						    oldBlockSize);
		prh.nextOffset = fboffset;
		prh.store();
		recHeadCache.invalidate(prh.id);
		storeDBState();
	    } catch (java.io.IOException ioe) {
		throw new RecordStoreException("splitRecord error");
	    }
	} else { 	    
	    // drop free space at the end of the file
	    dbDataEnd = recHead.offset + recHead.blockSize;
	}
    }
	  
    
    /**
     * Free a record into the Free list.  
     * Turns the RecordHeader block <code>rh</code> into a free 
     * block, then adds it to the free block linked list.  
     *
     * After calling this method the caller must call 
     * <code>storeDBState</code>.
     *
     * @param rh RecordHeader of record to make into a free block
     *
     * @exception RecordStoreException if there is an IO error updating the 
     *            free list
     */
    private void freeRecord(RecordHeader rh) 
	throws RecordStoreException
    {
	if (rh.offset == dbFirstRecordOffset) {
	    // don't put free blocks at the end of the record file
	    dbFirstRecordOffset = rh.nextOffset;
	    dbDataEnd = rh.offset;
	} else {
	    rh.id = -1;  // indicate this is a free block
	    rh.dataLenOrNextFree = dbFirstFreeBlockOffset;
	    // insert this new free block at front of free list
	    dbFirstFreeBlockOffset = rh.offset;
	    try {
		rh.store();
	    } catch (java.io.IOException ioe) {
		throw new RecordStoreException("free record failed");
	    }
	}
    }

    /**
     * Remove a free block from the free block linked list
     *
     * @param blockToFree record header for the free block to remove
     * 
     * @exception recordStoreException if error occurs during the
     *            update.
     */
    private void removeFreeBlock(RecordHeader blockToFree) 
	throws RecordStoreException 
    {
	RecordHeader block = new RecordHeader();
	RecordHeader prev = new RecordHeader();
	RecordHeader tmp = null;
	try {
	    int offset = dbFirstFreeBlockOffset;
	    while (offset != 0) {
		block.load(offset);
		if (block.offset == blockToFree.offset) {
		    if (block.id != -1) {
			throw new RecordStoreException("removeFreeBlock id" +
						       " is not -1");
		    }
		    if (prev.offset == 0) {
			// Set next free block as new freelist head
			dbFirstFreeBlockOffset = block.dataLenOrNextFree;
		    } else {
			/*
			 * Update previous block's pointer to the
			 * block this block was pointing to
			 */
			prev.dataLenOrNextFree = block.dataLenOrNextFree;
			prev.store();
		    }
		}
		offset = block.dataLenOrNextFree;
		// avoid creating lots of garbage!
		tmp = prev;
		prev = block;
		block = tmp;
	    }
	} catch (java.io.IOException ioe) {
	    throw new RecordStoreException("removeFreeBlock block not found");
	}	
    }

    /**
     * Helper method that stores the internal state variables
     * into the record store file.
     *
     * checkopen should have been called.  will not work
     * if dbraf is not open
     *
     * Updates dbLastModified time to current system time
     */
    private void storeDBState() throws RecordStoreException
    {
	try {
	    // set modification time
	    dbLastModified = System.currentTimeMillis();
	    // Capture the db state into the byte array
	    RecordStore.putInt(dbNumLiveRecords, dbState, RS_NUM_LIVE);
	    RecordStore.putInt(dbAuthMode, dbState, RS_AUTHMODE); 
	    RecordStore.putInt(dbVersion, dbState, RS_VERSION);
	    RecordStore.putInt(dbNextRecordID, dbState, RS_NEXT_ID);
	    RecordStore.putInt(dbFirstRecordOffset, dbState, RS_REC_START);
	    RecordStore.putInt(dbFirstFreeBlockOffset, dbState, RS_FREE_START);
	    RecordStore.putLong(dbLastModified, dbState, RS_LAST_MODIFIED);
	    RecordStore.putInt(dbDataStart, dbState, RS_DATA_START);
	    RecordStore.putInt(dbDataEnd, dbState, RS_DATA_END);
	    // Write the state to the db file
	    dbraf.seek(SIGNATURE_LENGTH); // skip RS header 8 bytes
	    int numbytes = DB_INIT.length - SIGNATURE_LENGTH;
	    dbraf.write(dbState, SIGNATURE_LENGTH, numbytes);
	} catch (java.io.IOException ioe) {
	    throw new RecordStoreException("error writing record store " +
					   "attributes");
	}
    }

    /*
     * Package Private Methods
     */

    /**
     * Get the open status of this record store.  (Package accessable
     * for use by record enumeration objects.)
     *
     * @return true if record store is open, false otherwise. 
     */
    boolean isOpen() {
	if (dbraf == null) {
	    return false;
	}
	return true;
    }

    /*
     * Private Utility Methods
     */

    /**
     * Throws a RecordStoreNotOpenException if the RecordStore
     * is closed.  (A RecordStore is closed if the RecordStoreFile
     * instance variable <code>dbraf</code> is null. 
     *
     * @exception RecordStoreNotOpenException if RecordStore is closed
     */
    private void checkOpen() throws RecordStoreNotOpenException
    {
	if (dbraf == null) {
	    throw new RecordStoreNotOpenException();
	}
    }

    /**
     * Notifies all registered listeners that a record changed.
     *
     * @param recordId the record id of the changed record.
     */
    private void notifyRecordChangedListeners(int recordId)
    {
	for (int i = 0; i < recordListener.size(); i++) {
	    RecordListener rl = (RecordListener)recordListener.elementAt(i);
	    rl.recordChanged(this, recordId);
	}
    }

    /**
     * Notifies all registered listeners that a record was added.
     *
     * @param recordId the record id of the added record.
     */
    private void notifyRecordAddedListeners(int recordId)
    {
	for (int i = 0; i < recordListener.size(); i++) {
	    RecordListener rl = (RecordListener)recordListener.elementAt(i);
	    rl.recordAdded(this, recordId);
	}
    }
    
    /**
     * Notifies all registered listeners that a record was deleted.
     *
     * @param recordId the record id of the changed record.
     */
    private void notifyRecordDeletedListeners(int recordId)
    {
	for (int i = 0; i < recordListener.size(); i++) {
	    RecordListener rl = (RecordListener)recordListener.elementAt(i);
	    rl.recordDeleted(this, recordId);
	}
    }


    /**
     * A convenience method for converting a byte array into
     * an int (assumes big-endian byte ordering).
     *
     * @param data the byte array returned from the database.
     * @param offset the offset into the array of the first byte to start from.
     *
     * @return an int corresponding to the first four bytes 
     *         of the array passed in.
     */
    static int getInt(byte[] data, int offset)
    {
	int r = data[offset++];
	r = (r << 8) | ((int)(data[offset++]) & 0xff);
	r = (r << 8) | ((int)(data[offset++]) & 0xff);
	r = (r << 8) | ((int)(data[offset++]) & 0xff);
	return r;
    }

    /**
     * A convenience method for converting a byte array into
     * a long (assumes big-endian byte ordering).
     *
     * @param data the byte array returned from the database.
     * @param offset the offset into the array of the first byte to start from.
     * @return a long corresponding to the first eight bytes 
     *         of the array passed in.
     */
    static long getLong(byte[] data, int offset)
    {
	long r = data[offset++];
	r = (r << 8) | ((long)(data[offset++]) & 0xff);
	r = (r << 8) | ((long)(data[offset++]) & 0xff);
	r = (r << 8) | ((long)(data[offset++]) & 0xff);
	r = (r << 8) | ((long)(data[offset++]) & 0xff);
	r = (r << 8) | ((long)(data[offset++]) & 0xff);
	r = (r << 8) | ((long)(data[offset++]) & 0xff);
	r = (r << 8) | ((long)(data[offset++]) & 0xff);
	return r;
    }


    /**
     * A convenience method for converting an integer into
     * a byte array.
     *
     * @param i the integer to turn into a byte array.
     * @param data a place to store the bytes of <code>i</code>.
     * @param offset starting point within <code>data<code> to 
     *        store <code>i</code>.
     *
     * @return the number of bytes written to the array.
     */
    static int putInt(int i, byte[] data, int offset)
    {
	data[offset++] = (byte)((i >> 24) & 0xff);
	data[offset++] = (byte)((i >> 16) & 0xff);
	data[offset++] = (byte)((i >> 8) & 0xff);
	data[offset] = (byte)(i & 0xff);
	return 4;
    }


    /**
     * A convenience method for converting a long into
     * a byte array.
     *
     * @param l the <code>long<code> to turn into a byte array.
     * @param data a place to store the bytes of <code>l</code>.
     * @param offset Starting point within <code>data</code> to 
     *        store <code>l</code>.
     *
     * @return the number of bytes written to the array.
     */
    static int putLong(long l, byte[] data, int offset)
    {
	data[offset++] = (byte)((l >> 56) & 0xff);
	data[offset++] = (byte)((l >> 48) & 0xff);
	data[offset++] = (byte)((l >> 40) & 0xff);
	data[offset++] = (byte)((l >> 32) & 0xff);
	data[offset++] = (byte)((l >> 24) & 0xff);
	data[offset++] = (byte)((l >> 16) & 0xff);
	data[offset++] = (byte)((l >> 8) & 0xff);
	data[offset] = (byte)(l & 0xff);
	return 8;
    }

    /**
     * Returns all of the recordId's currently in the record store.
     *
     * MUST be called after obtaining rsLock, e.g in a 
     * <code>synchronized (rsLock) {</code> block.
     *
     * @return an array of the recordId's currently in the record store
     *         or null if the record store is closed.
     */
    int[] getRecordIDs() 
    {
	if (dbraf == null) { // lower overhead than checkOpen()
 	    return null;
	}

	int index = 0;
	int[] tmp = new int[dbNumLiveRecords];
	int offset = dbFirstRecordOffset; // start at beginning of file
	RecordHeader rh = new RecordHeader(); 

	try {
	    while (offset != 0) {
		rh.load(offset);
		if (rh.id > 0) {
		    tmp[index++] = rh.id;
		}
		offset = rh.nextOffset;
	    }
	} catch (java.io.IOException ioe) {
	    return null;
	}
	return tmp;
    }

    /**
     * Remove free blocks from the record store and compact records
     * with data into as small a space in <code>rsFile</code> as
     * possible.  Operates from smallest to greatest offset in 
     * <code>rsFile</code>, copying data in chunks towards the 
     * beginning of the file, and updating record store meta-data
     * as it progresses.
     *
     * Warning: This is a slow operation that scales linearly
     * with rsFile size.  
     *
     * @exception RecordStoreNotOpenException if this record store 
     *            is closed
     * @exception RecordStoreException if an error occurs during record 
     *            store compaction
     */

    private void compactRecords() throws RecordStoreNotOpenException, 
	RecordStoreException {
	int offset = dbDataStart;  // after record store header structure
	int target = 0;
	int bytesLeft;
	int numToMove;
	byte[] chunkBuffer = new byte[DB_COMPACTBUFFER_SIZE];
	
	RecordHeader rh = new RecordHeader();
	
	int prevRec = 0;
	while (offset < dbDataEnd) {
	    try {
		rh.load(offset);
	    } catch (java.io.IOException ioe) {
		// NOTE - should throw some exception here
		System.out.println("Unexpected IOException in CompactRS!");
	    }
	    
	    if (rh.id == -1) {          // a free block
		if (target == 0) {
		    target = offset; 
		} // else skip free block
		offset += rh.blockSize;
	    } else {                    // a record block
		if (target == 0) {
		    // No move needed so far.
		    prevRec = offset;
		    offset += rh.blockSize;
		} else {
		    int old_offset = target;
		    // Move a record back in the file
		    rh.offset = target;
		    rh.nextOffset = prevRec;
		    try {
			rh.store();
			offset += DB_RECORD_HEADER_LENGTH;
			target += DB_RECORD_HEADER_LENGTH;
			bytesLeft = (rh.blockSize - DB_RECORD_HEADER_LENGTH);
			while (bytesLeft > 0) {
			    if (bytesLeft < DB_COMPACTBUFFER_SIZE) {
				numToMove = bytesLeft;
			    } else {
				numToMove = DB_COMPACTBUFFER_SIZE;
			    }
			    dbraf.seek(offset);
			    dbraf.read(chunkBuffer, 0, numToMove);
			    dbraf.seek(target);
			    dbraf.write(chunkBuffer, 0, numToMove);
			    offset += numToMove;
			    target += numToMove;
			    bytesLeft -= numToMove;
			} 
		    } catch (java.io.IOException ioe) {
			// NOTE - should throw some exception here
			System.out.println("Unexpected IOException " +
					   "in CompactRS!");
		    }
		    prevRec = old_offset;
		}
	    }
	}
	if (rh.offset != 0) {
	    dbDataEnd = rh.offset + rh.blockSize;
	}
	dbFirstRecordOffset = rh.offset;
	dbFirstFreeBlockOffset = 0;
	storeDBState();
    }

    /*
     * Internal Classes  
     */

    /**
     * A class representing a RecordHeader, which may be the start of a 
     * record block or a free block.  (In a free block, only the 
     * <code> NextOffset </code> and <code> BlockSize </code> fields
     * are valid.)  Currently it is a conveinience structure, with
     * fields visible and directly modifyable by the enclosing
     * RecordStore class.
     */

    private class RecordHeader {
	/*
	 * Each record is laid out in the file system as follows:
	 * Bytes - Usage
	 * 00-03 - Record ID
	 * 04-07 - Next record offset
	 * 08-11 - Record size...total (in bytes, big endian).
	 * 12-15 - Length of Data.  This is stored separately for the case 
	 *         where a record has gotten smaller.
	 * 16-xx - Data
	 *
	 * Each free block is laid out in the file system as follows:
	 * Bytes - Usage
	 * 00-03 - Free block ID (set to -1)
	 * 04-07 - Next record offset (not used by free block)
	 * 08-11 - Free block size in bytes (in bytes, big endian)
	 * 12-15 - Next free block offset (zero if this is the last free block)
	 * 16-xx - Data (not used by free block)
	 */

	/** REC_ID offset */
	private static final int REC_ID = 0;
	/** NEXT_OFFSET offset */
	private static final int NEXT_OFFSET = 4;
	/** BLOCK_SIZE offset */
	private static final int BLOCK_SIZE = 8;
	/** 
	 * data length offset of record block or next free block 
	 * offset of free block 
	 */
	private static final int DATALEN_OR_NEXTFREE = 12;
	/** DATA_OFFSET offset (offset to record data) */
	private static final int DATA_OFFSET = 16; 
	
	/** offset of the record block or free block in RSFile */
	int offset;
	/** record id -or- -1 if free block */
	int id;
	/** next record offset */
	int nextOffset;
	/** record size -or- free block size */
	int blockSize;  
	/** record length -or- next free block offset */
	int dataLenOrNextFree;
	
	/**
	 * default RecordHeader constructor - creates an empty header
	 */
	RecordHeader() { }
	
	/**
	 * Creates a new RecordHeader and initializes it with data
	 * read from a RecordStoreFile at offset <code> offset </code>.
	 *
	 * @param _offset seek offset in RecordStoreFile of desired 
	 *        RecordHeader.
	 *
	 * @exception IOException if there is an error reading the
	 *            underlying RecordStoreFile.
	 */
	RecordHeader(int _offset) throws java.io.IOException {
	    load(_offset);
	}

	/**
	 * Creates a new RecordHeader and initializes it with data 
	 * provided in the parameters.  This RecordHeader will not
	 * be stored (on disk/in persistant storage) until the 
	 * <code> store </code> method is called.
	 *
	 * @param _offset offset in RecordStoreFile where this 
	 *        RecordHeader should be stored.
	 * @param _id record id of this new RecordHeader
	 *
	 * @param next_offset next RecordHeader in linked list of
	 *        records.
	 * @param size total size of the storage block allocated for
	 *        this record or free block.
	 * @param len_or_free length of data in this record (may be shorter
	 *        than <code>size</code> -or- next free block if this
	 *        header is a free block header
	 */
	RecordHeader(int _offset, int _id, int next_offset, 
		     int size, int len_or_free) {
	    offset = _offset;
	    id = _id;
	    nextOffset = next_offset;
	    blockSize = size;
	    dataLenOrNextFree = len_or_free;
	}

	/**
	 * Re-uses a RecordHeader and initializes it with data
	 * read from a RecordStoreFile at offset <code> offset </code>.
	 *
	 * @param _offset seek offset in RecordStoreFile of desired 
	 *        RecordHeader.
	 *
	 * @exception IOException if there is an error reading the
	 *            underlying RecordStoreFile
	 */	
	void load(int _offset) throws java.io.IOException {
	    offset = _offset;
	    
	    // read rec header from file.
	    dbraf.seek(offset);
	    dbraf.read(recHeadBuf, 0, DB_RECORD_HEADER_LENGTH);
	    
	    id = RecordStore.getInt(recHeadBuf, REC_ID);
	    nextOffset = RecordStore.getInt(recHeadBuf, NEXT_OFFSET);
	    blockSize = RecordStore.getInt(recHeadBuf, BLOCK_SIZE);
	    dataLenOrNextFree = RecordStore.getInt(recHeadBuf, 
					       DATALEN_OR_NEXTFREE);
	}
	
	/**
	 * Flushes an in memory RecordHeader instance to storage in a 
	 * a RecordStoreFile at <code>offset</code>.
	 * @exception IOException if there is an error writing the
	 *            underlying RecordStoreFile
	 */	
	void store() throws java.io.IOException {
	    RecordStore.putInt(id, recHeadBuf, REC_ID);
	    RecordStore.putInt(nextOffset, recHeadBuf, NEXT_OFFSET);
	    RecordStore.putInt(blockSize, recHeadBuf, BLOCK_SIZE);
	    RecordStore.putInt(dataLenOrNextFree, recHeadBuf, 
			       DATALEN_OR_NEXTFREE);
	    
	    // write record header;
	    dbraf.seek(offset);
	    dbraf.write(recHeadBuf, 0, DB_RECORD_HEADER_LENGTH);
	}

	/**
	 * Reads data associated with this record from storage (a
	 * RecordStoreFile) at <code>offset</code>.
	 *
	 * Assumes CALLER has ensured <code>dataLenOrNextFree</code> is set 
	 * correctly, and that <code>dataLenOrNextFree</code> bytes will 
	 * fit into the array.
	 *
	 * @param buf data is read into this buffer.
	 * @param _offset position in <code>buf</code> to start reading
	 *        data into.
	 *
	 * @return number of bytes read.
	 *
	 * @exception IOException if there is an error reading the
	 *            underlying RecordStoreFile
	 */	
	int read(byte[] buf, int _offset) throws java.io.IOException {
	    dbraf.seek(offset + DATA_OFFSET);
	    return dbraf.read(buf, _offset, dataLenOrNextFree);
	}

	/**
	 * Writes data associated with this record to storage (a 
	 * RecordStoreFile) at <code>offset</code>.
	 *
	 * Assumes CALLER has ensured <code>dataLenOrNextFree</code> is
	 * set correctly.
	 *
	 * @param buf data to store in this record.
	 * @param _offset point in <code>buf</code> to begin write from.
	 *
	 * @exception IOException if there is an error writing the
	 *            underlying RecordStoreFile.
	 */	
	void write(byte[] buf, int _offset) throws java.io.IOException {
	    dbraf.seek(offset + DATA_OFFSET);
	    dbraf.write(buf, _offset, dataLenOrNextFree);
	}
    }


    /**
     * RecordHeaderCache providing a per RecordStore, in memory cache of 
     * recent  RecordHeader lookups in order to absorb in many cases the
     * need to search for records on disk.  Since the on disk data model
     * for record storage is a linked list, this can improve performance
     * when a record is accessed frequently.  
     *
     * (Currently implemented in a simple, direct mapped way.  Could be
     * modified to be random replacement/linear lookup, etc.  These design
     * considerations should be determined by measuring cache performance
     * on representative RMS use cases.)
     */
    private class RecordHeaderCache {

	/** a cache of RecordHeader objects */
	private RecordHeader[] mCache;
	
	/**
	 * Returns a new RecordHeaderCache able to hold up to <code>
	 * size</code> record headers.
	 *
	 * @param size max number of RecordHeader objects allowed 
	 *        in <code>mCache</code>.
	 */
	RecordHeaderCache(int size) {
	    mCache = new RecordHeader[size];
	}
	
	/**
	 * Returns a RecordHeader for record <code>rec_id</code> or 
	 * null if the desired record header is not in the cache
	 *
	 * @param rec_id record id of the desired record header.
	 *
	 * @return a RecordHeader object for record 
	 *         <code>rec_id</code>.
	 */
	RecordHeader get(int rec_id) {
	    int idx = rec_id % mCache.length;
	    RecordHeader rh = (RecordHeader)mCache[idx];
	    if ((mCache[idx] != null) && (mCache[idx].id != rec_id)) {
 		return null;
	    }
	    return rh;
	}
	
	/**
	 * Inserts a new RecordHeader into the cache.
	 *
	 * @param rh a RecordHeader to add to the cache.
	 */
	void insert(RecordHeader rh) {
	    int idx = rh.id % mCache.length;
	    mCache[idx] = rh;
	}
	
	/**
	 * Removes a RecordHeader from the cache if it exists,
	 * otherwise does nothing.  Used in the case that the
	 * cached RecordHeader is no longer valid.
	 *
	 * @param rec_id the record ID of the RecordHeader to 
	 *        invalidate
	 */
	void invalidate(int rec_id) {
	    if (rec_id > 0) {
		int idx = rec_id % mCache.length;
		if ((mCache[idx] != null) && 
		    (mCache[idx].id == rec_id)) {
		mCache[idx] = null;
		}
	    }
	}
    }

    /**
     * Apps must use <code>openRecordStore()</code> to get
     * a <code>RecordStore</code> object. This constructor
     * is used internally for creating RecordStore objects.
     *
     * <code>dbCacheLock</code> must be held before calling
     * this constructor.
     *
     * @param uidPath unique storage id for this record store
     * @param recordStoreName a string to name the record store
     * @param create if true, create the record store if it doesn't exist
     *
     * @exception RecordStoreException if something goes wrong setting up
     *            the new RecordStore.
     * @exception RecordStoreNotFoundException if can't find the record store
     *            and create is set to false.
     * @exception RecordStoreFullException if there is no room in storage
     *            to create a new record store
     */
    
    private RecordStore(String uidPath, String recordStoreName, boolean create)
        throws RecordStoreException, RecordStoreNotFoundException
    {
	this.recordStoreName = recordStoreName;
	this.uniqueIdPath = uidPath;
	
	recHeadCache = new RecordHeaderCache(CACHE_SIZE);
	rsLock = new Object();
	recordListener = new java.util.Vector(3); 
	
	boolean exists = RecordStoreFile.exists(uidPath);

	// Check for errors between app and record store existance.
	if (!create && !exists) {
	    throw new RecordStoreNotFoundException("cannot find record " 
						   + "store file");
	}
	/* 
	 * If a new RecordStore will be created in storage, 
	 * check to see if the space required is available.
	 */
	if (create && !exists) {
	    int space = RecordStoreFile.spaceAvailable();
	    if (space - DB_INIT.length < 0) { 
		throw new RecordStoreFullException();
	    }
	}

	// Create a RecordStoreFile for storing the record store.
	try {
	    dbraf = new RecordStoreFile(uidPath);
	    /*
	     * At this point we've opened the RecordStoreFile.  If we
	     * created a new record store, initialize the db attributes.
	     */
	    if (create && !exists) {
		// Initialize record store attributes
		dbraf.seek(RS_SIGNATURE);
		// Update the timestamp
		RecordStore.putLong(System.currentTimeMillis(),
				    DB_INIT, RS_LAST_MODIFIED);
		RecordStore.putInt(48, DB_INIT, RS_DATA_START);
		RecordStore.putInt(48, DB_INIT, RS_DATA_END);
		dbraf.write(DB_INIT);
	    } else {
		/*
		 * Create a buffer and read the database attributes
		 * Read the record store attributes. Set up internal state.
		 */
		byte[] buf = new byte[DB_INIT.length];
		dbraf.seek(RS_SIGNATURE);
		dbraf.read(buf);
		/*
		 * Verify that the file is actually a record store
		 * by verifying the record store "signature."
		 */
		for (int i = 0; i < SIGNATURE_LENGTH; i++) {
		    if (buf[i] != DB_INIT[i]) {
			throw new RecordStoreException("invalid record "+
						       "store contents");
		    }
		}

		// Convert byte array to internal state variables.
		dbNumLiveRecords = RecordStore.getInt(buf, RS_NUM_LIVE);
		dbVersion = RecordStore.getInt(buf, RS_VERSION);
		dbAuthMode = RecordStore.getInt(buf, RS_AUTHMODE);
		dbNextRecordID = RecordStore.getInt(buf, RS_NEXT_ID);
		dbFirstRecordOffset = RecordStore.getInt(buf, RS_REC_START);
		dbFirstFreeBlockOffset = RecordStore.getInt(buf, 
							    RS_FREE_START);
		dbLastModified = RecordStore.getLong(buf, RS_LAST_MODIFIED);
		dbDataStart = RecordStore.getInt(buf, RS_DATA_START);
		dbDataEnd = RecordStore.getInt(buf, RS_DATA_END);
	    }
	    
	} catch (java.io.IOException ioe) {
	    try {
		if (dbraf != null) {
		    dbraf.close();
		}
	    } catch (java.io.IOException ioe2) { 
		// ignore exception within exception block
	    } finally {
		dbraf = null;
	    }
	    throw new RecordStoreException("error opening record store " + 
					   "file");
	} 
    }
    
    /** 
     * Internal method to check record store owner vs. the vendor and suite
     * of the currently running midlet
     * 
     * @return <code>true</code> if vendor and suite name both match, 
     * <code>false</code> otherwise
     */
    private boolean checkOwner() {

	// varies with currently running midlet suite
	String myUid = RecordStoreFile.getUniqueIdPath(recordStoreName);
	// fixed at the time dbraf is created
	String rsfUid = dbraf.getUniqueIdPath();

	if (myUid.equals(rsfUid)) {
	    return true;
	} else {
	    return false;
	}
    }
    
    /** 
     * Internal method to determine if writing to this record store
     * is allowed for the calling MIDlet.  Returns <code>true</code>
     * if <code>checkOwner()</code> returns <code>true</code> or
     * <code>dbAuthMode</code> == 1 when <code>checkOwner()</code>
     * returns <code>false</code>.
     * 
     * @return <code>true</code> if the record modification request
     * should be allowed, <code>false</code> otherwise
     */
    private boolean checkWritable() {
	if (checkOwner()) {
	    return true;
	} else {
	    if (dbAuthMode == AUTHMODE_ANY) { // Read-Write mode
		return true;
	    }
	}
	return false;
    }

    /** pre initialized RecordStore header structure */
    private static final byte[] DB_INIT = {
	(byte)'m', (byte)'i', (byte)'d', (byte)'p', 
	(byte)'-', (byte)'r', (byte)'m', (byte)'s',
	0, 0, 0, 0, // num live records
	0, 0, 0, 0, // AUTHMODE_PRIVATE by default
	0, 0, 0, 0, // version
	0, 0, 0, 1, // next record id
	0, 0, 0, 0, // first record offset
	0, 0, 0, 0, // first free-space offset
	0, 0, 0, 0, // last modified (1st half)
	0, 0, 0, 0, // last modified (2nd half)
	0, 0, 0, 0, // start of data offset
	0, 0, 0, 0  // end of data offset
    };

    /** length of the signature string in bytes */
    private static final int SIGNATURE_LENGTH = 8;
    
    /** size of a per record meta-data object */
    private static final int DB_RECORD_HEADER_LENGTH = 16;

    /** 
     * storage space allocated in multiples of <code>DB_BLOCK_SIZE</code>,
     * which can not be smaller than DB_RECORD_HEADER_LENGTH and
     * must be a multiple of DB_RECORD_HEADER_LENGTH
     */
    private static final int DB_BLOCK_SIZE = 16; 

    /** size of the buffer for compacting record store */
    private static final int DB_COMPACTBUFFER_SIZE = 64;
    
    /** cache of open RecordStore instances */
    private static java.util.Vector dbCache = new java.util.Vector(3);

    /** lock to protect static dbcache state */
    private static final Object dbCacheLock = new Object();
    
    /** name of this record store */
    private String recordStoreName;

    /** unique storage id for this record store */
    private String uniqueIdPath;

    /** number of open instances of this record store */
    private int opencount;
    
    /** RecordStoreFile where this record store is stored */
    private RecordStoreFile dbraf;
    
    /** lock used to synchronize this record store */
    Object rsLock;

    /** recordListeners of this record store */
    private java.util.Vector recordListener;
    
    /** cache of record headers */
    private RecordHeaderCache recHeadCache;

    /** number of direct mapped cache entries */
    private static int CACHE_SIZE = 8;

    /** static buffer used in loading/storing RecordHeader data */
    private static byte[] recHeadBuf = new byte[DB_RECORD_HEADER_LENGTH];

    /*
     * This implementation assumes (and enforces) that there is only
     * one instance of a RecordStore object for any given database file 
     * in the file system. This assumption is enforceable when there 
     * is only one VM running, but will lead to data corruption problems
     * if multiple VM's are used to read/write into the same database.
     *
     * As a consequence of this assumption, the following database
     * attributes are read from the RecordStoreFile when the RecordStore
     * is opened, maintained in memory for performance efficiency, and
     * reflected back to the file only when necessary for the sake of
     * error resilience.
     */

    /** next record's id */
    private int dbNextRecordID = 1;
    
    /** record store version */
    private int dbVersion;	

    /** 
     * authorization mode of this record store 
     * 0:  AUTHMODE_PRIVATE,  Read/Write
     * 1:  AUTHMODE_ANY,      Read/Write
     * 2:  AUTHMODE_ANY_RO,   Read-Only
     */
    private int dbAuthMode;
    
    /** count of live records */
    private int dbNumLiveRecords;

    /** time record store was last modified (in milliseconds */
    private long dbLastModified;
    
    /** offset of first record */
    private int dbFirstRecordOffset;
    
    /** offset of first free block */
    private int dbFirstFreeBlockOffset; 

    /** offset of the first data block */
    private int dbDataStart = 48;

    /** offset of the last data block */
    private int dbDataEnd = 48;

    /** static buffer used in loading/storing dbState */
    private static byte[] dbState = new byte[DB_INIT.length];
	    

    /*
     * The layout of the database file is as follows:
     *
     * Bytes - Usage
     * 00-07 - Signature = 'midp-rms'
     * 08-11 - Number of live records in the database (big endian)
     * 12-15 - Authmode and Writable state info
     * 16-19 - Database "version" - a monotonically increasing revision 
     *         number (big endian)
     * 20-23 - Next record ID to use (big endian)
     * 24-27 - First record offset (bytes from beginning of file - big endian)
     * 28-31 - First free-block offset (bytes from beginning of file - big 
     *         endian)
     * 32-39 - Last modified (64-bit long, big endian, milliseconds since 
     *         jan 1970)
     * 40-43 - Start of Data storage
     * 44-47 - End of Data storage
     * 48-xx - Record storage
     */

    /** RS_SIGNATURE offset */
    private static final int RS_SIGNATURE = 0;
    /** RS_NUM_LIVE offset */
    private static final int RS_NUM_LIVE = 8;
    /** RS_AUTHMODE offset */
    private static final int RS_AUTHMODE = 12;
    /** RS_VERSION offset */
    private static final int RS_VERSION = 16;
    /** RS_NEXT_ID offset */
    private static final int RS_NEXT_ID = 20;
    /** RS_REC_START offset */
    private static final int RS_REC_START = 24;
    /** RS_FREE_START offset */
    private static final int RS_FREE_START = 28;
    /** RS_LAST_MODIFIED offset */
    private static final int RS_LAST_MODIFIED = 32;
    /** RS_START_OF_DATA offset */
    private static final int RS_DATA_START = 40;
    /** RS_END_OF_DATA offset */
    private static final int RS_DATA_END = 44;

}
