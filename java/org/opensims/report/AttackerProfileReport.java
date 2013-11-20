/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: AttackerProfileReport.java,v 1.24 2007/05/29 01:56:20 brett Exp $
 * $Id: AttackerProfileReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.24 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 * @author Jeff PIHL <jeff@outer.net>
 */

public class
    AttackerProfileReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.AttackerProfileReport.class.getName());


    //////////////////////////////////////////////////////////////////////
    // content formatting
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the XML content node for formatting.
     */

    public org.jdom.Element
    getContentNode (org.opensims.report.Navigate navigate)
	throws org.opensims.report.ReportException
	{
		org.jdom.Element content_node = new org.jdom.Element(org.opensims.xml.Node.REPORT_NODE);
		String ip_addr_str = getIpAddress(navigate);
		org.opensims.db.om.Host bogey = null;	
		org.opensims.model.HostOrBogey bogey_host = null;
		org.opensims.db.om.AlertDef alert_def = null;
	
		// primary lookup, by ip - used by Flash GUI	
		if (ip_addr_str != null) {		
			bogey = (org.opensims.db.om.Host) getManager().getCorrelator().getBogeyManager().lookupBogeyByIpAddr(ip_addr_str, false);

			if (log_.isDebugEnabled()) {
				log_.debug("ip_addr_str |" + ip_addr_str + "| maps to " + bogey);
			}
			
			// Maybe it's not a bogey, but an internal host attacking
			if (bogey == null) {
				bogey_host = getManager().getCorrelator().getScanManager().lookupHostByIpAddr(ip_addr_str, false);
			}
		}

		// primary lookup, by name - more readable
		String unique_id = navigate.getRawParam("unique_id");
		if (unique_id != null) {
			alert_def = (org.opensims.db.om.AlertDef) getManager().getCorrelator().getAlertManager().lookupAlertDefByUniqueId(unique_id);
		}
	
		// alternative lookup, by id - used by Flash GUI
		String alert_def_id = navigate.getRawParam("alert_def_id");
		if (alert_def_id != null) {
			alert_def = (org.opensims.db.om.AlertDef) getManager().getCorrelator().getAlertManager().lookupAlertDefById(alert_def_id);
		}
	
		// generate the report
		org.jdom.Element report_node = null;
		
		if (! ((bogey == null) && (bogey_host == null)) ) {
			if (bogey != null)
				report_node = (org.jdom.Element) bogey.getReportNode().clone();
			else
				report_node = (org.jdom.Element) bogey_host.getExportNode().clone();
	
			content_node.addContent(report_node);
			navigate.propagateParamValue("ip", ip_addr_str);
			if ( alert_def != null ) {
				navigate.propagateParamValue("alert_def_id", Integer.toString(alert_def.getId()));
			}
	
			try {
				// Disable the navigation output -- uncomment next line after we convert to data grid
				// navigate.setEnabled(false);
				
				boolean with_archive = navigate.requireAlertArchive();
				
				// add the alerts list
				String sql = "";
				StringBuffer buf = new StringBuffer();
		
				buf.append("SELECT alert.id, ");
				buf.append("alert.rm AS order_risk, 1 AS order_count, alert.tick AS order_tick ");
				buf.append("FROM alert JOIN host_org_link on ");
				buf.append("( alert.dst_host_id = host_org_link.host_id OR alert.src_host_id = host_org_link.host_id ), ");
				buf.append("host ");
				buf.append("WHERE host_org_link.organization = '");
				buf.append(navigate.getOrganization());
				buf.append("' AND host.id = alert.src_host_id AND host.id = '" + bogey.getId() + "' ");
				if (alert_def != null)
					buf.append("AND alert.alert_def_id = '" + alert_def.getId() + "' ");
				buf.append("AND " + navigate.getAlertIntervalSql("alert") );
	
				if ( with_archive ) {
					sql = buf.toString()
						+ " UNION "
						+ buf.toString().replaceAll( "FROM alert", "FROM alert_archive alert" );
				} else {
					sql = buf.toString();
				}
				sql = sql + navigate.lookupParamSql("order_by")
					+ navigate.lookupParamSql("order_dir")
					+ navigate.lookupParamSql("result_limit") ;
		
				if (log_.isDebugEnabled()) {
					log_.debug(sql);
				}
		
				java.util.Iterator result_iter = org.opensims.db.om.AlertPeer.executeQuery(sql).iterator();
		
				while (result_iter.hasNext()) {
					com.workingdogs.village.Record record = (com.workingdogs.village.Record) result_iter.next();
		
					String alert_id = record.getValue("id").asString();
					double order_risk = record.getValue("order_risk").asDouble();
					long order_tick = record.getValue("order_tick").asLong();
		
					org.opensims.db.om.Alert alert = (org.opensims.db.om.Alert) getManager().getCorrelator().getAlertManager().lookupAlertById(alert_id, false);
		
					if (alert != null) {
					org.jdom.Element alert_node = alert.getExportNode();
		
						if (alert_node != null) {
							org.opensims.db.om.Host host = (org.opensims.db.om.Host) alert.getDstHost();
			
							if (host != null) {
								org.jdom.Element host_node = host.getExportNode();
			
								if (host_node != null) {
									alert_node.addContent(host_node);
								}
							}
			
							alert_node.setAttribute("risk", org.opensims.db.om.Alert.formatRisk0(order_risk));
							alert_node.setAttribute("recent", getManager().getCorrelator().formatTick(order_tick));
							report_node.addContent(alert_node);
						}
					}
				}
			}
			catch (Exception e) {
				log_.error("select attacker targets", e);
				throw new org.opensims.report.ReportException(e.getMessage());
			}
		}
		else {
			String error_message = "";
			// form uses the raw parameter, not the cannonized one
			ip_addr_str = navigate.getRawParam("ip");
			if ( ip_addr_str == null ) {
				ip_addr_str = "";
			} else if ( ip_addr_str.equals("") ) {
				error_message = "Please enter an IP address and try again.";
			} else {
				error_message = "This does not appear to be a valid IP address." ;
			}
			content_node = new org.jdom.Element(org.opensims.xml.Node.NOTICE_NODE);
			org.jdom.Element text_node = new org.jdom.Element(org.opensims.xml.Node.TEXT_NODE);
			text_node.setAttribute("name", "ip");
			text_node.setAttribute("value", ip_addr_str );
			text_node.setAttribute("prompt", "Ip Address: " );
			text_node.setAttribute("size", "16" );
			text_node.setAttribute("error", error_message );
			content_node.addContent(text_node);
			//throw new org.opensims.report.ReportException("attacker not found");
		}
		
		if (log_.isDebugEnabled()) {
			log_.debug(org.opensims.xml.XmlBuilder.formatXML(content_node, false, true));
		}
	
		return content_node;
	}

	public String
	getSignature(org.opensims.report.Navigate navigate)
	{
		String ip_addr_str = getIpAddress(navigate);

		if ( ip_addr_str != null ) {
			log_.debug( "attacker_profile ip=" + ip_addr_str );
			navigate.propagateParamValue("ip", ip_addr_str);
			StringBuffer sig = new StringBuffer( super.getSignature(navigate) );
			sig.append( ":" + ip_addr_str );
			sig.append( ":" + navigate.propagateParamValue("unique_id") );
			sig.append( ":" + navigate.propagateParamValue("alert_def_id") );
			return sig.toString();
		}
		log_.debug( "attacker_profile ip=null" );
		return null;
	}

	public String
	getIpAddress(org.opensims.report.Navigate navigate)
	{
		String ip_addr_str = navigate.getRawParam("ip");
		if ( ip_addr_str == null ) return null;
		ip_addr_str = ip_addr_str.trim();
		if ( ip_addr_str.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$") ) {
			return ip_addr_str;
		}
		return null;
	}
}
