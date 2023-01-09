/*
 * @(#)Manager.java	1.29 02/07/31 @(#)
 *
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 * PROPRIETARY/CONFIDENTIAL
 * Use is subject to license terms.
 */

package javax.microedition.media;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import com.sun.mmedia.BasicPlayer;
import com.sun.mmedia.TonePlayer;
import com.sun.mmedia.WavPlayer;


/**
 * <code>Manager</code> is the access point for obtaining
 * system dependent resources such as <code>Players</code>
 * for multimedia processing.
 * <p>
 *
 * A <a href="Player.html"<code>Player</code></a> 
 * is an object used to
 * control and render media that
 * is specific to the 
 * <a href="#content-type">content type</a> 
 * of the data.
 * <p>
 * <code>Manager</code> provides access to an implementation specific
 * mechanism for constructing <code>Players</code>.
 * <p>
 * For convenience, <code>Manager</code> also provides a simplified 
 * method to generate simple tones.
 *
 * <h2>Simple Tone Generation</h2>
 * 
 * <blockquote>
 * The 
 * <a href="#playTone(int, int, int)">
 * <code>playTone</code></a>
 * function is defined to generate
 * tones.  Given the note and duration, the function will
 * produce the specified tone.
 * </blockquote>
 *
 * <h2>Creating Players</h2>
 * <blockquote>
 *
 * <code>Manager</code> provides two methods to create a 
 * <code>Player</code> for playing back media:
 * <ul>
 * <li> Create from a media locator.
 * <li> Create from an <code>InputStream</code>.
 * </ul>
 * The <code>Player</code> returned can be used to control the
 * presentation of the media.
 * </blockquote>
 * <p>
 *
 * <a name="content-type"></a>
 * <h2>Content Types</h2>
 * <blockquote>
 * Content types identify the type of media data.  They are 
 * defined to be the registered MIME types 
 * (<a href=
 * "http://www.iana.org/assignments/media-types/">
 * http://www.iana.org/assignments/media-types/</a>); 
 * plus
 * some user-defined types that generally follow the MIME syntax
 * (<a href="ftp://ftp.isi.edu/in-notes/rfc2045.txt">RFC 2045</a>,  
 * <a href="ftp://ftp.isi.edu/in-notes/rfc2046.txt">RFC 2046</a>).
 * <p>
 * For example, here are a few common content types:
 * <ol>
 * <li>Wave audio files: <code>audio/x-wav</code>
 * <li>AU audio files: <code>audio/basic</code>
 * <li>MP3 audio files: <code>audio/mpeg</code>
 * <li>MIDI files: <code>audio/midi</code>
 * <li>Tone sequences: <code>audio/x-tone-seq</code>
 * </ol>
 * </blockquote>
 *
 * <a name="media-locator"></a>
 * <h2>Media Locator</h2>
 * <blockquote>
 * <a name="media-protocol"></a>
 * Media locators are specified in 
 * <a href="http://www.ietf.org/rfc/rfc2396.txt">URI syntax</a> 
 * which is defined in the form:
 * <p>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;scheme&gt;:&lt;scheme-specific-part&gt;
 * <p>
 * The "scheme" part of the locator string identifies the name
 * of the protocol being used to deliver the data.
 * </blockquote>
 *
 * @see Player
 */

public final class Manager {

    /**
     * The locator to create a tone <code>Player</code>
     * to play back tone sequences. e.g. 
     * <pre>
     * try {
     *     Player p = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
     *     p.realize();
     *     ToneControl tc = (ToneControl)p.getControl("ToneControl");
     *     tc.setSequence(mySequence);
     *     p.start();
     * } catch (IOException ioe) {
     * } catch (MediaException me) {}
     * </pre>
     *
     * If a tone sequence is not set on the tone 
     * <code>Player</code> via its <code>ToneControl</code>, 
     * the <code>Player</code> does not carry any 
     * sequence.  <code>getDuration</code> returns 0 for this
     * <code>Player</code>.
     * <p>
     * The content type of the <code>Player</code> created from this
     * locator is <code>audio/x-tone-seq</code>.
     * <p>
     * A <code>Player</code> for this locator may not be supported 
     * for all implementations.
     * <p>
     * Value "device://tone" is assigned to <code>TONE_DEVICE_LOCATOR</code>.
     */
    final public static String TONE_DEVICE_LOCATOR = "device://tone";

    /**
     * The native implementation method to play a tone.
     *
     * @param note Defines the tone of the note.
     * @param dur  The duration of the tone in milli-seconds.
     * @param vol  Audio volume range from 0 to 100.
     *
     * @return the status. <= 0 means it failed to play the tone
     */
    native private static int nPlayTone(int note, int  dur, int vol);

    /**
     * This private constructor keeps anyone from actually
     * getting a <CODE>Manager</CODE>.
     */
    private Manager() {}

    /**
     * Return the list of supported content types for the given protocol.
     * <p>
     * See <a href="#content-type">content types</a> for the syntax
     * of the content types returned.
     * See <a href="#media-protocol">protocol name</a> for the syntax
     * of the protocol used. 
     * <p>
     * For example, if the given <code>protocol</code> 
     * is <code>"http"</code>,
     * then the supported content types that can be played back
     * with the <code>http</code> protocol will be returned.
     * <p>
     * If <code>null</code> is passed in as the <code>protocol</code>, 
     * all the supported content types for this implementation 
     * will be returned.  The returned array must be non-empty.
     * <p>
     * If the given <code>protocol</code> is an invalid or
     * unsupported protocol, then an empty array will be returned.
     *
     * @param protocol The input protocol for the supported content types.
     * @return The list of supported content types for the given protocol.
     */ 
    public static String [] getSupportedContentTypes(String protocol) {
	if (protocol == null)
	    return new String [] { "audio/x-wav", "audio/x-tone-seq" };
	if (protocol.equals("device"))
	    return new String [] { "audio/x-tone-seq" };
	if (protocol.equals("http"))
	    return new String [] { "audio/x-wav" };
	return new String[0];
    }

    /**
     * Return the list of supported protocols given the content
     * type.  The protocols are returned
     * as strings which identify what locators can be used for creating 
     * <code>Player</code>'s.
     * <p>
     * See <a href="#media-protocol">protocol name</a> for the syntax
     * of the protocols returned. 
     * See <a href="#content-type">content types</a> for the syntax
     * of the content type used.
     * <p>
     * For example, if the given <code>content_type</code>
     * is <code>"audio/x-wav"</code>, then the supported protocols
     * that can be used to play back <code>audio/x-wav</code>
     * will be returned.
     * <p>
     * If <code>null</code> is passed in as the 
     * <code>content_type</code>, 
     * all the supported protocols for this implementation 
     * will be returned.  The returned array must be non-empty.
     * <p>
     * If the given <code>content_type</code> is an invalid or
     * unsupported content type, then an empty array will be returned.
     *
     * @param content_type The content type for the supported protocols.
     * @return The list of supported protocols for the given content type.
     */
    public static String [] getSupportedProtocols(String content_type) {
	if (content_type == null)
	    return new String [] { "device", "http" };
	if (content_type.equals("audio/x-tone-seq"))
	    return new String [] { "device" };
	if (content_type.equals("audio/x-wav"))
	    return new String [] { "http" };
	return new String[0];
    }

    /**
     * Create a <code>Player</code> from an input locator.
     *
     * @param locator A locator string in URI syntax that describes
     * the media content.  
     * @return A new <code>Player</code>.
     * @exception IllegalArgumentException Thrown if <code>locator</code>
     * is <code>null</code>.
     * @exception MediaException Thrown if a <code>Player</code> cannot
     * be created for the given locator.
     * @exception IOException Thrown if there was a problem connecting
     * with the source pointed to by the <code>locator</code>.
     * @exception SecurityException Thrown if the caller does not
     * have security permission to create the <code>Player</code>.
     */
    public static Player createPlayer(String locator)
	throws IOException, MediaException {

	if (locator == null)
	    throw new IllegalArgumentException("locator: null");

	BasicPlayer p = null;
	boolean conn = true;
	if (locator.equals(TONE_DEVICE_LOCATOR)) {
	    p = new TonePlayer();
	    conn = false;
	} else {
	    if (locator.toLowerCase().endsWith(".wav") ||
		locator.startsWith("http:")) {
		p = new WavPlayer();
	    }
	}

	if (p == null)
	    throw new MediaException("Unsupported type.");
	
	p.setLocator(locator, conn);
	
	return p;
    }

    /**
     * Create a <code>Player</code> to play back media from an 
     * <code>InputStream</code>.  
     * <p>
     * The <code>type</code> argument
     * specifies the content-type of the input media.  If 
     * <code>null</code> is given, <code>Manager</code> will
     * attempt to determine the type.  However, since determining
     * the media type is non-trivial for some media types, it
     * may not be feasible in some cases.  The
     * <code>Manager</code> may throw a <code>MediaException</code>
     * to indicate that.
     * 
     * @param stream The <code>InputStream</code> that delivers the
     * input media.
     * @param type The <code>ContentType</code> of the media.
     * @return A new <code>Player</code>.
     * @exception IllegalArgumentException Thrown if <code>stream</code>
     * is <code>null</code>.
     * @exception MediaException Thrown if a <code>Player</code> cannot
     * be created for the given stream and type.
     * @exception IOException Thrown if there was a problem reading data
     * from the <code>InputStream</code>.
     * @exception SecurityException Thrown if the caller does not
     * have security permission to create the <code>Player</code>.
     */
    public static Player createPlayer(InputStream stream, String type) 
	throws IOException, MediaException {
	if (stream == null)
	    throw new IllegalArgumentException("stream: null");

	BasicPlayer p = null;

	if (type != null) {
	    type = type.toLowerCase();
	}
	if ((type == null) || 
	    ((type != null) && (type.equals("audio/x-wav")))) {
	    p = new WavPlayer();
	}

	if ((type != null) && (type.equals("audio/x-tone-seq"))) {
	    p = new TonePlayer();
	}

	if (p != null) {
	    p.setStrm(stream);
	    p.setLocator(null, false);
	    return p;
	} 

	throw new MediaException("Unsupported type.");
    }

    /**
     * Play back a tone as specified by a note and its duration.
     * A note is given in the range of 0 to 127 inclusive.  The frequency 
     * of the note can be calculated from the following formula:
     * <pre>
     *     SEMITONE_CONST = 17.31234049066755 = 1/(ln(2^(1/12)))
     *     note = ln(freq/8.176)*SEMITONE_CONST
     *     The musical note A = MIDI note 69 (0x45) = 440 Hz.
     * </pre>
     * This call is a non-blocking call. Notice that this method may
     * utilize CPU resources significantly on devices that don't
     * have hardware support for tone generation.
     * 
     * @param note Defines the tone of the note as specified by the 
     * above formula.
     * @param duration The duration of the tone in milli-seconds.
     * Duration must be positive.
     * @param volume Audio volume range from 0 to 100.  100 represents 
     * the maximum
     * volume at the current hardware level.  Setting the volume to a 
     * value less
     * than 0 will set the volume to 0.  Setting the volume to greater than
     * 100 will set the volume to 100. 
     *
     * @exception IllegalArgumentException Thrown if the given note or 
     * duration is out of range.
     * @exception MediaException Thrown if the tone cannot be played 
     * due to a device-related problem.
     */
    static public void playTone(int note, int duration, int volume)
	throws MediaException {
	if (note < 0 || note > 127 || duration <= 0)
	    throw new IllegalArgumentException("playTone");

	if (nPlayTone(note, duration, volume) <= 0)
	    throw new MediaException("can't play tone");
    }
}

