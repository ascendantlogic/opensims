/**
 * @LICENSE@
 */

package org.opensims.xml;

/**
 * $CVSId: UmlActivity.java,v 1.12 2007/02/08 01:08:46 mikee Exp $
 * $Id: UmlActivity.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.12 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 *
 * <P>
 * This essentially executes a simple UML activity diagram which has
 * been represented as XML (jdom), leveraging Java reflection on a
 * class which is the "subject" of the state machine.
 * </P>
 */

public class
    UmlActivity
    extends Object
{
    // protected fields
	protected org.opensims.Correlator correlator = null;
	protected org.jdom.Document document = null;
	protected Object subject = null;
	protected boolean running_flag = false;
	protected java.util.Hashtable state_table = null;
	protected java.util.Hashtable interval_table = null;
	protected org.jdom.Element current_state = null;

    // quality assurance
    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.xml.UmlActivity.class.getName());


    /**
     * Constructor
     */

    public
	UmlActivity (org.jdom.Document document, Object subject, org.opensims.Correlator correlator)
    {
		setDocument(document);
		setSubject(subject);
		setCorrelator(correlator);
    }


    /**
     * <P>Build a state transition table from the source XML document.</P>
     */

    public void
	init ()
	{
		setStateTable(new java.util.Hashtable());
		setIntervalTable(new java.util.Hashtable());
		java.util.Iterator state_iter = getDocument().getRootElement().getChildren(org.opensims.xml.Node.STATE_NODE).iterator();
	
		while (state_iter.hasNext()) {
			org.jdom.Element state_node = (org.jdom.Element) state_iter.next();
			String state_name = state_node.getAttributeValue("name");
	
			if (log_.isDebugEnabled()) {
				log_.debug("load state: " + state_name);
			}
	
			getStateTable().put(state_name, state_node);
		}
	}

    /**
     * Get the Correlator.
     */

    public org.opensims.Correlator
	getCorrelator ()
    {
		return correlator;
    }


    /**
     * Set the Correlator.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator)
    {
		this.correlator = correlator;
    }


    //////////////////////////////////////////////////////////////////////
    // run state methods
    //////////////////////////////////////////////////////////////////////

    /**
     * <P>Start with the designated initial "start" state.</P>
     */

    public void
	start ()
    {
		setRunningFlag(true);
		selectState(getDocument().getRootElement().getAttributeValue("initial_state"));
    }


    /**
     * <P>Stop running any further events.</P>
     */

    public void
	stop ()
    {
		setRunningFlag(false);
    }


    /**
     * Get the running flag.
     */

    public boolean
	getRunningFlag ()
    {
		return running_flag;
    }


    /**
     * Set the running flag.
     */

    public void
	setRunningFlag (boolean running_flag)
    {
		this.running_flag = running_flag;
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the source XML document.
     */

    public org.jdom.Document
	getDocument ()
    {
		return document;
    }


    /**
     * Set the source XML document.
     */

    public void
	setDocument (org.jdom.Document document)
    {
		this.document = document;
    }


    /**
     * Get the subject.
     */

    public Object
	getSubject ()
    {
		return subject;
    }


    /**
     * Set the subject.
     */

    public void
	setSubject (Object subject)
    {
		this.subject = subject;
    }


    //////////////////////////////////////////////////////////////////////
    // state transitions
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the state table.
     */

    protected java.util.Hashtable
	getStateTable ()
    {
		return state_table;
    }


    /**
     * Set the state table.
     */

    protected void
	setStateTable (java.util.Hashtable state_table)
    {
		this.state_table = state_table;
    }


    /**
     * Get the name of the current state
     */

    public org.jdom.Element
	getCurrentState ()
    {
		return current_state;
    }


    /**
     * Get the name of the current state
     */

    public String
	getCurrentStateName ()
    {
		String state_name = null;
	
		if (current_state != null) {
			state_name = current_state.getAttributeValue("name");
		}
		
		return state_name;
    }


    /**
     * Select a state, given a name
     */

    public void
	selectState (String state_name)
	{
		current_state = (org.jdom.Element) getStateTable().get(state_name);
		String	send_email_command = null;
	
		if (log_.isDebugEnabled()) {
			log_.debug("current state: " + state_name);
		}
		
		// Send an e-mail notification on discovery initiation
		if (state_name.equals("discover_active")) {
		
			if (getCorrelator().getConfig("notify.email") != null) {
			
				try {
					String to_addr = getCorrelator().getConfig("notify.email");
					String from_addr = "5600@symbiot.net";
					String smtp_host = "symbiot.net";
					String smtp_port = "587";
					String subj_line = "SymbiotNET Report: 5600 Starting Autodiscovery for " + ((org.opensims.model.GenericScan) getSubject()).getNetworkId();
					String msg_text = "\nPlease be advised that network scanning will commence in 5 seconds-\n\n";
					
					// create some properties and get the default Session	
					java.util.Properties props = new java.util.Properties();
					props.put("mail.smtp.host", smtp_host);
					props.put("mail.smtp.port", smtp_port);
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
	
		performActions(current_state);
		attemptTransits(current_state);
	}


    /**
     * Post a trigger event
     */

    public boolean
	postEventTrigger (String trigger_name)
	{
		boolean result = false;
	
		try {
			if (getRunningFlag()) {
				StringBuffer path = new StringBuffer();
		
				path.append("//");
				path.append(org.opensims.xml.Node.STATE_NODE);
				path.append("[@name = '");
				path.append(getCurrentStateName());
				path.append("']/");
				path.append(org.opensims.xml.Node.TRANSIT_NODE);
				path.append("[@trigger = '");
				path.append(trigger_name);
				path.append("']");
		
				org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
				org.jdom.Element transit_node = (org.jdom.Element) xpath.selectSingleNode(getDocument());
		
				if (log_.isDebugEnabled())
				{
					log_.debug("event search xpath: " + xpath.toString());
					log_.debug("posted event: " + trigger_name + " - triggers: " + transit_node);
				}
		
				if (transit_node != null) {
					result = attemptTransit(transit_node, true);
				}
			}
		}
		catch (org.jdom.JDOMException e) {
			log_.error("post event trigger", e);
		}
	
		return result;
	}


    //////////////////////////////////////////////////////////////////////
    // interval timers
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the interval table.
     */

    protected java.util.Hashtable
	getIntervalTable ()
    {
		return interval_table;
    }


    /**
     * Set the interval table.
     */

    protected void
	setIntervalTable (java.util.Hashtable interval_table)
    {
		this.interval_table = interval_table;
    }


    /**
     * Mark the time for a named interval.
     */

    protected void
	markInterval (String interval_name)
    {
		Long start_tick = new Long(System.currentTimeMillis());
	
		getIntervalTable().put(interval_name, start_tick);
    }


    /**
     * Get the elapsed time for a named interval.
     */

    protected long
	getElapsedInterval (String interval_name)
    {
		long interval = 0L;
	
		Long start_tick = (Long) getIntervalTable().get(interval_name);
	
		if (start_tick != null) {
			interval = System.currentTimeMillis() - start_tick.longValue();
		}
		
		if (log_.isDebugEnabled())
		{
			log_.debug("marked timestamp: " + start_tick + "; interval = " +interval);
		}
	
		return interval;
    }


    //////////////////////////////////////////////////////////////////////
    // action handlers
    //////////////////////////////////////////////////////////////////////

    /**
     * Are there any actions to be performed?
     */

    protected void
	performActions (org.jdom.Element state_node)
	{
		java.util.Iterator action_iter = state_node.getChildren().iterator();
	
		while (action_iter.hasNext()) {
			org.jdom.Element action_node = (org.jdom.Element) action_iter.next();
	
			if (action_node.getName().equals(org.opensims.xml.Node.ACTION_NODE)) {
				String method_name = action_node.getAttributeValue("method");
				String param = action_node.getAttributeValue("param");
		
				if (log_.isDebugEnabled()) {
					log_.debug("perform action: " + method_name);
				}
		
				// ignore the "null" return
				invokeMethod(method_name, param);
			}
			else if (action_node.getName().equals(org.opensims.xml.Node.MARK_NODE)) {
				String interval_name = action_node.getAttributeValue("interval");
		
				if (log_.isDebugEnabled()) {
					log_.info("mark interval: " + interval_name);
				}
		
				markInterval(interval_name);
			}
		}
	}


    /**
     * Attempt invoking the method via reflection
     */

    protected Object
	invokeMethod (String method_name, String param)
	{
		Object result = null;
	
		try {
			Class c = getSubject().getClass();
			Class[] param_types = new Class[] {String.class};
			Object[] param_list = new Object[] {param};
	
			java.lang.reflect.Method obj_method = c.getMethod(method_name, param_types);
			result = obj_method.invoke(getSubject(), param_list);
	
			if (log_.isDebugEnabled()) {
				log_.debug("invoke method: " + method_name + " - returns: " + result);
			}
		}
		catch (NoSuchMethodException e) {
			log_.error("invoke method", e);
		}
		catch (IllegalAccessException e) {
			log_.error("invoke method", e);
		}
		catch (java.lang.reflect.InvocationTargetException e) {
			log_.error("invoke method", e);
		}
	
		return result;
	}


    //////////////////////////////////////////////////////////////////////
    // transit handlers
    //////////////////////////////////////////////////////////////////////

    /**
     * Are there any transits out of this state which are ready?
     */

    protected boolean
	attemptTransits (org.jdom.Element state_node)
	{
		java.util.Iterator transit_iter = state_node.getChildren(org.opensims.xml.Node.TRANSIT_NODE).iterator();
	
		while (transit_iter.hasNext()) {
			org.jdom.Element transit_node = (org.jdom.Element) transit_iter.next();
	
			if (attemptTransit(transit_node, false)) {
				return true;
			}
		}
	
		return false;
	}


    /**
     * Is this transit ready?
     */

    protected boolean
	attemptTransit (org.jdom.Element transit_node, boolean trigger_happy)
	{
		String next_state = transit_node.getAttributeValue("next");
		String trigger = transit_node.getAttributeValue("trigger");
	
		if (log_.isDebugEnabled()) {
			log_.debug("attempt transit: " + next_state);
			log_.debug("  has trigger: " + trigger);
		}
	
		if ((trigger != null) && !trigger_happy) {
			// ignore transits which require unsatisfied trigger events
			return false;
		}
		else {
			// can we pass the guards and interval timer waits, at
			// this point?
			java.util.Iterator guard_iter = transit_node.getChildren().iterator();
	
			while (guard_iter.hasNext()) {
				org.jdom.Element guard_node = (org.jdom.Element) guard_iter.next();
		
				if (!testGuard(guard_node)) {
					return false;
				}
			}
		}

		// if we got this far, then perform the transit
		performActions(transit_node);
		selectState(next_state);
		
		return true;
	}


    /**
     * Can we pass this guard or interval timer wait?
     */

    protected boolean
	testGuard (org.jdom.Element guard_node)
	{
		boolean result = true;
	
		if (guard_node.getName().equals(org.opensims.xml.Node.GUARD_NODE)) {
			String method_name = guard_node.getAttributeValue("method");
			String param = guard_node.getAttributeValue("param");
	
			Boolean	invokeResult = (Boolean) invokeMethod(method_name, param);
			
			result = invokeResult.booleanValue();
	
			if (log_.isDebugEnabled()) {
				log_.debug("test guard: " + method_name + " - returns: " + result);
			}
		}
		else if (guard_node.getName().equals(org.opensims.xml.Node.WAIT_NODE)) {
			String interval_name = guard_node.getAttributeValue("interval");
			long period = 0;
			
			result = false;
	
			try {
				period = Long.parseLong(guard_node.getAttributeValue("period"));
		
				if (period <= getElapsedInterval(interval_name)) {
					result = true;
				}
			}
			catch (Exception e) {
				log_.error("parse wait period", e);
			}
	
			if (log_.isDebugEnabled()) {
				log_.info("test wait: " + interval_name + " - returns: " + result + "; waiting for " + period + " elapsed milliseconds");
			}
		}
	
		return result;
	}


    //////////////////////////////////////////////////////////////////////
    // quality assurance
    //////////////////////////////////////////////////////////////////////

    /**
     * command line test interface
     */

    public static void
	main (String[] args)
	{
		org.apache.log4j.BasicConfigurator.configure(new org.apache.log4j.ConsoleAppender(new org.apache.log4j.PatternLayout()));
	
		// parse the command line arguments
		String uml_file_name = args[0];
	
		try {
			// load the XML document which defines a UML activity diagram (aka, a finite state machine)
			java.io.File uml_file = new java.io.File(uml_file_name);
			java.io.FileReader file_reader = new java.io.FileReader(uml_file);
			java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);
			org.opensims.xml.XmlBuilder xml_builder = new org.opensims.xml.XmlBuilder(false);
	
			org.jdom.Document document = xml_builder.build(buf_reader);
	
			/**
			 * @TODO we need a JUnit test here
	
			String test_trigger = args[1];
	
			// run the tests
	
			org.opensims.xml.UmlActivity activity = new org.opensims.xml.UmlActivity(document);
	
			System.out.println("subject class: " + activity.getSubject());
	
			activity.init();
			activity.start();
	
			activity.postEventTrigger(test_trigger);
			*/
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
