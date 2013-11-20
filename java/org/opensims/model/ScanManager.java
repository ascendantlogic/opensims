/**
 * @LICENSE@
 */

package org.opensims.model;

/**
 * $CVSId: ScanManager.java,v 1.46 2007/05/26 16:06:17 mikee Exp $
 * $Id: ScanManager.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.46 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 *
 * <P>
 * This is effectively a mapping between agents (indexed by
 * "agent.getKey()") and their respective network models.  That could
 * also be handled through the AgentManager class, but this decoupling
 * allows the network models to persist even when particular agents
 * might be having communication problems.  So it makes the
 * autodiscovery and correlation processes more robust.
 * </P>
 */

public class
    ScanManager
    extends org.opensims.Manager
{
    // protected fields
    protected java.util.Hashtable host_table = new java.util.Hashtable();
    protected boolean traffic_armed = false;

    // quality assurance
    private static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.model.ScanManager.class.getName());


    /**
     * Constructor
     */

    public
	ScanManager (org.opensims.Correlator correlator)
    {
		super(correlator, org.opensims.model.ScanManager.class.getName());
    }


    //////////////////////////////////////////////////////////////////////
    // quality assurance / debugging
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the "traffic armed" flag - we're not currently using this,
     * but it provides a VERY convenient hook for debugging the
     * asynchronous traffic from particular agent plugins
     * (e.g. "symplugin-traffic")
     */

    public boolean
	getTrafficArmed ()
    {
		return traffic_armed;
    }


    /**
     * Toggle the "traffic armed" flag
     */

    public boolean
	toggleTrafficArmed ()
    {
		traffic_armed = !traffic_armed;
		return traffic_armed;
    }


    //////////////////////////////////////////////////////////////////////
    // scan session management
    //////////////////////////////////////////////////////////////////////

    /**
     * Register an agent on login
     */

    public void
	registerAgent (org.opensims.agent.Agent agent)
	{
		boolean internalScanAvail = false;
		
		try {
			// avoiding the proverbial chicken-and-the-egg problem, we
			// obtain some scan parameters from the config file...
	
			if (agent.getNetworkNode("internal") != null) 
			{
				StringBuffer path = new StringBuffer("//");
				path.append(org.opensims.xml.Node.NETWORK_NODE);
				path.append("[@topology = 'internal']/");
				path.append(org.opensims.xml.Node.SCAN_NODE);
				path.append("[@enabled = 'true']");

				org.jdom.Element internal_net_node = (org.jdom.Element) agent.getNetworkNode("internal");
				org.jdom.Element internal_scan_node = (org.jdom.Element) org.jdom.xpath.XPath.selectSingleNode(internal_net_node, path.toString());		
						
				if (internal_scan_node != null) {
					// create a Scan object, to maintain the state of autodiscovery
		
					String class_name = internal_scan_node.getAttributeValue("class");
					Class c = Class.forName(class_name);
			
					org.opensims.model.Scan scan = (org.opensims.model.Scan) c.newInstance();
			
					scan.init(this, agent, internal_scan_node, agent.getNetworkNode("internal"), agent.getNetworkNumber("internal"), agent.getNetworkDevice("internal"));
				
					// Restructure Hash Table to use network_number as a key, as opposed to the agent key
					// put(scan.getKey(), scan);
					put(agent.getNetworkNumber("internal"), scan);
					
					// Use this scan as the default for all agents that aren't directly assigned one (using the createThreads() method on the agent object --ME
					agent.setScan(scan);
					internalScanAvail = true;
				}
			}
			
			if (agent.getNetworkNode("dmz") != null) 
			{
				StringBuffer path = new StringBuffer("//");
				path.append(org.opensims.xml.Node.NETWORK_NODE);
				path.append("[@topology = 'dmz']/");
				path.append(org.opensims.xml.Node.SCAN_NODE);
				path.append("[@enabled = 'true']");
				
				org.jdom.Element external_net_node = (org.jdom.Element) agent.getNetworkNode("dmz");
				org.jdom.Element external_scan_node = (org.jdom.Element) org.jdom.xpath.XPath.selectSingleNode(external_net_node, path.toString());
						
				if (external_scan_node != null) {
					// create a Scan object, to maintain the state of autodiscovery
		
					String class_name = external_scan_node.getAttributeValue("class");
					Class c = Class.forName(class_name);
			
					org.opensims.model.Scan scan = (org.opensims.model.Scan) c.newInstance();
			
					scan.init(this, agent, external_scan_node, agent.getNetworkNode("dmz"), agent.getNetworkNumber("dmz"), agent.getNetworkDevice("dmz"));
					// Restructure Hash Table to use network_number as a key, as opposed to the agent key
					// put(scan.getKey(), scan);
					put(agent.getNetworkNumber("dmz"), scan);
					
					// First check if we have an "internal" scan, if not, then set this scan as default
					if (!internalScanAvail) {
						agent.setScan(scan);
					}		
				}
			}
		}
		catch (Exception e) {
			log_.error("register agent", e);
		}
	}


    /**
     * Lookup for Plugins that do not use the Autodiscovery Engine (FSM)
     * Somewhat circuitous and in need of revision
     */

    public org.opensims.model.Scan
	getScan (org.opensims.agent.Agent agent)
    {
    	org.opensims.model.Scan scan = agent.getScan();
		
		return scan;
    }

    /**
     * Loop through the scans under our management and find the one includes the provided IP address
     */

    public org.opensims.model.Scan
	getScanByIP (String ip_addr_str)
    {
    	org.opensims.model.Scan scan = null;
    	try {
			org.opensims.IPv4 ip_addr = new org.opensims.IPv4(ip_addr_str);
			
			for (java.util.Enumeration e = keys(); e.hasMoreElements(); ) {
				String scan_hash = (String) e.nextElement();
				scan = (org.opensims.model.Scan) get(scan_hash);
				
				if (scan.getNetworkId().includes(ip_addr)) {
					return scan;
				}
			}
		}
		catch (Exception e) {
			log_.error("getScanByIP IP conversion error", e);
		}
		return scan;
    }

    /**
     * Loop through the scans under our management and find the one includes the provided Network ID
     */

    public org.opensims.model.Scan
	getScanByNetworkNumber (String network_id)
    {
    	org.opensims.model.Scan scan = null;
    	
		for (java.util.Enumeration e = keys(); e.hasMoreElements(); ) {
			String scan_hash = (String) e.nextElement();
			scan = (org.opensims.model.Scan) get(scan_hash);
			
			if (scan.getNetworkNumber().equals(network_id)) {
				return scan;
			}
		}
		return scan;
    }



    /**
     * Handle an agent event notification
     */

    public void
	agentPostEvent (org.opensims.agent.Agent agent, String trigger_name)
    {
		for (java.util.Enumeration e = keys(); e.hasMoreElements(); ) {
			String scan_hash = (String) e.nextElement();
			org.opensims.model.Scan scan = (org.opensims.model.Scan) get(scan_hash);
			
			if (scan != null) {
				org.opensims.agent.Agent scan_agent = (org.opensims.agent.Agent) scan.getAgent();
				
				// Send the Agent a notification if it's specific that agent
				// or; send ALL agents a HEARTBEAT notification
				if ((scan_agent == agent) || (trigger_name == "agent.heartbeat")) 
					scan.agentPostEvent(trigger_name);
			}
		}
    }


    //////////////////////////////////////////////////////////////////////
    // report rendering
    //////////////////////////////////////////////////////////////////////

    /**
     * Get an XML representation for the collection of network models
     * being managed.
     */

    public org.jdom.Element
	getReportNode ()
    {
		org.jdom.Element report_node = new org.jdom.Element(org.opensims.xml.Node.MODEL_NODE);
	
		for (java.util.Enumeration e = elements(); e.hasMoreElements(); ) {
			org.opensims.model.Scan scan = (org.opensims.model.Scan) e.nextElement();
	
			org.jdom.Element scan_node = (org.jdom.Element) scan.getModelDoc().getRootElement().clone();
			report_node.addContent(scan_node);
		}
	
		return report_node;
    }


    //////////////////////////////////////////////////////////////////////
    // host table management
    //////////////////////////////////////////////////////////////////////

    /**
     * Lookup a host, by id - from the database
     */

    public org.opensims.model.HostOrBogey
    lookupHostById (String host_id, boolean update)
	{
		
		org.opensims.db.om.Host host = null;
	
		// try to find an existing row in the "host" table
	
		try {
			org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
			crit.add(org.opensims.db.om.HostPeer.ID, host_id);
			java.util.Iterator host_iter = org.opensims.db.om.HostPeer.doSelect(crit).iterator();
	
			if (host_iter.hasNext()) {
				host = (org.opensims.db.om.Host) host_iter.next();
		
				if (log_.isDebugEnabled()) {
					log_.debug("populate host " + host.toString());
				}
		
				// is this an existing, known host?
				org.opensims.model.HostOrBogey known_host = lookupHostByIpAddr(host.getIpAddr(), update);
		
				if (known_host != null) {
					host = (org.opensims.db.om.Host) known_host;
				}
				// is this an existing, known bogey?
				else {
					org.opensims.model.HostOrBogey known_bogey = getCorrelator().getBogeyManager().lookupBogeyByIpAddr(host.getIpAddr(), update);
		
					if (known_bogey != null) {
						host = (org.opensims.db.om.Host) known_bogey;
					}
				}
			}
		}
		catch (Exception e) {
			log_.error("lookup host by id |" + host_id + "|", e);
		}
	
		return (org.opensims.model.HostOrBogey) host;
	}


    /**
     * Get an Enumeration of the known hosts.
     */

    public java.util.Enumeration
	getKnownHosts ()
    {
		return host_table.elements();
    }


    /**
     * Lookup a host, by IP address - from the database.
     */

    public org.opensims.model.HostOrBogey
	lookupHostByIpAddr (String ip_addr_str, boolean updated)
    {
		return getKnownHost(ip_addr_str, updated);
    }


    /**
     * Is the given host known?
     */

    public org.opensims.model.HostOrBogey
	getKnownHost (String ip_addr_str, boolean updated)
	{
		org.opensims.db.om.Host host = null;
	
		try {
			// first, check whether this Host is already in our cache
			host = (org.opensims.db.om.Host) host_table.get(ip_addr_str);
	
			// if found or created, then set the last_tick
			if ((host != null) && updated) {
				host.setLastTick(getCorrelator().getTick());
			}
		}
		catch (Exception e) {
			log_.error("get known host: " + ip_addr_str, e);
		}
	
		return (org.opensims.model.HostOrBogey) host;
	}


    /**
     * Set the given host as known.
     */

    public void
	setKnownHost (String ip_addr_str, org.opensims.agent.Agent agent, org.opensims.db.om.Host host)
    {
		try {
			host_table.put(ip_addr_str, host);
			org.opensims.IPv4 ip_addr = new org.opensims.IPv4(ip_addr_str);
			
			if (host.getNetwork() == null) {
				// save network number in database
		
				for (java.util.Enumeration e = this.elements(); e.hasMoreElements(); ) {
					org.opensims.model.Scan scan = (org.opensims.model.Scan) e.nextElement();
					if (scan.getNetworkId().includes(ip_addr)) {
						host.setNetwork(scan.getNetworkNumber());
						host.save();
					}
				}
			}
	
			// remove from BogeyManager
			if (host.isBogey()) {
				getCorrelator().getBogeyManager().remove(host.getKey());
			}
	
			if (log_.isDebugEnabled()) {
				log_.debug("set known host " + host);
			}
		}
		catch (Exception e) {
			log_.error("set known host", e);
		}
    }


    /**
     * Create a new instance of a HostOrBogey "host".
     */

    public org.opensims.model.HostOrBogey
	createHost (String ip_addr_str, org.opensims.agent.Agent agent, org.jdom.Element node, boolean update)
	{
		org.opensims.model.HostOrBogey host = getCorrelator().getBogeyManager().createHostOrBogey(ip_addr_str, agent, node, update);
				
		if (log_.isDebugEnabled()) {
			String xml = null;
			if (node != null) 
				xml = org.opensims.xml.XmlBuilder.formatXML(node, false, true);
			
			log_.debug("create known " + ip_addr_str + " update " + update + " agent " + agent + " bogey " + host.isBogey() + " - " + host);
			log_.debug(xml);
		}
	
		if (update) {
			setKnownHost(ip_addr_str, agent, (org.opensims.db.om.Host) host);
		}
	
		return host;
	}

	public void
	flushHosts(long model_tick)
	{
		for (java.util.Enumeration e = getKnownHosts(); e.hasMoreElements(); ) {
			org.opensims.db.om.Host host = (org.opensims.db.om.Host) e.nextElement();
			try {
		        long period = model_tick - host.getLastSaveTick();
		        if (period > org.opensims.Tickable.HOST_EXPIRY_PERIOD) {
					if(host.isModified()) {
						host.save();
						if (log_.isDebugEnabled()) {
							log_.debug("flushing dirty host " + host + " " + host.getKey() + " tick " + model_tick);
						}
					}
		        }
			} catch (Exception ex) {
				log_.error("unable to flush host " + host.getKey(), ex);
			}
		}

	}
    
    /**
     * Ensure that all scan objects have network models loaded
     */    
    public boolean 
    areAllModelsLoaded()
    {
		boolean allModelsGood =  true;
		
		for (java.util.Enumeration e = elements(); e.hasMoreElements(); ) {
			org.opensims.model.Scan scan = (org.opensims.model.Scan) e.nextElement();
				
			// Ignore permissive mode when calculating all models done with scanning
			if (!scan.getPermissiveMode()) {	
				if (!scan.getCorrelationEnable()) {
					allModelsGood = false;
					return (allModelsGood);
				}
			}
		}		
		return allModelsGood;
	}
	
	
	/**
     * Ensure that all scan objects have completed their MAC scans
     */    
    public boolean 
    areAllScansDone()
    {
		boolean allScansGood =  true;
		
		for (java.util.Enumeration e = elements(); e.hasMoreElements(); ) {
			org.opensims.model.Scan scan = (org.opensims.model.Scan) e.nextElement();
	
			if (log_.isDebugEnabled()) {
				log_.debug("check if all scans are done | " + scan.getStatus() + " : " + scan.isScanComplete() );
			}
			
			if (!scan.isScanComplete()) {
				allScansGood = false;
				return (allScansGood);
			}
		}
		return allScansGood;
	}

}
