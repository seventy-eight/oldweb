package org.seventyeight.model.resources;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.seventyeight.GraphDragon;
import org.seventyeight.exceptions.CouldNotLoadObjectException;
import org.seventyeight.exceptions.CouldNotLoadResourceException;
import org.seventyeight.exceptions.ErrorWhileSavingException;
import org.seventyeight.exceptions.InconsistentParameterException;
import org.seventyeight.exceptions.IncorrectTypeException;
import org.seventyeight.exceptions.NoSuchJsonElementException;
import org.seventyeight.exceptions.ParameterDoesNotExistException;
import org.seventyeight.exceptions.ResourceDoesNotExistException;
import org.seventyeight.exceptions.TemplateDoesNotExistException;
import org.seventyeight.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.graph.OrderedExpander;
import org.seventyeight.model.AbstractResource;
import org.seventyeight.model.CheckCheckable;
import org.seventyeight.model.Extension;
import org.seventyeight.model.RequestContext;
import org.seventyeight.model.ResourceDescriptor;
import org.seventyeight.model.ResourceList;
import org.seventyeight.util.ExceptionUtils;
import org.seventyeight.util.RelationshipSorter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Collection extends AbstractResource {

	private static Logger logger = Logger.getLogger( Collection.class );
	
	public enum CollectionType implements RelationshipType {
		IN_COLLECTION
	}
	
	private Set<Long> cached;
	
	public Collection( Node node ) {
		super( node );
	}

	public String getPortrait() {
		return null;
	}

	public void save( RequestContext request, JsonObject jsonData ) throws ResourceDoesNotExistException, ParameterDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		doSave( new ResourceSaveImpl( this, request, jsonData ) );
	}
	
	public Index<Node> getIndex() {
		return GraphDragon.getInstance().getResourceIndex();
	}
	
	public void addResource( long identifier, long position ) throws CouldNotLoadResourceException {
		AbstractResource r = GraphDragon.getInstance().getResource( identifier );

		addResource( r, position );
	}
	
	public void addResource( AbstractResource resource, long position ) {
		logger.debug( "Adding " + resource + " at " + position + " to " + this );
		logger.debug( "Adding " + resource.getNode() + " at " + position + " to " + node );

		/* We should remove first?! */
		removeResource( resource );
		Relationship rel = node.createRelationshipTo( resource.getNode(), CollectionType.IN_COLLECTION );
		rel.setProperty( "order", position );
	}
	
	public void removeResource( long identifier ) throws CouldNotLoadResourceException {
		AbstractResource r = GraphDragon.getInstance().getResource( identifier );
		removeResource( r );
	}
	
	private List<AbstractResource> resourcesForView;
	
	@Override
	public void prepareView( RequestContext request ) {
		long offset = request.getKey( "offset", 0 );
		long length = request.getKey( "length", 5 );
		
		logger.debug( "Prepare collection, " + offset + ", " + length );
		
		OrderedExpander ox = new OrderedExpander( CollectionType.IN_COLLECTION, new OrderedExpander.Sorter( "order" ), (int)offset, (int)length );
		Traverser t = org.neo4j.kernel.Traversal.description().evaluator( Evaluators.excludeStartPosition() ).
															   evaluator( Evaluators.toDepth( 1 ) ).
															   expand( ox ).
															   traverse( node );
		
		resourcesForView = new ArrayList<AbstractResource>();
		
		for( Path p : t ) {
			try {
				AbstractResource r = GraphDragon.getInstance().getResource( p.endNode() );
				logger.debug( "ADDING RESOURCE: " + r );
				resourcesForView.add( r );
			} catch( CouldNotLoadObjectException e ) {
				ExceptionUtils.print( e, System.out, false );
			}
		}
	}
	
	public List<AbstractResource> getResources() {
		return resourcesForView;
	}
	
	/**
	 * TODO This is a really stupid implementation!!!
	 * @param resource
	 */
	public void removeResource( AbstractResource resource ) {
		logger.debug( "Removing " + resource + " from " + this );
		logger.debug( "Removing " + resource.getNode() + " to " + node );
		Iterator<Relationship> it = node.getRelationships( Direction.OUTGOING, CollectionType.IN_COLLECTION ).iterator();
		
		while( it.hasNext() ) {
			Relationship r = it.next();
			logger.debug( "CHECKING NODE IS " + r.getEndNode() + "/" + resource.getNode() );
			if( r.getEndNode().equals( resource.getNode() ) ) {
				logger.debug( "DELETING" );
				r.delete();
			}
		}
	}
	
	public void buildCache() {
		logger.debug( "Building cache" );
		cached = new HashSet<Long>();
		Iterable<Relationship> ships = node.getRelationships( Direction.OUTGOING, CollectionType.IN_COLLECTION );
		for( Relationship r : ships ) {
			logger.debug( "End node is " + r.getEndNode() );
			cached.add( (Long) r.getEndNode().getProperty( "identifier", 0l ) );
		}
		
		logger.debug( "Cache is now: " + cached );
	}
	
	public void print() {
		logger.debug( "Printing edges" );
		Iterable<Relationship> ships = node.getRelationships( Direction.OUTGOING, CollectionType.IN_COLLECTION );
		for( Relationship r : ships ) {
			long id = (Long) r.getEndNode().getProperty( "identifier", 0l );
			logger.debug( "End node is " + r.getEndNode() + " = " + id );
		}
	}
	
	public boolean isCollectionElement( AbstractResource resource ) {
		if( cached == null ) {
			buildCache();
		}
		
		return cached.contains( resource.getIdentifier() );
	}
	
	public void doList( RequestContext request, Writer writer, JsonObject jsonData ) throws IOException {
		logger.debug( "-----> IN HERE <------" );
		/* Determine query */
		String query = null;
		if( ( query = request.getKey( "query" ) ) != null ) {
			logger.debug( "Query is defined" );
		} else {
			logger.debug( "Query is NOT defined" );
			query = "";
			
			/* Get type */
			String type = request.getKey( "type", "*" );
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
			writer.write( "What? " + e.getMessage() );
		}
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
		
		@Override
		public Class<? extends Extension> getExtensionClass() {
			return null;
		}

		@Override
		public Collection newInstance() throws UnableToInstantiateObjectException {
			return super.newInstance();
		}
	}

}
