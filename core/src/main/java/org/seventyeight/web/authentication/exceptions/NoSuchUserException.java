package org.seventyeight.web.authentication.exceptions;

import org.seventyeight.web.exceptions.SeventyEightException;

public class NoSuchUserException extends SeventyEightException {

	public NoSuchUserException( String s ) {
		super( s );
	}

}
