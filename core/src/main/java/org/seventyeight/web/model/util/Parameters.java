package org.seventyeight.web.model.util;

import org.seventyeight.web.model.ParameterRequest;
import org.seventyeight.web.model.resources.User;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cwolfgang
 *         Date: 30-11-12
 *         Time: 23:23
 */
public class Parameters extends HashMap<String, String> implements ParameterRequest {

    private User user;

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser( User user ) {
        this.user = user;
    }

    @Override
    public String getParameter( String key ) {
        return this.get( key );
    }

    @Override
    public String[] getParameterValues( String key ) {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> T getValue( String key ) {
        return (T) this.get( key );
    }

    @Override
    public <T> T getValue( String key, T defaultValue ) {
        if( containsKey( key ) ) {
            return (T) get( key );
        } else {
            return defaultValue;
        }
    }
}
