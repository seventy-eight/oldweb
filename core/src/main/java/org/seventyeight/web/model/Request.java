package org.seventyeight.web.model;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import org.apache.velocity.VelocityContext;
import org.seventyeight.velocity.html.ThemeSelectInputDirective;
import org.seventyeight.web.model.resources.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * User: cwolfgang
 * Date: 16-11-12
 * Time: 21:43
 */
public class Request extends HttpServletRequestWrapper implements ParameterRequest, DatabaseRequest {

    private OGraphDatabase db;
    private RequestMethod method = RequestMethod.GET;
    private AbstractTheme theme = null;
    private VelocityContext context;

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

    public void setDB( OGraphDatabase db ) {
        this.db = db;
    }

    public void setRequestMethod( String m ) {
        this.method = RequestMethod.valueOf( m );
    }

    public boolean isRequestPost() {
        return method.equals( RequestMethod.GET );
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
    public OGraphDatabase getDB() {
        return db;
    }

    @Override
    public User getUser() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
