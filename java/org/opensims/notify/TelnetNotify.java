/**
 * @LICENSE@
 */

package org.opensims.notify;

/**
 * $CVSId: TelnetNotify.java,v 1.4 2004/10/02 17:16:37 paco Exp $
 * $Id: TelnetNotify.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.4 $
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>
 * Notification adapter for a simple text-based telnet listener
 * </P>
 */

public class
    TelnetNotify
    extends org.opensims.notify.GenericNotify
{
    // quality assurance

    private static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.notify.TelnetNotify.class.getName());


    //////////////////////////////////////////////////////////////////////
    // alert handling methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Send the given alert
     */

    public void
	sendAlert (org.opensims.alert.Alert alert)
    {
	//  get the config specific to the notification method

	java.util.Iterator method_iter = getNotifyNode().getChildren().iterator();

	if (method_iter.hasNext()) {
	    org.jdom.Element method_node = (org.jdom.Element) method_iter.next();

	    sendTelnet(alert, method_node);
	    setLastTick(getManager().getCorrelator().getTick());
	}
    }


    /**
     * Send an XMPP (Jabber) message
     */

    protected void
	sendTelnet (org.opensims.alert.Alert alert, org.jdom.Element method_node)
    {
	try {
	    // create a connection to the Telnet listener

	    java.net.InetAddress host = java.net.InetAddress.getByName(org.opensims.Config.fixNull(method_node.getAttributeValue("host"), "localhost"));
	    int port = Integer.parseInt(org.opensims.Config.fixNull(method_node.getAttributeValue("port"), "0"));
	    java.net.Socket socket = new java.net.Socket(host, port);
	    java.io.PrintWriter out = new java.io.PrintWriter(socket.getOutputStream());

	    out.println(alert.getSummary());
	    out.flush();

	    socket.close();
	}
	catch (Exception e) {
	    log_.error("sending Telnet - " + e.getMessage());
	}
    }
}
