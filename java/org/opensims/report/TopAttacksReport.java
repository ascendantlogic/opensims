/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: TopAttacksReport.java,v 1.18 2007/06/02 17:02:46 mikee Exp $
 * $Id: TopAttacksReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.18 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    TopAttacksReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.TopAttacksReport.class.getName());


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
	
			// select the top attacks
			StringBuffer buf = new StringBuffer();
	
			buf.append("SELECT alert_def.unique_id AS unique_id, ");
			buf.append("SUM(alert.rm*alert.incident_count) AS order_risk, SUM(alert.incident_count) AS order_count, MAX(alert.tick) AS order_tick ");
			buf.append("FROM alert JOIN host_org_link on ");
			buf.append("( alert.dst_host_id = host_org_link.host_id OR alert.src_host_id = host_org_link.host_id ), ");
			buf.append("alert_def ");
			buf.append("WHERE host_org_link.organization = '");
			buf.append(navigate.getOrganization());
			buf.append("' AND alert.alert_def_id = alert_def.id ");
			buf.append("AND " + navigate.getAlertIntervalSql("alert") );
			buf.append(" GROUP BY alert_def.unique_id ");

			if ( with_archive ) {
				sql = "SELECT unique_id, SUM(order_risk) as order_risk, SUM(order_count) as order_count, MAX(order_tick) as order_tick FROM ( "
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
				String order_count = record.getValue("order_count").asString();
				long order_count_long = record.getValue("order_count").asLong();

				long order_tick = record.getValue("order_tick").asLong();
				double order_risk = record.getValue("order_risk").asDouble();
				double base_risk = order_risk / order_count_long;
				double max_adjustment = (999 - base_risk);
				double avg_risk_minute = (order_risk / (timeLimitInSecs / 60));
				double percent_added = avg_risk_minute / ((timeLimitInSecs / 60) * 999);
				double adj_percent = Math.min(percent_added, 1.0);
				double risk_metric = base_risk + (max_adjustment * adj_percent);
				
				if (log_.isDebugEnabled()) {
					log_.debug("Risk Metric: " + order_risk + " : " + base_risk + " : " + avg_risk_minute + " : " + adj_percent +" :: " + risk_metric);
				}
		
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
			log_.error("select top attacks", e);
			throw new org.opensims.report.ReportException(e.getMessage());
		}

		return content_node;
    }
}
