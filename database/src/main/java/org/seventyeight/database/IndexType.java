package org.seventyeight.database;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 20:56
 */
public class IndexType {

    public static final IndexType STRING = new IndexType( "string" );
    public static final IndexType INTEGER = new IndexType( "integer" );
    public static final IndexType DOUBLE = new IndexType( "double" );
    public static final IndexType DATE = new IndexType( "date" );

    private String name;

    public IndexType( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
