package org.seventyeight.web.model;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author cwolfgang
 *         Date: 07-01-13
 *         Time: 20:42
 */
public abstract class AbstractExtensibleItem extends AbstractItem implements Extensible {

    private static Logger logger = Logger.getLogger( AbstractExtensibleItem.class );

    public AbstractExtensibleItem( Node node ) {
        super( node );
    }

    @Override
    public void save( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
        super.save( request, jsonData );

        logger.debug( "Handling extensions" );
        if( jsonData != null ) {
            handleJsonExtensionClass( request, jsonData );
        } else {
            logger.debug( "Json data was null. Skipping" );
        }

        logger.debug( "Extensions handled" );
    }

    @Override
    public List<Node> getExtensionClassNodes( Class<?> extensionClass ) {
        List<Edge> edges = node.getEdges( SeventyEight.ResourceEdgeType.extensionClass, Direction.OUTBOUND );

        List<Node> nodes = new LinkedList<Node>();

        for( Edge edge : edges ) {
            Node node = edge.getTargetNode();
            String clazzStr = (String) node.get( "class" );
            clazzStr.equals( extensionClass.getName() );
            nodes.add( node );
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
                List<Edge> edges = node.getEdges( SeventyEight.ResourceEdgeType.extensionClass, Direction.OUTBOUND, SeventyEight.FIELD_EXTENSION_CLASS, clazz.getName() );

                /* There should be only one */
                if( edges.size() == 0 ) {
                    /* Create new extension node, with extensionClass set */
                    extensionNode = SeventyEight.getInstance().createNode( getDB(), null /* For now! */, new String[] { SeventyEight.FIELD_EXTENSION_CLASS }, new Object[] { className } );

                    /* Create the relation from this to the extension node */
                    this.getNode().createEdge( extensionNode, SeventyEight.ResourceEdgeType.extensionClass );
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

}
