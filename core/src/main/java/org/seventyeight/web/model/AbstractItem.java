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

    @Override
	public final void save( ParameterRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		logger.debug( "Begin saving" );

        Save save = getSaver( request, json );
		
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

    public Save getSaver( ParameterRequest request, JsonObject json ) {
        return new Save( this, request, json );
    }

	protected class Save {

		protected AbstractItem item;
		protected ParameterRequest request;
		protected JsonObject jsonData;

		public Save( AbstractItem type, ParameterRequest request, JsonObject jsonData ) {
			this.item = type;
			this.request = request;
			this.jsonData = jsonData;
		}
		
		public void before() {}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
            /* Base implementation is a no op */
        }
		
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


    /**
     * Get all extension classes for an item. Iterate over them, get the extension hub or create it.
     * @param request
     * @param jsonData
     * @throws NoSuchExtensionException
     */
    public void handleJsonExtensionClass( Request request, JsonObject jsonData ) {
        logger.debug( "Handling extension class Json data" );

        List<JsonObject> objects = SeventyEight.getInstance().getJsonObjects( jsonData, SeventyEight.JsonType.extensionClass );
        logger.debug( "I got " + objects.size() + " extension types" );

        for( JsonObject obj : objects ) {
            String className = obj.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
            logger.debug( "Extension class name is " + className );

            AbstractExtensionHub hub = null;

            try {
                Class<?> clazz = Class.forName( className );
                logger.debug( "Extension class is " + clazz );
                List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, "extensionClass", clazz.getName() );

                /* There should be only one */
                if( edges.size() == 0 ) {
                    //throw new IllegalStateException( "No extension node defined for " + className );

                } else {
                    hub = getExtensionHub( edges.get( 0 ).getTargetNode() );

                    /* Remove any configured extensions from this hub */
                    hub.removeExtensions();
                }



                handleJsonConfig( hub, request, jsonData );

            } catch( ClassNotFoundException e ) {
                e.printStackTrace();
            } catch( CouldNotLoadObjectException e ) {
                logger.error( "Could not load " + className );
                logger.error( e );
            }

        }

    }

    public void handleJsonConfig( AbstractExtensionHub hub, Request request, JsonObject jsonData ) {
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
                e.save( request, o );
                logger.debug( "Describable saved" );
                node.createEdge( e.getNode(), d.getRelationType() );

                /**/
                if( d.doRemoveDataItemOnConfigure() ) {
                    logger.debug( "This should remove the data attached to this item" );
                }


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


    /**
     * Get the extension hubs for this {@link Item}. Base implementation returns the empty list
     * @return
     */
    public List<AbstractExtensionHub> getExtensionsHubs() {
        return Collections.emptyList();
    }


    public AbstractDataItem getDataItem( Class<? extends AbstractDataItem> dataClass ) throws CouldNotLoadObjectException {
        List<Edge> edges = node.getEdges( ResourceEdgeType.data, Direction.OUTBOUND, dataClass.getName() );

        if( edges.size() == 0 ) {
            return null;
        } else {
            return (AbstractDataItem) SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getTargetNode() );
        }
    }
}
