/**
 * @LICENSE@
 */

package org.opensims.model;

/**
 * $CVSId: Scan.java,v 1.36 2006/04/02 19:51:17 mikee Exp $
 * $Id: Scan.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.36 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    Scan
    extends org.opensims.Tickable,
	    org.opensims.Exportable
{
    /**
     * Initialize the scan
     */

    public void
	init (org.opensims.model.ScanManager scan_manager, org.opensims.agent.Agent agent, org.jdom.Element scan_node, org.jdom.Element network_node, String network_number, String device);


    /**
     * Represent as a String
     */

    public String
        getSummary ();


    /**
     * Represent a status line as a String.
     */

    public String
        getStatus ();


    //////////////////////////////////////////////////////////////////////
    // implement a finite state machine
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the FSM.
     */

    public org.opensims.xml.UmlActivity
	getFSM ();


    /**
     * Set the FSM.
     */

    public void
	setFSM (org.opensims.xml.UmlActivity fsm);


    /**
     * Get the associated Agent.
     */

    public org.opensims.agent.Agent
	getAgent ();


    /**
     * Set the associated Agent.
     */

    public void
	setAgent (org.opensims.agent.Agent agent);


    /**
     * Start the state machine running.
     */

    public void
	start ();


    /**
     * Stop the state machine running.
     */

    public void
	stop ();


    /**
     * Get a String representation for the current FSM state
     */

    public String
	getCurrentState ();


    /**
     * Receive an agent event, such as "agent.heartbeat"
     */

    public void
	agentPostEvent (String trigger_name);
	
	
	/**
     *  Get state of event correlation
     */
    
    public boolean
	getCorrelationEnable ();


    //////////////////////////////////////////////////////////////////////
    // file handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the model file.
     */

    public java.io.File
	getModelFile ();


    /**
     * Set the model file.
     */

    public void
	setModelFile (java.io.File model_file);


    //////////////////////////////////////////////////////////////////////
    // model document update
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the model document.
     */

    public org.jdom.Document
	getModelDoc ();


    /**
     * Set the model document.
     */

    public void
	setModelDoc (org.jdom.Document model_doc);


    /**
     * Walk the given document tree, using it to update the existing
     * XML elements and attributes
     */

    public void
	updateModel (org.jdom.Element update_network_node, String param);


    //////////////////////////////////////////////////////////////////////
    // receipt handling
    //////////////////////////////////////////////////////////////////////

    /**
     * Return the requested "chunk size" number of items from the
     * receipt list.
     */

    public String
	listReceiptsChunk (int chunk_size);


    //////////////////////////////////////////////////////////////////////
    // network model management
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the network node.
     */

    public org.jdom.Element
	getNetworkNode ();
	
    /**
     * Set the network node.
     */

    public void
	setNetworkNode (org.jdom.Element network_node);
	

    /**
     * Get the network number.
     */

    public String
	getNetworkNumber ();
	
    /**
     * Set the network number.
     */

    public void
	setNetworkNumber (String network_number);
	

    /**
     * Get the network identifier.
     */

    public org.opensims.IPv4
	getNetworkId ();


    /**
     * Set the network identifier.
     */

    public void
	setNetworkId (org.opensims.IPv4 network_id);


    /**
     * Get the device.
     */

    public String
    getDevice ();


    /**
     * Set the device.
     */

    public void
	setDevice (String device);
	

    /**
     * Get the permissive mode flag.
     */

    public boolean
	getPermissiveMode ();


    /**
     * Get the permissive mode flag.
     */

    public void
	setPermissiveMode (boolean permissive_mode);


    /**
     * Determine whether the given ip_addr is friend/foe/don't care
     */

    public org.opensims.model.HostOrBogey
	getHostOrBogey (org.opensims.IPv4 ip_addr);


    /**
     * Provides a test to see if the "scan" parameter has completed
     * Used for multiple scans running in parallel
     */

    public boolean
	isScanComplete ();


    //////////////////////////////////////////////////////////////////////
    // Tickable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Manager
     */

    public org.opensims.Manager
        getManager ();


    /**
     * Set the Manager
     */

    public void
        setManager (org.opensims.Manager manager);


    /**
     * Get the last_tick value - for expiry
     */

    public long
	getLastTick ();


    /**
     * Set the last_tick value - for expiry
     */

    public void
	setLastTick (long last_tick);


    /**
     * Get the hashtable lookup key
     */

    public String
	getKey ();


    /**
     * Check the expiry date/time
     */

    public boolean
	checkExpiry (long model_tick);


    //////////////////////////////////////////////////////////////////////
    // Exportable methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Format an XML node for export
     */

    public org.jdom.Element
	getExportNode ();
}
