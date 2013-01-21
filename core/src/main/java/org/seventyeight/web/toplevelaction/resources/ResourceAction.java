package org.seventyeight.web.toplevelaction.resources;

import org.apache.log4j.Logger;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.exceptions.CouldNotLoadResourceException;
import org.seventyeight.web.exceptions.NotFoundException;
import org.seventyeight.web.exceptions.TooManyException;
import org.seventyeight.web.model.*;
import org.seventyeight.web.util.ResourceUtils;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 17-01-13
 *         Time: 21:35
 */
public class ResourceAction implements TopLevelAction, Actionable {

    private static Logger logger = Logger.getLogger( ResourceAction.class );

    @Override
    public String getName() {
        return "resource";
    }

    @Override
    public boolean execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        return false;
    }

    @Override
    public Action getAction( Request request, String subSpace ) {
        logger.debug( "Get action " + subSpace );
        try {
            AbstractResource r = ResourceUtils.getResource( request.getDB(), subSpace );
            request.getContext().put( "title", r.getDisplayName() );
            return r;
        } catch( Exception e ) {
            logger.error( e );
            return null;
        }
    }
}
