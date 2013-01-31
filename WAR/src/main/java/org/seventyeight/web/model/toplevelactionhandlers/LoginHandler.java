package org.seventyeight.web.model.toplevelactionhandlers;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.authentication.SessionManager;
import org.seventyeight.web.authentication.exceptions.NoSuchUserException;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.TopLevelAction;
import org.seventyeight.web.model.resources.User;

import javax.servlet.http.HttpServletResponse;

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

    /*
    public void doLogin( Request request, HttpServletResponse response, JsonObject json ) {
        String username = request.getValue( "username", "" );
        String password = request.getValue( "password", "" );

        logger.debug( "Trying to login " + username );

        if( !username.isEmpty() ) {
            User user = null;
            try {
                user = User.getUserByUsername( request.getDB(), username );
            } catch( NoSuchUserException e ) {
                logger.warn( e );
                return;
            }
            SessionManager sm = SeventyEight.getInstance().getSessionManager();
            sm.createSession()
        }
    }
    */
}
