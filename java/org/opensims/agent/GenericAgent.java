/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: GenericAgent.java,v 1.39 2007/02/08 01:11:12 mikee Exp $
 * $Id: GenericAgent.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.39 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    GenericAgent
    implements org.opensims.agent.Agent
{
    // protected fields

    protected org.opensims.Manager manager = null;
    protected org.jdom.Element config_node = null;
    protected org.opensims.model.Scan scan = null;
    protected java.util.Hashtable scans_avail = new java.util.Hashtable();
    protected java.util.Stack scan_queue = new java.util.Stack();
    protected String agent_name = null;
    protected String mac_addr = null;
    protected org.opensims.IPv4 ip_addr = null;
    protected String nonce = null;
    protected String app_sig = null;
    protected String gid = null;
    protected java.util.Hashtable avail_plugins = new java.util.Hashtable();
    protected java.util.Hashtable plugin_threads_avail = new java.util.Hashtable();
    protected boolean local_flag = false;
    protected long last_tick = 0L;
    protected long heartbeat_expiry = 0L;
    protected java.util.Stack command_queue = new java.util.Stack();
    protected volatile Thread agent_thread = null;
    protected java.util.Stack transaction_queue = new java.util.Stack();
    protected boolean is_busy = false;
    protected java.util.Hashtable agent_threads_avail = new java.util.Hashtable();
    protected long agent_threads = 0L;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.GenericAgent.class.getName());


    /**
     * Initialize the agent object
     */

    public void
	init (org.opensims.agent.AgentManager agent_manager, org.jdom.Element config_node, String agent_name, String mac_addr, org.opensims.IPv4 ip_addr, String nonce, String app_sig, String gid, long agent_threads)
    {
		setManager(agent_manager);
		setLastTick(getManager().getCorrelator().getTick());
	
		setConfigNode(config_node);
		setLocalFlag(Boolean.valueOf(getConfigNode().getAttributeValue("local")).booleanValue());
	
		this.agent_name = agent_name;
		this.mac_addr = mac_addr;
		this.ip_addr = ip_addr;
		this.nonce = nonce;
		this.app_sig = app_sig;
		this.gid = gid;
		this.agent_threads = agent_threads;
    }

    //////////////////////////////////////////////////////////////////////
    // Runnable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Clone
     */

    public Object
	clone ()
	{
		try {
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			// This should never happen
			throw new InternalError(e.toString());
		}
	}


    /**
     * Get the agent thread.
     */

    public Thread
	getAgentThread ()
    {
		return agent_thread;
    }


    /**
     * Set the agent thread.
     */

    public void
	setAgentThread (Thread agent_thread)
    {
		this.agent_thread = agent_thread;
    }
    
    
    /**
     * Create the thread pool that this agent manages.
     */

    public void
	createThreads ()
    {
    	org.opensims.agent.Agent agent_clone = null;
    	
    	for (int thread_counter=0; thread_counter<agent_threads; thread_counter++) {
			// Create a series of clones of the agent, threaded
			agent_clone = (org.opensims.agent.Agent) this.clone();
			
			if (agent_clone != null) {
				String hashCode = Integer.toHexString(agent_clone.hashCode());
				
				agent_clone.start(true);
				agent_threads_avail.put (hashCode, agent_clone);
			}
	
			if (log_.isDebugEnabled())
			{
				log_.debug("cloned agent thread | " + agent_clone.getKey());
			}
		}
	}

 	/**
     * Destroy the thread pool that this agent manages.
     */

    public void
	destroyThreads ()
    {
		for (java.util.Enumeration e = agent_threads_avail.keys(); e.hasMoreElements(); ) {
		    String agent_hash = (String) e.nextElement();
		    org.opensims.agent.Agent agent_clone = (org.opensims.agent.Agent) agent_threads_avail.get(agent_hash);

		    if (agent_clone != null) {
				agent_clone = null;
		    }
		}
	}


    /**
     * Start the agent running
     */

    public void
	start (boolean doScanStart)
    {	    
		setAgentThread(new Thread(this, "agent " + hashCode() + " - " + getKey()));
		getAgentThread().start();
	
		if (log_.isInfoEnabled()) {
			log_.info("start " + getAgentName() + ":" + getKey());
		}
		
		if (doScanStart) {
			if (!scan_queue.empty()) {
				org.opensims.model.Scan scan = (org.opensims.model.Scan) scan_queue.pop();
			
				if (scan != null) {
					setScan(scan);
					scan.setAgent(this);
					getScan().start();
					getScan().agentPostEvent("agent.login"); 
				}
			}
		}	
    }


    /**
     * If at any point we are unable to communicate 
     * stop() is called to remove this agent
     */

    public void
	stop ()
    {
		if (log_.isInfoEnabled()) {
			log_.info("stop " + getAgentName() + ":" + getKey());
		}
		destroyThreads();
		setAgentThread(null);
    }


    /**
     * Runnable interface
     */

    public void
	run ()
    {
	    Thread this_thread = Thread.currentThread();
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
	    if (log_.isInfoEnabled()) {
			log_.info("new agent running - " + getAgentName() + ":" + getKey());
			
			while (this_thread == agent_thread) {
				// Basically wait for the dispatcher to allocate us something
				try {
					org.jdom.Element root = null;
					
					Thread.sleep(200);
					try {
						if ((root = nextTransaction()) != null) {
							long then = getManager().getCorrelator().getTick();
	
							is_busy = true;
							((org.opensims.agent.AgentManager) getManager()).processAgentTransaction ( this, root );
							is_busy = false;
							
							if (log_.isDebugEnabled())
								log_.debug("agent process queued transaction <duration ms>: " + Long.toString( getManager().getCorrelator().getTick() - then ) + " remaning queue: " + transaction_queue.size() );
	
						}
					}
					catch (Exception e) {
					}
				} 
				catch (InterruptedException e) {
				   // The VM doesn't want us to sleep anymore,
				   // so get back to work
				}
			}		
	    }
    }



    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////


    /**
     * Represent as a String
     */

    public String
	getSummary ()
	{
		StringBuffer buf = new StringBuffer();
		StringBuffer symagent_vers = new StringBuffer();

/* // No perms to run symagent as tomcat --ME		
		try
		{
			String[] get_vers_command = new String[2];
			
			get_vers_command[0] = "/usr/local/bin/symagent";
			get_vers_command[1] = "--version";
			org.opensims.SystemScript scriptObj = new org.opensims.SystemScript(get_vers_command);
			scriptObj.run();
			symagent_vers = scriptObj.getStdOut();
		}
		catch (Exception e)
		{
			log_.error("While getting version of the libsymbiot / symagent",e);
		}
*/	
		buf.append("\n");
		buf.append("\tLocal Agent Versions\n");
//		buf.append("\t\t" + symagent_vers);
		buf.append("\n\t");
		buf.append(this.toString());
		buf.append("\t");
		buf.append(getKey());
		buf.append("\n\t ip: ");
		buf.append(ip_addr.toString());
		buf.append("\n\t local: ");
		buf.append(getLocalFlag());
		buf.append("\n\t scan: ");
		buf.append(getScan().getCurrentState());
		buf.append("\n\t last_beat: ");
		buf.append(getManager().getCorrelator().getTick() - getLastTick());
		buf.append("\n\t threads: ");
		buf.append(Long.toString(agent_threads));
		
		for (java.util.Enumeration e = agent_threads_avail.keys(); e.hasMoreElements(); ) {
			String agent_hash = (String) e.nextElement();
			org.opensims.agent.Agent agent_clone = (org.opensims.agent.Agent) agent_threads_avail.get(agent_hash);
			buf.append("\n\t    ");
			buf.append(agent_hash);
			buf.append(" : in transaction? ");
			buf.append(Boolean.toString(agent_clone.isBusy()));
		}
		buf.append("\n\t plugins loaded [threads]: ");
		
		for (java.util.Enumeration e = plugin_threads_avail.keys(); e.hasMoreElements(); ) {
			String plugin_name = (String) e.nextElement();
			String plugin_thread_name = plugin_name + ":0";
			String thread_num = String.valueOf(getPluginThreadsAvail(plugin_name));
			buf.append("\n\t    ");
			buf.append(plugin_name);
			buf.append("  [");
			buf.append(thread_num);
			buf.append("]");
		}
		
		return buf.toString();
	}


    //////////////////////////////////////////////////////////////////////
    // authentication/access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Create a nonce value
     */

    public static String
	createNonce ()
    {
		Object o = new Object();
		String nonce = Integer.toHexString(o.hashCode());
	
		return nonce;
    }


    /**
     * getNonce
     */

    public String
	getNonce ()
    {
		return nonce;
    }


    /**
     * getAgentName
     */

    public String
	getAgentName ()
    {
		return agent_name;
    }


    /**
     * getMacAddr
     */

    public String
	getMacAddr ()
    {
		return mac_addr;
    }


    /**
     * getGid
     */

    public String
	getGid ()
    {
		return gid;
    }


    
    /**
     * Get the localtime of this Agent as a String. e.g. "GMT"
     */

    public String
	getLocaltime ()
    {
    	// <LOCALHOST localtime="US/Central" latitude="0.0" longitude="0.0" altitude="0.0">
    	
		org.jdom.Element localhost_node = null;
		String localtime = "GMT";
	
		try {
			StringBuffer path = new StringBuffer("//");
	
			path.append(org.opensims.xml.Node.AGENT_NODE);
			path.append("/");
			path.append(org.opensims.xml.Node.LOCALHOST_NODE);
			
			localhost_node = (org.jdom.Element) getManager().getCorrelator().getConfig().selectSingleNode(path.toString());
			
			if (localhost_node != null) {
				if (log_.isDebugEnabled()) {
					log_.debug("get localhost node: path |" + path.toString() + "| = " + localhost_node);
					log_.debug(org.opensims.xml.XmlBuilder.formatXML(localhost_node, false, true));
				}
				String lt = localhost_node.getAttributeValue("localtime");
				if (lt != null)
					localtime = lt;
			}
		}
		catch (Exception e) {
			log_.error("get localhost node", e);
		}
	
		return localtime;
    }




    /**
     * Get the XML node for the network being scanned.
     */

    public org.jdom.Element
	getNetworkNode (String topology)
    {
		org.jdom.Element network_node = null;
	
		try {
			StringBuffer path = new StringBuffer("./");
	
//			path.append(org.opensims.xml.Node.AGENT_NODE);
//			path.append("[@local = 'true']/");
			path.append(org.opensims.xml.Node.INTERFACE_NODE);
			path.append("[@role = 'sniffer']/");
			path.append(org.opensims.xml.Node.NETWORK_NODE);
			path.append("[@topology = '");
			path.append(topology);
			path.append("']");
			
//			network_node = (org.jdom.Element) getManager().getCorrelator().getConfig().selectSingleNode(path.toString());
			network_node = (org.jdom.Element) org.jdom.xpath.XPath.selectSingleNode(getConfigNode(), path.toString());		
			
			if (network_node != null) {
				if (log_.isDebugEnabled()) {
					log_.debug("get network node: path |" + path.toString() + "| = " + network_node);
					log_.debug(org.opensims.xml.XmlBuilder.formatXML(network_node, false, true));
				}
			}
		}
		catch (Exception e) {
			log_.error("get network node", e);
		}
	
		return network_node;
    }


    /**
     * Get a hex representation for the network address being scanned, when provided with the topology
     */

    public String
	getNetworkNumber (String topology)
    {
		String result = "";
	
		try {
			//! This should test the validity of "getNetworkNode(topology)" before continuing.. -ME
			org.opensims.IPv4 network_addr = new org.opensims.IPv4(getNetworkNode(topology).getAttributeValue("cidr"));
	
			result = org.opensims.IPv4.longToHex(network_addr.getNetworkAddr()).toUpperCase();
			
			if (log_.isDebugEnabled()) {
				log_.debug("get network number- node attribute: " + getNetworkNode(topology).getAttributeValue("cidr") + " -Address: " + network_addr.getNetworkAddr() + " -Hex: " + result);
			}
			
		}
		catch (Exception e) {
			log_.error("get the network number for an agent", e);
		}
	
		return result;
    }


    /**
     * Get the device of the network being scanned, when provided with the topology
     */

    public String
	getNetworkDevice (String topology)
    {
    	org.jdom.Element interface_node = null;
    	String device = "eth0";
    	
		interface_node = (org.jdom.Element) getNetworkNode(topology).getParent();
		if (interface_node != null) 
			device = interface_node.getAttributeValue("name");	
		
		if (log_.isDebugEnabled()) {
			log_.debug("get network device: " + device);
			log_.debug(org.opensims.xml.XmlBuilder.formatXML(interface_node, false, true));
		}
		return device;
    }


    /**
     * Get the associated scan object
     */

    public org.opensims.model.Scan
	getScan ()
    {
		return scan;
    }

    /**
     * Get the associated scan object
     */

    public org.opensims.model.Scan
	getScan (String network_number)
    {
		return (org.opensims.model.Scan) scans_avail.get(network_number);
    }

    /**
     * Set the associated scan object
     */

    public void
	setScan (org.opensims.model.Scan scan)
    {
		this.scan = scan;
    }

	/**
     * Set the associated scan object
     */

    public void
	setScan (String network_number)
    {
		this.scan = (org.opensims.model.Scan) scans_avail.get(network_number);
    }
    
    
    /**
     * Adds a scan object to the hash table, indexed by Network
     */

    public void
	addScan (String network_number, org.opensims.model.Scan scan)
    {
		scans_avail.put (network_number, scan);
		scan_queue.push(scan);
    }


    /**
     * Get the merged config node.
     */

    public org.jdom.Element
	getConfigNode ()
    {
		return config_node;
    }


    /**
     * Set the merged config node.
     */

    public void
	setConfigNode (org.jdom.Element config_node)
    {
		this.config_node = config_node;
    }


    /**
     * Gets the state of the agent
     */

    public boolean
	isBusy ()
    {
		return is_busy;
    }
    
    
    /**
     * Get the local agent flag.
     */

    public boolean
	getLocalFlag ()
    {
		return local_flag;
    }


    /**
     * Set the local agent flag.
     */

    public void
	setLocalFlag (boolean local_flag)
    {
		this.local_flag = local_flag;
    }


    /**
     * Attempt to logout
     */

    public boolean
	logout ()
    {
		boolean result = true;
	
		try {		
			stop();
			
			/**
			 * @TODO needs logout
			org.opensims.db.sqlj.AgentLoginT t = agent_login_t.logoff();
	
			if (log_.isInfoEnabled()) {
			log_.info("logoff result - " + t.getResultCode());
			}
	
			throw new org.opensims.agent.LoginException("agent not provisioned: " + getAgentName() + " mac_addr " + getMacAddr() + " ip addr " + local_ip.toString());
			*/
		}
		catch (Exception e) {
			log_.error("agent logout", e);
			result = false;
		}
	
		return result;
    }


    //////////////////////////////////////////////////////////////////////
    // plugin/configuration management
    //////////////////////////////////////////////////////////////////////

    /**
     * Set the named plugin
     */

    public void
	setPlugin (String plugin_name, org.opensims.agent.Plugin plugin)
    {
		if (log_.isDebugEnabled()) {
			log_.debug("set plugin: " + plugin_name + " @ " + plugin);
		}
	
		avail_plugins.put(plugin_name, plugin);
    }


    /**
     * Get the named plugin, if available
     */

    public org.opensims.agent.Plugin 
	getPlugin (String plugin_name)
    {
		org.opensims.agent.Plugin plugin = (org.opensims.agent.Plugin) avail_plugins.get(plugin_name);
		
		if (log_.isDebugEnabled()) {
			log_.debug("get plugin: " + plugin_name + " @ " + plugin);
		}
	
		return plugin;
    }
    
    /**
     * Get a random named plugin thread, if available
     */

    public org.opensims.agent.Plugin 
	getRandomPlugin (String plugin_name)
    {
		String plugin_thread_name = plugin_name + ":" + String.valueOf(Math.round(Math.random() * (getPluginThreadsAvail(plugin_name) -1)));	
		org.opensims.agent.Plugin plugin = (org.opensims.agent.Plugin) avail_plugins.get(plugin_thread_name);
		
		if (log_.isDebugEnabled()) {
			log_.debug("get random plugin: " + plugin_thread_name + " @ " + plugin);
		}
	
		return plugin;
    }

    /**
     * Remove the named plugin
     */

    public void 
	removePlugin (String plugin_name)
	{
		if (log_.isDebugEnabled()) {
			log_.debug("remove plugin: " + plugin_name + " @ " + plugin_name);
		}	
		avail_plugins.remove(plugin_name);
	}


	/**
     * Set the number of threads available for plugin
     */

    public void
	setPluginThreadsAvail (String plugin_name, long threads)
    {
		if (log_.isDebugEnabled()) {
			log_.debug("set number of threads avail for plugin: " + plugin_name + " threads " + threads);
		}
	
		plugin_threads_avail.put(plugin_name, new Long(threads));
    }


    /**
     * Set the number of threads available for plugin
     */

    public long 
	getPluginThreadsAvail (String plugin_name)
    {
		Long threads = (Long) plugin_threads_avail.get(plugin_name);
	
		if (log_.isDebugEnabled()) {
			log_.debug("get number of threads avail for plugin: " + plugin_name + " threads " + threads);
		}
	
		return (threads.longValue());
    }
    
    
    /**
     * Parse the available plugins
     */

    public void
	parsePlugins (org.jdom.Element node)
	{
		org.jdom.Element avail_node = node.getChild(org.opensims.xml.Node.AVAIL_PLUGINS_NODE);
	
		if (avail_node != null) {
			org.jdom.Element merged_node = ((org.opensims.agent.AgentManager) getManager()).mergeAvailConfig(avail_node, getConfigNode());
			java.util.Iterator plugin_iter = merged_node.getChildren(org.opensims.xml.Node.PLUGIN_NODE).iterator();
	
			while (plugin_iter.hasNext()) {
				// <PLUGIN enabled="true" name="symplugin-tester" signature="99juybb2UBNtHvmNw7b5PqPILhY=" version="1.0.0" class="org.opensims.agent.TesterPlugin">		
				org.jdom.Element plugin_node = (org.jdom.Element) plugin_iter.next();
				String plugin_name = plugin_node.getAttributeValue("name");
				String class_name = plugin_node.getAttributeValue("class");
				long threads = 1L;
				
				if (plugin_node.getAttributeValue("threads") != null)
					threads = Long.valueOf(plugin_node.getAttributeValue("threads")).longValue();
		
				// update the hash table linking # of threads to each plugin by name
				setPluginThreadsAvail(plugin_name, threads);
		
				// apply dynamic loading of Plugin subclasses
				try {
					if (class_name != null) {							
						// Each plugin can have its own thread pool, per the configuration
						for (long this_thread_number = 0L; this_thread_number < threads; this_thread_number++) {
							Class plugin_class = Class.forName(class_name);
							org.opensims.agent.Plugin plugin = (org.opensims.agent.Plugin) plugin_class.newInstance();
							String plugin_thread_name = plugin_name + ":" + Long.toString(this_thread_number);
							
							plugin.init(getManager().getCorrelator(), this, plugin_name, plugin_node, this_thread_number);
							plugin.start();
							
							// update the hash table linking the threaded plugin name to the plugin object
							setPlugin(plugin_thread_name, plugin);
							
							if (log_.isDebugEnabled()) {
								log_.debug("plugin support dynamically loaded for " + class_name + " - " + this_thread_number + " - " + plugin);
							}
						}
					}
				}
				catch (InstantiationException e) {
					log_.error("instance exception: " + class_name);
				}
				catch (ClassNotFoundException e) {
					log_.error("class not found: " + class_name);
				}
				catch (Exception e) {
					log_.error("load class " + class_name, e);
				}
			}
	
			// save the merged results
			org.jdom.Element new_config_node = (org.jdom.Element) getConfigNode().clone();
			new_config_node.removeChildren(org.opensims.xml.Node.PLUGIN_NODE);
			new_config_node.addContent(merged_node);
	
			if (log_.isDebugEnabled()) {
				log_.debug("MERGED new config_node: " + org.opensims.xml.XmlBuilder.formatXML(new_config_node, false, true));
			}
			
			setConfigNode(new_config_node);
		}
		// reply with config
		setConfigResponse();
	}


    /**
     * Load the agent configuration parameters from the configuration
     * DB
     */

    public void
	setConfigResponse ()
	{
		StringBuffer config = new StringBuffer();
	
		config.append("<AGENT><CONFIG nonce=\"");
		config.append(getNonce());
		config.append("\">");
	
		try {
			if (!getConfigLookAside(config)) {
			setHeartbeatExpiry(getConfigNode().getAttribute("heartbeat").getLongValue() * 2000L);
			String heartbeat = getConfigNode().getAttributeValue("heartbeat");
	
			config.append("<HEARTBEAT_INTERVAL value=\"").append(heartbeat).append("\"/>");
	
			/**
			 * @TODO does an agent (not a plugin) need more of its own config XML components?
	
			String config_str = agent_login_t.getConfig();
	
			if (config_str != null) {
				config.append(config_str);
			}
			 */
	
			// articulate the specific configuration for each enabled plugin
	
			config.append("<ENABLE_PLUGINS>");
	
			for (java.util.Enumeration e = plugin_threads_avail.keys(); e.hasMoreElements(); ) {
				String plugin_name = (String) e.nextElement();
				String plugin_thread_name = plugin_name + ":0";
				org.opensims.agent.Plugin plugin = getPlugin(plugin_thread_name);
	
				if ((plugin != null) && plugin.getEnabled()) {
					config.append(plugin.getConfig());
				}
			}
	
			config.append("</ENABLE_PLUGINS>");
			}
		}
		catch (Exception e) {
			log_.error("configure agent", e);
		}
		
		config.append("</CONFIG></AGENT>");
		enqueueCommand(config.toString(), true);
	}


    /**
     * Configuration lookaside, used for regression testing.  If an
     * XML file with the agent's MAC address exists in the "var.lib"
     * directory, then the agent config will be read from there
     * instead of the usual "webapp.xml".
     */

    protected boolean
	getConfigLookAside (StringBuffer config_buf)
	{
		boolean result = false;
	
		if (log_.isDebugEnabled()) {
			try {
				java.io.File var_lib = new java.io.File(getManager().getCorrelator().getConfig("var.lib"));
				java.io.File config_file = new java.io.File(var_lib, getMacAddr() + ".xml");
		
				if (config_file.canRead()) {
					if (log_.isDebugEnabled()) {
						log_.debug("config lookaside - " + config_file.toString());
					}
		
					java.io.FileReader file_reader = new java.io.FileReader(config_file);
					java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);
		
					String line = buf_reader.readLine();
		
					while (line != null) {
						config_buf.append(line);
						config_buf.append("\n");			
						line = buf_reader.readLine();
					}
		
					result = true;
					buf_reader.close();
				}
			}
			catch (Exception e) {
				log_.error("configure agent - lookaside", e);
			}
		}
	
		return result;	
	}


    //////////////////////////////////////////////////////////////////////
    // response/command handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Log the heartbeat, check for waiting periodic tasks... this
     * provides the "crontab" of a SIMS correlation engine
     */

    public void
	checkBeatTasks (org.jdom.Element node)
    {
		setLastTick(getManager().getCorrelator().getTick());
	
		if (log_.isDebugEnabled()) {
			log_.debug(org.opensims.xml.XmlBuilder.formatXML(node, false, true));
			log_.debug(getKey() + " local? " + getLocalFlag());
		}
	
		// if this is the local agent completing a login, then run
		// autodiscovery on the configured interfaces	
		getManager().getCorrelator().getScanManager().agentPostEvent(this, "agent.heartbeat");
    }


    /**
     * Queue the given command for an agent
     */

    public void
	enqueueCommand (String command, boolean is_config)
    {
		if (log_.isDebugEnabled()) {
			log_.debug("enqueue command: " + command);
		}
	
		if (is_config) {
			command_queue.push(command);
		}
		else {
			command_queue.push("<COMMAND>" + command + "</COMMAND>");
		}
    }


    /**
     * Pop the next a queued response... configuration must take
     * priority over other commands.
     */

    public String
	nextCommand ()
	throws Exception
    {
		String command = null;
	
		if (!command_queue.empty()) {
			command = (String) command_queue.pop();
	
			if (log_.isDebugEnabled()) {
				log_.debug("send agent - " + command);
			}
		}
		else {
			if (log_.isDebugEnabled()) {
				log_.debug("send agent - zero content returned");
			}
		}
	
		return command;
    }

    /**
     * Queue the given transaction for an agent/plugin
     */

    public void
	enqueueTransaction (org.jdom.Element node)
    {
		if (log_.isDebugEnabled()) {
			log_.debug("enqueue transaction: " + node);
		}
	    transaction_queue.push(node);
    }


    /**
     * Pop the next a queued transaction for an agent/plugin
     */

    public org.jdom.Element
	nextTransaction ()
	throws Exception
    {
		org.jdom.Element node = null;

		if (!transaction_queue.empty()) {
			node = (org.jdom.Element) transaction_queue.pop();

	    if (log_.isDebugEnabled()) {
			log_.debug("dequeue transaction - " + node);
	    }
	}

	return node;
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
	getKey (String agent_name, String mac_addr)
    {
		String key = agent_name + ":" + mac_addr;
	
		return key;
    }


    /**
     * Get the hashtable lookup key
     */

    public String
	getKey ()
    {
		return getKey(getAgentName(), getMacAddr());
    }


    /**
     * Check the expiry date/time
     */

    public boolean
	checkExpiry (long model_tick)
    {
		boolean result = false;
		long period = model_tick - getLastTick();
	
		/**
		 * way too much detail, even for DEBUG level - but great for testing race conditions!
	
		if (log_.isDebugEnabled()) {
			log_.debug("check heartbeat expiry: period " + period + " > expiry " + getHeartbeatExpiry() + " ?= " + (period > getHeartbeatExpiry()));
		}
	
		*/
	
		if (period > getHeartbeatExpiry()) {
			
			// Either the agent is non-responsive, or the known bug is happening where the symagent
			// stops sending the <BEAT>.
		
			this.logout();
			getManager().remove(getKey());
			result = true;
		}
	
		return result;
    }


    /**
     * Get the heartbeat expiry period
     */

    public long
	getHeartbeatExpiry ()
    {
		return heartbeat_expiry;
    }


    /**
     * Set the heartbeat expiry period
     */

    public void
	setHeartbeatExpiry (long heartbeat_expiry)
    {
		this.heartbeat_expiry = heartbeat_expiry;
	
		if (log_.isDebugEnabled()) {
			log_.debug("set heartbeat expiry: " + heartbeat_expiry);
		}
    }

}
