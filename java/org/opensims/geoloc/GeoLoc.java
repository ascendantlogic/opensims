/**
 * @LICENSE@
 */

package org.opensims.geoloc;

/**
 * $CVSId: GeoLoc.java,v 1.10 2004/09/01 20:54:51 paco Exp $
 * $Id: GeoLoc.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.10 $
 * @author Linas VARDYS <linas@symbiot.com>
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>
 * Contains methods for looking up IP address geographical info.
 * </P>
 */

public interface
    GeoLoc
{
    /**
     * Initialize external data resources, as needed.
     */

    public void
	init (org.opensims.Correlator correlator, org.jdom.Element node);


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Correlator.
     */

    public org.opensims.Correlator
	getCorrelator ();


    /**
     * Set the Correlator.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator);


    /**
     * Get the config node.
     */

    public org.jdom.Element
	getNode ();


    /**
     * Set the config node.
     */

    public void
	setNode (org.jdom.Element node);


    /**
     * Get the fixer XML document.
     */

    public org.jdom.Document
	getFixerDoc ();


    /**
     * Set the fixer XML document.
     */

    public void
	setFixerDoc (org.jdom.Document fixer_doc);


    /**
     * Fix the details for a country lookup.
     */

    public org.jdom.Element
	getFixerNodeByCode (String country_code);


    /**
     * Wrap the lookup results as an object
     */

    public org.opensims.geoloc.CountryInfo
	getCountryInfoByIpAddr (String ip_addr);
}
