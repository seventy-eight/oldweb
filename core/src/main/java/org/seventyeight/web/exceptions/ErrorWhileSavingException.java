package org.seventyeight.web.exceptions;

public class ErrorWhileSavingException extends SeventyEightException {

	public ErrorWhileSavingException( String s ) {
		super( s );
	}

	public ErrorWhileSavingException( Exception e ) {
		super( e );
	}
}
