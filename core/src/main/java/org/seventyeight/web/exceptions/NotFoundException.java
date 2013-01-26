package org.seventyeight.web.exceptions;

public class NotFoundException extends CouldNotLoadItemException {

    public NotFoundException( String s, Exception e ) {
        super( s, e );
    }

	public NotFoundException( String s ) {
		super( s );
	}

}
