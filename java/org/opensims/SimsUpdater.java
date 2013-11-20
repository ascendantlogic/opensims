/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: SimsUpdater.java,v 1.13 2006/04/25 02:34:13 mikee Exp $
 * $Id: SimsUpdater.java 1 2008-01-10 18:37:05Z smoot $
 * OpenSIMS updater.
 * @version $Revision: 1.13 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. ERWIN <mikee@symbiot.com>
 */

public class
    SimsUpdater
    implements org.opensims.Updater
{
    // protected fields

    protected Thread thread = null;
    protected org.opensims.Correlator correlator = null;
    protected org.jdom.Element config_node = null;
    protected java.io.File log_file = null;
    protected org.jdom.Document document = null;
    protected boolean lock_flag = false;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.SimsUpdater.class.getName());


    /**
     * Initialize the updater.
     */

    public void
	init (org.opensims.Correlator correlator, org.jdom.Element config_node)
    {
		setCorrelator(correlator);
		setConfigNode(config_node);
	
		// load the document	
		java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
		java.io.File updates_dir = new java.io.File(var_lib, "updates");
	
		setLogFile(new java.io.File(updates_dir, "updates.xml"));
		loadDocument();
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Represent as a String
     */

    public String
	getSummary ()
    {
		StringBuffer buf = new StringBuffer();
	
		buf.append("\n\t version: ");
		buf.append(getVersion());
		buf.append("\n\t tick of last installed update: ");
		buf.append(getLastTick());
		buf.append("\n");
	
		return buf.toString();
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


    /**
     * Get the config node.
     */

    public org.jdom.Element
	getConfigNode ()
    {
		return config_node;
    }


    /**
     * Set the config node.
     */

    public void
	setConfigNode (org.jdom.Element config_node)
    {
		this.config_node = config_node;
    }


    /**
     * Get the updates lock flag.
     */

    public boolean
	getLockFlag ()
    {
		return lock_flag;
    }


    /**
     * Set the updates lock flag.
     */

    public void
	setLockFlag (boolean lock_flag)
    {
		this.lock_flag = lock_flag;
    }


    //////////////////////////////////////////////////////////////////////
    // document handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the log file.
     */

    public java.io.File
	getLogFile ()
    {
		return log_file;
    }


    /**
     * Set the log file.
     */

    public void
	setLogFile (java.io.File log_file)
    {
		this.log_file = log_file;
    }


    /**
     * Get the log XML document.
     */

    public org.jdom.Document
	getDocument ()
    {
		return document;
    }


    /**
     * Set the log XML document.
     */

    public void
	setDocument (org.jdom.Document document)
    {
		this.document = document;
    }


    /**
     * Load the log XML document.
     */

    protected void
	loadDocument ()
	{
		java.io.BufferedReader buf_reader = null;
	
		try {
			// test the updates log file
	
			if (log_.isInfoEnabled()) {
				log_.info("updates log file: " + getLogFile());
			}
	
			if (getLogFile().canRead()) {
				java.io.FileReader file_reader = new java.io.FileReader(getLogFile());
				buf_reader = new java.io.BufferedReader(file_reader);
		
				setDocument(getCorrelator().buildXml(buf_reader));
				
				if (log_.isDebugEnabled()) {
					log_.debug(org.opensims.xml.XmlBuilder.formatXML(getDocument().getRootElement(), true, true));
				}
			}
			else {
				org.jdom.Element updates_node = new org.jdom.Element(org.opensims.xml.Node.PRODUCT_NODE);
		
				updates_node.setAttribute("repository", getCorrelator().getConfig("repository.url"));
				setDocument(new org.jdom.Document(updates_node));
				saveDocument();
			}
		}
		catch (Exception e) {
			log_.error("load document", e);
		}
		finally {
			if (buf_reader != null) {
				try {
					buf_reader.close();
				}
				catch (Exception e2) {
					log_.error("close reader", e2);
				}
			}
		}
	}


    /**
     * Save the log XML document.
     */

    public void
    saveDocument ()
	{
		if (log_.isInfoEnabled()) {
			log_.info("save log file: " + getLogFile());
		}
	
		try {
			if (!getLogFile().canWrite()) {
				log_.warn("save log file - no pre-existing (or bad file perms) " + getLogFile());
			}
	
			if (log_.isDebugEnabled()) {
				log_.debug("save log " + org.opensims.xml.XmlBuilder.formatXML(getDocument().getRootElement(), true, true));
			}
	
			java.io.FileWriter file_writer = new java.io.FileWriter(getLogFile());
			java.io.BufferedWriter buf_writer = new java.io.BufferedWriter(file_writer);
			java.io.PrintWriter print_writer = new java.io.PrintWriter(buf_writer);
	
			String xml = org.opensims.xml.XmlBuilder.formatXML(getDocument().getRootElement(), true, true);
	
			print_writer.write(xml);
			print_writer.flush();
			print_writer.close();
		}
		catch (Exception e) {
			log_.error("save log file", e);
		}
	}


    //////////////////////////////////////////////////////////////////////
    // download management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the tick of the most recent update.
     */

    public synchronized long
	getLastTick ()
    {
  		return Long.valueOf(getConfigNode().getAttributeValue("tick")).longValue();
    }
    
    /**
     * Get the version of the most recent update.
     */

    public String
	getVersion ()
    {
  		return getConfigNode().getAttributeValue("version");
    }


    /**
     * Test and set the lock flag, to provide non-blocking
     * synchronization.  If the lock flag is not true (not currently
     * locked) then set it and return true.  Otherwise, return false.
     */

    public synchronized boolean
	testSetLock ()
	{
		boolean result = false;
	
		if (!getLockFlag()) {
			setLockFlag(true);
			result = true;
		}
	
		return result;
	}


    /**
     * Evaluate and add a list of updates.
     */

    public synchronized int
	addUpdates (org.jdom.Element updates_node)
	{
		int count = 0;
	
		try {
			if (updates_node != null) {
				java.util.Iterator update_iter = updates_node.getChildren().iterator();
		
				while (update_iter.hasNext()) {
					org.jdom.Element update_node = (org.jdom.Element) update_iter.next();
		
					// <UPDATE name="banana9000" version="1.0.0" tick="1097940000000" file="2004.10.16.00001.tgz" />
		
					String file_name = update_node.getAttributeValue("file");
					StringBuffer path = new StringBuffer();
		
					path.append("//");
					path.append(org.opensims.xml.Node.UPDATE_NODE);
					path.append("[@file = '");
					path.append(file_name);
					path.append("']");
		
					org.jdom.Element extant_node = (org.jdom.Element) org.jdom.xpath.XPath.selectSingleNode(getDocument(), path.toString());
		
					if (extant_node == null) {
					getDocument().getRootElement().addContent((org.jdom.Element) update_node.clone());
					count++;
					}
				}
			}
		}
		catch (Exception e) {
			log_.error("add updates", e);
		}
	
		// save the document to disk, as needed
	
		if (count > 0) {
			saveDocument();
		}
	
		return count;
	}


    /**
     * Download the given update.
     */

    protected synchronized void
	downloadUpdate (org.jdom.Element update_node)
	throws Exception
	{
		String file_name = update_node.getAttributeValue("file");		    
		java.util.Hashtable param_table = new java.util.Hashtable();
	
		param_table.put("file", file_name);
	
		org.opensims.crypto.RepositoryUrl url = getCorrelator().getRepositoryUrl("GET", "download", param_table, null);
		int response_code = url.getResponseCode();
	
		if (response_code == java.net.HttpURLConnection.HTTP_OK) {
			// download tarball
	
			java.io.File var_lib = new java.io.File(getCorrelator().getConfig("var.lib"));
			java.io.File updates_dir = new java.io.File(var_lib, "updates");
			java.io.File download_file = new java.io.File(updates_dir, file_name);
	
			url.saveToFile(download_file);
	
			// note that the update is now ready to run
	
			update_node.setAttribute("status", "pending");
			update_node.setAttribute("since", String.valueOf(getCorrelator().getTick()));
	
			saveDocument();
	
			// revise the system Config last update tick
	
			String released_tick = update_node.getAttributeValue("tick");
	
			getConfigNode().setAttribute("tick", released_tick);
			getCorrelator().getConfig().saveConfigFile();
		}
	
		url.disconnect();
	}


    //////////////////////////////////////////////////////////////////////
    // Runnable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Start the update downloads.
     */

    public void
	start ()
    {
		java.util.Date date = new java.util.Date();

        thread = new Thread(this, "updater " + this.hashCode() + " initiated " + date.toString());

        if (log_.isInfoEnabled()) {
            log_.info("start: " + thread.getName());
        }

		setLockFlag(true);
        thread.start();
    }


    /**
     * Stop the update downloads.
     */

    public void
	stop ()
    {
        if (log_.isInfoEnabled()) {
            log_.info("stop: " + thread.getName());
        }

        thread = null;
		setLockFlag(false);
    }


    /**
     * Run the update downloads.
     */

    public void
	run ()
	{
		java.util.Iterator update_iter = null;
		String session_name = Thread.currentThread().getName();
	
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	
		try {
			loadDocument();
	
			// first, create a list of updates to download
	
			StringBuffer path = new StringBuffer();
	
			path.append("//");
			path.append(org.opensims.xml.Node.UPDATE_NODE);
			path.append("[not(@status)]");
	
			update_iter = org.jdom.xpath.XPath.selectNodes(getDocument(), path.toString()).iterator();
	
			// then attempt to download each of them
	
			while ((thread != null) && update_iter.hasNext()) {
				org.jdom.Element update_node = (org.jdom.Element) update_iter.next();	
				Thread.yield();
				downloadUpdate(update_node);
			}
		}
		catch (Exception e) {
			log_.error("update download failed", e);
			thread = null;
	
			return;
		}
		finally {
			setLockFlag(false);
		}
	
		/**
		 * @TODO notify sysadmin to check "Config: Updates" report
		 */
	
		if (log_.isInfoEnabled()) {
			log_.info("finish: " + session_name);
		}
	}

}
