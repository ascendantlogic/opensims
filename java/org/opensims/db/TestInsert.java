/**
 * @LICENSE@
 */

package org.opensims.db;

/**
 * $CVSId: TestInsert.java,v 1.2 2004/08/10 07:50:02 paco Exp $
 * $Id: TestInsert.java 1 2008-01-10 18:37:05Z smoot $
 * OpenSIMS - Torque object relational mapping INSERT test
 *
 * @author Paco NATHAN <paco@symbiot.com>
 *
 * <P>
 * Kudos to Ales Vaupotic for a great Torque tutorial: http://users.volja.net/amanita1/
 * </P>
 */

public class
    TestInsert
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

	    // add a host
        
	    org.opensims.db.om.Host host1 = new org.opensims.db.om.Host();

	    host1.setIpAddr("192.207.27.6");
	    host1.setNetwork("19950000");
	    host1.setCreated(new java.util.Date());
	    host1.save();

	    if (debug > 0) {
		System.out.println("added: " + host1.toString());
	    }

	    // add a bogey

	    org.opensims.db.om.Host host2 = new org.opensims.db.om.Host();

	    host2.setIpAddr("11.1.1.240");
	    host2.setCreated(new java.util.Date());
	    host2.save();

	    if (debug > 0) {
		System.out.println("added: " + host2.toString());
	    }
        }
        catch (Exception e) {
	    e.printStackTrace();
        }
    }
}
