/**
 * @LICENSE@
 */

package org.opensims.client;

/**
 * $CVSId: SubscriberManager.java,v 1.23 2005/04/05 14:36:57 dan Exp $
 * $Id: SubscriberManager.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.23 $
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>Kudos: we learned a lot from
 * http://www.shovemedia.com/multiserver/ and its excellent tutorial.</P>
 */

public class
    SubscriberManager
    extends org.opensims.Manager
{
    // protected fields

    protected java.util.Vector listeners = new java.util.Vector();
    protected java.util.Hashtable stream_xslt = new java.util.Hashtable();
    protected java.util.Hashtable stream_conf = new java.util.Hashtable();

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.client.SubscriberManager.class.getName());


    /**
     * Constructor
     */

    public
	SubscriberManager (org.opensims.Correlator correlator)
    {
	super(correlator, org.opensims.client.SubscriberManager.class.getName());

	try {
	    java.io.File xsl_dir = new java.io.File(getCorrelator().getConfig("webapp.dir"), "xsl");
	    java.util.Iterator listener_iter = getCorrelator().getConfig().getRootElement().getChildren(org.opensims.xml.Node.LISTENER_NODE).iterator();

	    // establish the listeners

	    while (listener_iter.hasNext()) {
		org.jdom.Element listener_node = (org.jdom.Element) listener_iter.next();
		int port = Integer.parseInt(listener_node.getAttributeValue("port"));

		// prepare the subscriber class

		org.jdom.Element subscriber_node = listener_node.getChild(org.opensims.xml.Node.SUBSCRIBER_NODE);
                String class_name = subscriber_node.getAttributeValue("class");
                Class subscriber_class = Class.forName(class_name);

		// instantiate the listener

		class_name = listener_node.getAttributeValue("class");
                Class c = Class.forName(class_name);
		org.opensims.client.Listener listener = (org.opensims.client.Listener) c.newInstance();
		
		if (log_.isDebugEnabled())
		{
			log_.debug("About to initialize listener on port " + port + " with subscriber class " + class_name);
		}

		listener.init(getCorrelator(), port, subscriber_class);
		listeners.add(listener);

		// load the XSL transforms, per available subscriber stream

		java.util.Iterator stream_iter = listener_node.getChildren(org.opensims.xml.Node.STREAM_NODE).iterator();

		while (stream_iter.hasNext()) {
		    org.jdom.Element stream_node = (org.jdom.Element) stream_iter.next();
		    String stream_name = stream_node.getAttributeValue("name");

		    java.io.File xsl_file = new java.io.File(xsl_dir, stream_node.getAttributeValue("transform"));
		    org.opensims.xml.XslTransform xslt = new org.opensims.xml.XslTransform(xsl_file);

		    if (log_.isDebugEnabled()) {
			log_.debug("load stream " + stream_name + " transform " + xslt + " from " + xsl_file);
		    }

		    stream_conf.put(stream_name, stream_node);
		    stream_xslt.put(stream_name, xslt);
		}
	    }
	}
	catch (Exception e) {
	    log_.error("load listeners and stream transforms", e);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Enumerate the listeners
     */

    public java.util.Enumeration
	getListeners ()
    {
	return listeners.elements();
    }


    //////////////////////////////////////////////////////////////////////
    // subscriber handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Create a new subscriber
     */

    public Subscriber
	createSubscriber (java.net.Socket socket, Class subscriber_class)
    {
	org.opensims.client.Subscriber subscriber = null;

	try {
	    if (log_.isDebugEnabled()) {
		log_.debug("connection accepted from " + socket.getInetAddress());
	    }

	    subscriber = (org.opensims.client.Subscriber) subscriber_class.newInstance();

	    subscriber.init(this, socket);
	    subscriber.start();
	}
	catch (Exception e) {
	    log_.error("creating subscriber", e);
	}

	return subscriber;
    }


    /**
     * Lookup a subscriber from the database
     */

    public org.opensims.client.Subscriber
	getSubscriber (org.opensims.IPv4 ip_addr, int port)
    {
	String key = org.opensims.client.GenericSubscriber.getKey(ip_addr, port);
	org.opensims.client.Subscriber subscriber = (org.opensims.client.Subscriber) get(key);

	return subscriber;
    }


    /**
     * Return an enumeration of the running subscribers
     */

    public synchronized java.util.Enumeration
	getSubscribers ()
    {
	return elements();
    }


    //////////////////////////////////////////////////////////////////////
    // stream management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the configuration node for the specified subscriber stream
     */

    public org.jdom.Element
	getStreamConfig (String stream_name)
    {
	org.jdom.Element stream_node = (org.jdom.Element) stream_conf.get(stream_name);

	return stream_node;
    }

    /**
     * Get the XSL transform for the specified subscriber stream
     */

    public org.opensims.xml.XslTransform
	getStreamTransform (String stream_name)
    {
	org.opensims.xml.XslTransform xslt = (org.opensims.xml.XslTransform) stream_xslt.get(stream_name);

	return xslt;
    }


    /**
     * Apply the XSL stylesheet as a transform for Plundereringing
     */

    public String
	plunderNode (org.jdom.Element node, String stream_name)
    {
	String plundered_text = "";

	try {
	    org.jdom.transform.JDOMSource source = new org.jdom.transform.JDOMSource(node);
	    java.io.ByteArrayOutputStream byte_out = new java.io.ByteArrayOutputStream();
	    javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(byte_out);

	    org.opensims.xml.XslTransform xslt = getStreamTransform(stream_name);

	    //xslt.transformer.setParameter("stream_name", stream_name.toUpperCase());
	    xslt.getTransformer().transform(source, result);
	    plundered_text = byte_out.toString();
	}
	catch (Exception e) {
	    log_.error("plunder node", e);
	}

	return plundered_text;
    }


    //////////////////////////////////////////////////////////////////////
    // sending messages
    //////////////////////////////////////////////////////////////////////

    /**
     * Push an XML message to the subscriber
     */

    public void
	pushMessage (org.opensims.client.Subscriber subscriber, org.jdom.Document xml_doc)
    {
	if (subscriber.getClientThread() != null) {
	    try {
		java.io.OutputStream out_stream = subscriber.getOutputStream();

		synchronized (out_stream) {
		    org.opensims.xml.XmlBuilder.xml_compact_outputter.output(xml_doc, out_stream);

		    out_stream.write(0);
		    out_stream.flush();
		}
	    }
	    catch (java.io.IOException e) {
		log_.warn("disconnect " + subscriber.getName() + " " + e.getMessage());
		subscriber.stop();
	    }
	}
    }


    /**
     * Push a text message to the subscriber
     */

    public void
	pushMessage (org.opensims.client.Subscriber subscriber, String text)
    {
	if (subscriber.getClientThread() != null) {
	    try {
		if (log_.isDebugEnabled()) {
		    log_.debug("push message: " + text);
		}

		java.io.OutputStream out_stream = subscriber.getOutputStream();

		synchronized (out_stream) {
		    out_stream.write(text.getBytes());
		    out_stream.write(0);
		    out_stream.flush();
		}
	    }
	    catch (java.io.IOException e) {
		log_.warn("disconnect " + subscriber.getName() + " " + e.getMessage());
		subscriber.stop();
	    }
	}
    }


    /**
     * Broadcast the XML to each subscriber -
     * optionally, EXCEPT the current one
     */

    public void
	broadcast (org.opensims.client.Subscriber self, org.jdom.Document xml_doc)
    {
	java.util.Enumeration e = getSubscribers();

	while (e.hasMoreElements()) {
	    org.opensims.client.Subscriber subscriber = (org.opensims.client.Subscriber) e.nextElement();

	    if (subscriber != self) {
		pushMessage(subscriber, xml_doc);
	    }
	}
    }


    /**
     * Broadcast the XML to a specified subscriber
     */

    public void
	sendToId (org.jdom.Document xml_doc, String dst_id, String src_id)
    {
	java.util.Enumeration e = getSubscribers();

	while (e.hasMoreElements()) {
	    org.opensims.client.Subscriber subscriber = (org.opensims.client.Subscriber) e.nextElement();

	    if (subscriber.getName().equals(dst_id)) {
		pushMessage(subscriber, xml_doc);
	    }
	}
    }


    /**
     * Broadcast a status message to each client
     */

    public void
	broadcastStatus (int rank, boolean reboot_flag, boolean modal_flag, int error_code, String message, String url)
    {
	java.util.Enumeration e = getSubscribers();

	while (e.hasMoreElements()) {
	    org.opensims.client.Subscriber subscriber = (org.opensims.client.Subscriber) e.nextElement();

	    subscriber.addStatusMessage(rank, reboot_flag, modal_flag, error_code, message, url);
	}
    }
}
