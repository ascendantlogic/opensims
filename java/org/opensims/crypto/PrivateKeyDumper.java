/**
 * @LICENSE@
 */

package org.opensims.crypto;

/**
 * $CVSId: PrivateKeyDumper.java,v 1.4 2004/09/01 20:54:51 paco Exp $
 * $Id: PrivateKeyDumper.java 1 2008-01-10 18:37:05Z smoot $
 * Dump a private key from a Java keystore
 * @version $Revision: 1.4 $
 * @author Mike ERWIN <mikee@symbiot.com>
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    PrivateKeyDumper
{
    // local definitions

    protected final static int MAX_ARGS = 5;


    public static void
	main (String[] args)
    {
	//org.apache.log4j.BasicConfigurator.configure(new org.apache.log4j.ConsoleAppender(new org.apache.log4j.PatternLayout()));

	try {
	    if (args.length < (MAX_ARGS - 1)) {
		System.err.println("usage: java PrivateKeyDumper <alias> <keystore_file> <keystore_pass> <key_pass> [ <file_out> ]");
		System.exit(1);
	    }
	    else {
		String alias = args[0];
		String keystore_file = args[1];
		String keystore_pass = args[2];
		String key_pass = args[3];
		String file_name = args[4];

		java.security.KeyStore keystore = java.security.KeyStore.getInstance("jks");

		keystore.load(new java.io.FileInputStream(keystore_file), keystore_pass.toCharArray());

		java.security.Key key = keystore.getKey(alias, key_pass.toCharArray());

		if (args.length < MAX_ARGS) {
		    System.out.write(key.getEncoded());
		}
		else {
		    java.io.FileOutputStream file_out = new java.io.FileOutputStream(file_name);
		    file_out.write(key.getEncoded());
		}
	    }
	}
	catch (Exception e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }
}
