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
    public Node getOutNode();
    public Node getInNode();
    public void delete();
}
