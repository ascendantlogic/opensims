/**
 * @LICENSE@
 */

package org.opensims.notify;

/**
 * $CVSId: XmppNotify.java,v 1.4 2004/10/02 17:16:37 paco Exp $
 * $Id: XmppNotify.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.4 $
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>
 * Notification adapter for XMPP protocl (Jabber)
 * </P>
 */

public class
    XmppNotify
    extends org.opensims.notify.GenericNotify
{
    // quality assurance

    private static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.notify.XmppNotify.class.getName());


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

	    sendXmpp(alert, method_node);
	    setLastTick(getManager().getCorrelator().getTick());
	}
    }


    /**
     * Send an XMPP (Jabber) message
     */

    protected void
	sendXmpp (org.opensims.alert.Alert alert, org.jdom.Element method_node)
    {
	try {
	    String xmpp_host = org.opensims.Config.fixNull(method_node.getAttributeValue("host"), "localhost");
	    int port = Integer.parseInt(org.opensims.Config.fixNull(method_node.getAttributeValue("port"), "0"));
	    boolean use_ssl = Boolean.valueOf(org.opensims.Config.fixNull(method_node.getAttributeValue("ssl"), "false")).booleanValue();
	    String user = org.opensims.Config.fixNull(method_node.getAttributeValue("user"), "nobody");
	    String pass = org.opensims.Config.fixNull(method_node.getAttributeValue("pass"), "guest");
	    String room = org.opensims.Config.fixNull(method_node.getAttributeValue("room"), "chat");
	    String msg_text = alert.getSummary();

	    // create a connection to the XMPP host

	    org.jivesoftware.smack.XMPPConnection con = null;

	    if (use_ssl) {
		con = new org.jivesoftware.smack.SSLXMPPConnection(xmpp_host, port);
	    }
	    else {
		con = new org.jivesoftware.smack.XMPPConnection(xmpp_host, port);
	    }

	    con.login(user, pass);

	    // join the group chat using a nickname, and send a
	    // message to all the other people in the chat room

	    org.jivesoftware.smack.GroupChat newGroupChat = con.createGroupChat(room);

	    newGroupChat.join(user);
	    newGroupChat.sendMessage(msg_text);
	}
	catch (Exception e) {
	    log_.error("sending XMPP - " + e.getMessage());
	}
    }
}
