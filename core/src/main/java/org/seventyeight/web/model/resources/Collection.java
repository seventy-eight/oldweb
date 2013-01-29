package org.seventyeight.web.model.resources;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.seventyeight.database.*;
import org.seventyeight.utils.ExceptionUtils;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

public class Collection extends AbstractResource {

	private static Logger logger = Logger.getLogger( Collection.class );

    public enum CollectionEdgeType implements EdgeType {
        in_collection
    }
	
	private Set<Long> cached;
	
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

		/* We should remove first?! */
		removeResource( resource );
        Edge edge = resource.getNode().createEdge( this.getNode(), CollectionEdgeType.in_collection );
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
	
	private List<AbstractResource> resourcesForView;

	public void prepareView( Request request ) {
		long offset = request.getValue( "offset", 0 );
		long length = request.getValue( "length", 5 );
		
		logger.debug( "Prepare collection, " + offset + ", " + length );

        /*
		OrderedExpander ox = new OrderedExpander( CollectionType.IN_COLLECTION, new OrderedExpander.Sorter( "order" ), (int)offset, (int)length );
		Traverser t = org.neo4j.kernel.Traversal.description().evaluator( Evaluators.excludeStartPosition() ).
															   evaluator( Evaluators.toDepth( 1 ) ).
															   expand( ox ).
															   traverse( node );
		
		resourcesForView = new ArrayList<AbstractResource>();
		
		for( Path p : t ) {
			try {
				AbstractResource r = GraphDragon.getInstance().getItem( p.endNode() );
				logger.debug( "ADDING RESOURCE: " + r );
				resourcesForView.add( r );
			} catch( CouldNotLoadObjectException e ) {
				ExceptionUtils.print( e, System.out, false );
			}
		}
		*/
	}
	
	public List<AbstractResource> getResources() {
		return resourcesForView;
	}

    /**
     * Remove a resource from the collection
     * @param resource
     */
	public void removeResource( AbstractResource resource ) {
		logger.debug( "Removing " + resource + " from " + this );
		logger.debug( "Removing " + resource.getNode() + " to " + node );

        List<Edge> edges = this.getNode().getEdges( resource.getNode(), CollectionEdgeType.in_collection, Direction.INBOUND );

        /**
         * Delete all
         */
        for( Edge e : edges ) {
            e.remove();
        }
	}
	
	public void buildCache() {
		logger.debug( "Building cache" );
		cached = new HashSet<Long>();

        List<Edge> edges = node.getEdges( CollectionEdgeType.in_collection, Direction.INBOUND );

		for( Edge e : edges ) {
			logger.debug( "End node is " + e.getSourceNode() );
			cached.add( (Long) e.getSourceNode().get( "identifier", 0l ) );
		}
		
		logger.debug( "Cache is now: " + cached );
	}
	
	public void print() {
		logger.debug( "Printing edges" );

        List<Edge> edges = node.getEdges( CollectionEdgeType.in_collection, Direction.INBOUND );

        for( Edge e : edges ) {
			long id = (Long) e.getSourceNode().get( "identifier", 0l );
			logger.debug( "End node is " + e.getSourceNode() + " = " + id );
		}
	}
	
	public boolean isCollectionElement( AbstractResource resource ) {
		if( cached == null ) {
			buildCache();
		}
		
		return cached.contains( resource.getIdentifier() );
	}
	
	public void doList( Request request, Writer writer, JsonObject jsonData ) throws IOException {
		logger.debug( "-----> IN HERE <------" );
		/* Determine query */
		String query = null;
		if( ( query = request.getValue( "query" ) ) != null ) {
			logger.debug( "Query is defined" );
		} else {
			logger.debug( "Query is NOT defined" );
			query = "";
			
			/* Get type */
			String type = request.getValue( "type", "*" );
			query += "type:" + type;
		}
		
		logger.debug( "QUERY = " + query );
		
		/* Save */
		if( request.isRequestPost() && jsonData != null ) {
			logger.debug( "Saving collection" );
			logger.debug( "JSONDATA IS " + jsonData );
			JsonObject jo = (JsonObject) jsonData.get( "collection" );
			if( jo != null ) {
				logger.debug( "Collection was set" );
				logger.debug( "JSON IS " + jo );
				update( jo );
			} else {
				logger.debug( "Collection was NULL" );
			}
		}
		
		/* COLLECTION */
		print();

        /*
		ResourceList resources = ResourceList.getResources( query, 3, 0, "title", false );
		//resources.setIndexOrigin( idx );
		
		resources.setCheckable( new CheckCheckable() {
			@Override
			public boolean isChecked( AbstractResource resource ) {
				return Collection.this.isCollectionElement( resource );
			}
		} );
		
		try {
			resources.setSelectable( true );
			
			request.getContext().put( "searchUrl", request.getRequestURI() );
			request.getContext().put( "class", Collection.class.getName() );
			logger.debug( "Search url: " + request.getRequestURI() );
			
			GraphDragon.getInstance().renderObject( writer, resources, "detailed.vm", request.getTheme(), request.getContext() );
			
		} catch( TemplateDoesNotExistException e ) {
			writer.writeToFile( "What? " + e.getMessage() );
		}
		*/
	}
		
	public void update( JsonObject jsonData ) {
		logger.debug( "Updating collection" );
		Set<Entry<String, JsonElement>> entries = jsonData.entrySet();
		
		for( Entry<String, JsonElement> entry : entries ) {
			try {
				logger.debug( "ENTRY: " + entry );
				long id = Long.parseLong( entry.getKey() );
				if( entry.getValue().isJsonNull() ) {
					removeResource( id );
				} else {
					addResource( id, 1l );
				}
			} catch( Exception e ) {
				ExceptionUtils.print( e, System.out, true );
				logger.warn( "Skipping resource " + entry + ": " + e.getMessage() );
			}
		}
	}
	

	public static class CollectionDescriptor extends ResourceDescriptor<Collection> {

		@Override
		public String getDisplayName() {
			return "Collection";
		}
		
		@Override
		public String getType() {
			return "collection";
		}
	}

}
