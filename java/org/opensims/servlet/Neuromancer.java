/**
 * @LICENSE@
 */

package org.opensims.servlet;

/**
 * $CVSId: Neuromancer.java,v 1.12 2006/08/30 15:31:47 mikee Exp $
 * $Id: Neuromancer.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.12 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    Neuromancer
{
    // public definitions

    public final static String ROLE_ADMIN = "opensims_admin";
    public final static String ROLE_LOGIN = "opensims_login";

    // protected fields

    protected java.io.File logins_file = null;
    protected org.jdom.Document document = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.servlet.Neuromancer.class.getName());


    /**
     * Initialization.
     */

    public void
	init (org.opensims.Config config)
    {
		// load the document
	
		java.io.File var_lib = new java.io.File(config.getParam("var.lib"));
	
		setLoginsFile(new java.io.File(var_lib, "logins.xml"));
		loadDocument();
    }


    //////////////////////////////////////////////////////////////////////
    // document handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the logins file.
     */

    public java.io.File
	getLoginsFile ()
    {
		return logins_file;
    }


    /**
     * Set the logins file.
     */

    public void
	setLoginsFile (java.io.File logins_file)
    {
		this.logins_file = logins_file;
    }


    /**
     * Get the logins XML document.
     */

    public org.jdom.Document
	getDocument ()
    {
		return document;
    }


    /**
     * Set the logins XML document.
     */

    public void
	setDocument (org.jdom.Document document)
    {
		this.document = document;
    }


    /**
     * Load the logins XML document.
     */

    protected void
	loadDocument ()
	{
		java.io.BufferedReader buf_reader = null;
	
		try {
			// test the updates logins file
	
			if (log_.isInfoEnabled()) {
				log_.info("logins file: " + getLoginsFile());
			}
	
			if (getLoginsFile().canRead() && (getLoginsFile().length() > 0) ) {
				java.io.FileReader file_reader = new java.io.FileReader(getLoginsFile());
				buf_reader = new java.io.BufferedReader(file_reader);
		
				org.opensims.xml.XmlBuilder xml_builder = new org.opensims.xml.XmlBuilder(false);
		
				setDocument(xml_builder.build(buf_reader));
		
				if (log_.isInfoEnabled()) {
					log_.info(org.opensims.xml.XmlBuilder.formatXML(getDocument().getRootElement(), true, true));
				}
			}
			else {
				// Populate a new document with login information out of the database
				org.jdom.Element neuromancer_node = new org.jdom.Element(org.opensims.xml.Node.NEUROMANCER_NODE);
				org.opensims.db.om.Users userRecord = null;
				
				try {
					org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();					
					java.util.Iterator user_iter = org.opensims.db.om.UsersPeer.doSelect(crit).iterator();
					
					while (user_iter.hasNext()) {
						org.jdom.Element logins_node = new org.jdom.Element(org.opensims.xml.Node.LOGIN_NODE);
						userRecord = (org.opensims.db.om.Users) user_iter.next();
						
						// <LOGIN username="admin" password="x" session="x" connected="true" admin="true" group="admin_group">
						
						logins_node.setAttribute("username", userRecord.getUserName());
						logins_node.setAttribute("password", userRecord.getUserPass());
						logins_node.setAttribute("group", userRecord.getOrganization());
						logins_node.setAttribute("connected", "false");
						
						// query the user_role directly
						String sql = "SELECT role_name FROM user_roles WHERE user_name='" + userRecord.getUserName() + "' AND role_name='" + ROLE_ADMIN + "'; ";
						java.util.List foundList = org.opensims.db.om.UserRolesPeer.executeQuery(sql);
						
						if (foundList.isEmpty())
							logins_node.setAttribute("admin", "false");
						else
							logins_node.setAttribute("admin", "true");
	
						org.jdom.Element browser_node = new org.jdom.Element(org.opensims.xml.Node.BROWSER_NODE);
						browser_node.setAttribute("ip_addr", "127.0.0.254");
						browser_node.setAttribute("port", "32767");
						browser_node.setAttribute("tick", String.valueOf(System.currentTimeMillis()));
						
						org.jdom.Element vuln_node = new org.jdom.Element(org.opensims.xml.Node.PARAM_NODE);
						vuln_node.setAttribute("name","VIZ.slider.vuln");
						vuln_node.setAttribute("value","0.0");
						org.jdom.Element threat_node = new org.jdom.Element(org.opensims.xml.Node.PARAM_NODE);
						threat_node.setAttribute("name","VIZ.slider.threat");
						threat_node.setAttribute("value","0.0");
						org.jdom.Element risk_node = new org.jdom.Element(org.opensims.xml.Node.PARAM_NODE);
						risk_node.setAttribute("name","VIZ.slider.risk");
						risk_node.setAttribute("value","0.0");
						
						org.jdom.Element subscriber_node = new org.jdom.Element(org.opensims.xml.Node.SUBSCRIBER_NODE);
						subscriber_node.setAttribute("ip_addr","127.0.0.254");
						subscriber_node.setAttribute("port","32767");
						subscriber_node.setAttribute("tick", String.valueOf(System.currentTimeMillis()));
						
						logins_node.addContent(browser_node);
						logins_node.addContent(subscriber_node);
						logins_node.addContent(vuln_node);
						logins_node.addContent(threat_node);
						logins_node.addContent(risk_node);

						neuromancer_node.addContent(logins_node);
						
						if (log_.isDebugEnabled()) {
							log_.debug("User found in DB: " + org.opensims.xml.XmlBuilder.formatXML(logins_node, false, true));
						}
					}
				}
				catch (Exception e) {
					log_.error("recreate login xml document", e);
				}
				
				setDocument(new org.jdom.Document(neuromancer_node));
				saveDocument();
			}
		}
		catch (Exception e) {
			log_.error("load document", e);
		}
		finally {
			if (buf_reader != null) {
				try {
					buf_reader.close();
				}
				catch (Exception e2) {
					log_.error("close reader", e2);
				}
			}
		}
	}


    /**
     * Save the logins XML document.
     */

    public void
	saveDocument ()
    {
		if (log_.isInfoEnabled()) {
			log_.info("save logins file: " + getLoginsFile());
		}
	
		try {
			if (!getLoginsFile().canWrite()) {
				log_.warn("save logins file - no pre-existing (or bad file perms) " + getLoginsFile());
			}
	
			if (log_.isDebugEnabled()) {
				log_.debug("save logins " + org.opensims.xml.XmlBuilder.formatXML(getDocument().getRootElement(), true, true));
			}
	
			java.io.FileWriter file_writer = new java.io.FileWriter(getLoginsFile());
			java.io.BufferedWriter buf_writer = new java.io.BufferedWriter(file_writer);
			java.io.PrintWriter print_writer = new java.io.PrintWriter(buf_writer);
	
			String xml = org.opensims.xml.XmlBuilder.formatXML(getDocument().getRootElement(), true, true);
	
			print_writer.write(xml);
			print_writer.flush();
			print_writer.close();
		}
		catch (Exception e) {
			log_.error("save logins file", e);
		}
    }


    //////////////////////////////////////////////////////////////////////
    // session management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the HttpSession from the request, and set it to be
     * persistent.
     */

    public javax.servlet.http.HttpSession
	getSession (javax.servlet.http.HttpServletRequest req)
	{
		javax.servlet.http.HttpSession session = req.getSession();
		session.setMaxInactiveInterval(-1);

		// manage login, if user is available	
		String user = req.getRemoteUser();
		if (user != null) {
			String session_id = session.getId();
			String ip_addr = req.getRemoteAddr();
			String port = String.valueOf(req.getRemotePort());
			String tick = String.valueOf(System.currentTimeMillis());
	
			if (log_.isDebugEnabled()) {
				String admin_role = String.valueOf(req.isUserInRole(ROLE_ADMIN));	
				log_.debug(session_id + " user " + user + " ip_addr " + ip_addr + " port " + port + " tick " + tick + " admin " + admin_role);
			}
	
			org.jdom.Element login_node = lookupLoginByUser(session, user);
	
			// update the access trail
			if (login_node != null) {
				login_node.setAttribute("session", session_id);
	
				org.jdom.Element browser_node = login_node.getChild(org.opensims.xml.Node.BROWSER_NODE);
	
				if (browser_node == null) {
					browser_node = new org.jdom.Element(org.opensims.xml.Node.BROWSER_NODE);
					login_node.addContent(browser_node);
				}
		
				browser_node.setAttribute("ip_addr", ip_addr);
				browser_node.setAttribute("port", port);
				browser_node.setAttribute("tick", tick);
			}
		}
		return session;
	}


    /**
     * Lookup the login node by user, setting one if needed.
     */

    public org.jdom.Element
	lookupLoginByUser (javax.servlet.http.HttpSession session, String user)
	{
		org.jdom.Element login_node = (org.jdom.Element) session.getAttribute("login");
	
		if (login_node == null) {
			login_node = lookupLoginByUser(user);
	
			if (login_node != null) {
				session.setAttribute("login", login_node);
			}
		}
		return login_node;
	}


    /**
     * Lookup the login node by user.
     */

    public org.jdom.Element
	lookupLoginByUser (String user)
	{
		org.jdom.Element login_node = null;
	
		try {
			StringBuffer path = new StringBuffer("//");
			path.append(org.opensims.xml.Node.LOGIN_NODE);
			path.append("[@username = '");
			path.append(user);
			path.append("']");
			login_node = (org.jdom.Element) org.jdom.xpath.XPath.selectSingleNode(getDocument(), path.toString());
		}
		catch (Exception e) {
			log_.error("lookup by user |" + user + "|", e);
		}
		return login_node;
	}


    /**
     * Lookup the login node by session ID.
     */

    public org.opensims.servlet.Login
	lookupLoginBySessionId (String session_id, java.net.Socket socket)
	{
		org.opensims.servlet.Login login = null;
	
		try {
			StringBuffer path = new StringBuffer("//");
	
			path.append(org.opensims.xml.Node.LOGIN_NODE);
			path.append("[@session = '");
			path.append(session_id);
			path.append("']");
	
			org.jdom.Element login_node = (org.jdom.Element) org.jdom.xpath.XPath.selectSingleNode(getDocument(), path.toString());
	
			if (login_node != null) {
				login = new org.opensims.servlet.Login();
				login.init(login_node);
		
				// update the access trail
				String ip_addr = socket.getInetAddress().getHostAddress();
				String port = String.valueOf(socket.getPort());
				String tick = String.valueOf(System.currentTimeMillis());
		
				org.jdom.Element subscriber_node = login_node.getChild(org.opensims.xml.Node.SUBSCRIBER_NODE);
				if (subscriber_node == null) {
					subscriber_node = new org.jdom.Element(org.opensims.xml.Node.SUBSCRIBER_NODE);
					login_node.addContent(subscriber_node);
				}
		
				subscriber_node.setAttribute("ip_addr", ip_addr);
				subscriber_node.setAttribute("port", port);
				subscriber_node.setAttribute("tick", tick);
			}
		}
		catch (Exception e) {
			log_.error("lookup by session |" + session_id + "|", e);
		}	
		return login;
	}
    
    
    /**
     * Lookup the group/organization from the database from a given user
     */

    public String
	lookupGroupByUser (String user)
    {
    	String organization = "guest_group";
		org.opensims.db.om.Users userRecord = null;

    	try {		
			org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
			crit.add(org.opensims.db.om.UsersPeer.USER_NAME, user);
			
			java.util.Iterator user_iter = org.opensims.db.om.UsersPeer.doSelect(crit).iterator();
			
			if (user_iter.hasNext()) {
				userRecord = (org.opensims.db.om.Users) user_iter.next();
			}
			if (userRecord != null) {
				organization = userRecord.getOrganization();
			}
		}
		catch (Exception e) {
			log_.error("group lookup exception", e);
		}
		return (organization);
    }
    
    
    /**
     * Lookup if a given user is logged in
     */

    public boolean
	isLoggedIn (String user)
    {
    	org.jdom.Element login_node = null;
    	boolean isOn = false;
    	
    	login_node = lookupLoginByUser (user);
    	if (login_node != null) {
    		isOn = Boolean.valueOf(login_node.getAttributeValue("connected")).booleanValue();
    	}
    	return (isOn);
    }
    
    
    /**
     * Lookup if a given user is logged in
     */

    public String
	getSessionId (String user)
    {
    	org.jdom.Element login_node = null;
    	String session = "";
    	
    	login_node = lookupLoginByUser (user);
    	if (login_node != null) {
    		session = login_node.getAttributeValue("session");
    	}
    	return session;
    }
    
    
    /**
     * Set a user's login status
     */

    public void
	setLogin (String user, String status)
    {
    	org.jdom.Element login_node = null;
    	
    	login_node = lookupLoginByUser (user);
    	if (login_node != null) {
    		login_node.setAttribute("connected", status);
    	}
    	if (log_.isDebugEnabled()) {
			log_.debug("set login status: " + login_node + " " + user + " " + status);
		}
			
    	saveDocument();
    }    

    
    
}
