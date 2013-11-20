/**
 * @LICENSE@
 */

package org.opensims.xml;

/**
 * $CVSId: XmlBuilder.java,v 1.5 2004/09/27 19:10:22 paco Exp $
 * $Id: XmlBuilder.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.5 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class 
    XmlBuilder
    extends Object
{
    // public final statics

    public final static String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public final static org.jdom.output.XMLOutputter xml_readable_outputter = new org.jdom.output.XMLOutputter(org.jdom.output.Format.getPrettyFormat());
    public final static org.jdom.output.XMLOutputter xml_compact_outputter = new org.jdom.output.XMLOutputter(org.jdom.output.Format.getCompactFormat());

    // protected fields

    protected org.jdom.input.SAXBuilder sax_builder = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.xml.XmlBuilder.class.getName());


    /**
     * Constructor -
     * subclass this to use an XML parsing method other than the default SAX.
     */
    
    public 
	XmlBuilder (boolean validate)
    {
	try {
	    sax_builder = new org.jdom.input.SAXBuilder(validate);
	}
	catch (Exception e) {
	    log_.error("create SAXBuilder", e);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // parsing methods

    /**
     * XML parsing - using JDOM/SAX push parsing
     */

    public synchronized org.jdom.Document
	build (java.io.BufferedReader buf_reader)
        throws org.jdom.JDOMException, Exception
    {
	return sax_builder.build(buf_reader);
    }


    //////////////////////////////////////////////////////////////////////
    // qc methods

    /**
     * Format a JDOM node into XML text output
     */

    public static String
	formatXML (org.jdom.Element node, boolean versioned, boolean readable)
    {
	java.io.StringWriter writer = new java.io.StringWriter();

	try {
	    if (versioned) {
		writer.write(XML_VERSION);
	    }

	    if (readable) {
		xml_readable_outputter.output(node, writer);
	    }
	    else {
		xml_compact_outputter.output(node, writer);
	    }
	}
	catch (Exception e) {
	    log_.error("format output", e);
	}

	return writer.toString();
    }


    /**
     * command line test interface
     */

    public static void
	main (String[] args)
    {
	String filename = args[0];
	System.err.println("loaded: " + filename);

	try {
	    java.io.File in_file = new java.io.File(filename);
	    java.io.FileInputStream in_stream = new java.io.FileInputStream(in_file);
	    java.io.InputStreamReader in_reader = new java.io.InputStreamReader(in_stream);
	    java.io.BufferedReader buf_reader = new java.io.BufferedReader(in_reader);

	    XmlBuilder xml_builder = new XmlBuilder(false);
	    org.jdom.Document xml_document = xml_builder.build(buf_reader);
	    org.jdom.Element table_node = xml_document.getRootElement();

	    System.err.println("parsed: " + table_node);
	}
	catch (Exception e) {
	    System.err.println("main: " + e.getMessage());
            e.printStackTrace();
	}
    }
}
