package org.seventyeight.web;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBManager;
import org.seventyeight.utils.StopWatch;
import org.seventyeight.web.authentication.exceptions.NoSuchUserException;
import org.seventyeight.web.authentication.exceptions.PasswordDoesNotMatchException;
import org.seventyeight.web.authentication.exceptions.UnableToCreateSessionException;
import org.seventyeight.web.exceptions.GizmoHandlerDoesNotExistException;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.model.*;
import org.seventyeight.web.servlet.Request;
import org.seventyeight.web.servlet.Response;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * User: cwolfgang
 * Date: 15-11-12
 * Time: 22:22
 */
//@MultipartConfig
@WebServlet( asyncSupported = true )
public class Rest extends HttpServlet {

    private static Logger logger = Logger.getLogger( Rest.class );

    @Override
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        doRequest( request, response );
    }

    @Override
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        doRequest( request, response );
    }

    public void doRequest( HttpServletRequest rqs, HttpServletResponse rsp ) throws ServletException, IOException {
        //PrintWriter out = response.getWriter();

        StopWatch sw = new StopWatch();
        sw.reset();

        sw.start( "preparing" );

        logger.debug( "Query  : " + rqs.getQueryString() );
        logger.debug( "URI    : " + rqs.getRequestURI() );
        logger.debug( "METHOD : " + rqs.getMethod() );

        /* Instantiating request */
        Request request = new Request( rqs );
        Response response = new Response( rsp );

        logger.debug( "[Parameters] " + rqs.getParameterMap() );

        /* Instantiating context */
        VelocityContext vc = new VelocityContext();
        vc.put( "title", "" );

        request.setContext( vc );
        request.getContext().put( "request", request );
        request.setRequestParts( rqs.getRequestURI().split( "/" ) );
        logger.debug( "------ " + Arrays.asList( request.getRequestParts() ) + " ------" );

        TopLevelGizmo action = null;
        try {
            action = SeventyEight.getInstance().getTopLevelGizmo( request.getRequestParts()[1] );
        } catch( GizmoHandlerDoesNotExistException e ) {
            generateException( request, rsp.getWriter(), e, e.getMessage() );
            return;
        }
        sw.stop();

        sw.start( action.getUrlName() );


        /**/
        request.setDB( OrientDBManager.getInstance().getDatabase() );
        vc.put( "database", request.getDB() );

        vc.put( "currentUrl", rqs.getRequestURI() );

        request.setUser( SeventyEight.getInstance().getAnonymousUser() );
        request.setTheme( SeventyEight.getInstance().getDefaultTheme() );

        try {
            logger.debug( "AUTHENTICATING" );
            SeventyEight.getInstance().getAuthentication().authenticate( request, response );
        } catch( PasswordDoesNotMatchException e ) {
            logger.debug( "Passwords does not match!" );
        } catch( NoSuchUserException e ) {
            logger.debug( "User does not exist!" );
        } catch( UnableToCreateSessionException e ) {
            logger.debug( "Could not create session!" );
        }


        try {
            parseRequest( action, request, response );
        } catch( Exception e ) {
            e.printStackTrace();
            generateException( request, rsp.getWriter(), e, e.getMessage() );
        }

        /* Close db connection */
        request.getDB().close();

        sw.stop();
        logger.info( sw.print( 1000 ) );
    }

    public void parseRequest( TopLevelGizmo gizmo, Request request, Response response ) throws GizmoHandlerDoesNotExistException, ActionHandlerException {
        logger.debug( "Parsing request" );
        SeventyEight.getInstance().getTopLevelActionHandler().execute( gizmo, request, response );
    }

    private void generateException( Request request, PrintWriter writer, Throwable e, String message ) {
        logger.error( e.getMessage() );
        try {
            VelocityContext vc = new VelocityContext();
            vc.put( "stacktrace", e.getStackTrace() );
            vc.put( "message", message );

            /*
               System.out.println( "ERROR context: " +vc );
               for( Object o : vc.getKeys() ) {
                   System.out.println( o.toString() + " = " + vc.get( o.toString() ) );
               }
               */

            org.seventyeight.web.model.Error error = new org.seventyeight.web.model.Error( (Exception)e );

            request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).setContext( vc ).renderObject( error, "view.vm" ) );
            request.getContext().put( "title", message );
            writer.print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( "org/seventyeight/web/main.vm" ) );
        } catch( Exception ec ) {
            request.getContext().put( "content", "Error while displaying exception" );
        }
    }

}
