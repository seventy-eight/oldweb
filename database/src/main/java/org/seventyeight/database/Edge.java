package org.seventyeight.database;

/**
 *
 * <pre>
 * outNode --> edge --> inNode
 * </pre>
 *
 * User: cwolfgang
 * Date: 19-11-12
 * Time: 08:49
 */
public interface Edge extends Parameterized<Edge> {

    /**
     * Get the source {@link Node} of the {@link Edge}
     * @param <T>
     * @return
     */
    public <T extends Node> T getSourceNode();

    /**
     * Get the target {@link Node} of the {@link Edge}
     * @param <T>
     * @return
     */
    public <T extends Node> T getTargetNode();
    public void remove();
}
