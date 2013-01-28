package org.seventyeight.web.exceptions;

public class UnableToInstantiateObjectException extends PersistenceException {

	public UnableToInstantiateObjectException( String s, Exception e ) {
		super( s, e );
	}

}
