/**
 * @LICENSE@
 */

package org.opensims.alert;

/**
 * $CVSId: Alert.java,v 1.13 2007/02/20 00:42:36 jeff Exp $
 * $Id: Alert.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.13 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    Alert
    extends Comparable,
	    org.opensims.Tickable,
	    org.opensims.Exportable
{
    /**
     * Represent as a String
     */

    public String
	getSummary ();


    //////////////////////////////////////////////////////////////////////
    // Comparable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Compares this object with the specified object for order
     */

    public int
	compareTo (Object o);

    //////////////////////////////////////////////////////////////////////
    // Incident methods
    //////////////////////////////////////////////////////////////////////

	/**
	 * returns earliest incident's tick
	 */
	public long
	firstTick();

	/**
	 * record an additional incident of this alert
	 */
	public void
	registerIncident( long new_tick );

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
     * Check the expiry date/time
     */

    public boolean
	checkExpiry (long model_tick);


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the database ID
     */

    public String
	getAlertId ();


    /**
     * Get the source host
     */

    public org.opensims.model.HostOrBogey
	getSrcHost ();


    /**
     * Get the destination host
     */

    public org.opensims.model.HostOrBogey
	getDstHost ();


    //////////////////////////////////////////////////////////////////////
    // Reportable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Format an XML node for rendering a report
     */

    public org.jdom.Element
	getReportNode ();


    //////////////////////////////////////////////////////////////////////
    // Exportable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Format an XML node for export
     */

    public org.jdom.Element
	getExportNode ();


    /**
     * Format an XML node for export - for the <CON/> elements
     */

    public org.jdom.Element
	getConnectionExportNode ();
}
