package org.seventyeight.web.authentication.exceptions;

import org.seventyeight.web.exceptions.SeventyEightException;

public class UnableToCreateSessionException extends SeventyEightException {

	public UnableToCreateSessionException( Exception e ) {
		super( e );
	}

}
