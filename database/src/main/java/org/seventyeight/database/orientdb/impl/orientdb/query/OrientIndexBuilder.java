package org.seventyeight.database.orientdb.impl.orientdb.query;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import org.apache.log4j.Logger;
import org.seventyeight.database.IndexValueType;
import org.seventyeight.database.Query;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDefaultQuery;
import org.seventyeight.database.query.IndexBuilder;
import org.seventyeight.database.IndexType;

/**
 * @author cwolfgang
 * Date: 20-11-12
 * Time: 21:05
 */
public class OrientIndexBuilder implements IndexBuilder<OrientIndexBuilder> {

    private static Logger logger = Logger.getLogger( OrientIndexBuilder.class );

    private String name;
    private OGraphDatabase db;

    private IndexType type = IndexType.REGULAR;

    private IndexValueType valueType = IndexValueType.STRING;

    private boolean built = false;

    public OrientIndexBuilder( OGraphDatabase db, String name ) {
        this.name = name;
        this.db = db;
    }

    @Override
    public OrientIndexBuilder setIndexType( IndexType type ) {
        this.type = type;

        return this;
    }

    @Override
    public OrientIndexBuilder setIndexValueType( IndexValueType valueType ) {
        this.valueType = valueType;

        return this;
    }

    @Override
    public String getIndexName() {
        return name;
    }

    @Override
    public Query build() {
        StringBuilder sb = new StringBuilder();
        sb.append( "CREATE INDEX " );
        sb.append( name );
        sb.append( " " );
        sb.append( type );
        sb.append( " " );
        sb.append( valueType );

        this.built = true;
        return new OrientDefaultQuery( db, sb.toString() );
    }

    @Override
    public boolean isBuilt() {
        return built;
    }

}
