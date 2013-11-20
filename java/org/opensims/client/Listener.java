/**
 * @LICENSE@
 */

package org.opensims.client;

/**
 * $CVSId: Listener.java,v 1.2 2004/07/02 08:40:46 paco Exp $
 * $Id: Listener.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.2 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    Listener
    extends Runnable
{
    /**
     * Initialize the listener
     */

    public void
	init (org.opensims.Correlator correlator, int port, Class subscriber_class);


    /**
     * Start the listener
     */
    
    public void 
	start ();


    /**
     * Stop the listener
     */

    public void 
	stop ();


    /**
     * Run the listener
     */

    public void 
	run ();
}
