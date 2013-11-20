/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: Updater.java,v 1.3 2005/10/17 01:14:48 mikee Exp $
 * $Id: Updater.java 1 2008-01-10 18:37:05Z smoot $
 * OpenSIMS updater.
 * @version $Revision: 1.3 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    Updater
    extends Runnable
{
    /**
     * Initialize the updater.
     */

    public void
	init (org.opensims.Correlator correlator, org.jdom.Element config_node);


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Represent as a String
     */

    public String
    getSummary ();
    
        
    /**
     * Get the Correlator.
     */

    public org.opensims.Correlator
	getCorrelator ();


    /**
     * Set the Correlator.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator);


    /**
     * Get the config node.
     */

    public org.jdom.Element
	getConfigNode ();


    /**
     * Set the config node.
     */

    public void
	setConfigNode (org.jdom.Element config_node);


    /**
     * Get the updates lock flag.
     */

    public boolean
	getLockFlag ();


    /**
     * Set the updates lock flag.
     */

    public void
	setLockFlag (boolean lock_flag);


    //////////////////////////////////////////////////////////////////////
    // document handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the log file.
     */

    public java.io.File
        getLogFile ();


    /**
     * Set the log file.
     */

    public void
        setLogFile (java.io.File log_file);


    /**
     * Get the updates log XML document.
     */

    public org.jdom.Document
	getDocument ();


    /**
     * Set the updates log XML document.
     */

    public void
	setDocument (org.jdom.Document document);


    /**
     * Save the log XML document.
     */

    public void
        saveDocument ();


    //////////////////////////////////////////////////////////////////////
    // download management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the tick of the most recent update.
     */

    public long
	getLastTick ();


    /**
     * Get the version of the most recent update.
     */

    public String
	getVersion ();


    /**
     * Test and set the lock flag, to provide non-blocking
     * synchronization.  If the lock flag is not true (not currently
     * locked) then set it and return true.  Otherwise, return false.
     */

    public boolean
	testSetLock ();


    /**
     * Evaluate and add a list of updates.
     */

    public int
	addUpdates (org.jdom.Element updates_node);


    //////////////////////////////////////////////////////////////////////
    // Runnable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Start the update downloads.
     */

    public void
        start ();


    /**
     * Stop the update downloads.
     */

    public void
        stop ();


    /**
     * Run the update downloads.
     */

    public void
        run ();
}
