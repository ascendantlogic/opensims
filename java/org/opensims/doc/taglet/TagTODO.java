/**
 * @LICENSE@
 */

package org.opensims.doc.taglet;


/**
 * A Taglet representing @TODO - based on the example from Sum by Jamie Ho
 */

public class
    TagTODO
    implements com.sun.tools.doclets.Taglet
{
    // local definitions
    
    protected static final String NAME = "TODO";
    protected static final String HEADER = "To-Do:";
    

    //////////////////////////////////////////////////////////////////////
    // how it gets called...
    //////////////////////////////////////////////////////////////////////

    public static void
	register (java.util.Map tagletMap)
    {
       org.opensims.doc.taglet.TagTODO tag = new org.opensims.doc.taglet.TagTODO();
       com.sun.tools.doclets.Taglet t = (com.sun.tools.doclets.Taglet) tagletMap.get(tag.getName());

       if (t != null) {
           tagletMap.remove(tag.getName());
       }

       tagletMap.put(tag.getName(), tag);
    }


    public String
	toString (com.sun.javadoc.Tag tag)
    {
	StringBuffer buf = new StringBuffer();

	buf.append("\n<DT><B>");
	buf.append(HEADER);
	buf.append("</B><DD>");
	buf.append("<TABLE cellpadding=2 cellspacing=0><TR><TD bgcolor=\"red\">");

	buf.append(tag.text());

	buf.append("</TD></TR></TABLE></DD>\n");

	return buf.toString();
    }


    public String
	toString (com.sun.javadoc.Tag[] tags)
    {
	String result = null;

        if (tags.length > 0) {
	    StringBuffer buf = new StringBuffer();

	    buf.append("\n<DT><B>");
	    buf.append(HEADER);
	    buf.append("</B><DD>");
	    buf.append("<TABLE cellpadding=2 cellspacing=0><TR><TD bgcolor=\"red\">");

	    for (int i = 0; i < tags.length; i++) {
		if (i > 0) {
		    buf.append(", ");
		}

		buf.append(tags[i].text());
	    }

	    buf.append("</TD></TR></TABLE></DD>\n");
	    result = buf.toString();
	}

	return result;
    }


    //////////////////////////////////////////////////////////////////////
    // access methods
    //////////////////////////////////////////////////////////////////////

    public String
	getName ()
    {
        return NAME;
    }


    //////////////////////////////////////////////////////////////////////
    // misc. interface stubs
    //////////////////////////////////////////////////////////////////////

    public boolean
	isInlineTag ()
    {
        return false;
    }


    public boolean
	inField ()
    {
        return true;
    }


    public boolean
	inConstructor ()
    {
        return true;
    }


    public boolean
	inMethod ()
    {
        return true;
    }


    public boolean
	inOverview ()
    {
        return true;
    }


    public boolean
	inPackage ()
    {
        return true;
    }


    public boolean
	inType ()
    {
        return true;
    }
}

