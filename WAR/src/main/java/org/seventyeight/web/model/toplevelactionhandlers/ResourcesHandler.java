package org.seventyeight.web.model.toplevelactionhandlers;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.exceptions.MissingDescriptorException;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.ResourceDescriptor;
import org.seventyeight.web.model.TopLevelAction;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 14-01-13
 *         Time: 08:55
 */
public class ResourcesHandler implements TopLevelAction {

    private static Logger logger = Logger.getLogger( ResourcesHandler.class );

    @Override
    public void prepare( Request request ) {
    }

    @Override
    public void execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        String[] parts = request.getRequestParts();
        String requestMethod = parts[2];

        /* Creating a new resource */
        if( requestMethod.equalsIgnoreCase( "create" ) ) {

            /* Instantiating the new resource */
            if( request.isRequestPost() ) {
                String type = request.getValue( "type" );

                if( type == null ) {
                    throw new ActionHandlerException( "No type given" );
                }

                /* Get the resource descriptor from the type name */
                ResourceDescriptor<?> descriptor = (ResourceDescriptor<?>) SeventyEight.getInstance().getDescriptorFromResourceType( type );

                if( descriptor == null ) {
                    throw new ActionHandlerException( new MissingDescriptorException( "Could not find descriptor for " + type ) );
                }

                /* First of all we need to create the resource node */
                logger.debug( "Newing resource" );
                AbstractResource r = null;
                try {
                    r = (AbstractResource) descriptor.newInstance( request.getDB() );
                    String title = request.getValue( "title", "" );
                    r.setTitle( title, null );
                } catch( UnableToInstantiateObjectException e ) {
                    throw new ActionHandlerException( e );
                }
                logger.debug( "RESOURCE IS " + r );

                /* Now the configuration page must be displayed */

            } else { /* Displaying the list of resource types */
                try {
                    request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( SeventyEight.getInstance(), "createResource.vm" ) );
                    response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
                } catch( Exception e ) {
                    throw new ActionHandlerException( e );
                }
                return;
            }
        }
    }

    @Override
    public String getName() {
        return "resources";
    }
}
