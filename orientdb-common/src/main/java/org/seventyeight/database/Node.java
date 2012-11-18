package org.seventyeight.database;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.List;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 22:44
 */
public interface Node {

    public Node createEdge( Node from, Node to, EdgeType type );

    public List<Edge> getEdges( Node item, EdgeType type );
    public List<Edge> getEdges( Node from, Node to, EdgeType type );

    public void removeEdge( Edge edge );
}
