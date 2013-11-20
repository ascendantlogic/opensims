/**
 * @LICENSE@
 */

package org.opensims.notify;

/**
 * $CVSId: SmtpNotify.java,v 1.4 2004/10/02 17:16:37 paco Exp $
 * $Id: SmtpNotify.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.4 $
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>
 * Notification adapter for SMTP protocol (email)
 * </P>
 */

public class
    SmtpNotify
    extends org.opensims.notify.GenericNotify
{
    // quality assurance

    private static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.notify.SmtpNotify.class.getName());


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

	    sendSmtp(alert, method_node);
	    setLastTick(getManager().getCorrelator().getTick());
	}
    }


    /**
     * Send an SMTP (email) message
     */

    protected void
	sendSmtp (org.opensims.alert.Alert alert, org.jdom.Element method_node)
    {
	try {
	    String to_addr = org.opensims.Config.fixNull(method_node.getAttributeValue("to"), "nobody@localhost");
	    String from_addr = org.opensims.Config.fixNull(method_node.getAttributeValue("from"), "nobody@localhost");
	    String smtp_host = org.opensims.Config.fixNull(method_node.getAttributeValue("host"), "localhost");
	    String subj_line = org.opensims.Config.fixNull(method_node.getAttributeValue("subject"), "iSIMS ruh roh");
	    String msg_text = alert.getSummary();

	    // create some properties and get the default Session

	    java.util.Properties props = new java.util.Properties();
	    props.put("mail.smtp.host", smtp_host);

	    javax.mail.Session session = javax.mail.Session.getInstance(props, null);
	
	    // create a message

	    javax.mail.Message msg = new javax.mail.internet.MimeMessage(session);
	    msg.setFrom(new javax.mail.internet.InternetAddress(from_addr));

	    javax.mail.internet.InternetAddress[] address = {new javax.mail.internet.InternetAddress(to_addr)};
	    msg.setRecipients(javax.mail.Message.RecipientType.TO, address);
	    msg.setSubject(subj_line);
	    msg.setSentDate(new java.util.Date());

	    // if the desired charset is known, use:
	    // setText(text, charset)

	    msg.setText(msg_text);
	    javax.mail.Transport.send(msg);
	}
	catch (Exception e) {
	    log_.error("sending SMTP - " + e.getMessage());
	}
    }
}
