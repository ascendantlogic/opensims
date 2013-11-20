/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: AgentOperationReport.java,v 1.9 2004/11/03 23:14:14 paco Exp $
 * $Id: AgentOperationReport.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.9 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    AgentOperationReport
    extends org.opensims.report.GenericReport
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.AgentOperationReport.class.getName());


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
	navigate.setEnabled(false);

	org.jdom.Element content_node = new org.jdom.Element(org.opensims.xml.Node.REPORT_NODE);
	org.jdom.Element report_node = getManager().getCorrelator().getAgentManager().getReportNode();

	if (report_node != null) {
	    content_node.addContent(report_node);
	}
	else {
	    throw new org.opensims.report.ReportException("agent operation not available");
	}

	return content_node;
    }
}
