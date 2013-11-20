/**
 * @LICENSE@
 */

package org.opensims.notify;

/**
 * $CVSId: HttpNotify.java,v 1.4 2004/10/02 17:16:37 paco Exp $
 * $Id: HttpNotify.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.4 $
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>
 * Notification adapter for HTTP protocol (web page "POST")
 * </P>
 */

public class
    HttpNotify
    extends org.opensims.notify.GenericNotify
{
    // quality assurance

    private static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.notify.HttpNotify.class.getName());


    //////////////////////////////////////////////////////////////////////
    // alert handling methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Send the given alert
     */

    public void
	sendAlert (org.opensims.alert.Alert alert)
    {
	// get the config specific to the notification method

	java.util.Iterator method_iter = getNotifyNode().getChildren().iterator();

	if (method_iter.hasNext()) {
	    org.jdom.Element method_node = (org.jdom.Element) method_iter.next();

	    sendHttp(alert, method_node);
	    setLastTick(getManager().getCorrelator().getTick());
	}
    }


    /**
     * Send an HTTP (web page "POST") message
     */

    protected void
	sendHttp (org.opensims.alert.Alert alert, org.jdom.Element method_node)
    {
	try {
	    String http_url = org.opensims.Config.fixNull(method_node.getAttributeValue("url"), "http://localhost/");
	    String msg_text = alert.getSummary();

	    /**
	     * @TODO these params belong in the config <NOTIFY/> node
	     */

	    org.jdom.Element event_node = alert.getExportNode();
	    event_node.setAttribute("node", getManager().getCorrelator().getName());

	    String trust_store = "/var/ssl/symbiot.ks";

	    System.setProperty("javax.net.ssl.trustStore", trust_store);
	    System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

	    // open an http connection to the target server

	    java.net.URL url = new java.net.URL(http_url);
	    java.net.HttpURLConnection http_con = (java.net.HttpURLConnection) url.openConnection();

            // set the appropriate HTTP parameters

            http_con.setRequestMethod("POST");
            http_con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            http_con.setDoOutput(true);
	    http_con.connect();

            // post the XML message

            java.io.OutputStream out = http_con.getOutputStream();
	    String event_xml = org.opensims.xml.XmlBuilder.formatXML(event_node, true, false);

            out.write(event_xml.getBytes());
	    out.flush();
            out.close();

	    http_con.getResponseCode();
	    http_con.disconnect();
	}
	catch (Exception e) {
	    log_.error("sending HTTP - " + e.getMessage());
	}
    }
}
