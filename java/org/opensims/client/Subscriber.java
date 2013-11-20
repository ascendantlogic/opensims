/**
 * @LICENSE@
 */

package org.opensims.client;

/**
 * $CVSId: Subscriber.java,v 1.22 2004/10/30 08:23:44 paco Exp $
 * $Id: Subscriber.java 1 2008-01-10 18:37:05Z smoot $
 * Flash XMLSocket subscriber
 * @version $Revision: 1.22 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Lindsey SIMON <lsimon@symbiot.com>
 * @author Linas VARDYS <linas@symbiot.com>
 *
 * We learned a lot from: http://www.shovemedia.com/multiserver/
 * ...and even more from: http://www.dagblastit.com/java/sockets.html
 */

public interface
    Subscriber
    extends Runnable,
	    org.opensims.Tickable
{
    // public definitions

    public final static int SO_TIMEOUT = 60000;


    /**
     * Initialize the subscriber object
     */

    public void
	init (org.opensims.client.SubscriberManager subscriber_manager, java.net.Socket socket);


    /**
     * Represent as a String
     */

    public String
	getSummary ();


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the IPv4 address.
     */

    public org.opensims.IPv4
	getIPv4 ();


    /**
     * Set the IPv4 address.
     */

    public void
	setIPv4 (org.opensims.IPv4 ip_addr);


    /**
     * Get the language (ISO 639) tag.
     * http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt
     */

    public String
	getLanguage ();


    /**
     * Set the language (ISO 639) tag.
     */

    public void
	setLanguage (String language);


    /**
     * Get the plunder flag (convert XML to linear format).
     */

    public boolean
	getPlunderFlag ();


    /**
     * Set the plunder flag (convert XML to linear format).
     */

    public void
	setPlunderFlag (boolean plunder_flag);


    /**
     * Get the first data flag (not received a VIZ packet yet).
     */

    public boolean
	getFirstData ();


    /**
     * Set the first data flag (not received a VIZ packet yet).
     */

    public void
	setFirstData (boolean first_data);


    /**
     * Get the Timer.
     */

    public java.util.Timer
	getTimer ();


    /**
     * Set the Timer.
     */

    public void
	setTimer (java.util.Timer timer);


    /**
     * Get the thread name.
     */

    public String
	getName ();


    //////////////////////////////////////////////////////////////////////
    // Tickable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Manager
     */

    public org.opensims.Manager
        getManager ();


    /**
     * Set the Manager
     */

    public void
        setManager (org.opensims.Manager manager);


    /**
     * Get the last_tick value - for expiry
     */

    public long
	getLastTick ();


    /**
     * Set the last_tick value - for expiry
     */

    public void
	setLastTick (long last_tick);


    /**
     * Get the hashtable lookup key
     */

    public String
	getKey ();


    /**
     * Test whether a <PING/> has been received recently enough to
     * keep the subscriber alive
     */

    public boolean
	checkExpiry (long model_tick);


    //////////////////////////////////////////////////////////////////////
    // Runnable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the client thread.
     */

    public Thread
	getClientThread ();


    /**
     * Set the client thread.
     */

    public void
	setClientThread (Thread client_thread);


    /**
     * Start the connection running.
     */

    public void
	start ();


    /**
     * If at any point we are unable to communicate with a socket,
     * stop() is called to remove this subscriber
     */

    public void
	stop ();


    /**
     * Runnable interface
     */

    public void
	run ();


    //////////////////////////////////////////////////////////////////////
    // socket handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the socket.
     */

    public java.net.Socket
	getSocket ();


    /**
     * Set the socket.
     */

    public void
	setSocket (java.net.Socket socket);


    /**
     * Get the input stream.
     */

    public java.io.InputStream
	getInputStream ();


    /**
     * Set the input stream.
     */

    public void
	setInputStream (java.io.InputStream input_stream);


    /**
     * Get the output stream.
     */

    public java.io.OutputStream
	getOutputStream ();


    /**
     * Set the output stream.
     */

    public void
	setOutputStream (java.io.OutputStream output_stream);


    //////////////////////////////////////////////////////////////////////
    // ping/status
    //////////////////////////////////////////////////////////////////////

    /**
     * Add a status message to the queue.
     */

    public void
	addStatusMessage (int rank, boolean reboot_flag, boolean modal_flag, int error_code, String message, String url);


    /**
     * Get the next (FIFO) status message in the queue.
     */

    public org.jdom.Element
	getStatusMessage ();


    /**
     * Get the ping period (milliseconds), i.e. the max allowed interval between <PING/>
     */

    public long
	getPingPeriod ();


    /**
     * Set the ping period (milliseconds).
     */

    public void
	setPingPeriod (long ping_period);


    /**
     * Get the interval since the last <PONG/> was received
     * (milliseconds).
     */

    public long
	getPingInterval ();


    //////////////////////////////////////////////////////////////////////
    // stream handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get a stream.
     */

    public org.opensims.stream.Stream
	getStream (String stream_name);


    /**
     * Push the XML content document out to the subscriber.
     */

    public void
	pushStream (org.jdom.Document content_doc, String stream_name);


    //////////////////////////////////////////////////////////////////////
    // session handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Login wrapper.
     */

    public org.opensims.servlet.Login
	getLogin ();


    /**
     * Set the Login wrapper.
     */

    public void
	setLogin (org.opensims.servlet.Login login);
}
