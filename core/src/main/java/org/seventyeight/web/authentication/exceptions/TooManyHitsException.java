package org.seventyeight.web.authentication.exceptions;

import org.seventyeight.exceptions.BlueDragonException;

public class TooManyHitsException extends BlueDragonException {

	public TooManyHitsException( String s ) {
		super( s );
	}

}
