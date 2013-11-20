/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: DefenderProfileReport.java,v 1.16 2006/10/01 23:56:07 mikee Exp $
 * $Id: DefenderProfileReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.16 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    DefenderProfileReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.DefenderProfileReport.class.getName());


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
		org.opensims.model.HostOrBogey host = null;
	
		// primary lookup, by ip - used by Flash GUI
		String ip_addr_str = navigate.getRawParam("ip");
		
		if (ip_addr_str != null) {
			host = getManager().getCorrelator().getScanManager().lookupHostByIpAddr(ip_addr_str, false);
	
			if (log_.isDebugEnabled()) {
					log_.debug("ip_addr_str |" + ip_addr_str + "| maps to " + host);
			}
		}

		// generate the report
		if (host != null) {
			org.jdom.Element report_node = (org.jdom.Element) host.getReportNode().clone();
			org.opensims.db.om.Org organization = null;
		
			try {
				// Find all groups in the database so we can select which should apply to this host				
				org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
				java.util.Iterator group_iter = org.opensims.db.om.OrgPeer.doSelect(crit).iterator();
				String group_checkbox = null;
				
				while (group_iter.hasNext()) {
					organization = (org.opensims.db.om.Org) group_iter.next();
					org.jdom.Element group_node = new org.jdom.Element(org.opensims.xml.Node.GROUP_NODE);
					
					String isSubmitted = navigate.getRawParam("update_group");
					
					// If we don't have administrative perms, don't allow updates
					if (!navigate.isInRoleAdmin()) 
						isSubmitted = null;
						
					if (isSubmitted != null) {
						group_checkbox = navigate.getRawParam("group_" + organization.getOrgName());
						
						if ("on".equals(group_checkbox)) {
							
							// already in database-- ignore
							if (host.isInGroup(organization.getOrgName())) {
								if (log_.isDebugEnabled()) {
									log_.debug("Host already in group");
								}
							}
							// add it
							else {
								host.setGroup(organization.getOrgName());
								if (log_.isDebugEnabled()) {
									log_.debug("Added host to group");
								}
							}
						}
						
						// delete it
						else {
							if (host.isInGroup(organization.getOrgName())) {
								host.removeGroup(organization.getOrgName());
								if (log_.isDebugEnabled()) {
									log_.debug("Host removed from group");
								}
							}
						}
					}
					if (log_.isDebugEnabled()) {
						log_.debug("isSubmitted? " + isSubmitted);
						log_.debug("Group Checkbox Set? " + "group_" + organization.getOrgName() + " : " + group_checkbox);
					}
					
					group_node.setAttribute("name", organization.getOrgName());
					group_node.setAttribute("enabled", String.valueOf(host.isInGroup(organization.getOrgName())));
					report_node.addContent(group_node);
				}
			}
			catch (Exception e) {
				log_.error("group db lookup error", e);
			}
	
			if (report_node != null) {
				content_node.addContent(report_node);
				navigate.propagateParamValue("ip", ip_addr_str);
			}
			
			if (log_.isDebugEnabled()) {
				log_.debug("REPORT NODE: " + org.opensims.xml.XmlBuilder.formatXML(report_node, true, true));
			}
	
			/**
			 * @TODO add the alerts list
			 */
			}
		else {
			throw new org.opensims.report.ReportException("undefined host");
		}
	
		return content_node;
	}
	
	public String
	getSignature(org.opensims.report.Navigate navigate)
	{
		String ip_addr_str = navigate.getRawParam("ip");
		navigate.propagateParamValue("ip", ip_addr_str);
		return super.getSignature(navigate) + ":" + ip_addr_str ;
	}

}
