/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: TesterPlugin.java,v 1.8 2004/10/01 05:27:53 paco Exp $
 * $Id: TesterPlugin.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.8 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    TesterPlugin
    extends org.opensims.agent.GenericPlugin
{
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.agent.TesterPlugin.class.getName());


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

	if (node_name.equals(org.opensims.xml.Node.TEST_MESSAGE_NODE)) {
	    handleTestMessage(node);
	    valid_transact = true;
	}

	return valid_transact;
    }


    /**
     * Parse an incoming <TEST_MESSAGE/> node from an agent ... save
     * the XML input as test data, e.g. from an agent, into a local
     * file ... to use for regression testing.
     */

    protected void
	handleTestMessage (org.jdom.Element node)
    {
	try {
	    java.io.File temp_dir = new java.io.File(getCorrelator().getConfig("temp.dir"));
	    java.io.File test_file = new java.io.File(temp_dir, getCorrelator().getConfig("symplugin-tester.test_file"));

	    java.io.FileWriter file_writer = new java.io.FileWriter(test_file);
	    java.io.PrintWriter print_writer = new java.io.PrintWriter(new java.io.BufferedWriter(file_writer));

	    String xml = org.opensims.xml.XmlBuilder.formatXML(node, false, true);
	    print_writer.print(xml);

	    print_writer.flush();
	    print_writer.close();
	    file_writer.close();
	}
	catch (Exception e) {
	    log_.error("error saving test vector", e);
	}
    }
}
