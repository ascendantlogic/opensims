/**
 * @LICENSE@
 */

package org.opensims.client;

/**
 * $CVSId: GenericSubscriber.java,v 1.33 2006/12/25 19:51:09 mikee Exp $
 * $Id: GenericSubscriber.java 1 2008-01-10 18:37:05Z smoot $
 * Flash XMLSocket subscriber
 * @version $Revision: 1.33 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Lindsey SIMON <lsimon@symbiot.com>
 * @author Linas VARDYS <linas@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 *
 * We learned a lot from: http://www.shovemedia.com/multiserver/
 * ...and even more from: http://www.dagblastit.com/java/sockets.html
 */

public class
    GenericSubscriber
    extends Object
    implements org.opensims.client.Subscriber
{
    // public definitions

    public final static int XMLSOCKET_BUF_MAX = 1024;
    public final static int XMLSOCKET_QUIESCE_PERIOD = 20;
    public final static int MAX_NETS_TO_DISPLAY = 2;

    // protected fields

    protected org.opensims.Manager manager = null;
    protected java.net.Socket socket = null;
    protected java.io.InputStream input_stream = null;
    protected java.io.OutputStream output_stream = null;
    protected org.opensims.IPv4 ip_addr = null;
    protected String language = "en";
    protected boolean plunder_flag = true;
    protected boolean first_data = true;
    protected java.util.Timer timer = null;
    protected volatile Thread client_thread = null;
    protected long last_tick = 0L;
    protected long ping_period = 0L;
    protected java.util.Hashtable resource_handler = new java.util.Hashtable();
    protected java.util.Hashtable stream_table = null;
    protected org.opensims.servlet.Login login = null;
    protected org.jdom.Element notify_node = new org.jdom.Element(org.opensims.xml.Node.NOTIFY_NODE);

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.client.GenericSubscriber.class.getName());

	private	int last_status_index = 0;

    /**
     * Initialize the subscriber object
     */

    public void
	init (org.opensims.client.SubscriberManager subscriber_manager, java.net.Socket socket)
	{
		try {
			setManager(subscriber_manager);
			setSocket(socket);
	
			getSocket().setTcpNoDelay(true);
			getSocket().setSoTimeout(SO_TIMEOUT);
	
			setInputStream(getSocket().getInputStream());
			setOutputStream(getSocket().getOutputStream());
	
			setIPv4(new org.opensims.IPv4(getSocket().getInetAddress().getHostAddress()));
			setStreamTable(new java.util.Hashtable());
			setPingPeriod(Long.parseLong(getManager().getCorrelator().getConfig("ping.period")));
	
			if (log_.isInfoEnabled()) {
				log_.info("connect " + getIPv4().toString() + ":" + getSocket().getPort() + " - local:" + getSocket().getLocalPort());
			}
	
			setPlunderFlag(true);
			setFirstData(true);
	
			// register the resource handlers
	
			registerResourceHandler("policy-file-request", "handleRequestPolicy");
			registerResourceHandler(org.opensims.xml.Node.PONG_NODE, "handleRequestPong");
			registerResourceHandler(org.opensims.xml.Node.CONNECT_NODE, "handleRequestConnect");
			registerResourceHandler(org.opensims.xml.Node.DISCONNECT_NODE, "handleRequestDisconnect");
			registerResourceHandler(org.opensims.xml.Node.SUBSCRIBE_NODE, "handleRequestSubscribe");
			registerResourceHandler(org.opensims.xml.Node.UNSUBSCRIBE_NODE, "handleRequestUnsubscribe");
		}
		catch (Exception e) {
			log_.error("init subscriber", e);
		}
	}


    /**
     * Represent as a String
     */

    public String
	getSummary ()
    {
		StringBuffer buf = new StringBuffer();
	
		buf.append(getName());
		buf.append("\n\t last_tick: ");
		buf.append(getManager().getCorrelator().getTick() - getLastTick());
	
		return buf.toString();
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the IPv4 address.
     */

    public org.opensims.IPv4
	getIPv4 ()
    {
		return ip_addr;
    }


    /**
     * Set the IPv4 address.
     */

    public void
	setIPv4 (org.opensims.IPv4 ip_addr)
    {
		this.ip_addr = ip_addr;
    }


    /**
     * Get the language (ISO 639) tag.
     * http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt
     */

    public String
	getLanguage ()
    {
		return language;
    }


    /**
     * Set the language (ISO 639) tag.
     */

    public void
	setLanguage (String language)
    {
		this.language = language;
    }


    /**
     * Get the plunder flag (convert XML to linear format).
     */

    public boolean
	getPlunderFlag ()
    {
		return plunder_flag;
    }


    /**
     * Set the plunder flag (convert XML to linear format).
     */

    public void
	setPlunderFlag (boolean plunder_flag)
    {
		this.plunder_flag = plunder_flag;
    }


    /**
     * Get the first data flag (not received a VIZ packet yet).
     */

    public boolean
	getFirstData ()
    {
		return first_data;
    }


    /**
     * Set the first data flag (not received a VIZ packet yet).
     */

    public void
	setFirstData (boolean first_data)
    {
		this.first_data = first_data;
    }


    /**
     * Get the Timer.
     */

    public java.util.Timer
	getTimer ()
    {
		return timer;
    }


    /**
     * Set the Timer.
     */

    public void
	setTimer (java.util.Timer timer)
    {
		this.timer = timer;
    }


    /**
     * Get the thread name.
     */

    public String
	getName ()
    {
		String name = getKey();
	
		if (getClientThread() != null) {
			name = getClientThread().getName();
		}
	
		return name;
    }


    //////////////////////////////////////////////////////////////////////
    // Tickable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Manager
     */

    public org.opensims.Manager
    getManager ()
    {
		return manager;
    }


    /**
     * Set the Manager
     */

    public void
    setManager (org.opensims.Manager manager)
    {
		this.manager = manager;
    }


    /**
     * Get the last_tick value - for expiry
     */

    public long
	getLastTick ()
    {
		return last_tick;
    }


    /**
     * Set the last_tick value - for expiry
     */

    public void
	setLastTick (long last_tick)
    {
		this.last_tick = last_tick;
    }


    /**
     * Get the hashtable lookup key
     */

    public static String
	getKey (org.opensims.IPv4 ip_addr, int port)
    {
		return ip_addr.toString() + ":" + port;
    }


    /**
     * Get the hashtable lookup key
     */

    public String
	getKey ()
    {
		return getIPv4().toString() + ":" + getSocket().getPort();
    }


    /**
     * Test whether a <PING/> has been received recently enough to
     * keep the subscriber alive
     */

    public boolean
	checkExpiry (long model_tick)
    {
        boolean result = false;

		if (getPingInterval() > getPingPeriod()) {
		    stop();
	    	shutdownSocket();
            result = true;
        }
        return result;
    }


    //////////////////////////////////////////////////////////////////////
    // Runnable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the client thread.
     */

    public Thread
	getClientThread ()
    {
		return client_thread;
    }


    /**
     * Set the client thread.
     */

    public void
	setClientThread (Thread client_thread)
    {
		this.client_thread = client_thread;
    }


    /**
     * Start the connection running.
     */

    public void
	start ()
	{
		setTimer(new java.util.Timer(true));
		setClientThread(new Thread(this, "sub " + hashCode() + " - " + getKey()));
	
		synchronized (getManager()) {
			getManager().put(getKey(), this);
		}
	
		getClientThread().start();
	
		if (log_.isInfoEnabled()) {
			log_.info("start " + getName());
		}
	}


    /**
     * If at any point we are unable to communicate with a socket,
     * stop() is called to remove this subscriber
     */

    public void
	stop ()
	{
		if (log_.isInfoEnabled()) {
			log_.info("stop " + getName());
		}
	
		synchronized (getManager()) {
			getManager().remove(getKey());
		}
	
		getTimer().cancel();
		setClientThread(null);
	}


    /**
     * Runnable interface
     */

    public void
	run ()
	{
		try {	
			Thread this_thread = Thread.currentThread();
	
			if (log_.isInfoEnabled()) {
				log_.info("new connection - " + getName());
			}
	
			// loop to parse incoming messages
			readMessages(this_thread);
		}
		catch (java.lang.NoClassDefFoundError e) {
			// WebappClassLoader: Lifecycle error : CL stopped
			// java.lang.NoClassDefFoundError: org/xml/sax/helpers/XMLReaderFactory
	
			log_.warn("NOTE: servlet seems to have restarted, old connection severed for " + getName());
	
			stop();
		}
		catch (org.jdom.input.JDOMParseException e) {
			log_.warn("NOTE: XML parse exception - probably being scanned by " + getName());
	
			stop();
		}
		catch (org.xml.sax.SAXParseException e) {
			log_.warn("NOTE: XML parse exception - probably being scanned by " + getName());
	
			stop();
		}
		catch (java.lang.InterruptedException e) {
			if (log_.isDebugEnabled()) {
				log_.error("subscriber " + getName(), e);
			}
	
			stop();
		} 
		catch (java.io.InterruptedIOException e) {
			if (log_.isDebugEnabled()) {
				log_.debug("subscriber " + getName(), e);
			}
	
			stop();
		} 
		catch (java.io.IOException e) {
			if (log_.isDebugEnabled()) {
				log_.debug("subscriber " + getName(), e);
			}
	
			stop();
		}
		finally {
			shutdownSocket();
		}
	}


    //////////////////////////////////////////////////////////////////////
    // socket handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the socket.
     */

    public java.net.Socket
	getSocket ()
    {
		return socket;
    }


    /**
     * Set the socket.
     */

    public void
	setSocket (java.net.Socket socket)
    {
		this.socket = socket;
    }


    /**
     * Get the input stream.
     */

    public java.io.InputStream
	getInputStream ()
    {
		return input_stream;
    }


    /**
     * Set the input stream.
     */

    public void
	setInputStream (java.io.InputStream input_stream)
    {
		this.input_stream = input_stream;
    }


    /**
     * Get the output stream.
     */

    public java.io.OutputStream
	getOutputStream ()
    {
		return output_stream;
    }


    /**
     * Set the output stream.
     */

    public void
	setOutputStream (java.io.OutputStream output_stream)
    {
		this.output_stream = output_stream;
    }


    /**
     * Loop to read and parse input from the subscriber.  Flash uses a
     * rather quirky protocol for its "XMLSocket" connections, based
     * on a zero-byte EOM delimiter.
     */

    protected void
	readMessages (Thread this_thread)
	throws java.io.IOException, java.io.InterruptedIOException,	java.lang.InterruptedException,	org.xml.sax.SAXParseException, org.jdom.input.JDOMParseException
	{
		StringBuffer bytes_so_far = new StringBuffer();
		byte[] buf = new byte[XMLSOCKET_BUF_MAX];
	
		this_thread.setPriority(Thread.MAX_PRIORITY - 1);
	
		while (this_thread == getClientThread()) {
			// collect all the bytes waiting on the input stream
	
			Thread.sleep(XMLSOCKET_QUIESCE_PERIOD);
	
			int avail = getInputStream().available();
	
			while (avail > 0) {
			int amount = avail;
	
			if (amount > buf.length) {
				amount = buf.length;
			}
	
			amount = getInputStream().read(buf, 0, amount);
	
			int marker = 0;
	
			for (int j = 0; j < amount; j++) {
				// scan for the zero-byte EOM delimiter
	
				if (buf[j] == (byte) 0) {
				String tmp = new String(buf, marker, j - marker);
				bytes_so_far.append(tmp);
	
				/**
				 * @TODO too much
				if (log_.isInfoEnabled()) {
					log_.info("in |" + bytes_so_far.toString() + "|");
				}
				*/
	
				parseRequest(new java.io.ByteArrayInputStream(bytes_so_far.toString().getBytes()));
	
				bytes_so_far.setLength(0);
				marker = j + 1;
				}
			}
	
			if (marker < amount) {
				// save all so far, still waiting for that final EOM
	
				bytes_so_far.append(new String(buf, marker, amount - marker));
			}
	
			avail = getInputStream().available();
			}
		}
	}


    /**
     * Shutdown the socket connection
     */

    protected void
	shutdownSocket ()
	{
		try {
			getManager().getCorrelator().getServlet().getNeuromancer().setLogin(login.getLoginName(), "false");
			// getManager().getCorrelator().getServlet().appendUserLog(login.getLoginName(), "", "Subscriber logout");
			
			// just to be sure
			synchronized (getManager()) {
				getManager().remove(getKey());
			}
	
			// shutdown socket and its IO streams
	
			getSocket().shutdownInput();
			getSocket().shutdownOutput();
	
			getInputStream().close();
			getOutputStream().close();
	
			getSocket().close();
	
			if (log_.isInfoEnabled()) {
			log_.info("subscriber signing off - " + getName());
			}
		}
		catch (java.net.SocketException e) {
			if (log_.isInfoEnabled()) {
			log_.info("subscriber " + getName() + " - Socket is closed");
			}
		}
		catch (java.io.IOException e) {
			log_.error("subscriber " + getName(), e);
		}
	}


    //////////////////////////////////////////////////////////////////////
    // resource handlers
    //////////////////////////////////////////////////////////////////////

    /**
     * Register the named resource handler.
     */

    protected void
	registerResourceHandler (String resource, String handler_name)
    {
        try {
            Class[] param_types = new Class[] { org.jdom.Element.class };
            java.lang.reflect.Method handler_method = getClass().getMethod(handler_name, param_types);

			if (log_.isDebugEnabled()) {
				log_.debug("register resource |" + resource + "| handler |" + handler_name + "| = " + handler_method);
			}
		    resource_handler.put(resource, handler_method);
        }
        catch (Exception e) {
            log_.error("invoke method", e);
        }
    }


    /**
     * Apply the requested resource handler.
     */

    protected void
	invokeResourceHandler (String resource, org.jdom.Element request_node)
	throws Exception
	{
		java.lang.reflect.Method handler_method = (java.lang.reflect.Method) resource_handler.get(resource);
		Object[] param_list = new Object[] { request_node };
	
		if (handler_method != null) {
			handler_method.invoke(this, param_list);
		}
	}


    /**
     * Parse the XML stream requests and invoke handlers to dispatch
     * them.
     */

    protected void
	parseRequest (java.io.ByteArrayInputStream in_bytes)
	throws org.xml.sax.SAXParseException, org.jdom.input.JDOMParseException
	{
		try {
			java.io.InputStreamReader in_reader = new java.io.InputStreamReader(in_bytes);
			java.io.BufferedReader buf_reader = new java.io.BufferedReader(in_reader);
			org.jdom.Document xml_doc = getManager().getCorrelator().buildXml(buf_reader);
	
			if (!xml_doc.hasRootElement()) {
				log_.warn("XML document has no root element");
			}
			else {
				org.jdom.Element request_node = xml_doc.getRootElement();
				String resource = request_node.getName();
		
				/**
				 * @TODO protocol handling is better represented by UML Activity Diagram
				 */
		
				if (log_.isInfoEnabled()) {
					String xml = org.opensims.xml.XmlBuilder.formatXML(request_node, false, true);
					log_.info("request: " + resource + " - " + xml);
				}
				invokeResourceHandler(resource, request_node);
			}
		}
		catch (org.jdom.input.JDOMParseException e) {
			throw e;
		}
		catch (org.xml.sax.SAXParseException e) {
			throw e;
		}
		catch (Exception e) {
			log_.error("subscriber XML error - " + getName(), e);
	
			org.jdom.Document reply_doc = new org.jdom.Document(new org.jdom.Element(org.opensims.xml.Node.ERROR_NODE));
			reply_doc.getRootElement().setAttribute("msg", "JDOM parsing error");
	
			((org.opensims.client.SubscriberManager) getManager()).pushMessage(this, reply_doc);
		}
	}


    //////////////////////////////////////////////////////////////////////
    // subscriber protocol management
    //////////////////////////////////////////////////////////////////////

    /**
     * Handle the Flash XML policy for low port number, etc.
     */

    public void
	handleRequestPolicy (org.jdom.Element request_node)
	throws Exception
	{
		StringBuffer buf = new StringBuffer();
	
		/**
		 * @TODO sort through the cross-domain policy issues
	
		 if some kind person at Macromedia would perchance answer our
		 questions about its use for port < 1024 ...
	
		 Upon establishing a connection with the specified port, the Flash
		 Player transmits <cross-domain-request/>, terminated by a null
		 byte. You may configure an XMLSocket server to serve both policy files
		 and normal XMLSocket connections over the same port, in which case,
		 the server should wait for <cross-domain-request/> before transmitting
		 a policy file.
		*/
	
		buf.append("<?xml version=\"1.0\"?>");
		buf.append("\n");
		buf.append("<!DOCTYPE cross-domain-policy SYSTEM \"http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd\">");
		buf.append("\n");
		buf.append("<!-- Policy file for xmlsocket://customer.symbiot.com -->");
		buf.append("\n");
		buf.append("<cross-domain-policy>");
		buf.append("\n");
		buf.append("   <allow-access-from domain=\"*\" to-ports=\"8443-8445\" />");
		buf.append("\n");
		buf.append("</cross-domain-policy>");
	
		((org.opensims.client.SubscriberManager) getManager()).pushMessage(this, buf.toString());
		setLastTick(getManager().getCorrelator().getTick());
	}


    /**
     * Handle a connection request.
     */

    public void
	handleRequestConnect (org.jdom.Element request_node)
	throws Exception
	{
		// <CONNECT username="test" lang="en" session="5647381230981230934876" />
	
		setLanguage(org.opensims.Config.fixNull(request_node.getAttributeValue("lang"), "en").trim());
		String method = org.opensims.Config.fixNull(request_node.getAttributeValue("method"), "").trim();
	
		if (method.equals("xml")) {
			setPlunderFlag(false);
		}
	
		// access the session and login
	
		String session_id = request_node.getAttributeValue("session");
	
		if (session_id != null) {
			org.opensims.servlet.Login login = getManager().getCorrelator().getServlet().getNeuromancer().lookupLoginBySessionId(session_id, getSocket());
	
			if (log_.isInfoEnabled()) {
				log_.info("session " + session_id + " login " + login);
			}
			
			// Returning users that have had an open browser should be reset
			if (login != null) {
				setLogin(login);
				getManager().getCorrelator().getServlet().getNeuromancer().setLogin(login.getLoginName(), "true");
				// getManager().getCorrelator().getServlet().appendUserLog(login.getLoginName(), session_id, "Subscriber login");
			}
		}
	
		// establish a mutual <PING/> and <PONG/> sanity check cycle
	
		org.jdom.Element ping_node = new org.jdom.Element(org.opensims.xml.Node.SUBSCRIBE_NODE);
		long ping_cycle = getPingPeriod() / 2L;
	
		ping_node.setAttribute("stream", org.opensims.stream.Stream.STREAM_PING);
		ping_node.setAttribute("delay", String.valueOf(ping_cycle));
		ping_node.setAttribute("refresh", String.valueOf(ping_cycle));
	
		if (addStream(org.opensims.stream.Stream.STREAM_PING, ping_node)) {
			setLastTick(getManager().getCorrelator().getTick());
		}
	}


    /**
     * Handle a disconnection request.
     */

    public void
	handleRequestDisconnect (org.jdom.Element request_node)
	throws Exception
    {	
		// force a disconnect, which would be implied by <PONG/> timeout anyway
		stop();
		shutdownSocket();
    }


    /**
     * Handle a stream subscription request.
     */

    public void
	handleRequestSubscribe (org.jdom.Element request_node)
	throws Exception
    {
		// <SUBSCRIBE stream="viz" refresh="5000" activity="10" threat="perimeter"/>
	
		String stream_name = org.opensims.Config.fixNull(request_node.getAttributeValue("stream"), "").trim().toLowerCase();
	
		if (addStream(stream_name, request_node)) {
			setLastTick(getManager().getCorrelator().getTick());
		}
    }


    /**
     * Handle a stream unsubscription request.
     */

    public void
	handleRequestUnsubscribe (org.jdom.Element request_node)
	throws Exception
    {
		String stream_name = org.opensims.Config.fixNull(request_node.getAttributeValue("stream"), "").trim().toLowerCase();
	
		if (!stream_name.equals("")) {
			// unsubscribe from the named stream	
			removeStream(stream_name.trim());
		}
		else {
			// unsubscribe all streams
			removeAllStreams();
		}
    }


    /**
     * Note that a <PONG/> reply to the <PING/> was received.
     */

    public void
	handleRequestPong (org.jdom.Element request_node)
	throws Exception
    {
		if (log_.isDebugEnabled()) {
			log_.debug("receive pong - " + getName() + " interval " + getPingInterval());
		}	
		setLastTick(getManager().getCorrelator().getTick());
    }


    //////////////////////////////////////////////////////////////////////
    // ping/status
    //////////////////////////////////////////////////////////////////////

    /**
     * Add a status message to the queue.
     */

    public void
	addStatusMessage (int rank, boolean reboot_flag, boolean modal_flag, int error_code, String message, String url)
	{
		org.jdom.Element status_node = new org.jdom.Element(org.opensims.xml.Node.STATUS_NODE);
	
		status_node.setAttribute("rank", String.valueOf(rank));
		status_node.setAttribute("reboot", reboot_flag ? "1" : "0");
		status_node.setAttribute("modal", modal_flag ? "1" : "0");
		status_node.setAttribute("error", String.valueOf(error_code));
		status_node.setAttribute("message", org.opensims.Config.fixNull(message, ""));
		status_node.setAttribute("url", org.opensims.Config.fixNull(url, ""));
	
		synchronized (notify_node) {
			notify_node.addContent(status_node);
		}
	}


    /**
     * Get the next (FIFO) status message in the queue.
     */

    public org.jdom.Element
	getStatusMessage ()
	{
		org.jdom.Element status_node = notify_node.getChild(org.opensims.xml.Node.STATUS_NODE);
		long start_idx = 0L;
	
		if (status_node != null) {
			status_node.detach();
		}
		else {
			// create one by default from the local agent
			// autodiscovery status
	
			String message = "no agent online";
			java.util.ArrayList scan_list = getManager().getCorrelator().getScanList();
	
			if (scan_list.size() > 0)
				message = "";
						
			if (scan_list.size() > MAX_NETS_TO_DISPLAY) {
				if ( (last_status_index + MAX_NETS_TO_DISPLAY) < scan_list.size() )
					last_status_index++;
				else
					last_status_index = 0;
			}
			
			for (int i = last_status_index; i < scan_list.size(); i++) {
				org.opensims.model.Scan scan = (org.opensims.model.Scan) scan_list.get(i);	
				message += scan.getStatus();
				message += "\n";
			}
	
			status_node = new org.jdom.Element(org.opensims.xml.Node.STATUS_NODE);
	
			status_node.setAttribute("reboot", "0");
			status_node.setAttribute("modal", "0");
			status_node.setAttribute("error", "0");
			status_node.setAttribute("message", message);
			status_node.setAttribute("url", "");
		}
	
		return status_node;
	}


    /**
     * Get the ping period (milliseconds), i.e. the max allowed
     * interval between <PING/> messages.
     */

    public long
	getPingPeriod ()
    {
		return ping_period;
    }


    /**
     * Set the ping period (milliseconds).
     */

    public void
	setPingPeriod (long ping_period)
    {
		this.ping_period = ping_period;
    }


    /**
     * Get the interval since the last <PONG/> was received
     * (milliseconds).
     */

    public long
	getPingInterval ()
    {
		long this_tick = getManager().getCorrelator().getTick();

		return this_tick - getLastTick();
    }


    //////////////////////////////////////////////////////////////////////
    // stream handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the stream table.
     */

    protected java.util.Hashtable
	getStreamTable ()
    {
		return stream_table;
    }


    /**
     * Set the stream table.
     */

    protected void
	setStreamTable (java.util.Hashtable stream_table)
    {
		this.stream_table = stream_table;
    }


    /**
     * Get a stream.
     */

    public org.opensims.stream.Stream
	getStream (String stream_name)
    {
		return (org.opensims.stream.Stream) getStreamTable().get(stream_name);
    }


    /**
     * Create a new stream, based on the given name.
     */

    protected boolean
	addStream (String stream_name, org.jdom.Element request_node)
	throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		boolean result = false;
		org.jdom.Element stream_node = ((org.opensims.client.SubscriberManager) getManager()).getStreamConfig(stream_name);
	
		if (log_.isDebugEnabled()) {
			String request_xml = org.opensims.xml.XmlBuilder.formatXML(request_node, false, true);
			String stream_xml = org.opensims.xml.XmlBuilder.formatXML(stream_node, false, true);
			log_.debug("add stream " + getName() + " " + stream_name + " request " + request_xml + " config " + stream_xml);
		}
	
		if (stream_node != null) {
			org.opensims.stream.Stream stream = getStream(stream_name);
	
			// cancel a previous instance, if any
	
			if (stream != null) {
			stream.stop();
			}
	
			// create a new instance
	
			String class_name = stream_node.getAttributeValue("class");
			Class c = Class.forName(class_name);
	
			stream = (org.opensims.stream.Stream) c.newInstance();
			stream.init(getManager().getCorrelator(), this, stream_name, request_node);
	
			/**
			 * @TODO too much
	
			if (log_.isInfoEnabled()) {
			log_.info("add stream " + getName() + " " + stream_name + " = " + stream);
			}
			 */
	
			getStreamTable().put(stream_name, stream);
			stream.start();
	
			result = true;
		}
	
		return result;
	}


    /**
     * Remove a stream.
     */

    protected void
	removeStream (String stream_name)
	{
		org.opensims.stream.Stream stream = getStream(stream_name);
	
		/**
		 * @TODO too much
	
		if (log_.isInfoEnabled()) {
			log_.info("remove stream " + getName() + " " + stream_name + " = " + stream);
		}
		 */
	
		// cancel a previous instance, if any
	
		if (stream != null) {
			stream.stop();
		}
	
		getStreamTable().remove(stream_name);
	}


    /**
     * Unsubscribe from all streams.
     */

    protected void
	removeAllStreams ()
    {
		for (java.util.Enumeration e = getStreamTable().keys(); e.hasMoreElements(); ) {
			String stream_name = (String) e.nextElement();
	
			if (!stream_name.equals("ping")) {
			removeStream(stream_name);
			}
		}
    }


    /**
     * Push the XML content document out to the subscriber.
     */

    public void
	pushStream (org.jdom.Document content_doc, String stream_name)
	{
		/**
		 * @TODO too much
	
		if (log_.isInfoEnabled()) {
			StringBuffer buf = new StringBuffer();
	
			buf.append("push: ");
			buf.append(stream_name);
			buf.append(" among:");
	
			for (java.util.Enumeration e = getStreamTable().keys(); e.hasMoreElements(); ) {
			buf.append(" ").append((String) e.nextElement());
			}
	
			log_.info(buf.toString());
		}
		 */
	
		if (log_.isDebugEnabled()) {
			log_.debug(org.opensims.xml.XmlBuilder.formatXML(content_doc.getRootElement(), false, true));
		}
	
		// push to subscriber, plundereringing if indicated
	
		if (getPlunderFlag()) {
			((org.opensims.client.SubscriberManager) getManager()).pushMessage(this, ((org.opensims.client.SubscriberManager) getManager()).plunderNode(content_doc.getRootElement(), stream_name));
		}
		else {
			((org.opensims.client.SubscriberManager) getManager()).pushMessage(this, content_doc);
		}
	}


    //////////////////////////////////////////////////////////////////////
    // session handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Login wrapper.
     */

    public org.opensims.servlet.Login
	getLogin ()
    {
		return login;
    }


    /**
     * Set the Login wrapper.
     */

    public void
	setLogin (org.opensims.servlet.Login login)
    {
		this.login = login;
    }
}
