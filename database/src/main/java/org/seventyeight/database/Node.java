package org.seventyeight.database;

import java.util.List;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 22:44
 */
public interface Node extends Parameterized<Node> {

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
    public <T extends Edge> T createEdge( Node to, EdgeType type );

    /**
     * Get the {@link Edge}'s with a certain {@link EdgeType} in a given {@link Direction}
     * @param type
     * @return
     */
    public <E extends Edge> List<E> getEdges( EdgeType type, Direction direction );

    /**
     * Get the {@link Edge}'s between this and the other {@link Node} with a certain {@link EdgeType} in a given {@link Direction}
     * @param other
     * @param type
     * @param direction
     * @return
     */
    public <E extends Edge> List<E> getEdges( Node other, EdgeType type, Direction direction );


    /**
     * Get the {@link Edge}'s from this to the other {@link Node} with a certain {@link EdgeType}
     * @param to
     * @param type
     * @return
     */
    public <E extends Edge> List<E> getEdgesTo( Node to, EdgeType type );

    /**
     * Remove {@link Edge}'s of a certain type in the {@link Direction}
     * @param type
     * @param direction
     * @return
     */
    public Node removeEdges( EdgeType type, Direction direction );

    public void remove();

    public Node save();
}
