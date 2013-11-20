/**
 * @LICENSE@
 */

package org.opensims.servlet;

/**
 * $CVSId: ServletException.java,v 1.1 2004/09/18 06:07:22 paco Exp $
 * $Id: ServletException.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.1 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    ServletException
    extends Exception
{
    /**
     * Constructor
     */

    public 
	ServletException (String message)
    {
	super(message);
    }


    /**
     * Constructor
     */

    public 
	ServletException (String message, Throwable cause)
    {
	super(message, cause);
    }
}
