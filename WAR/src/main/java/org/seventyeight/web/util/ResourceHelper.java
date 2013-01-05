package org.seventyeight.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.*;
import org.apache.log4j.Logger;

import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.Descriptor;
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

    public AbstractResource getResource( Request request, String idx ) throws CouldNotLoadResourceException, TooManyException, NotFoundException {
        Long id = null;
        AbstractResource r = null;
        try {
            id = new Long( idx );
            r = SeventyEight.getInstance().getResource( request.getDB(), id );

        } catch( NumberFormatException e ) {
            /* This is an identifier, let's try the title */
            String s = "";
            try {
                s = URLDecoder.decode( idx, "UTF-8" );
                logger.debug( "Finding " + s );
                //r = SeventyEight.getInstance().getResourceByTitle( s );
            } catch( UnsupportedEncodingException e1 ) {
                logger.warn( s + " not found" );
                throw new CouldNotLoadResourceException( "Unable to find resource[" + s + "]: " + e1.getMessage());
            }
        }

        return r;
    }

    public AbstractResource createResource( Descriptor descriptor, Request request, HttpServletResponse response ) throws ResourceNotCreatedException {
        try {
            /* We need the json object first to determine if this is a valid configuration */
            JsonObject jo = null;
            try {
                jo = getJsonFromRequest( request );
            } catch( Exception e ) {
                logger.warn( e.getMessage() );
                // throw new ResourceNotCreatedException( "The configuration did not contain a valid json object", e );
            }


            /* Initialize transaction for creation */
            //request.initializeTransaction();
            logger.debug( "Newing resource" );
            AbstractResource r = (AbstractResource) descriptor.newInstance( request.getDB() );
            logger.debug( "RESOURCE IS " + r );

            /* Set the owner */
            r.setOwner( request.getUser() );

            request.getContext().put( "identifier", r.getIdentifier() );

            logger.debug( "r: " + r.getIdentifier() );

            r.save( request, jo );
            //request.succeedTransaction();

            return r;
        } catch( Exception e ) {
            //request.failTransaction();
            throw new ResourceNotCreatedException( descriptor.getType(), e );
        }
    }


    /**
     * Get the top most configuration json object from a request.
     * @param request
     * @return
     * @throws org.seventyeight.web.exceptions.NoSuchJsonElementException
     */
    public JsonObject getJsonFromRequest( Request request ) throws NoSuchJsonElementException {
        String json = request.getParameter( "json" );
        //logger.debug( "JSON: " + json );
        if( json == null ) {
            throw new NoSuchJsonElementException( "Null" );
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println( gson.toJson( json ) );

        JsonParser parser = new JsonParser();
        JsonObject jo = (JsonObject) parser.parse( json );
        return jo;

        /*
        JsonElement e = jo.get( SeventyEight.__JSON_CONFIGURATION_NAME );
        if( e != null && e.isJsonObject() ) {
            return (JsonObject)e;
        } else {
            throw new NoSuchJsonElementException( "Could not find origin json configuration" );
        }
        */
    }
}
