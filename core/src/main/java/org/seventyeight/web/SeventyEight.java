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
import org.seventyeight.web.exceptions.CouldNotLoadResourceException;
import org.seventyeight.web.graph.Edge;
import org.seventyeight.web.handler.Renderer;
import org.seventyeight.web.model.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class SeventyEight {
	private static Logger logger = Logger.getLogger( SeventyEight.class );
	private static SeventyEight instance;
	
	public static final String defaultThemeName = "default";
	
	private org.seventyeight.loader.ClassLoader classLoader = null;

	private ODocument systemNode = null;

	private OGraphDatabase graphdb = null;
		
	public enum NodeType {
		item,
		/*resource,*/
		widgit,
		/*system*/
		text,
		
		unknown
	}
	
	private static final String SYSTEM_NODE_TYPE = "system";
	
	public interface EdgeType {
	}
	
	public enum ResourceEdgeType implements EdgeType {
		owner,
		contributor,
		extension,
		translation
	}
		
	public enum GroupEdgeType implements EdgeType {
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
	
	private Renderer renderer = new Renderer();
	private AbstractTheme defaultTheme;
	private I18N i18n;
	
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
	
	public SeventyEight( File path ) {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		instance = this;
		initialize( path );
	}

	private SeventyEight initialize( File path ) {
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
			graphdb = new OGraphDatabase( "local:" + orientdbPath.toString() );
			logger.info( "GRAPHDB: " + graphdb );
			graphdb.create();
		}


		/* Get the system node */
		graphdb.getDictionary().containsKey( SYSTEM_NODE_TYPE );
		if( graphdb.getDictionary().containsKey( SYSTEM_NODE_TYPE ) ) {
			systemNode = graphdb.getDictionary().get( SYSTEM_NODE_TYPE );
		} else {
			logger.info( "System node not found, installing" );
			install();
		}
		
		/* Settings */
		defaultLocale = new Locale( "danish" );
		
		return this;
	}
	
	public static SeventyEight getInstance() {
		return instance;
	}
	
	public void shutdown() {
		graphdb.close();
	}
	
	public AbstractResource getResource( Long id ) throws CouldNotLoadResourceException {
		ODocument node = getNodeByIndex();
		
		if( node != null ) {
			try {
				return (AbstractResource) getItem( node );
			} catch( CouldNotLoadObjectException e ) {
				logger.warn( "Unable to load resource object " + id );
				throw new CouldNotLoadResourceException( "Unable to get resource", e );
			}
		} else {
			throw new CouldNotLoadResourceException( id + "" );
		}
	}
	
	public ODocument getNodeByIndex() {
		return null;
	}
	
	public AbstractResource getResource( ODocument node ) throws CouldNotLoadObjectException {
		try {
			long id = (Long) node.field( "identifier" );
			return getResource( id );
		} catch( Exception e ) {
			throw new CouldNotLoadObjectException( node + " does not have identifier property", e );
		}
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
	
	
	/**
	 * Get a list of extension classes that implements this 
	 * @param extensionType
	 * @return
	 */
	/*
	public <T> List<T> getExtensions( Class<T> extensionType ) {
		logger.debug( "Getting extensions for " + extensionType );
		List<T> r = new ArrayList<T>();
		for( Class<Extension> e : extensions ) {
			//if( extensionType.isAssignableFrom( e ) ) {
			if( extensionType.equals( e ) ) {
				r.add( (T) e );
			}			
		}

		return r;
	}
	*/
	
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
			graphdb.createEdgeType( ResourceEdgeType.owner.name() );
		} catch( Exception e ) {
			/* No op */
		}
		try {
			graphdb.createEdgeType( ResourceEdgeType.contributor.name() );
		} catch( Exception e ) {
			/* No op */
		}
		try {
			graphdb.createEdgeType( ResourceEdgeType.extension.name() );
		} catch( Exception e ) {
			/* No op */
		}
		try {
			graphdb.createEdgeType( ResourceEdgeType.translation.name() );
		} catch( Exception e ) {
			/* No op */
		}
		
		/* Access types */
		try {
			graphdb.createEdgeType( GroupEdgeType.readAccess.name() );
		} catch( Exception e ) {
			/* No op */
		}
		
		try {
			graphdb.createEdgeType( GroupEdgeType.writeAccess.name() );
		} catch( Exception e ) {
			/* No op */
		}
		try {
			graphdb.createEdgeType( GroupEdgeType.reviewAccess.name() );
		} catch( Exception e ) {
			/* No op */
		}
		
		/* TODO: Do a check on the system node */
		
		systemNode = graphdb.createVertex( SYSTEM_NODE_TYPE );
		graphdb.getDictionary().put( SYSTEM_NODE_TYPE, systemNode );
		systemNode.save();
	}

	public ODocument createNode( Class<?> clazz, NodeType type ) {
		logger.debug( "Creating a vertex of type " + type + ", " + clazz.getName() );
		ODocument node = graphdb.createVertex( type.name() ).field( "class", clazz.getName() ).save();

		//mainNode.createRelationshipTo( node, TestRel.RELATION );

		return node;
	}

    /**
     * TODO: Implement
     * @param id
     * @return
     */
    public Item getItem( long id ) {
         return null;
    }
	
	public Item getItem( ODocument node ) throws CouldNotLoadObjectException {
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
			throw new CouldNotLoadObjectException( "Unable to get the class " + clazz, e );
		}
	}
	
	public List<ODocument> getEdges( Item item, EdgeType type ) {
		return getEdges( item, type.toString() );
	}
	
	public List<ODocument> getEdges( Item item, String label ) {
		Set<OIdentifiable> edges = graphdb.getOutEdges( item.getNode(), label );
		List<ODocument> result = new LinkedList<ODocument>();

		for( OIdentifiable e : edges ) {
			ODocument edge = (ODocument) e;
			if( edge.field( OGraphDatabase.LABEL ) != null && edge.field( OGraphDatabase.LABEL ).equals( label ) ) {
				result.add( edge );
			}
		}
		
		return result;
	}
	
	public List<ODocument> getNodes( Item item, EdgeType type ) {
		Set<OIdentifiable> edges = graphdb.getOutEdges( item.getNode(), type.toString() );
		List<ODocument> nodes = new LinkedList<ODocument>();
		
		for( OIdentifiable e : edges ) {
			nodes.add( graphdb.getOutVertex( e ) );
		}
		
		return nodes;
	}
	
	public List<Edge> getEdges( Item from, Item to ) {
		Set<ODocument> edges = graphdb.getEdgesBetweenVertexes( from.getNode(), to.getNode() );
		List<Edge> es = new LinkedList<Edge>();
		
		for( OIdentifiable e : edges ) {
			ODocument out = graphdb.getOutVertex( e );
			es.add( new Edge( (ODocument) e, from.getNode(), out ) );
		}
		
		return es;
	}
	
	public List<Edge> getEdges2( Item item, EdgeType type ) {
		logger.debug( "Getting edges from " + item + " of type " + type );
		Set<OIdentifiable> edges = graphdb.getOutEdges( item.getNode(), ( type != null ? type.toString() : null ) );
		logger.debug( "EDGES: " + edges );
		List<Edge> es = new LinkedList<Edge>();
		
		for( OIdentifiable e : edges ) {
			ODocument out = graphdb.getInVertex( e );
			
			Edge edge = new Edge( (ODocument) e, item.getNode(), out );
			es.add( edge );
			logger.debug( "Edge: " + edge );
		}
		
		return es;
	}
	
	public Item setLabel( Item item, String label ) {
		item.getNode().field( OGraphDatabase.LABEL, label );
		
		return item;
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
		return graphdb.createEdge( from.getNode(), to.getNode(), type.toString() ).field( OGraphDatabase.LABEL, type.toString() ).save();
	}
	
	public ODocument getOutNode( ODocument edge ) throws IllegalStateException {
		ODocument node = graphdb.getOutVertex( edge );
		if( node == null ) {
			throw new IllegalStateException( "End node for " + edge + " not found" );
		} else {
			return node;
		}
	}
	
	public void removeOutEdges( Item item, EdgeType type ) {
		logger.debug( "Removing out edges " + type + " from " + item );
		Set<OIdentifiable> edges = graphdb.getOutEdges( item.getNode(), type.toString() );
		for( OIdentifiable edge : edges ) {
			graphdb.removeEdge( (ODocument) edge );
		}
	}
	
	public void removeEdges( Item from, Item to, EdgeType type ) {
		logger.debug( "Removing relations " + type + " from " + from + " to " + to );
		List<Edge> edges = getEdges2( from, type );
				
		for( Edge edge : edges ) {
			if( edge.getOutNode().equals( to.getNode() ) ) {
				logger.debug( "Removing the edge " + edge );
				graphdb.removeEdge( edge.getEdge() );
			}
		}
	}
	
	public void removeEdge( ODocument edge ) {
		graphdb.removeEdge( edge );
	}
	
	
	/**
	 * Add an edge
	 * @param from
	 * @param to
	 * @param type
	 * @param replace
	 */
	public void addNodeRelation( Item from, Item to, EdgeType type, boolean replace ) {
		logger.debug( "Adding " + type + " to " + from );
		
		if( replace ) {
			removeEdges( from, to, type );
		}
		
		createEdge( from, to, type );
	}
	
	/**
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
	*/


    /**
     * Get the list of {@link AbstractExtension}s from an {@link AbstractItem}, that satisfies a given extension class
     * @param item - The item in question
     * @param oftype - The extensions must be of this class
     * @return A list of extensions
     */
    public List<AbstractExtension> getExtensions( AbstractItem item, Class<?> oftype ) {
        logger.debug( "Getting extensions for " + item + " of type " + oftype );
        //Iterator<Relationship> it = item.getNode().getRelationships( ExtensionRelations.EXTENSION, Direction.OUTGOING ).iterator();
        List<Edge> edges = getEdges2( item, ResourceEdgeType.extension );

        List<AbstractExtension> extensions = new ArrayList<AbstractExtension>();

        for( Edge edge : edges  ) {
            ODocument node = edge.getInNode();
            String c = (String) node.field( "class" );

            if( c != null && ( ( oftype == null ) || ( oftype != null && c.equals( oftype.getName() ) ) ) ) {
                logger.debug( "Adding " + c );
                AbstractExtension other = null;
                try {
                    Class<AbstractExtension> clazz = (Class<AbstractExtension>) Class.forName( c );
                    Constructor<AbstractExtension> constructor = clazz.getConstructor( ODocument.class );
                    other = constructor.newInstance( node );
                    extensions.add( other );
                } catch( Exception e ) {
                    logger.warn( "Unable to instantiate class " + c );
                }
            }
        }

        return extensions;
    }

	
	/*
	 * Basic getters
	 */
	
	public Locale getDefaultLocale() {
		return defaultLocale;
	}
	
	public AbstractTheme getDefaultTheme() {
		return defaultTheme;
	}
	
	public Renderer getRenderer() {
		return renderer;
	}
	
	public I18N getI18N() {
		return i18n;
	}

    public File getPath() {
        return path;
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
