/**
 * @LICENSE@
 */

package org.opensims.geoloc;

/**
 * $CVSId: GenericCountryInfo.java,v 1.3 2004/09/01 20:54:51 paco Exp $
 * $Id: GenericCountryInfo.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.3 $
 * @author Linas VARDYS <linas@symbiot.com>
 * @author Paco NATHAN <paco@symbiot.com>
 * <P>
 * Contains methods for looking up IP address geographical info.
 * </P>
 */

public class
    GenericCountryInfo
    implements org.opensims.geoloc.CountryInfo
{
    // protected fields

    protected String code = org.opensims.geoloc.CountryInfo.UNKNOWN_CODE;
    protected String name = org.opensims.geoloc.CountryInfo.UNKNOWN_NAME;
    protected boolean is_known = false;
    protected org.jdom.Element fixer_node = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.geoloc.GenericCountryInfo.class.getName());


    /**
     * Get the country code
     */

    public String
	getCode ()
    {
	return code;
    }


    /**
     * Set the country code
     */

    public void
	setCode (String code)
    {
	this.code = code;
    }


    /**
     * Get the country name
     */

    public String
	getName ()
    {
	return name;
    }


    /**
     * Set the country name
     */

    public void
	setName (String name)
    {
	this.name = name;
    }


    /**
     * Get whether the country is known
     */

    public boolean
	getIsKnown ()
    {
	return is_known;
    }


    /**
     * Set whether the country is known
     */

    public void
	setIsKnown (boolean is_known)
    {
	this.is_known = is_known;
    }


    /**
     * Get the fixer node.
     */

    public org.jdom.Element
	getFixerNode ()
    {
	return fixer_node;
    }


    /**
     * Set the fixer node.
     */

    public void
	setFixerNode (org.jdom.Element fixer_node)
    {
	this.fixer_node = fixer_node;
    }


    /**
     * Return a String representation
     */

    public String
	toString ()
    {
	StringBuffer buf = new StringBuffer();

	buf.append(getCode());
	buf.append(":");
	buf.append(getName());

	return buf.toString();
    }
}
