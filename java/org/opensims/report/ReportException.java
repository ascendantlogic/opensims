/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: ReportException.java,v 1.1 2004/09/01 23:32:14 paco Exp $
 * $Id: ReportException.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.1 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    ReportException
    extends Exception
{
    /**
     * Constructor
     */

    public 
	ReportException (String message)
    {
	super(message);
    }


    /**
     * Constructor
     */

    public 
	ReportException (String message, Throwable cause)
    {
	super(message, cause);
    }
}
