/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: Lingo.java,v 1.5 2004/10/03 12:37:38 paco Exp $
 * $Id: Lingo.java 1 2008-01-10 18:37:05Z smoot $
 * OpenSIMS - translation manager
 * @version $Revision: 1.5 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    Lingo
{
    // public statics

    public final static org.opensims.xml.XmlBuilder xml_builder = new org.opensims.xml.XmlBuilder(false);

    // protected fields

    org.jdom.Document lingo_doc = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.Lingo.class.getName());


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the "lingo" document.
     */

    public org.jdom.Document
	getLingoDoc ()
    {
	return lingo_doc;
    }


    /**
     * Load the "lingo" document.
     */

    public void
	loadLingoDoc (java.io.File lingo_file)
    {
	try {
	    java.io.FileInputStream file_in_stream = new java.io.FileInputStream(lingo_file);
	    java.io.InputStreamReader in_stream_reader = new java.io.InputStreamReader(file_in_stream, "UTF8");
	    java.io.BufferedReader buf_reader = new java.io.BufferedReader(in_stream_reader);

	    lingo_doc = xml_builder.build(buf_reader);

	    if (log_.isDebugEnabled()) {
		String xml = org.opensims.xml.XmlBuilder.formatXML(getLingoDoc().getRootElement(), true, true);
		log_.debug("lingo xml: " + xml);
	    }

	    buf_reader.close();
	}
	catch (Exception e) {
	    log_.error("error loading " + lingo_file.toString(), e); 
	}
    }


    //////////////////////////////////////////////////////////////////////
    // XSL document handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Load an XSL document.
     */

    public org.jdom.Document
	loadXslDoc (java.io.File xsl_file)
	throws Exception
    {
	if (log_.isDebugEnabled()) {
	    log_.debug("xsl file: " + xsl_file);
	}

	java.io.FileReader file_reader = new java.io.FileReader(xsl_file);
	java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);

	org.jdom.Document xsl_doc = xml_builder.build(buf_reader);

	if (log_.isDebugEnabled()) {
	    String xml = org.opensims.xml.XmlBuilder.formatXML(xsl_doc.getRootElement(), true, true);
	    log_.debug("xsl doc: " + xml);
	}

	return xsl_doc;
    }


    /**
     * Translate an XSL document.
     */

    public void
	translateXsl (String language, org.jdom.Document xsl_doc)
	throws Exception
    {
	StringBuffer path = new StringBuffer();

	path.append("//");
	path.append(org.opensims.xml.Node.TRANSLATE_NODE);

	if (log_.isDebugEnabled()) {
	    log_.debug("xpath: " + path.toString());
	}

	// construct a list of the nodes to translate

	org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
	java.util.Iterator translate_iter = xpath.selectNodes(xsl_doc).iterator();
	java.util.ArrayList node_list = new java.util.ArrayList();

	while (translate_iter.hasNext()) {
	    org.jdom.Element translate_node = (org.jdom.Element) translate_iter.next();
	    node_list.add(translate_node);
	}

	// translate each node

	for (int i = 0; i < node_list.size(); i++) {
	    org.jdom.Element translate_node = (org.jdom.Element) node_list.get(i);
	    String key = translate_node.getAttributeValue("key");
	    String text = translateNode(language, key);

	    if (log_.isDebugEnabled()) {
		log_.debug("translate: " + language + " key: " + key + " |" + text + "|");
	    }

	    org.jdom.Element parent_node = (org.jdom.Element) translate_node.getParent();

	    parent_node.addContent(text);
	    translate_node.detach();
	}

	// show the results

	if (log_.isDebugEnabled()) {
	    String xml = org.opensims.xml.XmlBuilder.formatXML(xsl_doc.getRootElement(), true, false);
	    log_.debug("translated xsl: " + xml);
	}
    }


    /**
     * Translate one node
     */

    public String
	translateNode (String language, String key)
	throws Exception
    {
	String text = "??";

	try {
	    StringBuffer path = new StringBuffer();

	    path.append("//");
	    path.append(org.opensims.xml.Node.TERM_NODE);
	    path.append("[@key = '");
	    path.append(key);
	    path.append("']/");
	    path.append(org.opensims.xml.Node.TEXT_NODE);
	    path.append("[@lang = '");
	    path.append(language);
	    path.append("']");

	    if (log_.isDebugEnabled()) {
		log_.debug("xpath: " + path.toString());
	    }

	    org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
	    org.jdom.Element text_node = (org.jdom.Element) xpath.selectSingleNode(getLingoDoc());

	    text = text_node.getText();
	}
	catch (Exception e) {
	    log_.error("translate lang " + language + " key " + key, e);
	}

	return text;
    }


    /**
     * Generate the translated XML document using the modified XSL
     * transform.
     */

    public org.jdom.Document
	generateDoc (org.jdom.Document xsl_doc, org.jdom.Element source_node)
	throws Exception
    {
	org.jdom.transform.XSLTransformer transform = new org.jdom.transform.XSLTransformer(xsl_doc);
	org.jdom.Document in_doc = new org.jdom.Document(source_node);

	return transform.transform(in_doc);
    }
}
