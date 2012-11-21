package org.seventyeight.database.orientdb.impl.orientdb.query;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import org.apache.log4j.Logger;
import org.seventyeight.database.IndexValueType;
import org.seventyeight.database.Query;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDefaultQuery;
import org.seventyeight.database.orientdb.impl.orientdb.OrientNode;
import org.seventyeight.database.query.IndexType;
import org.seventyeight.database.query.PutIndexBuilder;

/**
 * @author cwolfgang
 *         Date: 21-11-12
 *         Time: 13:17
 */
public class OrientPutIndexBuilder implements PutIndexBuilder<OrientPutIndexBuilder> {

    private static Logger logger = Logger.getLogger( OrientIndexBuilder.class );

    private String name;
    private OrientNode node;
    private Object key;

    private boolean built = false;

    public OrientPutIndexBuilder( OrientNode node, String name ) {
        this.name = name;
        this.node = node;
    }

    @Override
    public OrientPutIndexBuilder setKey( Object key ) {
        this.key = key;

        return this;
    }

    @Override
    public String getIndexName() {
        return name;
    }

    @Override
    public Query build() {
        StringBuilder query = new StringBuilder();
        query.append( "INSERT INTO index:" );
        query.append( name );
        query.append( " (key,rid) VALUES ('" );
        query.append( key );
        query.append( "', " );
        query.append( node.getDocument().getIdentity() );
        query.append( ")" );

        System.out.println( "Query: " + query.toString() );
        this.built = true;

        return new OrientDefaultQuery( node.getDB().getInternalDatabase(), query.toString() );
    }

    @Override
    public boolean isBuilt() {
        return built;
    }
}
