package org.seventyeight.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.CouldNotLoadResourceException;
import org.seventyeight.web.exceptions.NotFoundException;
import org.seventyeight.web.exceptions.TooManyException;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.Request;

public class ResourceHelper {
	
	private static Logger logger = Logger.getLogger( ResourceHelper.class );


	public AbstractResource getResource( Request request, HttpServletResponse response ) throws CouldNotLoadResourceException, TooManyException, NotFoundException {
		Long id = null;
		AbstractResource r = null;
		try {
			id = new Long( request.getRequestParts()[2] );
			r = SeventyEight.getInstance().getResource( request.getDB(), id );

		} catch( NumberFormatException e ) {
			/* This is an identifier, let's try the title */
			String s = "";
			try {
				s = URLDecoder.decode( request.getRequestParts()[4], "UTF-8" );
				logger.debug( "Finding " + s );
				//r = SeventyEight.getInstance().getResourceByTitle( s );
			} catch( UnsupportedEncodingException e1 ) {
				logger.warn( s + " not found" );
				throw new CouldNotLoadResourceException( "Unable to find resource[" + s + "]: " + e1.getMessage());
			}
		}

		return r;
	}
}
