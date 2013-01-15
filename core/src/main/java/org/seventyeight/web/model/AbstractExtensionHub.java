package org.seventyeight.web.model;

import org.apache.log4j.Logger;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;

import java.util.LinkedList;
import java.util.List;

/**
 * @author cwolfgang
 *         Date: 29-12-12
 *         Time: 22:03
 */
public abstract class AbstractExtensionHub<T extends Describable> extends Hub {

    private static Logger logger = Logger.getLogger( AbstractExtensionHub.class );

    public AbstractExtensionHub( Node node ) {
        super( node );
    }

    public abstract EdgeType getRelationType();

    /**
     * Get all the defined extensions for this type
     * @return
     */
    public List<T> getExtensions() {
        List<Edge> edges = node.getEdges( getRelationType(), Direction.OUTBOUND );

        List<T> items = new LinkedList<T>();

        for( Edge edge : edges ) {
            try {
                DatabaseItem item = SeventyEight.getInstance().getDatabaseItem( edge.getTargetNode() );
                logger.debug( "Got " + item );
                items.add( (T) item );
            } catch( Exception e ) {
                logger.warn( e );
            }
        }

        return items;
    }

    public void removeExtensions() {
        List<Edge> edges = node.getEdges( getRelationType(), Direction.OUTBOUND );

        for( Edge edge : edges ) {
            try {
                DatabaseItem item = SeventyEight.getInstance().getDatabaseItem( edge.getTargetNode() );
                logger.debug( "Removing " + item );
                item.remove();
                /* Removing edge */
                edge.remove();
            } catch( Exception e ) {
                logger.warn( e );
            }
        }
    }

    public void addExtension( T extension ) {
        createRelation( extension, getRelationType() );
    }
}
