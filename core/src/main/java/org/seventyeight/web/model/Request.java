package org.seventyeight.web.model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.seventyeight.web.model.resources.User;

public class Request extends HttpServletRequestWrapper {

	private User user;
	
	public Request( HttpServletRequest r, User user ) {
		super( r );
		
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
}
