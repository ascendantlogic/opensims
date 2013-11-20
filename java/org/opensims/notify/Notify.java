/**
 * @LICENSE@
 */

package org.opensims.notify;

/**
 * $CVSId: Notify.java,v 1.11 2004/10/02 17:16:37 paco Exp $
 * $Id: Notify.java 1 2008-01-10 18:37:05Z smoot $
 * Notification adapter
 * @version $Revision: 1.11 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    Notify
    extends org.opensims.Tickable
{
    /**
     * Initialize the notification object.
     */

    public void
	init (org.opensims.notify.NotifyManager notify_manager, org.jdom.Element notify_node);


    /**
     * Represent as a String
     */

    public String
	getSummary ();


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the XML configuration node.
     */

    public org.jdom.Element
	getNotifyNode ();


    /**
     * Set the XML configuration node.
     */

    public void
	setNotifyNode (org.jdom.Element notify_node);


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
     * Get the enabled flag.
     */

    public boolean
	getEnabled ();


    /**
     * Set the enabled flag.
     */

    public void
	setEnabled (boolean enabled);


    //////////////////////////////////////////////////////////////////////
    // alert handling methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Test whether the notification should fire
     */

    public void
	testTrigger (org.opensims.alert.Alert alert);


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
     * keep the notify alive
     */

    public boolean
	checkExpiry (long model_tick);
}
