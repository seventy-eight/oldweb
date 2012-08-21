package org.seventyeight.web.model;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.seventyeight.GraphDragon;
import org.seventyeight.model.AbstractObject.ExtensionRelations;
import org.seventyeight.model.extensionpoints.CreateListener;
import org.seventyeight.model.extensionpoints.SaveListener;
import org.seventyeight.model.extensionpoints.ViewListener;
import org.seventyeight.util.Date;
import org.seventyeight.util.ExceptionUtils;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.InconsistentParameterException;
import org.seventyeight.web.exceptions.IncorrectTypeException;
import org.seventyeight.web.exceptions.ParameterDoesNotExistException;
import org.seventyeight.web.exceptions.ResourceDoesNotExistException;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;

import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;


public abstract class AbstractItem implements Item {

	private static Logger logger = Logger.getLogger( AbstractItem.class );
	protected ODocument node;
	
	//protected Long identifier;
	protected Locale locale;
		
	public AbstractItem( ODocument node ) {
		this.node = node;
		this.locale = SeventyEight.getInstance().getDefaultLocale();
		//this.identifier = (Long) node.getProperty( "identifier" );
	}
	
	public AbstractItem( ODocument node, Locale locale ) {
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
		
		logger.debug( "End saving" );
	}

	protected abstract class Save {

		protected AbstractItem item;
		protected Request request;
		protected JsonObject jsonData;

		public Save( AbstractItem type, Request request, JsonObject jsonData ) {
			this.item = type;
			this.request = request;
			this.jsonData = jsonData;
		}
		
		public void before() {}
		
		public abstract void save() throws InconsistentParameterException, ErrorWhileSavingException;
		
		public void after() {}
		
		public void updateIndexes() {}
		
		public Request getRequest() {
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
						List<ODocument> nodes = SeventyEight.getInstance().getNodeRelation( getNode(), ExtensionRelations.EXTENSION, "class", cls );
						
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
							GraphDragon.getInstance().addNodeRelation( getNode(), e.getNode(), ExtensionRelations.EXTENSION, false );
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
		return node.getProperty( name );
	}
	
	public void setProperty( String name, Object property ) {
		logger.debug( "Setting " + name + " to " + property );
		node.setProperty( name, property );
	}
	
	public void setProperties( Map<String, Object> properties ) {
		Set<String> names = properties.keySet();
		
		for( String name : names ) {
			node.setProperty( name, properties.get( name ) );
		}
	}
		
	public Long getIdentifier() {
		return (Long) node.getProperty( "identifier" );
	}

	public void setIdentifier( Long identifier ) {
		logger.debug( "Setting id to " + identifier );
		node.setProperty( "identifier", identifier );
	}
	
	public abstract String getDisplayName();
		
	public Node getNode() {
		return node;
	}
	
	public void setLocale( Locale locale ) {
		this.locale = locale;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void updateIndexes( Index<Node> idx ) {
		logger.debug( "Removing index(" + idx.getName() + ") for " + this );
		idx.remove( getNode() );
		
		//idx.add( getNode(), "identifier", new ValueContext( identifier ).indexNumeric() );
		idx.add( getNode(), "identifier", getIdentifier() );
		//idx.add( getNode(), "type", getType() );
	}

	
	public Map<String, List<Node>> getExtensionNodes() {
		Iterator<Relationship> it = node.getRelationships( Direction.OUTGOING, ExtensionRelations.EXTENSION ).iterator();
		
		Map<String, List<Node>> nodes = new HashMap<String, List<Node>>();
		
		while( it.hasNext() ) {
			Node node = it.next().getEndNode();
			String clazz = (String) node.getProperty( "class", "" );
			
			if( !nodes.containsKey( clazz ) ) {
				nodes.put( clazz, new ArrayList<Node>() );
			}
			
			nodes.get( clazz ).add( node );
		}
		
		return nodes;
	}
	
	public String getConfigurableExtensions() {
		return getConfigurableExtensions( this.getClass(), this );
	}
	
	/**
	 * This used for preprocessing items before viewing.
	 */
	public void prepareView( RequestContext request ) {
		/* Default implementation is no op */
	}
	
	/*
	public static Class<?> getExtensionClass() {
		return this.getClass();
	}
	*/
	
	@SuppressWarnings( "unchecked" )
	public static String getConfigurableExtensions( Class<?> clazz, AbstractItem item ) {
		logger.debug( "I AM HERE " + clazz );
		List<Class<Extension>> list = (List<Class<Extension>>) GraphDragon.getInstance().getExtensions( clazz );
		logger.debug( "THE LIST IS " + list );
		
		Map<String, List<Node>> nodes = null;
		if( item != null ) {
			nodes = item.getExtensionNodes();
		} else {
			nodes = new HashMap<String, List<Node>>();
		}
		
		StringBuilder sb = new StringBuilder();
		
		for( Class<Extension> ext : list ) {
			logger.debug( "CLASS: " + ext.getCanonicalName() );
			
			if( nodes.containsKey( ext.getCanonicalName() ) ) {
				logger.debug( "Has nodes" );
				
				List<Node> ns = nodes.get( ext.getCanonicalName() );
				
				Constructor<?> c;
				try {
					c = ext.getConstructor( Node.class );
				} catch( Exception e ) {
					logger.warn( "Unable to get constructor for " + ext );
					continue;
				}
				
				for( Node n : ns ) {
					
					try {
						sb.append( GraphDragon.getInstance().renderObject( new StringWriter(), ext, c.newInstance( n ), "configure.vm", GraphDragon.getInstance().getDefaultTheme(), new VelocityContext() ).toString() );
					} catch( Exception e1 ) {
						logger.warn( "Unable to append " + ext + "-node(" + n + "): " + e1.getMessage() );
						logger.warn( e1 );
					}
				}
				
			} else {
				logger.debug( "No configured nodes" );
				try {
					sb.append( GraphDragon.getInstance().renderObject( new StringWriter(), ext, null, "configure.vm", GraphDragon.getInstance().getDefaultTheme(), new VelocityContext() ).toString() );
				} catch( TemplateDoesNotExistException e1 ) {
					logger.warn( "Unable to append " + ext + ": " + e1.getMessage() );
					logger.warn( e1 );
				}
			}
		}
		
		return sb.toString();
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
