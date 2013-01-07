package org.seventyeight.web.model;

import org.seventyeight.database.Node;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 07-01-13
 *         Time: 20:56
 */
public interface Extensible {

    /**
     * Get a {@link List} of extension classes defined for this object. <br />
     * Typically, only one is defined
     * @param extensionClass
     * @return
     */
    public List<Node> getExtensionClassNodes( Class<?> extensionClass );
}
