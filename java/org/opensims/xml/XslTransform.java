/**
 * @LICENSE@
 */

package org.opensims.xml;

/**
 * $CVSId: XslTransform.java,v 1.7 2005/11/15 21:35:29 mikee Exp $
 * $Id: XslTransform.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.7 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public class
    XslTransform
    extends Object
{
    // protected fields

    protected javax.xml.transform.Transformer transformer = null;

    // quality assurance

    private final static org.apache.log4j.Logger log_ = org.apache.log4j.Logger.getLogger(org.opensims.xml.XslTransform.class.getName());


    /**
     * Create a Transformer that will work with the specified
     * stylesheet file, then process the stylesheet into a compiled
     * Templates object.
     */

    public
	XslTransform (java.io.File xsl_file)
    {
	javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
	
	try {
	    setTransformer(tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xsl_file)));
	}
	catch (javax.xml.transform.TransformerConfigurationException e) {
	    log_.error("cannot instantiate factory", e);
	}
    }


    /**
     * Get the Transformer.
     */

    public javax.xml.transform.Transformer
	getTransformer ()
    {
	return transformer;
    }


    /**
     * Set the Transformer.
     */

    public void
	setTransformer (javax.xml.transform.Transformer transformer)
    {
	this.transformer = transformer;
    }


    /**
     * Command line test interface: expects an XML document and its
     * XSL stylesheet as the command line arguments
     */

    public static void
	main (String[] args)
    {  
	if (args.length < 2) {
	    System.err.println("usage: XslTransform <xml_file> <xsl_file>");
	    System.exit(1);
	}

	try {
	    java.io.File xml_file = new java.io.File(args[0]);
	    java.io.File xsl_file = new java.io.File(args[1]);

	    // create an XSLT transformer, optionally setting
	    // parameters...

	    XslTransform xslt = new XslTransform(xsl_file);

	    if (args.length > 2) {
		xslt.getTransformer().setParameter("iso_lang", args[2]);
	    }

	    // validate against the given DTD or XML Schema
	    // by turning on "SAXBuilder(true)"

	    org.jdom.input.SAXBuilder xmlBuild = new org.jdom.input.SAXBuilder(false);
	    org.jdom.Document document = xmlBuild.build(xml_file);

	    // apply the XSL stylesheet as a transform

	    org.jdom.transform.JDOMSource source = new org.jdom.transform.JDOMSource(document);

	    javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(System.out);

	    xslt.getTransformer().transform(source, result);
	}
	catch (Exception e) {
	    System.err.println("XslTransform: " + e.getMessage());
	    e.printStackTrace();
	}
    }
}
