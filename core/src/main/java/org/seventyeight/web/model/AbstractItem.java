package org.seventyeight.web.model;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.*;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletResponse;


public abstract class AbstractItem extends AbstractDatabaseItem implements Item, Extensible, Savable, Actionable, Action {

	private static Logger logger = Logger.getLogger( AbstractItem.class );

	public AbstractItem( Node node ) {
		super( node );
	}
	
	public AbstractItem( Node node, Locale locale ) {
        super( node );
	}

    @Override
    public String getUrlName() {
        return "Not really an action";
    }

    @Override
	public void save( CoreRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		logger.debug( "Begin saving" );

        Save save = getSaver( request, jsonData );

        request.setItem( this );

		save.before();
		save.save();
		save.after();
		save.updateIndexes();
		
		node.save();

        logger.debug( "Handling extensions" );
        if( jsonData != null ) {
            //handleJsonExtensionClass( request, jsonData );

            /* Get the extensions node */
            Node enode = getExtensionsNode();

            handleJsonConfigurations( enode, request, jsonData );
        } else {
            logger.debug( "Json data was null. Skipping" );
        }

        logger.debug( "Extensions handled" );
		
		logger.debug( "End saving item" );
	}

    public Save getSaver( CoreRequest request, JsonObject json ) {
        return new Save( this, request, json );
    }

	protected class Save {

		protected AbstractItem item;
		protected CoreRequest request;
		protected JsonObject jsonData;

		public Save( AbstractItem type, CoreRequest request, JsonObject jsonData ) {
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
		
		public CoreRequest getRequest() {
			return request;
		}
		
		public JsonObject getJsonData() {
			return jsonData;
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


    /**
     * This used for preprocessing items before viewing.
     */
    public void prepareView( ParameterRequest request ) {
        /* Default implementation is no op */
    }

    public AbstractDataItem getDataItem( Class<? extends AbstractDataItem> dataClass ) throws CouldNotLoadObjectException {
        List<Edge> edges = node.getEdges( ResourceEdgeType.data, Direction.OUTBOUND, dataClass.getName() );

        if( edges.size() == 0 ) {
            return null;
        } else {
            return (AbstractDataItem) SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getTargetNode() );
        }
    }



    /**
     * Get all extension classes for an item. Iterate over them, get the extension hub or create it.
     *
     * @param request
     * @param jsonData
     * @throws org.seventyeight.web.exceptions.NoSuchExtensionException
     */
    public void handleJsonExtensionClass( CoreRequest request, JsonObject jsonData ) {
        logger.debug( "Handling extension class Json data" );

        List<JsonObject> objects = SeventyEight.getInstance().getJsonObjects( jsonData, SeventyEight.JsonType.extensionClass );
        logger.debug( "I got " + objects.size() + " extension types" );

        for( JsonObject obj : objects ) {
            String className = obj.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
            logger.debug( "Extension class name is " + className );

            //AbstractExtensionHub hub = null;
            Node extensionNode = null;

            try {
                Class<?> clazz = Class.forName( className );
                logger.debug( "Extension class is " + clazz );
                List<Edge> edges = node.getEdges( SeventyEight.ResourceEdgeType.extensions, Direction.OUTBOUND, SeventyEight.FIELD_EXTENSION_CLASS, clazz.getName() );

                /* There should be only one */
                if( edges.size() == 0 ) {
                    /* Create new extension node, with extensions set */
                    extensionNode = SeventyEight.getInstance().createNode( getDB(), null /* For now! */, new String[] { SeventyEight.FIELD_EXTENSION_CLASS }, new Object[] { className } );

                    /* Create the relation from this to the extension node */
                    this.getNode().createEdge( extensionNode, SeventyEight.ResourceEdgeType.extensions );
                } else {
                    //hub = getExtensionHub( edges.get( 0 ).getTargetNode() );
                    extensionNode = edges.get( 0 ).getTargetNode();

                    /* Remove any configured extensions from this hub */
                    //hub.removeExtensions();
                    extensionNode.removeEdges( SeventyEight.ResourceEdgeType.extension, Direction.OUTBOUND );
                }



                handleJsonConfigurations( extensionNode, request, obj );

            } catch( ClassNotFoundException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     *
     * @param extensionsNode
     * @param request
     * @param jsonData
     * @return
     * @throws DescribableException
     */
    public Describable handleJsonConfiguration( Node extensionsNode, CoreRequest request, JsonObject jsonData ) throws DescribableException {
        String cls = jsonData.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, SeventyEight.__JSON_CLASS_NAME, cls );

        return handleJsonConfiguration( extensionsNode, request, jsonData, ( edges.size() > 0 ? edges.get( 0 ).getTargetNode() : null ) );
    }


    /**
     * Given a {@link JsonObject} return a {@link Describable}. The nodeMap determines whether to instantiate or not.
     *
     * @param extensionsNode
     * @param request
     * @param jsonData
     * @param nodeMap Can be null
     * @return
     */
    public Describable handleJsonConfiguration( Node extensionsNode, CoreRequest request, JsonObject jsonData, Map<String, Node> nodeMap ) throws DescribableException {
        String cls = jsonData.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
        if( nodeMap != null && nodeMap.containsKey( cls ) ) {
            return handleJsonConfiguration( extensionsNode, request, jsonData, nodeMap.get( cls ) );
        } else {
            return handleJsonConfiguration( extensionsNode, request, jsonData, (Node) null );
        }
    }

    public Describable handleJsonConfiguration( Node extensionsNode, CoreRequest request, JsonObject jsonData, Node enode ) throws DescribableException {
        try {
            /* Get Json configuration object class name */
            String cls = jsonData.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
            logger.debug( "Configuration class is " + cls );

            Class<?> clazz = Class.forName( cls );
            Descriptor<?> d = SeventyEight.getInstance().getDescriptor( clazz );
            logger.debug( "Descriptor is " + d );

            Describable e = null;

            /* Determine existence */
            if( enode != null ) {
                e = (Describable) SeventyEight.getInstance().getDatabaseItem( enode );
            } else {
                e = d.newInstance( getDB() );

                logger.debug( "Creating relation from hub node to describable" );
                extensionsNode.createEdge( e.getNode(), d.getRelationType() );

            }

            logger.debug( "Saving describable: " + e );
            e.save( request, jsonData );


            /* Remove data!? */
            if( d.doRemoveDataItemOnConfigure() ) {
                logger.debug( "This should remove the data attached to this item" );
            }

            return e;

        } catch( Exception e ) {
            logger.warn( "Unable to get describable for " + jsonData + ": " + e.getMessage() );
            throw new DescribableException( "Cannot get descriable", e );
        }
    }



    public void handleJsonExtensionClass( Node extensionsNode, CoreRequest request, JsonObject extensionConfiguration ) {
        String extensionClassName = extensionConfiguration.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
        logger.debug( "Extension class name is " + extensionClassName );

        /* Get Json configuration objects */
        List<JsonObject> configs = SeventyEight.getInstance().getJsonObjects( extensionConfiguration );
        logger.debug( "I got " + configs.size() + " configurations" );

        /* Prepare existing configuration nodes */
        List<Edge> extensionEdges = extensionsNode.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, SeventyEight.FIELD_EXTENSION_CLASS, extensionClassName );

        Map<String, Node> nodeMap = new HashMap<String, Node>();

        for( Edge edge : extensionEdges ) {
            Node node = edge.getTargetNode();
            String className = node.get( "class" );

            if( className != null ) {
                nodeMap.put( className, node );
            }
        }

        for( JsonObject c : configs ) {
            try {
                Describable d = handleJsonConfiguration( extensionsNode, request, c, nodeMap );
                /**/
                d.getNode().set( SeventyEight.FIELD_EXTENSION_CLASS, extensionClassName ).save();
            } catch( DescribableException e ) {
                logger.error( e );
            }
        }
    }

    /**
     *
     * @param extensionsNode
     * @param request
     * @param jsonData
     */
    public void handleJsonConfigurations( Node extensionsNode, CoreRequest request, JsonObject jsonData ) {

        logger.debug( "Handling extension class Json data" );

        List<JsonObject> extensionsObjects = SeventyEight.getInstance().getJsonObjects( jsonData, SeventyEight.JsonType.extensionClass );
        logger.debug( "I got " + extensionsObjects.size() + " extension types" );

        for( JsonObject obj : extensionsObjects ) {
            handleJsonExtensionClass( extensionsNode, request, obj );
        }
    }

    public void doConfigurationSubmit( Request request, HttpServletResponse response, JsonObject jsonData ) throws ErrorWhileSavingException, ParameterDoesNotExistException, IncorrectTypeException, ResourceDoesNotExistException, InconsistentParameterException, TemplateDoesNotExistException, IOException {
        save( request, jsonData );

        request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( this, "index.vm" ) );
        response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
    }

    @Override
    public Node getExtensionsNode() {
        logger.debug( "GET EXTENSIONS NODE!!!!!" );
        List<Edge> edges = node.getEdges( SeventyEight.ResourceEdgeType.extensions, Direction.OUTBOUND );
        logger.debug( "EDGES: " + edges );

        if( edges.size() == 1 ) {
            return edges.get( 0 ).getTargetNode();
        } else if( edges.size() == 0 ) {
            Node enode = getDB().createNode().set( "description", "This is an extensions node, containing all extension relations to " + this );
            node.createEdge( enode, ResourceEdgeType.extensions );
            return enode;
        } else {
            throw new IllegalStateException( "Too many extension nodes defined(" + edges.size() + ")" );
        }
    }

    public List<Node> getExtensionsNodes( Class<?> extensionClass ) {
        logger.debug( "Extensions node " + extensionClass );
        Node enode = getExtensionsNode();
        logger.debug( "Extensions node " + enode );

        List<Node> nodes = new ArrayList<Node>();

        if( enode != null ) {
            List<Edge> edges = enode.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, "extensionClass", extensionClass.getName() );
            for( Edge edge : edges ) {
                nodes.add( edge.getTargetNode() );
            }
        } else {
            logger.debug( "No extensions node" );
        }

        return nodes;
    }

    public List<Item> getContributingViews( String view, AbstractTheme theme ) {
        /* Default implementation is empty */
        return Collections.emptyList();
    }

    /**
     * Given a {@link Node} get all outbound extension {@link Edge}s outbound {@link Node}s.
     * @param node
     * @return
     */
    public static List<Node> getExtensionNodes( Node node, Class<?> extensionClass ) {
        List<Edge> edges = node.getEdges( SeventyEight.ResourceEdgeType.extension, Direction.OUTBOUND );

        List<Node> nodes = new LinkedList<Node>();

        for( Edge edge : edges ) {
            nodes.add( edge.getTargetNode() );
        }

        return nodes;
    }

    public void addAction( AbstractAction action ) {
        List<Edge> edges = node.getEdges( ResourceEdgeType.action, Direction.OUTBOUND, "action", action.getUrlName() );

        if( edges.size() == 0 ) {
            logger.debug( "No actions named " + action.getUrlName() + ", just adding" );

        } else {
            logger.debug( edges.size() + " actions named " + action.getUrlName() + ", removing them first" );
            for( Edge edge : edges ) {
                edge.getTargetNode().remove();
                edge.remove();
            }

            this.createRelation( action, ResourceEdgeType.action );
        }


        action.getNode().set( "action", action.getUrlName() );
        action.getNode().save();
        this.createRelation( action, ResourceEdgeType.action ).save();
    }

    @Override
    public Action getAction( Request request, String urlName ) {
        List<Edge> edges = node.getEdges( ResourceEdgeType.action, Direction.OUTBOUND, "action", urlName );

        if( edges.size() == 0 ) {
            return null;
        } else {
            try {
                return (Action) SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getTargetNode() );
            } catch( CouldNotLoadObjectException e ) {
                logger.warn( e.getMessage() );
                return null;
            }
        }
    }

    public abstract EdgeType getEdgeType();

    public <T extends AbstractItem> T getParent() {
        List<Edge> edges = node.getEdges( getEdgeType(), Direction.INBOUND );

        if( edges.size() == 0 ) {
            return null;
        } else {
            try {
                return (T) SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getSourceNode() );
            } catch( CouldNotLoadObjectException e ) {
                logger.warn( e );
                return null;
            }
        }
    }
}
