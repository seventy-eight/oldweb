package org.seventyeight.web;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.web.exceptions.CouldNotLoadObjectException;
import org.seventyeight.web.exceptions.CouldNotLoadResourceException;
import org.seventyeight.web.exceptions.NotFoundException;
import org.seventyeight.web.exceptions.TooManyException;
import org.seventyeight.web.handler.Renderer;
import org.seventyeight.web.model.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class SeventyEight {
	private static Logger logger = Logger.getLogger( SeventyEight.class );
	private static SeventyEight instance;

    public static final String INDEX_RESOURCES = "resources";
	
	public static final String defaultThemeName = "default";
	
	private org.seventyeight.loader.ClassLoader classLoader = null;

	private Node systemNode = null;

	//private OGraphDatabase graphdb = null;
		
	public enum NodeType {
		item,
		/*resource,*/
		widgit,
		/*system*/
		text,
		
		unknown
	}
	
	private static final String SYSTEM_NODE_TYPE = "system";
	
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

    private ConcurrentMap<Class, DatabaseInquirer> dbinq = new ConcurrentHashMap<Class, DatabaseInquirer>();

	public SeventyEight( Database db ) {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		instance = this;
		initialize( new File( System.getProperty( "user.home" ), ".seventyeight" ), db );
	}

	public SeventyEight( String path, Database db ) {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		instance = this;
		initialize( new File( path ), db );
	}
	
	public SeventyEight( File path, Database db ) {
		if( instance != null ) {
			throw new IllegalStateException( "Instance already defined" );
		}
		instance = this;
		initialize( path, db );
	}

	private SeventyEight initialize( File path, Database db ) {
		logger.debug( "Path: " + path.getAbsolutePath() );
		
		/* Prepare environment */
		logger.info( "Creating paths" );
		this.path = path;
		pluginsPath = new File( path, "plugins" );
		pluginsPath.mkdir();
		uploadPath = new File( path, "upload" );
		uploadPath.mkdir();

		/* Class loader */
		classLoader = new org.seventyeight.loader.ClassLoader( Thread.currentThread().getContextClassLoader() );

		/* Get the system node */
		db.containsKey( SYSTEM_NODE_TYPE );
		if( db.containsKey( SYSTEM_NODE_TYPE ) ) {
			systemNode = (Node) db.getValue( SYSTEM_NODE_TYPE );
		} else {
			logger.info( "System node not found, installing" );
			install( db );
		}
		
		/* Settings */
		defaultLocale = new Locale( "danish" );
		
		return this;
	}

	public static SeventyEight getInstance() {
		return instance;
	}
	
	public void shutdown() {
		//graphdb.close();
        System.out.println( "Shutting down" );
	}
	
	public AbstractResource getResource( Database db, Long id ) throws NotFoundException, TooManyException, CouldNotLoadResourceException {
        List<Node> nodes = db.getFromIndex( INDEX_RESOURCES, id );

        if( nodes.size() < 1 ) {
            throw new NotFoundException( "Resource with id " + id + " not found" );
        } else if( nodes.size() > 1 ) {
            throw new TooManyException( "Too many resources with id " + id + " found" );
        } else {
            Node node = nodes.get( 0 );

            if( node != null ) {
                try {
                    return (AbstractResource) getDatabaseItem( node );
                } catch( CouldNotLoadObjectException e ) {
                    logger.warn( "Unable to load resource object " + id );
                    throw new CouldNotLoadResourceException( "Unable to get resource", e );
                }
            } else {
                throw new CouldNotLoadResourceException( id + "" );
            }
        }
	}

    public AbstractResource setIdentifier( AbstractResource resource ) {
        logger.debug( "Setting identifier for resource" );
        Long id = getNextResourceIdentifier( resource.getNode().getDB() );
        resource.getNode().set( "identifier", id );

        return resource;
    }

    private synchronized long getNextResourceIdentifier( Database db ) {
        //Integer next = (Integer) mainNode.getProperty( "next-resource-id", 1 );
        Long next = (Long) db.getValue( "next-resource-id", 1 );
        //mainNode.setProperty( "next-resource-id", ( next + 1 ) );
        db.storeKeyValue( "next-resource-id", ( next + 1 ) );
        return next;
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
	 * @param
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
	
	private void install( Database graphdb ) {
		logger.info( "Installing system" );
		systemNode = graphdb.createNode();
        graphdb.storeKeyValue( SYSTEM_NODE_TYPE, systemNode );
		systemNode.save();

        /* Create index */
        graphdb.createIndex( INDEX_RESOURCES, IndexType.UNIQUE, IndexValueType.LONG );
	}

	public ODocument createNode( OGraphDatabase graphdb, Class<?> clazz, NodeType type ) {
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

    /**
     * Return a {@link DatabaseItem} given a node
     * @param node
     * @return
     * @throws CouldNotLoadObjectException
     */
	public DatabaseItem getDatabaseItem( Node node ) throws CouldNotLoadObjectException {
		String clazz = (String) node.get( "class" );

		if( clazz == null ) {
			logger.warn( "Class property not found" );
			throw new CouldNotLoadObjectException( "Class property not found: " + node );
		}
        logger.debug( "Item class: " + clazz );
		
		try {
			Class<Item> eclass = (Class<Item>) Class.forName(clazz, true, classLoader );
			Constructor<?> c = eclass.getConstructor( ODocument.class );
            DatabaseItem instance = (DatabaseItem) c.newInstance( node );
			return instance;
		} catch( Exception e ) {
			logger.error( "Unable to get the class " + clazz );
			throw new CouldNotLoadObjectException( "Unable to get the class " + clazz, e );
		}
	}

    public <T extends DatabaseInquirer> T getDatabaseInquirer( Class c ) throws NotFoundException {
        if( dbinq.containsKey( c ) ){
            return (T) dbinq.get( c );
        } else {
            throw new NotFoundException( "The database inquirer " + c + " does not exist" );
        }
    }


    /**
     * Get the list of {@link AbstractExtension}s from an {@link AbstractItem}, that satisfies a given extension class
     * @param item - The item in question
     * @param oftype - The extensions must be of this class
     * @return A list of extensions
     */
    public List<AbstractExtension> getExtensions( OGraphDatabase graphdb, AbstractItem item, Class<?> oftype ) {
        logger.debug( "Getting extensions for " + item + " of type " + oftype );
        //Iterator<Relationship> it = item.getNode().getRelationships( ExtensionRelations.EXTENSION, Direction.OUTGOING ).iterator();
        //List<Edge> edges = getEdges2( graphdb, item, ResourceEdgeType.extension );
        List<Edge> edges = item.getNode().getEdges( ResourceEdgeType.extension, Direction.OUTBOUND );

        List<AbstractExtension> extensions = new ArrayList<AbstractExtension>();

        for( Edge edge : edges  ) {
            Node node = edge.getTargetNode();
            String c = (String) node.get( "class" );

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
