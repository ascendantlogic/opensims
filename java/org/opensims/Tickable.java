/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: Tickable.java,v 1.4 2007/05/29 02:02:53 brett Exp $
 * $Id: Tickable.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.4 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    Tickable
{
    public final static int EXPIRY_PERIOD = 30000;
	public final static int HOST_EXPIRY_PERIOD = 14400000;

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
     * Check the expiry date/time
     */

    public boolean
	checkExpiry (long model_tick);

}
