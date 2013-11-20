/**
 * @LICENSE@
 */

package org.opensims.alert;

/**
 * $CVSId: AlertDef.java,v 1.10 2006/10/01 23:56:07 mikee Exp $
 * $Id: AlertDef.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.10 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    AlertDef
{
    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the XML node
     */

    public org.jdom.Element
	getNode ();


    /**
     * Set the XML node
     */

    public void
	setNode (org.jdom.Element node);


    /**
     * Get the alert unique_id
     */

    public String
	getUniqueId ();


    /**
     * Get the alert_type
     */

    public org.opensims.alert.AlertType
	getType ();


    /**
     * Enable the alert rule
     */

    public void
	enable_rule ();


    /**
     * Disable the alert rule
     */


    public void
	disable_rule ();


    /**
     * Sets the alert's type
     */

    public void
	set_type (org.opensims.alert.AlertType alert_type);
	
    
    /**
     * Query if the rule is enabled
     */

    public boolean
	isEnabled ();


    /**
     * Get the alert description
     */

    public String
	getDescription ();



    //////////////////////////////////////////////////////////////////////
    // alert filtering
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse the XML defintion for alert filter rules.
     */

    public void
	parseFilterRules (org.jdom.Element alert_def_node);


    //////////////////////////////////////////////////////////////////////
    // Reportable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Format an XML node for rendering a report
     */

    public org.jdom.Element
	getReportNode ();
}
