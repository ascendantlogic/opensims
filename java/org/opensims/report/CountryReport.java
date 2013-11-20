/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: CountryReport.java,v 1.18 2007/04/22 06:06:14 jeff Exp $
 * $Id: CountryReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.18 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 * @author Jeff PIHL <jeff@outer.net>
 */

public class
    CountryReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.CountryReport.class.getName());


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
		String country_code = getCountryCodeFromNavigate( navigate );
	
		if (country_code != null) {
			org.jdom.Element fixer_node = (org.jdom.Element) getManager().getCorrelator().getBogeyManager().getGeoLoc().getFixerNodeByCode(country_code).clone();
	
			content_node.addContent(fixer_node);
			navigate.propagateParamValue("cc", country_code);
	
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
				buf.append("' AND host.network IS NULL AND host.id = alert.src_host_id AND host.country = '" + country_code + "' ");
				buf.append("AND " + navigate.getAlertIntervalSql("alert") );
				buf.append("GROUP BY host.ip_addr ");

				if ( with_archive ) {
					sql = "SELECT ip_addr, SUM(order_tick) AS order_risk, SUM(order_count) AS order_count, MAX(order_tick) AS order_tick FROM ( "
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
		
					String ip_addr_str = record.getValue("ip_addr").asString();
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
		
					org.opensims.db.om.Host bogey = (org.opensims.db.om.Host) getManager().getCorrelator().getBogeyManager().lookupBogeyByIpAddr(ip_addr_str, false);
		
					if (bogey != null) {
						org.jdom.Element bogey_node = bogey.getExportNode();
			
						if (bogey_node != null) {
							bogey_node.setAttribute("risk", org.opensims.db.om.Alert.formatRisk0(risk_metric));
							bogey_node.setAttribute("count", order_count);
							bogey_node.setAttribute("recent", getManager().getCorrelator().formatTick(order_tick));			
							content_node.addContent(bogey_node);
						}
					}
				}
			}
			catch (Exception e) {
				log_.error("select country targets", e);
				throw new org.opensims.report.ReportException(e.getMessage());
			}
		}
		else {
			try {
				content_node = new org.jdom.Element(org.opensims.xml.Node.NOTICE_NODE);
				org.jdom.Element group_node = null;
				org.jdom.Element fixer_node = null;
				String group_by = null;
				java.util.Iterator country_iter = getManager().getCorrelator().getBogeyManager().getGeoLoc().getFixerDoc().getRootElement().getChildren(org.opensims.xml.Node.COUNTRY_NODE).iterator();
				while (country_iter.hasNext()) {
					fixer_node = (org.jdom.Element) country_iter.next();
					log_.info ( "fixer node: " + org.opensims.xml.XmlBuilder.formatXML( fixer_node, true, true ) );

					if (fixer_node != null) {
						group_by = fixer_node.getAttributeValue("iso3166").substring(0,1);
						if ( group_node == null || ! group_by.equals( group_node.getAttributeValue("group_by") ) ) {
							group_node = new org.jdom.Element(org.opensims.xml.Node.GROUP_NODE);
							group_node.setAttribute("group_by", group_by);
							content_node.addContent(group_node);
						}
						group_node.addContent((org.jdom.Element) fixer_node.clone());
					}
				}
			}
			catch (Exception e) {
				log_.error("select country list targets", e);
				throw new org.opensims.report.ReportException(e.getMessage());
			}
			//throw new org.opensims.report.ReportException("undefined country");
		}
	
		return content_node;
	}
	
	public String
	getSignature(org.opensims.report.Navigate navigate)
	{
		java.lang.String country_code = getCountryCodeFromNavigate(navigate);
		navigate.propagateParamValue("cc", country_code);
		if ( country_code != null ) {
			return super.getSignature(navigate) + ":" + country_code ;
		}
		return null;
	}
	
	private String
	getCountryCodeFromNavigate(org.opensims.report.Navigate navigate)
	{
		String country_code = navigate.getRawParam("cc");
		if (country_code != null && country_code.matches("[?A-Z]+")) {
			return country_code;
		}
		return null;
	}
	
}
