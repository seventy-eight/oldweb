package org.seventyeight.database.orientdb;

import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 23:31
 */
public class NodeVerifier {

    private Node node;
    private Map<String, Object> fields = new HashMap<String, Object>();
    private List<Relation> inBoundRelations = new LinkedList<Relation>();
    private List<Relation> outBoundRelations = new LinkedList<Relation>();

    private class Relation {
        Node other;
        EdgeType type;
        boolean bidirectional = true;

        public Relation( Node other, EdgeType type ) {
            this.other = other;
            this.type = type;
        }

        public Relation( Node other, EdgeType type, boolean bidirectional ) {
            this.other = other;
            this.type = type;
            this.bidirectional = bidirectional;
        }

        public String toString() {
            return "Relation[" + node + " with edge type " + type + "]";
        }
    }

    public NodeVerifier( Node node ) {
        this.node = node;
    }

    public NodeVerifier verify() {

        System.out.println( "[Verify Node]" );
        verifyNode();

        if( inBoundRelations.size() > 0 ) {
            System.out.println( "[Verify In bound relations]" );
            verifyRelations( inBoundRelations );
        }

        if( outBoundRelations.size() > 0 ) {
            System.out.println( "[Verify Out bound relations]" );
            verifyRelations( outBoundRelations );
        }

        return this;
    }

    private void verifyNode() {
        for( String key : fields.keySet() ) {
            Object value = fields.get( key );

            System.out.println( "[Field{" + key + "}] " + node.get( key ) + " == " + value );
            assertThat( node.get( key ), is( value ) );
        }
    }

    private void verifyRelations( List<Relation> relations ) {
        for( Relation r : relations ) {
            System.out.println( "Checking " + r );
            List<Edge> edges = null;
            if( r.bidirectional ) {
                edges = node.getEdges( r.other, r.type );
            } else {
                edges = node.getEdgesTo( r.other, r.type );
            }

            if( edges.size() == 0 ) {
                fail( "No relation with type " + r.type.toString() );
            }

            for( Edge e : edges ) {
                if( !e.getTargetNode().equals( r.other ) ) {
                    fail( "The other node was not " + r.other );
                }
            }
        }
    }

    public NodeVerifier addField( String key, Object value ) {
        fields.put( key, value );

        return this;
    }

    public NodeVerifier addInBoundRelation( Node other, EdgeType type ) {
        inBoundRelations.add( new Relation( other, type ) );

        return this;
    }

    public NodeVerifier addInBoundRelation( Node other, EdgeType type, boolean bidirectional ) {
        inBoundRelations.add( new Relation( other, type, bidirectional ) );

        return this;
    }

    public NodeVerifier addOutBoundRelation( Node other, EdgeType type ) {
        outBoundRelations.add( new Relation( other, type ) );

        return this;
    }

    public NodeVerifier addOutBoundRelation( Node other, EdgeType type, boolean bidirectional ) {
        outBoundRelations.add( new Relation( other, type, bidirectional ) );

        return this;
    }
}
