package org.seventyeight.database;

/**
 * A generic database wrapper
 *
 * User: cwolfgang
 * Date: 19-11-12
 * Time: 12:29
 */
public interface Database<IDB, NT extends Node> {

    public void storeKeyValue( String key, Object value );
    public <T> T getValue( String key );
    public <T> T getValue( String key, T defaultValue );

    /**
     * Get the internal database behind this wrapper
     * @return
     */
    public IDB getInternalDatabase();

    /**
     * Create a new {@link Node}
     * @return
     */
    public NT createNode();
}
