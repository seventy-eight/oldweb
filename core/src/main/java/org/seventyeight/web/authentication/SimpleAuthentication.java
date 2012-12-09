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

	private static final String __SESSION_ID = "session";
	private static final String __NAME_KEY = "username";
	private static final String __PASS_KEY = "password";
	private static final String __FORM_KEY = "login-form";
	
	private static final int __HOUR = 60 * 60;
	
	private static Logger logger = Logger.getLogger( SimpleAuthentication.class );
	
	public void authenticate( Request request, HttpServletResponse response ) throws PasswordDoesNotMatchException, NoSuchUserException, UnableToCreateSessionException {
		boolean authenticated = false;
		
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
		
		/**/
		if( !authenticated ) {
			logger.debug( "Not authenticated by session id" );
			String name = request.getValue( __NAME_KEY );
			String pass = request.getValue( __PASS_KEY );
			String form = request.getValue( __FORM_KEY );
			
			if( name != null && pass != null && form != null ) {
			
				User user = User.getUserByUsername( request.getDB(), name );

				/* Exactly one hit, check password */
				logger.debug( "PASSWORD: " + user.getPassword() );
				
				if( user.getPassword() != null && !pass.equals( user.getPassword() ) ) {
					logger.debug( "Wrong password" );
					throw new PasswordDoesNotMatchException( "Passwords does not match" );
				}
				
				/* The user should be authenticated? */
				request.setAuthenticated( true );
				request.setUser( user );
				//request.initializeTransaction();
				Session session = SeventyEight.getInstance().getSessionManager().createSession( request.getDB(), user, new Date(), 10 );
				Cookie cookie = new Cookie( __SESSION_ID, session.getHash() );
				cookie.setMaxAge( 10 * __HOUR );
				response.addCookie( cookie );
				logger.debug( "LOGGED IN, YAY!" );
				//request.succeedTransaction();
				/* Store some stuff */
			} else {
				/* Not trying to login! */
				logger.debug( "Not trying to login, hence not logged in!" );
			}
		}
	}

}
