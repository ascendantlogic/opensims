/**
 * @LICENSE@
 */

package org.opensims.db;

/**
 * $CVSId: TestSelect.java,v 1.1 2004/08/09 14:37:27 paco Exp $
 * $Id: TestSelect.java 1 2008-01-10 18:37:05Z smoot $
 * OpenSIMS - Torque object relational mapping SELECT test
 *
 * @author Paco NATHAN <paco@symbiot.com>
 *
 * <P>
 * Kudos to Ales Vaupotic for a great Torque tutorial: http://users.volja.net/amanita1/
 * </P>
 */

public class
    TestSelect
{
    private final static int debug = 1; // 0


    /**
     * command line test
     */

    public static void
	main (String[] args)
    {
	try {
	    // initialization

	    String torque_props = args[0];
	    org.apache.torque.Torque.init(torque_props);

	    // show all

	    if (debug > 0) {
		System.out.println("the host-or-bogey entries: ");
	    }

	    java.util.List host_list = org.opensims.db.om.HostPeer.doSelectAll();
	    printHost(host_list);

	    // select only bogeys

	    if (debug > 0) {
		System.out.println("the bogey entries only:");
	    }

	    org.apache.torque.util.Criteria crit = new org.apache.torque.util.Criteria();
	    crit.add(org.opensims.db.om.HostPeer.NETWORK, null);

	    host_list = org.opensims.db.om.HostPeer.doSelect(crit);

	    if (debug > 0) {
		printHost(host_list);
	    }
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /**
     * Helper method to print a list to standard out.
     */

    private static void
	printHost (java.util.List list)
	throws Exception
    {
	java.util.Iterator host_iter = list.iterator();

	while (host_iter.hasNext()) {
	    org.opensims.db.om.Host host = (org.opensims.db.om.Host) host_iter.next();
	    System.out.println(host);
	}
    }
}
