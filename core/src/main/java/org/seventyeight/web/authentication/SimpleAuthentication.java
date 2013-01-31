package org.seventyeight.web.authentication;

import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.authentication.exceptions.NoSuchUserException;
import org.seventyeight.web.authentication.exceptions.PasswordDoesNotMatchException;
import org.seventyeight.web.authentication.exceptions.UnableToCreateSessionException;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.resources.User;


public class SimpleAuthentication implements Authentication {

	public static final String __SESSION_ID = "session";
    public static final String __NAME_KEY = "username";
    public static final String __PASS_KEY = "password";
    public static final String __FORM_KEY = "login-form";

    public static final int __HOUR = 60 * 60;
	
	private static Logger logger = Logger.getLogger( SimpleAuthentication.class );
	
	public void authenticate( Request request, HttpServletResponse response ) throws PasswordDoesNotMatchException, NoSuchUserException, UnableToCreateSessionException {

		String hash = null;
		
		for( Cookie cookie : request.getCookies() ) {
			logger.debug( "Cookie: " + cookie.getName() + "=" + cookie.getValue() );
			if( cookie.getName().equals( __SESSION_ID ) ) {
				hash = cookie.getValue();
				break;
			}
		}
		
		if( hash != null ) {
			logger.debug( "Found hash: " + hash );
			Session session = SeventyEight.getInstance().getSessionManager().getSession( request.getDB(), hash );
			if( session != null ) {
				User user = session.getUser();
				if( user != null ) {
					logger.debug( "Session user is " + user );
					request.setAuthenticated( true );
					request.setUser( user );
					return;
				} else {
					logger.debug( "NOT VALiD USER" );
				}
			}

		}
	}

}
