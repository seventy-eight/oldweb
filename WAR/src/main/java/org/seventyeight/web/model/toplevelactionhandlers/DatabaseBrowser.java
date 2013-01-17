package org.seventyeight.web.model.toplevelactionhandlers;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import org.apache.log4j.Logger;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Node;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDatabase;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.Action;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.TopLevelAction;
import org.seventyeight.web.util.ResourceHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author cwolfgang
 *         Date: 22-12-12
 *         Time: 20:33
 */
public class DatabaseBrowser implements TopLevelAction {

    private Logger logger = Logger.getLogger( DatabaseBrowser.class );

    private ResourceHelper helper = new ResourceHelper();

    @Override
    public Action getAction( String subSpace ) {
        return null;
    }

    @Override
    public boolean execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
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

            }
        }

        try {
            response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
        } catch( Exception e ) {
            throw new ActionHandlerException( e );
        }

        return true;
    }

    public String getName() {
        return "db";
    }
}
