package org.seventyeight.web.handler;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.exceptions.NoSuchJsonElementException;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.model.*;
import org.seventyeight.web.util.ClassUtils;
import org.seventyeight.web.util.JsonUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author cwolfgang
 *         Date: 17-01-13
 *         Time: 14:10
 */
public class TopLevelActionHandler {

    private static Logger logger = Logger.getLogger( TopLevelActionHandler.class );

    // (0)/(1)handler/(2)first((3)second/(n)last
    // n is either an actions index or an action method

    public void execute( TopLevelAction topAction, Request request, HttpServletResponse response ) throws ActionHandlerException {

        if( topAction.execute( request, response ) ) {
            return;
        }

        /* Check for action first, start a first(2) */
        int i = 2;
        int l = request.getRequestParts().length;
        Action action = topAction;
        Action lastAction = null;
        String method = "index";
        for( ; i < l ; i++ ) {
            method = request.getRequestParts()[i];

            lastAction = action;

            if( action instanceof Actionable ) {
                action = ((Actionable)action).getAction( request, method );
            } else {
                /* Was not actionable, break */
                action = null;
                break;
            }

            /* If null, break */
            if( action == null ) {
                break;
            }
        }

        logger.debug( "[Action method] " + method + " -> " + action + "/" + lastAction );

        if( action != null ) {
            /* Last sub space was an action, call its index method */
            try {
                executeMethod( action, request, response, "index" );
            } catch( Exception e ) {
                throw new ActionHandlerException( e );
            }
        } else {
            if( i == l - 1 ) {
                /* We came to an end */

                if( !request.isRequestPost() ) {
                    /* First try to find a view, if not a POST */
                    try {
                        request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( lastAction, method + ".vm" ) );
                        response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
                        return;
                    } catch( TemplateDoesNotExistException e ) {
                        logger.warn( e );
                    } catch( IOException e ) {
                        throw new ActionHandlerException( e );
                    }
                }

                /* Then try to find a method */
                try {
                    logger.debug( "Action: " + lastAction + " -> " + method );
                    executeMethod( lastAction, request, response, method );
                } catch( Exception e ) {
                    throw new ActionHandlerException( e );
                }
            } else {
                throw new ActionHandlerException( method + " not defined for " + lastAction );
            }
        }

    }

    private void executeMethod( Action action, Request request, HttpServletResponse response, String actionMethod ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchJsonElementException {
        Method method = getRequestMethod( action, actionMethod, request.isRequestPost() );

        if( request.isRequestPost() ) {
            JsonObject json = null;
            try {
                json = JsonUtils.getJsonFromRequest( request );
            } catch ( Exception e ) {
                logger.debug( e.getMessage() );
            }
            method.invoke( action, request, response, json );
        } else {
            method.invoke( action, request, response );
        }
    }

    private Method getRequestMethod( Action action, String method, boolean post ) throws NoSuchMethodException {
        String m = "do" + method.substring( 0, 1 ).toUpperCase() + method.substring( 1, method.length() );
        logger.debug( "Method: " + method + " = " + m );
        if( post ) {
            //return resource.getClass().getDeclaredMethod( m, ParameterRequest.class, JsonObject.class );
            return ClassUtils.getEnheritedMethod( action.getClass(), m, Request.class, HttpServletResponse.class, JsonObject.class );
        } else {
            return action.getClass().getDeclaredMethod( m, Request.class, HttpServletResponse.class );
        }
    }
}
