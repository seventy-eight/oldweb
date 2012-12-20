package org.seventyeight.database.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.seventyeight.database.*;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBEnv;
import org.seventyeight.database.orientdb.impl.orientdb.OrientEdge;
import org.seventyeight.database.orientdb.impl.orientdb.OrientNode;
import org.seventyeight.utils.RandomCollections;
import org.seventyeight.utils.StopWatch;
import sun.misc.Sort;

import java.util.*;

/**
 * @author cwolfgang
 *         Date: 27-11-12
 *         Time: 13:36
 */
public class IndexTest {

    public enum TestEdgeType implements EdgeType {
        REPLY
    }

    public static final int NUMBER = 10000;
    public static final String INDEX = "replies";

    public static void main( String[] args ) {
        OrientDBEnv env = new OrientDBEnv();

        OrientNode node = env.createNode( "debate", 1 );

        env.getDB().createIndex( INDEX, IndexType.UNIQUE, IndexValueType.LONG, IndexValueType.LONG );

        StopWatch sw = new StopWatch();

        sw.reset();

        sw.start( "Create list" );
        List<Integer> list = new ArrayList<Integer>( NUMBER );
        for( int i = 0 ; i < NUMBER ; i++ ) {
            list.add( i );
        }
        sw.stop();

        sw.start( "Randomizing" );
        //RandomCollections.randomList( list );
        sw.stop();

        sw.start( "Adding replies" );
        for( int i : list ) {
            OrientNode n = env.createNode( "id", i );
            node.createEdge( n, TestEdgeType.REPLY ).set( "order", i ).save();
            //System.out.println( "[" + i + "] " + n );
            env.getDB().putToIndex( INDEX, n, 1, i );
        }
        sw.stop();



        sw.start( "Pagination #1" );
        List<OrientNode> nodes = env.getDB().getFromIndexAbove( INDEX, 50, 1, 100 );
        Collections.sort( nodes, new Sorter() );
        System.out.println( nodes );
        sw.stop();

        sw.start( "Pagination #2" );
        List<OrientNode> nodes2 = env.getDB().getFromIndexAbove( INDEX, 50, 1, 100 );
        Collections.sort( nodes2, new Sorter() );
        System.out.println( nodes2 );
        sw.stop();

        System.out.println( sw.print( 10000 ) );



    }

    public static class Sorter implements Comparator<OrientNode> {

        @Override
        public int compare( OrientNode o1, OrientNode o2 ) {
            int id1 = o1.get( "id" );
            int id2 = o2.get( "id" );

            if( id1 > id2 ) {
                return 1;
            } else if( id1 < id2 ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
