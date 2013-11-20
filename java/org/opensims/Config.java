/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: Config.java,v 1.22 2004/10/22 06:16:16 paco Exp $
 * $Id: Config.java 1 2008-01-10 18:37:05Z smoot $
 * OpenSIMS - configuration manager
 * @version $Revision: 1.22 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Jim NASBY <jnasby@symbiot.com>
 */

public class
    Config
    extends java.util.Hashtable 
{
    // public statics

    public final static org.opensims.xml.XmlBuilder xml_builder = new org.opensims.xml.XmlBuilder(false);

    // protected fields

    protected java.io.File file = null;
    protected org.jdom.Document document = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.Config.class.getName());


    /**
     * Load configuration settings from the specified XML file.
     */

    public void
	init (String file_name)
    {
	try {
	    setFile(new java.io.File(file_name));

	    if (log_.isInfoEnabled()) {
		log_.info("config file: " + getFile().toString());
	    }

	    java.io.FileReader file_reader = new java.io.FileReader(getFile());
	    java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);

	    setDocument(xml_builder.build(buf_reader));

	    if (log_.isDebugEnabled()) {
		String xml = org.opensims.xml.XmlBuilder.formatXML(getRootElement(), false, true);
		log_.debug(xml);
	    }

	    buf_reader.close();
	    parse();
	}
	catch (Exception e) {
	    log_.error("load file " + file_name, e); 
	}
    }


    //////////////////////////////////////////////////////////////////////
    // XML document handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the document file.
     */

    public java.io.File
	getFile ()
    {
	return file;
    }


    /**
     * Set the document file.
     */

    public void
	setFile (java.io.File file)
    {
	this.file = file;
    }


    /**
     * Get the XML document.
     */

    public org.jdom.Document
	getDocument ()
    {
	return document;
    }


    /**
     * Set the XML document.
     */

    public void
	setDocument (org.jdom.Document document)
    {
	this.document = document;
    }


    /**
     * Save the updated document to disk
     */

    public void
	saveConfigFile ()
    {
	synchronized (getDocument()) {
	    try {
		if (!getFile().canWrite()) {
		    log_.warn("save config file - no pre-existing (or bad file perms) " + getFile());
		}
		else {
		    java.io.FileWriter file_writer = new java.io.FileWriter(getFile());
		    java.io.BufferedWriter buf_writer = new java.io.BufferedWriter(file_writer);
		    java.io.PrintWriter print_writer = new java.io.PrintWriter(buf_writer);

		    String xml = org.opensims.xml.XmlBuilder.formatXML(getRootElement(), true, true);

		    print_writer.write(xml);
		    print_writer.flush();
		    print_writer.close();
		}
	    }
	    catch (Exception e) {
		log_.error("save config", e);
	    }
	}
    }


    //////////////////////////////////////////////////////////////////////
    // parsing
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse the configuration settings
     */

    public void
	parse ()
    {
	// load the name/value params

	java.util.Iterator param_iter = getRootElement().getChildren(org.opensims.xml.Node.PARAM_NODE).iterator();

        while (param_iter.hasNext()) {
            org.jdom.Element param_node = (org.jdom.Element) param_iter.next();

	    // <PARAM name="foo" value="bar" />

	    String name = param_node.getAttributeValue("name");
	    String value = param_node.getAttributeValue("value");

	    if (log_.isDebugEnabled()) {
		log_.debug("param: " + name + " = " + value);
	    }

	    put(name, value);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // factory methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Build an instance of a correlation engine
     */

    public org.opensims.Correlator
	buildCorrelator (org.opensims.servlet.Servlet servlet)
    {
	org.opensims.Correlator correlator = null;

	try {
	    org.jdom.Element correlator_node = getRootElement().getChild(org.opensims.xml.Node.CORRELATOR_NODE);
	    String class_name = correlator_node.getAttributeValue("class");
	    Class c = Class.forName(class_name);

	    correlator = (org.opensims.Correlator) c.newInstance();
	    correlator.init(servlet, this, correlator_node);
	}
	catch (Exception e) {
	    log_.error("build correlator instance", e);
	}

	return correlator;
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the root element, so that another class may lookup a
     * particular kind of node.
     */

    public org.jdom.Element
	getRootElement ()
    {
	return getDocument().getRootElement();
    }


    /**
     * Wrap the name/value params as a set of configuration
     * properties...  to create them.
     */

    public java.util.Properties
        getConfigProps ()
    {
	return getConfigProps(new java.util.Properties());
    }


    /**
     * Wrap the name/value params as a set of configuration
     * properties...  to operate on an existing propety list.
     */

    public java.util.Properties
        getConfigProps (java.util.Properties config_props)
    {
	for (java.util.Enumeration e = keys(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
	    String value = (String) get(name);

	    config_props.put(name, value);
	}

	return config_props;
    }


    /**
     * Lookup a name/value param.
     */

    public String
	getParam (String name)
    {
	return (String) get(name);
    }


    /**
     * Convenience method to lookup an XPath expression for a single node.
     */

    public Object
	selectSingleNode (String path)
	throws Exception
    {
	return org.jdom.xpath.XPath.selectSingleNode(getDocument(), path);
    }


    /**
     * Convenience method to lookup an XPath expression for a list of nodes.
     */

    public java.util.List
	selectNodes (String path)
	throws Exception
    {
	return org.jdom.xpath.XPath.selectNodes(getDocument(), path);
    }


    //////////////////////////////////////////////////////////////////////
    // format conversions utility methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Copy the given text file template
     */

    protected void
	copyTemplate (StringBuffer buf, String path, String file_name)
    {
	try {
	    java.io.File file = new java.io.File(path, file_name);
	    java.io.FileReader file_reader = new java.io.FileReader(file);
	    java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);
	    String line = null;

	    while ((line = buf_reader.readLine()) != null) {
		buf.append(line);
		buf.append("\n");
	    }

	    buf_reader.close();
	}
	catch (Exception e) {
	    log_.error("error copying template " + file_name, e); 
	}
    }


    /**
     * Filter a reported MAC address to make sure it is in the
     * canonical form
     */

    public static String
	parseMacAddr (String mac_addr)
    {
	StringBuffer key_mac_addr = new StringBuffer();

	java.util.StringTokenizer st = new java.util.StringTokenizer(mac_addr, ": -");

	while (st.hasMoreTokens()) {
	    key_mac_addr.append(st.nextToken());
	}

	return key_mac_addr.toString().toUpperCase();
    }


    /**
     * Filter an attribute, replacing null with the default
     */

    public static String
	fixNull (String value, String default_value)
    {
	return (value != null) ? value : default_value;
    }


    //////////////////////////////////////////////////////////////////////
    // quality assurance
    //////////////////////////////////////////////////////////////////////

    /**
     * command line test interface
     */

    public static void
	main (String[] argv)
    {
	org.apache.log4j.BasicConfigurator.configure(new org.apache.log4j.ConsoleAppender(new org.apache.log4j.PatternLayout()));

	try {
	    org.opensims.Config config = new org.opensims.Config();

	    // load and parse the XML file

	    config.init(argv[0]);
	}
	catch (Exception e) {
	    log_.error("command line invocation", e);
	}
    }
}
