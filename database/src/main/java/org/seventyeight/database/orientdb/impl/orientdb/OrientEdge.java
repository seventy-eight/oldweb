package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

/**
 * User: cwolfgang
 * Date: 18-11-12
 * Time: 22:37
 */
public class OrientEdge implements Edge<OrientEdge, OrientNode> {

    private static Logger logger = Logger.getLogger( OrientEdge.class );

    private OIdentifiable edge;
    private OrientNode out;
    private OrientNode in;

    public OrientEdge( OrientNode out, OrientNode in, EdgeType type ) {
        OrientNode n1 = (OrientNode) out;
        OrientNode n2 = (OrientNode) in;
        edge = n1.getDB().getInternalDatabase().createEdge( n1.getDocument(), n2.getDocument() ).field( OGraphDatabase.LABEL, type.toString() ).save();
        this.out = out;
        this.in = in;
    }

    public OrientEdge( OIdentifiable edge, OrientNode out, OrientNode in ) {
        this.edge = edge;
        this.out = out;
        this.in = in;
    }

    @Override
    public OrientNode getOutNode() {
        return out;
    }

    @Override
    public OrientNode getInNode() {
        return in;
    }

    @Override
    public void delete() {
        logger.debug( "Removing the edge " + this );
        ((OrientNode)out).getDB().getInternalDatabase().removeEdge( edge );
    }

    @Override
    public <T> T get( String key ) {
        return ((ODocument)edge.getRecord()).field( key );
    }

    @Override
    public <T> OrientEdge set( String key, T value ) {
        ((ODocument)edge.getRecord()).field( key, value );

        return this;
    }

    @Override
    public OrientEdge save() {
        edge.getRecord().save();

        return this;
    }


    @Override
    public String toString() {
        return "OrientEdge[" + edge + "]";
    }
}
