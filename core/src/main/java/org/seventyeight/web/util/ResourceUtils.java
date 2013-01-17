package org.seventyeight.web.util;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.ResourceDescriptor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author cwolfgang
 *         Date: 17-01-13
 *         Time: 21:24
 */
public class ResourceUtils {

    private static Logger logger = Logger.getLogger( ResourceUtils.class );

    private ResourceUtils() {

    }

    public static void getConfigureResourceView( Request request, HttpServletResponse response, AbstractResource resource, ResourceDescriptor descriptor ) throws TemplateDoesNotExistException, IOException {
        logger.debug( "Configuring " + resource );

        //ResourceDescriptor descriptor = (ResourceDescriptor) resource.getDescriptor();

        request.getContext().put( "url", "/resource/" + resource.getIdentifier() );
        request.getContext().put( "class", descriptor.getClazz().getName() );
        request.getContext().put( "header", "Configuring " + resource.getDisplayName() );
        request.getContext().put( "descriptor", descriptor );

        /* Required javascrips */
        request.getContext().put( "javascript", descriptor.getRequiredJavascripts() );

        logger.fatal( "NU ER VI HER " + request.getContext().get( "item" ) );
        request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( resource, "configure.vm" ) );
        response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );

    }
}
