package org.seventyeight.web.authentication.exceptions;

import org.seventyeight.web.exceptions.SeventyEightException;

public class TooManyHitsException extends SeventyEightException {

	public TooManyHitsException( String s ) {
		super( s );
	}

}
