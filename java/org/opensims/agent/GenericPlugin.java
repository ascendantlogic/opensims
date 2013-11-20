/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: GenericPlugin.java,v 1.7 2005/10/17 01:14:48 mikee Exp $
 * $Id: GenericPlugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.7 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    GenericPlugin
    implements org.opensims.agent.Plugin
{
    // protected fields

    protected org.opensims.Correlator correlator = null;
    protected org.opensims.agent.Agent agent = null;
    protected org.opensims.model.Scan scan = null;
    protected String name = null;
    protected org.jdom.Element runtime_node = null;
    protected boolean enabled = false;
    protected long total_threads = 0L;
    protected long this_thread_number = 0L;
    protected String version = null;
    protected String signature = null;
    protected volatile Thread plugin_thread = null;

    // quality assurance
    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.GenericPlugin.class.getName());


    /**
     * Load dynamic instances of subclasses, based on the config file.
     */

    public void
	init (org.opensims.Correlator correlator, org.opensims.agent.Agent agent, String name, org.jdom.Element runtime_node, long this_thread_number)
    {
	setCorrelator(correlator);
	setAgent(agent);
	setName(name);
	setRuntimeNode(runtime_node);
	setThisThreadNumber(this_thread_number);
	
	long threads = 1L;		
	if (getRuntimeNode().getAttributeValue("threads") != null)
		threads = Long.valueOf(getRuntimeNode().getAttributeValue("threads")).longValue();

	setThreads(threads);
	setEnabled(Boolean.valueOf(getRuntimeNode().getAttributeValue("enabled")).booleanValue());
	setVersion(getRuntimeNode().getAttributeValue("version"));
	setSignature(getRuntimeNode().getAttributeValue("signature"));
    }

    //////////////////////////////////////////////////////////////////////
    // Runnable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the plugin thread.
     */

    public Thread
	getPluginThread ()
    {
	return plugin_thread;
    }


    /**
     * Set the plugin thread.
     */

    public void
	setPluginThread (Thread plugin_thread)
    {
	this.plugin_thread = plugin_thread;
    }


    /**
     * Start the plugin running.
     */

    public void
	start ()
    {
    
	setPluginThread(new Thread(this, "plugin " + hashCode() + " - " + name + " " + this_thread_number));
	getPluginThread().start();

	if (log_.isInfoEnabled()) {
	    log_.info("start " + getName());
	}
    }


    /**
     * If at any point we are unable to communicate 
     * stop() is called to remove this plugin
     */

    public void
	stop ()
    {
	if (log_.isInfoEnabled()) {
	    log_.info("stop " + getName());
	}

	synchronized (getAgent()) {
	    getAgent().removePlugin(name);
	}	
	setPluginThread(null);
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
		log_.info("new plugin running - " + getName() + " " + this_thread_number);
	    }
	    
	    while (this_thread == plugin_thread) {
			// Basically wait for the dispatcher to allocate us something
			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
			   // The VM doesn't want us to sleep anymore,
			   // so get back to work
			}
		}

    }



    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Correlator object.
     */

    public org.opensims.Correlator
	getCorrelator ()
    {
	return correlator;
    }


    /**
     * Set the Correlator object.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator)
    {
	this.correlator = correlator;
    }


    /**
     * Get the parent Agent.
     */

    public org.opensims.agent.Agent
	getAgent ()
    {
	return agent;
    }


    /**
     * Set the parent Agent.
     */

    public void
	setAgent (org.opensims.agent.Agent agent)
    {
	this.agent = agent;
    }


    /**
     * Get the Agent's parent Scan.
     */

    public org.opensims.model.Scan
	getScan ()
    {
	return scan;
    }


    /**
     * Set the Agent's parent Scan.
     */

    public void
	setScan (org.opensims.model.Scan scan)
    {
	this.scan = scan;
    }


    /**
     * Get the Plugin name.
     */

    public String
	getName ()
    {
	return name;
    }


    /**
     * Set the Plugin name.
     */

    public void
	setName (String name)
    {
	this.name = name;
    }


    /**
     * Get the runtime XML specifications sent from the Agent.
     */

    public org.jdom.Element
	getRuntimeNode ()
    {
	return runtime_node;
    }


    /**
     * Set the runtime XML specifications sent from the Agent.
     */

    public void
	setRuntimeNode (org.jdom.Element runtime_node)
    {
	this.runtime_node = runtime_node;
    }


    /**
     * Get the total number of threads.
     */

    public long
	getThreads ()
    {
	return total_threads;
    }


    /**
     * Set the total number of threads.
     */

    public void
	setThreads (long total_threads)
    {
	this.total_threads = total_threads;
    }

    /**
     * Get the the thread number for this plugin instance.
     */

    public long
	getThisThreadNumber ()
    {
	return this_thread_number;
    }


    /**
     * Set the the thread number for this plugin instance.
     */

    public void
	setThisThreadNumber (long this_thread_number)
    {
	this.this_thread_number = this_thread_number;
    }


    /**
     * Get the enabled flag.
     */

    public boolean
	getEnabled ()
    {
	return enabled;
    }


    /**
     * Set the enabled flag.
     */

    public void
	setEnabled (boolean enabled)
    {
	this.enabled = enabled;
    }


    /**
     * Get the Plugin version.
     */

    public String
	getVersion ()
    {
	return version;
    }


    /**
     * Set the Plugin version.
     */

    public void
	setVersion (String version)
    {
	this.version = version;
    }


    /**
     * Get the Plugin signature.
     */

    public String
	getSignature ()
    {
	return signature;
    }


    /**
     * Set the Plugin signature.
     */

    public void
	setSignature (String signature)
    {
	this.signature = signature;
    }


    //////////////////////////////////////////////////////////////////////
    // configuration methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Only configure plugins which are both AVAILABLE and ENABLED.
     */

    public String
	getConfig ()
    {
	StringBuffer config = new StringBuffer();
	org.jdom.Element config_node = (org.jdom.Element) getRuntimeNode().getChild(org.opensims.xml.Node.CONFIG_NODE);

	if (config_node != null) {
	    // go (con)figure

	    config.append("<PLUGIN name=\"").append(getName()).append("\">");
	    config.append(org.opensims.xml.XmlBuilder.formatXML(config_node, false, false));
	    config.append("</PLUGIN>");

	    if (log_.isDebugEnabled()) {
		log_.debug("plugin name " + getName() + " sig " + getSignature() + " version " + getVersion() + " config " + config.toString());
	    }
	}

	return config.toString();
    }


    /**
     * Execute a specific configuration, e.g. during autodiscovery.
     */

    public boolean
	doConfig (org.opensims.model.Scan scan, String param)
    {
	boolean result = false;

	return result;
    }


    //////////////////////////////////////////////////////////////////////
    // parsing methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse the incoming XML node sent from the Agent.
     */

    public boolean
	parseNode (String node_name, org.jdom.Element node)
    {
	boolean valid_transact = false;

	return valid_transact;
    }
}
