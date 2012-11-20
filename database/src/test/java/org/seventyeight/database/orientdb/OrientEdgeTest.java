package org.seventyeight.database.orientdb;

import org.junit.ClassRule;
import org.junit.Test;
import org.seventyeight.database.Direction;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.orientdb.impl.orientdb.OrientEdge;
import org.seventyeight.database.orientdb.impl.orientdb.OrientNode;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * User: cwolfgang
 * Date: 19-11-12
 * Time: 23:09
 */
public class OrientEdgeTest {

    public enum TestType implements EdgeType {
        TEST,
        TEST2
    }

    @ClassRule
    public static OrientDBRule orule = new OrientDBRule();

    @Test
    public void test1() {
        OrientNode n1 = new OrientNode( orule.getDB() );
        n1.set( "field", "a" );
        n1.save();

        OrientNode n2 = new OrientNode( orule.getDB() );
        n2.set( "field", "b" );
        n2.save();

        OrientEdge e = new OrientEdge( n1, n2, TestType.TEST );

        List<OrientEdge> edges = n1.getEdges( n2, TestType.TEST, Direction.INBOUND );
        assertThat( edges.size(), is( 0 ) );

        edges = n1.getEdges( n2, TestType.TEST, Direction.OUTBOUND );
        assertThat( edges.size(), is( 1 ) );

        edges = n1.getEdges( n2, TestType.TEST, Direction.BOTH );
        assertThat( edges.size(), is( 1 ) );
    }

    @Test
    public void test2() {
        OrientNode n1 = new OrientNode( orule.getDB() );
        n1.set( "field", "a" );
        n1.save();

        OrientNode n2 = new OrientNode( orule.getDB() );
        n2.set( "field", "b" );
        n2.save();

        OrientNode n3 = new OrientNode( orule.getDB() );
        n3.set( "field", "c" );
        n3.save();

        OrientNode n4 = new OrientNode( orule.getDB() );
        n4.set( "field", "d" );
        n4.save();

        OrientEdge e = new OrientEdge( n1, n2, TestType.TEST );
        OrientEdge e1 = new OrientEdge( n1, n3, TestType.TEST );
        OrientEdge e2 = new OrientEdge( n1, n4, TestType.TEST2 );

        List<OrientEdge> edges = n1.getEdges( TestType.TEST2 );
        assertThat( edges.size(), is( 1 ) );
        assertThat( edges.get( 0 ), is( e2 ) );
        assertThat( e2.getTargetNode(), is( n4 ) );
    }
}
