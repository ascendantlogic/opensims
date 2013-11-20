/**
 * @LICENSE@
 */

package org.opensims.notify;

/**
 * $CVSId: GenericNotify.java,v 1.7 2004/10/12 19:28:43 paco Exp $
 * $Id: GenericNotify.java 1 2008-01-10 18:37:05Z smoot $
 * Notification adapter
 * @version $Revision: 1.7 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    GenericNotify
    implements org.opensims.notify.Notify
{
    // protected fields

    protected org.opensims.Manager manager = null;
    protected org.jdom.Element notify_node = null;
    protected String name = null;
    protected boolean enabled = false;
    protected long last_tick = 0L;

    // quality assurance

    private static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.notify.GenericNotify.class.getName());


    /**
     * Initialize the notification object.
     */

    public void
	init (org.opensims.notify.NotifyManager notify_manager, org.jdom.Element notify_node)
    {
	try {
	    // <NOTIFY name="NRL" enabled="true" />

	    setManager(notify_manager);
	    setLastTick(getManager().getCorrelator().getTick());
	    setNotifyNode(notify_node);

	    setName(org.opensims.Config.fixNull(getNotifyNode().getAttributeValue("name"), "generic_" + hashCode()));
	    setEnabled(Boolean.valueOf(org.opensims.Config.fixNull(getNotifyNode().getAttributeValue("enabled"), "false")).booleanValue());
	}
	catch (Exception e) {
	    log_.error("initialize notify", e);
	}
    }


    /**
     * Represent as a String
     */

    public String
	getSummary ()
    {
	StringBuffer buf = new StringBuffer();

	buf.append(getManager().getCorrelator().formatTick(getLastTick()));
	buf.append(" - ");
	buf.append(getKey());

	return buf.toString();
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the XML configuration node.
     */

    public org.jdom.Element
	getNotifyNode ()
    {
	return notify_node;
    }


    /**
     * Set the XML configuration node.
     */

    public void
	setNotifyNode (org.jdom.Element notify_node)
    {
	this.notify_node = notify_node;
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
     * Get the enabled flag.
     */

    public boolean
	getEnabled ()
    {
	return enabled;
    }


    /**
     * Set the enabled flag.
     */

    public void
	setEnabled (boolean enabled)
    {
	this.enabled = enabled;
    }


    //////////////////////////////////////////////////////////////////////
    // alert handling methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Test whether the notification should fire
     */

    public void
	testTrigger (org.opensims.alert.Alert alert)
    {
	if (getEnabled()) {
	    String text = alert.toString();

	    if (log_.isDebugEnabled()) {
		log_.debug("NOTIFY " + text);
	    }

	    sendAlert(alert);
	}
    }


    /**
     * Send the given alert
     */

    public void
	sendAlert (org.opensims.alert.Alert alert)
    {
	// do sumphin hear
    }


    //////////////////////////////////////////////////////////////////////
    // Tickable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Manager
     */

    public org.opensims.Manager
        getManager ()
    {
	return manager;
    }


    /**
     * Set the Manager
     */

    public void
        setManager (org.opensims.Manager manager)
    {
	this.manager = manager;
    }


    /**
     * Get the last_tick value - for expiry
     */

    public long
	getLastTick ()
    {
	return last_tick;
    }


    /**
     * Set the last_tick value - for expiry
     */

    public void
	setLastTick (long last_tick)
    {
	this.last_tick = last_tick;
    }


    /**
     * Get the hashtable lookup key
     */

    public String
	getKey ()
    {
	return getName();
    }


    /**
     * Test whether a conditon warrans that the notify stays alive
     */

    public boolean
	checkExpiry (long model_tick)
    {
        boolean result = false;

	// other classes might override for temporary notifications

        return result;
    }
}
