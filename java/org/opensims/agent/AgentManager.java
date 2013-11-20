/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: AgentManager.java,v 1.44 2006/12/25 19:52:54 mikee Exp $
 * $Id: AgentManager.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.44 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    AgentManager
    extends org.opensims.Manager
{
    // quality assurance
    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.AgentManager.class.getName());


    /**
     * Constructor
     */

    public
	AgentManager (org.opensims.Correlator correlator)
    {
		super(correlator, org.opensims.agent.AgentManager.class.getName());
    }


    //////////////////////////////////////////////////////////////////////
    // transaction handling methods
    //////////////////////////////////////////////////////////////////////

    public org.opensims.agent.Agent
	getAgent (org.opensims.IPv4 ip_addr, org.jdom.Element root)
	throws Exception
    {
		org.opensims.agent.Agent agent = null;
		
		// parse the incoming <AGENT/> node and lookup the agent
		String agent_name = root.getAttributeValue("agent_id");
		String plugin_name = root.getAttributeValue("plugin_id");
		String mac_addr = org.opensims.Config.parseMacAddr(root.getAttributeValue("mac_id"));
		String nonce = root.getAttributeValue("nonce");
		
		if (log_.isDebugEnabled())
		{
			log_.debug("agent - ");
			log_.debug("  ip addr - " + ip_addr.toString());
			log_.debug("  agent_name - " + agent_name);
			log_.debug("  plugin_name - " + plugin_name);
			log_.debug("  mac_addr - " + mac_addr);
			log_.debug("  nonce - " + nonce);
	
			String platform = root.getAttributeValue("platform");
			long tick = getCorrelator().parseTick(root.getAttributeValue("timestamp"));
	
			log_.debug("  platform - " + platform);
			log_.debug("  tick - " + tick);
			
			log_.debug("  actual agent communication - " + org.opensims.xml.XmlBuilder.formatXML(root, false, true));
		}
		
		// lookup an agent among the ones with current logins
		String key = org.opensims.agent.GenericAgent.getKey(agent_name, mac_addr);
		agent = (org.opensims.agent.Agent)get(key);
		
		if (log_.isDebugEnabled()) {
	    	log_.debug("lookup agent |" + key + "| in " + this.toString() + " -> " + agent);
	    	if (agent != null)
		    	log_.debug("agent thread state: " + (agent.getAgentThread()).isAlive());
	    }
		
		if (agent != null)
		{
			// We found an agent in our hash; make sure the given nonce matches our saved nonce
			if (!agent.getNonce().equals(nonce))
			{
				// Does not match, so assume this is a new agent instance running on that system
				if (log_.isDebugEnabled()) {
					log_.debug("Nonce given by agent (" + agent.getNonce() + ") does not match internal nonce (" + nonce + ")");
				}
				
				agent = null;
			}
		}
		
		// iterate over its incoming XML messages
		java.util.Iterator iter = root.getChildren().iterator();
	
		while (iter.hasNext())
		{
			org.jdom.Element node = (org.jdom.Element) iter.next();
			String node_name = node.getName();
			
			if (node_name.equals(org.opensims.xml.Node.LOGIN_NODE))
			{
				if (agent == null)
				{
					// Create a new agent object to manage communication
					String app_sig = node.getAttributeValue("app_signature");
					ip_addr = new org.opensims.IPv4(node.getAttributeValue("ip"));
					agent = loginAgent (key, agent_name, mac_addr, ip_addr, app_sig);
				}
				
				if (agent != null)
				{	
					// parse/config the available plugins and start
					// the agent
					agent.parsePlugins(node);
					agent.start(false);
					agent.createThreads();
		
					// add the agent to the manager's list
					put(agent.getKey(), agent);
		
					if (log_.isDebugEnabled())
					{
						log_.debug("added agent |" + agent.getKey() + "| into " + this.toString());
					}
				}
				else
				{
					// We couldn't find the agent in our configuration
					if (log_.isDebugEnabled())
						log_.debug("agent not authenticated 1 " + key + ":" + agent_name + ":" + mac_addr + ":" +  ip_addr );
					throw new org.opensims.agent.ProtocolException("agent not authenticated: " + key + " - " + node_name);
				}
			}
			else if (node_name.equals(org.opensims.xml.Node.LOGOUT_NODE))
			{
				if (agent != null)
				{
					if (agent.logout())
					{
						if (log_.isDebugEnabled())
							log_.debug("remove agent |" + agent.getKey() + "| from " + this.toString());
			
						remove(agent.getKey());
					}
				}
			}
			else if (agent == null)
			{
				// If we get here, we don't have an agent instance and all of the above
				// checks failed; reject the agent as not authenticated
				if (log_.isDebugEnabled())
					log_.debug("agent not authenticated login " + key + ":" + agent_name + ":" + mac_addr + ":" +  ip_addr );
				throw new org.opensims.agent.ProtocolException("agent not authenticated: " + key + " - " + node_name);
			}
		}
		
		return agent;
    }

    /**
     * Process Agent Command
     */

    public void
	processAgentCommand (org.opensims.agent.Agent agent, org.jdom.Element root)
	throws Exception
    {
    	String plugin_name = root.getAttributeValue("plugin_id");
    
    	// iterate over its incoming XML messages
		java.util.Iterator iter = root.getChildren().iterator();
	
		while (iter.hasNext())
		{
			org.jdom.Element node = (org.jdom.Element) iter.next();
			String node_name = node.getName();

			if ((node_name.equals(org.opensims.xml.Node.BEAT_NODE)) ||
				(node_name.equals(org.opensims.xml.Node.NOTICE_NODE))) {
				parseAgentNode(agent, plugin_name, node, node_name);
			}
			else if (agent == null)
			{
				// If we get here, we don't have an agent instance and all of the above
				// checks failed; reject the agent as not authenticated
				if (log_.isDebugEnabled())
					log_.debug("agent not authenticated beat/notice " );
				throw new org.opensims.agent.ProtocolException("agent not authenticated: " + node_name);
			}
		}
	}
				

    /**
     * Process Agent Transaction
     */

    public void
	processAgentTransaction (org.opensims.agent.Agent agent, org.jdom.Element root)
	throws Exception
    {
    	String plugin_name = root.getAttributeValue("plugin_id");
    
    	// iterate over its incoming XML messages
		java.util.Iterator iter = root.getChildren().iterator();
	
		while (iter.hasNext())
		{
			org.jdom.Element node = (org.jdom.Element) iter.next();
			String node_name = node.getName();

			if ((node_name.equals(org.opensims.xml.Node.LOGIN_NODE)) ||
				(node_name.equals(org.opensims.xml.Node.LOGOUT_NODE))) {
				// ignore- handled by getAgent()
			}
			else if (agent != null)
				parseAgentNode(agent, plugin_name, node, node_name);
			else
			{
				// If we get here, we don't have an agent instance and all of the above
				// checks failed; reject the agent as not authenticated
				if (log_.isDebugEnabled()) {
					log_.debug("agent not authenticated plugin " );
				}
				throw new org.opensims.agent.ProtocolException("agent not authenticated: " + node_name);
			}
		}
	}
				

    /**
     * Parse one XML node in an agent transaction
     */

    protected void
	parseAgentNode (org.opensims.agent.Agent agent, String plugin_name, org.jdom.Element node, String node_name)
	throws Exception
    {
	boolean valid_transact = false;
	
	if (log_.isDebugEnabled())
		log_.debug("handle agent start undetermined " + Long.toString(getCorrelator().getTick()) + " " + agent + ":" + plugin_name + ":" + node_name );

	if (node_name.equals(org.opensims.xml.Node.BEAT_NODE)) {
	    agent.checkBeatTasks(node);
	    getCorrelator().handleAgentBeat(agent, node);
	    
	   	if (log_.isDebugEnabled())
			log_.debug("handle agent finished beat " + Long.toString(getCorrelator().getTick()) + " " + agent + ":" + plugin_name + ":" + node );

	    valid_transact = true;
	}
	else if (node_name.equals(org.opensims.xml.Node.NOTICE_NODE)) {
	    getCorrelator().handleAgentNotice(agent, node);
	    valid_transact = true;
	    
	  	if (log_.isDebugEnabled())
			log_.debug("handle agent finished notice " + Long.toString(getCorrelator().getTick()) + " " + agent + ":" + plugin_name + ":" + node );

	}
	else {
	    org.opensims.agent.Plugin plugin = agent.getRandomPlugin (plugin_name);
	    
	    if (plugin != null) {
			valid_transact = plugin.parseNode(node_name, node);
	    }
	    
		if (log_.isDebugEnabled()) {
			log_.debug("handle agent finished plugin " + Long.toString(getCorrelator().getTick()) + " " + node_name + ":" + plugin_name + ":" + plugin );
		}
	}

	// protocol verification

	if (!valid_transact) {
	    throw new org.opensims.agent.ProtocolException("agent message not allowed: " + agent.getKey() + " - " + node_name);
	}
    }


    /**
     * Login an agent, authenticating based on the XML config provisioning.
     *
     * @TODO someday when we have "mutual authentication" with Tomcat 5.x, we will will use a GUID embedded in the X.509 certs.
     */

    public org.opensims.agent.Agent
	loginAgent (String key, String agent_name, String mac_addr, org.opensims.IPv4 ip_addr, String app_sig)
	throws org.opensims.agent.LoginException
	{
		org.opensims.agent.Agent agent = null;
	
		try {
			StringBuffer path = new StringBuffer("//");
	
			path.append(org.opensims.xml.Node.AGENT_NODE);
//			path.append("[@local = 'true']/");
			path.append("/");
			path.append(org.opensims.xml.Node.INTERFACE_NODE);
			path.append("[@mac_addr = '");
			path.append(mac_addr);
			path.append("']");
	
			org.jdom.Element interface_node = (org.jdom.Element) getCorrelator().getConfig().selectSingleNode(path.toString());
			org.jdom.Element agent_node = (org.jdom.Element) interface_node.getParent();
	
			if (log_.isDebugEnabled()) {
				String xml = org.opensims.xml.XmlBuilder.formatXML(agent_node, false, true);
				log_.debug("login agent |" + key + "| using " + xml);
			}
	
			if (agent_node == null) {
				// agent login is not allowed: disabled, disavowed, etc.
				throw new org.opensims.agent.LoginException("agent not provisioned: " + agent_name + " mac_addr " + mac_addr + " ip addr " + ip_addr.toString());
			}
			else {
				String nonce = org.opensims.agent.GenericAgent.createNonce();
		
				/**
				 * @TODO someday when we have "mutual authentication" with
				 * Tomcat 5.x, we will will use a GUID embedded in the X.509
				 * certs
		
				String agent_gid = this_agent_login_t.getAgentGid();
		
				 * "lend" the agent a heartbeat -- since the servlet lapsed
				 * continuity, not the agent
		
				this_agent_login_t.heartbeat(agent_gid, nonce);
		
				 */
		
				// Setup 2 threads as default (for queue management) as well as being needed to handle 
				// receipts for scanning 2 networks by default (internal, dmz) --ME
				long threads = 2L;
				String class_name = agent_node.getAttributeValue("class");
				
				if (agent_node.getAttributeValue("threads") != null) {
					threads = Long.valueOf(agent_node.getAttributeValue("threads")).longValue();
				}
				
				Class c = Class.forName(class_name);
				agent = (org.opensims.agent.Agent) c.newInstance();
				agent.init(this, agent_node, agent_name, mac_addr, ip_addr, nonce, app_sig, null, threads);
		
				// This setups the scan objects
				getCorrelator().getScanManager().registerAgent(agent);
			}
		}
		catch (Exception e) {
			log_.error("instantiating subject class", e);
		}
	
		return agent;
	}


    //////////////////////////////////////////////////////////////////////
    // config/command sequencing
    //////////////////////////////////////////////////////////////////////

    /**
     * At this point, we have two XML documents: one provided by the
     * agent which describes its enviornment, and one which is the
     * agent-specific portion of the configuration.  We merge those
     * into one runtime operating spec for this agent.
     */

    public org.jdom.Element
	mergeAvailConfig (org.jdom.Element avail_node, org.jdom.Element config_node)
	{
		org.jdom.Element merge_node = null;
	
		try {
			// create a temporary file for "config.xml"
			/**
			 * @TODO XSLT only allows one source document?  must we use a temp file?
			 */
	
			if (log_.isDebugEnabled()) {
				String xml = org.opensims.xml.XmlBuilder.formatXML(config_node, true, true);
				log_.debug("config node: " + xml);
			}
	
			java.io.File temp_dir = new java.io.File(getCorrelator().getConfig("temp.dir"));
			java.io.File temp_file = java.io.File.createTempFile("config", ".xml", temp_dir);
			java.io.FileWriter out_file = new java.io.FileWriter(temp_file);
		
			out_file.write(org.opensims.xml.XmlBuilder.formatXML(config_node, false, true));
			out_file.close();
	
			// build the XSL transform
	
			java.io.File xsl_dir = new java.io.File(getCorrelator().getConfig("webapp.dir"), "xsl");
			java.io.File plugin_merge_xsl_file = new java.io.File(xsl_dir, "plugin_merge.xsl");
	
			org.opensims.xml.XslTransform plugin_merge_xslt = new org.opensims.xml.XslTransform(plugin_merge_xsl_file);
	
			// run the transform
	
			org.jdom.transform.JDOMSource source = new org.jdom.transform.JDOMSource(avail_node);
			org.jdom.transform.JDOMResult result = new org.jdom.transform.JDOMResult();
	
			plugin_merge_xslt.getTransformer().setParameter("config_file", temp_file.getPath());
			plugin_merge_xslt.getTransformer().transform(source, result);
	
			// clean and return the results
	
			merge_node = result.getDocument().getRootElement();
			
			if (log_.isDebugEnabled()) {
				String xml = org.opensims.xml.XmlBuilder.formatXML(merge_node, true, true);
				log_.debug("merge node: " + xml);
			}
	
			merge_node.detach();
			temp_file.delete();
		}
		catch (Exception e) {
			log_.error("plugin parse", e);
		}
	
		return merge_node;
	}


    /**
     * Send the given command to the local agent
     */

    public void
	localAgentCommand (String command)
	{
		// find the local agent	
		for (java.util.Enumeration e = elements(); e.hasMoreElements(); ) {
			org.opensims.agent.Agent agent = (org.opensims.agent.Agent) e.nextElement();
	
			if (agent.getLocalFlag()) {
				if (log_.isInfoEnabled()) {
					log_.info("local agent command " + agent.getKey() + " " + command);
				}
		
				agent.enqueueCommand(command, false);		
				return;
			}
		}
	}


    //////////////////////////////////////////////////////////////////////
    // report rendering
    //////////////////////////////////////////////////////////////////////

    /**
     * Get an XML representation for the provisioning of the agents
     * and plugins being managed.
     */

    public org.jdom.Element
	getReportNode ()
	{
		org.jdom.Element report_node = new org.jdom.Element(org.opensims.xml.Node.PROVISION_NODE);
	
		for (java.util.Enumeration e = elements(); e.hasMoreElements(); ) {
			org.opensims.agent.Agent agent = (org.opensims.agent.Agent) e.nextElement();
	
			org.jdom.Element config_node = (org.jdom.Element) agent.getConfigNode().clone();
	
			config_node.setAttribute("last_beat", Long.toString(getCorrelator().getTick() - agent.getLastTick()));
			config_node.setAttribute("scan_state", agent.getScan().getCurrentState());
	
			report_node.addContent(config_node);
		}
	
		return report_node;
	}
}
