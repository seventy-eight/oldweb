package org.seventyeight.web.model.resources;

import java.util.List;

import org.apache.log4j.Logger;
import com.google.gson.JsonObject;
import org.seventyeight.database.*;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;
import org.seventyeight.web.util.ResourceSet;

public class Collection extends AbstractResource {

	private static Logger logger = Logger.getLogger( Collection.class );

    public enum CollectionEdgeType implements EdgeType {
        inCollection
    }
	
	private ResourceSet cache;
	
	public Collection( Node node ) {
		super( node );
	}

	public String getPortrait() {
		return null;
	}

    @Override
    public Save getSaver( CoreRequest request, JsonObject jsonData ) {
		return new ResourceSaveImpl( this, request, jsonData );
	}

	public void addResource( long identifier, long position ) throws CouldNotLoadItemException, TooManyException, NotFoundException {
		AbstractResource r = SeventyEight.getInstance().getResource( getDB(), identifier );

		addResource( r, position );
	}
	
	public void addResource( AbstractResource resource, long position ) {
		logger.debug( "Adding " + resource + " at " + position + " to " + this );
		logger.debug( "Adding " + resource.getNode() + " at " + position + " to " + node );

		/* We should remove first?!, NO! */
        Edge edge = resource.getNode().createEdge( this.getNode(), CollectionEdgeType.inCollection );
        edge.set( "order", position );
        edge.save();
	}

    /**
     * Remove a resource from a collection
     * @param identifier
     * @throws org.seventyeight.web.exceptions.CouldNotLoadItemException
     */
	public void removeResource( long identifier ) throws CouldNotLoadItemException, TooManyException, NotFoundException {
		AbstractResource r = SeventyEight.getInstance().getResource( getDB(), identifier );
		removeResource( r );
	}

    /**
     * Remove a resource from the collection
     * @param resource
     */
	public void removeResource( AbstractResource resource ) {
		logger.debug( "Removing " + resource + " from " + this );
		logger.debug( "Removing " + resource.getNode() + " to " + node );

        List<Edge> edges = this.getNode().getEdges( resource.getNode(), CollectionEdgeType.inCollection, Direction.INBOUND );

        /**
         * Delete all
         */
        for( Edge e : edges ) {
            e.remove();
        }
	}

	public void buildCache() {
		logger.debug( "Building cache" );
		cache = new ResourceSet();

        List<Edge> edges = node.getEdges( CollectionEdgeType.inCollection, Direction.INBOUND );

		for( Edge e : edges ) {
            try {
                AbstractResource r = SeventyEight.getInstance().getDatabaseItem( e.getTargetNode() );
                logger.debug( "End node is " + e.getSourceNode() );
                cache.add( r );
            } catch( CouldNotLoadObjectException e1 ) {
                e1.printStackTrace();
            }
		}

		logger.debug( "Cache is now: " + cache );
	}

	public boolean isCollectionElement( AbstractResource resource ) {
		if( cache == null ) {
			buildCache();
		}

		return cache.contains( resource.getIdentifier() );
	}

    public static final String INDEX_COLLECTIONS = "collections";

	public static class CollectionDescriptor extends ResourceDescriptor<Collection> {

		@Override
		public String getDisplayName() {
			return "Collection";
		}
		
		@Override
		public String getType() {
			return "collection";
		}

        @Override
        public void configureIndex( Database db ) {
            logger.debug( "Configuring " + INDEX_COLLECTIONS );
            // collectioId(rid), order, resourceId
            db.createIndex( INDEX_COLLECTIONS, IndexType.REGULAR, IndexValueType.LONG, IndexValueType.LONG, IndexValueType.LONG );
        }
	}

}
