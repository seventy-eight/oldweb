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
public interface Edge<EDGE extends Edge, NODE extends Node> extends Parameterized<EDGE> {
    public NODE getOutNode();
    public NODE getInNode();
    public void delete();
}
