package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;
import org.seventyeight.database.Edge;
import org.seventyeight.database.Edge2;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * User: cwolfgang
 * Date: 18-11-12
 * Time: 22:28
 */
public class OrientNode implements Node {

    private static Logger logger = Logger.getLogger( OrientNode.class );

    private OGraphDatabase db;
    private ODocument doc;

    public OrientNode( OGraphDatabase db ) {
        this.db = db;
        this.doc = db.createVertex();
    }

    public OrientNode( OGraphDatabase db, ODocument doc ) {
        this.db = db;
        this.doc = doc;
    }

    public ODocument getDocument() {
        return doc;
    }

    public OGraphDatabase getDB() {
        return db;
    }

    @Override
    public Edge createEdge( Node to, EdgeType type ) {
        OrientNode n = (OrientNode) to;
        logger.debug( "Creating edge(" + type + ") from " + doc.getClassName() + " to " + n.getDocument().getClassName() );

        ODocument edge = db.createEdge( doc , n.doc, type.toString() ).field( OGraphDatabase.LABEL, type.toString() ).save();

        return new OrientEdge( edge, this, n );
    }

    @Override
    public List<Edge> getEdges( EdgeType type ) {
        logger.debug( "Getting edges from " + this + " of type " + type );

        Set<OIdentifiable> edges = db.getOutEdges( doc, ( type != null ? type.toString() : null ) );
        logger.debug( "EDGES: " + edges );

        List<Edge> es = new LinkedList<Edge>();

        for( OIdentifiable e : edges ) {
            //ODocument out = db.getInVertex( e );
            ODocument other = db.getOutVertex( e );

            Edge edge = new OrientEdge( e, this, new OrientNode( db, other ) );
            es.add( edge );
            logger.debug( "Edge2: " + edge );
        }

        return es;
    }

    @Override
    public List<Edge> getEdges( Node other, EdgeType type ) {
        logger.debug( "Getting edges between " + this + " and " + other + " of type " + type );

        OrientNode n = (OrientNode) other;

        Set<OIdentifiable> ois = db.getEdgesBetweenVertexes( doc, n.doc, ( type != null ? new String[] { type.toString() } : null ) );

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
    public List<Edge> getEdgesTo( Node to, EdgeType type ) {
        logger.debug( "Getting edges from " + this + " to " + to + " of type " + type );

        OrientNode n = (OrientNode) to;

        Set<OIdentifiable> ois = db.getEdgesBetweenVertexes( doc, n.doc, ( type != null ? new String[] { type.toString() } : null ) );

        logger.debug( "EDGES: " + ois );
        List<Edge> es = new LinkedList<Edge>();

        for( OIdentifiable e : ois ) {
            if( db.getOutVertex( e ).equals( doc )) {
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
    public <T> void set( String key, T value ) {
        doc.field( key, value );
    }

    @Override
    public void save() {
        doc.save();
    }

    @Override
    public String toString() {
        return "OrientNode[" + doc + "]";
    }
}
