package org.seventyeight.web.model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class Request extends HttpServletRequestWrapper {

	public Request( HttpServletRequest r ) {
		super( r );
	}
	
	
	public T getValue( String key ) {
		this.getParameter( "" );
		if( containsKey( key ) ) {
			return get( key )[0];
		} else {
			return null;
		}
	}
}
