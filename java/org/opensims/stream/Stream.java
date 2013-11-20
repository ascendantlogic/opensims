/**
 * @LICENSE@
 */

package org.opensims.stream;

/**
 * $CVSId: Stream.java,v 1.12 2004/08/18 23:28:28 paco Exp $
 * $Id: Stream.java 1 2008-01-10 18:37:05Z smoot $
 * Flash XMLSocket subscriber stream
 * @version $Revision: 1.12 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Lindsey SIMON <lsimon@symbiot.com>
 */

public interface
    Stream
{
    // public final statics

    public final static String STREAM_INIT = "init";
    public final static String STREAM_PING = "ping";
    public final static String STREAM_VIZ = "viz";


    /**
     * Initialize from a config node.
     */

    public void
	init (org.opensims.Correlator correlator, org.opensims.client.Subscriber subscriber, String name, org.jdom.Element node);


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Correlator.
     */

    public org.opensims.Correlator
	getCorrelator ();


    /**
     * Set the Correlator.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator);


    /**
     * Get the Subscriber.
     */

    public org.opensims.client.Subscriber
	getSubscriber ();


    /**
     * Set the Subscriber.
     */

    public void
	setSubscriber (org.opensims.client.Subscriber subscriber);


    /**
     * Get the name.
     */

    public String
	getName ();


    /**
     * Set the name.
     */

    public void
	setName (String name);


    /**
     * Get the XML config node.
     */

    public org.jdom.Element
	getNode ();


    /**
     * Set the XML config node.
     */

    public void
	setNode (org.jdom.Element node);


    /**
     * Get the delay period (milliseconds).
     */

    public long
	getDelayPeriod ();


    /**
     * Set the delay period (milliseconds).
     */

    public void
	setDelayPeriod (long delay_period);


    /**
     * Get the refresh period (milliseconds).
     */

    public long
	getRefreshPeriod ();


    /**
     * Set the refresh period (milliseconds).
     */

    public void
	setRefreshPeriod (long refresh_period);


    /**
     * Get the last refresh (milliseconds).
     */

    public long
	getLastRefresh ();


    /**
     * Set the last refresh (milliseconds).
     */

    public void
	setLastRefresh (long last_refresh);


    /**
     * Get the activity window, which provides a window of "lookback"
     * (milliseconds) into the stream of security events.
     */

    public long
	getActivityWindow ();


    /**
     * Set the activity window (milliseconds).
     */

    public void
	setActivityWindow (long activity_window);


    //////////////////////////////////////////////////////////////////////
    // content methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Build a content document
     */

    public org.jdom.Document
	buildContentDocument (long tick);


    //////////////////////////////////////////////////////////////////////
    // lifecycle methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Start the stream.
     */

    public void
	start ();


    /**
     * Stop the stream.
     */

    public void
	stop ();


    /**
     * Run the stream update task.
     */

    public void
	run ();
}
