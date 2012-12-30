package org.seventyeight.web.model;

import org.seventyeight.database.Database;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 14:53
 */
public interface DatabaseItem {
    public Node getNode();
    public Edge createRelation( DatabaseItem other, EdgeType type );
    public String getItemClass();
    public Database getDB();

    /**
     * Remove this {@link Item} from the persistence layer
     */
    public void remove();
}
