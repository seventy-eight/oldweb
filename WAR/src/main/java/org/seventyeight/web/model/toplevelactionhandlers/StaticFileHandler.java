package org.seventyeight.web.model.toplevelactionhandlers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.TopLevelAction;


//@TopLevelActionHandlerType
public class StaticFileHandler implements TopLevelAction {
	
	private static Logger logger = Logger.getLogger( StaticFileHandler.class );

    @Override
    public void execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
		return "static";
	}

    @Override
    public void prepare( Request request ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /*
     private class S implements GetFile {

         public File getFile( HttpServletRequest request, HttpServletResponse response ) throws IOException {
             // Get requested file by path info.
             String requestedFile = request.getPathInfo();

             requestedFile = requestedFile.replaceFirst( "^/?.*?/", "" );

             logger.debug( "--------------------> " + requestedFile + " <------------------------" );

             // Check if file is actually supplied to the request URL.
             if( requestedFile == null ) {
                 // Do your thing if the file is not supplied to the request URL.
                 // Throw an exception, or send 404, or show default/warning page, or
                 // just ignore it.
                 response.sendError( HttpServletResponse.SC_NOT_FOUND );
                 return null;
             }

             String filename = URLDecoder.decode( requestedFile, "UTF-8" );
             logger.debug( "HERE!!!!" );
             return GraphDragon.getInstance().getRenderer().getStaticFile( filename );
         }

     }
     */
}
