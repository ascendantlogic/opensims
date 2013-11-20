/**
 * @LICENSE@
 */

package org.opensims.alert;

/**
 * $CVSId: Filter.java,v 1.8 2005/02/09 19:40:13 dan Exp $
 * $Id: Filter.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.8 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    Filter
{
    // protected fields

    protected org.jdom.Element node = null;
    protected org.opensims.IPv4 src_cidr = null;
    protected org.opensims.IPv4 dst_cidr = null;
    protected java.util.Hashtable platform_pattern = new java.util.Hashtable();
    protected java.util.Hashtable service_pattern = new java.util.Hashtable();
    protected double coeff = 1.0;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.alert.Filter.class.getName());

    
    /**
     * Constructor.
     */

    public
	Filter (String threat_level)
    {
	setNode(null);
	parseThreatCoeff(threat_level);
    }

    /**
     * Constructor.
     */

    public
	Filter (org.jdom.Element node)
    {
	setNode(node);

	parseThreatCoeff(org.opensims.Config.fixNull(getNode().getAttributeValue("threat"), "moderate"));
	parseSrcPattern(getNode().getChild(org.opensims.xml.Node.BOGEY_NODE));
	parseDstPattern(getNode().getChild(org.opensims.xml.Node.HOST_NODE));
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the XML node.
     */

    public org.jdom.Element
	getNode ()
    {
	return node;
    }


    /**
     * Set the XML node.
     */

    public void
	setNode (org.jdom.Element node)
    {
	this.node = node;
    }


    /**
     * Get the threat reduction coefficient.
     */

    public double
	getCoeff ()
    {
	return coeff;
    }


    /**
     * Set the threat reduction coefficient.
     */

    public void
	setCoeff (double coeff)
    {
	this.coeff = coeff;
    }


    //////////////////////////////////////////////////////////////////////
    // rule parsing
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse the "threat coefficient".
     *
     * @TODO realistically, this gets replaced by a fuzzy membership function.
     */

    protected void
	parseThreatCoeff (String threat_level)
    {
	if (threat_level.equals("non-existent")) {
	    setCoeff(0.00001);
	}
	else if (threat_level.equals("very-low")) {
	    setCoeff(0.001);
	}
	else if (threat_level.equals("low")) {
	    setCoeff(0.2);
	}
	else if (threat_level.equals("high")) {
	    setCoeff(0.8);
	}
	else if (threat_level.equals("very-high")) {
	    setCoeff(1.0);
	}
	else {
	    setCoeff(0.5);
	}
    }


    /**
     * Parse the "bogey" pattern, if any.
     */

    protected void
	parseSrcPattern (org.jdom.Element src_node)
    {
	try {
	    if (src_node != null) {
		String src_cidr_str = src_node.getAttributeValue("cidr");

		if (src_cidr_str != null) {
		    src_cidr = new org.opensims.IPv4(src_cidr_str);
		}
	    }
	}
	catch (Exception e) {
	    log_.error("parse src pattern", e);
	}
    }


    /**
     * Parse the "host" pattern, if any.
     */

    protected void
	parseDstPattern (org.jdom.Element dst_node)
    {
	try {
	    if (dst_node != null) {
		String dst_cidr_str = dst_node.getAttributeValue("cidr");

		if (dst_cidr_str != null) {
		    dst_cidr = new org.opensims.IPv4(dst_cidr_str);
		}

		parseAttributePatterns(dst_node.getChild(org.opensims.xml.Node.PLATFORM_NODE), platform_pattern);
		parseAttributePatterns(dst_node.getChild(org.opensims.xml.Node.SERVICE_NODE), service_pattern);
	    }
	}
	catch (Exception e) {
	    log_.error("parse dst pattern", e);
	}
    }


    /**
     * Parse the attributes of the given XML element to populate a
     * hash table of regex patterns.
     */

    protected void
	parseAttributePatterns (org.jdom.Element pattern_node, java.util.Hashtable pattern_hash)
    {
	try {
	    if (pattern_node != null) {
		int flags =
		    java.util.regex.Pattern.CANON_EQ |
		    java.util.regex.Pattern.CASE_INSENSITIVE |
		    java.util.regex.Pattern.COMMENTS |
		    java.util.regex.Pattern.DOTALL |
		    java.util.regex.Pattern.UNICODE_CASE;

		java.util.Iterator attr_iter = pattern_node.getAttributes().iterator();

		while (attr_iter.hasNext()) {
		    org.jdom.Attribute attr = (org.jdom.Attribute) attr_iter.next();

		    String attr_name = attr.getName();
		    String attr_value = attr.getValue();

		    if (log_.isDebugEnabled()) {
			log_.debug("pattern: " + attr_name + " |" + attr_value + "|");
		    }

		    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(attr_value, flags);
		    pattern_hash.put(attr_name, pattern);
		}
	    }
	}
	catch (Exception e) {
	    log_.error("parse attribute patterns", e);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // rule evaluation
    //////////////////////////////////////////////////////////////////////

    /**
     * Evaluate the context of an alert, to test whether it matches
     * this filter.  Assume the worst case; therefore, this returns
     * true unless patterns can be found which prove that the alert
     * context does not match the filter rule antecedents.
     */

    public boolean
	eval (org.opensims.db.om.Alert alert, org.opensims.db.om.Host host, org.opensims.db.om.Host bogey)
    {
	boolean result = true;

	try {
	    if (log_.isDebugEnabled()) {
		log_.debug(alert.getAlertDef().getDescription());
		log_.debug("alert " + alert.toString());
		log_.debug("host " + host.toString());
		log_.debug(org.opensims.xml.XmlBuilder.formatXML(host.getNode(), false, true));
		log_.debug("bogey " + bogey.toString());
		log_.debug("filter " + org.opensims.xml.XmlBuilder.formatXML(getNode(), false, true));
	    }

	    // does the "bogey" pass the source IP address test, if any?

	    if (src_cidr != null) {
		if (!src_cidr.includes(bogey.getIPv4())) {
		    if (log_.isDebugEnabled()) {
			log_.debug("src cidr => false");
		    }

		    return false;
		}
	    }

	    // does the "host" pass the destination IP address test, if any?

	    if (dst_cidr != null) {
		if (!dst_cidr.includes(host.getIPv4())) {
		    if (log_.isDebugEnabled()) {
			log_.debug("dst cidr => false");
		    }

		    return false;
		}
	    }

	    // are the "platform" attributes relevant for the required
	    // patterns ... assuming the worst case?

	    if (!platform_pattern.isEmpty()) {
		if (disqualifyAttributePatterns(host.getNode().getChild(org.opensims.xml.Node.PLATFORM_NODE), platform_pattern, false)) {
		    if (log_.isDebugEnabled()) {
			log_.debug("platform => false");
		    }

		    return false;
		}
	    }

	    // are the "service" attributes relevant for the required
	    // patterns ... assuming the best case?

	    if (!service_pattern.isEmpty())
	    {
	    	org.jdom.Element service_node = host.hostServiceNodeTargetedByAlert(alert);
	    	
	    	if (disqualifyAttributePatterns(service_node, service_pattern, false))
			{
				if (log_.isDebugEnabled())
				{
					log_.debug("service => false");
				}
				
				return false;
			}
	    }
	}
	catch (Exception e) {
	    log_.error("eval filter", e);
	}

	return result;
    }


    /**
     * Test the regex patterns for the given attributes.  Returns
     * <code>default_result</code> if all of the specified attributes
     * match.
     */

    protected boolean
	disqualifyAttributePatterns (org.jdom.Element pattern_node, java.util.Hashtable pattern_hash, boolean default_result)
    {
	if (pattern_node != null) {
	    for (java.util.Enumeration e = pattern_hash.keys(); e.hasMoreElements(); ) {
		String attr_name = (String) e.nextElement();
		String attr_value = pattern_node.getAttributeValue(attr_name);

		if (attr_value != null) {
		    java.util.regex.Pattern pattern = (java.util.regex.Pattern) pattern_hash.get(attr_name);
		    java.util.regex.Matcher matcher = pattern.matcher(attr_value);

		    boolean relevant = matcher.matches();

		    if (log_.isDebugEnabled()) {
			log_.debug("test match: " + attr_name + " pattern |" + pattern.pattern() + "| value |" + attr_value + "| relevant " + relevant);
		    }

		    if (!relevant) {
			// found a name/value pair which does not
			// match the required pattern, so we stop
			// searching any further

			return true;
		    }
		}
	    }
	}

	return default_result;
    }


    /**
     * Apply this Filter to return a modified threat estimator.
     */

    public double
	apply (double threat_estimator)
    {
	double result = threat_estimator * getCoeff();

	if (log_.isDebugEnabled()) {
	    log_.debug("modified threat: " + threat_estimator + " => " + result);
	}

	return result;
    }
}
