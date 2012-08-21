package org.seventyeight.web.exceptions;

public class TemplateDoesNotExistException extends SeventyEightException {

	public TemplateDoesNotExistException( String s ) {
		super( s );
	}

	public TemplateDoesNotExistException( Exception e ) {
		super( e );
	}
}
