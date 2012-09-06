package org.seventyeight.web.exceptions;

public class IllegalStateRuntimeException extends RuntimeException {

	public IllegalStateRuntimeException( String s ) {
		super( s );
	}

	public IllegalStateRuntimeException( String m, Exception e ) {
		super( m, e );
	}
	
	public IllegalStateRuntimeException( Exception e ) {
		super( e );
	}
}
