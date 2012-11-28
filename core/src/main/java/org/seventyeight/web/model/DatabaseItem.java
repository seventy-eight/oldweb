package org.seventyeight.web.model;

import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 14:53
 */
public interface DatabaseItem<T> {
    public Node getNode();
    public T createRelation( Item other, EdgeType type );
    public String getItemClass();
}
