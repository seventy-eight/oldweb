package org.seventyeight.web.extensions.debate;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;
import org.seventyeight.web.model.extensions.PostViewExtension;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 30-12-12
 *         Time: 21:54
 */
public class Debate extends AbstractItem implements PostViewExtension, Describable {

    private static Logger logger = Logger.getLogger( Debate.class );

    public Debate( Node node ) {
        super( node );
    }

    @Override
    public Descriptor<?> getDescriptor() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public final void save( ParameterRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
        //super.save( request, json );
        logger.debug( "JSON: " + json );
        String dobj = json.get( "debateClass" ).toString();
        logger.debug( "Debate class "  + dobj );

        /*
        Descriptor descriptor = null;
        try {
            descriptor = SeventyEight.getInstance().getDescriptor( dobj );
        } catch( ClassNotFoundException e ) {
            throw new ErrorWhileSavingException( e );
        }

        Describable instance = null;

        List<Edge> edges = node.getEdges( SeventyEight.ResourceEdgeType.extension, Direction.OUTBOUND );
        if( edges.size() == 0 ) {

        } else {
            logger.debug( "Removing existing extension" );
            Node enode = edges.get( 0 ).getTargetNode();
            enode.remove();
            edges.get( 0 ).remove();
        }

        try {
            instance = descriptor.newInstance( getDB() );
            instance.save( request, null );
        } catch( UnableToInstantiateObjectException e ) {
            throw new ErrorWhileSavingException( e );
        }

        node.createEdge( instance.getNode(), SeventyEight.ResourceEdgeType.extension );
        */
    }

    public static class DebateDescriptor extends Descriptor<Debate> {

        @Override
        public String getDisplayName() {
            return "Debate";
        }

        public List<Descriptor> getDebates() {
            return AbstractDebate.all();
        }

        @Override
        public Debate newInstance( Database db ) throws UnableToInstantiateObjectException {
            logger.fatal( "NEWING DEBATE" );
            return super.newInstance( db );

        }
    }
}
