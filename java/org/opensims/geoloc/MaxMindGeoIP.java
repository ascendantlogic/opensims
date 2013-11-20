/**
 * @LICENSE@
 */

package org.opensims.geoloc;

/**
 * $CVSId: MaxMindGeoIP.java,v 1.11 2004/09/02 05:50:10 paco Exp $
 * $Id: MaxMindGeoIP.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.11 $
 * @author Linas VARDYS <linas@symbiot.com>
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>
 * Contains methods for looking up IP address geographical info.
 * </P>
 */

public class
    MaxMindGeoIP
    extends org.opensims.geoloc.GenericGeoLoc
{
    // protected fields

    protected com.maxmind.geoip.LookupService geoip_resource = null;
       
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.geoloc.MaxMindGeoIP.class.getName());


    /**
     * Initialize external data resources, as needed.
     */

    public void
	init (org.opensims.Correlator correlator, org.jdom.Element node)
    {
	super.init(correlator, node);

	try{
	    java.io.File geoloc_file = new java.io.File(getNode().getAttributeValue("data"));

	    if (geoloc_file.canRead()) {
		geoip_resource = new com.maxmind.geoip.LookupService(geoloc_file.getPath());

		if (log_.isInfoEnabled()) {
		    log_.info("resource: " + geoip_resource + " using: " + geoloc_file.toString());
		}
	    }
	    else {
		log_.error("cannot load geolocation resource from " + geoloc_file.toString());
	    }
	}
	catch (Exception e) {
	    log_.error("getting LookupService resource", e);
	}
    }


    /**
     * Wrap the lookup results as an object
     */

    public org.opensims.geoloc.CountryInfo
	getCountryInfoByIpAddr (String ip_addr)
    {
	if (log_.isDebugEnabled()) {
	    log_.debug("get country info - |" + ip_addr + "|");
	}

	// access GeoIP database

	org.opensims.geoloc.GenericCountryInfo country_info = new org.opensims.geoloc.GenericCountryInfo();
	com.maxmind.geoip.Country country = geoip_resource.getCountry(org.opensims.Config.fixNull(ip_addr, ""));

	// prepare the country info object

	if (country != null) {
	    String country_code = org.opensims.Config.fixNull(country.getCode().toUpperCase(), org.opensims.geoloc.CountryInfo.UNKNOWN_CODE);

	    if (country_code.equals("--")) {
		country_info.setCode(org.opensims.geoloc.CountryInfo.UNKNOWN_CODE);
		country_info.setName(org.opensims.geoloc.CountryInfo.UNKNOWN_NAME);
	    }
	    else {
		country_info.setCode(country_code);
		country_info.setName(org.opensims.Config.fixNull(country.getName(), org.opensims.geoloc.CountryInfo.UNKNOWN_NAME));
	    }

	    // note whether the country is known

	    if (!country_info.getCode().equals(org.opensims.geoloc.CountryInfo.UNKNOWN_CODE)) {
		country_info.setIsKnown(true);
	    }
		
	    // attempt to fix the country info

	    country_info.setFixerNode(getFixerNodeByCode(country_info.getCode()));
	}

	if (log_.isDebugEnabled()) {
	    log_.debug("country info " + ip_addr + " " + country_info.toString());
	}
		
	return country_info;
    }	
}
