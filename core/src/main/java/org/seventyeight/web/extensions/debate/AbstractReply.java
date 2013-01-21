package org.seventyeight.web.extensions.debate;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 21-12-12
 *         Time: 13:16
 */
public class AbstractReply extends AbstractItem implements Reply, Describable {

    private static Logger logger = Logger.getLogger( AbstractReply.class );

    public AbstractReply( Node node ) {
        super( node );
    }

    @Override
    public int getOrder() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Reply> getReplies() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumberOfReplies() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Descriptor<?> getDescriptor() {
        return SeventyEight.getInstance().getDescriptor( getClass() );
    }

    @Override
    public String getDisplayName() {
        return "Tree Reply";
    }

    @Override
    public final void save( CoreRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
        logger.debug( "Saving reply" );

        String title = request.getValue( "title", "" );
        node.set( "title", title );

        String msg = request.getValue( "reply", "" );
        node.set( "reply", msg );

        String id = request.getValue( "nodeId", "" );
        node.set( "nodeId", id );

        node.save();
    }

    public static class ReplyDescriptor extends Descriptor<AbstractReply> {

        @Override
        public String getDisplayName() {
            return "Tree Reply";
        }
    }
}
