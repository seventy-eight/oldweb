package org.seventyeight.web.model.toplevelactionhandlers;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.annotations.VisibleAction;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

import com.google.gson.JsonObject;
import org.seventyeight.web.util.ResourceHelper;

//@TopLevelActionHandlerType
public class ResourceHandler implements TopLevelAction {

	private Logger logger = Logger.getLogger( ResourceHandler.class );

	private ResourceHelper helper = new ResourceHelper();

	/**
	 * The default behavior. Parts is of the form:
	 * (0)/(1)handler/(2)rid/(3)method/(4...)
	 * 
	 */

	public String getName() {
		return "resource";
	}

    @Override
    public void prepare( Request request ) {
        /*  */
        if( request.getRequestParts().length == 3 ) {
            String[] parts2 = new String[4];
            parts2[0] = request.getRequestParts()[0];
            parts2[1] = request.getRequestParts()[1];
            parts2[2] = request.getRequestParts()[2];
            parts2[3] = "view";
            request.setRequestParts( parts2 );
        }
    }

    @Override
    public void execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        String[] parts = request.getRequestParts();

        Method method = null;
        String requestMethod = parts[2];

        /* Special case */
        if( requestMethod.equalsIgnoreCase( "create" ) ) {
            String type = parts[2];
            logger.debug( "[Create] " + type );

            ResourceDescriptor<?> descriptor = (ResourceDescriptor<?>) SeventyEight.getInstance().getDescriptorFromResourceType( type );

            if( descriptor == null ) {
                throw new ActionHandlerException( new MissingDescriptorException( "Could not find descriptor for " + type ) );
            }

            if( request.isRequestPost() ) {
                logger.debug( "Creating new " + type );
                logger.debug( "Parameters: " + request.getParameterMap() );
                AbstractResource r = null;
                try {
                    r = helper.createResource( descriptor, request, response );
                } catch( ResourceNotCreatedException e ) {
                    throw new ActionHandlerException( e );
                }

                /* When POST, view the resource */
                try {
                    viewResource( r, request, response );
                } catch( Exception e ) {
                    throw new ActionHandlerException( e );
                }
            } else {
                logger.debug( "Configuring new " + type );
                request.getContext().put( "url", "/resource/" + type + "/create" );
                request.getContext().put( "class", descriptor.getClazz().getName() );
                request.getContext().put( "header", "Creating new " + type );
                request.getContext().put( "descriptor", descriptor );

                /* Required javascrips */
                request.getContext().put( "javascript", descriptor.getRequiredJavascripts() );

                try {
                    /* Special dual side */
                    try {
                        request.getContext().put( "dualSide", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderClassNoRecursive( descriptor.getClazz(), "dualSide.vm" ) );
                    } catch ( TemplateDoesNotExistException e ) {
                        /* No op */
                        logger.debug( "Dual side not defined" );
                    }

                    /* Options */
                    logger.fatal( "NU ER VI HER " + request.getContext().get( "item" ) );
                    request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderClass( descriptor.getClazz(), "configure.vm" ) );
                    response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
                } catch( TemplateDoesNotExistException e ) {
                    /* This solution does not work */
                    logger.warn( e );
                } catch( IOException e ) {
                    throw new ActionHandlerException( e );
                }
            }

        } else {

            /* Get the resource */
            AbstractResource r = null;
            try {
                r = helper.getResource( request, response );
            } catch( Exception e ) {
                throw new ActionHandlerException( e );
            }

            request.getContext().put( "url", "/resource/" + r.getIdentifier() );
            request.getContext().put( "header", "Editing " + r.getTitle() );

            /* Check authentication */
            if( !request.hasAccess( r ) ) {
                //throw new
            }

            /* Put the title */
            request.getContext().put( "title", r.getTitle() );

            /* Try implementation of method */
            if( parts.length == 4 ) {

                if( request.isRequestPost() ) {
                    logger.debug( "This is a POST request" );

                    try {
                        method = getRequestMethod( r, requestMethod, request.isRequestPost() );
                        method.invoke( method, request, null );

                    } catch( Exception e ) {
                        throw new ActionHandlerException( e );
                    }

                    /* When POST, view the resource */
                    try {
                        viewResource( r, request, response );
                    } catch( Exception e ) {
                        throw new ActionHandlerException( e );
                    }

                } else {
                    logger.debug( "This is a GET request" );

                    try {
                        method = getRequestMethod( r, requestMethod, request.isRequestPost() );
                        method.invoke( method, request, response );
                    } catch( Exception e ) {
                        logger.warn( "Unable to execute " + requestMethod );
                        logger.warn( e );
                    }


                    /* Try view file */
                    if( method == null ) {
                        logger.debug( "Locating template" );
                        try {
                            request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( r, requestMethod + ".vm" ) );
                            response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
                        } catch( TemplateDoesNotExistException e ) {
                            /* This solution does not work */
                            logger.warn( e );
                        } catch( IOException e ) {
                            throw new ActionHandlerException( e );
                        }
                    }


                }

                return;
            } else {
                requestMethod = "";
                throw new ActionHandlerException( "NOT IMPLEMENTED YET!" );
            }
        }
    }

    private void viewResource( AbstractResource resource, Request request, HttpServletResponse response ) throws IOException, TemplateDoesNotExistException {
        request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( resource, "view.vm" ) );
        response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
    }

    private Method getRequestMethod( AbstractResource resource, String method, boolean post ) throws NoSuchMethodException {
        String m = "do" + method.substring( 0, 1 ).toUpperCase() + method.substring( 1, method.length() );
        logger.debug( "Method: " + method + " = " + m );
        if( post ) {
            return resource.getClass().getDeclaredMethod( m, ParameterRequest.class, JsonObject.class );
        } else {
            return resource.getClass().getDeclaredMethod( m, Request.class, HttpServletResponse.class );
        }
    }

	public String getDisplayName() {
		return "Resource";
	}

	/* Top level menu item */
    /*
	public MenuItem getParent() {
		return null;
	}
	*/


}
