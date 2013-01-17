package org.seventyeight.web.model.toplevelactionhandlers;

import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.debate.ReplyHub;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 21-12-12
 *         Time: 13:24
 */
public class DebateHandler  {


    public void prepare( Request request ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public void execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        long rid = Long.parseLong( (String) request.getValue( "identifier" ) );

        AbstractResource resource = null;
        try {
            resource = SeventyEight.getInstance().getResource( request.getDB(), rid );
        } catch( Exception e ) {
            throw new ActionHandlerException( e );
        }

        /* Posting something */
        if( request.isRequestPost() ) {
            if( request.getRequestParts()[2].equals( "reply" ) ) {
                if( request.getRequestParts()[3].equals( "create" ) ) {
                    try {
                        //Reply reply = createReply( request, resource );
                        //resource.reply( reply );
                    } catch( Exception e ) {
                        throw new ActionHandlerException( e );
                    }
                }
            }
        }
    }

    /*
    protected Reply createReply( Request request, AbstractResource resource ) throws CouldNotLoadObjectException, UnableToInstantiateObjectException, ErrorWhileSavingException, ParameterDoesNotExistException, IncorrectTypeException, ResourceDoesNotExistException, InconsistentParameterException, NoSuchHubException {

        ReplyHub hub = resource.getReplyHub();
        Descriptor<?> d = hub.getReplyDescriptor();
        Reply reply = (Reply) d.newInstance( request.getDB() );
        reply.save( request, null );

        return reply;
    }
    */


    public String getName() {
        return "debate";
    }
}
