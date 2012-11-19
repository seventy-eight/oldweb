package org.seventyeight.database;

import java.util.List;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 22:44
 */
public interface Node<DB> extends Parameterized<Node> {

    /**
     * Get the {@link Database} attached to this {@link Node}
     * @return
     */
    public DB getDB();

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
     * Get the {@link Edge}'s between this and the other {@link Node} with a certain {@link EdgeType}
     * @param to
     * @param type
     * @return
     */
    public List<Edge> getEdges( Node other, EdgeType type );


    /**
     * Get the {@link Edge}'s from this to the other {@link Node} with a certain {@link EdgeType}
     * @param to
     * @param type
     * @return
     */
    public List<Edge> getEdgesTo( Node to, EdgeType type );

    public Node save();
}
