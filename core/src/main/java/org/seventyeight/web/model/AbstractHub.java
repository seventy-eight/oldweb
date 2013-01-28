package org.seventyeight.web.model;

import org.apache.log4j.Logger;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.CouldNotLoadObjectException;
import org.seventyeight.web.exceptions.HubException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 11:16
 */
public abstract class AbstractHub extends AbstractItem implements Describable {

    private static Logger logger = Logger.getLogger( AbstractHub.class );

    public AbstractHub( Node node ) {
        super( node );
    }

    public int getNumberOfItems() {
        return node.getEdges( null, Direction.OUTBOUND ).size();
    }

    public int getNextIdentifier() {
        return getNumberOfItems();
    }

    public void setResourceIdentifier( long id ) {
        node.set( "resourceIdentifier", id );
    }

    public Long getResourceIdentifier() throws HubException {
        AbstractResource r = getParent();
        return r.getIdentifier();
    }

    public int addItemWithId( DatabaseItem item, EdgeType relation ) {
        int id = getNextIdentifier();
        Edge edge = node.createEdge( item.getNode(), relation );
        edge.set( "id", id );

        return id;
    }

    public void addItem( DatabaseItem item, EdgeType relation ) {
        createRelation( item, relation );
    }

    public void removeAllOutboundEdges() {
        logger.debug( "[Removing all edges] " + this );

        List<Edge> edges = node.getEdges( null, Direction.OUTBOUND );
        for( Edge e : edges ) {
            e.remove();
        }
    }

    public Descriptor<?> getDescriptor() {
        return SeventyEight.getInstance().getDescriptor( getClass() );
    }
}
