package org.seventyeight.web.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.*;
import org.apache.log4j.Logger;

import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

public class ResourceHelper {
	
	private static Logger logger = Logger.getLogger( ResourceHelper.class );


	public AbstractResource getResource( Request request, HttpServletResponse response ) throws CouldNotLoadItemException, TooManyException, NotFoundException {
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
				throw new CouldNotLoadItemException( "Unable to find resource[" + s + "]: " + e1.getMessage());
			}
		}

		return r;
	}

    public AbstractResource getResource( Request request, String idx ) throws CouldNotLoadItemException, TooManyException, NotFoundException {
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
                throw new CouldNotLoadItemException( "Unable to find resource[" + s + "]: " + e1.getMessage());
            }
        }

        return r;
    }

    public void configureResource( Request request, HttpServletResponse response, AbstractResource resource, ResourceDescriptor descriptor ) throws ActionHandlerException {
        logger.debug( "Configuring " + resource );

        //ResourceDescriptor descriptor = (ResourceDescriptor) resource.getDescriptor();

        request.getContext().put( "url", "/resource/" + resource.getIdentifier() );
        request.getContext().put( "class", descriptor.getClazz().getName() );
        request.getContext().put( "header", "Configuring " + resource.getDisplayName() );
        request.getContext().put( "descriptor", descriptor );

        /* Required javascrips */
        request.getContext().put( "javascript", descriptor.getRequiredJavascripts() );

        try {
            /* Special dual side */
            try {
                request.getContext().put( "dualSide", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderClass( descriptor.getClazz(), "dualSide.vm" ) );
            } catch ( TemplateDoesNotExistException e ) {
                /* No op */
                logger.debug( "Dual side not defined" );
            }

            /* Options */
            logger.fatal( "NU ER VI HER " + request.getContext().get( "item" ) );
            request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( resource, "configure.vm" ) );
            response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
        } catch( TemplateDoesNotExistException e ) {
            /* This solution does not work */
            logger.warn( e );
        } catch( IOException e ) {
            throw new ActionHandlerException( e );
        }
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

            r.save( (CoreRequest) request, jo );
            //request.succeedTransaction();

            return r;
        } catch( Exception e ) {
            //request.failTransaction();
            throw new ResourceNotCreatedException( ((ResourceDescriptor)descriptor).getType(), e );
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
