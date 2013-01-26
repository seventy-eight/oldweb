package org.seventyeight.web.exceptions;

public class TooManyException extends CouldNotLoadResourceException {

    public TooManyException( String s, Exception e ) {
        super( s, e );
    }

	public TooManyException( String s ) {
		super( s );
	}

}
