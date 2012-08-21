package org.seventyeight.web;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.seventyeight.web.exceptions.CouldNotLoadObjectException;
import org.seventyeight.web.model.AbstractItem;
import org.seventyeight.web.model.Descriptor;
import org.seventyeight.web.model.Item;
import org.seventyeight.web.model.Locale;
import org.seventyeight.web.model.ResourceDescriptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.query.nativ.ONativeSynchQuery;
import com.orientechnologies.orient.core.query.nativ.OQueryContextNativeSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class SeventyEight {
	private static Logger logger = Logger.getLogger( SeventyEight.class );
	private static SeventyEight instance;
	
	private org.seventyeight.loader.ClassLoader classLoader = null;

	private ODocument systemNode = null;

	private OGraphDatabase graphdb = null;
		
	public enum NodeType {
		item,
		/*resource,*/
		widgit,
		/*system*/
		
		unknown
	}
	
	private static final String SYSTEM_NODE_TYPE = "system";
	
	public enum EdgeType {
		owner,
		extension,
		translation,
		
		readAccess,
		writeAccess,
		reviewAccess
	}
	
	/* Paths */
	private File path;
	private File orientdbPath;
	private File pluginsPath;
	private File uploadPath;
	
	/* Locale */
	private Locale defaultLocale;
	
	/**
	 * A map of descriptors
	 */
	private Map<Class<?>, Descriptor<?>> descriptors = new HashMap<Class<?>, Descriptor<?>>();
	
	/**
	 * A map of resource types and their descriptors
	 */
	private Map<String, ResourceDescriptor<?>> resourceTypes = new HashMap<String, ResourceDescriptor<?>>();

	public SeventyEight() {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		instance = this;
		initialize( new File( System.getProperty( "user.home" ), ".seventyeight" ) );
	}

	public SeventyEight( String path ) {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		instance = this;
		initialize( new File( path ) );
	}

	public void initialize( File path ) {
		logger.debug( "Path: " + path.getAbsolutePath() );
		
		/* Prepare environment */
		logger.info( "Creating paths" );
		this.path = path;
		path.mkdirs();
		orientdbPath = new File( path, ".orientdb" );
		orientdbPath.mkdir();
		pluginsPath = new File( path, "plugins" );
		pluginsPath.mkdir();
		uploadPath = new File( path, "upload" );
		uploadPath.mkdir();

		/* Class loader */
		classLoader = new org.seventyeight.loader.ClassLoader( Thread.currentThread().getContextClassLoader() );

		/* Initialize database */
		try {
			graphdb = new OGraphDatabase( "local:" + orientdbPath.toString() ).open( "admin", "admin" );
		} catch( Exception e ) {
			logger.info( "Installing OrientDB to " + orientdbPath );
			graphdb = new OGraphDatabase( "local:" + orientdbPath.toString() ).create();
		}

		OClass cl = graphdb.getVertexType( SYSTEM_NODE_TYPE );
		
		if( cl == null ) {
			logger.info( "System node type not found, commencing installation" );
			install();
		} else {
			/* Get the system node */
			List<ODocument> result = graphdb.query( new OSQLSynchQuery<ODocument>( "select * from " + SYSTEM_NODE_TYPE ) );

			if( result.size() == 1 ) {
				systemNode = result.get( 0 );
			} else if( result.size() == 0 ) {
				logger.info( "System node not found, reinstalling" );
				install();
			} else {
				throw new IllegalStateException( "Too many system nodes" );
			}
		}
		
		cl = graphdb.getVertexType( SYSTEM_NODE_TYPE );
		logger.debug( "OCLASS: " + cl );
		
		/* Settings */
		defaultLocale = new Locale( "danish" );
	}
	
	public static SeventyEight getInstance() {
		return instance;
	}
	
	public Descriptor<?> getDescriptor( Class<?> clazz ) {
		logger.debug( "Getting descriptor for " + clazz );
		if( descriptors.containsKey( clazz ) ) {
			return descriptors.get( clazz );
		} else {
			return null;
		}
	}
	
	/**
	 * Given a resource type name, get its descriptor
	 * @param typeName
	 * @return
	 */
	public Descriptor<?> getDescriptorFromResourceType( String typeName ) {
		logger.debug( "Getting descriptor for resource type " + typeName );
		if( resourceTypes.containsKey( typeName ) ) {
			logger.debug( "I found " + resourceTypes.get( typeName ) );
			return resourceTypes.get( typeName );
		} else {
			logger.debug( "What????" );
			return null;
		}
	}
	
	private void install() {
		logger.info( "Installing system" );
		
		try {
			graphdb.createVertexType( NodeType.item.name() );
		} catch( Exception e ) {
			/* No op */
		}
		
		try {
			graphdb.createVertexType( NodeType.widgit.name() );
		} catch( Exception e ) {
			/* No op */
		}
		try {
			graphdb.createVertexType( SYSTEM_NODE_TYPE );
		} catch( Exception e ) {
			/* No op */
		}
		
		try {
			graphdb.createEdgeType( EdgeType.owner.name() );
		} catch( Exception e ) {
			/* No op */
		}
		try {
			graphdb.createEdgeType( EdgeType.extension.name() );
		} catch( Exception e ) {
			/* No op */
		}
		try {
			graphdb.createEdgeType( EdgeType.translation.name() );
		} catch( Exception e ) {
			/* No op */
		}
		
		/* Access types */
		try {
			graphdb.createEdgeType( EdgeType.readAccess.name() );
		} catch( Exception e ) {
			/* No op */
		}
		
		try {
			graphdb.createEdgeType( EdgeType.writeAccess.name() );
		} catch( Exception e ) {
			/* No op */
		}
		try {
			graphdb.createEdgeType( EdgeType.reviewAccess.name() );
		} catch( Exception e ) {
			/* No op */
		}
		
		/* TODO: Do a check on the system node */
		
		systemNode = graphdb.createVertex( SYSTEM_NODE_TYPE );
		systemNode.save();
	}

	public ODocument createNode( Class<?> clazz, NodeType type ) {
		logger.debug( "Creating a vertex of type " + type + ", " + clazz.getName() );
		ODocument node = graphdb.createVertex( type.name() ).field( "class", clazz.getName() );

		//mainNode.createRelationshipTo( node, TestRel.RELATION );

		return node;
	}
	
	public Item getItemByNode( ODocument node ) throws CouldNotLoadObjectException {		
		String clazz = null;
		try {
			clazz = (String) node.field( "class" );
			logger.debug( "Resource class: " + clazz );
		} catch( Exception e ) {
			logger.warn( "Null occured: " + e.getMessage() );
			throw new CouldNotLoadObjectException( "Unable to get the class " + clazz );
		}
		
		try {
			Class<Item> eclass = (Class<Item>) Class.forName(clazz, true, classLoader);
			Constructor<?> c = eclass.getConstructor( ODocument.class );
			Item instance = (Item) c.newInstance( node );
			return instance;
		} catch( Exception e ) {
			logger.error( "Unable to get the class " + clazz );
			throw new CouldNotLoadObjectException( "Unable to get the class " + clazz );
		}
	}
	
	public List<ODocument> getEdges( Item item, String label ) {
		Set<OIdentifiable> edges = graphdb.getOutEdges( item.getNode() );
		List<ODocument> result = new LinkedList<ODocument>();
		
		for( OIdentifiable e : edges ) {
			ODocument edge = (ODocument) e;
			if( edge.field( OGraphDatabase.LABEL ) != null && edge.field( OGraphDatabase.LABEL ).equals( label ) ) {
				result.add( edge );
			}
		}
		
		return result;
	}
	
	public T addLabel() {
		
	}
	
	/**
	 * Create an edge between nodes
	 * @param from
	 * @param to
	 * @param type
	 * @return
	 */
	public ODocument createEdge( Item from, Item to, EdgeType type ) {
		logger.debug( "Creating edge(" + type + ") from " + from.getNode().getClassName() + " to " + to.getNode().getClassName() );
		return graphdb.createEdge( from.getNode(), to.getNode(), type.name() );
	}
	
	
	/**
	 * Add an edge
	 * @param node
	 * @param other
	 * @param label
	 * @param replace
	 */
	public void addNodeRelation( ODocument node, ODocument other, String label, boolean replace ) {
		logger.debug( "Adding " + label + " to " + node );
		
		if( replace ) {
			logger.debug( "Removing relations " + label + " for " + node );
			Set<OIdentifiable> edges = graphdb.getOutEdges( node, label );
			graphdb.getOutEdges( null ).
			
			for( OIdentifiable edge : edges ) {
				graphdb.removeEdge( (ODocument) edge );
			}
		}
		
		graphdb.createed
		node.createRelationshipTo( other, label );
	}
	
	/**
	 * Given a {@link Node}, retrieve the {@link Node}s that relates to it with {@link RelationshipType}. 
	 * @param node
	 * @param rel
	 * @return
	 */
	public List<Node> getNodeRelation( Node node, RelationshipType rel ) {
		return getNodeRelation( node, rel, null, null );
	}
	
	/**
	 * Given an {@link Item}, retrieve the {@link Item}s that relates to it with a label and have a special property with a given value.
	 * @param node
	 * @param rel
	 * @param key
	 * @param value
	 * @return
	 */
	public List<Item> getNodeRelation( Item item, String label, String key, Object value ) {
		logger.debug( "Getting relations for " + item + "(" + key + "/" + value + ")" );
		//Iterator<Relationship> it = node.getRelationships( rel, Direction.OUTGOING ).iterator();
		List<ODocument> edges = getEdges( item, label );
		List<Item> items = new LinkedList<Item>();
		
		if( key != null ) {
			for( ODocument i : edges ) {
				if( i.field( key ) != null && i.field( key ).equals( value ) ) {
					items.add( new AbstractItem )
					//items.add( e )
				}
			}
		}
		
		while( it.hasNext() ) {
			Relationship r = it.next();
			if( key == null ) {
				list.add( r.getEndNode() );
			} else {
				Object o = r.getProperty( key, null );
				if( o != null && o.equals( value ) ) {
					list.add( r.getEndNode() );
				}
			}
		}
		
		return list;
	}
	
	/*
	 * Basic getters
	 */
	
	public Locale getDefaultLocale() {
		return defaultLocale;
	}
	
	/* 
	 * JSON
	 */
	
	public static final String __JSON_CONFIGURATION_NAME = "config";
	public static final String __JSON_CLASS_NAME = "class";
	
	public List<JsonObject> getConfigurationJsonObjects( JsonObject obj ) {
		List<JsonObject> objects = new ArrayList<JsonObject>();
		
		JsonElement configElement = obj.get( __JSON_CONFIGURATION_NAME );
		
		/**/
		if( configElement != null ) {
			if( configElement.isJsonObject() ) {
				logger.debug( "obj is JsonObject" );
				objects.add( configElement.getAsJsonObject() );
			} else if( configElement.isJsonArray() ) {
				logger.debug( "obj is JsonArray" );
				JsonArray jarray = configElement.getAsJsonArray();
				
				for( JsonElement e : jarray ) {
					logger.debug( "e is jsonObject" );
					if( e.isJsonObject() ) {
						objects.add( e.getAsJsonObject() );
					}
				}
			}
		}			
		
		return objects;
	}
}
