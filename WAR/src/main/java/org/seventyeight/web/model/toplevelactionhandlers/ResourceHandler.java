package org.seventyeight.web.model.toplevelactionhandlers;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.annotations.VisibleAction;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

import com.google.gson.JsonObject;
import org.seventyeight.web.util.ResourceHelper;

@TopLevelActionHandlerType
public class ResourceHandler extends AbstractTopLevelActionHandler {

	private Logger logger = Logger.getLogger( ResourceHandler.class );
	
	private ResourceHelper helper = new ResourceHelper();

	/**
	 * The default behavior. Parts is of the form:
	 * (0)/(1)handler/(2)rid/(3)method/(4...) or
	 * (0)/(1)handler/(2)type/(3)method/(4...)
	 * 
	 * @param parts
	 * @return
	 * @throws NoSuchMethodException
	 */
	@Override
	public Method getMethod( String[] parts, Class<? extends TopLevelAction> clazz ) throws NoSuchMethodException {
		Method method;
		logger.debug( "Handling resource: " + Arrays.asList( parts ) );
		try {
			method = clazz.getDeclaredMethod( parts[3], Request.class, HttpServletResponse.class );
		} catch( Exception e ) {
			logger.debug( "The method " + parts[3] + " was not found in resource handler, using handleUnknown" );
			method = clazz.getDeclaredMethod( "handleUnknown", Request.class, HttpServletResponse.class );
		}

		logger.debug( "Method: " + parts[3] );
		return method;
	}
	
	public String getName() {
		return "resource";
	}

    public void execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        String[] parts = request.getRequestParts();
        logger.debug( "Handling resource: " + Arrays.asList( parts ) );

        Method method = null;
        String requestMethod = parts[3];

        AbstractResource r = null;
        try {
            r = helper.getResource( request, response );
        } catch( Exception e ) {
            throw new ActionHandlerException( e );
        }

        /* Try implementation of method */
        if( parts.length == 4 ) {
            try {
                method = getRequestMethod( r, requestMethod, request.isRequestPost() );
                if( request.isRequestPost() ) {
                    method.invoke( method, request, null );
                } else {
                    method.invoke( method, request, response );
                }

                return;
            } catch( Exception e ) {
                /* Try next solution */
            }
        } else {
            requestMethod = "";
            throw new ActionHandlerException( "NOT IMPLEMENTED YET!" );
        }

        /* Try view */
        if( method == null ) {
            logger.debug( "Locating template" );
            try {
                request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( r, requestMethod + ".vm" ).get() );
                response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate(), GraphDragon.getInstance().getDefaultTheme(), request.getContext() ) );
            } catch( TemplateDoesNotExistException e ) {
                /* This solution does not work */
                logger.warn( e );
            }
        }

        throw new ActionHandlerException( "Could not execute " + requestMethod );
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


	@VisibleAction
	public void handleUnknown( Request request, HttpServletResponse response ) throws ActionHandlerException {

		/* Request concerning a resource */
		if( isInt( request.getRequestParts()[2] ) ) {
			logger.debug( request.getRequestParts()[1] + " = " + request.getRequestParts()[2] );
			Long id = new Long( request.getRequestParts()[2] );

			AbstractResource r = null;
			try {
				r = SeventyEight.getInstance().getResource( id );
				logger.debug( "Resource: " + r );
				request.setResource( r );
			} catch( CouldNotLoadResourceException e ) {
				throw new ActionHandlerException( e );
			}

			Method method = null;
			try {
				String m = "do" + request.getRequestParts()[3].substring( 0, 1 ).toUpperCase() + request.getRequestParts()[3].substring( 1 ).toLowerCase();
				logger.debug( "Method: " + m );
				method = r.getClass().getDeclaredMethod( m, Request.class, Writer.class, JsonObject.class );
				StringWriter writer = new StringWriter();
				
				JsonObject jo = null;
				try {
					jo = helper.getJsonFromRequest( request );
				} catch( Exception e ) {
					logger.debug( e.getMessage() );
				}
				
				/* Initialize transaction for saving resource */
				//request.initializeTransaction();
                request.setTransactional( true );
				
				method.invoke( r, request, writer, jo );
				
				helper.viewUnknownMethod( r, request, response, writer.toString() );
				
			} catch( Exception e ) {
				throw new ActionHandlerException( e );
			}

		} else {
			request.failTransaction();

			/* This is a call to the resource type and should be a static method */
			Method method = null;
			try {
				Class<AbstractResource> clazz = (Class<AbstractResource>) GraphDragon.getInstance().getResourceTypes().get( request.getURIParts()[2] ).getClazz();
				String m = "do" + request.getURIParts()[3].substring( 0, 1 ).toUpperCase() + request.getURIParts()[3].substring( 1 ).toLowerCase();
				method = clazz.getDeclaredMethod( m, Request.class );
				method.invoke( null, request );
			} catch( Exception e ) {
				throw new ActionHandlerException( e );
			}
		}
	}
	
	@VisibleAction
	public void view( Request request, HttpServletResponse response ) throws ActionHandlerException {
		Long id = null;
		try {
			AbstractResource r = helper.getResource( request, response );
			
			/* Initialize transaction for saving resource */
			request.initializeTransaction();
			
			logger.debug( "RENCODNFNFG: " + request.getCharacterEncoding() );

			/* Save the resource if post */
			if( request.isRequestPost() ) {
				if( request.canEdit( r ) ) {
					logger.debug( "Saving " + r );
					
					JsonObject jo;
					try {
						jo = helper.getJsonFromRequest( request );
					} catch( Exception e ) {
						logger.warn( e.getMessage() );
						throw new ResourceNotCreatedException( "The configuration did not contain a valid json object", e );
					}
	
					/* Save before view */
					r.doSave( request, jo );
				} else {
					logger.warn( request.getUser() + " is trying to edit " + r );
					if( request.isAuthenticated() ) {
						throw new NoAccessException( "No access for " + id );
					} else {
						throw new NotLoggedInExceptionException( "Not logged in for " + id );
					}
				}
			}

			/* View resource */
			helper.viewResource( r, request, response );
		} catch( Exception e ) {
			request.failTransaction();
			throw new ActionHandlerException( e );
		}
	}

	@VisibleAction
	public void configure( Request request, HttpServletResponse response ) throws ActionHandlerException {
		Long id = new Long( request.getURIParts()[2] );
		try {
			AbstractResource r = GraphDragon.getInstance().getResource( id );

			if( request.canEdit( r ) ) {
				helper.configureResource( request, response );
			} else {
				logger.warn( request.getUser() + " is trying to configure " + r );
				if( request.isAuthenticated() ) {
					throw new NoAccessException( "No access for " + id );
				} else {
					throw new NotLoggedInExceptionException( "Not logged in for " + id );
				}
			}

		} catch( Exception e ) {
			throw new ActionHandlerException( e );
		}
	}

	@VisibleAction
	public void create( Request request, HttpServletResponse response ) throws ActionHandlerException {
		String type = request.getURIParts()[2];
		logger.debug( "Type is " + type );
		logger.debug( "RENCODNFNFG: " + request.getCharacterEncoding() );
		try {
			if( request.isRequestPost() ) {
				AbstractResource r = helper.createResource( request, response );
				helper.viewResource( r, request, response );
			} else {
				helper.configureNewResource( request, response );
			}
			request.succeedTransaction();
		} catch( Exception e ) {
			request.failTransaction();
			throw new ActionHandlerException( e );
		}
	}

	public String getDisplayName() {
		return "Resource";
	}

	/* Top level menu item */
	public MenuItem getParent() {
		return null;
	}

}
