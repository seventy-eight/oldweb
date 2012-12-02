package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;

/**
 * User: cwolfgang
 * Date: 18-11-12
 * Time: 22:37
 */
public class OrientEdge implements Edge {

    private static Logger logger = Logger.getLogger( OrientEdge.class );

    private OIdentifiable edge;
    private OrientNode source;
    private OrientNode target;

    public OrientEdge( OrientNode source, OrientNode target, EdgeType type ) {
        edge = source.getDB().getInternalDatabase().createEdge( source.getDocument(), target.getDocument() ).field( OGraphDatabase.LABEL, type.toString() ).save();
        this.source = source;
        this.target = target;
    }

    public OrientEdge( OIdentifiable edge, OrientNode source, OrientNode target ) {
        this.edge = edge;
        this.source = source;
        this.target = target;
    }

    @Override
    public OrientNode getSourceNode() {
        return source;
    }

    @Override
    public OrientNode getTargetNode() {
        return target;
    }

    @Override
    public void delete() {
        logger.debug( "Removing the edge " + this );
        ((OrientNode) source ).getDB().getInternalDatabase().removeEdge( edge );
    }

    @Override
    public <T> T get( String key ) {
        return ((ODocument)edge.getRecord()).field( key );
    }

    @Override
    public <T> T get( String key, T defaultValue ) {
        if( ((ODocument)edge.getRecord()).containsField( key ) ) {
            return ((ODocument)edge.getRecord()).field( key );
        } else {
            return defaultValue;
        }
    }

    @Override
    public <T> OrientEdge set( String key, T value ) {
        ((ODocument)edge.getRecord()).field( key, value );

        return this;
    }

    @Override
    public OrientEdge save() {
        edge.getRecord().save();

        return this;
    }

    @Override
    public boolean equals( Object obj ) {
        if( obj == this ) {
            return true;
        }

        if( obj instanceof OrientEdge ) {
            return ((OrientEdge)obj).edge.equals( edge );
        }

        return false;
    }

    @Override
    public String toString() {
        return "OrientEdge[" + edge + "]";
    }
}
