/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: Agent.java,v 1.24 2006/02/20 05:55:30 mikee Exp $
 * $Id: Agent.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.24 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public interface
    Agent
    extends Runnable, 
    	Cloneable,
    	org.opensims.Tickable
    	
{
    /**
     * Initialize the agent object
     */

    public void
	init (org.opensims.agent.AgentManager agent_manager, org.jdom.Element config_node, String agent_name, String mac_addr, org.opensims.IPv4 ip_addr, String nonce, String app_sig, String gid, long agent_threads);

    //////////////////////////////////////////////////////////////////////
    // Runnable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Clone
     */

    public Object
	clone ();


    /**
     * Get the agent thread.
     */

    public Thread
	getAgentThread ();


    /**
     * Set the agent thread.
     */

    public void
	setAgentThread (Thread agent_thread);


    /**
     * Create the thread pool that this agent manages.
     */

    public void
	createThreads ();


 	/**
     * Destroy the thread pool that this agent manages.
     */

    public void
	destroyThreads ();


    /**
     * Start the agent running
     */

    public void
	start (boolean doScanStart);

	
    /**
     * Stop the agent 
     */
	
    public void
	stop ();


    /**
     * Runnable interface
     */

    public void
	run ();

    //////////////////////////////////////////////////////////////////////
    // Misc methods
    //////////////////////////////////////////////////////////////////////


    /**
     * Represent as a String
     */

    public String
	getSummary ();


    /**
     * getNonce
     */

    public String
	getNonce ();


    /**
     * getAgentName
     */

    public String
	getAgentName ();


    /**
     * getMacAddr
     */

    public String
	getMacAddr ();


    /**
     * getGid
     */

    public String
	getGid ();


    /**
     * Get the localtime of this Agent as a String. e.g. "GMT"
     */

    public String
	getLocaltime ();
	

    /**
     * Get the XML node for the network being scanned.
     */

    public org.jdom.Element
	getNetworkNode (String topology);


    /**
     * Get a hex representation for the network address being scanned.
     */

    public String
	getNetworkNumber (String topology);


    /**
     * Get the device of the network being scanned, when provided with the topology
     */

    public String
	getNetworkDevice (String topology);


    /**
     * Get the associated scan object
     */

    public org.opensims.model.Scan
	getScan ();


    /**
     * Get the associated scan object
     */

    public org.opensims.model.Scan
	getScan (String network_number);
	

    /**
     * Set the associated scan object
     */

    public void
	setScan (org.opensims.model.Scan scan);

	/**
     * Set the associated scan object
     */

    public void
	setScan (String network_number);

    /**
     * Adds a scan object to the hash table, indexed by Network
     */

    public void
	addScan (String network_number, org.opensims.model.Scan scan);

    /**
     * Get the merged config node.
     */

    public org.jdom.Element
	getConfigNode ();


    /**
     * Set the merged config node.
     */

    public void
	setConfigNode (org.jdom.Element config_node);


    /**
     * Gets the state of the agent
     */

    public boolean
	isBusy ();
	
    
    /**
     * Get the local agent flag.
     */

    public boolean
	getLocalFlag ();


    /**
     * Set the local agent flag.
     */

    public void
	setLocalFlag (boolean is_local);


    /**
     * Attempt to logout
     */

    public boolean
	logout ();


    /**
     * Set the named plugin
     */

    public void
	setPlugin (String plugin_name, org.opensims.agent.Plugin plugin);

    /**
     * Get the named plugin, if available
     */

    public org.opensims.agent.Plugin 
	getPlugin (String plugin_name);	    
	    
	    
    /**
     * Get a random named plugin thread, if available
     */

    public org.opensims.agent.Plugin 
	getRandomPlugin (String plugin_name);


    /**
     * Remove the named plugin
     */

    public void 
	removePlugin (String plugin_name);
	    
	/**
     * Set the number of threads available for plugin
     */

    public void
	setPluginThreadsAvail (String plugin_name, long threads);


    /**
     * Set the number of threads available for plugin
     */

    public long
	getPluginThreadsAvail (String plugin_name);


    /**
     * Parse the available plugins
     */

    public void
	parsePlugins (org.jdom.Element node);


    /**
     * Load the agent configuration paramters from the configuration DB
     */

    public void
	setConfigResponse ();


    /**
     * Log the heartbeat, check for waiting periodic tasks... this
     * provides the "crontab" of a SIMS correlation engine
     */

    public void
	checkBeatTasks (org.jdom.Element node);


    /**
     * Queue the given command for an agent
     */

    public void
	enqueueCommand (String command, boolean is_config);


    /**
     * Pop the next a queued response... configuration must take
     * priority over other commands.
     */

    public String
	nextCommand ()
	throws Exception;

    /**
     * Queue the given transaction for an agent/plugin
     */

    public void
	enqueueTransaction (org.jdom.Element node);


    /**
     * Pop the next a queued transaction for an agent/plugin
     */
	
    public org.jdom.Element
	nextTransaction ()
	throws Exception;



    //////////////////////////////////////////////////////////////////////
    // Tickable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Manager
     */

    public org.opensims.Manager
        getManager ();


    /**
     * Set the Manager
     */

    public void
        setManager (org.opensims.Manager manager);


    /**
     * Get the last_tick value - for expiry
     */

    public long
	getLastTick ();


    /**
     * Set the last_tick value - for expiry
     */

    public void
	setLastTick (long last_tick);


    /**
     * Get the hashtable lookup key
     */

    public String
	getKey ();


    /**
     * Check the expiry date/time
     */

    public boolean
	checkExpiry (long model_tick);
}
