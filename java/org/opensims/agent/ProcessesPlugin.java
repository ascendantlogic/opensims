/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: ProcessesPlugin.java,v 1.6 2004/10/01 05:27:53 paco Exp $
 * $Id: ProcessesPlugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.6 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    ProcessesPlugin
    extends org.opensims.agent.GenericPlugin
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.ProcessesPlugin.class.getName());


    //////////////////////////////////////////////////////////////////////
    // parsing methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse the incoming XML node sent from the Agent.
     */

    public boolean
	parseNode (String node_name, org.jdom.Element node)
    {
	boolean valid_transact = false;

	if (node_name.equals(org.opensims.xml.Node.PROCESS_LIST_NODE)) {
	    handleProcessList(node);
	    valid_transact = true;
	}

	return valid_transact;
    }


    /**
     * Parse an incoming <PROCESS_LIST/> node from an Agent.
     */

    protected void
	handleProcessList (org.jdom.Element nids)
    {
	/**
	 * @TODO do something with HIDS info (processes)
	 */
    }
}
