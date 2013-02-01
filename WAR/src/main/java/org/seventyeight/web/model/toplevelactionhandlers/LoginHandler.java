package org.seventyeight.web.model.toplevelactionhandlers;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.authentication.Authentication;
import org.seventyeight.web.authentication.Session;
import org.seventyeight.web.authentication.SessionManager;
import org.seventyeight.web.authentication.SimpleAuthentication;
import org.seventyeight.web.authentication.exceptions.NoSuchUserException;
import org.seventyeight.web.authentication.exceptions.PasswordDoesNotMatchException;
import org.seventyeight.web.authentication.exceptions.UnableToCreateSessionException;
import org.seventyeight.web.exceptions.PersistenceException;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.TopLevelAction;
import org.seventyeight.web.model.resources.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author cwolfgang
 *         Date: 31-01-13
 *         Time: 13:49
 */
public class LoginHandler implements TopLevelAction {

    private static Logger logger = Logger.getLogger( LoginHandler.class );

    @Override
    public String getDisplayName() {
        return getUrlName();
    }

    @Override
    public String getUrlName() {
        return "login";
    }

    public void doIndex( Request request, HttpServletResponse response, JsonObject json ) throws NoSuchUserException, UnableToCreateSessionException, PasswordDoesNotMatchException, PersistenceException {
        String username = request.getValue( "username", "" );
        String password = request.getValue( "password", "" );

        logger.debug( "Trying to login " + username );

        Session session = SeventyEight.getInstance().getAuthentication().login( request.getDB(), username, password );

        Cookie cookie = new Cookie( SimpleAuthentication.__SESSION_ID, session.getHash() );
        cookie.setMaxAge( 10 * Authentication.__HOUR );
        response.addCookie( cookie );
        logger.debug( "LOGGED IN, YAY!" );
    }
}
