package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import org.seventyeight.database.Database;
import org.seventyeight.database.IndexValueType;
import org.seventyeight.database.Query;
import org.seventyeight.database.orientdb.impl.orientdb.query.OrientIndexBuilder;
import org.seventyeight.database.orientdb.impl.orientdb.query.OrientPutIndexBuilder;
import org.seventyeight.database.query.IndexType;

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

    public void createIndex( OrientNode node, String index, IndexType type, IndexValueType valueType ) {
        new OrientIndexBuilder( db, "letter" ).setIndexValueType( valueType ).setIndexType( type ).build().execute();
    }

    public <T> void putIndex( String name, OrientNode node, T key ) {
        new OrientPutIndexBuilder( node, name ).setKey( key ).build().execute();
    }
}
