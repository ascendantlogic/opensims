/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: LoginException.java,v 1.1 2004/06/15 07:29:07 paco Exp $
 * $Id: LoginException.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.1 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    LoginException
    extends Exception
{
    /**
     * Constructor
     */

    public 
	LoginException (String message)
    {
	super(message);
    }


    /**
     * Constructor
     */

    public 
	LoginException (String message, Throwable cause)
    {
	super(message, cause);
    }
}
