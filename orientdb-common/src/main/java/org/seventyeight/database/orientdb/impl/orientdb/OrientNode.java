package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

import java.util.List;

/**
 * User: cwolfgang
 * Date: 18-11-12
 * Time: 22:28
 */
public class OrientNode implements Node {

    private static Logger logger = Logger.getLogger( OrientNode.class );

    private OGraphDatabase db;
    private ODocument doc;

    public OrientNode( OGraphDatabase db, ODocument doc ) {
        this.db = db;
        this.doc = doc;
    }

    public ODocument getDocument() {
        return doc;
    }

    @Override
    public Node createEdge( Node to, EdgeType type ) {
        logger.debug( "Creating edge(" + type + ") from " + from.getNode().getClassName() + " to " + to.getNode().getClassName() );
        OrientNode n = (OrientNode) to;
        ODocument edge = db.createEdge( doc , n.getDocument(), type.toString() ).field( OGraphDatabase.LABEL, type.toString() ).save();
    }

    @Override
    public List<Edge> getEdges( Node item, EdgeType type ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Edge> getEdges( Node from, Node to, EdgeType type ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeEdge( Edge edge ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
