package org.seventyeight.web.exceptions;

public class ActionHandlerException extends SeventyEightException {

    private boolean cancelTransaction = false;

	public ActionHandlerException( String s ) {
		super( s );
	}

    public ActionHandlerException( String s, boolean cancelTransaction ) {
        super( s );
        this.cancelTransaction = cancelTransaction;
    }
	
	public ActionHandlerException( Exception e ) {
		super( e );
	}

    public ActionHandlerException( Exception e, boolean cancelTransaction ) {
        super( e );
        this.cancelTransaction = cancelTransaction;
    }

    public ActionHandlerException( String s, Exception e, boolean cancelTransaction ) {
        super( s, e );
        this.cancelTransaction = cancelTransaction;
    }
}
