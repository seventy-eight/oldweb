package org.seventyeight.web.authentication;

import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.authentication.exceptions.NoSuchUserException;
import org.seventyeight.web.authentication.exceptions.PasswordDoesNotMatchException;
import org.seventyeight.web.authentication.exceptions.UnableToCreateSessionException;
import org.seventyeight.web.exceptions.PersistenceException;
import org.seventyeight.web.servlet.Request;
import org.seventyeight.web.model.resources.User;
import org.seventyeight.web.servlet.Response;


public class SimpleAuthentication implements Authentication {

	private static Logger logger = Logger.getLogger( SimpleAuthentication.class );
	
	public void authenticate( Request request, Response response ) throws PasswordDoesNotMatchException, NoSuchUserException, UnableToCreateSessionException {

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
					logger.debug( "NOT VALID USER" );
				}
			}
		}
	}

    public Session login( Database db, String username, String password ) throws PasswordDoesNotMatchException, UnableToCreateSessionException, PersistenceException, NoSuchUserException {
        if( !username.isEmpty() ) {
            User user = User.getUserByUsername( db, username );

            if( user.getPassword() != null && !password.equals( user.getPassword() ) ) {
                logger.debug( "Wrong password" );
                throw new PasswordDoesNotMatchException( "Passwords does not match" );
            }

            //request.initializeTransaction();
            Session session = SeventyEight.getInstance().getSessionManager().createSession( db, user, new Date(), 10 );
            return session;
        } else {
            throw new NoSuchUserException( "" );
        }
    }

}
