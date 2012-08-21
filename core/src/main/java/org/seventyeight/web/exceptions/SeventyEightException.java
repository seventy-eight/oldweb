package org.seventyeight.web.exceptions;

public class SeventyEightException extends Exception {

	public SeventyEightException( String s ) {
		super( s );
	}

	public SeventyEightException( String m, Exception e ) {
		super( m, e );
	}
	
	public SeventyEightException( Exception e ) {
		super( e );
	}
}
