package org.seventyeight.web.exceptions;

public class TooManyException extends CouldNotLoadItemException {

    public TooManyException( String s, Exception e ) {
        super( s, e );
    }

	public TooManyException( String s ) {
		super( s );
	}

}
