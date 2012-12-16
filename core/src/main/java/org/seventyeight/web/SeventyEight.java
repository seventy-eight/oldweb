package org.seventyeight.web;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.loader.Loader;
import org.seventyeight.utils.FileUtilities;
import org.seventyeight.web.authentication.SessionManager;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.handler.Dictionary;
import org.seventyeight.web.handler.TemplateManager;
import org.seventyeight.web.model.*;
import org.seventyeight.web.model.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.seventyeight.web.model.resources.Article;
import org.seventyeight.web.model.resources.FileResource;
import org.seventyeight.web.model.resources.Group;
import org.seventyeight.web.model.resources.User;
import org.seventyeight.web.model.themes.Default;
import org.seventyeight.utils.Date;
import org.seventyeight.web.util.Installer;

public class SeventyEight {
	private static Logger logger = Logger.getLogger( SeventyEight.class );
	private static SeventyEight instance;

    /**
     * This index contains all the identifiers for resources
     */
    public static final String INDEX_RESOURCES = "resource-identifiers";

    /**
     * This index contains all the resource types, including the date created
     */
    public static final String INDEX_RESOURCE_TYPES = "resource-types";

    public static final String INDEX_SYSTEM_USERS = "system-users";

    public static final String INDEX_FILES = "files";

	
	public static final String defaultThemeName = "default";
	
	private org.seventyeight.loader.ClassLoader classLoader = null;
    private Loader pluginLoader;

	private Node systemNode = null;
    private User anonymous;

    /**
     * The {@link Dictionary}
     */
    private Dictionary dictionary = new Dictionary();

    public enum NodeType {
		item,
		/*resource,*/
		widgit,
		/*system*/
		text,
		
		unknown
	}
	
	private static final String SYSTEM_NODE = "system-node";
    private static final String SYSTEM_INSTALLED = "system-installed";
	
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
	
	private TemplateManager templateManager = new TemplateManager();
    private SessionManager sessionManager;
	private AbstractTheme defaultTheme = new Default();
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

    private ConcurrentMap<String, AbstractTheme> themes = new ConcurrentHashMap<String, AbstractTheme>();

    /**
     * A {@link Map} of top level actions, given by its name
     */
    private ConcurrentMap<String, TopLevelAction> topLevelActions = new ConcurrentHashMap<String, TopLevelAction>();

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
        this.pluginLoader = new Loader( classLoader );

        /* Add core types */
        addDescriptor( db, new User.UserDescriptor() );
        addDescriptor( db, new Group.GroupDescriptor() );
        addDescriptor( db, new Article.ArticleDescriptor() );
        addDescriptor( db, new FileResource.FileDescriptor() );

        defaultLocale = new Locale( "english" );


        /* Get the system node */
		if( db.containsNode( SYSTEM_NODE ) ) {
			systemNode = db.getNode( SYSTEM_NODE );
		} else {
			logger.info( "System node not found, installing" );
			//install( db );
            Installer installer = new Installer( db );
            try {
                systemNode = db.createNode();
                db.keepNode( SYSTEM_NODE, systemNode );
                installer.install();
            } catch( Exception e ) {
                throw new IllegalStateException( "Unable to install", e );
            }
        }

        /**/
        logger.debug( "Setting anonymous user" );
        List<Node> nodes = db.getFromIndex( INDEX_SYSTEM_USERS, "anonymous" );
        if( nodes.size() != 1 ) {
            throw new IllegalStateException( "Not one anonymous user found; " + nodes.size() );
        }
        anonymous = new User( nodes.get( 0 ) );
		
		/* Settings */
		defaultLocale = new Locale( "danish" );

        /* Configure indexes for descriptors */

        /* Themes */
        themes.put( "default", defaultTheme );

        sessionManager = new SessionManager( db );

        db.close();

		return this;
	}

	public static SeventyEight getInstance() {
		return instance;
	}
	
	public void shutdown() {
		//graphdb.close();
        System.out.println( "Shutting down" );
	}

    /**
     * From the given path, get all jars and extract them to their directories
     * @param path
     * @return
     * @throws IOException
     */
    public static List<File> extractPlugins( File path ) throws IOException {
        logger.debug( "Extracting plugins to " + path );

        File[] files = path.listFiles( FileUtilities.getExtension( "jar" ) );

        List<File> plugins = new ArrayList<File>();
        for( File f : files ) {
            String p = f.getName();
            p = p.substring( 0, ( p.length() - 4 ) );
            logger.debug( "f: " + f );
            logger.debug( "f: " + p );
            File op = new File( path, p );
            FileUtils.deleteDirectory( op );

            FileUtilities.extractArchive( f, op );
            plugins.add( op );
        }

        return plugins;
    }

    public org.seventyeight.loader.ClassLoader getClassLoader() {
        return classLoader;
    }

    public void getPlugins( List<File> plugins ) {
        for( File p : plugins ) {
            logger.debug( "Plugin " + p );
            try {
                pluginLoader.load( p, "" );
            } catch( Exception e ) {
                logger.error( "Unable to load " + p );
                logger.error( e );
            }
        }
    }

    public Node createNode( Database db, Class clazz ) {
        Node node = db.createNode();
        node.set( "class", clazz.getName() ).save();

        return node;
    }

    public AbstractResource createResource( Database db, String type ) throws UnableToInstantiateObjectException {
        ResourceDescriptor f = resourceTypes.get( type );
        logger.debug( "Creating new resource of type " + type );

        Node node = createNode( db, f.getClazz() );
        long id = getNextResourceIdentifier( db );

        node.set( "identifier", id );
        node.set( "created", new Date().getTime() );

        AbstractResource instance = f.newInstance( db );

        return instance;
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

    private Node getSystemNode( Database db ) {
        return db.getNode( SYSTEM_NODE );
    }

    private synchronized long getNextResourceIdentifier( Database db ) {
        //Integer next = (Integer) mainNode.getProperty( "next-resource-id", 1 );
        Node s = getSystemNode( db );
        //Long next = (Long) db.getValue( "next-resource-id", 1l );
        Long next = s.get( "next-resource-id", 1l );
        //mainNode.setProperty( "next-resource-id", ( next + 1 ) );
        //db.keepNode( "next-resource-id", ( next + 1 ) );
        s.set( "next-resource-id", ( next + 1 ) );
        s.save();
        return next;
    }

    /**
     * Add a {@link Descriptor}. If it is a {@link ResourceDescriptor}, the type is added too.
     * @param descriptor
     */
    public void addDescriptor( Database db, Descriptor<?> descriptor ) {
        this.descriptors.put( descriptor.getClazz(), descriptor );

        if( descriptor instanceof ResourceDescriptor ) {
            this.resourceTypes.put( descriptor.getType(), (ResourceDescriptor<?>) descriptor );
        }

        /**/
        descriptor.configureIndex( db );
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

    public AbstractTheme getTheme( String themeName ) {
        return defaultTheme;
    }

    public Collection<AbstractTheme> getAllThemes() {
        return themes.values();
    }

    public DatabaseItem getDatabaseItem( long id ) {
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
			throw new CouldNotLoadObjectException( "\"class\" property not found for " + node );
		}
        logger.debug( "Item class: " + clazz );

		try {
			Class<Item> eclass = (Class<Item>) Class.forName(clazz, true, classLoader );
			Constructor<?> c = eclass.getConstructor( Node.class );
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
    public List<AbstractExtension> getExtensions( AbstractItem item, Class<?> oftype ) {
        logger.debug( "Getting extensions for " + item + " of type " + oftype );
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

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void addTopLevelAction( String name, TopLevelAction handler ) {
        logger.debug( "Adding " + name + " to action handlers" );
        topLevelActions.put( name, handler );
    }

    public TopLevelAction getTopLevelAction( String name ) throws ActionHandlerDoesNotExistException {
        if( topLevelActions.containsKey( name ) ) {
            return topLevelActions.get( name );
        } else {
            throw new ActionHandlerDoesNotExistException( "The action handler " + name + " does not exist" );
        }
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
	
	public TemplateManager getTemplateManager() {
		return templateManager;
	}
	
	public I18N getI18N() {
		return i18n;
	}

    public File getPath() {
        return path;
    }

    public File getPluginsPath() {
        return pluginsPath;
    }

    public User getAnonymousUser() {
        return anonymous;
    }



    public Node createFile( Database db, String filename ) {
        logger.debug( "Creating new file " + filename );

        Node node = db.createNode();
        node.set( "file", filename );
        db.putToIndex( INDEX_FILES, node, filename );
        node.set( "created", new Date().getTime() );
        node.save();

        return node;

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
