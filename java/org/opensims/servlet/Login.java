/**
 * @LICENSE@
 */

package org.opensims.servlet;

/**
 * $CVSId: Login.java,v 1.4 2005/11/25 21:13:47 mikee Exp $
 * $Id: Login.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.4 $
 * @author Paco NATHAN <paco@symbiot.com>
 * @author Mike W. Erwin <mikee@symbiot.com>
 */

public class
    Login
{
    // protected fields

    protected org.jdom.Element login_node = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.servlet.Login.class.getName());


    /**
     * Initialization.
     */

    public void
	init (org.jdom.Element login_node)
    {
		setLoginNode(login_node);
    }


    //////////////////////////////////////////////////////////////////////
    // misc. access methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the login node.
     */

    public org.jdom.Element
	getLoginNode ()
    {
		return login_node;
    }


    /**
     * Set the login node.
     */

    public void
	setLoginNode (org.jdom.Element login_node)
    {
		this.login_node = login_node;
    }


    /**
     * Get the named param node.
     */

    protected org.jdom.Element
	getParamNode (String param_name)
    {
		org.jdom.Element param_node = null;
	
		try {
			StringBuffer path = new StringBuffer();
	
			path.append(org.opensims.xml.Node.PARAM_NODE);
			path.append("[@name = '");
			path.append(param_name);
			path.append("']");
	
			param_node = (org.jdom.Element) org.jdom.xpath.XPath.selectSingleNode(getLoginNode(), path.toString());
	
			if (log_.isDebugEnabled()) {
			log_.debug("path " + path.toString() + " = " + param_node);
	
			if (param_node != null) {
				log_.debug(org.opensims.xml.XmlBuilder.formatXML(param_node, true, true));
			}
			}
		}
		catch (Exception e) {
			log_.error("get param node |" + param_name + "|", e);
		}
	
		return param_node;
    }


    /**
     * Get the named param value.
     */

    public String
	getParamValue (String param_name)
    {
		String param_value = null;
		org.jdom.Element param_node = getParamNode(param_name);
	
		if (param_node != null) {
			param_value = param_node.getAttributeValue("value");
		}
	
		return param_value;
    }


    /**
     * Set the named param value.
     */

    public void
	setParamValue (String param_name, String param_value)
    {
		org.jdom.Element param_node = getParamNode(param_name);
	
		if (param_node == null) {
			param_node = new org.jdom.Element(org.opensims.xml.Node.PARAM_NODE);
			param_node.setAttribute("name", param_name);
	
			getLoginNode().addContent(param_node);
		}
	
		param_node.setAttribute("value", param_value);
	
		if (log_.isDebugEnabled()) {
			log_.debug(org.opensims.xml.XmlBuilder.formatXML(getLoginNode(), true, true));
		}
    }
    
    
    /**
     * Get the username
     */

    public String
	getLoginName ()
    {
	    String username = null;

		if (login_node != null) {
			username = login_node.getAttributeValue("username");
		}
		return username;
    }
    
    
    
    /**
     * Get the groupname
     */

    public String
	getGroupName ()
    {
	    String groupname = null;

		if (login_node != null) {
			groupname = login_node.getAttributeValue("group");
		}
		return groupname;
    }
    
}




