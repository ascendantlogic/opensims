/**
 * @LICENSE@
 */

package org.opensims.stream;

/**
 * $CVSId: GenericStream.java,v 1.15 2004/09/30 22:42:20 paco Exp $
 * $Id: GenericStream.java 1 2008-01-10 18:37:05Z smoot $
 * Flash XMLSocket subscriber stream
 * @version $Revision: 1.15 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Lindsey SIMON <lsimon@symbiot.com>
 */

public class
    GenericStream
    extends java.util.TimerTask
    implements org.opensims.stream.Stream
{
    // public definitions

    public final static long DEFAULT_DELAY_PERIOD = 500L;

    // protected fields

    protected org.opensims.Correlator correlator = null;
    protected org.opensims.client.Subscriber subscriber = null;
    protected String name = null;
    protected org.jdom.Element node = null;
    protected long delay_period = 0L;
    protected long refresh_period = 0L;
    protected long last_refresh = 0L;
    protected long activity_window = 0L;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.stream.GenericStream.class.getName());


    /**
     * Initialize from an XML config node.
     */

    public void
	init (org.opensims.Correlator correlator, org.opensims.client.Subscriber subscriber, String name, org.jdom.Element node)
    {
	setCorrelator(correlator);
	setSubscriber(subscriber);
	setName(name);
	setNode(node);

	// <SUBSCRIBE stream="viz" delay="1000" refresh="5000" activity="10"/>

	setDelayPeriod(getCorrelator().parseTick(org.opensims.Config.fixNull(getNode().getAttributeValue("delay"), "0").trim()));
	setRefreshPeriod(getCorrelator().parseTick(org.opensims.Config.fixNull(getNode().getAttributeValue("refresh"), "0").trim()));
	setActivityWindow(getCorrelator().parseTick(org.opensims.Config.fixNull(getNode().getAttributeValue("activity"), "0").trim()));
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Correlator.
     */

    public org.opensims.Correlator
	getCorrelator ()
    {
	return correlator;
    }


    /**
     * Set the Correlator.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator)
    {
	this.correlator = correlator;
    }


    /**
     * Get the Subscriber.
     */

    public org.opensims.client.Subscriber
	getSubscriber ()
    {
	return subscriber;
    }


    /**
     * Set the Subscriber.
     */

    public void
	setSubscriber (org.opensims.client.Subscriber subscriber)
    {
	this.subscriber = subscriber;
    }


    /**
     * Get the name.
     */

    public String
	getName ()
    {
	return name;
    }


    /**
     * Set the name.
     */

    public void
	setName (String name)
    {
	this.name = name;
    }


    /**
     * Get the XML config node.
     */

    public org.jdom.Element
	getNode ()
    {
	return node;
    }


    /**
     * Set the XML config node.
     */

    public void
	setNode (org.jdom.Element node)
    {
	this.node = node;
    }


    /**
     * Get the delay period (milliseconds).
     */

    public long
	getDelayPeriod ()
    {
	return delay_period;
    }


    /**
     * Set the delay period (milliseconds).
     */

    public void
	setDelayPeriod (long delay_period)
    {
	if (delay_period >= 0L) {
	    this.delay_period = delay_period;
	}
    }


    /**
     * Get the refresh period (milliseconds).
     */

    public long
	getRefreshPeriod ()
    {
	return refresh_period;
    }


    /**
     * Set the refresh period (milliseconds).
     */

    public void
	setRefreshPeriod (long refresh_period)
    {
	if (refresh_period >= 0L) {
	    this.refresh_period = refresh_period;
	}
    }


    /**
     * Get the last refresh (milliseconds).
     */

    public long
	getLastRefresh ()
    {
	return last_refresh;
    }


    /**
     * Set the last refresh (milliseconds).
     */

    public void
	setLastRefresh (long last_refresh)
    {
	this.last_refresh = last_refresh;
    }


    /**
     * Get the activity window, which provides a window of "lookback"
     * (milliseconds) into the stream of security events.
     */

    public long
	getActivityWindow ()
    {
	return activity_window;
    }


    /**
     * Set the activity window (milliseconds).
     */

    public void
	setActivityWindow (long activity_window)
    {
	this.activity_window = activity_window;
    }


    //////////////////////////////////////////////////////////////////////
    // content methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Build a content document
     */

    public org.jdom.Document
	buildContentDocument (long tick)
    {
	return null;
    }


    //////////////////////////////////////////////////////////////////////
    // lifecycle methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Start the stream.
     */

    public void
	start ()
    {
	if (log_.isDebugEnabled()) {
	    long this_tick = getCorrelator().getTick();
	    log_.debug(this_tick + " " + getName() + " start: delay " + getDelayPeriod() + " refresh " + getRefreshPeriod());
	}

	if (getRefreshPeriod() > 0L) {
	    getSubscriber().getTimer().schedule(this, getDelayPeriod(), getRefreshPeriod());
	}
	else {
	    getSubscriber().getTimer().schedule(this, getDelayPeriod());
	}
    }


    /**
     * Stop the stream.
     */

    public void
	stop ()
    {
	cancel();

	if (log_.isDebugEnabled()) {
	    long this_tick = getCorrelator().getTick();
	    log_.debug(this_tick + " " + getName() + " stop");
	}
    }


    /**
     * Run the stream update task.
     */

    public void
	run ()
    {
	Thread.currentThread().setName("stream - " + getSubscriber().getName());

	long this_tick = getCorrelator().getTick();
	org.jdom.Document content_doc = buildContentDocument(this_tick);

	/**
	 * @TODO too much

	if (log_.isDebugEnabled()) {
	    log_.debug(this_tick + " " + getName());

	    String xml = org.opensims.xml.XmlBuilder.formatXML(content_doc.getRootElement(), false, true);
	    log_.debug(" content - " + xml);
	}
	 */

	getSubscriber().pushStream(content_doc, getName());
	setLastRefresh(this_tick);
    }
}
