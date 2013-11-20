/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: IncidentReport.java,v 1.15 2005/11/25 21:13:46 mikee Exp $
 * $Id: IncidentReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.15 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    IncidentReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.IncidentReport.class.getName());


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
		String alert_id = navigate.getRawParam("alert_id");
		boolean found = false;
	
		if (alert_id != null) {
			org.opensims.alert.Alert alert = getManager().getCorrelator().getAlertManager().lookupAlertById(alert_id, false);
	
			if (alert != null) {
				org.jdom.Element report_node = (org.jdom.Element) alert.getReportNode().clone();
				if (report_node != null) {
					content_node.addContent(report_node);
					found = true;
				}		
				navigate.propagateParamValue("alert_id", alert_id);
			}
		}
	
		// error handling	
		if (!found) {
			throw new org.opensims.report.ReportException("incident report not available");
		}
	
		return content_node;
	}
}
