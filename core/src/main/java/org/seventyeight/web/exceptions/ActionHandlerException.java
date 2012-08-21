package org.seventyeight.web.exceptions;

public class ActionHandlerException extends SeventyEightException {

	public ActionHandlerException( String s ) {
		super( s );
	}
	
	public ActionHandlerException( Exception e ) {
		super( e );
	}

}
