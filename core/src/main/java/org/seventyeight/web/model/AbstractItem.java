package org.seventyeight.web.model;

import java.util.*;

import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.*;

import com.google.gson.JsonObject;


public abstract class AbstractItem extends AbstractDatabaseItem implements Item {

	private static Logger logger = Logger.getLogger( AbstractItem.class );

	public AbstractItem( Node node ) {
		super( node );
	}
	
	public AbstractItem( Node node, Locale locale ) {
        super( node );
	}
	
	protected final void save( Save save ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
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
				List<JsonObject> objects = SeventyEight.getInstance().getJsonObjects( jsonData );
				logger.debug( "I got " + objects.size() + " configurations" );
				
				for( JsonObject o : objects ) {
					logger.debug( "o: " + o );
					try {
						String cls = o.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
						logger.debug( "Class is " + cls );
						Class<?> clazz = Class.forName( cls );
						logger.debug( "Class is " + clazz );
						Descriptor<?> d = SeventyEight.getInstance().getDescriptor( clazz );
						logger.debug( "Descriptor is " + d );
						//List<ODocument> nodes = SeventyEight.getInstance().getNodeRelation( item, ResourceEdgeType.extension );

                        AbstractExtensionHub hub = getExtensionHub( d );

                        /* First remove the extensions */
                        hub.removeExtensions();

                        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND );
						
						logger.debug( "Extension nodes: " + edges.size() );
						if( edges.size() > 0 ) {
							logger.debug( "There were extensions defined" );
							//for() {
								
							//}
						} else {
							logger.debug( "There were NO extensions defined" );
							Describable e = d.newInstance( getDB() );
							logger.debug( "Saving configurable " + e );
							e.doSave( request, o );
							logger.debug( "Describable saved" );

                            //Hub hub = e.getHub();



							//SeventyEight.getInstance().addNodeRelation( db, item, e, ResourceEdgeType.extension, false );
                            node.createEdge( e.getNode(), d.getRelationType() );
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
	
	public void updateIndexes() {
		/**/
	}

    public List<Node> getExtensionNodes( Class<?> extensionClass ) {
        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND );

        List<Node> nodes = new LinkedList<Node>();

        for( Edge edge : edges ) {
            Node node = edge.getTargetNode();
            String clazzStr = (String) node.get( "class" );
            try {
                Class clazz = Class.forName( clazzStr );
                clazz.equals( extensionClass );
                nodes.add( node );
            } catch( ClassNotFoundException e ) {
                logger.warn( e );
            }
        }

        return nodes;
    }
	
	public Map<Class, List<Node>> getExtensionNodes() {
		//List<ODocument> ns = SeventyEight.getInstance().getNodes( db, this, ResourceEdgeType.extension );
        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND );
		
		Map<Class, List<Node>> nodes = new HashMap<Class, List<Node>>();
		
		for( Edge edge : edges ) {
            Node node = edge.getTargetNode();
			String clazzStr = (String) node.get( "class" );
            try {
                Class clazz = Class.forName( clazzStr );
                if( clazz != null ) {
                    if( !nodes.containsKey( clazz ) ) {
                        nodes.put( clazz, new ArrayList<Node>() );
                    }

                    nodes.get( clazz ).add( node );
                }
            } catch( ClassNotFoundException e ) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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


    public void handleJsonExtensionClass( Request request, JsonObject extensionClassData ) throws NoSuchExtensionException {
        logger.debug( "Handling extension class Json data" );

        String className = extensionClassData.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
        logger.debug( "Class is " + className );

        AbstractExtensionHub hub = null;

        try {
            Class<?> clazz = Class.forName( className );
            logger.debug( "Extension class is " + clazz );
            List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, clazz.getName() );

            /* There should be only one */
            if( edges.size() == 0 ) {
                throw new IllegalStateException( "No extension node defined for " + className );
            } else {
                hub = getExtensionHub( edges.get( 0 ).getTargetNode() );
            }
        } catch ( Exception e ) {
            throw new NoSuchExtensionException( e.getMessage(), e );
        }

        /* Remove any configured extensions from this hub */
        hub.removeExtensions();

        handleJsonConfig( request, extensionClassData );
    }

    public void handleJsonConfig( Request request, JsonObject jsonData ) {
        logger.debug( "Handling configuration Json data" );

        /* Get Json configuration objects */
        List<JsonObject> objects = SeventyEight.getInstance().getJsonObjects( jsonData );
        logger.debug( "I got " + objects.size() + " configurations" );

        for( JsonObject o : objects ) {
            logger.debug( "o: " + o );
            try {
                /* Get Json configuration object class name */
                String cls = o.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
                logger.debug( "Class is " + cls );

                Class<?> clazz = Class.forName( cls );
                logger.debug( "Class is " + clazz );
                Descriptor<?> d = SeventyEight.getInstance().getDescriptor( clazz );
                logger.debug( "Descriptor is " + d );


                Describable e = d.newInstance( getDB() );
                logger.debug( "Saving configurable " + e );
                e.doSave( request, o );
                logger.debug( "Describable saved" );
                node.createEdge( e.getNode(), d.getRelationType() );
            } catch( Exception e ) {
                logger.warn( "Unable to get descriptor for " + o + ": " + e.getMessage() );
                //ExceptionUtils.getRootCause( e ).printStackTrace();
            }
        }
    }

    public AbstractExtensionHub<?> getExtensionHub( Node node ) throws CouldNotLoadObjectException {
        return (AbstractExtensionHub) SeventyEight.getInstance().getDatabaseItem( node );
    }

    public AbstractExtensionHub<?> getExtensionHub( Descriptor<?> descriptor ) throws CouldNotLoadObjectException {
        List<Edge> edges = node.getEdges( descriptor.getRelationType(), Direction.OUTBOUND );

        if( edges.size() == 0 ) {
            return null;
        } else {
            if( edges.size() > 1 ) {
                logger.error( "Too many hubs defined for " + descriptor );
            }
            return (AbstractExtensionHub) SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getTargetNode() );
        }
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
			
			if( nodes.containsNode( ext.getCanonicalName() ) ) {
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



    /**
     * Get the extension hubs for this {@link Item}. Base implementation returns the empty list
     * @return
     */
    public List<AbstractExtensionHub> getExtensionsHubs() {
        return Collections.emptyList();
    }
}
