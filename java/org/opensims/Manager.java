/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: Manager.java,v 1.5 2005/09/26 18:58:23 mikee Exp $
 * $Id: Manager.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.5 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    Manager
    extends java.util.Hashtable
{
    // protected fields

    protected org.opensims.Correlator correlator = null;
    protected String name = "Manager";

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.Manager.class.getName());


    /**
     * Constructor
     */

    public
	Manager (org.opensims.Correlator correlator, String name)
    {
	super();

	setCorrelator(correlator);
	setName(name);
    }


    //////////////////////////////////////////////////////////////////////
    // access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Correlator object.
     */

    public org.opensims.Correlator
	getCorrelator ()
    {
	return correlator;
    }


    /**
     * Set the Correlator object.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator)
    {
	this.correlator = correlator;
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


    //////////////////////////////////////////////////////////////////////
    // Tickable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Removed the expired items
     */

    public void
	removeExpired (long model_tick)
    {
    long expired_items = 0L;
    
		if (log_.isDebugEnabled()) {
			if (size() > 0) {
			log_.debug("expire among " + size() + " items in " + getName() + " " + this.toString());
			}
		}
	
		for (java.util.Enumeration e = elements(); e.hasMoreElements(); ) {
			org.opensims.Tickable item = (org.opensims.Tickable) e.nextElement();
	
			if (item.checkExpiry(model_tick)) {
				String key = item.getKey();
		
				if (log_.isDebugEnabled()) {
					log_.debug("delist " + item + " " + key + " model tick " + model_tick + " vs. item tick " + item.getLastTick());
					expired_items++;
				}
			}
		}
		
		if (log_.isDebugEnabled()) {
			log_.debug("expired " + expired_items + " items in " + getName() + " " + this.toString());
		}
    }
}
