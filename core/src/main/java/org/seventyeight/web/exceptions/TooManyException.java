package org.seventyeight.web.exceptions;

public class TooManyException extends SeventyEightException {

    public TooManyException( String s, Exception e ) {
        super( s, e );
    }

    public TooManyException( Exception e ) {
        super( e );
    }

	public TooManyException( String s ) {
		super( s );
	}

}
