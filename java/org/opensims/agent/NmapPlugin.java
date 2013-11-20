/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: NmapPlugin.java,v 1.21 2006/03/09 04:22:19 mikee Exp $
 * $Id: NmapPlugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.21 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    NmapPlugin
    extends org.opensims.agent.GenericPlugin
{
    // public definitions

    public final static int MAX_SCAN_SIZE = 3;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.NmapPlugin.class.getName());


    //////////////////////////////////////////////////////////////////////
    // configuration methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Execute a specific configuration, e.g. during autodiscovery.
     *
     * @TODO the XSL file name should come from an attribute in the runtime_node.
     */

    public boolean
	doConfig (org.opensims.model.Scan scan, String param)
	{
		boolean result = false;	
		setScan(scan);
	
		try {
			java.io.File xsl_dir = new java.io.File(getCorrelator().getConfig("webapp.dir"), "xsl");
			java.io.File xsl_file = new java.io.File(xsl_dir, "plugin_config_nmap.xsl");
	
			org.opensims.xml.XslTransform config_xslt = new org.opensims.xml.XslTransform(xsl_file);
	
			org.jdom.transform.JDOMSource source_tree = new org.jdom.transform.JDOMSource(getAgent().getConfigNode());
			org.jdom.transform.JDOMResult result_tree = new org.jdom.transform.JDOMResult();
	
			String cmd_line_args = getCorrelator().getConfig(param);
	
			// use an iterator to prevent flooding NMAP with target IP addresses
			String ip_addr_list = getScan().listReceiptsChunk(MAX_SCAN_SIZE);
			String ref = "hosts";
	
			if (param.endsWith("port_scan") || param.endsWith("services_probe")) {
				ref = "services";
			}
	
			config_xslt.getTransformer().setParameter("cmd_line_args", cmd_line_args);
			config_xslt.getTransformer().setParameter("ip_addr_list", ip_addr_list);
			config_xslt.getTransformer().setParameter("ref", ref);
			
			// Test to see if arbitrary attributes are passed back from the agent --ME
			// config_xslt.getTransformer().setParameter("test", "123");
			
			config_xslt.getTransformer().transform(source_tree, result_tree);
			org.jdom.Element config_node = result_tree.getDocument().getRootElement();
	
			result = true;
			agent.enqueueCommand(org.opensims.xml.XmlBuilder.formatXML(config_node, false, true), false);
		}
		catch (Exception e) {
			log_.error("plugin config xslt", e);
		}
	
		return result;
	}


    //////////////////////////////////////////////////////////////////////
    // parsing methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse the incoming XML node sent from the Agent.
     */

    public boolean
	parseNode (String node_name, org.jdom.Element node)
	{
		boolean valid_transact = false;
	
		if (node_name.equals(org.opensims.xml.Node.NMAP_OUTPUT_NODE)) {
			handleNmapOutput(node);
			valid_transact = true;
		}
	
		return valid_transact;
	}


    /**
     * Parse an incoming <NMAP_OUTPUT/> node from the Agent.
     */

    public void
	handleNmapOutput (org.jdom.Element node)
	{
		try {
		
			if (log_.isDebugEnabled()) {
				log_.debug("starting NMAP_OUTPUT decode " + node + " " + getScan() );
			}
		
			if (getScan() != null) {
				// transform from the plugin's raw input to our autodiscovery XML schema
				java.io.File xsl_dir = new java.io.File(getCorrelator().getConfig("webapp.dir"), "xsl");
				java.io.File xsl_file = new java.io.File(xsl_dir, "plugin_scrub_nmap.xsl");
		
				org.opensims.xml.XslTransform scrub_xslt = new org.opensims.xml.XslTransform(xsl_file);
		
				org.jdom.transform.JDOMSource source_tree = new org.jdom.transform.JDOMSource(node);
				org.jdom.transform.JDOMResult result_tree = new org.jdom.transform.JDOMResult();
		
				scrub_xslt.getTransformer().transform(source_tree, result_tree);
		
				org.jdom.Element network_node = result_tree.getDocument().getRootElement();
				String param = network_node.getAttributeValue("ref");
		
				if (log_.isDebugEnabled()) {
					log_.debug("nmap handler - param " + param + " - network node - "+ org.opensims.xml.XmlBuilder.formatXML(network_node, false, true));
				}
				
				// Use Xpath to find the target network-- ultimately to find the correct scan object
				String path = "//nmaprun/host/address";
				org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path);
				org.jdom.Element nmap_node = (org.jdom.Element) xpath.selectSingleNode(node);
				
				if (nmap_node != null) {
					String ip = new String(nmap_node.getAttributeValue("addr"));
				
					if (log_.isDebugEnabled()) {
						log_.debug("nmap decode - addr found " + ip);
					}
					
					// use the results to update the network model
					org.opensims.model.Scan scan = getAgent().getManager().getCorrelator().getScanManager().getScanByIP(ip);
					scan.updateModel(network_node, param);
					scan.agentPostEvent("agent.receipt");
				}
			}
		}
		catch (Exception e) {
			log_.error("parse <NMAP_OUTPUT/>", e);
		}
	}
}
