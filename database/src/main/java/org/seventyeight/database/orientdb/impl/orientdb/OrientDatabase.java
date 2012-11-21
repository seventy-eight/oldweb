package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexDefinition;
import com.orientechnologies.orient.core.index.OIndexDefinitionFactory;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.seventyeight.database.Database;
import org.seventyeight.database.IndexValueType;
import org.seventyeight.database.Query;
import org.seventyeight.database.orientdb.impl.orientdb.query.OrientIndexBuilder;
import org.seventyeight.database.orientdb.impl.orientdb.query.OrientPutIndexBuilder;
import org.seventyeight.database.query.IndexType;

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
    public OrientNode createNode() {
        return new OrientNode( this );
    }

    public void createIndex( String index, IndexType type, IndexValueType valueType ) {
        //new OrientIndexBuilder( db, "letter" ).setIndexValueType( valueType ).setIndexType( type ).build().execute();

        OIndexDefinition def = OIndexDefinitionFactory.createIndexDefinition( null, Collections.singletonList( "letter" ), Collections.singletonList( OType.FLOAT ) );
        OIndex idx = db.getMetadata().getIndexManager().createIndex( index, type.toString(), null, null, null );
        db.getMetadata().getIndexManager().create();


        System.out.println( db.getMetadata().getIndexManager().getIndex( index ).getSize() );
        System.out.println( Arrays.asList( db.getMetadata().getIndexManager().getIndex( index ).getKeyTypes() ) );
    }

    public <T> void putIndex( String name, OrientNode node, T key ) {
        //new OrientPutIndexBuilder( node, name ).setKey( key ).build().execute();
        db.getMetadata().getIndexManager().getIndex( name ).put( key, node.getDocument() );
    }

    public <T> List<OrientNode> getFromIndex( String name, T key ) {
        Collection<ODocument> docs = db.getMetadata().getIndexManager().getIndex( name ).getEntries( Collections.singleton( key ) );


        List<OrientNode> nodes = new LinkedList<OrientNode>();

        for( ODocument d : docs ) {
            nodes.add( new OrientNode( this, d ) );
        }

        return nodes;
    }
}
