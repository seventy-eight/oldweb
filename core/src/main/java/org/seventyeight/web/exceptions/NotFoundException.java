package org.seventyeight.web.exceptions;

public class NotFoundException extends SeventyEightException {

    public NotFoundException( String s, Exception e ) {
        super( s, e );
    }

    public NotFoundException( Exception e ) {
        super( e );
    }

	public NotFoundException( String s ) {
		super( s );
	}

}
