package org.seventyeight.database;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 20:56
 */
public class IndexValueType {

    public static final IndexValueType STRING = new IndexValueType( "string" );
    public static final IndexValueType INTEGER = new IndexValueType( "integer" );
    public static final IndexValueType DOUBLE = new IndexValueType( "double" );
    public static final IndexValueType DATE = new IndexValueType( "date" );

    private String name;

    public IndexValueType( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
