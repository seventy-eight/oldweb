package org.seventyeight.web.model.toplevelactionhandlers;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.exceptions.MissingDescriptorException;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.servlet.Request;
import org.seventyeight.web.model.ResourceDescriptor;
import org.seventyeight.web.servlet.Response;
import org.seventyeight.web.util.ResourceHelper;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 14-01-13
 *         Time: 08:55
 */
public class ResourcesHandler {

    private static Logger logger = Logger.getLogger( ResourcesHandler.class );

    private ResourceHelper helper = new ResourceHelper();

    //@Override
    public void prepare( Request request ) {
    }

    //@Override
    public void execute( Request request, Response response ) throws ActionHandlerException {
        String[] parts = request.getRequestParts();
        String requestMethod = parts[2];

        /* Creating a new resource */
        if( requestMethod.equalsIgnoreCase( "create" ) ) {

            /* Instantiating the new resource */
            if( request.isRequestPost() ) {
                String className = request.getValue( "className" );

                if( className == null ) {
                    throw new ActionHandlerException( "No className given" );
                }

                /* Get the resource descriptor from the className name */
                ResourceDescriptor<?> descriptor = null;
                try {
                    descriptor = (ResourceDescriptor<?>) SeventyEight.getInstance().getResourceDescriptor( className );
                } catch( ClassNotFoundException e ) {
                    throw new ActionHandlerException( e );
                }

                if( descriptor == null ) {
                    throw new ActionHandlerException( new MissingDescriptorException( "Could not find descriptor for " + className ) );
                }

                /* First of all we need to create the resource node */
                logger.debug( "Newing resource" );
                AbstractResource r = null;
                try {
                    r = (AbstractResource) descriptor.newInstance( request.getDB() );
                    String title = request.getValue( "title", "" );
                    r.setTitle( title, null );
                    r.getNode().save();
                } catch( UnableToInstantiateObjectException e ) {
                    throw new ActionHandlerException( e );
                }
                logger.debug( "RESOURCE IS " + r );

                /* Now the configuration page must be displayed */
                helper.configureResource( request, response, r, descriptor );

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

    public String getName() {
        return "resources";
    }
}
