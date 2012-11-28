package org.seventyeight.database;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 20:56
 */
public enum IndexValueType {
    STRING( "string" ),
    INTEGER( "integer" ),
    LONG( "long" ),
    DOUBLE( "double" ),
    DATE( "date" );

    private String name;

    IndexValueType( String name ) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }


}
