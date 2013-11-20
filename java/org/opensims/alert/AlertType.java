/**
 * @LICENSE@
 */

package org.opensims.alert;

/**
 * $CVSId: AlertType.java,v 1.6 2004/08/17 22:24:59 paco Exp $
 * $Id: AlertType.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.6 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    AlertType
{
    /**
     * Get the XML node
     */

    public org.jdom.Element
	getNode ();


    /**
     * Set the XML node
     */

    public void
	setNode (org.jdom.Element node);


    /**
     * Get the alert type id
     */

    public String
	getTypeId ();


    //////////////////////////////////////////////////////////////////////
    // reporting support
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the count for the current report
     */

    public long
	getCount ();


    /**
     * Set the count for the current report
     */

    public void
	setCount (long count);


    /**
     * Increment the count for the current report
     */

    public void
	incrementCount (long count);


    /**
     * Get the relative frequency for the current report.
     */

    public double
	getFrequency ();


    /**
     * Set the relative frequency for the current report
     */

    public void
	setFrequency (long sum_count);


    /**
     * Get the report data point index
     */

    public int
	getReportIndex ();


    /**
     * Set the report data point index
     */

    public void
	setReportIndex (int report_index);
}
