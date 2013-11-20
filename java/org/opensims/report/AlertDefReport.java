/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: AlertDefReport.java,v 1.19 2007/04/22 06:06:13 jeff Exp $
 * $Id: AlertDefReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.19 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    AlertDefReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.AlertDefReport.class.getName());


    //////////////////////////////////////////////////////////////////////
    // content formatting
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the XML content node for formatting.
     */


/*
		BUG:: When user updates using the submit the cache quashes the refresh
*/



    public org.jdom.Element
    getContentNode (org.opensims.report.Navigate navigate)
	throws org.opensims.report.ReportException
	{
		org.jdom.Element content_node = new org.jdom.Element(org.opensims.xml.Node.REPORT_NODE);
		org.opensims.db.om.AlertDef alert_def = null;
		org.opensims.model.HostOrBogey host = null;
	
		// restriction based on IP, if provided
		String ip_addr_str = navigate.getRawParam("ip");
		
		if (ip_addr_str != null) {
			host = getManager().getCorrelator().getScanManager().lookupHostByIpAddr(ip_addr_str, false);
	
			if (log_.isDebugEnabled()) {
				log_.debug("ip_addr_str |" + ip_addr_str + "| maps to " + host);
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
		if (alert_def != null) {		
			String isSubmitted = navigate.getRawParam("update_alert");
			String enabled_checkbox = null;

			// If we don't have administrative perms, don't allow updates
			if (!navigate.isInRoleAdmin()) 
				isSubmitted = null;
				
			//! Force no updates until we can resolve the alert Def Doc save issue -ME
			isSubmitted = null;
				
			if (isSubmitted != null) {
				enabled_checkbox = navigate.getRawParam("alert_enabled");
				String new_alert_type = navigate.getRawParam("new_alert_type");
			
				// Handle the enabled check box
				if ("on".equals(enabled_checkbox)) {
					if (! alert_def.isEnabled()) {								
						alert_def.enable_rule();						
						getManager().getCorrelator().getAlertManager().saveAlertDefsFile();
					}
				}
				else {
					if (alert_def.isEnabled()) {
						alert_def.disable_rule();
						getManager().getCorrelator().getAlertManager().saveAlertDefsFile();
					}
				}
				
				// Handle the alert type selector
				String old_alert_type = alert_def.getNode().getAttributeValue("type");
						
				if (! new_alert_type.equals(old_alert_type)) {
					org.opensims.alert.AlertType new_alert_type_obj = (org.opensims.alert.AlertType) getManager().getCorrelator().getAlertManager().lookupAlertTypeByName(new_alert_type);
					alert_def.set_type( new_alert_type_obj );
					alert_def.getNode().setAttribute("type", new_alert_type);					
					getManager().getCorrelator().getAlertManager().saveAlertDefsFile();
				}
			}
			
			org.jdom.Element report_node = (org.jdom.Element) alert_def.getReportNode();
			content_node.addContent(report_node);
			navigate.propagateParamValue("unique_id", unique_id);
	
			try {
				// Disable the navigation output -- uncomment next line after we convert to data grid
				// navigate.setEnabled(false);

				boolean with_archive = navigate.requireAlertArchive();
				
				// query to find the count		
				String sql = "";
				StringBuffer countQuery = new StringBuffer();
				
				countQuery.append("SELECT SUM(incident_count) AS alert_sum FROM alert ");
				countQuery.append("JOIN host_org_link on ");
				countQuery.append("( alert.dst_host_id = host_org_link.host_id OR alert.src_host_id = host_org_link.host_id ) ");
				countQuery.append("WHERE host_org_link.organization = '");
				countQuery.append(navigate.getOrganization());
				countQuery.append("' AND " + navigate.getAlertIntervalSql("alert") );
				
				// if we were given a host to restrict the output by (and it exists)
				if (host != null)
					countQuery.append(" AND alert.dst_host_id = '" + host.getHostId() + "'");
						
				if ( with_archive ) {
					sql = "select sum(alert_sum) as alert_sum from ("
						+ countQuery.toString()
						+ " UNION "
						+ countQuery.toString().replaceAll( "FROM alert", "FROM alert_archive alert" )
						+ " ) as alert" ;
				} else {
					sql = countQuery.toString();
				}
		
				if (log_.isDebugEnabled()) {
					log_.debug(sql);
				}
			
				java.util.Iterator result_iter = org.opensims.db.om.AlertPeer.executeQuery(sql).iterator();
		
				// calculate the total count of alerts in this period
				if (result_iter.hasNext()) {
					com.workingdogs.village.Record record = (com.workingdogs.village.Record) result_iter.next();
					String alert_sum = record.getValue("alert_sum").asString();
					content_node.setAttribute("total", alert_sum);
				}
		
				// add the attackers list
				StringBuffer buf = new StringBuffer();
		
				buf.append("SELECT host.ip_addr AS ip_addr, ");
				buf.append("SUM(alert.rm*alert.incident_count) AS order_risk, SUM(alert.incident_count) AS order_count, MAX(alert.tick) AS order_tick ");
				buf.append("FROM alert JOIN host_org_link on ");
				buf.append("( alert.dst_host_id = host_org_link.host_id OR alert.src_host_id = host_org_link.host_id ), ");
				buf.append("host ");
				buf.append("WHERE host_org_link.organization = '");
				buf.append(navigate.getOrganization());
				buf.append("' AND host.id = alert.src_host_id AND alert.alert_def_id = '" + alert_def.getId() + "' ");
				buf.append("AND " + navigate.getAlertIntervalSql("alert") );
				
				// if we were given a host to restrict the output by (and it exists)
				if (host != null)
					buf.append(" AND alert.dst_host_id = '" + host.getHostId() + "'");
				
				buf.append("GROUP BY host.ip_addr ");

				if ( with_archive ) {
					sql = "SELECT ip_addr, SUM(order_risk) AS order_risk, SUM(order_count) AS order_count, MAX(order_tick) AS order_tick FROM ( "
						+ buf.toString()
						+ " UNION "
						+ buf.toString().replaceAll( "FROM alert", "FROM alert_archive alert" )
						+ " ) AS alert GROUP BY ip_addr ";
				} else {
					sql = buf.toString();
				}
				sql = sql + navigate.lookupParamSql("order_by")
					+ navigate.lookupParamSql("order_dir")
					+ navigate.lookupParamSql("result_limit") ;
		
				if (log_.isDebugEnabled()) {
					log_.debug(sql);
				}
		
				result_iter = org.opensims.db.om.HostPeer.executeQuery(sql).iterator();
		
				while (result_iter.hasNext()) {
					com.workingdogs.village.Record record = (com.workingdogs.village.Record) result_iter.next();
		
					String ip_addr_str_rec = record.getValue("ip_addr").asString();
					double order_risk = record.getValue("order_risk").asDouble();
					String order_count = record.getValue("order_count").asString();
					long order_tick = record.getValue("order_tick").asLong();
					
					org.jdom.Element export_node = null;
					org.opensims.model.HostOrBogey bogey = null;
					org.opensims.model.HostOrBogey local_host = getManager().getCorrelator().getScanManager().getKnownHost(ip_addr_str_rec, false);
					
					// First off, is it a defeneder host?
					if (local_host != null) {
						export_node = local_host.getExportNode();
					}
					else {
						bogey = (org.opensims.model.HostOrBogey) getManager().getCorrelator().getBogeyManager().lookupBogeyByIpAddr(ip_addr_str_rec, false);
			
						if (bogey != null) {
							export_node = bogey.getExportNode();
						}
					}
					
					if (export_node != null) {
						export_node.setAttribute("risk", org.opensims.db.om.Alert.formatRisk2(order_risk));
						export_node.setAttribute("count", order_count);
						export_node.setAttribute("recent", getManager().getCorrelator().formatTick(order_tick));			
						content_node.addContent(export_node);
					}
					if (log_.isDebugEnabled()) {
						log_.debug("report host node added: " + org.opensims.xml.XmlBuilder.formatXML(export_node, false, true));
					}
							
				}
			}
			catch (Exception e) {
				log_.error("select alert def targets", e);
				throw new org.opensims.report.ReportException(e.getMessage());
			}
		}
		else {
			throw new org.opensims.report.ReportException("undefined alert");
		}
	
		return content_node;
	}
	
	public String
	getSignature(org.opensims.report.Navigate navigate)
	{
		String unique_id = navigate.getRawParam("unique_id");
		navigate.propagateParamValue("unique_id", unique_id);

		String ip_addr_str = navigate.getRawParam("ip");
		navigate.propagateParamValue("ip", ip_addr_str);

		String alert_def_id = navigate.getRawParam("alert_def_id");
		navigate.propagateParamValue("alert_def_id", alert_def_id);

		return super.getSignature(navigate) + ":" + unique_id + ":" + ip_addr_str + ":" + alert_def_id ;
	}

}
