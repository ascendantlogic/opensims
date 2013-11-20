/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: ReportManager.java,v 1.19 2006/06/29 20:55:47 jeff Exp $
 * $Id: ReportManager.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.19 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    ReportManager
    extends org.opensims.Manager
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.ReportManager.class.getName());


    /**
     * Constructor
     */

    public
	ReportManager (org.opensims.Correlator correlator)
    {
	super(correlator, org.opensims.report.ReportManager.class.getName());

	try {
	    java.util.Iterator report_iter = getCorrelator().getConfig().getRootElement().getChildren(org.opensims.xml.Node.REPORT_NODE).iterator();

	    // establish the reports

	    while (report_iter.hasNext()) {
		org.jdom.Element report_node = (org.jdom.Element) report_iter.next();

		// instantiate the report

                String class_name = report_node.getAttributeValue("class");
                Class c = Class.forName(class_name);
		org.opensims.report.Report report = (org.opensims.report.Report) c.newInstance();

		report.init(this, report_node);

		if (log_.isDebugEnabled()) {
		    log_.debug("load report instance: " + report.getKey() + " enabled " + report.getEnabled());
		}

		if (report.getEnabled()) {
		    put(report.getKey(), report);
		}
	    }
	}
	catch (Exception e) {
	    log_.error("load report transforms", e);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // render page content
    //////////////////////////////////////////////////////////////////////

    /**
     * Render the requested report
     */

    public String
	renderReport (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res, java.util.StringTokenizer resource_list)
    {
	String error_message = null;

	try {
	    // adjust thread priorities

	    Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);

	    // build the content

	    /**
	     * @TODO the alternative approach

	    if (resource_list.hasMoreTokens()) {
	        key = resource_list.nextToken();
	    }
	     */

	    org.opensims.report.Navigate navigate = new org.opensims.report.Navigate(getCorrelator(), req);
	    String content_type = navigate.getParamValue("content_type");

	    String key = navigate.getParamValue("name");
	    org.opensims.report.Report report = (org.opensims.report.Report) get(key);

	    if (log_.isDebugEnabled()) {
		log_.debug("render report |" + key + "| type " + content_type + " " + report);
	    }

	    if (report != null) {
		synchronized (report) {
		    org.jdom.Element content_node = report.getContentNode(navigate);
		    org.jdom.Element site_node = report.transformContentNode(content_node,navigate);

		    if (log_.isDebugEnabled()) {
			String xml = org.opensims.xml.XmlBuilder.formatXML(site_node, false, true);
			log_.debug("render " + getName() + " - " + xml);
		    }

		    // render the XML content as HTML for the HTTP response

		    if (content_type.equals(org.opensims.servlet.Servlet.CONTENT_TYPE_HTML)) {
			res.setContentType(org.opensims.servlet.Servlet.CONTENT_TYPE_TEXT_HTML);

			org.jdom.transform.JDOMSource source = new org.jdom.transform.JDOMSource(site_node);

			java.io.ByteArrayOutputStream byte_out = new java.io.ByteArrayOutputStream();
			javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(byte_out);

			getCorrelator().getServlet().applySiteTransform(source, result, content_type);

			java.io.PrintWriter writer = res.getWriter();

			writer.print(byte_out.toString());
			writer.flush();
		    }

		    // return the XML content directly as the HTTP response

		    else if (content_type.equals(org.opensims.servlet.Servlet.CONTENT_TYPE_XML)) {
			res.setContentType(org.opensims.servlet.Servlet.CONTENT_TYPE_APPLICATION_XML);

			java.io.PrintWriter writer = res.getWriter();

			writer.print(org.opensims.xml.XmlBuilder.formatXML(site_node, true, true));
			writer.flush();
		    }

		    // return the XML content directly as the HTTP response

		    else if (content_type.equals(org.opensims.servlet.Servlet.CONTENT_TYPE_CSV)) {
			res.setContentType(org.opensims.servlet.Servlet.CONTENT_TYPE_TEXT_PLAIN);

			org.jdom.transform.JDOMSource source = new org.jdom.transform.JDOMSource(site_node);

			java.io.ByteArrayOutputStream byte_out = new java.io.ByteArrayOutputStream();
			javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(byte_out);

			getCorrelator().getServlet().applySiteTransform(source, result, content_type);

			java.io.PrintWriter writer = res.getWriter();

			writer.print(byte_out.toString());
			writer.flush();
		    }
		}
	    }
	    else {
		throw new org.opensims.report.ReportException("report not found: " + key);
	    }
	}
	catch (Exception e) {
	    log_.error("render report", e);
	    error_message = e.getMessage();
	}

	return error_message;
    }

    public org.jdom.Element
	renderReport ( org.opensims.report.Navigate navigate )
    {
	return null;
    }

    /**
     * Get Report Content 
     */

    public org.jdom.Element
	getReportContent (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res, java.util.StringTokenizer resource_list)
    {
    	org.jdom.Element site_node = null;
    	
		try {
			// adjust thread priorities
			Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
	
			// build the content
			org.opensims.report.Navigate navigate = new org.opensims.report.Navigate(getCorrelator(), req);
			String content_type = navigate.getParamValue("content_type");
	
			String key = navigate.getParamValue("name");
			org.opensims.report.Report report = (org.opensims.report.Report) get(key);
	
			if (log_.isDebugEnabled()) {
				log_.debug("render report |" + key + "| type " + content_type + " " + report);
			}
	
			if (report != null) {
				synchronized (report) {
					site_node = report.getContentNode(navigate);
		
					if (log_.isDebugEnabled()) {
						String xml = org.opensims.xml.XmlBuilder.formatXML(site_node, false, true);
						log_.debug("render " + getName() + " - " + xml);
					}
				}
			}
			else {
				throw new org.opensims.report.ReportException("report not found: " + key);
			}
		}
		catch (Exception e) {
			log_.error("render report", e);
		}
		return site_node;
    }


}
