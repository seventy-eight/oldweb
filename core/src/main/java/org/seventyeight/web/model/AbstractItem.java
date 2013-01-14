package org.seventyeight.web.model;

import java.util.*;

import org.apache.log4j.Logger;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.seventyeight.database.*;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.*;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletResponse;


public abstract class AbstractItem extends AbstractDatabaseItem implements Item, Extensible {

	private static Logger logger = Logger.getLogger( AbstractItem.class );

	public AbstractItem( Node node ) {
		super( node );
	}
	
	public AbstractItem( Node node, Locale locale ) {
        super( node );
	}

    @Override
	public void save( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		logger.debug( "Begin saving" );

        Save save = getSaver( request, jsonData );
		
		save.before();
		save.save();
		save.after();
		save.updateIndexes();
		
		node.save();

        logger.debug( "Handling extensions" );
        if( jsonData != null ) {
            handleJsonExtensionClass( request, jsonData );
        } else {
            logger.debug( "Json data was null. Skipping" );
        }

        logger.debug( "Extensions handled" );
		
		logger.debug( "End saving item" );
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
     * @param request
     * @param jsonData
     * @throws org.seventyeight.web.exceptions.NoSuchExtensionException
     */
    public void handleJsonExtensionClass( ParameterRequest request, JsonObject jsonData ) {
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



                handleJsonConfig( extensionNode, request, obj );

            } catch( ClassNotFoundException e ) {
                e.printStackTrace();
            }
        }
    }

    public void handleJsonConfig( Node hubNode, ParameterRequest request, JsonObject jsonData ) {
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
                logger.debug( "Saving " + e );
                e.save( request, o );

                logger.debug( "Creating relation from hub node to describable" );
                hubNode.createEdge( e.getNode(), d.getRelationType() );

                /* Remove data!? */
                if( d.doRemoveDataItemOnConfigure() ) {
                    logger.debug( "This should remove the data attached to this item" );
                }


            } catch( Exception e ) {
                logger.warn( "Unable to get descriptor for " + o + ": " + e.getMessage() );
                //ExceptionUtils.getRootCause( e ).printStackTrace();
            }
        }
    }

    public void doConfigurationSubmit( ParameterRequest request, JsonObject jsonData ) throws ErrorWhileSavingException, ParameterDoesNotExistException, IncorrectTypeException, ResourceDoesNotExistException, InconsistentParameterException {
        save( request, jsonData );
    }

    @Override
    public Node getExtensionsNode() {
        List<Edge> edges = node.getEdges( SeventyEight.ResourceEdgeType.extensions, Direction.OUTBOUND );

        if( edges.size() == 1 ) {
            return edges.get( 0 ).getTargetNode();
        } else if( edges.size() == 0 ) {
            return null;
        } else {
            throw new IllegalStateException( "Too many extension nodes defined(" + edges.size() + ")" );
        }
    }

    public List<Node> getExtensionsNodes( Class<?> extensionClass ) {
        Node node = getExtensionsNode();
        logger.debug( "Extensions node " + node );

        List<Node> nodes = new ArrayList<Node>();

        if( node != null ) {
            List<Edge> edges = node.getEdges( ResourceEdgeType.extension, Direction.OUTBOUND, "extensionClass", extensionClass.getName() );
            for( Edge edge : edges ) {
                nodes.add( edge.getTargetNode() );
            }
        } else {
            logger.debug( "No extensions node" );
        }

        return nodes;
    }

    /**
     * Given a {@link Node} get all outbound extension {@link Edge}s outbound {@link Node}s.
     * @param node
     * @return
     */
    public static List<Node> getExtensionNodes( Node node ) {
        List<Edge> edges = node.getEdges( SeventyEight.ResourceEdgeType.extension, Direction.OUTBOUND );

        List<Node> nodes = new LinkedList<Node>();

        for( Edge edge : edges ) {
            nodes.add( edge.getTargetNode() );
        }

        return nodes;
    }

}
