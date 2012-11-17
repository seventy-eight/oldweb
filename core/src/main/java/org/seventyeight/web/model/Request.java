package org.seventyeight.web.model;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
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

    public Request( HttpServletRequest httpServletRequest ) {
        super( httpServletRequest );
    }

    public void setDB( OGraphDatabase db ) {
        this.db = db;
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
}
