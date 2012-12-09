package org.seventyeight.web.authentication;

import org.seventyeight.web.authentication.exceptions.NoSuchUserException;
import org.seventyeight.web.authentication.exceptions.PasswordDoesNotMatchException;
import org.seventyeight.web.authentication.exceptions.UnableToCreateSessionException;
import org.seventyeight.web.model.Request;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 03-12-12
 *         Time: 16:49
 */
public interface Authentication {
    void authenticate( Request request, HttpServletResponse response ) throws PasswordDoesNotMatchException, NoSuchUserException, UnableToCreateSessionException, NoSuchUserException;
}
