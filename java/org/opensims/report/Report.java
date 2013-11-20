/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: Report.java,v 1.13 2006/06/29 20:55:47 jeff Exp $
 * $Id: Report.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.13 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Jeff PIHL <jeff@outer.net>
 */

public interface
    Report
    extends org.opensims.Tickable
{
    /**
     * Initialize the report object
     */

    public void
	init (org.opensims.report.ReportManager report_manager, org.jdom.Element config_node);


    /**
     * Represent as a String
     */

    public String
	getSummary ();


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////


    /**
     * Get the config node.
     */

    public org.jdom.Element
	getConfigNode ();


    /**
     * Set the config node.
     */

    public void
	setConfigNode (org.jdom.Element config_node);


    /**
     * Get the report name.
     */

    public String
	getName ();


    /**
     * Set the report name.
     */

    public void
	setName (String name);


	/**
	 * get the report signature per the navigate object
	 */

	 public String
	 getSignature(org.opensims.report.Navigate navigate);
	 
	 
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


    /**
     * Get the XSLT transform for the specific content.
     */

    public org.opensims.xml.XslTransform
	getContentTransform ()
	throws Exception;


    //////////////////////////////////////////////////////////////////////
    // content formatting
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the XML content node for formatting.
     */

    public org.jdom.Element
	getContentNode (org.opensims.report.Navigate navigate)
	throws org.opensims.report.ReportException;


    /**
     * Transform the XML content node using this report's XSLT transform.
     */

    public org.jdom.Element
	transformContentNode (org.jdom.Element content_node, org.opensims.report.Navigate navigate)
	throws org.opensims.report.ReportException;


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
}
