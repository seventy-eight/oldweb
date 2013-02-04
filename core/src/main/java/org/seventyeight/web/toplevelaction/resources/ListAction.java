package org.seventyeight.web.toplevelaction.resources;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.model.Action;
import org.seventyeight.web.servlet.Request;
import org.seventyeight.web.servlet.Response;
import org.seventyeight.web.util.ResourceSet;

import java.io.IOException;

/**
 * @author cwolfgang
 *         Date: 04-02-13
 *         Time: 16:51
 */
public class ListAction implements Action {

    private static Logger logger = Logger.getLogger( ListAction.class );

    @Override
    public String getUrlName() {
        return "list";
    }

    @Override
    public String getDisplayName() {
        return "Resource list";
    }

    public void doIndex( Request request, Response response ) throws TemplateDoesNotExistException, IOException {
        String type = request.getValue( "type", null );

        ResourceSet set = SeventyEight.getInstance().getResourcesByType( request.getDB(), type );

        request.getContext().put( "set", set );
        request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( set, "list.vm" ) );
        response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
    }

    public void doSelect( Request request, Response response ) throws TemplateDoesNotExistException, IOException {
        String type = request.getValue( "type", null );

        ResourceSet set = SeventyEight.getInstance().getResourcesByType( request.getDB(), type );

        request.getContext().put( "set", set );
        request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( set, "select.vm" ) );
        response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
    }
}
