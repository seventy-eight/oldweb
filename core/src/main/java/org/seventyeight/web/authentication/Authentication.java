package org.seventyeight.web.authentication;

import org.seventyeight.database.Database;
import org.seventyeight.web.authentication.exceptions.NoSuchUserException;
import org.seventyeight.web.authentication.exceptions.PasswordDoesNotMatchException;
import org.seventyeight.web.authentication.exceptions.UnableToCreateSessionException;
import org.seventyeight.web.exceptions.PersistenceException;
import org.seventyeight.web.model.Request;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 03-12-12
 *         Time: 16:49
 */
public interface Authentication {
    public static final String __SESSION_ID = "session";
    public static final String __NAME_KEY = "username";
    public static final String __PASS_KEY = "password";
    public static final String __FORM_KEY = "login-form";
    public static final int __HOUR = 60 * 60;

    void authenticate( Request request, HttpServletResponse response ) throws PasswordDoesNotMatchException, UnableToCreateSessionException, NoSuchUserException;
    public Session login( Database db, String username, String password ) throws PasswordDoesNotMatchException, UnableToCreateSessionException, PersistenceException, NoSuchUserException;
}
