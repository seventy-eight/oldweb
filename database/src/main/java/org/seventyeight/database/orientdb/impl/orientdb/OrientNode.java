package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;
import org.seventyeight.database.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * User: cwolfgang
 * Date: 18-11-12
 * Time: 22:28
 */
public class OrientNode implements Node<OGraphDatabase, OrientDatabase, OrientNode> {

    private static Logger logger = Logger.getLogger( OrientNode.class );

    private OrientDatabase db;
    private ODocument doc;

    public OrientNode( OrientDatabase db ) {
        this.db = db;
        this.doc = db.getInternalDatabase().createVertex();
    }

    public OrientNode( OrientDatabase db, ODocument doc ) {
        this.db = db;
        this.doc = doc;
    }

    public ODocument getDocument() {
        return doc;
    }

    @Override
    public OrientDatabase getDB() {
        return db;
    }

    @Override
    public Edge createEdge( OrientNode to, EdgeType type ) {
        OrientNode n = (OrientNode) to;
        logger.debug( "Creating edge(" + type + ") from " + doc.getClassName() + " to " + n.getDocument().getClassName() );

        ODocument edge = db.getInternalDatabase().createEdge( doc, n.doc, type.toString() ).field( OGraphDatabase.LABEL, type.toString() ).save();

        return new OrientEdge( edge, this, n );
    }

    @Override
    public List<Edge> getEdges( EdgeType type ) {
        logger.debug( "Getting edges from " + this + " of type " + type );

        Set<OIdentifiable> edges = db.getInternalDatabase().getOutEdges( doc, ( type != null ? type.toString() : null ) );
        logger.debug( "EDGES: " + edges );

        List<Edge> es = new LinkedList<Edge>();

        for( OIdentifiable e : edges ) {
            //ODocument out = db.getInVertex( e );
            ODocument other = db.getInternalDatabase().getOutVertex( e );

            Edge edge = new OrientEdge( e, this, new OrientNode( db, other ) );
            es.add( edge );
            logger.debug( "Edge2: " + edge );
        }

        return es;
    }

    @Override
    public List<Edge> getEdges( OrientNode other, EdgeType type ) {
        logger.debug( "Getting edges between " + this + " and " + other + " of type " + type );

        OrientNode n = (OrientNode) other;

        Set<OIdentifiable> ois = db.getInternalDatabase().getEdgesBetweenVertexes( doc, n.doc, ( type != null ? new String[]{ type.toString() } : null ) );

        logger.debug( "EDGES: " + ois );
        List<Edge> es = new LinkedList<Edge>();

        for( OIdentifiable e : ois ) {

            Edge edge = new OrientEdge( e, this, other );
            es.add( edge );
            logger.debug( "Edge2: " + edge );
        }

        return es;
    }

    @Override
    public List<Edge> getEdgesTo( OrientNode to, EdgeType type ) {
        logger.debug( "Getting edges from " + this + " to " + to + " of type " + type );

        OrientNode n = (OrientNode) to;

        Set<OIdentifiable> ois = db.getInternalDatabase().getEdgesBetweenVertexes( doc, n.doc, ( type != null ? new String[]{ type.toString() } : null ) );

        logger.debug( "EDGES: " + ois );
        List<Edge> es = new LinkedList<Edge>();

        for( OIdentifiable e : ois ) {
            if( db.getInternalDatabase().getOutVertex( e ).equals( doc )) {
                Edge edge = new OrientEdge( e, this, to );
                es.add( edge );
                logger.debug( "Edge2: " + edge );
            }
        }

        return es;
    }

    @Override
    public <T> T get( String key ) {
        return doc.field( key );
    }

    @Override
    public <T> OrientNode set( String key, T value ) {
        doc.field( key, value );

        return this;
    }

    @Override
    public OrientNode save() {
        doc.save();

        return this;
    }

    @Override
    public OrientNode removeEdges( EdgeType type, Direction direction ) {
        db.getInternalDatabase().removeEdge(null);

        return this;
    }

    @Override
    public String toString() {
        return "OrientNode[" + doc + "]";
    }
}
