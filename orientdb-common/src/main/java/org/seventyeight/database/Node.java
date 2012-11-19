package org.seventyeight.database;

import java.util.List;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 22:44
 */
public interface Node {

    /**
     * Create an {@link Edge} to another {@link Node}
     * @param to
     * @param type
     * @return
     */
    public Edge createEdge( Node to, EdgeType type );

    /**
     * Get the {@link Edge}'s with a certain {@link EdgeType}
     * @param type
     * @return
     */
    public List<Edge> getEdges( EdgeType type );

    /**
     * The the {@link Edge}'s to another {@link Node} with a certain {@link EdgeType}
     * @param to
     * @param type
     * @return
     */
    public List<Edge> getEdges( Node to, EdgeType type );

    /**
     * Get a property from the {@link Node}
     * @param key
     * @param <T>
     * @return
     */
    public <T> T get( String key );

    /**
     * Set a property on the {@link Node}
     * @param key
     * @param value
     * @param <T>
     */
    public <T> void set( String key, T value );

    public void save();
}
