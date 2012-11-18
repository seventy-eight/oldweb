package org.seventyeight.database.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.seventyeight.database.EdgeType;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 23:31
 */
public class DocumentVerifier {

    private OGraphDatabase db;

    private ODocument document;
    private Map<String, Object> fields = new HashMap<String, Object>();
    private List<Relation> inBoundRelations = new LinkedList<Relation>();
    private List<Relation> outBoundRelations = new LinkedList<Relation>();

    private class Relation {
        ODocument other;
        EdgeType type;
        public Relation( ODocument other, EdgeType type ) {
            this.other = other;
            this.type = type;
        }
    }

    public DocumentVerifier( OGraphDatabase db, ODocument document ) {
        this.db = db;
        this.document = document;
    }

    public DocumentVerifier verify() {

        System.out.println( "[Verify Document]" );
        verifyDocument();

        if( inBoundRelations.size() > 0 ) {
            System.out.println( "[Verify In bound relations]" );
            verifyRelations( inBoundRelations );
        }

        if( outBoundRelations.size() > 0 ) {
            System.out.println( "[Verify Out bound relations]" );
            verifyRelations( inBoundRelations );
        }

        return this;
    }

    private void verifyDocument() {
        for( String key : fields.keySet() ) {
            Object value = fields.get( key );

            System.out.println( "[Field{" + key + "}] " + document.field( key ) + " == " + value );
            assertThat( document.field( key ), is( value ) );
        }
    }

    private void verifyRelations( List<Relation> relations ) {

        for( Relation r : relations ) {
            Set<OIdentifiable> ois = db.getEdgesBetweenVertexes( document, r.other, new String[] { r.type.toString() } );
            if( ois.size() == 0 ) {
                fail( "The relation between does not have " + r.type.toString() );
            }
        }
    }

    public DocumentVerifier addField( String key, Object value ) {
        fields.put( key, value );

        return this;
    }

    public DocumentVerifier addInBoundRelation( ODocument other, EdgeType type ) {
        inBoundRelations.add( new Relation( other, type ) );

        return this;
    }
}
