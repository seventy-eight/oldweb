package org.seventyeight.web.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDictionary {
	protected Map<String, Map<String, String>> dictionary = new HashMap<String, Map<String, String>>();
	protected Date modified;
	
	public abstract void initialize();
	
	public String get( String language, String lookup ) {
		String r = dictionary.get( language ).get( lookup );
		if( r != null ) {
			return r;
		} else {
			return lookup;
		}
	}
}
