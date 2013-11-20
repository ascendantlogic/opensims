/**
 * @LICENSE@
 */

package org.opensims.alert;

/**
 * $CVSId: AlertManager.java,v 1.47 2007/02/20 00:42:36 jeff Exp $
 * $Id: AlertManager.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.47 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    AlertManager
    extends org.opensims.Manager
{
    // protected fields

    protected org.jdom.Document alert_defs_doc = null;
    protected org.jdom.Document alert_filters_doc = null;
    protected java.util.Hashtable alert_type_table = new java.util.Hashtable();
    protected java.util.Hashtable alert_def_table = new java.util.Hashtable();

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.alert.AlertManager.class.getName());
	
	// private fields
	
	private boolean reloading = false;

    /**
     * Constructor.
     */

    public
	AlertManager (org.opensims.Correlator correlator)
	{
		super(correlator, org.opensims.alert.AlertManager.class.getName());
	
		// load the alert defs XML file
		try {
			java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
			java.io.File alert_defs_file = new java.io.File(var_lib, "alert_defs.xml");
	
			if (log_.isInfoEnabled()) {
				log_.info("alert defs file: " + alert_defs_file);
			}
	
			java.io.FileReader file_reader = new java.io.FileReader(alert_defs_file);
			java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);
	
			setAlertDefsDoc(getCorrelator().buildXml(buf_reader));
	
			/**
			 * this is really too much detail for any sane being
	
				if (log_.isDebugEnabled()) {
					String xml = org.opensims.xml.XmlBuilder.formatXML(getAlertDefsDoc().getRootElement(), false, true);
					log_.debug("alert defs " + xml);
				}
			 */
	
			buf_reader.close();
		}
		catch (Exception e) {
			log_.error("load alert defs", e);
		}
	
		// load the alert filters XML file (optional)
	
		try {
			java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
			java.io.File alert_filters_file = new java.io.File(var_lib, "alert_filters.xml");
	
			if (log_.isInfoEnabled()) {
				log_.info("alert filters file: " + alert_filters_file);
			}
	
			if (!alert_filters_file.canRead()) {
				setAlertFiltersDoc(new org.jdom.Document(new org.jdom.Element("FILTERS")));
			}
			else {
				java.io.FileReader file_reader = new java.io.FileReader(alert_filters_file);
				java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);
		
				setAlertFiltersDoc(getCorrelator().buildXml(buf_reader));
		
				/**
				 * this is really too much detail for any sane being
		
				if (log_.isDebugEnabled()) {
					String xml = org.opensims.xml.XmlBuilder.formatXML(getAlertFiltersDoc().getRootElement(), false, true);
					log_.debug("alert filters " + xml);
				}
				*/
				buf_reader.close();
			}
		}
		catch (Exception e) {
			log_.error("load alert filters", e);
		}
	
		// build the lookup tables
	
		try {
			buildAlertTypeTable();
			buildAlertDefTable();
			applyAlertFiltersToAlertDefs();
		}
		catch (Exception e) {
			log_.error("build tables", e);
		}
	}


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////


    /**
     * Get the Alert Defs XML document.
     */

    public org.jdom.Document
	getAlertDefsDoc ()
    {
		return alert_defs_doc;
    }


    /**
     * Set the Alert Defs XML document.
     */

    public void
	setAlertDefsDoc (org.jdom.Document alert_defs_doc)
    {
		this.alert_defs_doc = alert_defs_doc;
    }

    
    /**
     * Set the Alert Defs XML document.
     */

    public boolean
	isReloading ()
    {
		return(reloading);
    }

    
    /**
     * Save the Alert Defs XML document.
     */

    public void
	saveAlertDefsFile ()
    {
		try {
			java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
			java.io.File alert_defs_file = new java.io.File(var_lib, "alert_defs.xml");
			
			if (!alert_defs_file.canWrite()) {
				log_.warn("save alert defs file - no pre-existing (or bad file perms) " + alert_defs_file);
			}
	
			java.io.FileWriter file_writer = new java.io.FileWriter(alert_defs_file);
			java.io.BufferedWriter buf_writer = new java.io.BufferedWriter(file_writer);
			java.io.PrintWriter print_writer = new java.io.PrintWriter(buf_writer);

			String xml = org.opensims.xml.XmlBuilder.formatXML(getAlertDefsDoc().getRootElement(), true, true);	
	
			print_writer.write(xml);
			print_writer.flush();
			print_writer.close();
			
			// Update the external list of rules that should be disabled (/etc/snort/removed.sids)
			//
			// @TODO re-factor this into a hash table of removed unique_ids & iterate  (-ME)
			//
			
			java.io.File removed_sids_file = new java.io.File(var_lib, "removed.sids");		
			if (!removed_sids_file.canWrite()) {
				log_.warn("save removed sids file - no pre-existing (or bad file perms) " + removed_sids_file);
			}
	
			java.io.FileWriter sids_file_writer = new java.io.FileWriter(removed_sids_file);
			java.io.BufferedWriter sids_buf_writer = new java.io.BufferedWriter(sids_file_writer);
			java.io.PrintWriter sids_print_writer = new java.io.PrintWriter(sids_buf_writer);

			StringBuffer path = new StringBuffer();
			path.append("//").append(org.opensims.xml.Node.ALERT_DEF_NODE);
		
			org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
			java.util.Iterator def_iter = xpath.selectNodes(getAlertDefsDoc()).iterator();
		
			while (def_iter.hasNext()) {
				org.jdom.Element def_node = (org.jdom.Element) def_iter.next();
		
				String unique_id = def_node.getAttributeValue("unique_id");
				String def_enabled = "true";
				
				if (def_node.getAttributeValue("enabled") != null) {
					def_enabled = def_node.getAttributeValue("enabled");
				}

				if (def_enabled.equals("false")) {
					sids_print_writer.write(unique_id + "\n");
				}
			}
			sids_print_writer.flush();
			sids_print_writer.close();			
		
		}
		catch (Exception e) {
			log_.error("save alert defs file", e);
		}
    }    
    

    /**
     * Get the Alert Filters XML document.
     */

    public org.jdom.Document
	getAlertFiltersDoc ()
    {
		return alert_filters_doc;
    }


    /**
     * Set the Alert Filters XML document.
     */

    public void
	setAlertFiltersDoc (org.jdom.Document alert_filters_doc)
    {
		this.alert_filters_doc = alert_filters_doc;
    }


    /**
     * Save the Alert Filters XML document.
     */

    public void
	saveAlertFiltersFile ()
    {
		try {
			java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
			java.io.File alert_filters_file = new java.io.File(var_lib, "alert_filters.xml");
	
			if (!alert_filters_file.canWrite()) {
				log_.warn("save alert filters file - no pre-existing (or bad file perms) " + alert_filters_file);
			}
	
			java.io.FileWriter file_writer = new java.io.FileWriter(alert_filters_file);
			java.io.BufferedWriter buf_writer = new java.io.BufferedWriter(file_writer);
			java.io.PrintWriter print_writer = new java.io.PrintWriter(buf_writer);
	
			String xml = org.opensims.xml.XmlBuilder.formatXML(getAlertDefsDoc().getRootElement(), true, true);
	
			print_writer.write(xml);
			print_writer.flush();
			print_writer.close();
		}
		catch (Exception e) {
			log_.error("save alert filters file", e);
		}
    }    
 

    //////////////////////////////////////////////////////////////////////
    // manage the alert types
    //////////////////////////////////////////////////////////////////////

    /**
     * Build the AlertType table.
     */

    protected void
	buildAlertTypeTable ()
	throws Exception
	{
		StringBuffer path = new StringBuffer();
		path.append("//").append(org.opensims.xml.Node.ALERT_TYPE_NODE);
	
		org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
		java.util.Iterator type_iter = xpath.selectNodes(getAlertDefsDoc()).iterator();
	
		while (type_iter.hasNext()) {
			org.jdom.Element type_node = (org.jdom.Element) type_iter.next();
			String type_name = type_node.getAttributeValue("name");
	
			org.opensims.db.om.AlertType alert_type = null;
	
			// select or insert a new alert_type row ("sinsert")
	
			org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
			crit.add(org.opensims.db.om.AlertTypePeer.NAME, type_name);
	
			if (log_.isDebugEnabled()) {
				log_.debug("Torque - lookup alert_type: " + type_name);
			}
	
			// try to find an existing row in the "alert_type" table
	
			java.util.Iterator alert_type_iter = org.opensims.db.om.AlertTypePeer.doSelect(crit).iterator();
	
			while (alert_type_iter.hasNext()) {
				alert_type = (org.opensims.db.om.AlertType) alert_type_iter.next();
		
				if (log_.isDebugEnabled()) {
					log_.debug("Torque - found alert_type: " + alert_type.toString());
				}
			}
	
			// provision a new row
	
			if (alert_type == null) {
				alert_type = new org.opensims.db.om.AlertType();
		
				if (log_.isDebugEnabled()) {
					log_.debug("Torque - adding alert_type: " + type_name);
				}
		
				alert_type.setName(type_name);
				alert_type.setCreated(new java.util.Date());
		
				alert_type.save();
			}
	
			// non-persistent initialization
	
			type_node.setAttribute("id", alert_type.getTypeId());
			type_node.setAttribute("label", alert_type.getName());
	
			if (log_.isDebugEnabled()) {
				String xml = org.opensims.xml.XmlBuilder.formatXML(type_node, false, true);
				log_.debug("alert type " + xml);
			}
	
			alert_type.setNode(type_node);	
			alert_type_table.put(type_name, alert_type);
		}  
	}


    /**
     * Get an Enumeration of the alert types.
     */

    public java.util.Enumeration
	getAlertTypes ()
    {
		return alert_type_table.elements();
    }


    /**
     * Lookup an alert type, by name - faster, more readable.
     */

    public org.opensims.alert.AlertType
	lookupAlertTypeByName (String type_name)
    {
		return (org.opensims.alert.AlertType) alert_type_table.get(type_name);
    }


    /**
     * Lookup an alert type, by id - used by Flash GUI.
     */

    public org.opensims.alert.AlertType
	lookupAlertTypeById (String alert_type_id)
	{
		org.opensims.alert.AlertType alert_type = null;
	
		try {
			StringBuffer path = new StringBuffer();
	
			path.append("//").append(org.opensims.xml.Node.ALERT_TYPE_NODE);
			path.append("[@id = '");
			path.append(alert_type_id);
			path.append("']");
	
			org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
			org.jdom.Element type_node = (org.jdom.Element) xpath.selectSingleNode(getAlertDefsDoc());
	
			if (type_node != null) {
				String type_name = type_node.getAttributeValue("name");
				alert_type = lookupAlertTypeByName(type_name);
			}
		}
		catch (Exception e) {
			log_.error("alert type lookup by id |" + alert_type_id + "|", e);
		}
	
		return alert_type;
	}


    //////////////////////////////////////////////////////////////////////
    // manage the alert definitions
    //////////////////////////////////////////////////////////////////////

    /**
     * Build the AlertDef table.
     */

    protected void
	buildAlertDefTable ()
	throws Exception
	{
		StringBuffer path = new StringBuffer();
		path.append("//").append(org.opensims.xml.Node.ALERT_DEF_NODE);
	
		org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
		java.util.Iterator def_iter = xpath.selectNodes(getAlertDefsDoc()).iterator();
	
		while (def_iter.hasNext()) {
			org.jdom.Element def_node = (org.jdom.Element) def_iter.next();
	
			String unique_id = def_node.getAttributeValue("unique_id");
			String type_name = def_node.getAttributeValue("type");
	
			org.opensims.alert.AlertType alert_type = lookupAlertTypeByName(type_name);
			org.opensims.db.om.AlertDef alert_def = null;
	
			// select or insert a new alert_def row (i.e., a "sinsert")
			org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
			crit.add(org.opensims.db.om.AlertDefPeer.UNIQUE_ID, unique_id);
	
			if (log_.isDebugEnabled()) {
				log_.debug("Torque - lookup alert_def: " + unique_id);
			}
	
			// try to find an existing row in the "alert_def" table
			java.util.Iterator alert_def_iter = org.opensims.db.om.AlertDefPeer.doSelect(crit).iterator();
	
			while (alert_def_iter.hasNext()) {
				alert_def = (org.opensims.db.om.AlertDef) alert_def_iter.next();
		
				if (log_.isDebugEnabled()) {
					log_.debug("Torque - found alert_def: " + alert_def.toString());
				}
			}
	
			// provision a new row
			if (alert_def == null) {
				alert_def = new org.opensims.db.om.AlertDef();
		
				if (log_.isDebugEnabled()) {
					log_.debug("Torque - adding alert_def: " + unique_id);
				}
		
				alert_def.setUniqueId(unique_id);
				alert_def.setCreated(new java.util.Date());
				alert_def.setAlertType((org.opensims.db.om.AlertType) alert_type);
		
				alert_def.save();
			}
	
			// non-persistent initialization
			String alert_def_id = String.valueOf(alert_def.getId());
	
			def_node.setAttribute("id", alert_def_id);

			if (log_.isDebugEnabled()) {
				String xml = org.opensims.xml.XmlBuilder.formatXML(def_node, false, true);
				log_.debug("alert def " + xml);
			}
	
			alert_def.setNode(def_node);	
			alert_def_table.put(unique_id, alert_def);
		}  
	}


    /**
     * Apply alert filters to their alert definitions - DSC
     */
    protected void
    applyAlertFiltersToAlertDefs ()
	throws Exception
    {
    	StringBuffer path = new StringBuffer();
    	
		path.append("//").append(org.opensims.xml.Node.ALERT_DEF_NODE);
		org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
		java.util.Iterator iter = xpath.selectNodes(getAlertFiltersDoc()).iterator();
		
		while (iter.hasNext()) {
			org.jdom.Element filterNode = (org.jdom.Element)iter.next();
			String uniqueID = filterNode.getAttributeValue("unique_id");
			org.opensims.alert.AlertDef defNode = lookupAlertDefByUniqueId(uniqueID);
			
			if (defNode != null) {
				defNode.parseFilterRules(filterNode);
			}
		}
    }


    /**
     * Get an Enumeration of the alert defs.
     */

    public java.util.Enumeration
	getAlertDefs ()
    {
		return alert_def_table.elements();
    }


    /**
     * Lookup an alert def, by unique_id - faster, more readable.
     */

    public org.opensims.alert.AlertDef
	lookupAlertDefByUniqueId (String unique_id)
    {
		return (org.opensims.alert.AlertDef) alert_def_table.get(unique_id);
    }


    /**
     * Lookup an alert type, by id - not used, not preferrable.
     */

    public org.opensims.alert.AlertDef
	lookupAlertDefById (String alert_def_id)
	{
		org.opensims.alert.AlertDef alert_def = null;
	
		try {
			StringBuffer path = new StringBuffer();
	
			path.append("//").append(org.opensims.xml.Node.ALERT_DEF_NODE);
			path.append("[@id = '");
			path.append(alert_def_id);
			path.append("']");
	
			if (log_.isDebugEnabled()) {
				log_.debug("lookup alert def by id |" + alert_def_id + "| " + path.toString());
			}
	
			org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
			org.jdom.Element def_node = (org.jdom.Element) xpath.selectSingleNode(getAlertDefsDoc());
	
			if (def_node != null) {
				String unique_id = def_node.getAttributeValue("unique_id");
				alert_def = lookupAlertDefByUniqueId(unique_id);
			}
		}
		catch (Exception e) {
			log_.error("alert def lookup by id |" + alert_def_id + "|", e);
		}
	
		return alert_def;
	}


    /**
     * Lookup an alert filter, by unique id.
     */

    public org.jdom.Element
	lookupFilterByUniqueId (String unique_id)
	{
		org.jdom.Element alert_def_node = null;
	
		try {
			StringBuffer path = new StringBuffer("//");
	
			path.append(org.opensims.xml.Node.ALERT_DEF_NODE);
			path.append("[@unique_id = '");
			path.append(unique_id);
			path.append("']");
	
			if (log_.isDebugEnabled()) {
				log_.debug("lookup filter by unique id |" + unique_id + "| " + path.toString());
			}
	
			alert_def_node = (org.jdom.Element) org.jdom.xpath.XPath.selectSingleNode(getAlertFiltersDoc(), path.toString());
		}
		catch (Exception e) {
			log_.error("lookup filter by unique id |" + unique_id + "|", e);
		}
	
		return alert_def_node;
	}


    /**
     * Reload the alert definitions and the alert filters and reapply
     */

    public void
	reload ()
	{
		// First, ensure we aren't going to be accessing any of the internal data structures
		getCorrelator().setEngineEnable(false);
		reloading = true;
			
		// Dump both of the hash tables
		alert_type_table.clear();
		alert_def_table.clear();
		
		// Dump the original XML docs
		this.alert_filters_doc =  null;
		this.alert_defs_doc = null;

		// reread the alert defs XML file
		try {
			java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
			java.io.File alert_defs_file = new java.io.File(var_lib, "alert_defs.xml");
	
			if (log_.isInfoEnabled()) {
				log_.info("reload alert defs file: " + alert_defs_file);
			}
	
			java.io.FileReader file_reader = new java.io.FileReader(alert_defs_file);
			java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);
	
			org.opensims.xml.XmlBuilder def_xml_builder = new org.opensims.xml.XmlBuilder(false);
			setAlertDefsDoc(def_xml_builder.build(buf_reader));	
			buf_reader.close();			
		}
		catch (Exception e) {
			log_.error("reload alert defs", e);
		}
	
		// reread the alert filters XML file
		try {
			java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
			java.io.File alert_filters_file = new java.io.File(var_lib, "alert_filters.xml");
	
			if (log_.isInfoEnabled()) {
				log_.info("reload alert filters file: " + alert_filters_file);
			}
	
			if (!alert_filters_file.canRead()) {
				setAlertFiltersDoc(new org.jdom.Document(new org.jdom.Element("FILTERS")));
			}
			else {
				java.io.FileReader file_reader = new java.io.FileReader(alert_filters_file);
				java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);
		
				org.opensims.xml.XmlBuilder filter_xml_builder = new org.opensims.xml.XmlBuilder(false);
				setAlertFiltersDoc(filter_xml_builder.build(buf_reader));
				buf_reader.close();
			}
		}
		catch (Exception e) {
			log_.error("reload alert filters", e);
		}
	
		// rebuild the lookup tables
		try {
			buildAlertTypeTable();
			buildAlertDefTable();
			applyAlertFiltersToAlertDefs();
		}
		catch (Exception e) {
			log_.error("rebuild alert tables", e);
		}
	
		// Restate correlation flag
		reloading = false;
		getCorrelator().setEngineEnable(true);
	}


    //////////////////////////////////////////////////////////////////////
    // manage alerts
    //////////////////////////////////////////////////////////////////////

    /**
     * Lookup an alert from memory or the database.
     */

    public org.opensims.alert.Alert
	lookupAlertById (String alert_id, boolean update)
	{
		org.opensims.db.om.Alert alert = null;
	
		try {
			org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
	
			crit.add(org.opensims.db.om.AlertPeer.ID, alert_id);
	
			// try to find an existing row in the "alert" table
			java.util.Iterator alert_iter = org.opensims.db.om.AlertPeer.doSelect(crit).iterator();
	
			if (alert_iter.hasNext()) {
				alert = (org.opensims.db.om.Alert) alert_iter.next();
		
				if (log_.isDebugEnabled()) {
					log_.debug("populate alert " + alert.toString());
				}
		
				String alert_def_id = String.valueOf(alert.getAlertDefId());
				org.opensims.alert.AlertDef alert_def = lookupAlertDefById(alert_def_id);
		
				if (log_.isDebugEnabled()) {
					log_.debug("found alert def |" + alert_def_id + "| " + alert_def);
				}
		
				alert.setAlertDef((org.opensims.db.om.AlertDef) alert_def);
		
				String src_host_id = String.valueOf(alert.getSrcHostId());
				org.opensims.model.HostOrBogey src_host = getCorrelator().getScanManager().lookupHostById(src_host_id, update);
				alert.setHostRelatedBySrcHostId((org.opensims.db.om.Host) src_host);
		
				String dst_host_id = String.valueOf(alert.getDstHostId());
				org.opensims.model.HostOrBogey dst_host = getCorrelator().getScanManager().lookupHostById(dst_host_id, update);
				alert.setHostRelatedByDstHostId((org.opensims.db.om.Host) dst_host);
		
				alert.setManager(this);
			}
		}
		catch (Exception e) {
			log_.error("lookup alert by id |" + alert_id + "|", e);
		}
	
		return (org.opensims.alert.Alert) alert;
	}


    /**
     * Lookup an alert from the database.
     * Note: this is the only point where an Alert gets created/populated/saved.
     */

    public org.opensims.alert.Alert
	correlateAlert (org.opensims.agent.Agent agent, String source, String unique_id, org.opensims.model.HostOrBogey src_host, String src_port, org.opensims.model.HostOrBogey dst_host, String dst_port, String protocol, long tick)
	{
		org.opensims.db.om.Alert alert = null;
		
		if (reloading)
			return (alert);
	
		if (log_.isDebugEnabled()) {
			log_.debug("Start correlate alert: " + unique_id + " @ " + src_host.getRole() + " " + src_host.getHostId() + " - " + dst_host.getRole() + " " + dst_host.getHostId());
		}
	
		/**
		 * @TODO realistically, these rules need to be refactored into a UML state machine
		 */
	
		if (!dst_host.isBogey() || !src_host.isBogey()) {
			try {
			
				// provision a new row
				org.opensims.alert.AlertDef alert_def = lookupAlertDefByUniqueId(unique_id);
				
				if (log_.isDebugEnabled())
				{
					log_.debug("Attempting to correlate alert with unique ID: " + unique_id);
				}
		
				if (alert_def != null) 
				{
					if (alert_def.isEnabled())
					{
						if (log_.isDebugEnabled()) {
							org.opensims.db.om.AlertType alert_type = (org.opensims.db.om.AlertType) alert_def.getType();
							log_.debug("Torque - adding alert: " + unique_id + " - " + alert_type.getTypeId() + " " + alert_type.getName());
						}

						alert = new org.opensims.db.om.Alert();
			
						alert.setSource(source);
						alert.setAlertDef((org.opensims.db.om.AlertDef) alert_def);
						alert.setHostRelatedBySrcHostId((org.opensims.db.om.Host) src_host);
						alert.setSrcPort(src_port);
						alert.setHostRelatedByDstHostId((org.opensims.db.om.Host) dst_host);
						alert.setDstPort(dst_port);
						alert.setProtocol(protocol);
						alert.setCreated(new java.util.Date());
						alert.setTick(tick);

						// non-persistent initialization
						alert.setManager(this);
// moved to correlator at time of alert.save()						
//						put(alert.getKey(), alert);
					}
					else {
						// NOOP: rule is disabled, dump alert to the bit bucket and move on
					
						if (log_.isDebugEnabled()) {
							log_.debug("DEBUG: alert_id " + unique_id + " is DISABLED-- not recording alert.");
						}
					}
				}
				else {
					/**
					 * @TODO needs to go into notification as well
					 */
		
					log_.warn("alert def " + unique_id + " IS NOT DEFINED");
				}
			}
			catch (Exception e) {
				log_.error("using Torque to create a new row in the alert table", e);
			}
		}
	
		if (log_.isDebugEnabled()) {
			log_.debug("Finish correlate alert: " + unique_id + " @ " + src_host.getRole() + " " + src_host.getHostId() + " - " + dst_host.getRole() + " " + dst_host.getHostId());
		}
		return alert;
	}

}
