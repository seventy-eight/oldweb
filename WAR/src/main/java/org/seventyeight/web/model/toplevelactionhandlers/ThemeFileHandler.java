package org.seventyeight.web.model.toplevelactionhandlers;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.model.AbstractTheme;
import org.seventyeight.web.model.Action;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.TopLevelAction;
import org.seventyeight.web.util.FileHelper;
import org.seventyeight.web.util.GetFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;


//@TopLevelActionHandlerType
public class ThemeFileHandler implements TopLevelAction {
	
	private static Logger logger = Logger.getLogger( ThemeFileHandler.class );

    @Override
    public boolean execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        FileHelper fh = new FileHelper();
        try {
            fh.getFile( request, response, new GetThemeFile( request.getTheme() ), true );
        } catch( IOException e ) {
            throw new ActionHandlerException( e );
        }

        return true;
    }

    public String getUrlName() {
		return "theme";
	}

    @Override
    public Action getAction( Request request, String subSpace ) {
        return null;
    }

    private class GetThemeFile implements GetFile {

         AbstractTheme theme;

         public GetThemeFile( AbstractTheme theme ) {
             this.theme = theme;
         }

         public File getFile( HttpServletRequest request, HttpServletResponse response ) throws IOException {
             // Get requested file by path info.
             String requestedFile = request.getPathInfo();

             requestedFile = requestedFile.replaceFirst( "^/?.*?/", "" );

             logger.debug( "[Theme file] " + requestedFile );

             // Check if file is actually supplied to the request URL.
             if( requestedFile == null ) {
                 // Do your thing if the file is not supplied to the request URL.
                 // Throw an exception, or send 404, or show default/warning page, or
                 // just ignore it.
                 response.sendError( HttpServletResponse.SC_NOT_FOUND );
                 return null;
             }

             String filename = URLDecoder.decode( requestedFile, "UTF-8" );
             //return GraphDragon.getInstance().getRenderer().getStaticFile( filename );
             return SeventyEight.getInstance().getThemeFile( theme, filename );
         }

     }

}
