package org.seventyeight.web.handler;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.exceptions.NoSuchJsonElementException;
import org.seventyeight.web.model.*;
import org.seventyeight.web.util.ClassUtils;
import org.seventyeight.web.util.JsonUtils;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author cwolfgang
 *         Date: 17-01-13
 *         Time: 14:10
 */
public class TopLevelGizmoHandler {

    private static Logger logger = Logger.getLogger( TopLevelGizmoHandler.class );

    // (0)/(1)handler/(2)first((3)second/(n)last
    // n is either an actions index or an action method

    private void handleItemType( ItemType type, Request request ) {
        if( request.getRequestParts().length > 2 ) {
            String name = request.getRequestParts()[2];
            AbstractItem item = type.getItem( name, request.getDB() );
            request.getContext().put( "title", item.getDisplayName() );

        } else {
            /* TODO, what? */
        }
    }

    public void actions( Actionable actionable, int uriStart, Request request, HttpServletResponse response ) throws ActionHandlerException {

        int i = uriStart;
        int l = request.getRequestParts().length;
        Action action = null;
        Action lastAction = null;
        String urlName = "index";
        for( ; i < l ; i++ ) {
            urlName = request.getRequestParts()[i];
            logger.debug( "Url name is " + urlName );

            lastAction = action;
            action = null;

            for( Action a : actionable.getActions() ) {
                logger.debug( "Action is " + a );
                if( a.getUrlName().equals( urlName ) ) {
                    action = a;
                    break;
                }
            }

            if( action == null ) {
                logger.debug( "Action was null, breaking" );
                break;
            }

            if( action instanceof Actionable ) {
                actionable = (Actionable) action;
            } else {
                break;
            }
        }

        logger.debug( "[Action method] " + urlName + " -> " + action + "/" + lastAction );

        if( action != null ) {
            /* Last sub space was an action, do its index method */
            logger.debug( "Action was NOT null" );
            executeThing( request, response, action, "index" );
        } else {
            if( i == l - 1 ) {
                /* We came to an end */
                logger.debug( "Action was null" );
                executeThing( request, response, lastAction, urlName );

            } else {
                throw new ActionHandlerException( urlName + " not defined for " + lastAction );
            }
        }

    }

    private void executeThing( Request request, HttpServletResponse response, Item item, String urlName ) throws ActionHandlerException {
        if( !request.isRequestPost() ) {
            /* First try to find a view, if not a POST */
            try {
                logger.debug( "Item: " + item + " -> " + urlName );
                executeMethod( item, request, response, urlName );
                return;
            } catch( Exception e ) {
                logger.debug( e.getMessage() );
            }

            logger.debug( "TRYING VIEW FILE" );

            try {
                request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( item, urlName + ".vm" ) );
                response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
                return;
            } catch( Exception e ) {
                throw new ActionHandlerException( e );
            }
        }
    }

    private void executeMethod( Item item, Request request, HttpServletResponse response, String actionMethod ) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchJsonElementException {
        Method method = getRequestMethod( item, actionMethod, request.isRequestPost() );

        if( request.isRequestPost() ) {
            JsonObject json = null;
            try {
                json = JsonUtils.getJsonFromRequest( request );
            } catch ( Exception e ) {
                logger.debug( e.getMessage() );
            }
            method.invoke( item, request, response, json );
        } else {
            method.invoke( item, request, response );
        }
    }

    private Method getRequestMethod( Item item, String method, boolean post ) throws NoSuchMethodException {
        String m = "do" + method.substring( 0, 1 ).toUpperCase() + method.substring( 1, method.length() );
        logger.debug( "Method: " + method + " = " + m );
        if( post ) {
            //return resource.getClass().getDeclaredMethod( m, ParameterRequest.class, JsonObject.class );
            return ClassUtils.getEnheritedMethod( item.getClass(), m, Request.class, HttpServletResponse.class, JsonObject.class );
        } else {
            //return action.getClass().getDeclaredMethod( m, Request.class, HttpServletResponse.class );
            return ClassUtils.getEnheritedMethod( item.getClass(), m, Request.class, HttpServletResponse.class );
        }
    }
}
