/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: Correlator.java,v 1.41 2007/02/20 00:42:36 jeff Exp $
 * $Id: Correlator.java 1 2008-01-10 18:37:05Z smoot $
 * OpenSIMS correlation engine
 * @version $Revision: 1.41 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. Erwin <mikee@symbiot.com>
 */

public interface
    Correlator
    extends Runnable
{
    /**
     * Initialize the correlation engine
     */

    public void
	init (org.opensims.servlet.Servlet servlet, org.opensims.Config config, org.jdom.Element correlator_node);


    /**
     * Establish the managers
     */

    public void
	initManagers();


    /**
     * Represent as a String
     */

    public String
	getSummary ();


    //////////////////////////////////////////////////////////////////////
    // configuration management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the containing servlet.
     */

    public org.opensims.servlet.Servlet
	getServlet ();


    /**
     * Set the containing servlet.
     */

    public void
	setServlet (org.opensims.servlet.Servlet servlet);


    /**
     * Get the configuration document object
     */

    public org.opensims.Config
        getConfig ();


    /**
     * Set the configuration document object
     */

    public void
        setConfig (org.opensims.Config config);


    /**
     * Get a configuration property
     */

    public String
        getConfig (String name);


    /**
     * Return the fully qualified domain name (FQDN).
     */

    public String
	getName ();


    //////////////////////////////////////////////////////////////////////
    // manager access
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the AgentManager.
     */

    public org.opensims.agent.AgentManager
	getAgentManager ();


    /**
     * Return the ScanManager.
     */

    public org.opensims.model.ScanManager
	getScanManager ();


    /**
     * Return the BogeyManager.
     */

    public org.opensims.model.BogeyManager
	getBogeyManager ();


    /**
     * Return the AlertManager.
     */

    public org.opensims.alert.AlertManager
	getAlertManager ();


    /**
     * Return the NotifyManager.
     */

    public org.opensims.notify.NotifyManager
	getNotifyManager ();


    /**
     * Return the ReportManager.
     */

    public org.opensims.report.ReportManager
	getReportManager ();


    /**
     * Return the SubscriberManager.
     */

    public org.opensims.client.SubscriberManager
	getSubscriberManager ();


    //////////////////////////////////////////////////////////////////////
    // handle agent messages
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse an incoming <BEAT/> node from an agent
     */

    public void
	handleAgentBeat (org.opensims.agent.Agent agent, org.jdom.Element node);


    /**
     * Parse an incoming <NOTICE/> node from an agent
     */

    public void
	handleAgentNotice (org.opensims.agent.Agent agent, org.jdom.Element node);


    /**
     * Send alerts through notification
     */

    public void
	handleAlert (org.opensims.alert.Alert alert);


    //////////////////////////////////////////////////////////////////////
    // network model management
    //////////////////////////////////////////////////////////////////////

    /**
     * XML parsing - using JDOM/SAX push parsing
     */

    public org.jdom.Document
        buildXml (java.io.BufferedReader buf_reader)
        throws org.jdom.JDOMException, Exception;


    /**
     * Enable event correlation
     */

    public void
	setEngineEnable (boolean engine_enable);


    /**
     * Get event correlation state
     */

    public boolean
	getEngineEnable ();


    /**
     * Insert an alert into the database
     */

	public org.opensims.db.om.Alert
	saveAlert( org.opensims.db.om.Alert alert )
	throws java.lang.Exception;

	/**
	 * process an incoming alert
	 */

    public org.opensims.alert.Alert
	correlateAlert (org.opensims.agent.Agent agent, String source, String unique_id, org.opensims.model.HostOrBogey src_host, String src_port, org.opensims.model.HostOrBogey dst_host, String dst_port, String protocol, long tick);


    /**
     * Get a list of all scan objects available to the correlator
     */

    public java.util.ArrayList
	getScanList ();


    /**
     * Get a list of all scan objects available to the correlator
     */

    public org.opensims.model.Scan
	getDefaultScan ();
	

    /**
     * Use the activity window, etc., to filter the alert/notify streams
     */

    public java.util.ArrayList
	getAlertList (org.opensims.stream.Stream stream, long tick, int alert_throttle);


    /**
     * Render NET/ATK/DEF/ALT/CON as XML, to build a content document
     */

    public org.jdom.Document
	buildContentDocument (long tick, java.util.ArrayList scan_list, java.util.ArrayList alert_list);
	
	public org.jdom.Document
	buildContentDocument (long tick, java.util.ArrayList scan_list, java.util.ArrayList alert_list, String group);


    //////////////////////////////////////////////////////////////////////
    // time flies when
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the current simulation time.
     */

    public long
	getTick ();


    /**
     * Format a timestamp into a tick used for internal time
     * representations
     */

    public long
	parseTick (String timestamp);


    /**
     * Format a tick from the internal representation into SQL
     * date/time format
     */

    public String
	formatTick (long tick);


    /**
     * Format a tick from the internal representation into a human readable
     * date/time format
     */

    public String
	formatReadableTick (long tick);
	
	
  	/**
     * Format a difference between 2 ticks as human readable
     */

    public String
	formatTickDifference (long firstTick, long secondTick);					



    //////////////////////////////////////////////////////////////////////
    // repository access
    //////////////////////////////////////////////////////////////////////

    /**
     * Get a URL to authenticate and connect with the shared
     * repository.
     */

    public org.opensims.crypto.RepositoryUrl
	getRepositoryUrl (String method, String uri_path, java.util.Hashtable param_table, StringBuffer content);


    //////////////////////////////////////////////////////////////////////
    // update management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Updater.
     */

    public org.opensims.Updater
	getUpdater ();


    /**
     * Set the Updater.
     */

    public void
	setUpdater (org.opensims.Updater updater);


    //////////////////////////////////////////////////////////////////////
    // Runnable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Start the threaded components.
     */

    public void
	start ();


    /**
     * Stop the threaded components.
     */

    public void
	stop ();


    /**
     * Run the correlation engine.
     */

    public void 
	run ();
}
