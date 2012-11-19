package org.seventyeight.web.model;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.spi.IllegalStateException;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.EdgeType;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.IllegalStateRuntimeException;
import org.seventyeight.web.exceptions.InconsistentParameterException;
import org.seventyeight.web.exceptions.IncorrectTypeException;
import org.seventyeight.web.exceptions.ParameterDoesNotExistException;
import org.seventyeight.web.exceptions.ResourceDoesNotExistException;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.handler.Renderer.Render;

import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;


public abstract class AbstractItem implements Item {

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
						List<ODocument> nodes = SeventyEight.getInstance().getNodeRelation( item, ResourceEdgeType.extension );
						
						logger.debug( "Extension nodes: " + nodes.size() );
						if( nodes.size() > 0 ) {
							logger.debug( "There was extensions defined" );
							//for() {
								
							//}
						} else {
							logger.debug( "There were NO extensions defined" );
							Configurable e = d.newInstance();
							logger.debug( "Saving configurable " + e );
							e.save( request, o );
							logger.debug( "Configurable saved" );
							SeventyEight.getInstance().addNodeRelation( db, item, e, ResourceEdgeType.extension, false );
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

	
	public Map<String, List<ODocument>> getExtensionNodes() {
		List<ODocument> ns = SeventyEight.getInstance().getNodes( db, this, ResourceEdgeType.extension );
		
		Map<String, List<ODocument>> nodes = new HashMap<String, List<ODocument>>();
		
		for( ODocument node : ns ) {
			String clazz = node.field( "class" );
			
			if( clazz != null ) {
				if( !nodes.containsKey( clazz ) ) {
					nodes.put( clazz, new ArrayList<ODocument>() );
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
		
		Render render = SeventyEight.getInstance().getRenderer().getRender( new StringWriter() );
		
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
						//sb.append( SeventyEight.getInstance().getRenderer().renderObject( new StringWriter(), ext, c.newInstance( n ), "configure.vm", SeventyEight.getInstance().getDefaultTheme(), new VelocityContext(), SeventyEight.getInstance().getDefaultLocale() ).toString() );
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
			return node.get( key );
		}
	}
	
	public <T> T getField( String key ) throws IllegalStateRuntimeException {
		if( node.get( key ) == null ) {
			throw new IllegalStateRuntimeException( "Field " + key + " does not exist" );
		} else {
			return node.get( key );
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
	
	
}
