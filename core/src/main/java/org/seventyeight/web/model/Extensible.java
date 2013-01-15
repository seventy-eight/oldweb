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
     * Get the {@link Node} containing all the {@link Item}s extensions defined for this object. <br />
     * If there's no {@link Node} defined, a new is created.
     * @return
     */
    public Node getExtensionsNode();
}
