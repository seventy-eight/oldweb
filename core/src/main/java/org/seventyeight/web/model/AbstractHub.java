package org.seventyeight.web.model;

import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.CouldNotLoadObjectException;
import org.seventyeight.web.exceptions.HubException;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 11:16
 */
public abstract class AbstractHub extends AbstractItem implements Describable {

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
        Long id = node.get( "resourceIdentifier" );
        if( id == null ) {
                AbstractResource r = getParent();
                setResourceIdentifier( r.getIdentifier() );
                return r.getIdentifier();
        } else {
            return id;
        }
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
