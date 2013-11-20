/**
 * @LICENSE@
 */

package org.opensims.model;

/**
 * $CVSId: GenericScan.java,v 1.46 2007/02/08 01:10:03 mikee Exp $
 * $Id: GenericScan.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.46 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    GenericScan
    implements org.opensims.model.Scan,
	       org.opensims.model.AutoDiscovery
{
    // protected fields
    protected org.opensims.Manager manager = null;
    protected org.opensims.agent.Agent agent = null;
    protected org.opensims.xml.UmlActivity fsm = null;
    protected java.io.File model_file = null;
    protected org.jdom.Document model_doc = null;
    protected org.opensims.IPv4 network_id = null;
    protected boolean permissive_mode = false;
    protected java.util.Vector receipts = new java.util.Vector();
    protected long last_tick = 0L;
    protected String network_number;
    protected org.jdom.Element network_node;
    protected String device;
    protected boolean engine_enable = false;
    protected boolean scan_complete = false;
 
    // quality assurance
    private static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.model.GenericScan.class.getName());


	// internal fields
	private java.util.Vector whitelisted_CIDRs = new java.util.Vector();
	private org.jdom.Element task_node = null;
	private org.opensims.Task receiptTask = null;
	private java.util.Timer receiptTimer = null;
	

    /**
     * Initialize the scan
     */

    public void
	init (org.opensims.model.ScanManager scan_manager, org.opensims.agent.Agent agent, org.jdom.Element scan_node, org.jdom.Element network_node, String network_number, String device)
	{
	
		task_node = new org.jdom.Element(org.opensims.xml.Node.TASK_NODE);		
		task_node.setAttribute("enabled", "true");
		task_node.setAttribute("method", "taskExpireReceipts");
		task_node.setAttribute("delay", "3600000");
		task_node.setAttribute("period", "3600000");			//! ME: Edit Task.java so that passing "0" will allow a single execution
	
		setManager(scan_manager);
		setLastTick(getManager().getCorrelator().getTick());
		setNetworkNumber(network_number);
		setNetworkNode(network_node);
		setDevice(device);
	
		if (log_.isDebugEnabled()) {
			log_.debug("init scan [scan node] " + org.opensims.xml.XmlBuilder.formatXML(scan_node, false, false) + " | [network node] " + org.opensims.xml.XmlBuilder.formatXML(network_node, false, false));
		}
	
		setAgent(agent);
		setPermissiveMode(Boolean.valueOf(scan_node.getAttributeValue("permissive")).booleanValue());
	
		// load the XML document which defines a UML activity
		// diagram (aka, a finite state machine or "statechart")
	
		try {
			java.io.File uml_dir = new java.io.File(getManager().getCorrelator().getConfig("webapp.dir"), "uml");
			java.io.File uml_file = new java.io.File(uml_dir, scan_node.getAttributeValue("fsm"));
	
			if (log_.isInfoEnabled()) {
				log_.info("scan UML file: " + uml_file);
			}
	
			java.io.FileReader file_reader = new java.io.FileReader(uml_file);
			java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);
	
			org.jdom.Document document = getManager().getCorrelator().buildXml(buf_reader);
	
			setFSM(new org.opensims.xml.UmlActivity(document, this, getManager().getCorrelator()));
		}
		catch (Exception e) {
			log_.error("initializing scan", e);
		}
	
		// Add to Agent's Scan Queue-- Agent will dequeue on its start()
		getAgent().addScan(getNetworkNumber(),this);
	
		// keep track of the file used to store this network model
		java.io.File var_lib = new java.io.File(getManager().getCorrelator().getConfig("var.lib"));
		setModelFile(new java.io.File(var_lib, getNetworkNumber() + ".model.xml"));
		
		if (log_.isInfoEnabled()) {
			log_.info("model file: " + getModelFile());
		}
	
		// do we start the autodiscovery FSM or run permissive?
		if (getPermissiveMode()) {
			loadModelFile(null);
			scan_complete = true;
		}
	
		getFSM().init();
	}


    /**
     * Represent as a String
     */

    public String
    getSummary ()
    {
		StringBuffer buf = new StringBuffer();
	
		buf.append(getKey());
		buf.append("\n\t model: ");
		buf.append(getModelFile());
		buf.append("\n\t network: ");
		buf.append(getNetworkId().toString());
		buf.append("\n\t permissive: ");
		buf.append(getPermissiveMode());
		buf.append("\n\t FSM state: ");
		buf.append(getCurrentState());
		buf.append("\n\t last_tick: ");
		buf.append(getManager().getCorrelator().getTick() - last_tick);
	
		return buf.toString();
    }


    /**
     * Represent a status line as a String.
     */

    public String
    getStatus ()
    {
		StringBuffer buf = new StringBuffer();
		String returnValue;
		long since = (getManager().getCorrelator().getTick() - last_tick) / 1000;
		
		buf.append(network_node.getAttributeValue("cidr"));
		buf.append(" ");
		buf.append(getCurrentState());
	
		// buf.append(" [");
		// buf.append(since);
		// buf.append("s]");
		
		if (buf.length() > 28)
			returnValue = buf.substring(0,28);
		else
			returnValue = buf.toString();
			
		return returnValue;
    }


    //////////////////////////////////////////////////////////////////////
    // implement the autodiscovery FSM
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the FSM.
     */

    public org.opensims.xml.UmlActivity
	getFSM ()
    {
		return fsm;
    }


    /**
     * Set the FSM.
     */

    public void
	setFSM (org.opensims.xml.UmlActivity fsm)
    {
		this.fsm = fsm;
    }


    /**
     * Get the associated Agent.
     */

    public org.opensims.agent.Agent
	getAgent ()
    {
		return agent;
    }


    /**
     * Set the associated Agent.
     */

    public void
	setAgent (org.opensims.agent.Agent agent)
    {
		this.agent = agent;
    }


    /**
     * Start the state machine running.
     */

    public void
	start ()
    {
		getFSM().start();
    }


    /**
     * Stop the state machine running.
     */

    public void
	stop ()
    {
		if (getFSM() != null) {
			getFSM().stop();
		}
    }


    /**
     * Get a String representation for the current FSM state
     */

    public String
	getCurrentState ()
    {
		return getFSM().getCurrentState().getAttributeValue("name");
    }


    /**
     * Receive an agent event, such as "agent.heartbeat"
     */

    public void
	agentPostEvent (String trigger_name)
    {
		getFSM().postEventTrigger(trigger_name);
		setLastTick(getManager().getCorrelator().getTick());
    }


    //////////////////////////////////////////////////////////////////////
    // file handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the model file.
     */

    public java.io.File
	getModelFile ()
    {
		return model_file;
    }


    /**
     * Set the model file.
     */

    public void
	setModelFile (java.io.File model_file)
    {
		this.model_file = model_file;
    }


    /**
     * Load the model file (called by FSM).
     */

    public boolean
	loadModelFile (String param)
	{
		try {
			if (log_.isDebugEnabled()) {
				log_.debug("load model file: " + param);
			}
			loadModelDoc();
	
			// initialize the table of known hosts
			java.util.Iterator host_iter = getModelDoc().getRootElement().getChildren(org.opensims.xml.Node.HOST_NODE).iterator();
	
			while (host_iter.hasNext()) {
				org.jdom.Element host_node = (org.jdom.Element) host_iter.next();
				org.opensims.IPv4 ip_addr = new org.opensims.IPv4(host_node.getAttributeValue("ip_addr"));
				String ip_addr_str = ip_addr.toString();
				org.opensims.model.HostOrBogey host = ((org.opensims.model.ScanManager) getManager()).createHost(ip_addr_str, getAgent(), host_node, true);
			}
		}
		catch (Exception e) {
			log_.error("load model file", e);
		}

		setLastTick(getManager().getCorrelator().getTick());
		
		return true;
	}


    /**
     * Either read or create the network model document.
     */

    protected void
	loadModelDoc ()
	throws Exception
	{
		org.jdom.Element network_node = null;
	
		if (getModelFile().canRead()) {
			// if a previously saved file exists, load it
			java.io.FileReader file_reader = new java.io.FileReader(getModelFile());
			java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);
	
			setModelDoc(getManager().getCorrelator().buildXml(buf_reader));
			buf_reader.close();
	
			// update the file location
			network_node = (org.jdom.Element) getModelDoc().getRootElement().clone();
			network_node.setAttribute("file", getModelFile().toString());
			
			// refresh the whitelist nodes with the main config file ones
			java.util.Iterator whitelist_iter = org.jdom.xpath.XPath.selectNodes(network_node, org.opensims.xml.Node.WHITELIST_NODE).iterator();
			
			while (whitelist_iter.hasNext()) {
				org.jdom.Element whitelist_node = (org.jdom.Element) whitelist_iter.next();
				whitelist_node.detach();
				if (log_.isDebugEnabled()) {
					log_.debug("Whitelist item removed: " + org.opensims.xml.XmlBuilder.formatXML(whitelist_node, false, false));
				}
			}
			
			// add back the whitelist entries from the webapp.xml config
			org.jdom.Element config_network_node = (org.jdom.Element) getNetworkNode();			
			java.util.Iterator config_whitelist_iter = org.jdom.xpath.XPath.selectNodes(config_network_node, org.opensims.xml.Node.WHITELIST_NODE).iterator();
			while (config_whitelist_iter.hasNext()) {
				org.jdom.Element whitelist_node = (org.jdom.Element) config_whitelist_iter.next();
				org.jdom.Element new_whitelist_node = (org.jdom.Element) whitelist_node.clone();
				network_node.addContent(new_whitelist_node);
				if (log_.isDebugEnabled()) {
					log_.debug("Whitelist item added: " + org.opensims.xml.XmlBuilder.formatXML(whitelist_node, false, false));
				}
			}
			
			// overwrite the existing model file with the newly updated whitelists
			setModelDoc(new org.jdom.Document(network_node));			
		}
		else {
			// otherwise, create an initial <NETWORK/> node using the
			// config XML node as a template
			network_node = (org.jdom.Element) getNetworkNode().clone();
			network_node.setAttribute("file", getModelFile().toString());
			
			if (log_.isDebugEnabled()) {
				log_.debug("load model doc [network node] " + org.opensims.xml.XmlBuilder.formatXML(network_node, false, false));
			}
	
			// scrub out the firewall authentication	
			org.jdom.Element firewall_node = (org.jdom.Element) network_node.getChild(org.opensims.xml.Node.FIREWALL_NODE);
	
			if (firewall_node != null) {
				firewall_node.removeAttribute("user");
				firewall_node.removeAttribute("password");
				firewall_node.removeAttribute("enable");
				firewall_node.removeAttribute("exec");
			}
	
			// create a new XML document
			setModelDoc(new org.jdom.Document(network_node));
		}
	
		// make sure that the network CIDR is set
		if (getNetworkId() == null) {
			setNetworkId(new org.opensims.IPv4(network_node.getAttributeValue("cidr")));
		}
	
		if (log_.isDebugEnabled()) {
			String xml = org.opensims.xml.XmlBuilder.formatXML(network_node, true, true);
			log_.debug("load model " + xml);
		}
	}


    /**
     * Save the model file (called by FSM).
     */

    public boolean
	saveModelFile (String param)
	{
		if (log_.isInfoEnabled()) {
			log_.info("save model file: " + param);
		}
	
		try {
			if (!getModelFile().canWrite()) {
				log_.warn("save model file - no pre-existing (or bad file perms) " + getModelFile());
			}
	
			if (log_.isDebugEnabled()) {
				log_.debug("save model " + org.opensims.xml.XmlBuilder.formatXML(getModelDoc().getRootElement(), true, true));
			}
	
			java.io.FileWriter file_writer = new java.io.FileWriter(getModelFile());
			java.io.BufferedWriter buf_writer = new java.io.BufferedWriter(file_writer);
			java.io.PrintWriter print_writer = new java.io.PrintWriter(buf_writer);
	
			String xml = org.opensims.xml.XmlBuilder.formatXML(getModelDoc().getRootElement(), true, true);
	
			print_writer.write(xml);
			print_writer.flush();
			print_writer.close();
		}
		catch (Exception e) {
			log_.error("save model file", e);
		}
	
		setLastTick(getManager().getCorrelator().getTick());
	
		return true;
	}


    //////////////////////////////////////////////////////////////////////
    // plugin configuration
    //////////////////////////////////////////////////////////////////////

    /**
     * Is the action enabled for this network? (called by FSM).
     */

    public boolean
	networkAllows (String param)
	{
		boolean result = false;
	
		if (getPermissiveMode()) {
			if (log_.isInfoEnabled()) {
				log_.info("permissive override disallows: " + param);
			}
		}
		else {
			try {	    
			if (log_.isDebugEnabled()) {
				log_.debug("network allows: " + param);
			}
	
			if (param == null) {
				// ignore
			}
			else if (param.equals("discovery")) {
				StringBuffer path = new StringBuffer();
	
				path.append("//").append(org.opensims.xml.Node.SCAN_NODE);
				path.append("[@enabled = 'true']");
	
				org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
				org.jdom.Element scan_node = (org.jdom.Element) xpath.selectSingleNode(getModelDoc());
	
				if (scan_node != null) {
					result = true;
				}
			}
			else {
				String config_value = getManager().getCorrelator().getConfig(param);
	
				if (config_value != null) {
					result = true;
				}
			}
			}
			catch (org.jdom.JDOMException e) {
			log_.error("XPath / network allows", e);
			}
		}
	
		setLastTick(getManager().getCorrelator().getTick());
	
		return result;
	}


    /**
     * Configure a plugin for the agent (called by FSM).
     */

    public boolean
	configPlugin (String param)
	{
		boolean result = false;
	
		if (log_.isDebugEnabled()) {
			log_.debug("config plugin: " + param);
		}
	
		try {
			java.util.StringTokenizer st = new java.util.StringTokenizer(param, ".");
	
			if (st.hasMoreTokens()) {
				String plugin_name = st.nextToken();
		
				/**
				 * @TODO Perhaps always use thread:0 for server issued commands
				 * or, use a queue/stack etc. --ME
				 */
				org.opensims.agent.Plugin plugin = getAgent().getRandomPlugin(plugin_name);
		
				if (log_.isDebugEnabled()) {
					log_.debug("config plugin: " + plugin_name + " @ " + plugin);
				}
		
				//! Should additionally check if the plugin is enabled before continuing
				//! Note: Tweaks to the webapp.conf will need to be done to enable mac-lookup and nmap
				// if ((plugin != null) && plugin.getEnabled()) {
				if (plugin != null) {
					result = plugin.doConfig(this, param);
				}
			}
		}
		catch (Exception e) {
			log_.error("config plugin", e);
		}
	
		setLastTick(getManager().getCorrelator().getTick());
	
		return result;
	}


    //////////////////////////////////////////////////////////////////////
    // receipt handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Set a receipt (called by FSM).
     */

    public boolean
	setReceipt (String param)
	{
		boolean result = false;
		
		if (log_.isDebugEnabled()) {
			log_.debug("set receipt: " + param);
		}
		
		// setup a timer to manually flush dangling jobs
		if (param != null) {
			receiptTimer = new java.util.Timer(true);
			receiptTask = new org.opensims.Task(this, receiptTimer, task_node);
		}
	
		if (param == null) {
			// ignore
		}
		else if (param.equals("scan")) {
			// add a receipt for the network CIDR
			receipts.add(getNetworkId().toString());
			result = true;
			scan_complete = false;
		}
		else if (param.equals("hosts") || param.equals("services")) {
			// add a receipt for each host IP address
			for (java.util.Enumeration e = ((org.opensims.model.ScanManager) getManager()).getKnownHosts(); e.hasMoreElements(); ) {
				org.opensims.model.HostOrBogey host = (org.opensims.model.HostOrBogey) e.nextElement();
				try {
					if ( (getNetworkId().includes(host.getIPv4())) && (! isWhitelisted(host.getIPv4())) ) {
						receipts.add(host.getIpAddr());
					}
				}
				catch (Exception ex) {
					log_.error("Looking up host IP for receipt processing", ex);
				}
			}
			result = true;
		}
	
		if (log_.isDebugEnabled()) {
			log_.debug("receipts: " + receipts.toString());
		}
	
		setLastTick(getManager().getCorrelator().getTick());
	
		return result;
	}


    /**
     * Wait for the known receipts to be cleared (called by FSM).
     */

    public boolean
	waitKnownReceipts (String param)
	{
		if (log_.isDebugEnabled()) {
			log_.debug("wait known receipts: " + param + " - empty: " + receipts.isEmpty() + " - " + receipts.toString());
		}
		return receipts.isEmpty();
	}


    /**
     * Clear a receipt
     */

    public boolean
	clearReceipt (String key)
    {
    	boolean removed = false;
    	
    	if (key.equals(getNetworkId().toString()))
    		scan_complete = true;
    	
		setLastTick(getManager().getCorrelator().getTick());
		removed = receipts.remove(key);
		
		// RESET the timer to manually flush dangling jobs
		receiptTask.cancel();
		receiptTimer.cancel();
		if (!receipts.isEmpty()) {
			receiptTimer = new java.util.Timer(true);
			receiptTask = new org.opensims.Task(this, receiptTimer, task_node);
		}

		if (log_.isDebugEnabled()) {
			log_.debug("clear receipt: " + key + " - " + receipts.toString() + " removed: " + removed);
		}
		
		return removed;
    }
    

    /**
     * Expire a set of non-responsive receipts - called by the timer task
     */

    public void
	taskExpireReceipts ()
    {
 		log_.info("dangling receipts found: " + receipts.size() );
		
    	if (!receipts.isEmpty()) 
			receipts.removeAllElements();
		
		receiptTask.cancel();
		this.agentPostEvent("agent.receipt");
   	}
    

    /**
     * Return the requested "chunk size" number of items from the
     * receipt list.
     */

    public String
	listReceiptsChunk (int chunk_size)
    {
		StringBuffer buf = new StringBuffer();
		java.util.Enumeration e = receipts.elements();
	
		for (int i = 0; i < chunk_size && e.hasMoreElements(); i++) {
			String receipt = (String) e.nextElement();
			buf.append(" ").append(receipt);
		}
	
		if (log_.isDebugEnabled()) {
			log_.debug("list receipts chunk: " + chunk_size + " - " + buf.toString());
		}
	
		return buf.toString();
    }


    //////////////////////////////////////////////////////////////////////
    // network model management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the network node.
     */

    public org.jdom.Element
	getNetworkNode ()
    {
		return network_node;
    }


    /**
     * Set the network node.
     */

    public void
	setNetworkNode (org.jdom.Element network_node)
    {
		this.network_node = network_node;
    }


    /**
     * Get the network number.
     */

    public String
	getNetworkNumber ()
    {
		return network_number;
    }


    /**
     * Set the network number.
     */

    public void
	setNetworkNumber (String network_number)
    {
		this.network_number = network_number;
    }


    /**
     * Get the network identifier.
     */

    public org.opensims.IPv4
	getNetworkId ()
    {
		return network_id;
    }


    /**
     * Set the network identifier.
     */

    public void
	setNetworkId (org.opensims.IPv4 network_id)
    {
		this.network_id = network_id;
    }

    /**
     * Get the device.
     */

    public String
    getDevice ()
    {
		return device;
    }


    /**
     * Set the device.
     */

    public void
	setDevice (String device)
    {
		this.device = device;
    }

    /**
     * Get the permissive mode flag.
     */

    public boolean
	getPermissiveMode ()
    {
		return permissive_mode;
    }


    /**
     * Get the permissive mode flag.
     */

    public void
	setPermissiveMode (boolean permissive_mode)
    {
		this.permissive_mode = permissive_mode;
	
		if (log_.isDebugEnabled()) {
			log_.debug("set permissive_mode " + permissive_mode);
		}
    }


    /**
     * Determine whether the given ip_addr is friend/foe/don't care
     */

    public org.opensims.model.HostOrBogey
	getHostOrBogey (org.opensims.IPv4 ip_addr)
	{
		org.opensims.model.HostOrBogey host = null;
		String ip_addr_str = ip_addr.toString();

		if (isWhitelisted(ip_addr)) {
			// host is whitelisted, return null
		}
		// should only consider unknowns as foes *after* we know the network CIDR
		else if (((org.opensims.model.ScanManager) getManager()).areAllModelsLoaded()) {
	
			// address is within the "defender" space - but does it exist?
			org.opensims.model.Scan extant_scan = ((org.opensims.model.ScanManager) getManager()).getScanByIP(ip_addr_str);
			
			if (extant_scan != null) {
				host = ((org.opensims.model.ScanManager) getManager()).getKnownHost(ip_addr_str, true);
						
				if (host == null) {
					// Test the permissive mode of the network
					if (getPermissiveMode()) {					
						if (log_.isDebugEnabled()) {
							log_.debug("permissive: create " + ip_addr_str);
						}
			
						// <HOST mac_addr="00B0D0E94A34" ip_addr="172.16.1.4" host_id="2" status="up">
	
						org.jdom.Element host_node = new org.jdom.Element(org.opensims.xml.Node.HOST_NODE);		
						host_node.setAttribute("ip_addr", ip_addr_str);
						host_node.setAttribute("mac_addr", "000000000000");
						host_node.setAttribute("status", "up");
						host_node.setAttribute("group", "admin_group");
			
						host = ((org.opensims.model.ScanManager) getManager()).createHost(ip_addr_str, getAgent(), host_node, true);
					}
				}
			}
	
			// this is a bogey
			if (host == null) {
				host = getManager().getCorrelator().getBogeyManager().lookupBogeyByIpAddr(ip_addr_str, true);
			}
		}
		return host;
	}


    /**
     * Determine the whitelist status for the given IP address, based
     * on network model.
     */

    public boolean
	isWhitelisted (org.opensims.IPv4 ip_addr)
	{
		
		// First check to see if we have a cache of whitelisted CIDRs
		// if not, set one up
		if (whitelisted_CIDRs.isEmpty()) {
			try {
				StringBuffer path = new StringBuffer("//");
		
				path.append(org.opensims.xml.Node.WHITELIST_NODE);
				path.append("[@enabled = 'true']");
		
				org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
				java.util.Iterator whitelist_iter = xpath.selectNodes(getModelDoc()).iterator();
		
				while (whitelist_iter.hasNext()) {
					org.jdom.Element whitelist_node = (org.jdom.Element) whitelist_iter.next();
					org.opensims.IPv4 cidr = new org.opensims.IPv4(whitelist_node.getAttributeValue("cidr"));
			
					/**
					 * too much noise - leave for potential debug later
			
					if (log_.isDebugEnabled()) {
						log_.debug(cidr.toString() + " includes? " + ip_addr.toString());
					}
					 */
					 
					 whitelisted_CIDRs.add(cidr);
				}
			}
			catch (Exception e) {
				log_.error("XPath / whitelisted? " + ip_addr.toString(), e);
			}
		}

		java.util.Iterator whitelist_cidr_iter = whitelisted_CIDRs.iterator();
		while (whitelist_cidr_iter.hasNext()) {
			org.opensims.IPv4 cidr = (org.opensims.IPv4) whitelist_cidr_iter.next();
			if (cidr.includes(ip_addr)) {
				return true;
			}
		}
	
		return false;
	}

    /**
     * Provides a test to see if the "scan" parameter has completed
     * Used for multiple scans running in parallel
     */

    public boolean
	isScanComplete ()
	{
		return scan_complete;
	}
	
	
    /**
     * Transmit a "scrubbed" version of the network model to the
     * repository, if configured (called by FSM).
     */

    public boolean
	transmitToRepository (String param)
    {
		if (log_.isDebugEnabled()) {
			log_.debug("transmit to repository: " + param);
		}
	
		setLastTick(getManager().getCorrelator().getTick());
	
		return true;
    }


    //////////////////////////////////////////////////////////////////////
    // model document update
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the model document.
     */

    public org.jdom.Document
	getModelDoc ()
    {
		return model_doc;
    }


    /**
     * Set the model document.
     */

    public void
	setModelDoc (org.jdom.Document model_doc)
    {
		this.model_doc = model_doc;
    }


    /**
     * Update host ID mappings assigned by the database (called by FSM).
     */

    public boolean
	updateHostAssignments (String param)		
	{
		try {
			if (log_.isDebugEnabled()) {
				log_.debug("update host assignments: " + param);
			}
	
			java.util.Iterator host_iter = getModelDoc().getRootElement().getChildren(org.opensims.xml.Node.HOST_NODE).iterator();
	
			while (host_iter.hasNext()) {
				org.jdom.Element host_node = (org.jdom.Element) host_iter.next();
				String host_id = host_node.getAttributeValue("host_id");
		
				// every host in the model needs a GUID	
				if (host_id == null) {
					org.opensims.IPv4 ip_addr = new org.opensims.IPv4(host_node.getAttributeValue("ip_addr"));
					String ip_addr_str = ip_addr.toString();
		
					org.opensims.model.HostOrBogey host = ((org.opensims.model.ScanManager) getManager()).createHost(ip_addr_str, getAgent(), host_node, true);
		
					host_id = host.getHostId();
					host_node.setAttribute("host_id", host_id);
		
					if (log_.isDebugEnabled()) {
						log_.debug("update host assignment: " + host.toString());
					}
				}
			}
	
			if (log_.isDebugEnabled()) {
				log_.debug("UPDATED: " + org.opensims.xml.XmlBuilder.formatXML(getModelDoc().getRootElement(), false, true));
			}
		}
		catch (Exception e) {
			log_.error("update model", e);
		}
	
		setLastTick(getManager().getCorrelator().getTick());
	
		return true;
	}


    /**
     * Walk the given document tree, using it to update the existing
     * XML elements and attributes
     */

    public void
	updateModel (org.jdom.Element update_network_node, String param)
	{
		try {
			if (getModelDoc() != null) {
				// fix the model root element
				org.jdom.Element extant_network_node = getModelDoc().getRootElement();
				updateNode(update_network_node, extant_network_node, null);
		
				// update ip_addr/mac_addr for each host
				java.util.Iterator update_host_iter = update_network_node.getChildren(org.opensims.xml.Node.HOST_NODE).iterator();
		
				while (update_host_iter.hasNext()) {
					org.jdom.Element update_host_node = (org.jdom.Element) update_host_iter.next();
					updateHost(update_host_node, param, extant_network_node);
				}
		
				// clear the scan receipt for the network
				if (param.equals("scan")) {
					clearReceipt(getNetworkId().toString());
				}
		
				if (log_.isDebugEnabled()) {
					log_.debug("UPDATED: " + param + " " + org.opensims.xml.XmlBuilder.formatXML(extant_network_node, false, true));
				}
			}
		}
		catch (Exception e) {
			log_.error("update model", e);
		}
	}


    /**
     * Make updates to one host node in the XML document
     */

    protected void
	updateHost (org.jdom.Element update_host_node, String param, org.jdom.Element extant_network_node)
	throws Exception
	{
		// <HOST ip_addr="67.107.81.193" mac_addr="00:02:17:60:F8:C9" />
	
		org.opensims.IPv4 ip_addr = new org.opensims.IPv4(update_host_node.getAttributeValue("ip_addr"));
		String ip_addr_str = ip_addr.toString();
	
		if (log_.isDebugEnabled()) {
			String d_update = null;
			String d_extant = null;
	
			if (update_host_node != null) {
				d_update = org.opensims.xml.XmlBuilder.formatXML(update_host_node, false, true);
			}
	
			if (extant_network_node != null) {
				d_extant = org.opensims.xml.XmlBuilder.formatXML(extant_network_node, false, true);
			}
	
			log_.debug("update host: ip_addr " + ip_addr_str + " cidr.includes " + getNetworkId().includes(ip_addr) + " whitelisted " + isWhitelisted(ip_addr));
			log_.debug("update host: param " + param + "\n update " + d_update + "\n extant " + d_extant);
		}
	
		if (getNetworkId().includes(ip_addr) && !isWhitelisted(ip_addr)) 
		{
			String mac_addr = update_host_node.getAttributeValue("mac_addr");
	
			if (mac_addr != null) {
				mac_addr = org.opensims.Config.parseMacAddr(mac_addr);
				update_host_node.setAttribute("mac_addr", mac_addr);
			}
			
			// find or create a persistent object for representing
			// this host
	
			org.jdom.Element extant_host_node = null;
			org.opensims.model.HostOrBogey host = ((org.opensims.model.ScanManager) getManager()).getKnownHost(ip_addr_str, true);
	
			if (log_.isDebugEnabled()) {
				log_.debug("host " + host);
			}
	
			if (host == null) {
				// then, create a host object
				extant_host_node = updateNode(update_host_node, null, extant_network_node);
				host = ((org.opensims.model.ScanManager) getManager()).createHost(ip_addr_str, getAgent(), extant_host_node, true);
				
				extant_host_node.setAttribute("host_id", host.getHostId());
				extant_host_node.setAttribute("group", host.getGroups());
				String fqdn = host.getDomainName();
				if (fqdn != null) {
					extant_host_node.setAttribute("fqdn", fqdn);
				}
				long host_tick = host.getLastTick();
				if (host_tick != 0L) {
					String lastseen = Long.toString(host_tick);
					extant_host_node.setAttribute("lastseen", lastseen);
				}
			}
			else {
				// use the existing host object	
				extant_host_node = host.getNode();
				extant_host_node.setAttribute("group", host.getGroups());
				String fqdn = host.getDomainName();
				if (fqdn != null) {
					extant_host_node.setAttribute("fqdn", fqdn);
				}
				long host_tick = host.getLastTick();
				if (host_tick != 0L) {
					String lastseen = Long.toString(host_tick);
					extant_host_node.setAttribute("lastseen", lastseen);
				}
				updateNode(update_host_node, extant_host_node, extant_network_node);
			}
			
			// update the non-attribute content inside of <HOST/>
			if (param.equals("hosts")) {
				org.jdom.Element uptime_node = (org.jdom.Element) update_host_node.getChild(org.opensims.xml.Node.UPTIME_NODE);
		
				if (uptime_node != null) {
					uptime_node.detach();
					extant_host_node.removeChildren(org.opensims.xml.Node.UPTIME_NODE);
					extant_host_node.addContent(uptime_node);
				}
		
				org.jdom.Element platform_node = (org.jdom.Element) update_host_node.getChild(org.opensims.xml.Node.PLATFORM_NODE);
		
				if (platform_node != null) {
					platform_node.detach();
					extant_host_node.removeChildren(org.opensims.xml.Node.PLATFORM_NODE);
					platform_node.setAttribute("osfamily",((platform_node.getAttributeValue("osfamily")).replace(' ','_')));
					extant_host_node.addContent(platform_node);
				}
		
				clearReceipt(ip_addr.toString());
			}
			else if (param.equals("services")) {
				java.util.Iterator update_service_iter = update_host_node.getChildren(org.opensims.xml.Node.SERVICE_NODE).iterator();
		
				while (update_service_iter.hasNext()) {
					org.jdom.Element update_service_node = (org.jdom.Element) update_service_iter.next();
					String update_port = update_service_node.getAttributeValue("port");
					String update_protocol = update_service_node.getAttributeValue("protocol");
		
					String service_path = "//HOST[@ip_addr = '" + ip_addr.toString() + "']/SERVICE[@port = '" + update_port + "' and @protocol = '" + update_protocol + "']";
					org.jdom.Element extant_service_node = findNode(getModelDoc(), service_path);
						
					updateNode(update_service_node, extant_service_node, extant_host_node);
				}
		
				clearReceipt(ip_addr.toString());
			}
		}
	}


    /**
     * Update any missing attributes from the update_node to the
     * (existing) extant_node, ignoring text content.
     */

    protected org.jdom.Element
	updateNode (org.jdom.Element update_node, org.jdom.Element extant_node, org.jdom.Element parent)
	{
		if (extant_node == null) {
		
			// copy into parent, if the extant_node does not exist
			extant_node = new org.jdom.Element(update_node.getName());
			parent.addContent(extant_node);
		}
	
		// copy over the enumerated attributes
		java.util.Iterator update_attr_iter = update_node.getAttributes().iterator();
	
		while (update_attr_iter.hasNext()) {
			org.jdom.Attribute update_attr = (org.jdom.Attribute) update_attr_iter.next();
			extant_node.setAttribute(update_attr.getName(), update_attr.getValue());
		}
		return extant_node;
	}


    /**
     * Use XPath to locate a node within an XML document.
     */

    protected org.jdom.Element
	findNode (org.jdom.Document document, String path)
	{
		org.jdom.Element result = null;
	
		try {
			org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path);
	
			result = (org.jdom.Element) xpath.selectSingleNode(document);
		}
		catch (Exception e) {
			log_.error("find node XPath", e);
		}
	
		return result;
	}


    //////////////////////////////////////////////////////////////////////
    // event correlation methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Enable event correlation (called by FSM).
     */

    public boolean
	setCorrelationEnable (String param)
	{
		if (log_.isDebugEnabled()) {
			log_.debug("set correlation enable: " + param);
		}
	
		engine_enable = Boolean.valueOf(param).booleanValue();
		getManager().getCorrelator().setEngineEnable(engine_enable);
	
		return true;
	}
    
    /**
     *  Get state of event correlation
     */
    
    public boolean
	getCorrelationEnable ()
    {
		return (engine_enable);
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
	getKey (String ref)
    {
		return ref;
    }


    /**
     * Get the hashtable lookup key
     */

    public String
	getKey ()
    {
		return getAgent().getKey();
    }


    /**
     * Check the expiry date/time
     */

    public boolean
	checkExpiry (long model_tick)
	{
		boolean result = false;
		long period = model_tick - getLastTick();
	
		if (period > EXPIRY_PERIOD) {
			getManager().remove(getKey());
			result = true;
		}
	
		return result;
	}


    //////////////////////////////////////////////////////////////////////
    // Exportable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Format an XML node for export
     */

    public org.jdom.Element
	getExportNode ()
	{
		org.jdom.Element export_node = new org.jdom.Element(org.opensims.xml.Node.NET_NODE);
		org.jdom.Element network_node = getModelDoc().getRootElement();
	
		export_node.setAttribute("id", "0");
		export_node.setAttribute("ip", getNetworkId().toString());
		export_node.setAttribute("name", network_node.getAttributeValue("description"));
	
		return export_node;
	}

}
