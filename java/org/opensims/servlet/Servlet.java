/**
 * @LICENSE@
 */

package org.opensims.servlet;

/**
 * $CVSId: Servlet.java,v 1.25 2006/06/29 20:55:48 jeff Exp $
 * $Id: Servlet.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.25 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Suresh RAMACHANDRAN <suresh@symbiot.com>
 */

public class
    Servlet
    extends javax.servlet.http.HttpServlet
{
    // public definitions

    public final static String CONTENT_TYPE_CSV = "csv";
    public final static String CONTENT_TYPE_HTML = "html";
    public final static String CONTENT_TYPE_PDF = "pdf";
    public final static String CONTENT_TYPE_XLS = "xls";
    public final static String CONTENT_TYPE_XML = "xml";

    public final static String CONTENT_TYPE_TEXT_HTML = "text/html";
    public final static String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public final static String CONTENT_TYPE_APPLICATION_XML = "application/xml";

    public final static String TRANSFER_ENCODING_GZIP = "gzip";
    public final static String TRANSFER_ENCODING_ZLIB = "zlib";

    // protected fields

    protected boolean enable_flag = true;
    protected javax.servlet.ServletConfig servlet_config = null;
    protected final org.opensims.Config config_doc = new org.opensims.Config();
    protected java.util.Properties config_props = new java.util.Properties();
    protected java.util.Hashtable site_transform = new java.util.Hashtable();
    protected java.util.Hashtable resource_handler = new java.util.Hashtable();
    protected org.opensims.servlet.Neuromancer neuromancer = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.servlet.Servlet.class.getName());


    //////////////////////////////////////////////////////////////////////
    // servlet interface
    //////////////////////////////////////////////////////////////////////

    /**
     * Servlet initialization method added as part of the Tomcat
     * implementation
     */

    public void
	init (javax.servlet.ServletConfig servlet_config)
	throws javax.servlet.ServletException
    {
	// NB: there's a "bug" in Tomcat so the following initialization is required --
	// see http://archives.real-time.com/pipermail/tomcat-devel/2000-July/000407.html

	super.init(servlet_config);

	// pick up where we would generally start

	Thread.currentThread().setName(getClass().getName() + " " + this.hashCode());
	setServletConfig(servlet_config);

	try {
	    if (log_.isInfoEnabled()) {
		log_.info("initializing servlet - " + this);
	    }

	    // setup the customer configuration

	    String config_file = getServletConfig().getInitParameter("webapp.config");

	    if (config_file != null) {
		getConfigDoc().init(config_file);
		setConfigProps(getConfigDoc().getConfigProps(config_props));
	    }

	    // load initialization parameters into static config

	    for (java.util.Enumeration e = getServletConfig().getInitParameterNames(); e.hasMoreElements(); ) {
		String name = (String) e.nextElement();
		String value = getServletConfig().getInitParameter(name);

		if (log_.isInfoEnabled()) {
		    log_.info("init param: " + name + " = " + value);
		}

		addConfig(name, value);
	    }
	}
	catch (Exception e) {
	    log_.error("init servlet", e);
	    throw new javax.servlet.ServletException(e);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // configuration management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the ServletConfig.
     */

    public javax.servlet.ServletConfig
	getServletConfig ()
    {
	return servlet_config;
    }


    /**
     * Set the ServletConfig.
     */

    public void
	setServletConfig (javax.servlet.ServletConfig servlet_config)
    {
	this.servlet_config = servlet_config;
    }


    /**
     * Get the configuration document object.
     */

    public org.opensims.Config
	getConfigDoc ()
    {
	return config_doc;
    }


    /**
     * Get the set of configuration properties.
     */

    protected java.util.Properties
	getConfigProps ()
    {
	return config_props;
    }


    /**
     * Set the set of configuration properties.
     */

    protected void
	setConfigProps (java.util.Properties config_props)
    {
	this.config_props = config_props;
    }


    /**
     * Get a configuration property.
     */

    public String
	getConfig (String name)
    {
	return getConfigProps().getProperty(name);
    }


    /**
     * Add a configuration property.
     */

    public void
	addConfig (String name, String value)
    {
	getConfigProps().put(name, value);
    }


    //////////////////////////////////////////////////////////////////////
    // manage site transforms (page content)
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the XSL transform for the given content type.
     */

    protected org.opensims.xml.XslTransform
	getSiteTransform (String content_type)
    {
	return (org.opensims.xml.XslTransform) site_transform.get(content_type);
    }


    /**
     * Load the XSL transform for the given content type.
     */

    protected void
	loadSiteTransform (String content_type)
    {
	try {
            java.io.File xsl_dir = new java.io.File(getConfig("webapp.dir"), "xsl");
            java.io.File xsl_file = new java.io.File(xsl_dir, "site_" + content_type + ".xsl");

	    org.opensims.xml.XslTransform transform = new org.opensims.xml.XslTransform(xsl_file);

	    site_transform.put(content_type, transform);
	}
	catch (Exception e) {
	    log_.error("load site transform", e);
	}
    }


    /**
     * Apply the site transform for the given content type.
     */

    public void
	applySiteTransform (javax.xml.transform.Source source, javax.xml.transform.Result result, String content_type)
	throws org.opensims.servlet.ServletException
    {
	try {
	    org.opensims.xml.XslTransform site_transform = getSiteTransform(content_type);

            site_transform.getTransformer().transform(source, result);
	}
	catch (Exception e) {
	    log_.error("apply site transform", e);
	    throw new org.opensims.servlet.ServletException(e.getMessage());
	}
    }


    /**
     * Send the given text back as the HTTP response.
     * Attempts to fix IE bugs: 
     * http://support.microsoft.com/default.aspx?scid=kb;en-us;323308
     */

    public void
	sendText (javax.servlet.http.HttpServletResponse res, String content_type, String text)
	throws Exception
    {
	// specify content attributes

	res.setContentType(content_type);
	res.setContentLength(text.length());

	// disable caching (for IE bug fixes)

	/**
	 * @TODO does this cause IE headaches?

	res.addHeader("Cache-Control", "no-cache");
	res.addHeader("Cache-Control", "no-store");
	res.addHeader("Pragma", "no-cache");
	 */

	res.setHeader("Pragma", "public");
	res.setHeader("Cache-Control", "max-age=0");

	/**
	 * @TODO way too much info

	if (log_.isDebugEnabled()) {
	    log_.debug(text);
	}

	 */

	java.io.PrintWriter writer = res.getWriter();
	
	writer.print(text);
	writer.flush();
    }


    //////////////////////////////////////////////////////////////////////
    // login persistence
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the login persistence manager.
     */

    public org.opensims.servlet.Neuromancer
	getNeuromancer ()
    {
	return neuromancer;
    }


    /**
     * Set the login persistence manager.
     */

    public void
	setNeuromancer (org.opensims.servlet.Neuromancer neuromancer)
    {
	this.neuromancer = neuromancer;
    }


    //////////////////////////////////////////////////////////////////////
    // internationalization
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the best-fit language.
     */

    public String
	getLanguage (javax.servlet.http.HttpServletRequest req)
    {
	String accept_language = req.getHeader("Accept-Language");

	if (log_.isInfoEnabled()) {
	    log_.info("accept language: " + accept_language);
	}

	if (accept_language == null) {
	    accept_language = "en";
	}

	/*
 22 Sep 2004 12:48:50,554 [live 67.107.81.194] INFO org.opensims.servlet.Servlet - accept language: en-us,en;q=0.9,fr;q=0.7,ja;q=0.6,fa;q=0.4,de;q=0.3,es;q=0.1
	*/

	/**
	 * @TODO fix the language ISO 638 code
	 */

	return accept_language.substring(0, 2);
    }


    //////////////////////////////////////////////////////////////////////
    // URI request handlers
    //////////////////////////////////////////////////////////////////////

    /**
     * Register the named URI resource handler.
     */

    protected void
	registerResourceHandler (String resource, String handler_name)
    {
        try {
            Class[] param_types = new Class[] { javax.servlet.http.HttpServletRequest.class, javax.servlet.http.HttpServletResponse.class, java.util.StringTokenizer.class };
            java.lang.reflect.Method handler_method = getClass().getMethod(handler_name, param_types);

	    if (log_.isDebugEnabled()) {
		log_.debug("register resource |" + resource + "| handler |" + handler_name + "| = " + handler_method);
	    }

	    resource_handler.put(resource, handler_method);
        }
        catch (Exception e) {
            log_.error("register method", e);
        }
    }


    /**
     * Apply the requested URI resource handler.
     */

    protected void
	invokeResourceHandler (String resource, javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res, java.util.StringTokenizer resource_list)
    {
	try {
            java.lang.reflect.Method handler_method = (java.lang.reflect.Method) resource_handler.get(resource);
            Object[] param_list = new Object[] {req, res, resource_list};

	    handler_method.invoke(this, param_list);
	}
        catch (Exception e) {
            log_.error("invoke method", e);
        }
    }


    /**
     * Handle the top-level URI for an HTTP request.
     */

    public void
	handleRequest (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res)
	throws Exception
    {
	java.util.StringTokenizer resource_list = new java.util.StringTokenizer(req.getRequestURI(), "/");
	String resource = null;

	// strip off the first (servlet) then get the second (resource)

	for (int i = 0; i < 2; i++) {
	    if (resource_list.hasMoreTokens()) {
		resource = resource_list.nextToken();
	    }
	}

	if (log_.isDebugEnabled()) {
	    String path = req.getRequestURI();
	    String addr = req.getRemoteAddr();
	    String user = req.getRemoteUser();
	    String type = req.getContentType();

	    log_.debug("req " + path + " | " + resource  + " - " + addr + " user " + user + " type " + type);
	}

	if (getEnableFlag()) {
	    // switch on the path "resource"

	    if (resource != null) {
		Thread.currentThread().setName(resource + " " + req.getRemoteAddr());
		invokeResourceHandler(resource, req, res, resource_list);
	    }
	}
	else {
	    // unless the servlet currently disabled

	    res.sendError(javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE, "system is rebooting");
	}
    }


    /**
     * Get the HttpSession from the request, and set it to be
     * persistent.
     */

    public javax.servlet.http.HttpSession
	getSession (javax.servlet.http.HttpServletRequest req)
    {
	javax.servlet.http.HttpSession session = req.getSession();

	session.setMaxInactiveInterval(-1);

	return session;
    }


    /**
     * Get the enable flag.
     */

    public boolean
	getEnableFlag ()
    {
	return enable_flag;
    }


    /**
     * Set the enable flag.
     */

    public void
	setEnableFlag (boolean enable_flag)
    {
	this.enable_flag = enable_flag;
    }
}
