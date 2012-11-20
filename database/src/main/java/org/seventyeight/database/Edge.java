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
public interface Edge<EDGE extends Edge<EDGE, NODE>, NODE extends Node<NODE, EDGE>> extends Parameterized<EDGE> {
    public NODE getOutNode();
    public NODE getInNode();
    public void delete();
}
