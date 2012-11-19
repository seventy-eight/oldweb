package org.seventyeight.database.orientdb;

import org.junit.ClassRule;
import org.junit.Test;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.database.orientdb.impl.orientdb.OrientEdge;
import org.seventyeight.database.orientdb.impl.orientdb.OrientNode;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 23:08
 */
public class OrientNodeTest {

    public enum TestType implements EdgeType {
        TEST
    }

    @ClassRule
    public static OrientDBRule orule = new OrientDBRule();

    @Test
    public void test1() {
        Node n = new OrientNode( orule.getDB() );
        n.set( "field", "f" );

        NodeVerifier verifier = new NodeVerifier( n ).addField( "field", "f" ).verify();
    }

    @Test
    public void test2() {
        Node n1 = new OrientNode( orule.getDB() );
        n1.set( "field", "a" );
        n1.save();

        Node n2 = new OrientNode( orule.getDB() );
        n2.set( "field", "b" );
        n2.save();

        Edge e = new OrientEdge( n1, n2, TestType.TEST );

        NodeVerifier verifier = new NodeVerifier( n1 ).addField( "field", "a" ).addOutBoundRelation( n2, TestType.TEST ).verify();
    }
}
