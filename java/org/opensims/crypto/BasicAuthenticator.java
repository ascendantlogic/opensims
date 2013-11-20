/**
 * @LICENSE@
 */

package org.opensims.crypto;

/**
 * $CVSId: BasicAuthenticator.java,v 1.1 2004/10/19 00:33:29 paco Exp $
 * $Id: BasicAuthenticator.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.1 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    BasicAuthenticator
    extends java.net.Authenticator
{
    // protected fields

    protected String user = null;
    protected String password = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.crypto.BasicAuthenticator.class.getName());


    /**
     * Constructor.
     */

    public
	BasicAuthenticator (String user, String password)
    {
	setUser(user);
	setPassword(password);
    }


    /**
     * Get the user.
     */

    public String
	getUser ()
    {
	return user;
    }


    /**
     * Set the user.
     */

    public void
	setUser (String user)
    {
	this.user = user;
    }


    /**
     * Get the password.
     */

    public String
	getPassword ()
    {
	return password;
    }


    /**
     * Set the password.
     */

    public void
	setPassword (String password)
    {
	this.password = password;
    }


    /**
     * Return the user/password pair.
     */

    protected java.net.PasswordAuthentication
	getPasswordAuthentication ()
    {
	return new java.net.PasswordAuthentication(getUser(), getPassword().toCharArray());
    }
}
