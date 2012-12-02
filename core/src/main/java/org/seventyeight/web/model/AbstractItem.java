package org.seventyeight.web.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.IllegalStateRuntimeException;
import org.seventyeight.web.exceptions.InconsistentParameterException;
import org.seventyeight.web.exceptions.IncorrectTypeException;
import org.seventyeight.web.exceptions.ParameterDoesNotExistException;
import org.seventyeight.web.exceptions.ResourceDoesNotExistException;

import com.google.gson.JsonObject;


public abstract class AbstractItem implements Item, DatabaseItem<AbstractItem> {

	private static Logger logger = Logger.getLogger( AbstractItem.class );
	protected Node node;
	
	//protected Long identifier;
	protected Locale locale;
		
	public AbstractItem( Node node ) {
		this.node = node;
		this.locale = SeventyEight.getInstance().getDefaultLocale();
		//this.identifier = (Long) node.getProperty( "identifier" );
	}
	
	public AbstractItem( Node node, Locale locale ) {
		this.node = node;
		this.locale = locale;
		//this.identifier = (Long) node.getProperty( "identifier" );
	}
	
	protected final void doSave( Save save ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		logger.debug( "Begin saving" );
		
		save.before();
		save.save();
		logger.debug( "Handling extensions" );
		save.handleExtensions();
		logger.debug( "Extensions handled" );
		save.after();
		save.updateIndexes();
		
		node.save();
		
		logger.debug( "End saving" );
	}

	protected abstract class Save {

		protected AbstractItem item;
		protected ParameterRequest request;
		protected JsonObject jsonData;

		public Save( AbstractItem type, ParameterRequest request, JsonObject jsonData ) {
			this.item = type;
			this.request = request;
			this.jsonData = jsonData;
		}
		
		public void before() {}
		
		public abstract void save() throws InconsistentParameterException, ErrorWhileSavingException;
		
		public void after() {}
		
		public void updateIndexes() {}
		
		public ParameterRequest getRequest() {
			return request;
		}
		
		public JsonObject getJsonData() {
			return jsonData;
		}
		
		public void handleExtensions() {
			logger.debug( "Handling extensions" );
			if( jsonData != null ) {
				List<JsonObject> objects = SeventyEight.getInstance().getConfigurationJsonObjects( jsonData );
				logger.debug( "I got " + objects.size() + " configurations" );
				
				for( JsonObject o : objects ) {
					logger.debug( "o: " + o );
					try {
						String cls = o.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
						logger.debug( "Class is " + cls );
						Class<?> clazz = Class.forName( cls );
						logger.debug( "Class is " + clazz );
						Descriptor<?> d = SeventyEight.getInstance().getDescriptor( clazz );
						logger.debug( "Descroiptor is " + d );
						//List<ODocument> nodes = SeventyEight.getInstance().getNodeRelation( item, ResourceEdgeType.extension );
                        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND );
						
						logger.debug( "Extension nodes: " + edges.size() );
						if( edges.size() > 0 ) {
							logger.debug( "There were extensions defined" );
							//for() {
								
							//}
						} else {
							logger.debug( "There were NO extensions defined" );
							Configurable e = d.newInstance( getDB() );
							logger.debug( "Saving configurable " + e );
							e.save( request, o );
							logger.debug( "Configurable saved" );
							//SeventyEight.getInstance().addNodeRelation( db, item, e, ResourceEdgeType.extension, false );
                            node.createEdge( e.getNode(), ResourceEdgeType.extension );
						}
					} catch( Exception e ) {
						logger.warn( "Unable to get descriptor for " + o + ": " + e.getMessage() );
						//ExceptionUtils.getRootCause( e ).printStackTrace();
					}
				}
			} else {
				logger.debug( "Json data was null. Skipping" );
			}
		}
	}
	
	public Object getProperty( String name ) {
		return node.get( name );
	}
	
	public void setProperty( String name, Object property ) {
		logger.debug( "Setting " + name + " to " + property );
		node.set( name, property );
	}
			
	public Long getIdentifier() {
		return (Long) node.get( "identifier" );
	}

	public void setIdentifier( Long identifier ) {
		logger.debug( "Setting id to " + identifier );
		node.set( "identifier", identifier );
	}
		
	public Node getNode() {
		return node;
	}
	
	public void setLocale( Locale locale ) {
		this.locale = locale;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void updateIndexes() {
		/**/
	}

	
	public Map<String, List<Node>> getExtensionNodes() {
		//List<ODocument> ns = SeventyEight.getInstance().getNodes( db, this, ResourceEdgeType.extension );
        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND );
		
		Map<String, List<Node>> nodes = new HashMap<String, List<Node>>();
		
		for( Edge edge : edges ) {
            Node node = edge.getTargetNode();
			String clazz = (String) node.get( "class" );
			
			if( clazz != null ) {
				if( !nodes.containsKey( clazz ) ) {
					nodes.put( clazz, new ArrayList<Node>() );
				}
				
				nodes.get( clazz ).add( node );
			}
		}
		
		return nodes;
	}
	
	/*
	public String getConfigurableExtensions() {
		return getConfigurableExtensions( this.getClass(), this );
	}
	*/
	
	/**
	 * This used for preprocessing items before viewing.
	 */
	public void prepareView( ParameterRequest request ) {
		/* Default implementation is no op */
	}
	
	/*
	public static Class<?> getExtensionClass() {
		return this.getClass();
	}
	*/
	
	/*
	@SuppressWarnings( "unchecked" )
	public static String getConfigurableExtensions( Class<?> clazz, AbstractItem item ) {
		logger.debug( "I AM HERE " + clazz );
		List<Class<Extension>> list = (List<Class<Extension>>) SeventyEight.getInstance().getExtensions( clazz );
		logger.debug( "THE LIST IS " + list );
		
		Map<String, List<ODocument>> nodes = null;
		if( item != null ) {
			nodes = item.getExtensionNodes();
		} else {
			nodes = new HashMap<String, List<ODocument>>();
		}
		
		StringBuilder sb = new StringBuilder();
		
		Renderer render = SeventyEight.getInstance().getTemplateManager().getRenderer( new StringWriter() );
		
		for( Class<Extension> ext : list ) {
			logger.debug( "CLASS: " + ext.getCanonicalName() );
			
			if( nodes.containsKey( ext.getCanonicalName() ) ) {
				logger.debug( "Has nodes" );
				
				List<ODocument> ns = nodes.get( ext.getCanonicalName() );
				
				Constructor<?> c;
				try {
					c = ext.getConstructor( ODocument.class );
				} catch( Exception e ) {
					logger.warn( "Unable to get constructor for " + ext );
					continue;
				}
				
				for( ODocument n : ns ) {
					
					try {
						//sb.append( SeventyEight.getInstance().getTemplateManager().renderObject( new StringWriter(), ext, c.newInstance( n ), "configure.vm", SeventyEight.getInstance().getDefaultTheme(), new VelocityContext(), SeventyEight.getInstance().getDefaultLocale() ).toString() );
						render.renderObject( ext, c.newInstance( n ), "configure.vm", new VelocityContext() );
					} catch( Exception e1 ) {
						logger.warn( "Unable to append " + ext + "-node(" + n + "): " + e1.getMessage() );
						logger.warn( e1 );
					}
				}
				
			} else {
				logger.debug( "No configured nodes" );
				try {
					//sb.append( SeventyEight.getInstance().renderObject( new StringWriter(), ext, null, "configure.vm", SeventyEight.getInstance().getDefaultTheme(), new VelocityContext(), SeventyEight.getInstance().getDefaultLocale() ).toString() );
					render.renderObject( ext, null, "configure.vm", new VelocityContext() );
				} catch( TemplateDoesNotExistException e1 ) {
					logger.warn( "Unable to append " + ext + ": " + e1.getMessage() );
					logger.warn( e1 );
				}
			}
		}
		
		return sb.toString();
	}
	*/
	
	/**
	 * Get a field
	 * @param key
	 * @param def
	 * @return
	 */
	public <T> T getField( String key, T def ) {
		if( node.get( key ) == null ) {
			return def;
		} else {
			return (T) node.get( key );
		}
	}
	
	public <T> T getField( String key ) throws IllegalStateRuntimeException {
		if( node.get( key ) == null ) {
			throw new IllegalStateRuntimeException( "Field " + key + " does not exist" );
		} else {
			return (T) node.get( key );
		}
	}

	/*
	public void getConfigurableExtensions() {
		List<UserExtension> userExtensions = GraphDragon.getInstance().getExtensions( UserExtension.class );
		
		for( UserExtension e : userExtensions ) {
			try {
				GraphDragon.getInstance().render( e.getClass(), this, "configure", GraphDragon.getInstance().getDefaultTheme(), new VelocityContext() );
			} catch( TemplateDoesNotExistException e1 ) {
				logger.warn( "Unable to append " + e + ": " + e1.getMessage() );
			}
		}
	}
	*/

    @Override
    public AbstractItem createRelation( DatabaseItem<AbstractItem> other, EdgeType type ) {
        node.createEdge( other.getNode(), type );

        return this;
    }

    @Override
    public String getItemClass() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Database getDB() {
        return node.getDB();
    }
}
