package org.seventyeight.web.exceptions;

public class CouldNotLoadItemException extends SeventyEightException {

	public CouldNotLoadItemException( String s ) {
		super( s );
	}
	
	public CouldNotLoadItemException( String s, Exception e ) {
		super( s, e );
	}

}
