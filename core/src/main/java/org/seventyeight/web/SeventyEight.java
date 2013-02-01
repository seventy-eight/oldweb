package org.seventyeight.web;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.loader.Loader;
import org.seventyeight.utils.FileUtilities;
import org.seventyeight.web.authentication.Authentication;
import org.seventyeight.web.authentication.SessionManager;
import org.seventyeight.web.authentication.SessionsHub;
import org.seventyeight.web.authentication.SimpleAuthentication;
import org.seventyeight.web.debate.treelike.ReplyTreeHub;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.extensions.Copyright.Copyright;
import org.seventyeight.web.extensions.debate.Debate;
import org.seventyeight.web.extensions.debate.simple.SimpleDebate;
import org.seventyeight.web.handler.Dictionary;
import org.seventyeight.web.handler.TemplateManager;
import org.seventyeight.web.handler.TopLevelGizmoHandler;
import org.seventyeight.web.hubs.AuthoritativeHub;
import org.seventyeight.web.hubs.OwnershipsHub;
import org.seventyeight.web.model.*;
import org.seventyeight.web.model.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.seventyeight.web.model.resources.*;
import org.seventyeight.web.model.themes.Default;
import org.seventyeight.utils.Date;
import org.seventyeight.web.util.Installer;
import org.seventyeight.web.util.ResourceDescriptorList;

public class SeventyEight {
	private static Logger logger = Logger.getLogger( SeventyEight.class );
	private static SeventyEight instance;

    public static final String TEMPLATE_PATH_NAME = "templates";
    public static final String THEMES_PATH_NAME = "themes";
    public static final String PLUGINS_PATH_NAME = "plugins";

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
     * A map of descriptors keyed by their super class
     */
    private Map<Class<?>, Descriptor<?>> descriptors = new HashMap<Class<?>, Descriptor<?>>();

    /**
     * A map of interfaces corresponding to specific {@link Descriptor}s<br />
     * This is used to map an extension class/interface to those {@link Describable}s {@link Descriptor}s implementing them.
     */
    private Map<Class, List<Descriptor>> descriptorList = new HashMap<Class, List<Descriptor>>();

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

    public static final String FIELD_EXTENSION_CLASS = "extensionClass";

	public enum ResourceEdgeType implements EdgeType {
        ownerships,
        owner,
		contributor,
		extension,
        extensions,
		translation,
        action,
        data
	}

    public enum ItemRelation implements EdgeType {
        scores
    }
		
	public enum GroupEdgeType implements EdgeType {
		readAccess,
		writeAccess
	}

    public enum AuthoritativeEdgeType implements EdgeType {
        authoritative,
        moderator,
        viewer
    }

    private TopLevelGizmoHandler topLevelActionHandler = new TopLevelGizmoHandler();

	/* Paths */
	private File path;
	private File orientdbPath;
	private File pluginsPath;
	private File uploadPath;
    private File themesPath;
	
	/* Locale */
	private Locale defaultLocale;
	
	private TemplateManager templateManager = new TemplateManager();
    private SessionManager sessionManager;
	private AbstractTheme defaultTheme = new Default();
	private I18N i18n;

	/**
	 * A map of resource types and their descriptors
	 */
	private Map<String, ResourceDescriptor<?>> resourceTypes = new HashMap<String, ResourceDescriptor<?>>();

    private ConcurrentMap<Class, DatabaseInquirer> dbinq = new ConcurrentHashMap<Class, DatabaseInquirer>();

    private ConcurrentMap<String, AbstractTheme> themes = new ConcurrentHashMap<String, AbstractTheme>();

    private Authentication authentication = new SimpleAuthentication();

    /**
     * A {@link Map} of top level actions, given by its name
     */
    private ConcurrentMap<String, TopLevelGizmo> topLevelGizmos = new ConcurrentHashMap<String, TopLevelGizmo>();

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

        themesPath = new File( path, "themes" );
        themesPath.mkdir();

		/* Class loader */
		classLoader = new org.seventyeight.loader.ClassLoader( Thread.currentThread().getContextClassLoader() );
        this.pluginLoader = new Loader( classLoader );

        /* Add core types */
        addDescriptor( db, new User.UserDescriptor() );
        addDescriptor( db, new Group.GroupDescriptor() );
        addDescriptor( db, new Article.ArticleDescriptor() );
        addDescriptor( db, new FileResource.FileDescriptor() );
        addDescriptor( db, new Image.ImageDescriptor() );

        //addDescriptor( db, new ReplyTreeHub.ReplyTreeExtensionDescriptor() );
        //addDescriptor( db, new TreeReply.ReplyDescriptor() );

        addDescriptor( db, new Debate.DebateDescriptor() );
        //addDescriptor( db, new Debate2.DebateDescriptor() );

        addDescriptor( db, new SimpleDebate.SimpleDebateDescriptor() );
        addDescriptor( db, new Copyright.CopyrightDescriptor() );

        addDescriptor( db, new AuthoritativeHub.AuthoritativeHubDescriptor() );
        addDescriptor( db, new OwnershipsHub.OwnershipsHubDescriptor() );
        addDescriptor( db, new SessionsHub.SessionsHubDescriptor() );

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
     * @param basePath
     * @return
     * @throws IOException
     */
    public static List<File> extractPlugins( File basePath ) throws IOException {
        logger.debug( "Extracting plugins to " + PLUGINS_PATH_NAME );
        File path = new File( basePath, PLUGINS_PATH_NAME );
        File themes = new File( basePath, THEMES_PATH_NAME );

        FileUtils.deleteDirectory( themes );
        themes.mkdir();

        File[] files = path.listFiles( FileUtilities.getExtension( "jar" ) );

        List<File> plugins = new ArrayList<File>();
        for( File f : files ) {
            logger.debug( "Extracting plugin " + f );
            String p = f.getName();
            p = p.substring( 0, ( p.length() - 4 ) );
            File op = new File( path, p );
            FileUtils.deleteDirectory( op );

            FileUtilities.extractArchive( f, op );
            plugins.add( op );

            /* Copy any theme directory to path */
            File pthemes = new File( op, "themes" );
            if( pthemes.exists() ) {
                logger.debug( "Copying themes from " + pthemes + " to " + themes );
                FileUtils.copyDirectory( pthemes, themes );
            }
        }

        return plugins;
    }

    public File getThemeFile( AbstractTheme theme, String filename ) throws IOException {
        File themePath = new File( themesPath, theme.getName() );
        File themeFile = new File( themePath, filename );

        if( themeFile.exists() ) {
            return themeFile;
        }

        throw new IOException( "Theme file " + themeFile + " does not exist" );
    }

    public org.seventyeight.loader.ClassLoader getClassLoader() {
        return classLoader;
    }

    public void getPlugins( List<File> plugins ) {
        for( File p : plugins ) {
            logger.debug( "Plugin " + p );
            try {
                /* Maybe check for classes directory */
                pluginLoader.load( p, "" );
            } catch( Exception e ) {
                logger.error( "Unable to load " + p );
                logger.error( e );
            }
        }
    }

    public Node createNode( Database db, Class clazz ) {
        Node node = db.createNode();
        if( clazz != null ) {
            node.set( "class", clazz.getName() ).save();
        }

        return node;
    }

    public <T extends AbstractDatabaseItem> T createItem( Database db,  Class<?> clazz ) throws UnableToInstantiateObjectException {
        Node node = createNode( db, clazz );

        try {
            Constructor c = clazz.getConstructor( Node.class );
            T instance = (T) c.newInstance( node );
            instance.getNode().save();
            return instance;
        } catch( Exception e ) {
            throw new UnableToInstantiateObjectException( clazz.getName(), e );
        }
    }

    public Node createNode( Database db, Class clazz, String[] keys, Object[] values ) {
        Node node = db.createNode();
        if( clazz != null ) {
            node.set( "class", clazz.getName() ).save();
        }

        if( keys != null ) {
            for( int i = 0 ; i < keys.length ; i++ ) {
                node.set( keys[i], values[i] );
            }
        }

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
	
	public AbstractResource getResource( Database db, Long id ) throws NotFoundException, TooManyException, CouldNotLoadItemException {
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
                    throw new CouldNotLoadItemException( "Unable to get resource", e );
                }
            } else {
                throw new CouldNotLoadItemException( id + "" );
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

    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * Add a {@link Descriptor}. If it is a {@link ResourceDescriptor}, the type is added too.
     * @param descriptor
     */
    public void addDescriptor( Database db, Descriptor<?> descriptor ) {
        this.descriptors.put( descriptor.getClazz(), descriptor );

        if( descriptor instanceof ResourceDescriptor ) {
            this.resourceTypes.put( ((ResourceDescriptor)descriptor).getType(), (ResourceDescriptor<?>) descriptor );
        }

        addExtension( descriptor );

        /**/
        descriptor.configureIndex( db );
    }
	
	public <T extends Descriptor> T getDescriptor( Class<?> clazz ) {
		logger.debug( "Getting descriptor for " + clazz );
		if( descriptors.containsKey( clazz ) ) {
			return (T) descriptors.get( clazz );
		} else {
			return null;
		}
	}

    public Descriptor<?> getDescriptor( String className ) throws ClassNotFoundException {
        return getDescriptor( Class.forName( className ) );
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

    public Descriptor<?> getResourceDescriptor( String clazz ) throws ClassNotFoundException {
        return getResourceDescriptor( Class.forName( clazz ) );
    }

    public Descriptor<?> getResourceDescriptor( Class<?> clazz ) {
        return descriptors.get( clazz );
    }

    public Collection<ResourceDescriptor<?>> getResourceDescriptors() {
        return new ResourceDescriptorList( resourceTypes.values() );
    }

    public void addExtension( Extension extension ) {

    }

    public void addExtension( Descriptor<?> descriptor ) {
        logger.debug( "Adding extension descriptor " + descriptor.getClazz() );

        List<Class<?>> interfaces = getInterfaces( descriptor.getClazz() );
        for( Class<?> i : interfaces ) {
            logger.debug( "INTERFACE: " + i );
            List<Descriptor> list = null;
            if( !descriptorList.containsKey( i ) ) {
                descriptorList.put( i, new ArrayList<Descriptor>() );
            }
            list = descriptorList.get( i );

            list.add( descriptor );
        }
    }

    private void getViewsFromClass( Class<?> clazz ) {
        String path = TemplateManager.getUrlFromClass( clazz );
    }

    public List<Class<?>> getInterfaces( Class<?> clazz ) {
        List<Class<?>> interfaces = new ArrayList<Class<?>>();
        interfaces.addAll( Arrays.asList( clazz.getInterfaces() ) );

        Class<?> s = clazz.getSuperclass();
        if( s != null ) {
            interfaces.addAll( getInterfaces( s ) );
        }

        return interfaces;
    }

    public List<Descriptor> getExtensionDescriptors( String clazz ) throws ClassNotFoundException {
        return getExtensionDescriptors( Class.forName( clazz ) );
    }

    public List<Descriptor> getExtensionDescriptors( Class clazz ) {
        if( descriptorList.containsKey( clazz ) ) {
            return descriptorList.get( clazz );
        } else {
            return Collections.emptyList();
        }
    }

    public Map<Class, List<Descriptor>> getAllDescriptors() {
        return descriptorList;
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
	public <T extends DatabaseItem> T getDatabaseItem( Node node ) throws CouldNotLoadObjectException {
		String clazz = (String) node.get( "class" );

		if( clazz == null ) {
			logger.warn( "Class property not found" );
			throw new CouldNotLoadObjectException( "\"class\" property not found for " + node );
		}
        logger.debug( "Item class: " + clazz );

		try {
			Class<Item> eclass = (Class<Item>) Class.forName(clazz, true, classLoader );
			Constructor<?> c = eclass.getConstructor( Node.class );
            return (T) c.newInstance( node );
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

    public void addTopLevelGizmo( TopLevelGizmo handler ) {
        logger.debug( "Adding " + handler.getUrlName() + " to action handlers" );
        topLevelGizmos.put( handler.getUrlName(), handler );
    }

    public TopLevelGizmo getTopLevelGizmo( String name ) throws GizmoHandlerDoesNotExistException {
        if( topLevelGizmos.containsKey( name ) ) {
            return topLevelGizmos.get( name );
        } else {
            throw new GizmoHandlerDoesNotExistException( "The GIZMO handler " + name + " does not exist" );
        }
    }

    public TopLevelGizmoHandler getTopLevelActionHandler() {
        return topLevelActionHandler;
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

    public File getThemesPath() {
        return themesPath;
    }

    public void setThemesPath( File path ) {
        this.themesPath = path;
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
    //public static final String __JSON_EXTENSION_NAME = "extensions";
	public static final String __JSON_CLASS_NAME = "class";

    public enum JsonType {
        config,
        extensionClass
    }

    public List<JsonObject> getJsonObjects( JsonObject obj ) {
        return getJsonObjects( obj, JsonType.config );
    }
	
	public List<JsonObject> getJsonObjects( JsonObject obj, JsonType type ) {
        logger.debug( "Getting " + type + " Json objects" );

		List<JsonObject> objects = new ArrayList<JsonObject>();
		
		JsonElement configElement = obj.get( type.toString() );
		
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

    /**********************/
    /*  TEMPORARY STUFF   */
    /**********************/

    public Class<? extends AbstractHub> getReplyHubType() {
        return ReplyTreeHub.class;
    }


}
