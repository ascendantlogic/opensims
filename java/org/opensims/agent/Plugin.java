/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: Plugin.java,v 1.9 2005/10/17 01:14:48 mikee Exp $
 * $Id: Plugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.9 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    Plugin extends Runnable
{

    /**
     * Load dynamic instances of subclasses, based on the config file.
     */

    public void
	init (org.opensims.Correlator correlator, org.opensims.agent.Agent agent, String name, org.jdom.Element runtime_node, long thread_number);

    //////////////////////////////////////////////////////////////////////
    // Runnable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the plugin thread.
     */

    public Thread
	getPluginThread ();


    /**
     * Set the plugin thread.
     */

    public void
	setPluginThread (Thread plugin_thread);


    /**
     * Start the plugin running.
     */

    public void
	start ();


    /**
     * If at any point we are unable to communicate 
     * stop() is called to remove this plugin
     */

    public void
	stop ();


    /**
     * Runnable interface
     */

    public void
	run ();




    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Correlator object.
     */

    public org.opensims.Correlator
	getCorrelator ();


    /**
     * Set the Correlator object.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator);


    /**
     * Get the parent Agent.
     */

    public org.opensims.agent.Agent
	getAgent ();


    /**
     * Set the parent Agent.
     */

    public void
	setAgent (org.opensims.agent.Agent agent);


    /**
     * Get the Agent's parent Scan.
     */

    public org.opensims.model.Scan
	getScan ();


    /**
     * Set the Agent's parent Scan.
     */

    public void
	setScan (org.opensims.model.Scan scan);


    /**
     * Get the Plugin name.
     */

    public String
	getName ();


    /**
     * Set the Plugin name.
     */

    public void
	setName (String name);


    /**
     * Get the runtime XML specifications sent from the Agent.
     */

    public org.jdom.Element
	getRuntimeNode ();


    /**
     * Set the runtime XML specifications sent from the Agent.
     */

    public void
	setRuntimeNode (org.jdom.Element runtime_node);


    /**
     * Get the enabled flag.
     */

    public boolean
	getEnabled ();


    /**
     * Set the enabled flag.
     */

    public void
	setEnabled (boolean enabled);

    /**
     * Get the total number of threads.
     */

    public long
	getThreads ();


    /**
     * Set the total number of threads.
     */

    public void
	setThreads (long total_threads);
	
    /**
     * Get the the thread number for this plugin instance.
     */

    public long
	getThisThreadNumber ();


    /**
     * Set the the thread number for this plugin instance.
     */

    public void
	setThisThreadNumber (long this_thread);
    
    /**
     * Get the Plugin version.
     */

    public String
	getVersion ();


    /**
     * Set the Plugin version.
     */

    public void
	setVersion (String version);


    /**
     * Get the Plugin signature.
     */

    public String
	getSignature ();


    /**
     * Set the Plugin signature.
     */

    public void
	setSignature (String signature);


    //////////////////////////////////////////////////////////////////////
    // configuration methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Only configure plugins which are both AVAILABLE and ENABLED.
     */

    public String
	getConfig ();


    /**
     * Execute a specific configuration, e.g. during autodiscovery.
     */

    public boolean
	doConfig (org.opensims.model.Scan scan, String param);


    //////////////////////////////////////////////////////////////////////
    // parsing methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse the incoming XML node sent from the agent.
     */

    public boolean
	parseNode (String node_name, org.jdom.Element node);
}
