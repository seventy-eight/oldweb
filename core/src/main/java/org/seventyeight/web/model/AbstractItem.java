package org.seventyeight.web.model;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.structure.Tuple;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.*;

import com.google.gson.JsonObject;
import org.seventyeight.web.hubs.AuthoritativeHub;
import org.seventyeight.web.hubs.ScoresHub;
import org.seventyeight.web.model.resources.User;
import org.seventyeight.web.servlet.Request;

import javax.servlet.http.HttpServletResponse;


public abstract class AbstractItem extends AbstractDatabaseItem implements Item, Savable, Action, Authorizer {

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
        node.save();

		save.after();

		node.save();

        save.updateIndexes();

        if( jsonData != null ) {
            logger.debug( "Removing actions" );
            recursivelyRemoveActions();

            logger.debug( "Handling extensions" );
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
    public Describable handleJsonConfiguration( CoreRequest request, JsonObject jsonData, Map<String, Tuple<Edge, Node>> nodeMap ) throws DescribableException {
        String cls = jsonData.get( SeventyEight.__JSON_CLASS_NAME ).getAsString();
        if( nodeMap != null && nodeMap.containsKey( cls ) ) {
            Describable d = handleJsonConfiguration( request, jsonData, nodeMap.get( cls ).getSecond() );
            nodeMap.remove( cls );
            return d;
        } else {
            return handleJsonConfiguration( request, jsonData, (Node) null );
        }
    }

    public void addExtension( Describable extension ) throws CouldNotLoadObjectException, UnableToInstantiateObjectException {
        logger.debug( "Adding " + extension );
        node.createEdge( extension.getNode(), ResourceEdgeType.extension ).save();
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
                e =  SeventyEight.getInstance().getDatabaseItem( enode );
            } else {
                e = d.newInstance( getDB() );
                addExtension( e );
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

        Map<String, Tuple<Edge, Node>> nodeMap = new HashMap<String, Tuple<Edge, Node>>();


        for( Edge edge : extensionEdges ) {
            Node node = edge.getTargetNode();
            String className = node.get( "class" );

            if( className != null ) {
                nodeMap.put( className, new Tuple( edge, node ) );
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

        /* Remove */
       logger.debug( "Removing superfluous extensions" );
        for( Tuple<Edge, Node> t : nodeMap.values() ) {
            recursivelyRemoveExtensions( t.getFirst() );
        }

    }

    public void recursivelyRemoveExtensions( Edge extensionEdge ) {
        recursivelyRemove( extensionEdge, ResourceEdgeType.extension );
    }
    public void recursivelyRemoveActions() {
        for( Edge e : getALlActions() ) {
            recursivelyRemove( e, ResourceEdgeType.action );
        }
    }

    public void recursivelyRemove( Edge startEdge, EdgeType type ) {

        Node node = startEdge.getTargetNode();

        for( Edge edge : node.getEdges( type, Direction.OUTBOUND ) ) {
            Node next = edge.getTargetNode();
            recursivelyRemoveExtensions( edge );
        }

        logger.debug( "Removing " + startEdge );
        startEdge.remove();
        logger.debug( "Removing " + node );
        node.remove();

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
        //response.sendRedirect( getUrl() );
        //request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( this, "index.vm" ) );
        //response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
    }

    public List<Node> getExtensionNodesByExtensionClass( Class<?> extensionClass ) {
        logger.debug( "[Getting extensions] " + extensionClass );
        logger.debug( "[NODE] " + node );

        List<Node> nodes = new ArrayList<Node>();

        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, "extensionClass", extensionClass.getName() );
        for( Edge edge : edges ) {
            nodes.add( edge.getTargetNode() );
        }

        return nodes;
    }

    public List<Node> getExtensionNodesByClass( Class<?> clazz ) {
        logger.debug( "[Getting extensions] " + clazz );
        logger.debug( "[NODE] " + node );

        List<Node> nodes = new ArrayList<Node>();

        List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, "class", clazz.getName() );
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

    public List<Edge> getALlExtensions() {
        return node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND );
    }

    public List<Edge> getALlActions() {
        return node.getEdges( ResourceEdgeType.action, Direction.OUTBOUND );
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

    protected void addScore( String name, double score ) {
        Edge edge = node.getFirstEdge( SeventyEight.ItemRelation.scores, Direction.OUTBOUND );

        ScoresHub hub = null;
        if( edge == null ) {
            try {
                hub = SeventyEight.getInstance().createItem( getDB(), ScoresHub.class );
            } catch( UnableToInstantiateObjectException e ) {
                logger.warn( e.getMessage() );
                return;
            }
        } else {
            hub = new ScoresHub( edge.getTargetNode() );
        }

        hub.addScore( name, score );
    }

    public <T extends AbstractHub> T getHub( Descriptor<? extends AbstractHub> descriptor ) throws PersistenceException {
        List<Edge> edges = node.getEdges( descriptor.getRelationType(), Direction.OUTBOUND );

        if( edges.size() == 0 ) {
            T instance = (T) descriptor.newInstance( getDB() );
            createRelation( instance, descriptor.getRelationType() );
            return instance;
        } else {
            return SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getTargetNode() );
        }
    }

    public boolean isOwner( User owner ) throws PersistenceException {
        return false;
    }

    @Override
    public Authorization getAuthorization( User user ) {
        AuthoritativeHub hub = null;
        try {
            hub = getHub( (Descriptor<? extends AbstractHub>) SeventyEight.getInstance().getDescriptor( AuthoritativeHub.class ) );
        } catch( PersistenceException e ) {
            logger.warn( e.getMessage() );
            return Authorization.NONE;
        }

        /* First check ownerships */
        try {
            if( isOwner( user ) ) {
                return Authorization.MODERATE;
            }
        } catch( PersistenceException e ) {
            logger.warn( e );
        }

        List<Node> mnodes = hub.getNodes( SeventyEight.AuthoritativeEdgeType.moderator );
        for( Node n : mnodes ) {
            try {
                Authoritative a = (Authoritative) SeventyEight.getInstance().getDatabaseItem( n );
                if( a.isAuthoritative( user ) ) {
                    return Authorization.MODERATE;
                }
            } catch( CouldNotLoadObjectException e ) {
                logger.warn( e.getMessage() );
            }
        }

        List<Node> vnodes = hub.getNodes( SeventyEight.AuthoritativeEdgeType.viewer );
        for( Node n : vnodes ) {
            try {
                Authoritative a = (Authoritative) SeventyEight.getInstance().getDatabaseItem( n );
                if( a.isAuthoritative( user ) ) {
                    return Authorization.VIEW;
                }
            } catch( CouldNotLoadObjectException e ) {
                logger.warn( e.getMessage() );
            }
        }

        logger.debug( "None of the above" );
        return Authorization.NONE;
    }


    public void remove() {
        logger.info( "Removing the Item " + this );

        /* First remove the parent edge */
        if( getEdgeType() != null ) {
            List<Edge> edges = node.getEdges( getEdgeType(), Direction.INBOUND );

            if( edges.size() == 1 ) {
                edges.get( 0 ).remove();
            }
        }

        /* Recursively remove child nodes? */

        /* Then the node itself */
        node.remove();

        /* Then indexes associated */
        if( this instanceof Indexed ) {
            Indexed i = (Indexed) this;
            for( String idxName : i.getIndexNames() ) {
                getDB().removeFromIndex( idxName, this.node );
            }
        }
    }

}
