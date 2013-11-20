/**
 * @LICENSE@
 */

package org.opensims.geoloc;

/**
 * $CVSId: CountryInfo.java,v 1.3 2004/09/01 20:54:51 paco Exp $
 * $Id: CountryInfo.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.3 $
 * @author Linas VARDYS <linas@symbiot.com>
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>
 * Contains methods for looking up IP address geographical info.
 * </P>
 */

public interface
    CountryInfo
{
    // public definitions

    public final static String UNKNOWN_CODE = "??";
    public final static String UNKNOWN_NAME = "unknown";


    /**
     * Get the country code
     */

    public String
	getCode ();


    /**
     * Set the country code
     */

    public void
	setCode (String code);


    /**
     * Get the country name
     */

    public String
	getName ();


    /**
     * Set the country name
     */

    public void
	setName (String name);


    /**
     * Get whether the country is known
     */

    public boolean
	getIsKnown ();


    /**
     * Set whether the country is known
     */

    public void
	setIsKnown (boolean is_known);


    /**
     * Get the fixer node.
     */

    public org.jdom.Element
	getFixerNode ();


    /**
     * Set the fixer node.
     */

    public void
	setFixerNode (org.jdom.Element fixer_node);


    /**
     * Return a String representation
     */

    public String
	toString ();
}
