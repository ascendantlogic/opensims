/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: TopAttackersReport.java,v 1.18 2007/06/02 17:02:46 mikee Exp $
 * $Id: TopAttackersReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.18 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    TopAttackersReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.TopAttackersReport.class.getName());


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
				if (alert_sum != null)
					content_node.setAttribute("total", alert_sum);
			}
	
			// select the top attackers
			StringBuffer buf = new StringBuffer();

			buf.append("SELECT host.ip_addr AS ip_addr, host.network AS network, ");
			buf.append("SUM(alert.rm*alert.incident_count) AS order_risk, SUM(alert.incident_count) AS order_count, MAX(alert.tick) AS order_tick ");
			buf.append("FROM alert JOIN host_org_link on ");
			buf.append("( alert.dst_host_id = host_org_link.host_id OR alert.src_host_id = host_org_link.host_id ), ");
			buf.append("host ");
			buf.append("WHERE host_org_link.organization = '");
			buf.append(navigate.getOrganization());
			buf.append("' AND alert.src_host_id = host.id ");
			buf.append("AND " + navigate.getAlertIntervalSql("alert") );
			buf.append(" GROUP BY host.ip_addr, host.network ");

			if ( with_archive ) {
				sql = "SELECT ip_addr, network, SUM(order_risk) AS order_risk, SUM(order_count) AS order_count, MAX(order_tick) AS order_tick FROM ( "
					+ buf.toString()
					+ " UNION "
					+ buf.toString().replaceAll( "FROM alert", "FROM alert_archive alert" )
					+ " ) AS alert GROUP BY ip_addr, network ";
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
		
				String ip_addr_str = record.getValue("ip_addr").asString();
				String network = record.getValue("network").asString();
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
				
				org.jdom.Element bogey_node = null;
		
				if (log_.isDebugEnabled()) {
					log_.debug("ip " + ip_addr_str + " network " + network + " order count " + order_count);
				}
		
				if (network != null) {
					org.opensims.model.HostOrBogey host = getManager().getCorrelator().getScanManager().lookupHostByIpAddr(ip_addr_str, false);
		
					if (host != null) {
						bogey_node = (org.jdom.Element) host.getExportNode().clone();
					}
				}
				else {
					org.opensims.model.HostOrBogey bogey = getManager().getCorrelator().getBogeyManager().lookupBogeyByIpAddr(ip_addr_str, false);
		
					if (bogey != null) {
						bogey_node = (org.jdom.Element) bogey.getExportNode().clone();
					}
				}
		
				if (bogey_node != null) {
					bogey_node.setAttribute("risk", org.opensims.db.om.Alert.formatRisk0(risk_metric));
					bogey_node.setAttribute("count", order_count);
					bogey_node.setAttribute("recent", getManager().getCorrelator().formatTick(order_tick));
		
					content_node.addContent(bogey_node);
				}
			}
		}
		catch (Exception e) {
			log_.error("select top attackers", e);
			throw new org.opensims.report.ReportException(e.getMessage());
		}
	
		return content_node;
	}
}
