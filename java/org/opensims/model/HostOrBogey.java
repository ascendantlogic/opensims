/**
 * @LICENSE@
 */

package org.opensims.model;

/**
 * $CVSId: HostOrBogey.java,v 1.27 2005/12/04 17:47:22 mikee Exp $
 * $Id: HostOrBogey.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.27 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. Erwin <mikee@symbiot.com>
 */

public interface
    HostOrBogey
    extends Comparable,
	    org.opensims.Tickable,
	    org.opensims.Exportable
{
    // public definitions

    public final static String UNKNOWN_OS = "??";


    /**
     * Represent as a String
     */

    public String
        getSummary ();


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
    // Comparable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Compares this object with the specified object for order
     */

    public int
	compareTo (Object o);


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the IP address
     */

    public org.opensims.IPv4
	getIPv4 ()
	throws Exception;


    /**
     * Set the IP address of this host
     */

    public void
	setIPv4 (org.opensims.IPv4 ipv4);


    /**
     * Get the IP address as a String
     */

    public String
	getIpAddr ();


    /**
     * Get the database ID
     */

    public String
	getHostId ();


    /**
     * Set the database ID
     */

    public void
	setHostId (String host_id);


    /**
     * Get the geolocation info
     */

    public org.opensims.geoloc.CountryInfo
	getCountryInfo ();


    /**
     * Set the geolocation info
     */

    public void
	setCountryInfo (org.opensims.geoloc.CountryInfo country_info);


    /**
     * Get the XML node
     */

    public org.jdom.Element
	getNode ();


    /**
     * Get the XML node
     */

    public void
	setNode (org.jdom.Element node);


    /**
     * Is this a host or a bogey?
     */

    public boolean
	isBogey ();


    /**
     * Get the incident role
     */

    public String
	getRole ();


    /**
     * Get the warning flag.
     */

    public boolean
	getWarningFlag ();


    /**
     * Set the warning flag.
     */

    public void
	setWarningFlag (boolean warning_flag);


    //////////////////////////////////////////////////////////////////////
    // Reportable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Format an XML node for rendering a report
     */

    public org.jdom.Element
	getReportNode ();


    /**
     * Lookup the domain name.
     */

    public String
	getDomainName ();
	
	
	/**
     * Sets a group to this Host, returns the group added or null
     */

    public String
	setGroup (String group);
	
	
    /**
     * Sets a group to this Host
     */

    public String
	setGroup ();	


    /**
     * Lookup the groups/orgs associated with this host.
     *  (comma delimeted list)
     */

    public String
	getGroups ();


    /**
     * Removes the Group Association from this Host
     */

    public void
	removeGroup (String group);


    /**
     * Lookup the provided groups and determine it it's associated with this host.
     */

    public boolean
	isInGroup (String group);


    //////////////////////////////////////////////////////////////////////
    // Exportable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Export as an XML node
     */

    public org.jdom.Element
        getExportNode ();
}
