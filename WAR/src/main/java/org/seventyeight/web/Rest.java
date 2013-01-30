package org.seventyeight.web;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBManager;
import org.seventyeight.utils.StopWatch;
import org.seventyeight.web.authentication.Authentication;
import org.seventyeight.web.authentication.SimpleAuthentication;
import org.seventyeight.web.authentication.exceptions.NoSuchUserException;
import org.seventyeight.web.authentication.exceptions.PasswordDoesNotMatchException;
import org.seventyeight.web.authentication.exceptions.UnableToCreateSessionException;
import org.seventyeight.web.exceptions.GizmoHandlerDoesNotExistException;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.model.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public void doRequest( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        StopWatch sw = new StopWatch();
        sw.reset();

        sw.start( "preparing" );

        logger.debug( "Query  : " + request.getQueryString() );
        logger.debug( "URI    : " + request.getRequestURI() );
        logger.debug( "METHOD : " + request.getMethod() );
        logger.debug( "CONTENT: " + request.getContentType() );
        logger.debug( "LENGTH : " + request.getContentLength() );

        /* Instantiating request */
        Request r = null;
        if( Request.isMultipart( request ) ) {
            logger.debug( "WAS MULTI PART" );
           r = new MultiPartRequest( request );
        } else {
            logger.debug( "WAS NOT MULTI PART" );
            r = new Request( request );
        }

        logger.debug( "[Parameters] " + request.getParameterMap() );

        /* Instantiating context */
        VelocityContext vc = new VelocityContext();
        vc.put( "title", "" );

        r.setContext( vc );
        r.getContext().put( "request", r );
        r.setRequestParts( request.getRequestURI().split( "/" ) );
        logger.debug( "------ " + Arrays.asList( r.getRequestParts() ) + " ------" );

        TopLevelGizmo action = null;
        try {
            action = SeventyEight.getInstance().getTopLevelGizmo( r.getRequestParts()[1] );
        } catch( GizmoHandlerDoesNotExistException e ) {
            generateException( r, out, e, e.getMessage() );
            return;
        }
        sw.stop();

        sw.start( action.getUrlName() );


        /**/
        r.setDB( OrientDBManager.getInstance().getDatabase() );
        vc.put( "database", r.getDB() );

        vc.put( "currentUrl", request.getRequestURI() );

        r.setUser( SeventyEight.getInstance().getAnonymousUser() );
        r.setTheme( SeventyEight.getInstance().getDefaultTheme() );

        Authentication auth = new SimpleAuthentication();
        try {
            logger.debug( "AUTHENTICATING" );
            auth.authenticate( r, response );
        } catch( PasswordDoesNotMatchException e ) {
            logger.debug( "Passwords does not match!" );
        } catch( NoSuchUserException e ) {
            logger.debug( "User does not exist!" );
        } catch( UnableToCreateSessionException e ) {
            logger.debug( "Could not create session!" );
        }


        try {
            parseRequest( action, r, response );
        } catch( Exception e ) {
            e.printStackTrace();
            generateException( r, out, e, e.getMessage() );
        }

        /* Close db connection */
        r.getDB().close();

        sw.stop();
        logger.info( sw.print( 1000 ) );
    }

    public void parseRequest( TopLevelGizmo gizmo, Request request, HttpServletResponse response ) throws GizmoHandlerDoesNotExistException, ActionHandlerException {
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
