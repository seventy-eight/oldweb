package org.seventyeight.web.model;

import org.seventyeight.web.model.resources.User;

public interface ParameterRequest {	
	public User getUser();

    public void setUser( User user );

	public String getParameter( String key );
	public String[] getParameterValues( String key );
	public <T> T getValue( String key );
    public <T> T getValue( String key, T defaultValue );
}
