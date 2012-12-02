package org.seventyeight.web;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.seventyeight.utils.StopWatch;
import org.seventyeight.web.model.Request;

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

        r.setUser( SeventyEight.getInstance().getAnonymousUser() );

        /* TODO authentication */

        r.setRequestParts( request.getRequestURI().split( "/" ) );
        logger.debug( "------ " + Arrays.asList( r.getRequestParts() ) + " ------" );

    }

    public void parseRequest( Request request, HttpServletResponse response ) {
        logger.debug( "Parsing request" );
        String[] parts = request.getRequestParts();
    }

}
