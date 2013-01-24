package org.seventyeight.web.model;

import org.seventyeight.database.Database;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.IllegalStateRuntimeException;

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
        Edge edge = node.createEdge( other.getNode(), type ).save();
        node.save();
        other.getNode().save();
        return edge;
    }

    @Override
    public String getItemClass() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Database getDB() {
        return node.getDB();
    }

    @Override
    public void remove() {
    }

    /**
     * Get a field
     * @param key
     * @param def
     * @return
     */
    public <T> T getField( String key, T def ) {
        if( node.get( key ) == null ) {
            return def;
        } else {
            return (T) node.get( key );
        }
    }

    public <T> T getField( String key ) throws IllegalStateRuntimeException {
        if( node.get( key ) == null ) {
            throw new IllegalStateRuntimeException( "Field " + key + " does not exist" );
        } else {
            return (T) node.get( key );
        }
    }


    @Override
    public boolean equals( Object obj ) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }

        AbstractDatabaseItem item = (AbstractDatabaseItem) obj;
        return item.getNode().getId( false ).equals( this.getNode().getId( false ) );

    }
}
