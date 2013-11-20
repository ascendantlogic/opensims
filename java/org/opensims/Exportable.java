/**
 * @LICENSE@
 */

package org.opensims;

/**
 * $CVSId: Exportable.java,v 1.2 2004/07/01 05:38:42 paco Exp $
 * $Id: Exportable.java 1 2008-01-10 18:37:05Z smoot $
 * @version $Revision: 1.2 $
 * @author Paco NATHAN <paco@symbiot.com>
 */

public interface
    Exportable
{
    /**
     * Export as an XML node
     */

    public org.jdom.Element
        getExportNode ();
}
