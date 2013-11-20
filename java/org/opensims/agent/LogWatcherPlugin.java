/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: LogWatcherPlugin.java,v 1.15 2007/02/20 00:42:36 jeff Exp $
 * $Id: LogWatcherPlugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.15 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    LogWatcherPlugin
    extends org.opensims.agent.GenericPlugin
{
    // public definitions
    
    public final static int default_regex_pattern_flags =
		java.util.regex.Pattern.CANON_EQ |
		java.util.regex.Pattern.CASE_INSENSITIVE |
		java.util.regex.Pattern.COMMENTS |
		java.util.regex.Pattern.DOTALL |
		java.util.regex.Pattern.UNICODE_CASE;
	
	// Nagios
	
    public final static String nagios_pattern_regex = ".*\\[(\\d+)\\]\\s+SERVICE;([\\d\\.]+);.*_(\\d+);.*";
	
	// PIX syslog
	
	public final static String pix_syslog_pattern_regex = "(\\w+\\s+\\d+\\s+[\\d\\:]+)\\s+([\\d\\.]+)/([\\d\\.]+)\\s+(\\%PIX-[\\-\\d]+)\\:\\s+(\\w+)\\s+(\\w+)\\s+src\\s+\\w+\\:([\\d\\.]+)/(\\d+)\\s+dst\\s+\\w+\\:([\\d\\.]+)/(\\d+).*";
	
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.LogWatcherPlugin.class.getName());


    //////////////////////////////////////////////////////////////////////
    // parsing methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse the incoming XML node sent from the agent
     */

    public boolean
	parseNode (String node_name, org.jdom.Element node)
    {
	boolean valid_transact = false;

	if (node_name.equals(org.opensims.xml.Node.LOG_ALERT_NODE)) {
	    if (verifyScan()) {
		handleLogAlert(node);
	    }

	    valid_transact = true;
	}

	return valid_transact;
    }


    /**
     * This plugin is among a class which run independent of the
     * autodiscovery process ("static enable") ... so we handle its
     * configuration differently.
     */

    protected boolean
	verifyScan ()
    {
	boolean result = false;

	if (getScan() == null) {
	    setScan(getCorrelator().getScanManager().getScan(getAgent()));

	    if (getScan() == null) {
		// ignore the incoming events, to avoid data quality errors
	    }
	    else {
		result = true;
	    }
	}
	else {
	    result = true;
	}

	return result;
    }


    /**
     * Parse an incoming <LOG_ALERT/> node from an Agent.
     */

    protected void
	handleLogAlert (org.jdom.Element node)
    {
	/*
<LOG_ALERT count="5">
  <ENTRY ref="nagios" text="# Nagios 1.2 Status File" />
  <ENTRY ref="nagios" text="[1096061375] PROGRAM;1096048985;29144;1;0;0;0;1;1;1;0;0;1;0" />
  <ENTRY ref="nagios" text="[1096061375] HOST;172.16.1.8;DOWN;1096061245;1096049855;0;852;0;0;0;0;0;0;1;0;0;0.00;0;1;1;/bin/ping -n -U -c 1 172.16.1.8" />
  <ENTRY ref="nagios" text="[1096061375] SERVICE;172.16.1.8;ajp13_8009;CRITICAL;1/3;HARD;1096061193;1096061373;ACTIVE;1;1;0;1096049855;0;CRITICAL;722;0;0;0;0;0;1;0;0;0;0;0.00;0;1;1;1;Connection refused by host" />
  <ENTRY ref="nagios" text="[1096061375] SERVICE;172.16.1.8;msdtc_8443;CRITICAL;1/3;HARD;1096061213;1096061393;ACTIVE;1;1;0;1096049875;0;CRITICAL;723;0;0;0;0;0;1;0;0;0;0;0.00;0;1;1;1;Connection refused by host" />
</LOG_ALERT>

			# # #

Nagios status log file format for SERVICE:

[Time of last update] SERVICE;
Host Name (string);
Service Description (string);
Status (OK/WARNING/CRITICAL/UNKNOWN);
Retry number (#/#);
State Type (SOFT/HARD);
Last check time (long time);
Next check time (long time);
Check type (ACTIVE/PASSIVE);
Checks enabled (0/1);
Accept Passive Checks (0/1);
Event Handlers Enabled (0/1);
Last state change (long time);
Problem acknowledged (0/1);
Last Hard State (OK/WARNING/CRITICAL/UNKNOWN);
Time OK (long time);
Time Unknown (long time);
Time Warning (long time);
Time Critical (long time);
Last Notification Time (long time);
Current Notification Number (#);
Notifications Enabled (0/1);
Latency (#);
Execution Time (#);
Flap Detection Enabled (0/1);
Service is Flapping (0/1);
Percent State Change (###.##);
Scheduled Downtime Depth (#);
Failure Prediction Enabled (0/1);
Process Performance Date (0/1);
Obsess Over Service (0/1);
Plugin Output (string)

	*/

	try {
	    if (log_.isDebugEnabled()) {
		log_.debug(org.opensims.xml.XmlBuilder.formatXML(node, false, true));
	    }

	    java.util.Iterator entry_iter = node.getChildren(org.opensims.xml.Node.ENTRY_NODE).iterator();

	    while (entry_iter.hasNext()) {
		org.jdom.Element entry_node = (org.jdom.Element) entry_iter.next();

		String ref = org.opensims.Config.fixNull(entry_node.getAttributeValue("ref"), "");
		String text = org.opensims.Config.fixNull(entry_node.getAttributeValue("text"), "");

		if (ref.equals("nagios")) {
		    if (log_.isDebugEnabled()) {
			log_.debug("ref: |" + ref + "| = |" + text + "|");
		    }

		    // [1096061375] SERVICE;172.16.1.8;ajp13_8009;CRITICAL

		    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(nagios_pattern_regex, default_regex_pattern_flags);
		    java.util.regex.Matcher matcher = pattern.matcher(text);

		    if (matcher.matches()) {
			long tick = getCorrelator().parseTick(matcher.group(1) + "000");
			org.opensims.IPv4 dst_ip_addr = new org.opensims.IPv4(matcher.group(2));
			org.opensims.model.HostOrBogey dst_host = getScan().getHostOrBogey(dst_ip_addr);
			String dst_port = matcher.group(3);

			if (log_.isDebugEnabled()) {
			    log_.debug("match: " + tick + " - " + dst_ip_addr.toString() + " - " + dst_port);
			}

			String source = "nagios";
			String unique_id = "nagios:1:1";
			String protocol = "tcp"; // this may change?

			// attempt to correlate

			/**
			 * @TODO notify the ScanManager to update the network model for service availability
			org.opensims.alert.Alert alert = correlateAlert(source, unique_id, dst_host, dst_port, dst_host, dst_port, protocol, tick);

			if (alert != null) {
			    // put it through the state machine
			}
			 */
		    }
		}
		else if (ref.equals("pix_syslog"))
		{
			if (log_.isDebugEnabled())
				log_.debug("ref: |" + ref + "| = |" + text + "|");
			
			// May 16 05:18:47 192.168.1.1/192.168.1.1 %PIX-4-106023: Deny tcp src outside:216.110.110.66/4220 dst inside:216.110.44.248/135 by access-group "101"
			// 111111111111111 22222222222 33333333333 4444444444444  5555 666             77777777777777 8888            99999999999999 000
			
			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(pix_syslog_pattern_regex,default_regex_pattern_flags);
		    java.util.regex.Matcher matcher = pattern.matcher(text);
		    
		    if (matcher.matches())
		    {
		    	// Extract the source of the infraction
		    	org.opensims.IPv4 src_ip_addr = new org.opensims.IPv4(matcher.group(7));
		    	String src_port = matcher.group(8);
		    	
		    	// Determine if the source is a host or bogey
		    	org.opensims.model.HostOrBogey src_host = getScan().getHostOrBogey(src_ip_addr);
		    	
		    	// Extract the target
		    	org.opensims.IPv4 dst_ip_addr = new org.opensims.IPv4(matcher.group(9));
		    	String dst_port = matcher.group(10);
		    	
		    	// Determine if the target is a host or bogey
		    	org.opensims.model.HostOrBogey dst_host = getScan().getHostOrBogey(dst_ip_addr);
		    	
		    	// Other interesting information
		    	String source = "PIX";
		    	String unique_id = "pix:1:1";			// Using this to match up with a generic Snort alert definition
		    	String protocol = matcher.group(6);
		    	
		    	// We should actually get the tick from PIX event datetime stamp
		    	long tick = getCorrelator().getTick();
		    	
		    	// attempt to correlate
		    	correlateAlert(source,unique_id,src_host,src_port,dst_host,dst_port,protocol,tick);
		    }
		    else if (log_.isDebugEnabled())
		    {
		    	log_.debug("ref: |" + ref + "| does not have a matching pattern");
		    }
		}
	    }
	}
	catch (Exception e) {
	    log_.error("parse log entries", e);
	}
    }


    /**
     * Attempt to correlate an alert.
     */

    protected org.opensims.alert.Alert
        correlateAlert (String source, String unique_id, org.opensims.model.HostOrBogey src_host, String src_port, org.opensims.model.HostOrBogey dst_host, String dst_port, String protocol, long tick)
	throws Exception
    {
	org.opensims.alert.Alert alert = getCorrelator().correlateAlert(getAgent(), source, unique_id, src_host, src_port, dst_host, dst_port, protocol, tick);

	if ( alert != null ) {
	    if (log_.isDebugEnabled()) {
			log_.debug("correlate alert " + alert.getAlertId());
		}
	}

	return alert;
    }
}
