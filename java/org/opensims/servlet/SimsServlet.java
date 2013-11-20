/**
 * @LICENSE@
 */

package org.opensims.servlet;

/**
 * $CVSId: SimsServlet.java,v 1.43 2007/10/22 01:40:12 mikee Exp $
 * $Id: SimsServlet.java 14 2008-03-03 18:30:21Z smoot $
 * @version $Revision: 1.43 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 * @author Suresh RAMACHANDRAN <suresh@symbiot.com>
 */

public class
    SimsServlet
    extends org.opensims.servlet.Servlet
{
    // public definitions

    public final static int SC_CUSTOM_SERVER_ERROR = 599;

    // protected fields

    protected org.opensims.Correlator correlator = null;
    protected org.opensims.Lingo lingo = null;
    protected java.io.File user_log_file = null;
    protected java.io.FileWriter user_file_writer;
	protected java.io.BufferedWriter user_buf_writer;
	protected java.io.PrintWriter user_print_writer;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.servlet.SimsServlet.class.getName());

    /**
     * Enabling the following flag CAUSES ALL UNPARSED INPUT TO
     * SHOW ON THE LOG FILE.  This is intended to bypass the XML
     * parsers, in case some of the incoming messages are way too
     * wonky to debug otherwise.  This is an extreme approach, and
     * generally overflows the log files.
     */

    private final static boolean divert_reader_ = false; // true


    //////////////////////////////////////////////////////////////////////
    // servlet lifecycle methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Servlet initialization method added as part of the Tomcat
     * implementation
     */

    public void
	init (javax.servlet.ServletConfig servlet_config)
	throws javax.servlet.ServletException
    {
		super.init(servlet_config);
	
		// establish the context for singleton/unique resources
		javax.servlet.ServletContext servlet_context = getServletConfig().getServletContext();
		setCorrelator((org.opensims.Correlator) servlet_context.getAttribute("Correlator"));
	
		if (log_.isInfoEnabled()) {
			java.io.File tempdir_path = (java.io.File) servlet_context.getAttribute("javax.servlet.context.tempdir");
			log_.info("tempdir path: " + tempdir_path.getPath() + " - existing correlator: " + getCorrelator());
		}
	
		if (getCorrelator() == null) {
			setCorrelator(getConfigDoc().buildCorrelator(this));
			servlet_context.setAttribute("Correlator", getCorrelator());
			getCorrelator().start();
	
			// build the GUI index page	
			if (log_.isDebugEnabled()) {
				for (java.util.Enumeration e = servlet_context.getAttributeNames(); e.hasMoreElements(); ) {
					String attr_name = (String) e.nextElement();
		
					log_.debug("context attribute: " + attr_name + " = " + servlet_context.getAttribute(attr_name));
				}
			}
		}
	
		// load the site page transforms
		loadSiteTransform(CONTENT_TYPE_HTML);
		loadSiteTransform(CONTENT_TYPE_CSV);
	
		// register the URI resources
		registerResourceHandler("live", "handleRequestLive");
		registerResourceHandler("report", "handleRequestReport");
		registerResourceHandler("support", "handleRequestSupport");
		registerResourceHandler("agent", "handleRequestAgent");
		registerResourceHandler("param", "handleRequestParam");
		registerResourceHandler("test", "handleRequestTest");
	
		// language translation support	
		setLingo(new org.opensims.Lingo());
	
		java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
		java.io.File lingo_file = new java.io.File(var_lib, "lingo.xml");
	
		if (log_.isInfoEnabled()) {
			log_.info("lingo file: " + lingo_file);
		}
		
		getLingo().loadLingoDoc(lingo_file);
		
		// establish a "Neuromancer" object for persisting logins
		org.opensims.servlet.Neuromancer neuromancer = new org.opensims.servlet.Neuromancer();
		neuromancer.init(getConfigDoc());
		setNeuromancer(neuromancer);
		
		// Setup the user audit trail (as append)
		try {
			java.io.File var_log = new java.io.File("/var/log/opensims");
			user_log_file = new java.io.File(var_log, "audit.log");
			user_file_writer = new java.io.FileWriter(user_log_file, true);
			user_buf_writer = new java.io.BufferedWriter(user_file_writer);
			user_print_writer = new java.io.PrintWriter(user_buf_writer);    
		}
		catch (Exception e) {
			log_.error("Unable to open audit.log for writing, bad permissions or corrupt directory " + user_log_file);
		}
	}


    /**
     * Called by the servlet container to indicate to a servlet that
     * the servlet is being taken out of service. This method is only
     * called once all threads within the servlet's service method
     * have exited or after a timeout period has passed. After the
     * servlet container calls this method, it will not call the
     * service method again on this servlet.
     */

    public void
	destroy ()
    {
   		user_print_writer.flush();
    	user_print_writer.close();
    
		if (getCorrelator() != null) {
			getCorrelator().stop();
		}
	
		super.destroy();
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Correlator.
     */

    public org.opensims.Correlator
	getCorrelator ()
    {
		return correlator;
    }


    /**
     * Set the Correlator.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator)
    {
		this.correlator = correlator;
    }


    /**
     * Get the Lingo translator.
     */

    public org.opensims.Lingo
	getLingo ()
    {
		return lingo;
    }


    /**
     * Set the Lingo translator.
     */

    public void
	setLingo (org.opensims.Lingo lingo)
    {
		this.lingo = lingo;
    }

    
    /**
     * Append to the user log audit trail.
     */

    public void
	appendUserLog (String user, String sessionId, String entry)
    {
    	String log_entry;
    	
    	log_entry = getCorrelator().formatTick(getCorrelator().getTick()) + " : " + sessionId + " : " + user + " : " + entry + "\n";
    	
		user_print_writer.write(log_entry);
		user_print_writer.flush();
    }


    /**
     * Append to the user log audit trail.
     */

    public void
	appendUserLog (javax.servlet.http.HttpServletRequest req, String entry)
    {
    	javax.servlet.http.HttpSession session = req.getSession();
    	String sessionId = session.getId();
    	
    	appendUserLog (req.getRemoteUser(), sessionId, entry);
    }

    
    //////////////////////////////////////////////////////////////////////
    // HTTP transaction processing
    //////////////////////////////////////////////////////////////////////

    /**
     * Handle a GET request
     */

    public void
	doGet (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res)
	throws javax.servlet.ServletException, java.io.IOException
    {
		doPost(req, res);
    }


    /**
     * Handle a POST request
     */

    public void
	doPost (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res)
	throws javax.servlet.ServletException, java.io.IOException
    {
		try {
			// track the user and session
			String user = req.getRemoteUser();
			javax.servlet.http.HttpSession session = req.getSession();
	
/*
			// Experimenting with how to force tomcat to invalidate a session 
			
			log_.info("user " + user + " session " + session.getId() + " :: " + getNeuromancer().getSessionId(user) + " subscriber logged in? " + getNeuromancer().isLoggedIn(user));
						
			// if ( (!session.getId().equals(getNeuromancer().getSessionId(user))) && (user != null) ) {
			if (getNeuromancer().lookupLoginByUser(user) != null) {
				session.setMaxInactiveInterval(30);
				log_.info("setting 30 second timer on session " + session.getId() );
				// session.invalidate();
			}
*/

			// test the mutual-authentication, if any in use
			if (log_.isInfoEnabled() && (req.getAuthType() != null)) {
				log_.info("x509 auth " + req.getAuthType());
		
				for (java.util.Enumeration e = req.getAttributeNames(); e.hasMoreElements(); ) {
					String attr_name = (String) e.nextElement();
		
					log_.info("x509 attr " + attr_name + " = " + req.getAttribute(attr_name));
				}
		
				java.security.cert.X509Certificate[] certs = (java.security.cert.X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
		
				if (certs != null) {
					for (int i = 0; i < certs.length; i++) {
					log_.info("x509 client cert [" + i + "] = " + certs[i].toString());
					}
				}
				else if ("https".equals(req.getScheme())) {
					log_.info("x509 This was an HTTPS request, but no client certificate is available");
				}
				else {
					log_.info("x509 This was not an HTTPS request, so no client certificate is available");
				}
			}
	
			// switch on the top-level URI "verb"
			handleRequest(req, res);
		}
		catch (Exception e) {
			log_.error("handle post", e);
			throw new javax.servlet.ServletException(e);
		}
    }


    /**
     * <P>
     * Reads an XML document from the HTTP request content stream, for
     * QA/testing.
     * </P>
     * <P>
     * One of the "Catch-22" issue of XML parsing is that if there are
     * horribly bad formatting errors in the input XML (or even minor
     * ones) then an XML parser will typically cough and sputter
     * before you can get much chance to debug/inspect the offending
     * data.  This method provides a way to debug those problem,
     * e.g. from the agent messages.
     * </P>
     */

    protected org.jdom.Document
	readDocument (javax.servlet.http.HttpServletRequest req)
	throws Exception
    {
		org.jdom.Document document = null;
	
		if (divert_reader_) {
			StringBuffer buf = new StringBuffer();
			java.io.BufferedReader buf_reader = req.getReader();
			String line = buf_reader.readLine();
	
			while (line != null) {
				if (log_.isDebugEnabled()) {
					log_.debug("| " + line);
				}		
				buf.append(line);
				line = buf_reader.readLine();
			}
			java.io.StringReader str_reader = new java.io.StringReader(buf.toString());	
			document = getCorrelator().buildXml(new java.io.BufferedReader(str_reader));
		}
		else {
			document = getCorrelator().buildXml(req.getReader());
		}
	
		return document;
    }


    /**
     * Handle requests for generating HTML pages.
     */

    protected String
	renderPage (javax.servlet.http.HttpServletResponse res, String content_type, String page_name)
    {
		String error_message = null;
	
		try {
			// establish the XML source document
			java.io.File page_dir = new java.io.File(getConfig("webapp.dir"), "xsl");
			java.io.File page_file = new java.io.File(page_dir, page_name);
	
			if (log_.isInfoEnabled()) {
				log_.info("page: " + page_file);
			}

			java.io.FileReader file_reader = new java.io.FileReader(page_file);
			java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);	
			org.jdom.Element page_node = getCorrelator().buildXml(buf_reader).getRootElement();
	
			if (log_.isDebugEnabled()) {
				String xml = org.opensims.xml.XmlBuilder.formatXML(page_node, false, true);
				log_.debug("render " + xml);
			}
	
			// apply the site transform	
			org.jdom.transform.JDOMSource source = new org.jdom.transform.JDOMSource(page_node);
			java.io.ByteArrayOutputStream byte_out = new java.io.ByteArrayOutputStream();
			javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(byte_out);	
			applySiteTransform(source, result, content_type);
	
			// send the resulting content
			sendText(res, CONTENT_TYPE_TEXT_HTML, byte_out.toString());
		}
		catch (Exception e) {
			log_.error("render page", e);
			error_message = e.getMessage();
		}
	
		return error_message;
    }


    /**
     * Send the XML/XHTML for the requested parameter.
     */

    protected void
	sendParam (javax.servlet.http.HttpServletResponse res, String content_type, String language, String req_name, org.jdom.Element source_node)
	throws Exception
    {
		try {
			// establish the XSL source document	
			String param_file = "param_" + req_name + ".xsl";
			java.io.File xsl_dir = new java.io.File(getConfig("webapp.dir"), "xsl");
			java.io.File xsl_file = new java.io.File(xsl_dir, param_file);
	
			// translate the XSL document
			org.jdom.Document xsl_doc = getLingo().loadXslDoc(xsl_file);
			getLingo().translateXsl(language, xsl_doc);
			org.jdom.Document param_doc = getLingo().generateDoc(xsl_doc, source_node);
			String text = org.opensims.xml.XmlBuilder.formatXML(param_doc.getRootElement(), true, true);
	
			// send the results
			sendText(res, content_type, text);
		}
		catch (Exception e) {
			log_.error("param: " + req_name, e);
			res.sendError(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
    }


    //////////////////////////////////////////////////////////////////////
    // URI request handlers
    //////////////////////////////////////////////////////////////////////

    /**
     * Handle requests for generating HTML to manage "live" client
     * sessions.
     */

    public void
	handleRequestLive (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res, java.util.StringTokenizer resource_list)
	throws Exception
    {
		javax.servlet.http.HttpSession session = getSession(req);
		String view = req.getParameter("view");
		String instance = req.getParameter("instance");
	
		if (log_.isDebugEnabled()) {
			log_.debug("live: view |" + view + "| instance |" + instance + "|");
		}
	
		org.jdom.Element source_node = new org.jdom.Element(org.opensims.xml.Node.LIVE_NODE);	
		source_node.setAttribute("view", view);
		source_node.setAttribute("instance", instance);
		source_node.setAttribute("session", session.getId());

		sendParam(res, CONTENT_TYPE_TEXT_HTML, getLanguage(req), "live", source_node);
		
		appendUserLog(req, "live: view: " + view + " instance: " + instance);
    }


    /**
     * Handle requests for generating predefined reports.
     */

    public void
	handleRequestReport (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res, java.util.StringTokenizer resource_list)
	throws Exception
    {
		String language = getLanguage(req);
		String error_message = getCorrelator().getReportManager().renderReport(req, res, resource_list);
	
		if (error_message != null) {
			res.sendError(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST, error_message);
		}
		
		appendUserLog(req, "report: " + resource_list);
    }


    /**
     * Handle requests for generating HTML to manage customer support.
     */

    public void
	handleRequestSupport (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res, java.util.StringTokenizer resource_list)
	throws Exception
    {
		String language = getLanguage(req);
		String req_name = req.getParameter("name");
	
		if (req_name != null) {
			if (req_name.equals("contacts") || req_name.equals("diagnostics") || req_name.equals("docs")) {
				String page_name = "support_" + req_name + ".xml";
				String error_message = renderPage(res, CONTENT_TYPE_HTML, page_name);
		
				if (error_message != null) {
					res.sendError(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST, error_message);
				}
			}
		}
		appendUserLog(req, "support: " + req_name);
    }


    /**
     * Handle the protocol (FSM) for an agent transaction, determining
     * the appropriate HTTP response status code, according to RFC
     * 2616 and the OpenSIMS AgentSDK specs.
     */

    public void
	handleRequestAgent (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res, java.util.StringTokenizer resource_list)
	throws Exception
    {
		try {
			// parse the essential <AGENT/> info
			long then = getCorrelator().getTick();
	
			if (log_.isDebugEnabled())
				log_.debug("parse agent transmission start: " + Long.toString( getCorrelator().getTick() - then ));
	
	
			org.opensims.IPv4 ip_addr = new org.opensims.IPv4(req.getRemoteAddr());
			org.jdom.Element root = readDocument(req).getRootElement();
			
			// This method ONLY gets the agent object and does an agent login if needed now --ME
			org.opensims.agent.Agent agent = getCorrelator().getAgentManager().getAgent(ip_addr, root);
			
			if (log_.isDebugEnabled())
				log_.debug("parse agent transmission 1: " + Long.toString( getCorrelator().getTick() - then ));
	
	
			// Check to see if we have commands to process
			getCorrelator().getAgentManager().processAgentCommand(agent, root);
	
			if (log_.isDebugEnabled())
				log_.debug("parse agent transmission 2: " + Long.toString( getCorrelator().getTick() - then ));
	
	
			// prepare the response
			if (agent != null) {
				String command = agent.nextCommand();
	
				if (command != null) {
					sendText(res, CONTENT_TYPE_APPLICATION_XML, command);
			}
	
			if (log_.isDebugEnabled())
				log_.debug("parse agent transmission 3: " + Long.toString( getCorrelator().getTick() - then ));
	
			
			// getCorrelator().getAgentManager().processAgentTransaction(agent, root);
			agent.enqueueTransaction(root);
		
			if (log_.isDebugEnabled())
				log_.debug("parse agent transmission 4: " + Long.toString( getCorrelator().getTick() - then ));
		
	
			}
		// otherwise, respond with an error status code
		}
		catch (org.opensims.agent.ResourceException e) {
			// @TODO: used to be called the "minimal_use_mode"
			// database or other resource is unavailable - try again later
	
			res.sendError(SC_CUSTOM_SERVER_ERROR, "internal resources are unavailable");
			log_.error("NOTE: internal resources seem to be unavailable");
		}
		catch (org.opensims.agent.LoginException e) {
			// login error - got mac_addr?
	
			res.sendError(javax.servlet.http.HttpServletResponse.SC_ACCEPTED, e.getMessage());
			log_.error("NOTE: " + e.getMessage());
		}
		catch (org.opensims.agent.ProtocolException e) {
			// if an agent receives a 404 error, close its connection -
			// this is handled differently than browser sessions
	
			javax.servlet.http.HttpSession session = req.getSession(true);
			session.invalidate();
	
			// something else barfed - reply with a conflict
	
			res.sendError(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			log_.error("NOTE: " + e.getMessage());
		}
    }


    /**
     * Handle requests for generating XML to represent internal
     * parameters.
     */

    public void
	handleRequestParam (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res, java.util.StringTokenizer resource_list)
	throws Exception
    {
		javax.servlet.http.HttpSession session = getSession(req);
		String req_name = req.getParameter("name");

		if (req_name != null) {
			org.jdom.Element source_node = new org.jdom.Element(req_name.toUpperCase());
	
			if (req_name.equals("popup")) {
				source_node.setAttribute("session", session.getId());
				source_node.setAttribute("context", req.getParameter("context"));
		
				sendParam(res, CONTENT_TYPE_APPLICATION_XML, getLanguage(req), req_name, source_node);
			}
			else if (req_name.equals("nav")) {
				source_node.setAttribute("instance", String.valueOf(Math.round(Math.random() * 10000.0)));
				source_node.setAttribute("session", session.getId());
		
				sendParam(res, CONTENT_TYPE_TEXT_HTML, getLanguage(req), req_name, source_node);
			}
			else if (req_name.equals("nav_config")) {
				source_node.setAttribute("instance", org.opensims.Config.fixNull(req.getParameter("instance"), "0"));
				source_node.setAttribute("session", session.getId());
		
				sendParam(res, CONTENT_TYPE_APPLICATION_XML, getLanguage(req), req_name, source_node);
			}
			else if (req_name.equals("socket_config")) {
				source_node.setAttribute("instance", org.opensims.Config.fixNull(req.getParameter("instance"), "0"));
				source_node.setAttribute("session", session.getId());
		
				sendParam(res, CONTENT_TYPE_APPLICATION_XML, getLanguage(req), req_name, source_node);
			}
			else if (req_name.equals("live_config")) {
				source_node.setAttribute("instance", org.opensims.Config.fixNull(req.getParameter("instance"), "0"));
				source_node.setAttribute("session", session.getId());
				source_node.setAttribute("view", req.getParameter("view"));
		
				sendParam(res, CONTENT_TYPE_APPLICATION_XML, getLanguage(req), req_name, source_node);
			}
			else if (req_name.equals("error")) {
				source_node.setAttribute("code", org.opensims.Config.fixNull(req.getParameter("code"), "0"));
				source_node.setAttribute("tick", String.valueOf(getCorrelator().getTick()));
		
				sendParam(res, CONTENT_TYPE_APPLICATION_XML, getLanguage(req), req_name, source_node);
			}
		}
		
		appendUserLog(req, "menu: " + req_name);
    }


    /**
     * Handle requests for internal diagnostics "test" summary report.
     */

    public void
	handleRequestTest (javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res, java.util.StringTokenizer resource_list)
	throws Exception
    {
		String language = getLanguage(req);
		StringBuffer buf = new StringBuffer();
	
		buf.append("<HTML><HEAD><TITLE>Webapp Operation</TITLE><BODY>");
		buf.append("<PRE>");
		buf.append(getClass().getName() + " - working");
		buf.append("\n ");
		buf.append("build tag: " + getConfig("build.tag"));
		buf.append("\n ");
	
		javax.servlet.ServletContext servlet_context = getServletConfig().getServletContext();
	
		for (java.util.Enumeration e = servlet_context.getAttributeNames(); e.hasMoreElements(); ) {
			String attr_name = (String) e.nextElement();
			Object attr_value = servlet_context.getAttribute(attr_name);
	
			buf.append("attribute: " + attr_name + " = " + attr_value);
			buf.append("\n ");
		}
	
		buf.append(getCorrelator().getSummary());
		buf.append("</PRE>");
		buf.append("</BODY></HTML>");
	
		// send the results	
		sendText(res, CONTENT_TYPE_TEXT_HTML, buf.toString());
		
		appendUserLog(req, "test request");
    }
}

