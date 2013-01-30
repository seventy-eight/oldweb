package org.seventyeight.web.extensions.debate.simple;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.extensions.debate.AbstractDebate;
import org.seventyeight.web.extensions.debate.DebateException;
import org.seventyeight.web.model.CoreRequest;
import org.seventyeight.web.model.Describable;
import org.seventyeight.web.model.Descriptor;
import org.seventyeight.web.model.Reply;

/**
 * @author cwolfgang
 *         Date: 05-01-13
 *         Time: 23:25
 */
public class SimpleDebate extends AbstractDebate implements Describable {

    private static Logger logger = Logger.getLogger( SimpleDebate.class );

    public SimpleDebate( Node node ) {
        super( node );
    }

    @Override
    public Descriptor<?> getReplyDescriptor() {
        return null;
    }

    @Override
    public void addReply( Reply reply ) throws DebateException {
    }

    @Override
    public void save( CoreRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
        super.save( request, json );

        logger.fatal( "[MY JSON IS] " + json );

        String snade = request.getValue( "snade" );
        logger.debug( "[SIMPLE DEBATE] " + request.getParameter( "snade" ) );
        node.set( "SNADE", snade );

        node.save();
    }

    public static class SimpleDebateDescriptor extends DebateImplDescriptor {

        @Override
        public String getDisplayName() {
            return "Simple debate";
        }
    }
}
