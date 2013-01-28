package org.seventyeight.web.exceptions;

public class CouldNotLoadObjectException extends PersistenceException {

	public CouldNotLoadObjectException( String s ) {
		super( s );
	}

	public CouldNotLoadObjectException( String s, Exception e ) {
		super( s, e );
	}

}
