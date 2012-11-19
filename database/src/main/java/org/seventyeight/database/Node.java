package org.seventyeight.database;

import java.util.List;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 22:44
 */
public interface Node<NODE extends Node<NODE, EDGE>, EDGE extends Edge<EDGE, NODE>> extends Parameterized<NODE> {

    /**
     * Get the {@link Database} attached to this {@link Node}
     * @return
     */
    public Database getDB();

    /**
     * Create an {@link Edge} to another {@link Node}
     * @param to
     * @param type
     * @return
     */
    public EDGE createEdge( NODE to, EdgeType type );

    /**
     * Get the {@link Edge}'s with a certain {@link EdgeType}
     * @param type
     * @return
     */
    public List<EDGE> getEdges( EdgeType type );

    /**
     * Get the {@link Edge}'s between this and the other {@link Node} with a certain {@link EdgeType}
     * @param type
     * @return
     */
    public List<EDGE> getEdges( NODE other, EdgeType type );


    /**
     * Get the {@link Edge}'s from this to the other {@link Node} with a certain {@link EdgeType}
     * @param to
     * @param type
     * @return
     */
    public List<EDGE> getEdgesTo( NODE to, EdgeType type );

    /**
     * Remove {@link Edge}'s of a certain type in the {@link Direction}
     * @param type
     * @param direction
     * @return
     */
    public Node removeEdges( EdgeType type, Direction direction );

    public NODE save();
}
