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


public abstract class AbstractItem extends AbstractDatabaseItem implements Item, Savable, Actionable, Action {

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
            handleJsonConfigurations( request, jsonData );
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
     *
     * @param request
     * @param jsonData
     * @return
     * @throws DescribableException
     */
    public Describable handleJsonConfiguration( CoreRequest request, JsonObject jsonData ) throws DescribableException {
        String cls = jsonData.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, SeventyEight.__JSON_CLASS_NAME, cls );

        return handleJsonConfiguration( request, jsonData, ( edges.size() > 0 ? edges.get( 0 ).getTargetNode() : null ) );
    }


    /**
     * Given a {@link JsonObject} return a {@link Describable}. The nodeMap determines whether to instantiate or not.
     *
     * @param request
     * @param jsonData
     * @param nodeMap Can be null
     * @return
     */
    public Describable handleJsonConfiguration( CoreRequest request, JsonObject jsonData, Map<String, Node> nodeMap ) throws DescribableException {
        String cls = jsonData.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
        if( nodeMap != null && nodeMap.containsKey( cls ) ) {
            return handleJsonConfiguration( request, jsonData, nodeMap.get( cls ) );
        } else {
            return handleJsonConfiguration( request, jsonData, (Node) null );
        }
    }

    public Describable handleJsonConfiguration( CoreRequest request, JsonObject jsonData, Node enode ) throws DescribableException {
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
                node.createEdge( e.getNode(), d.getRelationType() );

            }

            logger.debug( "Saving describable: " + e );
            e.save( request, jsonData );


            /* Remove data!? */
            if( d.doRemoveDataItemOnConfigure() ) {
                logger.debug( "This should remove the data attached to this item" );
            }

            return e;

        } catch( Exception e ) {
            logger.warn( "Unable to handle configuration for " + jsonData + ": " + e.getMessage() );
            e.printStackTrace();
            logger.warn( e );
            throw new DescribableException( "Cannot handle configuration", e );
        }
    }



    public void handleJsonExtensionClass( CoreRequest request, JsonObject extensionConfiguration ) {
        String extensionClassName = extensionConfiguration.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
        logger.debug( "Extension class name is " + extensionClassName );

        /* Get Json configuration objects */
        List<JsonObject> configs = SeventyEight.getInstance().getJsonObjects( extensionConfiguration );
        logger.debug( "I got " + configs.size() + " configurations" );

        /* Prepare existing configuration nodes */
        List<Edge> extensionEdges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, SeventyEight.FIELD_EXTENSION_CLASS, extensionClassName );

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
                Describable d = handleJsonConfiguration( request, c, nodeMap );
                /**/
                d.getNode().set( SeventyEight.FIELD_EXTENSION_CLASS, extensionClassName ).save();
            } catch( DescribableException e ) {
                logger.error( e );
            }
        }
    }

    /**
     *
     * @param request
     * @param jsonData
     */
    public void handleJsonConfigurations( CoreRequest request, JsonObject jsonData ) {

        logger.debug( "Handling extension class Json data" );

        List<JsonObject> extensionsObjects = SeventyEight.getInstance().getJsonObjects( jsonData, SeventyEight.JsonType.extensionClass );
        logger.debug( "I got " + extensionsObjects.size() + " extension types" );

        for( JsonObject obj : extensionsObjects ) {
            handleJsonExtensionClass( request, obj );
        }
    }

    public String getUrl() {
        return "";
    }

    public void doConfigurationSubmit( Request request, HttpServletResponse response, JsonObject jsonData ) throws ErrorWhileSavingException, ParameterDoesNotExistException, IncorrectTypeException, ResourceDoesNotExistException, InconsistentParameterException, TemplateDoesNotExistException, IOException {
        save( request, jsonData );
        response.sendRedirect( getUrl() );
        //request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( this, "index.vm" ) );
        //response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
    }

    public List<Node> getExtensionsNodes( Class<?> extensionClass ) {
        logger.debug( "[Getting extensions] " + extensionClass );
        logger.debug( "[NODE] " + node );

        List<Node> nodes = new ArrayList<Node>();

        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, "extensionClass", extensionClass.getName() );
        for( Edge edge : edges ) {
            nodes.add( edge.getTargetNode() );
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
        if( getEdgeType() != null ) {
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
        } else {
            return null;
        }
    }
}
