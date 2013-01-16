package org.seventyeight.web.exceptions;

public class DescribableException extends SeventyEightException {
	public DescribableException( String s, Exception e ) {
		super( s, e );
	}
}
