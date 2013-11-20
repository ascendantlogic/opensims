/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: EffectorPlugin.java,v 1.5 2004/10/01 05:27:53 paco Exp $
 * $Id: EffectorPlugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.5 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    EffectorPlugin
    extends org.opensims.agent.GenericPlugin
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.EffectorPlugin.class.getName());


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

	if (node_name.equals(org.opensims.xml.Node.APP_RESULTS_NODE)) {
	    handleAppResults(node);
	    valid_transact = true;
	}

	return valid_transact;
    }


    /**
     * Parse an incoming <APP_RESULTS/> node from an Agent.
     */

    protected void
	handleAppResults (org.jdom.Element node)
    {
	// do something with the results
    }
}
