package org.seventyeight.web.model.toplevelactionhandlers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.seventyeight.GraphDragon;
import org.seventyeight.annotations.TopLevelActionHandlerType;
import org.seventyeight.annotations.VisibleAction;
import org.seventyeight.exceptions.ActionHandlerException;
import org.seventyeight.model.TopLevelActionHandler;
import org.seventyeight.web.Request;
import org.seventyeight.web.model.AbstractTopLevelActionHandler;
import org.seventyeight.web.util.FileHelper;
import org.seventyeight.web.util.GetFile;


@TopLevelActionHandlerType
public class StaticFileHandler extends AbstractTopLevelActionHandler {
	
	private static Logger logger = Logger.getLogger( StaticFileHandler.class );
	
	@Override
	public Method getMethod( String[] parts, Class<? extends TopLevelActionHandler> clazz ) throws NoSuchMethodException {
		return clazz.getDeclaredMethod( "get", Request.class, HttpServletResponse.class );
	}

	public String getName() {
		return "static";
	}
	
	@VisibleAction
	public void get( Request request, HttpServletResponse response ) throws ActionHandlerException {
		FileHelper fh = new FileHelper();
		try {
			fh.getFile( request, response, new S(), true );
		} catch( IOException e ) {
			throw new ActionHandlerException( e );
		}
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
			return GraphDragon.getInstance().getRenderer().getStaticFile( filename );
		}
		
	}
}
