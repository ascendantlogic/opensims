/**
 * @LICENSE@
 */

package org.opensims.client;

/**
 * $CVSId: GenericListener.java,v 1.10 2005/04/05 14:36:56 dan Exp $
 * $Id: GenericListener.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.10 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    GenericListener
    implements org.opensims.client.Listener
{
    // protected fields

    protected Thread thread = null;
    protected org.opensims.Correlator correlator = null;
    protected int port = 0;
    protected java.net.ServerSocket listener = null;
    protected Class subscriber_class = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.client.GenericListener.class.getName());


    /**
     * Initialize the listener
     */

    public void
	init (org.opensims.Correlator correlator, int port, Class subscriber_class)
    {
	try {
	    this.correlator = correlator;
	    this.port = port;
	    this.subscriber_class = subscriber_class;

	    listener = new java.net.ServerSocket(port);

	    if (log_.isInfoEnabled()) {
		log_.info("listening on port " + listener.getLocalPort());
	    }
	}
	catch (Exception e) {
	    log_.error("open server socket " + port, e);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // threading methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Start the listener
     */
    
    public void 
	start ()
    {
	thread = new Thread(this, getClass().getName() + " " + this.hashCode() + " " + port);
	thread.start();

	if (log_.isInfoEnabled()) {
	    log_.info("listener started");
	}
    }


    /**
     * Stop the listener
     */

    public void 
	stop ()
    {
	try {
	    if (listener != null) {
		listener.close();
	    }
	}
	catch (Exception e) {
	    log_.error("listener close failed", e);
	}

	thread = null;

	if (log_.isInfoEnabled()) {
	    log_.info("listener stopped");
	}
    }


    /**
     * Run the listener
     */

    public void 
	run ()
    {
	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

	if (listener != null) {
	    if (log_.isInfoEnabled()) {
		log_.info("accepting connections");
	    }

	    while (thread != null) {
		try {
		    java.net.Socket socket = listener.accept();

		    org.opensims.client.Subscriber subscriber = correlator.getSubscriberManager().createSubscriber(socket, subscriber_class);

		    Thread.yield();
		}
		catch (java.io.IOException ioe) {
		    //this generally gets called during shutdown - ignore
		}
		catch (Exception e) {
		    log_.error("socket accept failed", e);
		    return;
		}
	    }
	}
    }
}
