package org.seventyeight.web.exceptions;

public class PersistenceException extends SeventyEightException {

	public PersistenceException( String s, Exception e ) {
		super( s, e );
	}

    public PersistenceException( String s ) {
        super( s );
    }

    public PersistenceException( Exception e ) {
        super( e );
    }
}
