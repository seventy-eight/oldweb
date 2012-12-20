package org.seventyeight.web.model;

import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.utils.Date;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.CouldNotLoadObjectException;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 11:16
 */
public abstract class Hub extends AbstractDatabaseItem implements Describable {

    public Hub( Node node ) {
        super( node );
    }

    public AbstractResource getResource() throws CouldNotLoadObjectException {
        List<Edge> edges = node.getEdges( SeventyEight.HubRelation.resourceHubRelation, Direction.INBOUND );

        if( edges.size() == 1 ) {
            return (AbstractResource) SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getSourceNode() );
        } else {
            throw new IllegalStateException( "Found " + edges.size() + " edges, not 1" );
        }
    }

    public int getNumberOfItems() {
        return node.getEdges( null, Direction.OUTBOUND ).size();
    }

    public int getNextIdentifier() {
        return getNumberOfItems();
    }

    public int addItem( DatabaseItem item, EdgeType relation ) {
        int id = getNextIdentifier();
        Edge edge = node.createEdge( item.getNode(), relation );
        edge.set( "id", id );

        return id;
    }

    public Descriptor<?> getDescriptor() {
        return SeventyEight.getInstance().getDescriptor( getClass() );
    }
}
