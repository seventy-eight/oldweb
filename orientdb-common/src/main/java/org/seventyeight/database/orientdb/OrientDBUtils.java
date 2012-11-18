package org.seventyeight.database.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 22:40
 */
public class OrientDBUtils {

    private static Logger logger = Logger.getLogger( OrientDBUtils.class );

    private OrientDBUtils() {}


    /**
     * Create an {@link EdgeType} edge between from {@link Node} and to {@link Node}
     * @param graphdb
     * @param from
     * @param to
     * @param type
     * @return
     */
    public static ODocument createEdge( OGraphDatabase graphdb, Node from, Node to, EdgeType type ) {
        logger.debug( "Creating edge(" + type + ") from " + from.getNode().getClassName() + " to " + to.getNode().getClassName() );
        return graphdb.createEdge( from.getNode(), to.getNode(), type.toString() ).field( OGraphDatabase.LABEL, type.toString() ).save();
    }


    /**
     * Get a {@link List} of {@link Edge}'s from a {@link Node} with a certain {@link EdgeType}.
     * @param db
     * @param item
     * @param type
     * @return
     */
    public static List<Edge> getEdges( OGraphDatabase db, Node item, EdgeType type ) {
        logger.debug( "Getting edges from " + item + " of type " + type );

        Set<OIdentifiable> edges = db.getOutEdges( item.getNode(), ( type != null ? type.toString() : null ) );
        logger.debug( "EDGES: " + edges );
        List<Edge> es = new LinkedList<Edge>();

        for( OIdentifiable e : edges ) {
            ODocument out = db.getInVertex( e );

            Edge edge = new Edge( (ODocument) e, item.getNode(), out );
            es.add( edge );
            logger.debug( "Edge: " + edge );
        }

        return es;
    }

    public static List<Edge> getEdges( OGraphDatabase db, Node from, Node to, EdgeType type ) {
        logger.debug( "Getting edges from " + from + " to " + to + " of type " + type );

        Set<OIdentifiable> ois = db.getEdgesBetweenVertexes( from.getNode(), to.getNode(), ( type != null ? new String[] { type.toString() } : null ) );

        logger.debug( "EDGES: " + ois );
        List<Edge> es = new LinkedList<Edge>();

        for( OIdentifiable e : ois ) {
            //ODocument out = db.getInVertex( e );

            Edge edge = new Edge( (ODocument) e, from.getNode(), to.getNode() );
            es.add( edge );
            logger.debug( "Edge: " + edge );
        }

        return es;
    }



    public static void removeEdge( OGraphDatabase db, ODocument edge ) {
        db.removeEdge( edge );
    }

    /**
     * Remove outgoing edges from a node of a certain type
     * @param graphdb
     * @param item
     * @param type
     */
    public static void removeOutEdges( OGraphDatabase graphdb, Node item, EdgeType type ) {
        logger.debug( "Removing out edges " + type + " from " + item );

        Set<OIdentifiable> edges = graphdb.getOutEdges( item.getNode(), type.toString() );
        for( OIdentifiable edge : edges ) {
            graphdb.removeEdge( (ODocument) edge );
        }
    }

    /**
     * Remove edges from first {@link Node} to the second {@link Node}
     * @param graphdb
     * @param from
     * @param to
     * @param type
     */
    public void removeEdges( OGraphDatabase graphdb, Node from, Node to, EdgeType type ) {
        logger.debug( "Removing relations " + type + " from " + from + " to " + to );

        List<Edge> edges = getEdges( graphdb, from, type );

        for( Edge edge : edges ) {
            if( edge.getOutNode().equals( to.getNode() ) ) {
                logger.debug( "Removing the edge " + edge );
                graphdb.removeEdge( edge.getEdge() );
            }
        }
    }
}
