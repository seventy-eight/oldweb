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
    public <T extends Node> T getSourceNode();
    public <T extends Node> T getTargetNode();
    public void delete();
}
