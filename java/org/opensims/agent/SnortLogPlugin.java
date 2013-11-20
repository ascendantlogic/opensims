/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: SnortLogPlugin.java,v 1.32 2006/01/27 21:57:38 mikee Exp $
 * $Id: SnortLogPlugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.32 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    SnortLogPlugin
    extends org.opensims.agent.LogWatcherPlugin
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.SnortLogPlugin.class.getName());


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
	    java.io.File xsl_file = new java.io.File(xsl_dir, "plugin_config_snort_log.xsl");

	    org.opensims.xml.XslTransform config_xslt = new org.opensims.xml.XslTransform(xsl_file);

	    org.jdom.transform.JDOMSource source_tree = new org.jdom.transform.JDOMSource(getAgent().getConfigNode());
	    org.jdom.transform.JDOMResult result_tree = new org.jdom.transform.JDOMResult();

	    String path = getCorrelator().getConfig(param);
	    config_xslt.getTransformer().setParameter("path", path);

	    config_xslt.getTransformer().transform(source_tree, result_tree);
	    org.jdom.Element config_node = result_tree.getDocument().getRootElement();

	    result = true;
	    getAgent().enqueueCommand(org.opensims.xml.XmlBuilder.formatXML(config_node, false, true), false);
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
     * Parse the incoming XML node sent from the agent.
     */

    public boolean
	parseNode (String node_name, org.jdom.Element node)
    {
	boolean valid_transact = false;

	if (node_name.equals(org.opensims.xml.Node.NIDS_NODE)) {
	    if (verifyScan()) {
		handleNids(node);
	    }

	    valid_transact = true;
	}

	return valid_transact;
    }


    /**
     * Parse an incoming <NIDS/> node from an agent.
     */

    protected void
	handleNids (org.jdom.Element node)
    {
	// <NIDS count="1" file="/var/log/snort/alert" platform="snort">

	String source = node.getAttributeValue("platform");
	String file_name = node.getAttributeValue("file");

	java.util.Iterator entry_iter = node.getChildren(org.opensims.xml.Node.ENTRY_NODE).iterator();

        while (entry_iter.hasNext()) {
	    try {
		// <ENTRY priority="" time="1074153822347">
		// <TYPE class="" desc="(spp_portscan2) Portscan detected from 67.107.81.195" id="117:1:1" />
		// <PACKET device="" proto="tcp">
		// <SRC ip="67.107.81.195" port="54044" />
		// <DST ip="67.107.81.198" port="1367" />

		if (log_.isDebugEnabled()) {
			log_.debug("Start handleNids: " + node);
		}

		org.jdom.Element entry_node = (org.jdom.Element) entry_iter.next();
		org.jdom.Element packet_node = entry_node.getChild(org.opensims.xml.Node.PACKET_NODE);

		org.jdom.Element src_node = packet_node.getChild(org.opensims.xml.Node.SRC_NODE);
		org.opensims.IPv4 src_ip_addr = new org.opensims.IPv4(src_node.getAttributeValue("ip"));

		// get the correct scan object to lookup the network
		org.opensims.model.Scan scan = getAgent().getManager().getCorrelator().getScanManager().getScanByIP(src_ip_addr.toString());

		// filtering for whitelisted src_host
		org.opensims.model.HostOrBogey src_host = scan.getHostOrBogey(src_ip_addr);
		
		if (log_.isDebugEnabled()) {
		    String host_id = null;

		    if (src_host != null) {
				host_id = src_host.getHostId();
		    }

		    log_.debug("src " + src_ip_addr.toString() + " - " + host_id);
		}

		if (src_host != null) {
		    // keep going... filter for dst_host

		    org.jdom.Element dst_node = packet_node.getChild(org.opensims.xml.Node.DST_NODE);
		    org.opensims.IPv4 dst_ip_addr = new org.opensims.IPv4(dst_node.getAttributeValue("ip"));

		    org.opensims.model.HostOrBogey dst_host = scan.getHostOrBogey(dst_ip_addr);

		    if (log_.isDebugEnabled()) {
			String host_id = null;
			
				if (dst_host != null) {
					host_id = dst_host.getHostId();
				}
				log_.debug("dst " + dst_ip_addr.toString() + " - " + host_id);
		    }

		    if (dst_host != null) {
			// keep going... determine if this alert is valid based on filter rules

			if (log_.isDebugEnabled()) {
				log_.debug("Checkpoint_1 handleNids: destination != null");
			}

			org.jdom.Element type_node = entry_node.getChild(org.opensims.xml.Node.TYPE_NODE);
			String id = type_node.getAttributeValue("id");

			// construct the IDS unique identifier; take only the first two numerics, leaving
			// the rest (with snort, the remainder is simply a revision number)

			StringBuffer buf = new StringBuffer(source);
			java.util.StringTokenizer st = new java.util.StringTokenizer(id, ":", false);

			for (int i = 0; st.hasMoreTokens(); i++) {
			    if (i < 2) {
				buf.append(":");
				buf.append(st.nextToken());
			    }
			    else {
				break;
			    }
			}

			String unique_id = buf.toString();
			String protocol = packet_node.getAttributeValue("proto");

			if (log_.isDebugEnabled()) {
				log_.debug("Checkpoint_2 handleNids: unique_id: " + unique_id);
			}

			String src_port = org.opensims.Config.fixNull(src_node.getAttributeValue("port"), "0").trim();

			if (src_port.equals("")) {
			    src_port = "0";
			}

			String dst_port = org.opensims.Config.fixNull(dst_node.getAttributeValue("port"), "0").trim();

			if (dst_port.equals("")) {
			    dst_port = "0";
			}

			/**
			 * @TODO Snort is full-on fubar for handling timezone/time change; do not trust its ticks

			long tick = getCorrelator().parseTick(entry_node.getAttributeValue("time"));

			 */

			long tick = getCorrelator().getTick();

			// attributes which aren't used in decisions:
			//String alert_class = type_node.getAttributeValue("class");
			//String desc = type_node.getAttributeValue("desc");
			//String device = packet_node.getAttributeValue("device");

			// attempt to correlate

			correlateAlert(source, unique_id, src_host, src_port, dst_host, dst_port, protocol, tick);
			
			// FOR DEBUG ONLY:
			// Artificially introduce a wait to simulate slow database access or calc crunch  --ME
			// getPluginThread().sleep(4000);
			
			if (log_.isDebugEnabled()) {
				log_.debug("Finish handleNids: " + node);
			}
		    }
		}
	    }
	    catch (Exception e) {
		log_.error("NIDS traffic", e);
	    }
	}
    }
}
