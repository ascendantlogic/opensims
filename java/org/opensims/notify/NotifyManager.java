/**
 * @LICENSE@
 */

package org.opensims.notify;

/**
 * $CVSId: NotifyManager.java,v 1.10 2004/10/12 19:28:43 paco Exp $
 * $Id: NotifyManager.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.10 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    NotifyManager
    extends org.opensims.Manager
{
    // quality assurance

    private static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.notify.NotifyManager.class.getName());


    /**
     * Constructor
     */

    public
	NotifyManager (org.opensims.Correlator correlator)
    {
	super(correlator, org.opensims.notify.NotifyManager.class.getName());

	// instantiate a notification entry for each <NOTIFY/> node in
	// the config document

	try {
	    java.util.Iterator notify_iter = getCorrelator().getConfig().getRootElement().getChildren(org.opensims.xml.Node.NOTIFY_NODE).iterator();

	    while (notify_iter.hasNext()) {
		org.jdom.Element notify_node = (org.jdom.Element) notify_iter.next();

		if (log_.isDebugEnabled()) {
		    log_.debug("notify " + org.opensims.xml.XmlBuilder.formatXML(notify_node, false, true));
		}

		createNotify(notify_node);
	    }
	}
	catch (Exception e) {
	    log_.error("loading notify rules", e);
	}
    }


    /**
     * Create a new notify
     */

    public org.opensims.notify.Notify
	createNotify (org.jdom.Element notify_node)
    {
	org.opensims.notify.Notify notify = null;

        try {
            String class_name = notify_node.getAttributeValue("class");
            Class c = Class.forName(class_name);

            notify = (org.opensims.notify.Notify) c.newInstance();
	    notify.init(this, notify_node);

	    if (log_.isDebugEnabled()) {
		log_.debug("load notify instance: " + notify.getKey() + " enabled " + notify.getEnabled());
	    }

	    put(notify.getKey(), notify);
        }
        catch (Exception e) {
            log_.error("instantiate notify class", e);
        }

	return notify;
    }


    //////////////////////////////////////////////////////////////////////
    // decision support
    //////////////////////////////////////////////////////////////////////

    /**
     * Consider the given alert for notification
     *
     * @TODO ready for a UML activity diagram/statechart
     */

    public synchronized void
	consider (org.opensims.alert.Alert alert)
    {
        for (java.util.Enumeration e = elements(); e.hasMoreElements(); ) {
	    org.opensims.notify.Notify notify = (org.opensims.notify.Notify) e.nextElement();

	    notify.testTrigger(alert);
	}	
    }
}
