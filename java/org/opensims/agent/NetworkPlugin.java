/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: NetworkPlugin.java,v 1.6 2004/10/01 05:27:53 paco Exp $
 * $Id: NetworkPlugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.6 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    NetworkPlugin
    extends org.opensims.agent.GenericPlugin
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.NetworkPlugin.class.getName());


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

	if (node_name.equals(org.opensims.xml.Node.NET_TRAFFIC_NODE)) {
	    handleNetTraffic(node);
	    valid_transact = true;
	}

	return valid_transact;
    }


    /**
     * Parse an incoming <NET_TRAFFIC/> node from an Agent.
     *
     * @TODO get the passive autodiscovery and network traffic modeling back online.
     */

    protected void
	handleNetTraffic (org.jdom.Element node)
    {
	/*
 <NET_TRAFFIC byte_count="1440" device="en0" packet_count="30" ptime_end="1076507704550" ptime_start="1076507704548" unknown_count="0">
  <PACKET family="IPv4" packet_count="1" proto="icmp" service="">
   <SRC ip="205.238.131.194" mac_id="00:0A:95:6C:2C:6E" />
   <DST ip="172.16.1.0" mac_id="00:D0:58:6C:70:B8" />
  </PACKET>
  <PACKET family="IPv4" packet_count="1" proto="tcp" service="http">
   <SRC ip="205.238.131.194" mac_id="00:0A:95:6C:2C:6E" port="48425" />
   <DST ip="172.16.1.10" mac_id="00:D0:58:6C:70:B8" port="80" />
  </PACKET>
	 */

        if (log_.isDebugEnabled()) {
            log_.debug(org.opensims.xml.XmlBuilder.formatXML(node, false, true));
        }

	/*
	try {
	    String packet_count = node.getAttributeValue("packet_count");
	    java.math.BigDecimal last_tick = new java.math.BigDecimal(node.getAttributeValue("ptime_end"));

	    if (log_.isInfoEnabled()) {
		log_.info("sniff - pkts " + packet_count);
	    }

	    java.util.Iterator pkt_iter = node.getChildren("PACKET").iterator();

	    while (pkt_iter.hasNext()) {
		org.jdom.Element pkt_node = (org.jdom.Element) pkt_iter.next();
		String protocol = pkt_node.getAttributeValue("proto");

		org.jdom.Element src_node = pkt_node.getChild("SRC");
		String src_ip_addr = src_node.getAttributeValue("ip");
		String src_port_str = src_node.getAttributeValue("port");

		if (src_port_str == null) {
		    src_port_str = "0";
		}

		java.math.BigDecimal src_port = new java.math.BigDecimal(src_port_str);

		org.jdom.Element dst_node = pkt_node.getChild("DST");
		String dst_ip_addr = dst_node.getAttributeValue("ip");
		String dst_port_str = dst_node.getAttributeValue("port");

		if (dst_port_str == null) {
		    dst_port_str = "0";
		}

		java.math.BigDecimal dst_port = new java.math.BigDecimal(dst_port_str);

		if ((src_ip_addr != null) &&
		    (dst_ip_addr != null)
		    ) {
		    connection_p.logConnection(src_ip_addr, src_port, dst_ip_addr, dst_port, protocol.substring(0, 1).toUpperCase(), last_tick);
		    connection_p.getConnection().commit();
		}
	    }
	}
	catch (Exception e) {
	    log_.error("network traffic", e);
	}
	*/
    }
}
