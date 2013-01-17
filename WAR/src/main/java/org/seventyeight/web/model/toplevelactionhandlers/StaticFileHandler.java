package org.seventyeight.web.model.toplevelactionhandlers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.model.Action;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.TopLevelAction;
import org.seventyeight.web.util.FileHelper;
import org.seventyeight.web.util.GetFile;


//@TopLevelActionHandlerType
public class StaticFileHandler implements TopLevelAction {
	
	private static Logger logger = Logger.getLogger( StaticFileHandler.class );

    @Override
    public boolean execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        FileHelper fh = new FileHelper();
        try {
            fh.getFile( request, response, new S(), true );
        } catch( IOException e ) {
            throw new ActionHandlerException( e );
        }

        return true;
    }

    public String getName() {
		return "static";
	}

    @Override
    public Action getAction( String subSpace ) {
        return null;
    }

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
             //return GraphDragon.getInstance().getRenderer().getStaticFile( filename );
             return SeventyEight.getInstance().getTemplateManager().getStaticFile( filename );
         }

     }

}
