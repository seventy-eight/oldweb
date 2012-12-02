package org.seventyeight.web.model;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;


public abstract class AbstractTopLevelActionHandler implements TopLevelAction {

	private Logger logger = Logger.getLogger( AbstractTopLevelActionHandler.class );
	
	/**
	 * The default behavior. Parts is of the form:
	 * (0)/(1)handler/(2)method/(3...)*
	 * @param parts
	 * @return
	 * @throws NoSuchMethodError
	 */
	public Method getMethod( String[] parts, Class<? extends TopLevelAction> clazz ) throws NoSuchMethodException {
		logger.debug( "PART[2]: " + parts[2] );
		Method method = clazz.getDeclaredMethod( parts[2], Request.class, HttpServletResponse.class );
		
		return method;
	}
	
	private static final Pattern rx_test_int = Pattern.compile( "^\\d+$" );
	
	public boolean isInt( String t ) {
		return rx_test_int.matcher( t ).find();
	}
	
	protected void createOutput( Request request, HttpServletResponse response, String template ) throws TemplateDoesNotExistException, IOException {
		response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( template ).get() );
	}
	
	protected void createOutput( Request request, HttpServletResponse response ) throws TemplateDoesNotExistException, IOException {
        response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( "" ).get() ); // OBS!!!
	}

}
