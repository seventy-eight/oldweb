package org.seventyeight.database;

import java.util.List;

/**
 * A generic database wrapper
 *
 * User: cwolfgang
 * Date: 19-11-12
 * Time: 12:29
 */
public interface Database<IDB, NT extends Node> {

    public void storeKeyValue( String key, Object value );
    public boolean containsKey( String key );
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

    /**
     * Create an index with given value types
     * @param indexName
     * @param type
     * @param valueTypes
     */
    public void createIndex( String indexName, IndexType type, IndexValueType ... valueTypes );

    /**
     * Put an element to the given index
     * @param indexName
     * @param node
     * @param keys
     */
    public void putToIndex( String indexName, NT node, Object ... keys );


    public List<NT> getFromIndex( String name, Object ... keys );

    /**
     * Remove a {@link Node} from the named index
     * @param indexName
     * @param node
     */
    public void removeNodeFromIndex( String indexName, NT node );
}
