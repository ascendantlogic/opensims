/**
 * @LICENSE@
 */

package org.opensims.model;

/**
 * $CVSId: BogeyManager.java,v 1.30 2007/04/22 16:47:10 mikee Exp $
 * $Id: BogeyManager.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.30 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike ERWIN <mikee@symbiot.com>
 */

public class
    BogeyManager
    extends org.opensims.Manager
{
    // protected fields
    protected org.opensims.geoloc.GeoLoc geoloc = null;

    // quality assurance
    private static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.model.BogeyManager.class.getName());


    /**
     * Constructor
     */

    public
	BogeyManager (org.opensims.Correlator correlator)
    {
		super(correlator, org.opensims.model.BogeyManager.class.getName());
	
		// instantiate the geolocation data source
		try {
			org.jdom.Element geoloc_node = getCorrelator().getConfig().getRootElement().getChild(org.opensims.xml.Node.GEOLOC_NODE);
	
			if (geoloc_node != null) {
				String class_name = geoloc_node.getAttributeValue("class");
				Class c = Class.forName(class_name);
		
				if (log_.isInfoEnabled()) {
					log_.info("geoloc class: " + class_name);
				}
		
				setGeoLoc((org.opensims.geoloc.GenericGeoLoc) c.newInstance());
				getGeoLoc().init(getCorrelator(), geoloc_node);
			}
		}
        catch (Exception e) {
            log_.error("instantiate geoloc data source", e);
        }
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the GeoLoc object
     */

    public org.opensims.geoloc.GeoLoc
	getGeoLoc ()
    {
		return geoloc;
    }


    /**
     * Set the GeoLoc object
     */

    public void
	setGeoLoc (org.opensims.geoloc.GeoLoc geoloc)
    {
		this.geoloc = geoloc;
    }


    //////////////////////////////////////////////////////////////////////
    // bogey management
    //////////////////////////////////////////////////////////////////////

    /**
     * Create a new instance of a HostOrBogey
     */

    public synchronized org.opensims.model.HostOrBogey
	createHostOrBogey (String ip_addr_str, org.opensims.agent.Agent agent, org.jdom.Element node, boolean update)
	{
		org.opensims.db.om.Host host = null;
		org.opensims.geoloc.CountryInfo country_info = null;
	
		// select or insert a host ("sinsert")
		try {
			org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
			crit.add(org.opensims.db.om.HostPeer.IP_ADDR, ip_addr_str);
	
			// try to find an existing row in the "host" table
			java.util.Iterator host_iter = org.opensims.db.om.HostPeer.doSelect(crit).iterator();
	
			if (host_iter.hasNext()) {
				host = (org.opensims.db.om.Host) host_iter.next();
				if (log_.isDebugEnabled()) {
					log_.debug("Torque host found: " + host.toString());
				}
			}
	
			// determine the geolocation info
			if (getGeoLoc() != null) {
				country_info = getGeoLoc().getCountryInfoByIpAddr(ip_addr_str);
			}
	
			// not found, create a new row
			if (host == null) {
				host = new org.opensims.db.om.Host();
				boolean isDefender = false;
		
				if (log_.isDebugEnabled()) {
					log_.debug("Torque - adding host: " + ip_addr_str);
				}
		
				host.setIpAddr(ip_addr_str);
				host.setCreated(new java.util.Date());
				host.setCountryInfo(country_info);
				host.setLastTick(getCorrelator().getTick());
		
				// track the relation to the network model
				org.opensims.IPv4 ip_addr = new org.opensims.IPv4(ip_addr_str);
				if (agent != null) {
					for (java.util.Enumeration e = getCorrelator().getScanManager().elements(); e.hasMoreElements(); ) {
						org.opensims.model.Scan scan = (org.opensims.model.Scan) e.nextElement();
						if (scan.getNetworkId().includes(ip_addr)) {
							host.setNetwork(scan.getNetworkNumber());
							isDefender = true;
						}
					}
				}
				if (log_.isDebugEnabled()) {
					log_.debug("Torque - host added: " + host.toString());
				}
	
				// All hosts require a link to the "admin_group"
				// however, the guest_group by default has NO links;
				// we don't filter on attackers by group
				if (isDefender) {
					host.setGroup();
				}
				host.save();
			}
			else if (agent == null) {
				// only for "bogey" objects
				if (update) {
					host.setLastTick(getCorrelator().getTick());
				}
		
				// XML extended data does not require save to DB
				host.setCountryInfo(country_info);
			}
	
			// initialize any non-persistent fields
			if (node != null) {
				host.setNode(node);
			}
			host.setManager(this);
		}
		catch (Exception e) {
			log_.error("using Torque to access host table", e);
		}
	
		return host;
	}


    /**
     * Create a new instance of a HostOrBogey (different input variables)
     */

    public synchronized org.opensims.model.HostOrBogey
	createHostOrBogey (String ip_addr_str, String networkID, org.jdom.Element node, boolean update)
	{
		org.opensims.db.om.Host host = null;
		org.opensims.geoloc.CountryInfo country_info = null;
	
		// select or insert a host ("sinsert")
		try {
			org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
			crit.add(org.opensims.db.om.HostPeer.IP_ADDR, ip_addr_str);
	
			// try to find an existing row in the "host" table
			java.util.Iterator host_iter = org.opensims.db.om.HostPeer.doSelect(crit).iterator();
	
			if (host_iter.hasNext()) {
				host = (org.opensims.db.om.Host) host_iter.next();
				if (log_.isDebugEnabled()) {
					log_.debug("Torque host found: " + host.toString());
				}
			}
	
			// determine the geolocation info
			if (getGeoLoc() != null) {
				country_info = getGeoLoc().getCountryInfoByIpAddr(ip_addr_str);
			}
	
			// not found, create a new row
			if (host == null) {
				host = new org.opensims.db.om.Host();
				boolean isDefender = (networkID != null);
		
				if (log_.isDebugEnabled()) {
					log_.debug("Torque - adding host: " + ip_addr_str);
				}
		
				host.setIpAddr(ip_addr_str);
				host.setCreated(new java.util.Date());
				host.setCountryInfo(country_info);
				host.setLastTick(getCorrelator().getTick());
		
				// track the relation to the network model
				host.setNetwork(networkID);
				
				if (log_.isDebugEnabled()) {
					log_.debug("Torque - host added: " + host.toString());
				}
	
				// All hosts require a link to the "admin_group"
				// however, the guest_group by default has NO links;
				// we don't filter on attackers by group
				if (isDefender) {
					host.setGroup();
				}
				host.save();
			}
			else {
				// only for "bogey" objects
				if (update) {
					host.setLastTick(getCorrelator().getTick());
				}
		
				// XML extended data does not require save to DB
				host.setCountryInfo(country_info);
			}
	
			// initialize any non-persistent fields
			if (node != null) {
				host.setNode(node);
			}
			host.setManager(this);
		}
		catch (Exception e) {
			log_.error("using Torque to access host table", e);
		}
	
		return host;
	}


    /**
     * Create a new instance of a HostOrBogey, specifically a "bogey"
     */

    public org.opensims.model.HostOrBogey
	createBogey (String ip_addr_str, boolean update)
	{
		org.opensims.model.HostOrBogey host = createHostOrBogey(ip_addr_str, (org.opensims.agent.Agent) null, null, update);
	
		if (update) {
			put(ip_addr_str, host);
		}
	
		return host;
	}


    /**
     * Lookup a bogey, by IP address - from the database
     */

    public org.opensims.model.HostOrBogey
	lookupBogeyByIpAddr (String ip_addr_str, boolean update)
	{
		org.opensims.model.HostOrBogey bogey = (org.opensims.model.HostOrBogey) get(ip_addr_str);
	
		if (bogey != null) {
			if (update) {
				bogey.setLastTick(getCorrelator().getTick());
			}
		}
		else {
			bogey = createBogey(ip_addr_str, update);
		}
	
		return bogey;
	}


    //////////////////////////////////////////////////////////////////////
    // Tickable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Removed the expired hosts
     */

    public void
	removeExpired (long model_tick)
	{
		for (java.util.Enumeration e = elements(); e.hasMoreElements(); ) {
			org.opensims.db.om.Host host = (org.opensims.db.om.Host) e.nextElement();
	
			if (host.checkExpiry(model_tick)) {
				String key = host.getKey();
				if(host.isModified()) {
					try {
						host.save();
						if (log_.isDebugEnabled()) {
							log_.debug("saving dirty host " + host + " " + key + " tick " + model_tick);
						}
					} catch (Exception ex) {
						log_.error("unable to save dirty host " + host + " " + key, ex);
					}
				}
				if (log_.isDebugEnabled()) {
					log_.debug("delist " + host + " " + key + " tick " + model_tick);
				}
				remove(key);
			}
		}
	}

}
