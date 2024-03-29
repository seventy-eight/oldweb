package org.seventyeight.web.model.toplevelactionhandlers;

import org.apache.log4j.Logger;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Index;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.servlet.Request;
import org.seventyeight.web.model.TopLevelExecutor;
import org.seventyeight.web.servlet.Response;
import org.seventyeight.web.util.ResourceHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author cwolfgang
 *         Date: 22-12-12
 *         Time: 20:33
 */
public class DatabaseBrowser implements TopLevelExecutor {

    private Logger logger = Logger.getLogger( DatabaseBrowser.class );

    private ResourceHelper helper = new ResourceHelper();

    @Override
    public void execute( Request request, Response response ) throws ActionHandlerException {
        request.getContext().put( "outbound", Direction.OUTBOUND );
        request.getContext().put( "inbound", Direction.INBOUND );

        if( request.getRequestParts()[2].equals( "browse" ) ) {
            if( request.getRequestParts()[3].equals( "resource" ) ) {
                try {
                    AbstractResource r = helper.getResource( request, request.getRequestParts()[4] );

                    request.getContext().put( "idx", r.getNode().getId( true ) );

                    request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( r.getNode(), "view.vm" ) );
                } catch( Exception e ) {
                    throw new ActionHandlerException( e );
                }
            } else if( request.getRequestParts()[3].equals( "node" ) ) {
                if( request.getRequestParts()[4].equals( "orient" ) ) {
                    String idStr = null;
                    try {
                        idStr = URLDecoder.decode( request.getRequestParts()[5], "UTF-8" );
                    } catch( UnsupportedEncodingException e ) {
                        throw new ActionHandlerException( e );
                    }
                    Node node = request.getDB().getByIndex( idStr );
                    try {
                        request.getContext().put( "idx", node.getId( true ) );
                        request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( node, "view.vm" ) );
                    } catch( Exception e ) {
                        throw new ActionHandlerException( e );
                    }
                }
            } else if( request.getRequestParts()[3].equals( "edge" ) ) {

            } else if( request.getRequestParts()[3].equals( "index" )   ) {

                String idStr = null;
                try {
                    idStr = URLDecoder.decode( request.getRequestParts()[4], "UTF-8" );
                } catch( UnsupportedEncodingException e ) {
                    throw new ActionHandlerException( e );
                }
                Index index = request.getDB().getIndex( idStr );
                try {
                    request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( index, "view.vm" ) );
                } catch( Exception e ) {
                    throw new ActionHandlerException( e );
                }
            }
        }

        try {
            response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
        } catch( Exception e ) {
            throw new ActionHandlerException( e );
        }
    }

    public String getUrlName() {
        return "db";
    }
}
