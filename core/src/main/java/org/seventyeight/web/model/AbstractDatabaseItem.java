package org.seventyeight.web.model;

import org.seventyeight.database.Database;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

/**
 * @author cwolfgang
 *         Date: 28-11-12
 *         Time: 13:37
 */
public abstract class AbstractDatabaseItem implements DatabaseItem {

    protected Node node;

    public AbstractDatabaseItem( Node node ) {
        this.node = node;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public Edge createRelation( DatabaseItem other, EdgeType type ) {
        return node.createEdge( other.getNode(), type );
    }

    @Override
    public String getItemClass() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Database getDB() {
        return node.getDB();
    }
}
