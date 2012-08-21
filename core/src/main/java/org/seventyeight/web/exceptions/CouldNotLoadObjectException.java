package org.seventyeight.web.exceptions;

public class CouldNotLoadObjectException extends SeventyEightException {

	public CouldNotLoadObjectException( String s ) {
		super( s );
	}

	public CouldNotLoadObjectException( String s, Exception e ) {
		super( s, e );
	}

}
