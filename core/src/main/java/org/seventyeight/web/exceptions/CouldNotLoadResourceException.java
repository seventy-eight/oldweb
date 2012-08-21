package org.seventyeight.web.exceptions;

public class CouldNotLoadResourceException extends SeventyEightException {

	public CouldNotLoadResourceException( String s ) {
		super( s );
	}
	
	public CouldNotLoadResourceException( String s, Exception e ) {
		super( s, e );
	}

}
