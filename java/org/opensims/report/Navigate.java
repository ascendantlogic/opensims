/**
 * @LICENSE@
 */

package org.opensims.report;

/**
 * $CVSId: Navigate.java,v 1.16 2006/07/26 23:12:29 jeff Exp $
 * $Id: Navigate.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.16 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. Erwin <mikee@symbiot.com>
 * @author Jeff PIHL <jeff@outer.net>
 */

public class
    Navigate
{
    // protected fields

    protected org.opensims.Correlator correlator = null;
//    protected javax.servlet.http.HttpServletRequest request = null;
    protected org.jdom.Document document = null;
    protected java.util.Hashtable param_table = new java.util.Hashtable();
    protected boolean enabled = true;
	protected java.util.Hashtable request_param_table = null;
	
    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.report.Navigate.class.getName());

	// private fields

	private	String username;
	private String organization;
	private boolean isAdmin;


    /**
     * Constructor.
     */

    public
	Navigate (org.opensims.Correlator correlator, javax.servlet.http.HttpServletRequest request)
	throws org.opensims.report.ReportException
    {
    	// Force "admin" in circumstances where full authentication hasn't been overriden 
    	if (request.getRemoteUser() == null)
    		this.username = "admin";
    	else
			this.username = request.getRemoteUser();
	
		setCorrelator(correlator);
		
		this.organization = getCorrelator().getServlet().getNeuromancer().lookupGroupByUser(this.username);
		dupRequestParameters(request);
		
		isAdmin = request.isUserInRole(org.opensims.servlet.Neuromancer.ROLE_ADMIN);

        try {
            java.io.File xsl_dir = new java.io.File(getCorrelator().getConfig("webapp.dir"), "xsl");
            java.io.File driver_file = new java.io.File(xsl_dir, "rpt_navigate.xml");

		    java.io.FileReader file_reader = new java.io.FileReader(driver_file);
            java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);

            setDocument(getCorrelator().buildXml(buf_reader));

			// set the standard parameters	
			propagateParamValue("name", getRawParam("name"));
			saveParamValue("content_type");
			saveParamValue("order_by");
			saveParamValue("order_dir");
			saveParamValue("result_limit");
			saveParamValue("time_limit");
			saveDateValue("from_date");
			saveDateValue("to_date");

            if (log_.isDebugEnabled()) {
                String xml = org.opensims.xml.XmlBuilder.formatXML(getDocument().getRootElement(), false, true);
                log_.debug(xml);
	    	}
        }
        catch (Exception e) {
            log_.error("load navigation driver", e);
            throw new org.opensims.report.ReportException(e.getMessage());
        }
    }

/* obsolete
    public
	Navigate ( org.opensims.Correlator correlator, String name, String time_limit, String order_by, String order_dir, String result_limit )
	throws org.opensims.report.ReportException
    {
		this.username = "admin";
		setCorrelator(correlator);
		this.organization = "admin_group";
		isAdmin = true;

        try {
            java.io.File xsl_dir = new java.io.File(getCorrelator().getConfig("webapp.dir"), "xsl");
            java.io.File driver_file = new java.io.File(xsl_dir, "rpt_navigate.xml");

			java.io.FileReader file_reader = new java.io.FileReader(driver_file);
            java.io.BufferedReader buf_reader = new java.io.BufferedReader(file_reader);

            setDocument(getCorrelator().buildXml(buf_reader));

			// set the standard parameters	
			propagateParamValue("name", name);
			saveParamValue("order_by", order_by);
			saveParamValue("order_dir", order_dir);
			saveParamValue("result_limit", result_limit);
			saveParamValue("time_limit", time_limit);

            if (log_.isDebugEnabled()) {
                String xml = org.opensims.xml.XmlBuilder.formatXML(getDocument().getRootElement(), false, true);
                log_.debug(xml);
			}
        }
        catch (Exception e) {
            log_.error("load navigation driver", e);
            throw new org.opensims.report.ReportException(e.getMessage());
        }
    }
*/

    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the Correlator.
     */

    public org.opensims.Correlator
	getCorrelator ()
    {
		return correlator;
    }


    /**
     * Set the Correlator.
     */

    public void
	setCorrelator (org.opensims.Correlator correlator)
    {
		this.correlator = correlator;
    }


    /**
     * Get a local copy of the HTTP request parameters.
     */

    public void
	dupRequestParameters (javax.servlet.http.HttpServletRequest request)
    {
	    //todo capture compound parameters (getParameterValues)
	    //request_param_table = new java.util.Hashtable( request.getParameterMap() );
		request_param_table = new java.util.Hashtable();

		java.util.Enumeration e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String k = (String) e.nextElement();
			String v = request.getParameter(k);
			request_param_table.put( k, v );
		}		
    }

	/**
	 * get the http request (our copy) value of the parameter
	 */
	 
	public String
	getParameter( String param_name )
	{
		return (String) request_param_table.get( param_name );
	}

	public java.util.Date
	getDate( String param_name )
	{
		java.util.Date date = null;
		String param_value = getParameter( param_name );
		if ( param_value != null ) {
			java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat("yyyy-MM-dd");
			try {
				date = date_format.parse(param_value);
			} catch (java.text.ParseException e ) {
				date = null;
			}
		}
		return date;

	}
	
    /**
     * Get the navigation parameters represented as an XML document.
     */

    public org.jdom.Document
	getDocument ()
    {
		return document;
    }


    /**
     * Set the navigation parameters represented as an XML document.
     */

    public void
	setDocument (org.jdom.Document document)
    {
		this.document = document;
    }


    /**
     * Get the named parameter value.
     */

    public String
	getParamValue (String param_name)
    {
		return (String) param_table.get(param_name);
    }


    /**
     * Save the named parameter value based on the HTTP request.
     */

    public void
	saveParamValue (String param_name)
    {
		String test_value = (String) getParameter(param_name); 
		org.jdom.Element option_node = lookupOption(param_name, test_value);
	
		if (option_node == null) {
			// go for the default value instead
			option_node = lookupOption(param_name, null);
		}
	
		String param_value = option_node.getAttributeValue("value");
		saveParamValue( param_name, param_value );
    }


    /**
     * Set the named parameter with the value provided
     */

    public void
	saveParamValue (String param_name, String param_value)
    {
		param_table.put(param_name, param_value);
	
		// also establish the new value in the XML representation
		org.jdom.Element param_node = new org.jdom.Element(org.opensims.xml.Node.PARAM_NODE);
		param_node.setAttribute("name", param_name);
		param_node.setAttribute("value", param_value);
		getDocument().getRootElement().addContent(param_node);
    }

	/**
	 * save the named parameter value (for DATE values) based on the HTTP request.
	 */
	public void
	saveDateValue( String param_name )
	{
		String new_value = (String) getParameter(param_name);
		org.jdom.Element date_node = lookupDate(param_name);
		if ( new_value != null ) {
			try {
				java.text.SimpleDateFormat date_format = new java.text.SimpleDateFormat("yyyy-MM-dd");
				java.util.Date date = date_format.parse(new_value);
				if(  log_.isDebugEnabled() ) {
					log_.debug( param_name + " got date =" + date.toString() + " ticks=" + date.getTime() );
				}
				// reformat to "cannonical" form when  re-displayed in the nav form
				new_value = date_format.format( date );
				param_table.put(param_name,new_value);
			} catch (java.text.ParseException e) {
				// the param string was not a valid date
				new_value = null;
			}
		}
		if ( date_node != null) {
			if ( new_value != null ) {
				param_table.put(param_name,new_value);
				date_node.setAttribute("value",new_value);
			} else {
				date_node.setAttribute("value","");
			}
		}
	}

    /**
     * Get the String value for a paramter directly from the HTTP
     * request. 
     */

    public String
	getRawParam (String param_name)
    {
    	log_.debug( "getRawParam( '" + param_name + "' )" );
		String param_value = (String) getParameter(param_name);
	
		if (log_.isDebugEnabled()) {
			log_.debug("get raw param: |" + param_name + "| = |" + param_value + "|");
		}
	
		return param_value;
    }


	/**
	 * determine if this request is for an explicit date range selection or not
	 * "not" meaning last hour, last day, ...  floating time range
	 */
	 public boolean
	 useDateRange()
	 {
	 	return getParamValue("time_limit").equals("custom_date");
	 }
	 
	 /**
	  * determine if the report specified by date range represents a complete report
	  * i.e. it does not span today.
	  * note: thihs function needs to also guarantee that report date range is valid
	  */
	 public boolean
	 isDateRangeComplete()
	 {
		long today = (new java.util.Date()).getTime();
		long from = getFromDate().getTime();
		long to = getToDate().getTime();
 		if ( ( from <= today ) && ( from < today ) && ( to < today ) ) {
	 		return true;
	 	}
	 	return false;
	 }

	/**
	 * determine if the report specified by date range represents a valid date range
	 * i.e. from is before or equal to to to, range is not in future
	 */
	public boolean
	isDateRangeValid()
	{
		boolean is_valid = false;
		try {
			long today = (new java.util.Date()).getTime();
			long tomorrow = today + ( 24 * 60 * 60 * 1000 ) ;
			long from = getFromDate().getTime();
			long to = getToDate().getTime();
	 		if ( ( from <= to ) && ( from <= today ) && ( to < tomorrow ) ) {
		 		is_valid = true;
		 	}
		} catch ( Exception e ) {
			// any exception here implies invalid date range
		}
		return is_valid;
	}

	
	public java.util.Date
	getFromDate()
	{
		return getDate( "from_date" );
	}
	
	public java.util.Date
	getToDate()
	{
		java.util.Date date = getDate( "to_date" );
		if ( date == null ) {
			java.util.Date now = new java.util.Date();
			// need to reformat now into date to strip out miliseconds (they can't be set manually evidently)
			date = new java.util.Date( now.getYear(), now.getMonth(), now.getDate() );
		}
		date.setHours( 23 );
		date.setMinutes( 59 );
		date.setSeconds( 59 );
		// need to reformat to set miliseconds (they can't be set manually evidently)
		java.util.Date to = new java.util.Date( date.getTime() + 999 );
		if( log_.isDebugEnabled() ) {
			log_.debug( " to_date = " + to.getTime() );
		}
		return to;
	}

	/**
	 * return the signature string for the provided date
	 */
	public String
	formatDateSignature( java.util.Date date )
	{
		java.text.SimpleDateFormat signature_format = new java.text.SimpleDateFormat("yyyy-MM-dd");
		return signature_format.format( date );
	}
	
	public String
	getFromDateSignature()
	{
		return formatDateSignature( getFromDate() );
	}
	
	public String
	getToDateSignature()
	{
		return formatDateSignature( getToDate() );
	}
	
	/**
	 * calculate start and end 'tick' sql to be used against the alert table
	 */
	public String
	getAlertIntervalSql( String table )
	{
		StringBuffer sql = new StringBuffer();
		if ( useDateRange() ) {
			sql.append( " (" );
			sql.append( table + ".tick >= " + getFromDate().getTime() );
			sql.append( " and " );
			sql.append( table + ".tick <= " + getToDate().getTime() );
			sql.append( ") " );
		} else {
			// traditional time_limit selection
			Long earliestTick = new Long( getCorrelator().getTick() - getTimeLimitInTicks() );
			sql.append( table + ".tick >= " + earliestTick.toString() );
		}
		return sql.toString();
	}


	/**
	 * calculate time interval of alert tick selection
	 */
	public long
	getAlertIntervalAsSecs()
	{
		long time_interval_sec = 0;
		if ( useDateRange() ) {
			time_interval_sec = getToDate().getTime() + 1 - getFromDate().getTime();
		} else {
			time_interval_sec = getTimeLimitInTicks() / 1000 ;
		}
		return time_interval_sec;
	}
	

	/**
	 * determine if the report requires digging into the alert_archive table
	 */
	public boolean
	requireAlertArchive()
	{
		long age_limit = Long.valueOf(getCorrelator().getConfig("data.age_limit")).longValue();
		if ( useDateRange() ) {
			return ( getFromDate().getTime() <= ( getCorrelator().getTick() - age_limit ) );
		} else {
			return ( getTimeLimitInTicks() <= age_limit );
		}
	}

	
    /**
     * Translate the time_limit parameter selected by the user into ticks,
     */
    
    public long
    getTimeLimitInTicks()
    {
		long hours = 0;
    	try {
	    	hours = Long.parseLong(getParamValue("time_limit"));
	    } catch ( java.lang.NumberFormatException e ) {
	    	// in case this gets called when time_limit is set to date_range
	    }
		long ticks = 1000 * 60 * 60 * hours ;
    	return ticks;
    }
    

    /**
     * Add the name/value pair to the document, to propagate it
     * through the next HTTP request.
     */

    public void
	propagateParamValue (String param_name, String param_value)
    {
    	if ( param_value == null ) return;
    	
		org.jdom.Element param_node = new org.jdom.Element(org.opensims.xml.Node.HIDDEN_NODE);
		param_node.setAttribute("name", param_name);
		param_node.setAttribute("value", param_value);
		getDocument().getRootElement().addContent(param_node);
	
		// keep a copy in the table	
		param_table.put(param_name, param_value);
    }

	/**
	 * covenience funtion for propagating a value from raw request param into http request
	 */
	public String
	propagateParamValue( String param_name )
	{
		String param_value = getRawParam( param_name );
		propagateParamValue( param_name, param_value );
		return param_value;
	}

    /**
     * Check whether an option exists which matches the given test
     * value for the named parameter.
     */

    public org.jdom.Element
	lookupOption (String param_name, String test_value)
    {
		org.jdom.Element option_node = null;

        try {
            StringBuffer path = new StringBuffer();

            path.append("//");
            path.append(org.opensims.xml.Node.MENU_NODE);
            path.append("[@name = '");
            path.append(param_name);
            path.append("']/");
            path.append(org.opensims.xml.Node.OPTION_NODE);

			if (test_value != null) {
				// check the given test value		
				path.append("[@value = '");
				path.append(test_value);
				path.append("']");
			}
			else {
				// get the default value		
				path.append("[@default = 'true']");
			}

            org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
            option_node = (org.jdom.Element) xpath.selectSingleNode(getDocument());

			if (log_.isDebugEnabled()) {
				log_.debug("lookup option: path " + path.toString() + " option " + option_node);
			}
        }
        catch (Exception e) {
            log_.error("option lookup: " + param_name, e);
        }

		return option_node;
    }
    
    public org.jdom.Element
	lookupDate (String param_name)
    {
		org.jdom.Element date_node = null;

        try {
            StringBuffer path = new StringBuffer();

            path.append("//");
            path.append(org.opensims.xml.Node.DATE_NODE);
            path.append("[@name = '");
            path.append(param_name);
            path.append("']");

            org.jdom.xpath.XPath xpath = org.jdom.xpath.XPath.newInstance(path.toString());
            date_node = (org.jdom.Element) xpath.selectSingleNode(getDocument());

			if (log_.isDebugEnabled()) {
				log_.debug("lookup date: path " + path.toString() + " date " + date_node);
			}
        }
        catch (Exception e) {
            log_.error("ookup date: " + param_name, e);
        }

		return date_node;
    }
    
    /**
     * Get the named parameter represented as SQL.
     */

    public String
	lookupParamSql (String param_name)
    {
		String param_sql = " ";
	
		org.jdom.Element option_node = lookupOption(param_name, getParamValue(param_name));
	
		if (option_node != null) {
			param_sql = option_node.getAttributeValue("sql") + " ";
		}
	
		return param_sql;
    }


    //////////////////////////////////////////////////////////////////////
    // reporting methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the enabled flag.
     */

    public boolean
	getEnabled ()
    {
		return enabled;
    }


    /**
     * Set the enabled flag.
     */

    public void
	setEnabled (boolean enabled)
    {
		this.enabled = enabled;
    }


    /**
     * Get the username supplied with the request
     */

    public String
	getUsername ()
    {
		return username;
    }
    
    
    /**
     * Get the username supplied with the request
     */

    public String
	getOrganization ()
    {
		return organization;
    }
    
    /**
     * Is this user in the admin role?
     */

    public boolean
	isInRoleAdmin ()
    {
		return isAdmin;	
	}
	
	

    /**
     * Get a copy of the document represented as XML.
     */

    public org.jdom.Element
	getReportNode ()
    {
		org.jdom.Element report_node = null;
	
		if (getEnabled()) {
			report_node = (org.jdom.Element) getDocument().getRootElement().clone();
		}
		else {
			report_node = new org.jdom.Element(org.opensims.xml.Node.NAVIGATE_NODE);
		}
	
		return report_node;
    }
}
