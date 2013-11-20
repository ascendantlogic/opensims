/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: MacLookupPlugin.java,v 1.20 2006/04/02 19:51:17 mikee Exp $
 * $Id: MacLookupPlugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.20 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    MacLookupPlugin
    extends org.opensims.agent.GenericPlugin
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.MacLookupPlugin.class.getName());


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
			java.io.File xsl_file = new java.io.File(xsl_dir, "plugin_config_mac_lookup.xsl");
	
			org.opensims.xml.XslTransform config_xslt = new org.opensims.xml.XslTransform(xsl_file);
	
			org.jdom.transform.JDOMSource source_tree = new org.jdom.transform.JDOMSource(getAgent().getConfigNode());
			org.jdom.transform.JDOMResult result_tree = new org.jdom.transform.JDOMResult();
			
			config_xslt.getTransformer().setParameter("device", getScan().getDevice());
			config_xslt.getTransformer().setParameter("target", getScan().getNetworkNode().getAttributeValue("cidr"));
	
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
	
		if (node_name.equals(org.opensims.xml.Node.MAC_LIST_NODE)) {
			handleMacList(node);
			valid_transact = true;
		}
	
		return valid_transact;
    }


    /**
     * Parse an incoming list of <MAC_ADDR/> per host.
     */

    protected void
	handleMacList (org.jdom.Element node)
    {
		try {
			if (getScan() != null) {	
				// Find the right scan object before doing anything
				org.opensims.IPv4 network_addr = new org.opensims.IPv4(new String(node.getAttributeValue("scan_target")));
				String network_number = org.opensims.IPv4.longToHex(network_addr.getNetworkAddr()).toUpperCase();
				org.opensims.model.Scan scan = getAgent().getScan(network_number);
		
				if (log_.isDebugEnabled()) {
					log_.debug("mac lookup - found network number: " + network_number);
				}
		
				// transform from the plugin's raw input to our autodiscovery XML schema
				java.io.File xsl_dir = new java.io.File(getCorrelator().getConfig("webapp.dir"), "xsl");
				java.io.File xsl_file = new java.io.File(xsl_dir, "plugin_scrub_mac_lookup.xsl");
		
				org.opensims.xml.XslTransform scrub_xslt = new org.opensims.xml.XslTransform(xsl_file);
		
				org.jdom.transform.JDOMSource source_tree = new org.jdom.transform.JDOMSource(node);
				org.jdom.transform.JDOMResult result_tree = new org.jdom.transform.JDOMResult();
		
				scrub_xslt.getTransformer().transform(source_tree, result_tree);
				org.jdom.Element network_node = result_tree.getDocument().getRootElement();
		
				// use the results to update the network model
				scan.updateModel(network_node, "scan");
				scan.agentPostEvent("agent.receipt");

				// only direct the agent to disable mac scanning if ALL scans have completed
				// this scan should have gotten set to done in "scan.updateModel" above
				if (((org.opensims.model.ScanManager) scan.getManager()).areAllScansDone()) {			
					// disable the plugin from looping on further scans, until directed
					String command = "<DISABLE_PLUGINS><PLUGIN name=\"" + getName() + "\"/></DISABLE_PLUGINS>";
					getAgent().enqueueCommand(command, false);
					
					if (log_.isDebugEnabled()) {
						log_.debug("mac lookup - all scans done? true");
					}
				}
				else {
					if (log_.isDebugEnabled()) {
						log_.debug("mac lookup - all scans done? false");
					}
				}
			}
		}
		catch (Exception e) {
			log_.error("parse <MAC_LIST/>", e);
		}
    }
}
