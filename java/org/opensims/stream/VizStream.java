/**
 * @LICENSE@
 */

package org.opensims.stream;

/**
 * $CVSId: VizStream.java,v 1.11 2005/11/25 21:13:47 mikee Exp $
 * $Id: VizStream.java 1 2008-01-10 18:37:05Z smoot $
 * Stream for attacker/defender visualization
 * @version $Revision: 1.11 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Lindsey SIMON <lsimon@symbiot.com>
 * @author Mike W. Erwin <mikee@symbiot.com>
 */

public class
    VizStream
    extends org.opensims.stream.GenericStream
{
    // public definitions

    public final static int DEFAULT_THROTTLE = 15;
    public final static String TOPOLOGY_PERIMETER = "perimeter";
    public final static String TOPOLOGY_INTERNAL = "internal";
    public final static String TOPOLOGY_MODEL = "model";

    // protected fields

    protected int alert_throttle = DEFAULT_THROTTLE;
    protected String threat_topology = TOPOLOGY_PERIMETER;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.stream.VizStream.class.getName());


    /**
     * Initialize from a config node
     */

    public void
	init (org.opensims.Correlator correlator, org.opensims.client.Subscriber subscriber, String name, org.jdom.Element node)
    {
		super.init(correlator, subscriber, name, node);
		setThreatTopology(org.opensims.Config.fixNull(getNode().getAttributeValue("threat"), org.opensims.stream.VizStream.TOPOLOGY_PERIMETER).trim());
	
		try {
			setAlertThrottle(Integer.valueOf(getNode().getAttributeValue("throttle")).intValue());
		}
		catch (Exception e) {
			setAlertThrottle(org.opensims.stream.VizStream.DEFAULT_THROTTLE);
		}
	
		// add some delay, initially, to allow the INIT stream to
		// reach the client first
		if (getDelayPeriod() == 0L) {
			setDelayPeriod(org.opensims.stream.GenericStream.DEFAULT_DELAY_PERIOD);
		}
    }


    //////////////////////////////////////////////////////////////////////
    // access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Set the value for alert throttle
     */

    public void
	setAlertThrottle (int alert_throttle)
    {
		this.alert_throttle = alert_throttle;
    }


    /**
     * Get the value for alert throttle
     */

    public int
	getAlertThrottle ()
    {
		return alert_throttle;
    }


    /**
     * Set the indicator for threat topology
     */

    public void
	setThreatTopology (String threat_topology)
    {
		this.threat_topology = threat_topology;
    }


    /**
     * Get the indicator for threat topology
     */

    public String
	getThreatTopology ()
    {
		return threat_topology;
    }


    //////////////////////////////////////////////////////////////////////
    // content methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Build a content document
     *
     * @TODO switch on the threat_topology to show "internal", "model", etc.
     */

    public org.jdom.Document
	buildContentDocument (long tick)
    {
		org.jdom.Document content_doc = null;
	
		// use the activity window and other criteria to filter the
		// alert/notify streams
		java.util.ArrayList scan_list = getCorrelator().getScanList();
		java.util.ArrayList alert_list = getCorrelator().getAlertList(this, tick, getAlertThrottle());
	
		// render NET/ATK/DEF/ALT/CON as XML, to build a content node,
		// then apply XSL transforms
		content_doc = getCorrelator().buildContentDocument(tick, scan_list, alert_list);
	
		return content_doc;
    }
}
