package org.seventyeight.web.authentication.exceptions;

import org.seventyeight.web.exceptions.SeventyEightException;

public class PasswordDoesNotMatchException extends SeventyEightException {

	public PasswordDoesNotMatchException( String s ) {
		super( s );
	}

}
