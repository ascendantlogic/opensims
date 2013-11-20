/**
 * @LICENSE@
 */

package org.opensims.agent;

/**
 * $CVSId: ProtocolException.java,v 1.1 2004/06/15 07:29:07 paco Exp $
 * $Id: ProtocolException.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.1 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    ProtocolException
    extends Exception
{
    /**
     * Constructor
     */

    public 
	ProtocolException (String message)
    {
	super(message);
    }


    /**
     * Constructor
     */

    public 
	ProtocolException (String message, Throwable cause)
    {
	super(message, cause);
    }
}
