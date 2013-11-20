/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: SimsCorrelator.java,v 1.70 2007/05/14 19:05:01 mikee Exp $
 * $Id: SimsCorrelator.java 1 2008-01-10 18:37:05Z smoot $
 * OpenSIMS correlation engine
 * @version $Revision: 1.70 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. Erwin <mikee@symbiot.com>
 */

public class
    SimsCorrelator
    implements org.opensims.Correlator
{
    // public final statics

    public final static String CVS_REVISION = "$Revision: 1.70 $";

    public final static org.opensims.xml.XmlBuilder xml_builder = new org.opensims.xml.XmlBuilder(false);

    public final static String FORMAT_SQL_DATE = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    public final static java.text.SimpleDateFormat format_sql_date = new java.text.SimpleDateFormat(FORMAT_SQL_DATE);

	public final static String FORMAT_READABLE_DATE = "yyyy-MM-dd HH:mm:ss";
    public final static java.text.SimpleDateFormat format_readable_date = new java.text.SimpleDateFormat(FORMAT_READABLE_DATE);


    // protected fields

    protected org.opensims.servlet.Servlet servlet = null;
    protected org.opensims.Config config = null;
    protected org.opensims.Updater updater = null;
    protected org.jdom.Element correlator_node = null;
    protected volatile Thread init_thread = null;
    protected java.util.ArrayList timerList = new java.util.ArrayList();
    protected boolean engine_enable = false;

    protected org.opensims.agent.AgentManager agent_manager = null;
    protected org.opensims.model.ScanManager scan_manager = null;
    protected org.opensims.model.BogeyManager bogey_manager = null;
    protected org.opensims.alert.AlertManager alert_manager = null;
    protected org.opensims.notify.NotifyManager notify_manager = null;
    protected org.opensims.report.ReportManager report_manager = null;
    protected org.opensims.client.SubscriberManager subscriber_manager = null;

	protected java.util.Hashtable alert_limbo_hash = new java.util.Hashtable();
//	protected org.opensims.db.om.Alert last_alert = null;
//	protected Object last_alert_lock = new Object();
	protected long max_alert_ticks = 5 * 1000; //todo: allow override in config param

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.SimsCorrelator.class.getName());


    /**
     * Initialize the Correlator object
     */

    public void
	init (org.opensims.servlet.Servlet servlet, org.opensims.Config config, org.jdom.Element correlator_node)
    {
		setServlet(servlet);
		setConfig(config);
	
		this.correlator_node = correlator_node;
	
		// initialize the database support
		try {
			java.io.File build_dir = new java.io.File(getConfig("opensims.home"), "build");
				java.io.File torque_props = new java.io.File(build_dir, "torque.properties");
	
			org.apache.torque.Torque.init(torque_props.toString());
		}
		catch (org.apache.torque.TorqueException e) {
			log_.error("initializing Torque for object relational mapping", e);
		}
	
		// initialize the updater	
		try {
		
			// select its Config node
			StringBuffer path = new StringBuffer("//");

			path.append(org.opensims.xml.Node.PRODUCT_NODE);
			path.append("/");
			path.append(org.opensims.xml.Node.UPDATE_NODE);

			org.jdom.Element update_node = (org.jdom.Element) getConfig().selectSingleNode(path.toString());
	
			// instantiate the class	
			String class_name = update_node.getAttributeValue("class");
			Class c = Class.forName(class_name);
	
			org.opensims.Updater updater = (org.opensims.Updater) c.newInstance();
	
			updater.init(this, update_node);
			setUpdater(updater);
		}
		catch (Exception e) {
			log_.error("initializing the updater", e);
		}
		initManagers();
    }


    /**
     * Establish the managers
     */

    public void
	initManagers()
    {
		agent_manager = new org.opensims.agent.AgentManager(this);
		scan_manager = new org.opensims.model.ScanManager(this);
		bogey_manager = new org.opensims.model.BogeyManager(this);
		alert_manager = new org.opensims.alert.AlertManager(this);
		notify_manager = new org.opensims.notify.NotifyManager(this);
		report_manager = new org.opensims.report.ReportManager(this);
		subscriber_manager = new org.opensims.client.SubscriberManager(this);
    }


    /**
     * Represent as a String
     */

    public String
	getSummary ()
    {
		StringBuffer buf = new StringBuffer();
		
		buf.append("\n software updater: " + getUpdater().getSummary());
	
		for (java.util.Enumeration e = getSubscriberManager().elements(); e.hasMoreElements(); ) {
			org.opensims.client.Subscriber subscriber = (org.opensims.client.Subscriber) e.nextElement();
			buf.append("\n subscriber: " + subscriber.getSummary());
		}
	
		buf.append("\n ");
	
		for (java.util.Enumeration e = getAgentManager().elements(); e.hasMoreElements(); ) {
			org.opensims.agent.Agent agent = (org.opensims.agent.Agent) e.nextElement();
			buf.append("\n agent: " + agent.getSummary());
		}
	
		buf.append("\n ");
	
		for (java.util.Enumeration e = getScanManager().elements(); e.hasMoreElements(); ) {
			org.opensims.model.Scan scan = (org.opensims.model.Scan) e.nextElement();
			buf.append("\n scan: " + scan.getSummary());
		}
	
		buf.append("\n ");
	
		for (java.util.Enumeration e = getReportManager().elements(); e.hasMoreElements(); ) {
			org.opensims.report.Report report = (org.opensims.report.Report) e.nextElement();
			buf.append("\n report: " + report.getSummary());
		}
	
		buf.append("\n ");
	
		for (java.util.Enumeration e = getNotifyManager().elements(); e.hasMoreElements(); ) {
			org.opensims.notify.Notify notify = (org.opensims.notify.Notify) e.nextElement();
			buf.append("\n notify: " + notify.getSummary());
		}
	
		buf.append("\n ");
	
		for (java.util.Enumeration e = getBogeyManager().elements(); e.hasMoreElements(); ) {
			org.opensims.model.HostOrBogey bogey = (org.opensims.model.HostOrBogey) e.nextElement();
			buf.append("\n bogey: " + bogey.getSummary());
		}
	
		buf.append("\n ");
	
		for (java.util.Enumeration e = getAlertManager().elements(); e.hasMoreElements(); ) {
			org.opensims.alert.Alert alert = (org.opensims.alert.Alert) e.nextElement();
			buf.append("\n alert: " + alert.getSummary());
		}
		
		return buf.toString();
    }


    //////////////////////////////////////////////////////////////////////
    // configuration management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the containing servlet.
     */

    public org.opensims.servlet.Servlet
	getServlet ()
    {
		return servlet;
    }


    /**
     * Set the containing servlet.
     */

    public void
	setServlet (org.opensims.servlet.Servlet servlet)
    {
		this.servlet = servlet;
    }


    /**
     * Get the configuration document object
     */

    public org.opensims.Config
    getConfig ()
    {
		return config;
    }


    /**
     * Set the configuration document object
     */

    public void
	setConfig (org.opensims.Config config)
    {
		this.config = config;
    }


    /**
     * Get a configuration property
     */

    public String
	getConfig (String name)
    {
		/**
		 * @TODO rework this to decouple the containing servlet, as a
		 * more effective abstraction layer
		 */
		return getServlet().getConfig(name);
    }


    /**
     * Return the fully qualified domain name (FQDN).
     */

    public String
	getName ()
    {
		return correlator_node.getAttributeValue("name");
    }


    //////////////////////////////////////////////////////////////////////
    // manager access
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the AgentManager.
     */

    public org.opensims.agent.AgentManager
	getAgentManager ()
    {
		return agent_manager;
    }


    /**
     * Return the ScanManager.
     */

    public org.opensims.model.ScanManager
	getScanManager ()
    {
		return scan_manager;
    }


    /**
     * Return the BogeyManager.
     */

    public org.opensims.model.BogeyManager
	getBogeyManager ()
    {
		return bogey_manager;
    }


    /**
     * Return the AlertManager.
     */

    public org.opensims.alert.AlertManager
	getAlertManager ()
    {
		return alert_manager;
    }


    /**
     * Return the NotifyManager.
     */

    public org.opensims.notify.NotifyManager
	getNotifyManager ()
    {
		return notify_manager;
    }


    /**
     * Return the ReportManager.
     */

    public org.opensims.report.ReportManager
	getReportManager ()
    {
		return report_manager;
    }


    /**
     * Return the SubscriberManager.
     */

    public org.opensims.client.SubscriberManager
	getSubscriberManager ()
    {
		return subscriber_manager;
    }


    //////////////////////////////////////////////////////////////////////
    // handle agent messages
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse an incoming <BEAT/> node from an agent
     */

    public void
	handleAgentBeat (org.opensims.agent.Agent agent, org.jdom.Element node)
    {
	// <BEAT load="1.00439453125"/>

	/**
	 * @TODO bridge this 'load' into notification
	 */
    }


    /**
     * Parse an incoming <NOTICE/> node from an agent
     */

    public void
	handleAgentNotice (org.opensims.agent.Agent agent, org.jdom.Element node)
    {
		String text = node.getAttributeValue("text");
		String priority = node.getAttributeValue("priority");
	
		/**
		 * @TODO bridge this 'text' into notification
		 */
	
		log_.warn("NOTE: " + agent.getKey() + " priority " + priority + " - " + text);
    }


    /**
     * Send alerts through notification
     */

    public void
	handleAlert (org.opensims.alert.Alert alert)
    {
		getNotifyManager().consider(alert);
    }


    //////////////////////////////////////////////////////////////////////
    // network model management
    //////////////////////////////////////////////////////////////////////

    /**
     * XML parsing - using JDOM/SAX push parsing
     */

    public synchronized org.jdom.Document
	buildXml (java.io.BufferedReader buf_reader)
	throws org.jdom.JDOMException, Exception
    {
 		return xml_builder.build(buf_reader);
    }


    /**
     * Enable event correlation
     */

    public void
	setEngineEnable (boolean engine_enable)
    {
		if (log_.isInfoEnabled()) {
			log_.info("attempting to set engine_enable to " + engine_enable + " :: Check for all models loaded? " + getScanManager().areAllModelsLoaded() );
		}
		
		if ((engine_enable) && (getScanManager().areAllModelsLoaded()) && (!getAlertManager().isReloading())) {
			this.engine_enable = true;
		}
		else {
			this.engine_enable = false;
		}
	}


    /**
     * Get event correlation state
     */

    public boolean
	getEngineEnable ()
    {
		return(this.engine_enable);
	}


    /**
     * Insert an alert into the database
     * Note: caller is responsible for checking alert != null 
     */

	public org.opensims.db.om.Alert
	saveAlert( org.opensims.db.om.Alert alert )
	throws java.lang.Exception
	{
		alert.save();
		getAlertManager().put(alert.getKey(), alert);

// note (host).tick now set when alert is correlated, flushed by bogeyManager or ScanManabger
// 		long tick = alert.getTick();
// 
// 		// correct the ticks, to avoid errors during "age out" later
// 
// 		org.opensims.db.om.Host host = (org.opensims.db.om.Host) alert.getSrcHost();
// 
// 		if (host.getTick() < tick) {
// 			host.setTick(tick);
// 			host.save();
// 		}
// 
// 		host = (org.opensims.db.om.Host) alert.getSrcHost();
// 
// 		if (host.getTick() < tick) {
// 			host.setTick(tick);
// 			host.save();
// 		}

		// send the alert to notification
		handleAlert(alert);

		return alert;
	}

	/**
	 * process an incoming alert
	 */

    public org.opensims.alert.Alert
	correlateAlert (org.opensims.agent.Agent agent, String source, String unique_id, org.opensims.model.HostOrBogey src_host, String src_port, org.opensims.model.HostOrBogey dst_host, String dst_port, String protocol, long tick)
    {
		org.opensims.db.om.Alert alert = null;
		org.opensims.db.om.Alert last_alert = null;
		org.opensims.db.om.Alert saved_alert = null;

		try {
			if (engine_enable) {
				synchronized( alert_limbo_hash ) {
					String alert_key = org.opensims.db.om.Alert.getKey( unique_id, src_host, src_port, dst_host, dst_port, protocol );
					last_alert = (org.opensims.db.om.Alert) alert_limbo_hash.get(alert_key) ;
					if ( last_alert != null &&  ( getTick() - last_alert.firstTick() ) <= max_alert_ticks ) {
						// condense consecutive alerts
						if( log_.isDebugEnabled()) {
							log_.debug("DEBUG: condensing alert " + alert_key);
						}
						last_alert.registerIncident(tick);
					} else {
						alert = (org.opensims.db.om.Alert) getAlertManager().correlateAlert(agent, source, unique_id, src_host, src_port, dst_host, dst_port, protocol, tick);
						if ( alert != null ) {
							// flush out the last alert (which may or may not be condensed)
							if ( last_alert != null ) {
								saved_alert = saveAlert( last_alert );
								long incident_count = last_alert.getIncidentCount();
								if ( incident_count > 10 ) {
									log_.info( "correlateAlert: just condenced " + incident_count + " alerts =" + last_alert.getKey() );
								}
							}
							// put this alert into limbo
							alert_limbo_hash.put(alert_key, alert);
						}
					}
					 // these ticks will be saved on the next scan flush or bogey expiry
					src_host.setLastTick( tick );
					dst_host.setLastTick( tick );
				}
			}
		}
		catch (Exception e) {
			log_.error("correlate alert", e);
		}
	
		return (org.opensims.alert.Alert) saved_alert;
    }


    /**
     * Get a list of all scan objects available to the correlator
     */

    public java.util.ArrayList
	getScanList ()
    {
		java.util.ArrayList scan_list = new java.util.ArrayList();
	
		// add all of the scan objects to an array
		for (java.util.Enumeration e = getScanManager().elements(); e.hasMoreElements(); ) {
			org.opensims.model.Scan scan = (org.opensims.model.Scan) e.nextElement();
			scan_list.add(scan);
		}
	
		return scan_list;
    }

    /**
     * Get a list of all scan objects available to the correlator
     */

    public org.opensims.model.Scan
	getDefaultScan ()
    {
		java.util.ArrayList scan_list = new java.util.ArrayList();
		org.opensims.model.Scan scan = null;
		boolean found = false;
		
		// get the first one and call it default
		for (java.util.Enumeration e = getScanManager().elements(); (e.hasMoreElements() && !found); ) {
			scan = (org.opensims.model.Scan) e.nextElement();
			if (scan != null) {
				found = true;
			}
		}
		return scan;
    }


    /**
     * Use the activity window, etc., to filter the alert/notify streams
     * @TODO sort top alerts by risk, prior to applying throttle
     */

    public java.util.ArrayList
	getAlertList (org.opensims.stream.Stream stream, long tick, int alert_throttle)
    {
		int alert_count = 0;
		java.util.ArrayList alert_list = new java.util.ArrayList();
		long min_tick = tick - stream.getActivityWindow();
	
		for (java.util.Enumeration e = getAlertManager().elements(); e.hasMoreElements(); ) {
			org.opensims.alert.Alert alert = (org.opensims.alert.Alert) e.nextElement();
	
			// apply the activity window to filter by tick
			if ((alert.getLastTick() >= min_tick) && (alert_count < alert_throttle)) {
				alert_list.add(alert);
				alert_count++;
			}
		}
	
		return alert_list;
    }


    /**
     * Render NET/ATK/DEF/ALT/CON as XML, to build a content document
     */
     
    public org.jdom.Document
	buildContentDocument (long tick, java.util.ArrayList scan_list, java.util.ArrayList alert_list)
    {
    	return (buildContentDocument (tick, scan_list, alert_list, "admin_group"));
	}
 

    public org.jdom.Document
	buildContentDocument (long tick, java.util.ArrayList scan_list, java.util.ArrayList alert_list, String group)
    {
		org.jdom.Document content_doc = null;
	
		try {
	
			// using the active alerts, build the list for ATK/DEF/CON
			java.util.Hashtable host_table = new java.util.Hashtable();
			java.util.Hashtable conn_table = new java.util.Hashtable();
			java.util.Hashtable warn_table = new java.util.Hashtable();
	
			for (int i = 0; i < alert_list.size(); i++) {
				org.opensims.alert.Alert alert = (org.opensims.alert.Alert) alert_list.get(i);
				org.opensims.model.HostOrBogey src_host = alert.getSrcHost();	
				org.opensims.model.HostOrBogey dst_host = alert.getDstHost();
		
				// Filter out hosts that don't meet our group
				if ((src_host.isInGroup(group) && (!src_host.isBogey())) || 
					(dst_host.isInGroup(group) && (!dst_host.isBogey()))) {
					
					String conn_key = src_host.getHostId() + ":" + dst_host.getHostId();
					host_table.put(src_host.getHostId(), src_host);
					host_table.put(dst_host.getHostId(), dst_host);
					conn_table.put(conn_key, alert);
			
					if (!src_host.isBogey()) {
						warn_table.put(src_host.getHostId(), src_host);
					}
				}
				// Remove the alert as well
				else {
					alert_list.remove(i--);
				}
				if (log_.isDebugEnabled()) {
					log_.debug("Alert and host filtering by <" + group + "> alert_list size: " + alert_list.size() + " src: " + (src_host.getIPv4()).toString() + " " + src_host.isInGroup(group) + " dest: " + (dst_host.getIPv4()).toString() + " " + dst_host.isInGroup(group));
				}
			}
	
			// build the content document
			org.jdom.Element model_node = new org.jdom.Element(org.opensims.xml.Node.MODEL_NODE);
	
			content_doc = new org.jdom.Document(model_node);
	
			model_node.setAttribute("tick", String.valueOf(tick));
			model_node.setAttribute("tick_start", String.valueOf(tick));
			model_node.setAttribute("tick_cursor", String.valueOf(tick));
			model_node.setAttribute("revision", CVS_REVISION);
	
			// NET
			for (int i = 0; i < scan_list.size(); i++) {
				org.opensims.model.Scan scan = (org.opensims.model.Scan) scan_list.get(i);
				model_node.addContent(scan.getExportNode());
		
				/**
				 * @TODO need to use a "selected_network" here, to select only one
				 */
			}
	
			// ATK/DEF
			for (java.util.Enumeration e = host_table.elements(); e.hasMoreElements(); ) {
				org.opensims.model.HostOrBogey host = (org.opensims.model.HostOrBogey) e.nextElement();
				org.jdom.Element export_node = host.getExportNode();
		
				// insider threats get warning flag
				if (warn_table.get(host.getHostId()) != null) {
					export_node.setAttribute("warn", "1");
				}
		
				model_node.addContent(export_node);
			}
	
			// ALT
			if (log_.isDebugEnabled()) {
				log_.debug("sending " + alert_list.size() + " alerts");
			}
	
			for (int i = 0; i < alert_list.size(); i++) {
				org.opensims.alert.Alert alert = (org.opensims.alert.Alert) alert_list.get(i);
				model_node.addContent(alert.getExportNode());
		
				/**
				 * @TODO connection node should be reworked for VIZ stream schema
				 */
		
				model_node.addContent(alert.getConnectionExportNode());
			}
		}
		catch (Exception e) {
			log_.error("building model", e);
		}
	
		return content_doc;
    }
    

    //////////////////////////////////////////////////////////////////////
    // time flies when
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the current simulation time.
     */

    public long
	getTick ()
    {
		return System.currentTimeMillis();
    }


    /**
     * Format a timestamp into a tick used for internal time
     * representations
     */

    public long
	parseTick (String timestamp)
    {
		long tick = 0L;
	
		try {
			if ((timestamp != null) && !timestamp.equals("NaN")) {
				tick = Long.parseLong(timestamp);
			}
		}
		catch (Exception e) {
				log_.error("parse timestamp " + timestamp, e);
		}
	
		return tick;
    }


    /**
     * Format a tick from the internal representation into SQL
     * date/time format
     */

    public String
	formatTick (long tick)
    {
    	org.opensims.model.Scan scan = getDefaultScan();
    	String localtime = "GMT";
    	
    	if (scan != null) {
    		localtime = scan.getAgent().getLocaltime();
    	}
    	format_sql_date.setTimeZone(java.util.TimeZone.getTimeZone(localtime));
		return format_sql_date.format(new java.util.Date(tick));
    }
    
    
    /**
     * Format a tick from the internal representation into a human readable
     * date/time format
     */

    public String
	formatReadableTick (long tick)
    {
    	org.opensims.model.Scan scan = getDefaultScan();
    	String localtime = "GMT";
    	
    	if (scan != null) {
    		localtime = scan.getAgent().getLocaltime();
    	}
    	format_sql_date.setTimeZone(java.util.TimeZone.getTimeZone(localtime));
		return format_readable_date.format(new java.util.Date(tick));
    }

    /**
     * Format a difference between 2 ticks as human readable
     */

    public String
	formatTickDifference (long firstTick, long secondTick)    
    {					
		// Calculate the age between the two ticks and make human-readable 
		long difference = 0L;
		String diffString = "";
		
		if (firstTick > secondTick)
			difference = firstTick - secondTick;
		else
			difference = secondTick - firstTick;
		
		difference /= 1000L;						// seconds
		if (difference < 60L)
			diffString = String.valueOf(difference) + "s";
		else {
			difference /= 60L;						// minutes
			if (difference < 60L)
				diffString = String.valueOf(difference) + "m";
			else {
				difference /= 60L;					// hours
				if (difference < 24L)
					diffString = String.valueOf(difference) + "h";
				else {
					difference /= 24L;				// days
					if (difference < 7L)
						diffString = String.valueOf(difference) + "d";
					else {
						difference /= 7L;			// weeks
						if (difference < 52L)
							diffString = String.valueOf(difference) + "w";
						else {				
							difference /= 52L;		// years
							diffString = String.valueOf(difference) + "y";
						}
					}
				}
			}
		}
		return diffString;
	}


    //////////////////////////////////////////////////////////////////////
    // repository access
    //////////////////////////////////////////////////////////////////////

    /**
     * Get a URL to authenticate and connect with the shared
     * repository.
     */

    public org.opensims.crypto.RepositoryUrl
	getRepositoryUrl (String method, String uri_path, java.util.Hashtable param_table, StringBuffer content)
    {
		org.opensims.crypto.RepositoryUrl url = null;
		try {
			url = new org.opensims.crypto.RepositoryUrl(getConfig(), method, uri_path, param_table, content);
		}
		catch (Exception e) {
			log_.error("WARN: cannot reach repository", e);
		}
		return url;
    }


    //////////////////////////////////////////////////////////////////////
    // update management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Updater.
     */

    public org.opensims.Updater
	getUpdater ()
    {
		return updater;
    }


    /**
     * Set the Updater.
     */

    public void
	setUpdater (org.opensims.Updater updater)
    {
		this.updater = updater;
    }


    //////////////////////////////////////////////////////////////////////
    // Task management
    //////////////////////////////////////////////////////////////////////

    /**
     * Start the threaded components
     */

    public void
	start ()
    {
		init_thread = new Thread(this, getClass().getName() + " " + this.hashCode());
		init_thread.start();
	
		if (log_.isInfoEnabled()) {
			log_.info("started for " + getServlet().getServletName());
		}
    }


    /**
     * Stop the threaded components
     */

    public void
	stop ()
    {
		for (java.util.Enumeration e = getSubscriberManager().getListeners(); e.hasMoreElements(); ) {
			org.opensims.client.Listener listener = (org.opensims.client.Listener) e.nextElement();	
			listener.stop();
			listener = null;
		}
	
		if (log_.isInfoEnabled()) {
			log_.info("stopped for " + getServlet().getServletName());
		}
	
		for (java.util.Iterator iter = timerList.iterator(); iter.hasNext();)
		{
			java.util.Timer	oneTimer = (java.util.Timer)iter.next();
			oneTimer.cancel();
		}
		timerList.clear();
		
		init_thread = null;
    }


    /**
     * Task to cycle through Manager classes, checking whether to
     * remove the expired Tickable objects - for Alert, Bogey,
     * Subscriber, Agent, etc.
     */

    public void
	taskCheckExpiry ()
    {		
		//!! Definitely should be worked into things ! -ME
		/**
		 * @TODO if an Agent gets removed from its AgentManager, should its Scan stop() and be removed from ScanManager?
		 *
		 synchronized (getScanManager()) {
		  getScanManager().removeExpired(getTick());
		 }
		 */
		
		synchronized(getScanManager()) {
			getScanManager().flushHosts(getTick());
		}
	
		synchronized (getBogeyManager()) {
			getBogeyManager().removeExpired(getTick());
		}
	
		synchronized (getAlertManager()) {
			getAlertManager().removeExpired(getTick());
		}
	
		synchronized (getSubscriberManager()) {
			getSubscriberManager().removeExpired(getTick());
		}
    }


    /**
     * Task to check with the repository for updates.
     * @TODO also push error messages through notification
     */

    public void
	taskCheckUpdates ()
    {	 
		if (getUpdater().testSetLock()) {
			java.util.Hashtable param_table = new java.util.Hashtable();
			param_table.put("tick", String.valueOf(getUpdater().getLastTick()));
			org.opensims.crypto.RepositoryUrl url = this.getRepositoryUrl("GET", "update", param_table, null);
	
			if (url != null) {
				int response_code = url.getResponseCode();
	
				if (response_code == java.net.HttpURLConnection.HTTP_OK) {
					org.jdom.Element updates_node = null;
	
					// parse the list of available updates
	
					try {
						updates_node = buildXml(url.getContentReader()).getRootElement();
					}
					catch (Exception e) {
						log_.error("parse updates list", e);
					}
					finally {
						url.disconnect();
					}
		
					// evaluate the updates and schedule downloads
					getUpdater().addUpdates(updates_node);
				}
				else if (response_code > java.net.HttpURLConnection.HTTP_NO_CONTENT) {
					String error_message = "updates: error code " + response_code + ", " + url.getResponseMessage();
					url.disconnect();
					log_.warn(error_message);
				}
		
				// fire off another download session, to be sure
				getUpdater().start();
			}
		}
    }


    /**
     * Task to ping the logger - implementing a sanity check for the
     * webapp.
     */

    public void
	taskPingLogger ()
    {
		if (log_.isInfoEnabled()) {
			log_.info("ping " + formatTick(getTick()));
		}
    }


    /**
     * Return an Iterator of the bogeys which are ready to age out
     * of the database.  This method is intended to be superceded.
     */

    protected java.util.Iterator
	enumAgingBogeys (long age_limit, String error_message)
	throws Exception
    {
		org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
	
		crit.add(org.opensims.db.om.HostPeer.TICK, age_limit, org.apache.torque.util.Criteria.LESS_THAN);
		crit.add(org.opensims.db.om.HostPeer.NETWORK, (Object) "network IS NULL", org.apache.torque.util.Criteria.CUSTOM);
		crit.add(org.opensims.db.om.HostPeer.LINGER, 0);
	
		if (log_.isDebugEnabled()) {
			log_.debug(error_message + " - " + crit.toString());
		}
	
		return org.opensims.db.om.HostPeer.doSelect(crit).iterator();
    }


    /**
     * Task to delete and vacuum the database.  Note that the
     * sequencing is important, because of REFERENCE dependencies
     * between tables in the database; if violated it will cause
     * "foreign key constraint" errors in the log.
     *
     * @TODO Reduce thread priority; also, can we reduce the connection priority? Actually, let's spin off another thread.
     */

    public void
	taskDataVacuum ()
    {
		org.apache.torque.util.Criteria crit = null;
		long age_limit = System.currentTimeMillis() - Long.valueOf(getConfig("data.age_limit")).longValue();
		String error_message = null;
		String	currentScriptCommand = null;
	
		try
		{
			String[] command = new String[4];
			
			command[0] = "/usr/bin/sudo";
			command[1] = "/usr/local/opensims/tools/update/purge.sh";
			command[2] = "alert";
			command[3] = age_limit + "";
			
			org.opensims.SystemScript scriptObj = new org.opensims.SystemScript(command);
			currentScriptCommand = scriptObj.toString();
			
			if (log_.isDebugEnabled())
				log_.debug("executing script: " + currentScriptCommand);
			
			scriptObj.run();
		}
		catch (Exception e)
		{
			log_.error("While executing script '" + currentScriptCommand + "' to age alerts",e);
		}

		// don't age the hosts, since it has consistency issues with the foreign key contraints --ME 
		/*	
		try
		{
			String[] command = new String[4];
			
			command[0] = "/usr/bin/sudo";
			command[1] = "/usr/local/isims/remedy/purge.sh";
			command[2] = "host";
			command[3] = age_limit + "";
			
			org.opensims.SystemScript scriptObj = new org.opensims.SystemScript(command);
			currentScriptCommand = scriptObj.toString();
			
			if (log_.isDebugEnabled())
				log_.debug("executing script: " + currentScriptCommand);
			
			scriptObj.run();
		}
		catch (Exception e)
		{
			log_.error("While executing script '" + currentScriptCommand + "' to age bogeys",e);
		}
		*/	
		
		/*
		try {
			crit = new org.apache.torque.util.Criteria();
			crit.add(org.opensims.db.om.AlertPeer.TICK, age_limit, org.apache.torque.util.Criteria.LESS_THAN);
	
			error_message = "age out - " + getTick() + " : " + age_limit + " - " + crit.toString();
	
			if (log_.isDebugEnabled()) {
			log_.debug(error_message + " - " + crit.toString());
			}
	
			org.opensims.db.om.AlertPeer.doDelete(crit);
		}
		catch (Exception e) {
			log_.warn("data prune: " + error_message + " - " + e.getMessage());
		}
	
		// age out the bogeys - handled specially, since we've had so
		// many troubles with foreign key constraints
	
		try {
			crit = new org.apache.torque.util.Criteria();
			crit.add(org.opensims.db.om.HostPeer.TICK, age_limit, org.apache.torque.util.Criteria.LESS_THAN);
			crit.add(org.opensims.db.om.HostPeer.NETWORK, (Object) "network IS NULL", org.apache.torque.util.Criteria.CUSTOM);
			crit.add(org.opensims.db.om.HostPeer.LINGER, 0);
	
			error_message = "age out - " + getTick() + " : " + age_limit;
	
			if (log_.isDebugEnabled()) {
			log_.debug(error_message + " - " + crit.toString());
			}
	
			org.opensims.db.om.HostPeer.doDelete(crit);
		}
		catch (Exception e) {
			log_.warn("data prune: " + error_message + " - " + e.getMessage());
		}
		*/
		// -- DSC
    }


	protected void
	runAlertLimbo( boolean force )
	{
		synchronized( alert_limbo_hash ) {
			long now = getTick();
			for (java.util.Enumeration e = alert_limbo_hash.keys(); e.hasMoreElements(); ) {
				String alert_key = (String) e.nextElement();
				org.opensims.db.om.Alert alert = (org.opensims.db.om.Alert) alert_limbo_hash.get(alert_key);
				if ( force || ( now - alert.firstTick() ) > max_alert_ticks ) {
					try {
						saveAlert( alert );
					}
					catch (Exception ex) {
						log_.error("Failed to save alert =" + alert_key, ex);
					}
					alert_limbo_hash.remove(alert_key);
				}
			}
		}
	}
	/**
	 * auto-flush old Alerts held in limbo
 	 */
 	 
	public void
	taskFlushAlertLimbo()
	{
		runAlertLimbo( false );
	}

	 	 
	/**
	 * flush all alerts and wipe alert_limbo_hash clean
	 */
	public void
	taskExpungeAlertLimbo()
	{
		runAlertLimbo( true );
	}

    /**
     * Run the correlation engine, initiating timer tasks and starting
     * the listeners.
     */

    public void 
	run ()
    {
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		// Change the system-wide networking parameters for timeouts
		// The numeric arguments are in milliseconds
		System.setProperty("sun.net.client.defaultConnectTimeout","120000");
		System.setProperty("sun.net.client.defaultReadTimeout","120000");

        // parse the tasks from webapp.xml config

        java.util.Iterator task_iter = correlator_node.getChildren(org.opensims.xml.Node.TASK_NODE).iterator();

        while (task_iter.hasNext()) {
			org.jdom.Element task_node = (org.jdom.Element) task_iter.next();
			boolean enabled = Boolean.valueOf(org.opensims.Config.fixNull(task_node.getAttributeValue("enabled"), "false")).booleanValue();
	
			if (enabled)
			{
				java.util.Timer	newTimer = new java.util.Timer(true);				
				timerList.add(newTimer);
				org.opensims.Task task = new org.opensims.Task(this, newTimer, task_node);
			}
		}
	
		// ready to start the listeners
		for (java.util.Enumeration e = getSubscriberManager().getListeners(); e.hasMoreElements(); ) 
		{
			org.opensims.client.Listener listener = (org.opensims.client.Listener) e.nextElement();	
			listener.start();
		}
    }
}
