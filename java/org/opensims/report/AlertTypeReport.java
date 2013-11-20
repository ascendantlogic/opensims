/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: AlertTypeReport.java,v 1.18 2007/04/22 06:06:14 jeff Exp $
 * $Id: AlertTypeReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.18 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 @ @author Jeff PIHL <jeff@outer.net>
 */

public class
    AlertTypeReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.AlertTypeReport.class.getName());


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
		org.opensims.db.om.AlertType alert_type = null;
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
		String type_name = navigate.getRawParam("type_name");
		if (type_name != null) {
			alert_type = (org.opensims.db.om.AlertType) getManager().getCorrelator().getAlertManager().lookupAlertTypeByName(type_name);
		}
	
		// alternative lookup, by id - used by Flash GUI
		String alert_type_id = navigate.getRawParam("alert_type_id");
		if (alert_type_id != null) {
			alert_type = (org.opensims.db.om.AlertType) getManager().getCorrelator().getAlertManager().lookupAlertTypeById(alert_type_id);
		}
	
		// generate the report
		if (alert_type != null) {
			org.jdom.Element report_node = (org.jdom.Element) alert_type.getNode().clone();
	
			content_node.addContent(report_node);
			navigate.propagateParamValue("alert_type_id", String.valueOf(alert_type.getId()));
	
			try {
				// Disable the navigation output -- uncomment next line after we convert to data grid
				// navigate.setEnabled(false);

				long	timeLimitInSecs = navigate.getAlertIntervalAsSecs();
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
		
				// add the attacks list
				StringBuffer buf = new StringBuffer();
		
				buf.append("SELECT alert_def.unique_id AS unique_id, ");
				buf.append("SUM(alert.rm*alert.incident_count) AS order_risk, SUM(alert.incident_count) AS order_count, MAX(alert.tick) AS order_tick ");
				buf.append("FROM alert JOIN host_org_link on ");
				buf.append("( alert.dst_host_id = host_org_link.host_id OR alert.src_host_id = host_org_link.host_id ), ");
				buf.append("alert_def ");
				buf.append("WHERE host_org_link.organization = '");
				buf.append(navigate.getOrganization());
				buf.append("' AND alert.alert_def_id = alert_def.id AND alert_def.alert_type_id = " + alert_type.getId() + " ");
				buf.append("AND " + navigate.getAlertIntervalSql("alert") );
				
				// if we were given a host to restrict the output by (and it exists)
				if (host != null)
					buf.append(" AND alert.dst_host_id = '" + host.getHostId() + "'");
				buf.append("GROUP BY alert_def.unique_id ");
				
				if ( with_archive ) {
					sql = "SELECT unique_id, SUM(order_risk) AS order_risk, SUM(order_count) AS order_count, MAX(order_tick) AS order_tick FROM ( "
						+ buf.toString()
						+ " UNION "
						+ buf.toString().replaceAll( "FROM alert", "FROM alert_archive alert" )
						+ " ) AS alert GROUP BY unique_id ";
				} else {
					sql = buf.toString();
				}
				sql = sql + navigate.lookupParamSql("order_by")
					+ navigate.lookupParamSql("order_dir")
					+ navigate.lookupParamSql("result_limit") ;
		
				if (log_.isDebugEnabled()) {
					log_.debug(sql);
				}
		
				result_iter = org.opensims.db.om.AlertPeer.executeQuery(sql).iterator();
		
				while (result_iter.hasNext()) {
					com.workingdogs.village.Record record = (com.workingdogs.village.Record) result_iter.next();
		
					String unique_id = record.getValue("unique_id").asString();
					double order_risk = record.getValue("order_risk").asDouble();
					String order_count = record.getValue("order_count").asString();
					long order_tick = record.getValue("order_tick").asLong();
						
					long order_count_long = record.getValue("order_count").asLong();
					double base_risk = order_risk / order_count_long;
					double max_adjustment = (999 - base_risk);
					double avg_risk_minute = (order_risk / (timeLimitInSecs / 60));
					double percent_added = avg_risk_minute / ((timeLimitInSecs / 60) * 999);
					double adj_percent = Math.min(percent_added, 1.0);
					double risk_metric = base_risk + (max_adjustment * adj_percent);
				
					org.opensims.alert.AlertDef alert_def = getManager().getCorrelator().getAlertManager().lookupAlertDefByUniqueId(unique_id);
		
					if (alert_def != null) {
						org.jdom.Element def_node = (org.jdom.Element) alert_def.getReportNode().clone();
			
						if (def_node != null) {
							def_node.setAttribute("risk", org.opensims.db.om.Alert.formatRisk0(risk_metric));
							def_node.setAttribute("count", order_count);
							def_node.setAttribute("recent", getManager().getCorrelator().formatTick(order_tick));
							content_node.addContent(def_node);
						}
					}
				}
			}
			catch (Exception e) {
				log_.error("select alert type targets", e);
				throw new org.opensims.report.ReportException(e.getMessage());
			}
		}
		else {
			throw new org.opensims.report.ReportException("undefined alert type");
		}
	
		return content_node;
	}

	public String
	getSignature(org.opensims.report.Navigate navigate)
	{
		StringBuffer sig = new StringBuffer( super.getSignature(navigate) );
		sig.append( ":" + navigate.propagateParamValue("alert_type_id") );
		sig.append( ":" + navigate.propagateParamValue("type_name") );
		sig.append( ":" + navigate.propagateParamValue("ip") );
		return sig.toString();
	}

}
