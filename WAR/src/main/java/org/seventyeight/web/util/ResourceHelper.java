package org.seventyeight.web.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.seventyeight.GraphDragon;
import org.seventyeight.exceptions.ActionHandlerException;
import org.seventyeight.exceptions.CouldNotLoadResourceException;
import org.seventyeight.exceptions.ErrorWhileSavingException;
import org.seventyeight.exceptions.InconsistentParameterException;
import org.seventyeight.exceptions.IncorrectTypeException;
import org.seventyeight.exceptions.MissingDescriptorException;
import org.seventyeight.exceptions.NoAccessException;
import org.seventyeight.exceptions.NoSuchJsonElementException;
import org.seventyeight.exceptions.NotLoggedInExceptionException;
import org.seventyeight.exceptions.ParameterDoesNotExistException;
import org.seventyeight.exceptions.ResourceDoesNotExistException;
import org.seventyeight.exceptions.ResourceNotCreatedException;
import org.seventyeight.exceptions.TemplateDoesNotExistException;
import org.seventyeight.exceptions.UnableToConfigureResourceException;
import org.seventyeight.exceptions.UnableToRenderOutputException;
import org.seventyeight.exceptions.UnableToRenderResourceException;
import org.seventyeight.model.Descriptor;
import org.seventyeight.model.AbstractItem;
import org.seventyeight.model.AbstractObject;
import org.seventyeight.model.AbstractResource;
import org.seventyeight.model.Configurable;
import org.seventyeight.model.Extension;
import org.seventyeight.model.RequestContext;
import org.seventyeight.web.Request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.CouldNotLoadResourceException;
import org.seventyeight.web.exceptions.NotFoundException;
import org.seventyeight.web.exceptions.TooManyException;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.Request;

public class ResourceHelper {
	
	private static Logger logger = Logger.getLogger( ResourceHelper.class );


	public void viewResource( AbstractResource resource, Request request, HttpServletResponse response ) throws NoAccessException, NotLoggedInExceptionException, UnableToRenderResourceException {
		if( request.hasAccess( resource ) ) {
			try {
				/* Initialize transaction for saving resource */
				request.initializeTransaction();
	
				logger.debug( "Prepare viewing" );
				resource.prepareView( request );
				
				logger.debug( "Viewing " + resource );
				
				/* Portrait */
				//request.getContext().put( "portrait", GraphDragon.getInstance().renderObject( new StringWriter(), resource.getPortrait(), "view.vm", request.getTheme(), request.getContext() ) );
				request.getContext().put( "portrait", resource.getPortrait() );
	
				request.getContext().put( "content", GraphDragon.getInstance().renderObject( new StringWriter(), resource, "view.vm", request.getTheme(), request.getContext() ) );
				request.getContext().put( "title", resource.getTitle() );
	
				createOutput( request, response );
	
				request.succeedTransaction();
			} catch( Exception e ) {
				request.failTransaction();
				throw new UnableToRenderResourceException( e.getClass().getCanonicalName() + ": " + e.getMessage() );
			}
		} else {
			logger.warn( request.getUser() + " is trying to access " + resource );
			if( request.isAuthenticated() ) {
				throw new NoAccessException( "No access for " + resource );
			} else {
				throw new NotLoggedInExceptionException( "Not logged in for " + resource );
			}
		}
	}
	
	public void viewUnknownMethod( AbstractResource resource, Request request, HttpServletResponse response, String content ) throws NoAccessException, NotLoggedInExceptionException, UnableToRenderResourceException {
		if( request.hasAccess( resource ) ) {
			try {
				/* Initialize transaction for saving resource */
				request.initializeTransaction();
	
				logger.debug( "Unknown method" + resource );
				
				request.getContext().put( "content", content );
				
				createOutput( request, response );
	
				request.succeedTransaction();
			} catch( Exception e ) {
				request.failTransaction();
				throw new UnableToRenderResourceException( e.getClass().getCanonicalName() + ": " + e.getMessage() );
			}
		} else {
			logger.warn( request.getUser() + " is trying to access " + resource );
			if( request.isAuthenticated() ) {
				throw new NoAccessException( "No access for " + resource );
			} else {
				throw new NotLoggedInExceptionException( "Not logged in for " + resource );
			}
		}
	}
	
	public void saveResource( AbstractResource resource, Request request, HttpServletResponse response ) throws ResourceDoesNotExistException, ParameterDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		/* Save resource */
		JsonObject jo;
		try {
			jo = getJsonFromRequest( request );
		} catch( Exception e ) {
			logger.warn( e.getMessage() );
			throw new ParameterDoesNotExistException( "The configuration did not contain a valid json object", e );
		}
		resource.doSave( request, jo );
		
		/* Save extensions */
		//List<Class<Extension>> list = GraphDragon.getInstance().getExtensions( resource.getc );
	}

	public void configureResource( Request request, HttpServletResponse response ) throws UnableToConfigureResourceException {
		Long id = new Long( request.getURIParts()[2] );
		try {
			AbstractResource r = GraphDragon.getInstance().getResource( id );

			if( request.canEdit( r ) ) {
				List<String> parts = new ArrayList<String>();
				getConfigureTemplate( r, parts, request );
				request.getContext().put( "parts", parts );
				
				/* Get configurable extensions */
				
				
				request.getContext().put( "content", GraphDragon.getInstance().render( "org/seventyeight/model/resources/outer_configure.vm", request.getTheme(), request.getContext() ) );
	
				createOutput( request, response );
			} else {
				logger.warn( request.getUser() + " is trying to configure " + r );
				if( request.isAuthenticated() ) {
					throw new NoAccessException( "No access for " + id );
				} else {
					throw new NotLoggedInExceptionException( "Not logged in for " + id );
				}
			}

		} catch( Exception e ) {
			throw new UnableToConfigureResourceException( e );
		}
	}

	public AbstractResource createResource( Request request, HttpServletResponse response ) throws ResourceNotCreatedException {
		String type = request.getURIParts()[2];
		logger.debug( "Type is " + type );
		try {
			/* We need the json object first to determine if this is a valid configuration */
			JsonObject jo;
			try {
				jo = getJsonFromRequest( request );
			} catch( Exception e ) {
				logger.warn( e.getMessage() );
				throw new ResourceNotCreatedException( "The configuration did not contain a valid json object", e );
			}
			
			
			/* Initialize transaction for creation */
			request.initializeTransaction();
			logger.debug( "Newing resource" );
			AbstractResource r = (AbstractResource) GraphDragon.getInstance().getDescriptor( type ).newInstance();
			logger.debug( "RESOURCE IS " + r );
			
			/* Set the owner */
			r.setOwner( request.getUser() );

			request.getContext().put( "identifier", r.getIdentifier() );

			logger.debug( "r: " + r.getIdentifier() );

			r.doSave( request, jo );
			request.succeedTransaction();
			
			return r;
		} catch( Exception e ) {
			request.failTransaction();
			throw new ResourceNotCreatedException( type, e );
		}
	}
	
	public void configureNewResource( Request request, HttpServletResponse response ) throws UnableToRenderOutputException {
		String type = request.getURIParts()[2];
		logger.debug( "Type is " + type );
		try {
			Descriptor<?> descriptor = GraphDragon.getInstance().getResourceTypes().get( request.getURIParts()[2] );
			
			if( descriptor == null ) {
				throw new MissingDescriptorException( "Could not find descriptor for " + request.getURIParts()[2] );
			}
			//String enc = (String) GraphDragon.getInstance().getResourceTypes().get( request.getURIParts()[2] ).getMethod( "getEncodingType" ).invoke( null );
	
			//request.getContext().put( "encoding", enc );
			request.getContext().put( "type", type );
			//request.getContext().put( "item", "" );
			request.getContext().put( "descriptor", descriptor );
			request.getContext().put( "abstractItem", AbstractItem.class );
			request.getContext().put( "class", GraphDragon.getInstance().getResourceTypes().get( request.getURIParts()[2] ) );
			request.getContext().put( "url", "/resource/" + request.getURIParts()[2] + "/create" );
			//List<String> parts = new ArrayList<String>();
			logger.debug( "BEFORE CONFIGURE!!!!" );
			String configs = configure( descriptor, request );
			
			//test2( GraphDragon.getInstance().getResourceTypes().get( request.getURIParts()[2] ), request, response );
			
			//request.getContext().put( "parts", parts );
			request.getContext().put( "configurations", configs );
	
			request.getContext().put( "content", GraphDragon.getInstance().render( "org/seventyeight/model/resources/outer_new.vm", request.getTheme(), request.getContext() ) );
			
			createOutput( request, response );
		} catch( Exception e ) {
			throw new UnableToRenderOutputException( e );
		}
	}
				
	public String configure( Descriptor<?> descriptor, Request request ) {
		logger.debug( "descriptor for configure: " + descriptor );
		
		List<Class<? extends Configurable>> list = GraphDragon.getInstance().getConfigurableExtensions( descriptor );
		
		//List<AbstractFactory<?>> factories = new ArrayList<AbstractFactory<?>>();
		List<String> cs = new ArrayList<String>();
		for( Class<? extends Configurable> c : list ) {
			cs.add( configure( GraphDragon.getInstance().getDescriptor( c ), request ) );
		}
		
		request.getContext().put( "configurations", cs );
				
		/* Configure the clazz */
		String result = configureClass( descriptor.getClazz(), request );
		
		//logger.debug( "OUTPUT RESULT: " + result );
		
		return result;
	}
	
	public List<String> getConfigureTemplate( AbstractResource resource, List<String> content, Request request ) {
		List<String> ts = GraphDragon.getInstance().getTemplateFile( resource, "configure.vm", -1 );
		
		for( String t : ts ) {
			logger.debug( "Template: " + t );
			try {
				content.add( GraphDragon.getInstance().render( new StringWriter(), resource, t, request.getTheme(), request.getContext() ).toString() );
			} catch( TemplateDoesNotExistException e ) {
				logger.debug( "No configure file for " + t );
			}
		}

		Collections.reverse( content );
		
		return content;
	}
	

	
	public String configureClass( Class<?> clazz, Request request ) {
		List<String> ts = GraphDragon.getInstance().getTemplateFile( clazz, "configure.vm", -1 );
		logger.debug( "Templates for class: " + clazz.getCanonicalName() );
		logger.debug( ts );
		
		StringBuilder sb = new StringBuilder();
		
		for( String t : ts ) {
			try {
				sb.append( GraphDragon.getInstance().render( t, request.getTheme(), request.getContext() ).toString() );
			} catch( TemplateDoesNotExistException e ) {
				logger.debug( e.getMessage() );
			}
		}
		
		logger.debug( "Out of loop" );
		
		try {
			VelocityContext c = new VelocityContext();
			c.put( "title", "config" );
			c.put( "configuration", sb.toString() );
			c.put( "class", clazz.getCanonicalName() );
			String r = GraphDragon.getInstance().render( "org/seventyeight/model/resources/configuration.vm", request.getTheme(), c ).toString();
			return r;
		} catch( TemplateDoesNotExistException e ) {
			logger.warn( "Could not generate form: " + e.getMessage() );
		}
		
		return "";
	}


	private void createOutput( Request request, HttpServletResponse response ) throws TemplateDoesNotExistException, IOException {
		response.getWriter().print( GraphDragon.getInstance().render( request.getTemplate(), GraphDragon.getInstance().getDefaultTheme(), request.getContext() ) );
	}
	
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
	
	/**
	 * Get the top most configuration json object from a request.
	 * @param request
	 * @return
	 * @throws NoSuchJsonElementException
	 */
	public JsonObject getJsonFromRequest( Request request ) throws NoSuchJsonElementException {
		String json = request.getParameter( "json" );
		JsonParser parser = new JsonParser();
		JsonObject jo = (JsonObject) parser.parse( json );
		logger.info( "JSON: " + request.getParameter( "json" ) );
		
		JsonElement e = jo.get( GraphDragon.__JSON_CONFIGURATION_NAME );
		if( e != null && e.isJsonObject() ) {
			return (JsonObject)e;
		} else {
			throw new NoSuchJsonElementException( "Could not find origin json configuration" );
		}
	}

}
