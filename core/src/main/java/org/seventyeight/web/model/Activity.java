package org.seventyeight.web.model;

import org.seventyeight.database.Database;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.utils.Date;

/**
 * @author cwolfgang
 *         Date: 16-12-12
 *         Time: 23:01
 */
public class Activity implements DatabaseItem, Timestamped {

    public static final String KEY_TEXT = "text";

    protected Node node;

    public Activity( Node node ) {
        this.node = node;
    }

    public void setText( String text ) {
        node.set( KEY_TEXT, text );
    }

    public String getText() {
        return node.get( KEY_TEXT );
    }

    @Override
    public long getTimestamp() {
        return node.get( KEY_TIMESTAMP );
    }

    @Override
    public Date getTimestampAsDate() {
        return new Date( (Long) node.get( KEY_TIMESTAMP ) );
    }

    @Override
    public void setTimestamp( long timestamp ) {
        node.set( KEY_TIMESTAMP, timestamp );
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
