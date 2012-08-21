package org.seventyeight.web.exceptions;

public class UnableToRenderOutputException extends SeventyEightException {

	public UnableToRenderOutputException( String s ) {
		super( s );
	}

	public UnableToRenderOutputException( Exception e ) {
		super( e );
	}

	public UnableToRenderOutputException( String s, Exception e ) {
		super( s, e );
	}
}
