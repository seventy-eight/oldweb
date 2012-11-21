package org.seventyeight.database.query;

/**
 * @author cwolfgang
 *         Date: 21-11-12
 *         Time: 13:27
 */
public class IndexType {

    public static final IndexType UNIQUE = new IndexType( "UNIQUE" );
    public static final IndexType FULLTEXT = new IndexType( "FULLTEXT" );
    public static final IndexType DICTIONARY = new IndexType( "DICTIONARY" );
    public static final IndexType REGULAR = new IndexType( "NOTUNIQUE" );

    private String name;

    private IndexType( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
