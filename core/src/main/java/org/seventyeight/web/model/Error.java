package org.seventyeight.web.model;

public class Error {
	private Exception e; 
	public Error( Exception e ) {
		this.e = e;
	}
	
	public StackTraceElement[] getStackTrace() {
		return e.getStackTrace();
	}
	
	public String getMessage() {
		return e.getMessage();
	}
}
