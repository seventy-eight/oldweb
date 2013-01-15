package org.seventyeight.web.extensions.debate.simple;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.extensions.debate.AbstractDebate;
import org.seventyeight.web.extensions.debate.DebateInterface;
import org.seventyeight.web.model.Descriptor;
import org.seventyeight.web.model.ParameterRequest;

/**
 * @author cwolfgang
 *         Date: 05-01-13
 *         Time: 23:25
 */
public class SimpleDebate extends AbstractDebate {

    private static Logger logger = Logger.getLogger( SimpleDebate.class );

    public SimpleDebate( Node node ) {
        super( node );
    }

    @Override
    public Descriptor<?> getReplyDescriptor() {
        return null;
    }

    @Override
    public void save( ParameterRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
        super.save( request, json );

        logger.fatal( "[MY JSON IS] " + json );

        String snade = request.getValue( "snade" );
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
