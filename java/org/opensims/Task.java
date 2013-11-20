/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: Task.java,v 1.6 2005/12/26 17:37:43 mikee Exp $
 * $Id: Task.java 1 2008-01-10 18:37:05Z smoot $
 * OpenSIMS correlation engine - timer task
 * @version $Revision: 1.6 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    Task
    extends java.util.TimerTask
{
    // protected fields
    protected Object subject = null;
    protected java.lang.reflect.Method task_method = null;

    // quality assurance
    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.Task.class.getName());


    /**
     * Constructor
     */

    public
	Task (Object subject, java.util.Timer timer, org.jdom.Element task_node)
	{
		try {
			// parse the TimerTask parameters
			String method_name = task_node.getAttributeValue("method");
			org.jdom.Attribute task_delay_attr = task_node.getAttribute("delay");
			org.jdom.Attribute task_period_attr = task_node.getAttribute("period");
	
			long delay = 0L;
			long period = 0L;
	
			if (task_delay_attr != null) {
				delay = task_delay_attr.getLongValue();
			}
	
			if (task_period_attr != null) {
				period = task_period_attr.getLongValue();
			}
	
			if (log_.isInfoEnabled()) {
				log_.info("set timer " + method_name + " for " + subject.toString() + " delay " + delay + " period " + period);
			}
	
			// build the reflection method
			this.subject = subject;
			this.task_method = subject.getClass().getMethod(method_name, null);
	
			// set the timer
			timer.schedule(this, delay, period);
		}
		catch (Exception e) {
			log_.error("instantiate timer task", e);
		}
	}


    /**
     * Invoke the subject's reflected method to provide the
     * Runnable task.
     */

    public void
	run ()
	{
		try {
			if (log_.isDebugEnabled())
			{
				log_.debug("about to invoke task: " + task_method.toString());
			}
			
			Object result = task_method.invoke(subject, null);
			
			if (log_.isDebugEnabled())
			{
				log_.debug("invoked task: " + task_method.toString() + " - returned: " + result);
			}
		}
		catch (IllegalAccessException e) 
		{
			String	description = e.getCause().toString();
			log_.error("invoke task: " + task_method.toString(), e);
		}
		catch (java.lang.reflect.InvocationTargetException e)
		{
			String	description = e.getCause().toString();
			log_.error("invoke task: " + task_method.toString(), e);
		}
		catch (Exception e) 
		{
			String	description = e.getCause().toString();
			log_.error("invoke task: " + task_method.toString(), e);
		}
	}

}
