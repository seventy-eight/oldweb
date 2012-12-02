package org.seventyeight.web.model;

import org.apache.velocity.VelocityContext;
import org.seventyeight.database.Database;
import org.seventyeight.web.model.resources.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * User: cwolfgang
 * Date: 16-11-12
 * Time: 21:43
 */
public class Request extends HttpServletRequestWrapper implements ParameterRequest, DatabaseRequest {

    private Database db;
    private RequestMethod method = RequestMethod.GET;
    private AbstractTheme theme = null;
    private VelocityContext context;

    private User user;

    private String[] requestParts;

    public enum RequestMethod {
        GET,
        POST,
        DELETE,
        PUT
    }

    public Request( HttpServletRequest httpServletRequest ) {
        super( httpServletRequest );
        setRequestMethod( httpServletRequest.getMethod() );
    }

    public void setDB( Database db ) {
        this.db = db;
    }

    public void setRequestMethod( String m ) {
        this.method = RequestMethod.valueOf( m );
    }

    public boolean isRequestPost() {
        return method.equals( RequestMethod.GET );
    }

    public String[] getRequestParts() {
        return requestParts;
    }

    public void setRequestParts( String[] parts ) {
        this.requestParts = parts;
    }

    public AbstractTheme getTheme() {
        return theme;
    }

    public VelocityContext getContext() {
        return context;
    }

    public void setContext( VelocityContext context ) {
        this.context = context;
    }

    @Override
    public Database getDB() {
        return db;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser( User user ) {
        this.user = user;
    }

    @Override
    public <T> T getValue( String key ) {
        return (T) this.getParameter( key );
    }

    @Override
    public <T> T getValue( String key, T defaultValue ) {
        if( this.getParameter( key ) != null ) {
            return (T) this.getParameter( key );
        } else {
            return defaultValue;
        }
    }
}
