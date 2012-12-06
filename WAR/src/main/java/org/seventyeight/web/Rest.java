package org.seventyeight.web;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBManager;
import org.seventyeight.utils.StopWatch;
import org.seventyeight.web.exceptions.ActionHandlerDoesNotExistException;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.model.*;

import javax.servlet.ServletException;
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
        sw.start();

        /* Instantiating context */
        VelocityContext vc = new VelocityContext();
        vc.put( "title", "" );

        /* Instantiating request */
        Request r = new Request( request );
        r.setContext( vc );
        r.getContext().put( "request", r );


        /**/
        r.setDB( OrientDBManager.getInstance().getDatabase() );

        r.setUser( SeventyEight.getInstance().getAnonymousUser() );

        /* TODO authentication */

        r.setRequestParts( request.getRequestURI().split( "/" ) );
        logger.debug( "------ " + Arrays.asList( r.getRequestParts() ) + " ------" );

        try {
            parseRequest( r, response );
        } catch( Exception e ) {
            e.printStackTrace();
            generateException( r, out, e, e.getMessage() );
        }

    }

    public void parseRequest( Request request, HttpServletResponse response ) throws ActionHandlerDoesNotExistException, ActionHandlerException {
        logger.debug( "Parsing request" );
        String[] parts = request.getRequestParts();

        TopLevelAction action = SeventyEight.getInstance().getTopLevelAction( parts[1] );
        action.prepare( request );
        action.execute( request, response );
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

            request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer().setContext( vc ).renderObject( error, "view.vm" ).get() );
            request.getContext().put( "title", message );
            writer.print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( "org/seventyeight/web/main.vm" ).get() );
        } catch( Exception ec ) {
            request.getContext().put( "content", "Error while displaying exception" );
        }
    }

}
