/**
 * @LICENSE@
 */

package org.opensims.stream;

/**
 * $CVSId: InitStream.java,v 1.6 2004/09/01 20:54:52 paco Exp $
 * $Id: InitStream.java 1 2008-01-10 18:37:05Z smoot $
 * Stream for attacker/defender visualization
 * @version $Revision: 1.6 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Lindsey SIMON <lsimon@symbiot.com>
 */

public class
    InitStream
    extends org.opensims.stream.GenericStream
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.stream.InitStream.class.getName());


    //////////////////////////////////////////////////////////////////////
    // content methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Build a content document
     */

    public org.jdom.Document
	buildContentDocument (long tick)
    {
	org.jdom.Element content_node = new org.jdom.Element(org.opensims.xml.Node.INIT_NODE);
	org.jdom.Document content_doc = new org.jdom.Document(content_node);

	try {
	    java.io.File xsl_dir = new java.io.File(getCorrelator().getConfig("webapp.dir"), "xsl");
	    java.io.File init_file = new java.io.File(xsl_dir, "vuln_init.xml");

	    if (log_.isDebugEnabled()) {
		log_.debug("vuln_init file: " + init_file);
	    }

	    java.io.FileReader file_reader = new java.io.FileReader(init_file);
	    java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);

	    content_doc = getCorrelator().buildXml(buf_reader);
	    content_node = content_doc.getRootElement();

	    // insert the alert_type definitions into the stream

	    for (java.util.Enumeration e = getCorrelator().getAlertManager().getAlertTypes(); e.hasMoreElements(); ) {
		org.opensims.alert.AlertType alert_type = (org.opensims.alert.AlertType) e.nextElement();
		content_node.addContent((org.jdom.Element) alert_type.getNode().clone());
	    }

	    if (log_.isDebugEnabled()) {
		log_.debug(org.opensims.xml.XmlBuilder.formatXML(content_node, false, true));
	    }
	}
	catch (Exception e) {
	    log_.error("building INIT stream content document", e);
	}

	return content_doc;
    }
}
