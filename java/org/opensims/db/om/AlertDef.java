/**
 * @LICENSE@
 */

package org.opensims.db.om;

/**
 * The skeleton for this class was autogenerated by Torque on:
 *
 * [Mon Aug 09 19:10:11 EDT 2004]
 *
 * <P>
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 * </P>
 *
 * $CVSId: AlertDef.java,v 1.13 2006/10/01 23:56:07 mikee Exp $
 * $Id: AlertDef.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.13 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 * @see <A href="/docs/torque/opensims-schema.html#alert_def">Torque-generated SQL Database Schema</A>
 */

public class
    AlertDef
    extends org.opensims.db.om.BaseAlertDef
    implements org.apache.torque.om.Persistent,
	       org.opensims.alert.AlertDef	       
{
    // protected fields

    protected org.jdom.Element node = null;
    protected java.util.ArrayList filter_rules = new java.util.ArrayList();
    protected java.util.ArrayList filter_nodes = new java.util.ArrayList();

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.db.om.AlertDef.class.getName());



    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the XML node
     */

    public org.jdom.Element
	getNode ()
    {
		return node;
    }


    /**
     * Set the XML node
     */

    public void
	setNode (org.jdom.Element node)
    {
		this.node = node;
    }


    /**
     * Get the alert_type
     */

    public org.opensims.alert.AlertType
	getType ()
    {
		org.opensims.alert.AlertType result = null;
	
		try {
			result = (org.opensims.alert.AlertType) getAlertType();
		}
		catch (Exception e) {
			log_.error("get alert type", e);
		}
	
		return result;
    }


    /**
     * Enable the alert rule
     */

    public void
	enable_rule ()
    {
    	getNode().setAttribute("enabled","true");
    }


    /**
     * Disable the alert rule
     */

    public void
	disable_rule ()
    {
    	getNode().setAttribute("enabled","false");
    }


    /**
     * Sets the alert's type
     */

    public void
	set_type (org.opensims.alert.AlertType alert_type)
    {
    	try {
			// Update DB Link
			this.setAlertType((org.opensims.db.om.AlertType) alert_type);
			this.save();
		}
		catch (Exception e) {
			log_.error("set alert type", e);
		}
    }

    
    /**
     * Query if the rule is enabled
     */

    public boolean
	isEnabled ()
    {
    	boolean ena_flag = true;
    	
    	if (getNode().getAttributeValue("enabled") != null) {
    		ena_flag = Boolean.valueOf(getNode().getAttributeValue("enabled")).booleanValue();
    	}
    	
    	return (ena_flag);
    }


    /**
     * Get the alert description
     */

    public String
	getDescription ()
    {
		return getNode().getAttributeValue("msg");
    }


    //////////////////////////////////////////////////////////////////////
    // alert filtering
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse the XML defintions for alert filter rules.
     */

    public void
	parseFilterRules (org.jdom.Element alert_def_node)
    {
		try {
			if (alert_def_node != null) {
				filter_rules = new java.util.ArrayList();
				filter_nodes = new java.util.ArrayList();
		
				java.util.Iterator filter_iter = alert_def_node.getChildren().iterator();
		
				while (filter_iter.hasNext()) {
					org.jdom.Element filter_node = (org.jdom.Element) filter_iter.next();
		
					filter_nodes.add(filter_node);
		
					if (filter_node.getName().equals(org.opensims.xml.Node.FILTER_RULE_NODE)) {
						org.opensims.alert.Filter filter = new org.opensims.alert.Filter(filter_node);
			
						if (log_.isDebugEnabled()) {
							String xml = org.opensims.xml.XmlBuilder.formatXML(filter_node, false, true);
							log_.debug("parse filter rule: " + getUniqueId() + " - " + xml);
						}
			
						filter_rules.add(filter);
					}
				}
			}
		}
		catch (Exception e) {
			log_.error("parse filter rules", e);
		}
    }


    /**
     * Return a threat estimator.
     */

    public double
	estimateThreat (org.opensims.db.om.Alert alert, org.opensims.db.om.Host host, org.opensims.db.om.Host bogey)
	{
		double threat_estimator = 0.0;
	
		try {
			String type_name = getAlertType().getName();
			org.opensims.db.om.AlertType alert_type = (org.opensims.db.om.AlertType) host.getManager().getCorrelator().getAlertManager().lookupAlertTypeByName(type_name);
	
			// apply XPath to get "threat_node" from the estimators doc
	
			double full_threat = 0;
			org.jdom.Element type_node = alert_type.getNode();
			if (type_node != null) {
				full_threat = Double.valueOf(type_node.getAttributeValue("threat")).doubleValue();
			}
			
			threat_estimator = full_threat;
	
			// apply the local filter rules
			for (int i = 0; i < filter_rules.size(); i++)
			{
				org.opensims.alert.Filter filter = (org.opensims.alert.Filter) filter_rules.get(i);
				
				if (log_.isDebugEnabled())
				{
					log_.debug("test filter rule: " + getUniqueId() + " - " + threat_estimator);
				}
				
				if (filter.eval(alert, host, bogey))
				{
					return filter.apply(full_threat);
				}
			}
		}
		catch (Exception e) {
			log_.error("estimate threat", e);
		}
	
		return threat_estimator;
	}


    //////////////////////////////////////////////////////////////////////
    // Reportable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Format an XML node for rendering a report
     */

    public org.jdom.Element
	getReportNode ()
	{
		org.jdom.Element report_node = (org.jdom.Element) getNode().clone();
	
		// apply the local filter nodes
	
		for (int i = 0; i < filter_nodes.size(); i++) {
			org.jdom.Element filter_node = (org.jdom.Element) filter_nodes.get(i);
	
			report_node.addContent((org.jdom.Element) filter_node.clone());
		}
	
		if (log_.isDebugEnabled()) {
			String xml = org.opensims.xml.XmlBuilder.formatXML(report_node, false, true);
			log_.debug("alert def report: " + xml);
		}
	
		return report_node;
	}
}
