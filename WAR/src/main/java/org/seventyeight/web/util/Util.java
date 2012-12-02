package org.seventyeight.web.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public abstract class Util {

	public static Cookie getCookie( HttpServletRequest request, String key ) {
		for( Cookie cookie : request.getCookies() ) {
			if( cookie.getName().equals( key ) ) {
				return cookie;
			}
		}
		
		return null;
	}
}
