package org.seventyeight.web.model;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.velocity.VelocityContext;
import org.seventyeight.database.Database;
import org.seventyeight.database.EdgeType;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.authentication.Authentication;
import org.seventyeight.web.model.resources.Group;
import org.seventyeight.web.model.resources.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.List;

/**
 * User: cwolfgang
 * Date: 16-11-12
 * Time: 21:43
 */
public class Request extends HttpServletRequestWrapper implements ParameterRequest, DatabaseRequest {

    private static Logger logger = Logger.getLogger( Request.class );

    private Database db;
    private RequestMethod method = RequestMethod.GET;
    private AbstractTheme theme = null;
    private VelocityContext context;

    private AbstractResource resource;

    private String template = "org/seventyeight/web/main.vm";

    private boolean transactional = false;

    private User user;
    private boolean authenticated;

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
        return method.equals( RequestMethod.POST );
    }

    public String[] getRequestParts() {
        return requestParts;
    }

    public void setRequestParts( String[] parts ) {
        this.requestParts = parts;
    }

    public void setResource( AbstractResource resource ) {
        this.resource = resource;
    }

    public AbstractResource getResource() {
        return resource;
    }

    public void setTransactional( boolean t ) {
        this.transactional = t;
    }

    public boolean hasTransaction() {
        return transactional;
    }

    /**
     * Determine if the current {@link User} can edit/write the resource
     * @param resource
     * @return
     */
    public boolean canEdit( AbstractResource resource ) {
        return isAllowed( resource, SeventyEight.GroupEdgeType.writeAccess );
    }

    /**
     * Determine if the current {@link User} can access the resource
     * @param resource
     * @return
     */
    public boolean hasAccess( AbstractResource resource ) {
        return isAllowed( resource, SeventyEight.GroupEdgeType.readAccess );
    }

    private boolean isAllowed( AbstractResource resource, SeventyEight.GroupEdgeType groupType ) {
        logger.debug( resource + " allowed in " + groupType );
        List<Group> groups = resource.getGroups( groupType );

        for( Group group : groups ) {
            if( group.isMember( user ) ) {
                return true;
            }
        }

        return false;
    }


    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated( boolean authenticated ) {
        this.authenticated = authenticated;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate( String template ) {
        this.template = template;
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
