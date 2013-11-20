/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: GenericReport.java,v 1.22 2006/07/26 23:12:29 jeff Exp $
 * $Id: GenericReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.22 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Jeff PIHL <jeff@outer.net>
 */

public class
    GenericReport
    implements org.opensims.report.Report
{
    // protected fields

    protected org.opensims.Manager manager = null;
    protected org.jdom.Element config_node = null;
    protected String name = null;
    protected boolean enabled = false;
    protected long last_tick = 0L;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.GenericReport.class.getName());


    /**
     * Initialize the report object
     */

    public void
	init (org.opensims.report.ReportManager report_manager, org.jdom.Element config_node)
    {
	setManager(report_manager);

	try {
	    // <REPORT enabled="false" name="Incident Report" class="org.opensims.report.IncidentReport" transform="rpt_incident.xsl"/>

	    setConfigNode(config_node);
	    setName(getConfigNode().getAttributeValue("name"));
	    setEnabled(getConfigNode().getAttribute("enabled").getBooleanValue());
	}
	catch (Exception e) {
	    log_.error("parse report config", e);
	}

	setLastTick(getManager().getCorrelator().getTick());
    }


    /**
     * Represent as a String
     */

    public String
	getSummary ()
    {
	StringBuffer buf = new StringBuffer();

	buf.append(getKey());
	buf.append("\t");
	buf.append(this.toString());
	buf.append("\n\t enabled:");
	buf.append(getEnabled());

	return buf.toString();
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the config node.
     */

    public org.jdom.Element
	getConfigNode ()
    {
	return config_node;
    }


    /**
     * Set the config node.
     */

    public void
	setConfigNode (org.jdom.Element config_node)
    {
	this.config_node = config_node;
    }


    /**
     * Get the report name.
     */

    public String
	getName ()
    {
	return name;
    }


    /**
     * Set the report name.
     */

    public void
	setName (String name)
    {
	this.name = name;
    }


	/**
	 * get the report signature per the navigate object
	 */

	 public String
	 getSignature(org.opensims.report.Navigate navigate)
	 {
	 	StringBuffer sig = new StringBuffer();
	 	if ( navigate == null ) return null;
		sig.append( navigate.getOrganization() );
		sig.append( ":" + navigate.getParamValue("name") );
	 	if ( navigate.useDateRange() ) {
			if ( navigate.isDateRangeComplete() ) {
				sig.append( ":complete" );
			} else {
				sig.append( ":partial" );
			}
			sig.append( ":" + navigate.getFromDateSignature() );
			sig.append( ":" + navigate.getToDateSignature() );
	 	} else {
			sig.append( ":" + navigate.getParamValue("time_limit") );
		}
		sig.append( ":" + navigate.getParamValue("order_by") );
		sig.append( ":" + navigate.getParamValue("order_dir") );
		sig.append( ":" + navigate.getParamValue("result_limit") );
		return sig.toString();
	 }
	 
	 
    /**
     * Get the enabled flag.
     */

    public boolean
	getEnabled ()
    {
	return enabled;
    }


    /**
     * Set the enabled flag.
     */

    public void
	setEnabled (boolean enabled)
    {
	this.enabled = enabled;
    }


    /**
     * Get the XSLT transform for the specific content.
     */

    public org.opensims.xml.XslTransform
	getContentTransform ()
	throws Exception
    {
	java.io.File xsl_dir = new java.io.File(getManager().getCorrelator().getConfig("webapp.dir"), "xsl");
	java.io.File xsl_file = new java.io.File(xsl_dir, getConfigNode().getAttributeValue("transform"));

	if (log_.isDebugEnabled()) {
	    log_.debug("load report transform " + getName() + " - " + xsl_file);
	}

	return new org.opensims.xml.XslTransform(xsl_file);
    }


    //////////////////////////////////////////////////////////////////////
    // content formatting
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the XML content node for formatting.
     */

    public org.jdom.Element
	getContentNode (org.opensims.report.Navigate navigate)
	throws org.opensims.report.ReportException
    {
	org.jdom.Element content_node = new org.jdom.Element(org.opensims.xml.Node.REPORT_NODE);

	// empty placeholder 

	return content_node;
    }


    /**
     * Transform the XML content node using this report's XSLT transform.
     */

    public org.jdom.Element
	transformContentNode (org.jdom.Element content_node, org.opensims.report.Navigate navigate)
	throws org.opensims.report.ReportException
    {
	org.jdom.Element site_node = null;

	try {
	    if (log_.isDebugEnabled()) {
		String xml = org.opensims.xml.XmlBuilder.formatXML(content_node, false, true);
		log_.debug("render " + getName() + " transform " + getContentTransform() + " - " + xml);
	    }

	    org.jdom.transform.JDOMSource source = new org.jdom.transform.JDOMSource(content_node);
            org.jdom.transform.JDOMResult result = new org.jdom.transform.JDOMResult();

	    getContentTransform().getTransformer().transform(source, result);

	    site_node = result.getDocument().getRootElement();

	    if (navigate.getEnabled()) {
		site_node.addContent(navigate.getReportNode());
	    }
	}
	catch (Exception e) {
	    log_.error("transform content node", e);
	    throw new org.opensims.report.ReportException(e.getMessage());
	}
	finally {
	    if (site_node == null) {
		throw new org.opensims.report.ReportException("could not render report in the requested format");
	    }
	}

        return site_node;
    }


    //////////////////////////////////////////////////////////////////////
    // Tickable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Manager
     */

    public org.opensims.Manager
        getManager ()
    {
	return manager;
    }


    /**
     * Set the Manager
     */

    public void
        setManager (org.opensims.Manager manager)
    {
	this.manager = manager;
    }


    /**
     * Get the last_tick value - for expiry
     */

    public long
	getLastTick ()
    {
	return last_tick;
    }


    /**
     * Set the last_tick value - for expiry
     */

    public void
	setLastTick (long last_tick)
    {
	this.last_tick = last_tick;
    }


    /**
     * Get the hashtable lookup key
     */

    public static String
	getKey (String name)
    {
	String key = name;

	return key;
    }


    /**
     * Get the hashtable lookup key
     */

    public String
	getKey ()
    {
	return getKey(getName());
    }


    /**
     * Check the expiry date/time
     */

    public boolean
	checkExpiry (long model_tick)
    {
	// OVERRIDE: report templates do not expire
	return false;
    }
}
