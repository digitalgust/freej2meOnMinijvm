/*
 * @(#)BasicPlayer.java	1.69 02/09/11 @(#)
 *
 * Copyright (c) 1996-2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package com.sun.mmedia;

import java.io.*;
import java.util.Vector;
import javax.microedition.io.*;
import javax.microedition.media.*;
import java.util.Hashtable;
import javax.microedition.media.control.*;

/**
 * BasicPlayer provides basic implementation for the Player methods.
 * Many of the methods call do&lt;method&gt; to do the actual work that can
 * be overridden by subclasses.
 */
public abstract class BasicPlayer implements Player, VolumeControl {

    /**
     * global player id 
     */
    private static int pcount = -1;

    /**
     * hastable to map playerID to instances
     */
    private static Hashtable mplayers = new Hashtable(4);

    /**
     * lock object 
     */
    private static Object idLock = new Object();

    /**
     * the locator of this player
     */
    private String locator;

    /**
     * the state of this player
     */
    int state = UNREALIZED;

    /**
     * the loopCount of this player
     */
    int loopCountSet = 1, loopCount;

    /**
     * the flag to indicate whether the Player is currently paused at EOM.
     * If true, the Player will seek back to the beginning when
     * restarted.
     */
    boolean EOM = false;

    /**
     * the flag to indicate looping after EOM.
     */
    boolean loopAfterEOM = false;

    /**
     * this player's playerlisteners
     */
    Vector listeners = new Vector(2);

    /**
     * Asynchronous event mechanism.
     */
    EvtQ evtQ = null;

    /**
     * event queue lock obj
     */
    Object evtLock = new Object();

    /**
     * player ID of this player
     */
    protected int pID = 0;

    /**
     * the default constructor
     */
    public BasicPlayer() {
	synchronized (idLock) {
	    pcount = (pcount+1) % 32767;
	    pID = pcount;
	}
	mplayers.put(new Integer(pID), this);
    }

    
    /**
     * Check to see if the Player is closed.  If the
     * unrealized boolean flag is true, check also to
     * see if the Player is UNREALIZED.
     *
     * @param unrealized the flag whether to check the unrealized state.
     */
    protected void chkClosed(boolean unrealized) {
	if (state == CLOSED || (unrealized && state == UNREALIZED)) { 
	    throw new IllegalStateException("The Player is " +
			(state == CLOSED ? "closed" : "unrealized"));
	}
    }


    /**
     * Set the number of times the <code>Player</code> will loop
     * and play the content.
     * <p>
     * By default, the loop count is one.  That is, once started,
     * the <code>Player</code> will start playing from the current
     * media time to the end of media once.
     * <p>
     * If the loop count is set to N where N is bigger than one,
     * starting the <code>Player</code> will start playing the
     * content from the current media time to the end of media.
     * It will then loop back to the beginning of the content
     * (media time zero) and play till the end of the media.
     * The number of times it will loop to the beginning and
     * play to the end of media will be N-1.
     * <p>
     * Setting the loop count to 0 is invalid.  An
     * <code>IllegalArgumentException</code> will be thrown.
     * <p>
     * Setting the loop count to -1 will loop and play the content
     * indefinitely.
     * <p>
     * If the <code>Player</code> is stopped before the preset loop
     * count is reached either because <code>stop</code> is called,
     * calling <code>start</code> again will
     * resume the looping playback from where it was stopped until it
     * fully reaches the preset loop count.
     * <p>
     * An <i>END_OF_MEDIA</i> event will be posted
     * every time the <code>Player</code> reaches the end of media.
     * If the <code>Player</code> loops back to the beginning and
     * starts playing again because it has not completed the loop
     * count, a <i>STARTED</i> event will be posted.
     *
     * @param count indicates the number of times the content will be
     * played.  1 is the default.  0 is invalid.  -1 indicates looping
     * indefintely.
     * @exception IllegalArgumentException Thrown if the given
     * count is invalid.
     * @exception IllegalStateException Thrown if the
     * <code>Player</code> is in the <i>STARTED</i>
     * or <i>CLOSED</i> state.
     */
    public void setLoopCount(int count) 
	throws IllegalArgumentException, IllegalStateException {
	chkClosed(false);
	if (state == STARTED)
	    throw new IllegalStateException("setLoopCount");
	if (count == 0 || count < -1)
	    throw new IllegalArgumentException("setLoopCount");
	loopCountSet = count;
	loopCount = count;
    }


    /**
     * Constructs portions of the <code>Player</code> without
     * acquiring the scarce and exclusive resources.
     * This may include examining media data and may
     * take some time to complete.
     * <p>
     * When <code>realize</code> completes successfully,
     * the <code>Player</code> is in the
     * <i>REALIZED</i> state.
     * <p>
     * If <code>realize</code> is called when the <code>Player</code> is in
     * the <i>REALIZED</i>, <i>PREFETCHTED</i> or <i>STARTED</i> state,
     * the request will be ignored.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @exception MediaException Thrown if the <code>Player</code> cannot
     * be realized.
     * @exception SecurityException Thrown if the caller does not
     * have security permission to realize the <code>Player</code>.
     *
     */
    public synchronized void realize() throws MediaException {
	chkClosed(false);

	if (state >= REALIZED)
	    return;

	doRealize();
	state = REALIZED;
    }

    /**
     * The worker method to realize the player.
     *
     */
    abstract protected void doRealize() throws MediaException;


    /**
     * Acquires the scarce and exclusive resources
     * and processes as much data as necessary
     * to reduce the start latency.
     * <p>
     * When <code>prefetch</code> completes successfully,
     * the <code>Player</code> is in
     * the <i>PREFETCHED</i> state.
     * <p>
     * If <code>prefetch</code> is called when the <code>Player</code>
     * is in the <i>UNREALIZED</i> state,
     * it will implicitly call <code>realize</code>.
     * <p>
     * If <code>prefetch</code> is called when the <code>Player</code>
     * is already in the <i>PREFETCHED</i> state, the <code>Player</code>
     * may still process data necessary to reduce the start
     * latency.  This is to guarantee that start latency can
     * be maintained at a minimum.
     * <p>
     * If <code>prefetch</code> is called when the <code>Player</code>
     * is in the <i>STARTED</i> state,
     * the request will be ignored.
     * <p>
     * If the <code>Player</code> cannot obtain all
     * of the resources it needs, it throws a <code>MediaException</code>.
     * When that happens, the <code>Player</code> will not be able to
     * start.  However, <code>prefetch</code> may be called again when
     * the needed resource is later released perhaps by another
     * <code>Player</code> or application.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @exception MediaException Thrown if the <code>Player</code> cannot
     * be prefetched.
     * @exception SecurityException Thrown if the caller does not
     * have security permission to prefetch the <code>Player</code>.
     *
     */
    public synchronized void prefetch() throws MediaException {
	chkClosed(false);

	if (state >= PREFETCHED)
	    return;

	if (state < REALIZED)
	    realize();
		
	doPrefetch();

	state = PREFETCHED;
    }

    /**
     * the worker method to prefetch the player
     */
    abstract protected void doPrefetch() throws MediaException;


    /**
     * Starts the <code>Player</code> as soon as possible.
     * If the <code>Player</code> was previously stopped
     * by calling <code>stop</code>,
     * it will resume playback
     * from where it was previously stopped.  If the
     * <code>Player</code> has reached the end of media,
     * calling <code>start</code> will automatically
     * start the playback from the start of the media.
     * <p>
     * When <code>start</code> returns successfully,
     * the <code>Player</code> must have been started and
     * a <code>STARTED</code> event will
     * be delivered to the registered <code>PlayerListener</code>s.
     * However, the <code>Player</code> is not guaranteed to be in
     * the <i>STARTED</i> state.  The <code>Player</code> may have
     * already stopped (in the <i>PREFETCHED</i> state) because
     * the media has 0 or a very short duration.
     * <p>
     * If <code>start</code> is called when the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>REALIZED</i> state,
     * it will implicitly call <code>prefetch</code>.
     * <p>
     * If <code>start</code> is called when the <code>Player</code>
     * is in the <i>STARTED</i> state,
     * the request will be ignored.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @exception MediaException Thrown if the <code>Player</code> cannot
     * be started.
     * @exception SecurityException Thrown if the caller does not
     * have security permission to start the <code>Player</code>.
     */
    public synchronized void start() throws MediaException {
	chkClosed(false);

	if (state >= STARTED)
	    return;

	if (state < REALIZED)
	    realize();

	if (state < PREFETCHED)
	    prefetch();

	// If it's at the EOM, it will automatically
	// loop back to the beginning.
	if (EOM)
	    setMediaTime(0);

	if (!doStart())
	    throw new MediaException("start");

	state = STARTED;
	sendEvent(PlayerListener.STARTED, new Long(getMediaTime()));
    }

    /** 
     * The worker method to actually start the player
     *
     * @return Whether the player is successfully started
     */
    abstract protected boolean doStart();


    /**
     * Stops the <code>Player</code>.  It will pause the playback at
     * the current media time.
     * <p>
     * When <code>stop</code> returns, the <code>Player</code> is in the
     * <i>PREFETCHED</i> state.
     * A <code>STOPPED</code> event will be delivered to the registered
     * <code>PlayerListener</code>s.
     * <p>
     * If <code>stop</code> is called on
     * a stopped <code>Player</code>, the request is ignored.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     */
    public synchronized void stop() {
	chkClosed(false);

	loopAfterEOM = false;
	
	if (state < STARTED)
	    return;

	doStop();

	state = PREFETCHED;
	sendEvent(PlayerListener.STOPPED, new Long(getMediaTime()));
    }

    /**
     * the worker method to stop the player
     */
    abstract protected void doStop();


    /**
     * Release the scarce or exclusive
     * resources like the audio device acquired by the <code>Player</code>.
     * <p>
     * When <code>deallocate</code> returns, the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>REALIZED</i> state.
     * <p>
     * If the <code>Player</code> is blocked at
     * the <code>realize</code> call while realizing, calling
     * <code>deallocate</code> unblocks the <code>realize</code> call and
     * returns the <code>Player</code> to the <i>UNREALIZED</i> state.
     * Otherwise, calling <code>deallocate</code> returns the
     * <code>Player</code> to  the <i>REALIZED</i> state.
     * <p>
     * If <code>deallocate</code> is called when the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>REALIZED</i>
     * state, the request is ignored.
     * <p>
     * If the <code>Player</code> is <code>STARTED</code>
     * when <code>deallocate</code> is called, <code>deallocate</code>
     * will implicitly call <code>stop</code> on the <code>Player</code>.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     */
    public synchronized void deallocate() {
	chkClosed(false);

	loopAfterEOM = false;
	
	if (state < PREFETCHED)
	    return;

	if (state == STARTED)
	    stop();

	doDeallocate();
	state = REALIZED;
    }

    /**
     * the worker method to deallocate the player
     */
    abstract protected void doDeallocate();


    /**
     * Close the <code>Player</code> and release its resources.
     * <p>
     * When the method returns, the <code>Player</code> is in the
     * <i>CLOSED</i> state and can no longer be used.
     * A <code>CLOSED</code> event will be delivered to the registered
     * <code>PlayerListener</code>s.
     * <p>
     * If <code>close</code> is called on a closed <code>Player</code>
     * the request is ignored.
     */
    public synchronized void close() {
	if (state == CLOSED)
	    return;
	
	deallocate();
	doClose();

	state = CLOSED;
	try {
	    if (stream != null)
		stream.close();
	} catch (IOException e) { }
	sendEvent(PlayerListener.CLOSED, null);
	mplayers.remove(new Integer(pID));
    }

    /**
     * the worker method to close the player
     */
    abstract protected void doClose();
    

    /**
     * Sets the <code>Player</code>'s&nbsp;<i>media time</i>.
     * <p>
     * For some media types, setting the media time may not be very
     * accurate.  The returned value will indicate the
     * actual media time set.
     * <p>
     * Setting the media time to negative values will effectively
     * set the media time to zero.  Setting the media time to
     * beyond the duration of the media will set the time to
     * the end of media.
     * <p>
     * There are some media types that cannot support the setting
     * of media time.  Calling <code>setMediaTime</code> will throw
     * a <code>MediaException</code> in those cases.
     *
     * @param now The new media time in microseconds.
     * @return The actual media time set in microseconds.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>CLOSED</i> state.
     * @exception MediaException Thrown if the media time
     * cannot be set.
     * @see #getMediaTime
     */
    public synchronized long setMediaTime(long now) throws MediaException {
	chkClosed(true);

	if (now < 0)
	    now = 0;
	
	long theDur = getDuration();
	if ((theDur != TIME_UNKNOWN) && (now > theDur))
	    now = theDur;

	long rtn = doSetMediaTime(now);
	EOM = false;

	return rtn;
    }

    /**
     * The worker method to actually set player's media time.
     *
     * @param now The new media time in microseconds.
     * @return The actual media time set in microseconds.
     * @exception MediaException Thrown if an error occurs
     * while setting the media time.
     */
    abstract protected long doSetMediaTime(long now) throws MediaException;


    /**
     * Gets this <code>Player</code>'s current <i>media time</i>.
     * If the <i>media time</i> cannot be determined,
     * <code>getMediaTime</code> returns <code>TIME_UNKNOWN</code>.
     *
     * @return The current <i>media time</i> in microseconds or
     * <code>TIME_UNKNOWN</code>.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @see #setMediaTime
     */
    public long getMediaTime() {
	chkClosed(false);
	return doGetMediaTime();
    }
    

    /**
     * The actual worker method to gets this player's 
     * current <i>media time</i> in microseconds.
     * 
     * @return The current <i>media time</i> in microseconds.
     */
    abstract protected long doGetMediaTime();

    /**
     * Gets the current state of this <code>Player</code>.
     * The possible states are: <i>UNREALIZED</i>,
     * <i>REALIZED</i>, <i>PREFETCHED</i>, <i>STARTED</i>, <i>CLOSED</i>.
     *
     * @return The <code>Player</code>'s current state.
     */
    public int getState() {
	/**
	 * A race condition can occur between
	 * the return of this method and the execution of
	 * a state changing method.
	 */
	return state;
    }


    /**
     * Get the duration of the media.
     * The value returned is the media's duration
     * when played at the default rate.
     * <br>
     * If the duration cannot be determined (for example, the
     * <code>Player</code> is presenting live
     * media)  <CODE>getDuration</CODE> returns <CODE>TIME_UNKNOWN</CODE>.
     *
     * @return The duration in microseconds or <code>TIME_UNKNOWN</code>.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     */
    public long getDuration() {
	chkClosed(false);
	return doGetDuration();
    }

    /**
     * The actual worker method to retrieve the duration.
     *
     * @return A <CODE>long</CODE> object representing the duration or 
     * TIME_UNKNWON.
     */
    abstract protected long doGetDuration();


    /**
     * Add a player listener for this player.
     *
     * @param playerListener the listener to add.
     * If <code>null</code> is used, the request will be ignored.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @see #removePlayerListener
     */
    public void addPlayerListener(PlayerListener playerListener) {
	chkClosed(false);
	if (playerListener != null)
	    listeners.addElement(playerListener);
    }


    /**
     * Remove a player listener for this player.
     *
     * @param playerListener the listener to remove.
     * If <code>null</code> is used or the given
     * <code>playerListener</code> is not a listener for this
     * <code>Player</code>, the request will be ignored.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @see #addPlayerListener
     */
    public void removePlayerListener(PlayerListener playerListener) {
	chkClosed(false);
	listeners.removeElement(playerListener);
    }


    /**
     * Deliver the events to the player listeners.
     *
     * @param evt the evt type
     * @param evtData the data associated with this event.
     *
     */
    public void sendEvent(String evt, Object evtData) {

	//  There's always one listener for EOM -- itself.
	if (listeners.size() == 0 && evt != PlayerListener.END_OF_MEDIA)
	    return;

	// Deliver the event to the listeners.
	synchronized (evtLock) {
	    if (evtQ == null)
		evtQ = new EvtQ(this);
	    evtQ.sendEvent(evt, evtData);
	}
    }

    /**
     * the worker method to deliver EOM event
     */
    synchronized void doLoop() {

	// If a loop count is set, we'll loop back to the beginning.
	if ((loopCount > 1) || (loopCount == -1)) {
	    try {
		if (setMediaTime(0) == 0) {
		    if (loopCount > 1)
			loopCount--;
		    start();
		} else
		    loopCount = 1;
	    } catch (MediaException ex) {
		loopCount = 1;
	    }
	} else if (loopCountSet > 1)
	    loopCount = loopCountSet;

	loopAfterEOM = false;
    }

    /**
     * Obtain the collection of <code>Control</code>s
     * from this player.
     * @return the collection of <code>Control</code> objects.
     */
    public Control[] getControls() {
	chkClosed(true);
	return new Control[] { this };
    }
    
    /**
     * Gets the <code>Control</code> that supports the specified 
     * class or interface. The full class
     * or interface name should be specified.
     * <code>Null</code> is returned if the <code>Control</code>
     * is not supported.
     *
     * @param type the class name of the <code>Control</code>.  
     * @return <code>Control</code> for the class or interface
     * name.
     */
    public Control getControl(String type) {
	chkClosed(true);

	// Prepend the package name if the type given does not
	// have the package prefix.
	if (type.indexOf('.') < 0)
	    return doGetControl("javax.microedition.media.control." + type);
	
	return doGetControl(type);
    }


    /**
     * The worker method to actually obtain the control.
     *
     * @param type the class name of the <code>Control</code>.  
     * @return <code>Control</code> for the class or interface
     * name.
     */

    abstract protected Control doGetControl(String type);


    /**
     * ===============================
     * For global PlayerID management
     * =============================== 
    */
    /**
     * Obtain a BasicPlayer instance based on the global id.
     *
     * @param pid the given global id.
     * @return the instance of BasicPlayer associated with given global id
     *
     */
    public static BasicPlayer get(int pid) {
	return (BasicPlayer)(mplayers.get(new Integer(pid)));
    }


    /**
     * ==========================
     * Methods for VolumeControl.
     * ==========================
     */
    /**
     * volume level
     */
    private int  level = -1;
    /**
     * mute state
     */
    private boolean mute;

    /**
     * The worker method to actually obtain the control.
     *
     * @param vol the volume level to be set.  
     * @return the actual level has been set.
     */

    protected abstract int doSetLevel(int vol);

    /**
     * set player mute.
     *
     * @param mute the flag to mute the player or not
     *
     */
    public void setMute(boolean mute) {
	if (mute && !this.mute) {
	    doSetLevel(0);
	    this.mute = true;
	    sendEvent(PlayerListener.VOLUME_CHANGED, this);
	} else if (!mute && this.mute) {
	    this.level = doSetLevel(level);
	    this.mute = false;
	    sendEvent(PlayerListener.VOLUME_CHANGED, this);
	}
    }

    /**
     * Check if this player is muted.
     *
     * @return The mute state.
     */
    public boolean isMuted() {
	return mute;
    }
    
    /**
     * Set the volume using a linear point scale 0 to 100.
     * @param ll The new volume specified in the level scale.
     * @return The level that was actually set.
     */
    public int setLevel(int ll) {
	int newl;

	if (ll < 0) {
	    ll  = 0;
	} else if (ll > 100) {
	    ll = 100;
	} 

	if (!mute) {
	    newl = doSetLevel(ll);
	    if (newl != level) {
		level = newl;
		sendEvent(PlayerListener.VOLUME_CHANGED, this);
	    }
	}
	return level;
    }

    /**
     * Get the current volume set for this
     * player.
     *
     * @return The volume in the level scale (0-100).
     */
    public int getLevel() {
	return level;
    }

    /**
     * ================
     * Input functions.
     * ================
     */
    /**
     * the source input stream of this player
     */
    protected InputStream stream;
    /** 
     * the current position in source stream
     */
    long location;

    /**
     * Set the locator of this player.
     *
     * @param locator the locator to be set.
     * @param con the flag if to make the connection
     *
     */
    public void setLocator(String locator, boolean con) throws IOException, 
    MediaException {
	this.locator = locator;
	if (con)
	    openConnection();
    }

    /**
     * Set the input stream of this player.
     *
     * @param stream the input stream to be set.
     *
     */
    public void setStrm(InputStream stream) {
	this.stream = stream;
    }

    /**
     * establish the connection with the source.
     *
     */
    private void openConnection() throws IOException, MediaException {
	try {
	    HttpConnection httpCon = (HttpConnection)Connector.open(locator);
	    int rescode = httpCon.getResponseCode();
	    // both 4XX and 5XX are error codes
	    if (rescode >= 400) {
		httpCon.close();
		throw new IOException("bad url");
	    } else {
		stream = httpCon.openInputStream();
		String ctype = httpCon.getType();
		boolean supportedCT = false;
		if (locator.endsWith(".wav")) {
		    supportedCT = true;
		} else if (ctype != null && 
			   ctype.toLowerCase().equals("audio/x-wav")) {
		    supportedCT = true;
		}
		    
		httpCon.close();

		if (!supportedCT) {
		    stream.close();
		    stream =  null;
		    throw new MediaException("unsupported media type");
		}
	    }
	} catch (IOException ioex) {
	    throw ioex;
	} catch (MediaException mex) {
	    throw mex;
	} catch (Exception ex) {
	    new IOException(ex.getMessage() + " failed to connect");
	}

	location = 0;
    }

    /**
     * Read a data buffer from source stream.
     *
     * @param buffer the byte array to hold the read data
     * @param offset the offset of byte array.
     * @param length the maximum bytes to be read
     * @return the actual number of bytes have been read
     */
    protected int readStrm(byte buffer[], int offset, int length) 
	throws IOException {
	int len = stream.read(buffer, offset, length);
	if (len > 0)
	    location += len;
	return len;
    }

    /**
     * In source stream, seek to a particular position
     * 
     * @param where the position intended to seek to.
     * @return the actual position seeked to.
     * @exception an error occurs during the seeking
     */

    protected long seekStrm(long where) throws IOException, MediaException {
	if (stream == null)
	    return location;
	long skipped, oldLocation = location;
	if (where < oldLocation) { // seek backward
	    reopenStrm();
	    location = 0;
	    skipped = stream.skip(where);
	} else  {
	    skipped = stream.skip((where - oldLocation));
	}

	if (skipped > 0)
	    location += skipped;

	return location;
    }

    /**
     * This is a skip fully method 
     * 
     * @param numBytes the number of bytes intended to skip.
     * @return the actual number of bytes has been skipped.
     * @exception if an error occurs or skipped bytes is less
     * then the intended numBytes.
     */
    protected long skipStrm(int numBytes) throws IOException {
	long skipped = stream.skip(numBytes);
	if (skipped > 0)
	    location += skipped;
	if (skipped < numBytes)
	    throw new IOException("skipped over eom");
	return (skipped);
    }

    /**
     * Re-open the source stream.
     *
     * @exception if an error occurs.
     */
    private void reopenStrm() throws IOException, MediaException {
	try {
	    stream.reset();
	    return;
	} catch (IOException ex) {
	    if (locator == null)
		throw ex;
	}

	try {
	    stream.close();
	    stream = null;
	} catch (IOException e) {}

	
	openConnection();
    }

    /**
     * Get the current position of the source stream
     *
     * @return the current position.
     */
    protected long getStrmLoc() {
	return location;
    }
}


/**
 * The thread that's responsible for delivering Player events.
 * This class lives for only 5 secs.  If no event comes in
 * 5 secs, it will exit.
 */
class EvtQ extends Thread {
    /**
     * the player instance
     */
    private BasicPlayer p;
    /**
     * event type array
     */
    private String[] evtQ;
    /**
     * event data array
     */
    private Object[] evtDataQ;
    /**
     * head and tail pointer of the event queue
     */
    private int head, tail;
    /**
     * the default size of the event queue
     */
    private static final int size = 12;

    /**
     * The constructor
     *
     * @param p the instance of BasicPlayer intending to post event to
     *        this event queue.
     */
    EvtQ(BasicPlayer p) {
	this.p = p;
	evtQ = new String[size];
	evtDataQ = new Object[size];
	start();
    }

    /**
     * Put an event in the event queue and wake up the thread to
     * deliver it.  If the event queue is filled, block.
     *
     * @param evt the evt type
     * @param evtData the event data
     */
    synchronized void sendEvent(String evt, Object evtData) {
	// Wait if the event queue is full.
	// This potentially will block the Player's main thread.
	while ((head + 1) % size == tail) {
	    try {
		wait();
	    } catch (Exception e) { }
	}
	evtQ[head] = evt;
	evtDataQ[head] = evtData;
	if (++head == size)
	    head = 0;
	notify();
    }

    /**
     * the run method for interface Runnable
     */
    public void run() {

	String evt = "";
	Object evtData = null;
	boolean evtToGo = false;

	for (;;) {

	    synchronized (this) {

		// If the queue is empty, we'll wait for at most
		// 5 secs.
		if (head == tail) {
		    try {
			wait(5000);
		    } catch (Exception e) {
		    }
		} 

		if (head != tail) { 
		    evt = evtQ[tail];
		    evtData = evtDataQ[tail];
		    // For garbage collection.
		    evtDataQ[tail] = null;
		    evtToGo = true;
		    if (++tail == size)
			tail = 0;
		    notify();
		} else
		    evtToGo = false;

	    } // synchronized this

	    if (evtToGo) {
		if (evt == PlayerListener.END_OF_MEDIA) {
		    synchronized (p) {
			p.EOM = true;
			p.loopAfterEOM = false;
			if (p.state > Player.PREFETCHED) {
			    p.state = Player.PREFETCHED;
			    if (p.loopCount > 1 || p.loopCount == -1) {
				p.loopAfterEOM = true;
			    }
			}
		    }
                }

		synchronized (p.listeners) {
		    PlayerListener l;
		    for (int i = 0; i < p.listeners.size(); i++) {
			try {
			    l = (PlayerListener)p.listeners.elementAt(i);
			    l.playerUpdate(p, evt, evtData);
			} catch (Exception e) {
			    System.err.println("Error in playerUpdate: " + e);
		        }
		    }
		}

		if (p.loopAfterEOM) 
		    p.doLoop();
	    }
	    
	    if (!evtToGo || evt == PlayerListener.CLOSED) {
		// If there's no event waking up after 5 secs,
		// we'll kill the thread.
		synchronized (p.evtLock) {
		    p.evtQ = null;
		    break;
		}
	    }
	}
    }
}
