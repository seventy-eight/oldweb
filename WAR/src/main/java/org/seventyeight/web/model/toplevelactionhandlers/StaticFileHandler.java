package org.seventyeight.web.model.toplevelactionhandlers;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.servlet.Request;
import org.seventyeight.web.model.TopLevelExecutor;
import org.seventyeight.web.servlet.Response;
import org.seventyeight.web.util.FileHelper;
import org.seventyeight.web.util.GetFile;


//@TopLevelActionHandlerType
public class StaticFileHandler implements TopLevelExecutor {
	
	private static Logger logger = Logger.getLogger( StaticFileHandler.class );

    @Override
    public void execute( Request request, Response response ) throws ActionHandlerException {
        FileHelper fh = new FileHelper();
        try {
            fh.getFile( request, response, new S(), true );
        } catch( IOException e ) {
            throw new ActionHandlerException( e );
        }
    }

    public String getUrlName() {
		return "static";
	}


    private class S implements GetFile {

         public File getFile( HttpServletRequest request, Response response ) throws IOException {
             // Get requested file by path info.
             String requestedFile = request.getPathInfo();

             requestedFile = requestedFile.replaceFirst( "^/?.*?/", "" );

             logger.debug( "--------------------> " + requestedFile + " <------------------------" );

             // Check if file is actually supplied to the request URL.
             if( requestedFile == null ) {
                 // Do your thing if the file is not supplied to the request URL.
                 // Throw an exception, or send 404, or show default/warning page, or
                 // just ignore it.
                 response.sendError( Response.SC_NOT_FOUND );
                 return null;
             }

             String filename = URLDecoder.decode( requestedFile, "UTF-8" );
             logger.debug( "HERE!!!!" );
             //return GraphDragon.getInstance().getRenderer().getStaticFile( filename );
             return SeventyEight.getInstance().getTemplateManager().getStaticFile( filename );
         }

     }

}
