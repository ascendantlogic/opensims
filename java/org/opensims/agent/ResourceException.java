/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: ResourceException.java,v 1.1 2004/06/15 07:29:07 paco Exp $
 * $Id: ResourceException.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.1 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    ResourceException
    extends Exception
{
    /**
     * Constructor
     */

    public 
	ResourceException (String message)
    {
	super(message);
    }


    /**
     * Constructor
     */

    public 
	ResourceException (String message, Throwable cause)
    {
	super(message, cause);
    }
}
