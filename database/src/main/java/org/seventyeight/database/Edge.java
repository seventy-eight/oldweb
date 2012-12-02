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
public interface Edge<NODE extends Node<NODE, EDGE>, EDGE extends Edge<NODE, EDGE>> extends Parameterized<EDGE> {
    public NODE getSourceNode();
    public NODE getTargetNode();
    public void delete();
}
