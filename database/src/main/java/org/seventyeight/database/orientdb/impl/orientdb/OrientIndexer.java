package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.apache.log4j.Logger;
import org.seventyeight.database.IndexBuilder;
import org.seventyeight.database.IndexType;
import org.seventyeight.database.Indexer;
import org.seventyeight.utils.Builder;

/**
 * @author cwolfgang
 * Date: 20-11-12
 * Time: 21:05
 */
public class OrientIndexer implements IndexBuilder<OrientIndexer> {

    private static Logger logger = Logger.getLogger( OrientIndexer.class );

    private String name;
    private OGraphDatabase db;

    private boolean uniqueIndex = false;
    private boolean fulltextIndex = false;

    private IndexType type = IndexType.STRING;

    private boolean built = false;

    public OrientIndexer( OGraphDatabase db, String name ) {
        this.name = name;
        this.db = db;
    }

    @Override
    public OrientIndexer setUniqueness( boolean unique ) {
        uniqueIndex = unique;
        if( unique ) {
            fulltextIndex = false;
        }

        return this;
    }

    @Override
    public OrientIndexer setFulltextIndexed( boolean fulltextIndexed ) {
        fulltextIndex = fulltextIndexed;
        if( fulltextIndexed ) {
            uniqueIndex = false;
        }

        return this;
    }

    @Override
    public OrientIndexer setIndexType( IndexType type ) {
        this.type = type;

        return this;
    }

    @Override
    public String getIndexName() {
        return name;
    }

    @Override
    public OrientIndexer build() {
        if( built ) {
            throw new IllegalStateException( "Index already built" );
        }

        StringBuilder sb = new StringBuilder();
        sb.append( "CREATE INDEX " );
        sb.append( name );
        sb.append( " " );
        if( uniqueIndex ) {
            sb.append( "UNIQUE " );
        } else if( fulltextIndex ) {
            sb.append( "FULLTEXT " );
        } else {
            sb.append( "NOTUNIQUE " );
        }

        sb.append( type );

        System.out.println( "QUERY: " + sb );

        //db.getMetadata().getIndexManager().createIndex( name, type.toString(), null, null, null );
        System.out.println( "DB: " + db );
        OCommandSQL sql = new OCommandSQL( sb.toString() );
        db.command( sql );
        System.out.println( "--->" + sql.getText() );

        this.built = true;
        return this;
    }

    @Override
    public boolean isBuilt() {
        return built;
    }

    @Override
    public Boolean get() {
        if( !built ) {
            throw new IllegalStateException( "Index not built" );
        }

        return built;
    }
}
