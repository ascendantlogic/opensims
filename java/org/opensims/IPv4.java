/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: IPv4.java,v 1.7 2004/10/11 04:52:00 paco Exp $
 * $Id: IPv4.java 1 2008-01-10 18:37:05Z smoot $
 * IPv4 address calculations with CIDR support
 * @version $Revision: 1.7 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Jim NASBY <jnasby@symbiot.com>
 * @author Matt BRADBURY <bradbury@symbiot.com>
 * @author Linas VARDYS <linas@symbiot.com>
 */

public class
    IPv4
    extends Object
{
    // public final statics

    public final static int FULL_CIDR = 32;
    public final static long DEFAULT_MASK = 0x00000000FFFFFFFF;
    
    // constants used for packaging ips in cidr2ips

    public final static String NETWORK = "NETWORK";
    public final static String BROADCAST = "BROADCAST";
    
    // protected fields

    protected long addr_network = 0;
    protected long addr_broadcast = 0;

    protected int cidr = 0;
    protected long mask = DEFAULT_MASK;

    // quality assurance

    //private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.Config.class.getName());
    private final static int debug_ = 0; // 1


    //////////////////////////////////////////////////////////////////////
    // constructors
    //////////////////////////////////////////////////////////////////////

    /**
     * Constructor -
     * individual IP address
     */

    public
 	IPv4 (long addr_network)
	throws Exception
    {
	parseAddressBlock(addr_network, addr_network);
    }


    /**
     * Constructor -
     * IP address range
     */

    public
 	IPv4 (long addr_network, long addr_broadcast)
	throws Exception
    {
	parseAddressBlock(addr_network, addr_broadcast);
    }


    /**
     * Constructor -
     * individual IP address
     */

    public
 	IPv4 (Integer addr_network)
	throws Exception
    {
	parseAddressBlock(addr_network.longValue(), addr_network.longValue());
    }


    /**
     * Constructor -
     * IP address range
     */

    public
 	IPv4 (Integer addr_network, Integer addr_broadcast)
	throws Exception
    {
	parseAddressBlock(addr_network.longValue(), addr_broadcast.longValue());
    }


    /**
     * Constructor -
     * individual IP address or range (depending on optional CIDR)
     */

    public
 	IPv4 (String addr_str)
	throws Exception
    {
	String cidr_str = "0";
	String dotted_quad = addr_str;

	cidr = FULL_CIDR;

	// attempt to parse a network mask value

	if (addr_str.indexOf("/") >= 0) {
	    java.util.StringTokenizer st = new java.util.StringTokenizer(addr_str, "/", false);
	    String token = st.nextToken().trim();

	    if (token.length() > 0) {
		dotted_quad = token;
	    }

	    if (st.hasMoreTokens()) {
		token = st.nextToken().trim();

		if (token.length() > 0) {
		    cidr_str = token;
		}
	    }

	    cidr = Integer.parseInt(cidr_str);
	    calculateMask();
	}

	long delta = (1 << (FULL_CIDR - cidr)) - 1;

	addr_network = quadToLong(dotted_quad) & mask;
	addr_broadcast = addr_network + delta;

	if (debug_ > 0) {
	    System.err.println("IPv4: addr " + Long.toHexString(addr_network) + " - " + Long.toHexString(addr_broadcast) + " mask " + Long.toHexString(mask) + " cidr " + cidr + " delta " + delta);
	}
    }


    /**
     * Constructor -
     * IP address range
     */

    public
 	IPv4 (String addr_network, String addr_broadcast)
	throws Exception
    {
	parseAddressBlock(quadToLong(addr_network), quadToLong(addr_broadcast));
    }


    /**
     * Factory method, to create a CIDR from an IP address and its netmask.
     */

    public static IPv4
	mask2cidr (String ip_addr_str, String net_mask_str)
	throws Exception
    {
	IPv4 ip_addr = new IPv4(ip_addr_str);
	IPv4 net_mask = new IPv4(net_mask_str);

	long nm = ip_addr.getNetworkAddr() & net_mask.getNetworkAddr();
	long bc = nm + (0xFFFFFFFF ^ net_mask.getNetworkAddr());

	IPv4 cidr = new IPv4(longToQuad(nm), longToQuad(bc));

	return cidr;
    }


    //////////////////////////////////////////////////////////////////////
    // internal parsing methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse a network address block
     */

    protected void
 	parseAddressBlock (long addr_network, long addr_broadcast)
	throws Exception
    {
	this.addr_network = addr_network;
	this.addr_broadcast = addr_broadcast;

	if (addr_network < 0) {
	    throw new Exception("invalid network address value: " + addr_network);
	}
	else if (addr_broadcast < 0) {
	    throw new Exception("invalid broadcast address value: " + addr_broadcast);
	}
	else if (addr_network > addr_broadcast) {
	    throw new Exception("network address cannot be higher than broadcast address");
	}
	else if (addr_network == addr_broadcast) {
	    cidr = FULL_CIDR;
	    //mask = DEFAULT_MASK
	}
	else {
	    cidr = FULL_CIDR - (int) (Math.log((double) (addr_broadcast - addr_network + 1)) / Math.log(2.0));
	    calculateMask();
	}

	if (debug_ > 0) {
	    System.err.println("IPv4: addr " + Long.toHexString(addr_network) + " - " + Long.toHexString(addr_broadcast) + " mask " + Long.toHexString(mask) + " cidr " + cidr);
	}
    }


    /**
     * Set the mask based on the current CIDR value
     */

    protected void
	calculateMask ()
    {
	if ((cidr != 0xFFFFFFFF) && (cidr > 0)) {
	    mask = 0xFFFFFFFF << (FULL_CIDR - cidr);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // set operations
    //////////////////////////////////////////////////////////////////////

    /**
     * Test the given IP address value, returning true if it fits within
     * the network mask
     */

    public boolean
	includes (IPv4 test_addr)
    {
	return includes(test_addr.getNetworkAddr());
    }


    /**
     * Test the given IP address value, returning true if it fits within
     * the network mask
     */

    public boolean
	includes (long test_addr)
    {
	if (debug_ > 0) {
	    System.err.println("IPv4: addr " + Long.toHexString(addr_network) + " mask " + Long.toHexString(mask) + " test " + Long.toHexString(test_addr));
	}

	return ((mask & addr_network) == 0) || ((test_addr & mask) == addr_network);
    }


    //////////////////////////////////////////////////////////////////////
    // access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get a numeric representation for the network address
     */

    public long
	getNetworkAddr ()
    {
	return addr_network;
    }


    /**
     * Get a numeric representation for the broadcast address
     */

    public long
	getBroadcastAddr ()
    {
	return addr_broadcast;
    }


    /**
     * Get the CIDR value
     */

    public int
	getCIDR ()
    {
	return cidr;
    }


    /**
     * Get a string representation with CIDR
     */

    public String
	toString ()
    {
    	return getString();
    }


    /**
     * Same as toString()
     */

    public String
	getString ()
    {
	String s = longToQuad(addr_network);

	if ((cidr > 0) &&
	    (cidr < FULL_CIDR)
	    ) {
	    s += "/" + cidr;
	}

	return s;
    }


    //////////////////////////////////////////////////////////////////////
    // conversion utility methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Parse a dotted quad to get its 32 bit value
     */

    public static long
	quadToLong (String dotted_quad)
    {
	long addr = 0;
	java.util.StringTokenizer st = new java.util.StringTokenizer(dotted_quad, ".", false);

	for (int i = 0; i < 4; i++) {
	    long quad = 0;

	    if (st.hasMoreTokens()) {
		String quad_str = st.nextToken().trim();

		try {
		    quad = Long.parseLong(quad_str);
		}
		catch (NumberFormatException nfe) {
		    System.err.println("IPv4: bad number format for quad: " + quad_str);
		    System.err.println("IPv4: " + nfe.getMessage());
		    nfe.printStackTrace();
		}
	    }

	    addr = (addr << 8) | quad;
	}

	return addr;
    }


    /**
     * Get a dotted quad string representation for the given 32-bit value
     */

    public static String
	longToQuad (long addr)
    {
	String s = Long.toString((addr >> 24) & 0xFF);

	s += "." + Long.toString((addr >> 16) & 0xFF);
	s += "." + Long.toString((addr >> 8) & 0xFF);
	s += "." + Long.toString(addr & 0xFF);

	return s;
    }


    /**
     * Parse a hex string to its 32-bit integer value
     */

    public static long
	hexToLong (String addr)
    {
	return Long.valueOf(addr, 16).longValue();
    }


    /**
     * Parse a 32-bit address into its hex representation
     */

    public static String
	longToHex (long addr)
    {
        return Long.toHexString(addr);
    }


    /**
     * Parse a 32-bit hex address
     */

    public static IPv4
	parseHex (String addr_network)
	throws Exception
    {
	return new IPv4(hexToLong(addr_network));
    }


    /**
     * Parse a 32-bit hex address range
     */

    public static IPv4
	parseHex (String addr_network, String addr_broadcast)
	throws Exception
    {
	return new IPv4(hexToLong(addr_network), hexToLong(addr_broadcast));
    }


    //////////////////////////////////////////////////////////////////////
    // Oracle stored procedure support
    //////////////////////////////////////////////////////////////////////

    /**
     * function cidrToLong (cidr string, network[] long, broadcast[] long) returns boolean ;
     */

    public static boolean
	cidrToLong (String cidr_addr, long[] addr_network, long[] addr_broadcast)
    {
	boolean result = true;

	try {
	    IPv4 ip = new IPv4(cidr_addr);
	    addr_network[0] = ip.getNetworkAddr();
	    addr_broadcast[0] = ip.getBroadcastAddr();
	}
	catch (Exception e) {
	    if (debug_ > 0) {
		System.err.println("IPv4: " + e.getMessage());
		e.printStackTrace();
	    }

	    result = false;
	}

	return result;
    }

    /**
     * function cidrToHex (cidr string, network[] hex, broadcast[] hex) returns boolean ;
     */

    public static boolean
	cidrToHex (String cidr_addr, String[] addr_network, String[] addr_broadcast)
    {
	boolean result = true;

	try {
	    IPv4 ip = new IPv4(cidr_addr);
	    addr_network[0] = longToHex(ip.getNetworkAddr());
	    addr_broadcast[0] = longToHex(ip.getBroadcastAddr());
	}
	catch (Exception e) {
	    if (debug_ > 0) {
		System.err.println("IPv4: " + e.getMessage());
		e.printStackTrace();
	    }

	    result = false;
	}

	return result;
    }


    /**
     * function longToCidr (network long, broadcast long, cidr[] string) returns boolean ;
     */

    public static boolean
	longToCidr (long addr_network, long addr_broadcast, String[] cidr_addr)
    {
	boolean result = true;

	try {
	    IPv4 ip = new IPv4(addr_network, addr_broadcast);
	    cidr_addr[0] = ip.toString();
	}
	catch (Exception e) {
	    if (debug_ > 0) {
		System.err.println("IPv4: " + e.getMessage());
		e.printStackTrace();
	    }

	    result = false;
	}

	return result;
    }


    /**
     * function ipToHex (ip string, hex[] string) returns boolean ;
     */

    public static boolean
	ipToHex (String ip_addr, String[] hex_addr)
    {
	boolean result = true;

	try {
	    IPv4 ip = new IPv4(ip_addr);
	    hex_addr[0] = longToHex(ip.getNetworkAddr());
	}
	catch (Exception e) {
	    if (debug_ > 0) {
		System.err.println("IPv4: " + e.getMessage());
		e.printStackTrace();
	    }

	    result = false;
	}

	return result;
    }


    /**
     * function hexToIp (hex string, ip[] string) returns boolean ;
     */

    public static boolean
	hexToIp (String hex_addr, String[] ip_addr)
    {
	boolean result = true;

	try {
	    IPv4 ip = new IPv4(hexToLong(hex_addr));
	    ip_addr[0] = ip.toString();
	}
	catch (Exception e) {
	    if (debug_ > 0) {
		System.err.println("IPv4: " + e.getMessage());
		e.printStackTrace();
	    }

	    result = false;
	}

	return result;
    }


    /**
     * This static method accepts two longs and returns a CIDR string.
     * @param ip_begin_long
     * @param ip_broadcast_long
     * @return
     * @throws Exception Garbage In, Garbage Out
     */
    public static String ips2cidr(long ip_begin_long, long ip_broadcast_long) throws Exception{
    	String cidr_string = "";
    	IPv4 ip = new IPv4(ip_begin_long, ip_broadcast_long);
    	return ip.toString();
    }//

    /**
     * This static method takes two string representaions composing a network range,
     * and returns a CIDR string representing the two.
     * @param ip_begin_string
     * @param ip_broadcast_string
     * @return
     * @throws Exception
     */
    public static String ips2cidr(String ip_begin_string, String ip_broadcast_string) throws Exception{
    	return ips2cidr(IPv4.quadToLong(ip_begin_string), IPv4.quadToLong(ip_broadcast_string));
    }//	
    
    
    /**
     * This static method accepts a CIDR String and returns a HashMap contining the following:
     * <ul>
     * <li>Key=IPv4.NETWORK, Value=String - Beginning IP address of network.</li>
     * <li>Key=IPv4.BROADCAST, Value=String - Broadcast address of network.</li>
     * </ul>
     * @param cidr_string
     * @return java.util.HashMap of values
     */
    public static java.util.HashMap cidr2ips(String cidr_string) {
    	java.util.HashMap result = new java.util.HashMap();
    	
    	String ip_network = null;
    	String ip_broadcast = null;
    	try {
    		IPv4 ip_source = new IPv4(cidr_string);
    		ip_broadcast =  new IPv4(ip_source.getBroadcastAddr()).toString();
    		ip_network =  new IPv4(ip_source.getNetworkAddr()).toString();
    		
    	} catch (Exception e) {
    		throw new IllegalArgumentException(cidr_string + " is not a valid network.");
    	}
    	
    	result.put(IPv4.NETWORK, ip_network);
    	result.put(IPv4.BROADCAST, ip_broadcast);
   	
    	return result;
    }//
    
    
    //////////////////////////////////////////////////////////////////////
    // Q/A methods
    //////////////////////////////////////////////////////////////////////

    /**
     * command line test interface
     */

    public static void
	main (String args[])
	throws Exception
    {
	String ip_addr_str = args[0];
	String net_mask_str = args[1];

	IPv4 ip_addr = new IPv4(ip_addr_str);
	IPv4 net_mask = new IPv4(net_mask_str);

	long nm = ip_addr.getNetworkAddr() & net_mask.getNetworkAddr();
	long bc = nm + (0xFFFFFFFF ^ net_mask.getNetworkAddr());

	IPv4 cidr = new IPv4(longToQuad(nm), longToQuad(bc));

	System.out.println("ip_addr: " + ip_addr.toString());
	System.out.println("net_mask: " + net_mask.toString());
	System.out.println("net_addr: " + longToQuad(nm));
	System.out.println("broadcast: " + longToQuad(bc));

	System.out.println("cidr: " + cidr.toString());
	System.out.println("cidr: " + mask2cidr(ip_addr_str, net_mask_str));

	/**
	 * @TODO previous unit tests

	IPv4 z = new IPv4(args[0]);
	long test_addr = (new IPv4(args[1])).getNetworkAddr();

	System.err.println("str  = " + z.getString());
	System.err.println("long = " + z.getNetworkAddr());
	System.err.println("quad = " + longToQuad(z.getNetworkAddr()));
	System.err.println("hex  = " + longToHex(z.getNetworkAddr()));
	System.err.println("stop = " + longToHex(z.getBroadcastAddr()));
	System.err.println("cidr = " + z.getCIDR());
	System.err.println("includes: " + z.includes(test_addr));

	boolean result;

	String hex_addr[] = new String[1];
	result = ipToHex(args[1], hex_addr);
	System.err.println("ipToHex: " + result + " " + hex_addr[0]);

	String ip_addr[] = new String[1];
	result = hexToIp(args[2], ip_addr);
	System.err.println("hexToIp: " + result + " " + ip_addr[0]);

	String cidr_addr[] = new String[1];
	//result = longToCidr(args[2], args[3], cidr_addr);
	System.err.println("longToCidr: " + result + " " + cidr_addr[0]);

	String addr_network[] = new String[1];
	String addr_broadcast[] = new String[1];
	//result = cidrToLong(args[0], addr_network, addr_broadcast);
	System.err.println("cidrToLong: " + result + " " + addr_network[0] + " - " + addr_broadcast[0]);

	 */
    }
}
