/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: NetworkModelReport.java,v 1.15 2006/03/02 16:42:41 mikee Exp $
 * $Id: NetworkModelReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.15 $
 * @author Mike W. ERWIN <mikee@symbiot.com>
 * @author Paco NATHAN <paco@symbiot.com> 
 */

public class
    NetworkModelReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.NetworkModelReport.class.getName());

	private java.util.ArrayList hostList = new java.util.ArrayList();
	

    //////////////////////////////////////////////////////////////////////
    // content formatting
    //////////////////////////////////////////////////////////////////////
    
    /**
     * Iterates across a JDOM tree looking for matching HOST Elements.  (RECURSIVE)
     */

	private void
	stripHostNodes (org.jdom.Element currentElement)
	{	
		long lastseen = 0L;
		long now = 0L;
		
		// RECURSIVELY drill down through all elements looking for more <HOSTS>s
		java.util.Iterator element_iter = currentElement.getChildren().iterator();
		while (element_iter.hasNext()) {
			org.jdom.Element childElement = (org.jdom.Element) element_iter.next();
			
			if (childElement.getName() == org.opensims.xml.Node.HOST_NODE) {
				// <HOST> Element found in child, extract IP address
				String ip_addr_str = childElement.getAttributeValue("ip_addr");
			
				// Does this HOST node appear anywhere in our list of hosts?
				if (ip_addr_str != null) {
					// If it's no in our list, remove it
					if (! hostList.contains(ip_addr_str)) {
						element_iter.remove();
						// currentElement.detach();
					}
					else {					
						if (childElement.getAttributeValue("lastseen") != null) {
							// Calculate the age since we last scanned this host and make human-readable 
							lastseen = Long.parseLong(childElement.getAttributeValue("lastseen"));
							now = getManager().getCorrelator().getTick();
						
							childElement.setAttribute ("age", getManager().getCorrelator().formatTickDifference(now, lastseen));
						}
						else {
							childElement.setAttribute ("age", "?");
						}
					}
				}
			}
			else {
				this.stripHostNodes(childElement);
			}
		}
	}
    
    
    
    /**
     * Return the XML content node for formatting.
     */

    public org.jdom.Element
    getContentNode (org.opensims.report.Navigate navigate)
	throws org.opensims.report.ReportException
    {
		navigate.setEnabled(false);
		
		if (log_.isDebugEnabled()) {
			log_.debug("network model report | username, organization: " + navigate.getUsername() + ", " + navigate.getOrganization());
		}
	
		org.jdom.Element content_node = new org.jdom.Element(org.opensims.xml.Node.REPORT_NODE);
		org.jdom.Element report_node = getManager().getCorrelator().getScanManager().getReportNode();
		
		if (report_node != null) {
			content_node.addContent(report_node);
			
			if (log_.isDebugEnabled()) {
				log_.debug("Content Node Before Filtering: ");
				log_.debug(org.opensims.xml.XmlBuilder.formatXML(content_node, false, true));
			}
		
			// Fill an array of hosts that we need to keep
			hostList = new java.util.ArrayList();
			try {
				org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
				crit.add(org.opensims.db.om.HostOrgLinkPeer.ORGANIZATION, navigate.getOrganization());
				java.util.Iterator link_iter = org.opensims.db.om.HostOrgLinkPeer.doSelectJoinHost(crit).iterator();
				
				while (link_iter.hasNext()) {
					org.opensims.db.om.HostOrgLink hostOrgLink = (org.opensims.db.om.HostOrgLink) link_iter.next();
					org.opensims.db.om.Host host = hostOrgLink.getHost();								
					String ip_addr_string = (host.getIPv4()).toString();
					hostList.add(ip_addr_string);
				}
			}
			catch (Exception e) {
				log_.error("Torque error on host/group join", e);
			}
			
			// Loop through the <HOST> elements and delete the ones that aren't in our filter include list
			stripHostNodes(content_node);			
		}
		else {
			throw new org.opensims.report.ReportException("network model not available");
		}
		
		if (log_.isDebugEnabled()) {
			log_.debug("Content Node After Filtering: ");
			log_.debug(org.opensims.xml.XmlBuilder.formatXML(content_node, false, true));
		}
		
		return content_node;
	}
}
