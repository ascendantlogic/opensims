/**
 * @LICENSE@
 */

package org.opensims.geoloc;

/**
 * $CVSId: GenericGeoLoc.java,v 1.9 2005/03/18 16:26:03 dan Exp $
 * $Id: GenericGeoLoc.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.9 $
 * @author Linas VARDYS <linas@symbiot.com>
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>
 * Contains methods for looking up IP address geographical info.
 * </P>
 */

public class
    GenericGeoLoc
    implements org.opensims.geoloc.GeoLoc
{
    // protected fields

    protected org.opensims.Correlator correlator = null;
    protected org.jdom.Element node = null;
    protected org.jdom.Document fixer_doc = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.geoloc.GenericGeoLoc.class.getName());


    /**
     * Initialize external data resources, as needed.
     */

    public void
	init (org.opensims.Correlator correlator, org.jdom.Element node)
    {
	setCorrelator(correlator);
	setNode(node);

	// attempt to load the "fixer" file

	try {
	    String fixer_file_name = getNode().getAttributeValue("fixer");

	    if (fixer_file_name != null) {
                java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
                java.io.File fixer_file = new java.io.File(var_lib, fixer_file_name);

		if (fixer_file.canRead()) {
		    if (log_.isInfoEnabled()) {
			log_.info("country fixer file: " + fixer_file);
		    }

		    java.io.FileReader file_reader = new java.io.FileReader(fixer_file);
		    java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);

		    setFixerDoc(getCorrelator().buildXml(buf_reader));
		}
	    }
	}
	catch (Exception e) {
	    log_.error("loading fixer XML doc", e);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Correlator.
     */

    public org.opensims.Correlator
	getCorrelator ()
    {
	return correlator;
    }


    /**
     * Set the Correlator.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator)
    {
	this.correlator = correlator;
    }


    /**
     * Get the config node.
     */

    public org.jdom.Element
	getNode ()
    {
	return node;
    }


    /**
     * Set the config node.
     */

    public void
	setNode (org.jdom.Element node)
    {
	this.node = node;
    }


    /**
     * Get the fixer XML document.
     */

    public org.jdom.Document
	getFixerDoc ()
    {
	return fixer_doc;
    }


    /**
     * Set the fixer XML document.
     */

    public void
	setFixerDoc (org.jdom.Document fixer_doc)
    {
	this.fixer_doc = fixer_doc;
    }


    /**
     * Fix the details for a country lookup.
     */

    public org.jdom.Element
	getFixerNodeByCode (String country_code)
    {
	org.jdom.Element fixer_node = null;

	try {
	    if (getFixerDoc() != null) {
		StringBuffer path = new StringBuffer();

		path.append("//");
		path.append(org.opensims.xml.Node.COUNTRY_NODE);
		path.append("[@iso3166 = '");
		path.append(country_code);
		path.append("']");

		org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
		fixer_node = (org.jdom.Element) xpath.selectSingleNode(getFixerDoc());
	    }
	    
	    if (fixer_node == null)
	    {
		// if the country is not available, fake it

		fixer_node = new org.jdom.Element(org.opensims.xml.Node.COUNTRY_NODE);

		fixer_node.setAttribute("name", country_code);
		fixer_node.setAttribute("iso3166", country_code);
		fixer_node.setAttribute("flag", country_code + ".jpg");
		fixer_node.setAttribute("factbook", "http://www.cia.gov/cia/publications/factbook/geos/" + country_code.toLowerCase() + ".html");
	    }
	}
	catch (Exception e) {
	    log_.error("lookup iso3166 fixer", e);
	}

	return fixer_node;
    }


    /**
     * Wrap the lookup results as an object.
     */

    public org.opensims.geoloc.CountryInfo
	getCountryInfoByIpAddr (String ip_addr)
    {
	org.opensims.geoloc.CountryInfo country_info = new org.opensims.geoloc.GenericCountryInfo();

	return country_info;
    }	
}
