package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.common.collection.OCompositeKey;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OCompositeIndexDefinition;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexDefinition;
import com.orientechnologies.orient.core.index.OSimpleKeyIndexDefinition;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransaction;
import org.seventyeight.database.Database;
import org.seventyeight.database.IndexValueType;
import org.seventyeight.database.orientdb.impl.orientdb.query.OrientIndexBuilder;
import org.seventyeight.database.IndexType;

import java.util.*;

/**
 * User: cwolfgang
 * Date: 19-11-12
 * Time: 12:29
 */
public class OrientDatabase implements Database<OGraphDatabase, OrientNode> {

    private OGraphDatabase db;

    public OrientDatabase( OGraphDatabase db ) {
        this.db = db;
    }

    public OGraphDatabase getInternalDatabase() {
        return db;
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public void beginTransation() {
        db.begin( OTransaction.TXTYPE.OPTIMISTIC );
    }

    @Override
    public void commitTransaction() {
        db.commit();
    }

    @Override
    public void rollbackTransaction() {
        db.rollback();
    }

    @Override
    public OrientNode createNode() {
        return new OrientNode( this );
    }

    @Override
    public void keepNode( String key, OrientNode node ) {
        System.out.println( "BEFORE: " + db.getDictionary().keys() );
        db.getDictionary().put( key, node.getDocument() );
        System.out.println( "AFTER: " + db.getDictionary().keys() );
    }

    @Override
    public OrientNode getNode( String key ) {
        return new OrientNode( this, (ODocument) db.getDictionary().get( key ) );
    }

    @Override
    public boolean containsNode( String key ) {
        return db.getDictionary().containsKey( key );
    }

    public OClass createClass( String className, String superClass, boolean abstractClass ) {

        if( superClass != null ) {
            OClass sclass = db.getMetadata().getSchema().getClass( superClass );
            if( abstractClass ) {
                return db.getMetadata().getSchema().createAbstractClass( className, sclass );
            } else {
                return db.getMetadata().getSchema().createClass( className );
            }
        } else {
            if( abstractClass ) {
                return db.getMetadata().getSchema().createAbstractClass( className );
            } else {
                return db.getMetadata().getSchema().createClass( className );
            }
        }
    }

    public void createIndex2( String index, IndexType type, IndexValueType valueType ) {
        new OrientIndexBuilder( db, "letter" ).setIndexValueType( valueType ).setIndexType( type ).build().execute();
    }

    public void createIndex( String index, IndexType type, IndexValueType ... valueTypes ) {
        //OIndexDefinition def = OIndexDefinitionFactory.createIndexDefinition( null, Collections.singletonList( "letter" ), Collections.singletonList( OType.FLOAT ) );
        OSimpleKeyIndexDefinition def2 = new OSimpleKeyIndexDefinition( getOrientValuesTypes( valueTypes ) );
        OIndex idx = db.getMetadata().getIndexManager().createIndex( index, type.toString(), def2, null, null );
        //db.getMetadata().getIndexManager().create();

        System.out.println( db.getMetadata().getIndexManager().getIndex( index ).getSize() );
        System.out.println( Arrays.asList( db.getMetadata().getIndexManager().getIndex( index ).getKeyTypes() ) );
    }

    private OType[] getOrientValuesTypes( IndexValueType[] types ) {
        OType[] otypes = new OType[types.length];
        for( int i = 0 ; i < types.length ; i++ ) {
            IndexValueType v = types[i];
            otypes[i] = getOrientType( v );
        }

        return otypes;
    }

    private OType getOrientType( IndexValueType type ) {
        switch( type ) {
            case STRING:
                return OType.STRING;

            case INTEGER:
                return OType.INTEGER;

            case LONG:
                return OType.LONG;

            case DATE:
                return OType.DATE;

            default:
                throw new IllegalStateException( type + " not allowed" );
        }
    }

    public void putToIndex( String name, OrientNode node, Object ... keys ) {
        //new OrientPutIndexBuilder( node, name ).setKey( key ).build().execute();

        //db.getMetadata().getIndexManager().getIndex( name ).create()
        if( keys.length > 1 ) {
            OCompositeKey k = new OCompositeKey( keys );
            db.getMetadata().getIndexManager().getIndex( name ).put( k, node.getDocument() );
        } else {
            db.getMetadata().getIndexManager().getIndex( name ).put( keys[0], node.getDocument() );
        }
    }

    public List<OrientNode> getFromIndex( String name, Object ... keys ) {
        int count = db.getMetadata().getIndexManager().getIndex( name ).getDefinition().getParamCount();
        System.out.println( "COUNT: " + count );
        Collection<OIdentifiable> docs = null;
        if( keys.length > 1 ) {
            docs = db.getMetadata().getIndexManager().getIndex( name ).getValues( Collections.singleton( new OCompositeKey( keys ) ) );
        } else {
            //docs = db.getMetadata().getIndexManager().getIndex( name ).getValues( Collections.singleton( new OCompositeKey( keys[0] ) ) );
            if( count == 1 ) {
                docs = db.getMetadata().getIndexManager().getIndex( name ).getValuesBetween( keys[0], keys[0] );
            } else {
                docs = db.getMetadata().getIndexManager().getIndex( name ).getValuesBetween( new OCompositeKey( keys[0] ), new OCompositeKey( keys[0] ) );
            }
        }

        List<OrientNode> nodes = new LinkedList<OrientNode>();

        for( OIdentifiable d : docs ) {
            nodes.add( new OrientNode( this, (ODocument) d.getRecord() ) );
        }

        return nodes;
    }

    public List<OrientNode> getFromIndexAbove( String name, int limit, Object ... keys ) {
        int count = db.getMetadata().getIndexManager().getIndex( name ).getDefinition().getParamCount();
        System.out.println( "COUNT: " + count );
        System.out.println( "COUNT: " + keys.length );
        Collection<OIdentifiable> docs = null;
        if( keys.length > 1 ) {
            docs = db.getMetadata().getIndexManager().getIndex( name ).getValuesMajor( new OCompositeKey( keys ), true, limit );
        } else {
            if( count == 1 ) {
                docs = db.getMetadata().getIndexManager().getIndex( name ).getValuesMajor( keys[0], true, limit );
            } else {
                docs = db.getMetadata().getIndexManager().getIndex( name ).getValuesMajor( new OCompositeKey( keys[0] ), true, limit );
            }
        }

        System.out.println( "NNODEASS: " + docs );

        List<OrientNode> nodes = new LinkedList<OrientNode>();

        for( OIdentifiable d : docs ) {
            nodes.add( new OrientNode( this, (ODocument) d.getRecord() ) );
        }

        return nodes;
    }

    /*
    public void bla() {
        db.getMetadata().getIndexManager().getIndex("").getValues()
    }
    */

    public void removeNodeFromIndex( String indexName, OrientNode node ) {
        db.getMetadata().getIndexManager().getIndex( indexName ).remove( node.getDocument() );
    }

    @Override
    public OrientNode getByIndex( String idx ) {
        ODocument doc = db.load( new ORecordId( idx ) );

        if( doc != null ) {
            return new OrientNode( this, doc );
        } else {
            return null;
        }
    }
}
