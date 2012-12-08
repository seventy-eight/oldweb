package org.seventyeight.web.authentication.exceptions;

import org.seventyeight.exceptions.BlueDragonException;

public class PasswordDoesNotMatchException extends BlueDragonException {

	public PasswordDoesNotMatchException( String s ) {
		super( s );
	}

}
