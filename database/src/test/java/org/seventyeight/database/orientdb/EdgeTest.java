package org.seventyeight.database.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBEnv;
import org.seventyeight.database.orientdb.impl.orientdb.OrientEdge;
import org.seventyeight.database.orientdb.impl.orientdb.OrientNode;
import org.seventyeight.utils.StopWatch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author cwolfgang
 *         Date: 27-11-12
 *         Time: 13:36
 */
public class EdgeTest {

    public enum TestEdgeType implements EdgeType {
        FRIEND,
        ENEMY,
        NEUTRAL
    }

    public static void main( String[] args ) {
        OrientDBEnv env = new OrientDBEnv();

        OrientNode node = env.createNode( "Person", "a" );

        StopWatch sw = new StopWatch();

        sw.reset();

        sw.start( "Friends" );
        for( int i = 0 ; i < 1000 ; i++ ) {
            OrientNode n = env.createNode( "age", i % 10 + 20 );
            node.createEdge( n, TestEdgeType.FRIEND ).set( "age", i % 10 + 20 ).save();
        }
        sw.stop();

        sw.start( "Enemies" );
        for( int i = 0 ; i < 10000 ; i++ ) {
            OrientNode n = env.createNode( "age", "20" );
            node.createEdge( n, TestEdgeType.ENEMY );
        }
        sw.stop();

        sw.start( "Neutral" );
        for( int i = 0 ; i < 10000 ; i++ ) {
            OrientNode n = env.createNode( "age", "21" );
            node.createEdge( n, TestEdgeType.NEUTRAL );
        }
        sw.stop();

        /*
        sw.start( "Getting friends 2" );
        Set<OIdentifiable> ois = node.getDB().getInternalDatabase().getOutEdges( node.getDocument(), TestEdgeType.FRIEND.toString() );
        sw.stop();
        */

        /*
        sw.start( "Getting enemies" );
        List<OrientEdge> edges2 = node.getEdges( TestEdgeType.ENEMY, Direction.OUTBOUND );
        sw.stop();
        */

        sw.start( "Getting ALL" );
        Set<OIdentifiable> ois2 = node.getDB().getInternalDatabase().getOutEdges( node.getDocument() );
        sw.stop();

        System.out.println( ois2.size() );
        sw.start( "Looping ALL" );
        //List<ODocument> list = new LinkedList<ODocument>();
        List<ODocument> list = new ArrayList<ODocument>();
        for( OIdentifiable oi : ois2 ) {
            ODocument doc = ((ODocument)oi.getRecord());
            if( doc.field( OGraphDatabase.LABEL ).equals( TestEdgeType.FRIEND.toString() ) ) { //  && ((Integer)doc.field( "age" )).equals( 20 )
                list.add( doc );
            }
        }
        sw.stop();
        System.out.println( "SIZE: " + list.size() );

        sw.start( "Getting friends" );
        List<OrientEdge> edges = node.getEdges( TestEdgeType.FRIEND, Direction.OUTBOUND );
        sw.stop();

        System.out.println( sw.print( 1000 ) );



    }
}
