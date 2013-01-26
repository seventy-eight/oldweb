package org.seventyeight.web.exceptions;

public class NotFoundException extends CouldNotLoadResourceException {

    public NotFoundException( String s, Exception e ) {
        super( s, e );
    }

	public NotFoundException( String s ) {
		super( s );
	}

}
