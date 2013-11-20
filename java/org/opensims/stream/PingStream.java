/**
 * @LICENSE@
 */

package org.opensims.stream;

/**
 * $CVSId: PingStream.java,v 1.6 2004/10/30 08:23:44 paco Exp $
 * $Id: PingStream.java 1 2008-01-10 18:37:05Z smoot $
 * Stream for attacker/defender visualization
 * @version $Revision: 1.6 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Lindsey SIMON <lsimon@symbiot.com>
 */

public class
    PingStream
    extends org.opensims.stream.GenericStream
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.stream.PingStream.class.getName());


    //////////////////////////////////////////////////////////////////////
    // content methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Build a content document
     */

    public org.jdom.Document
	buildContentDocument (long tick)
    {
	org.jdom.Element content_node = new org.jdom.Element(org.opensims.xml.Node.PING_NODE);

	try {
	    long ping_cycle = getSubscriber().getPingPeriod() / 2L;

	    content_node.setAttribute("tick", String.valueOf(tick));

	    if (log_.isDebugEnabled()) {
		log_.debug("send ping - " + getSubscriber().getName() + " cycle " + ping_cycle);
	    }

	    org.jdom.Element status_node = getSubscriber().getStatusMessage();

	    if (status_node != null) {
		content_node.addContent(status_node);
	    }
	}
	catch (Exception e) {
	    log_.error("ping error - " + getSubscriber().getName(), e);
	}

	return new org.jdom.Document(content_node);
    }
}
