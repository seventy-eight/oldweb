package org.seventyeight.web.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Dictionary {
	protected Map<String, Map<String, String>> dictionary = new HashMap<String, Map<String, String>>();
	protected Date modified;

    public void insert( String language, String text, String translation ) {
        if( dictionary.containsKey( language ) ) {
            dictionary.put( language, new HashMap<String, String>() );
        }
        Map<String, String> ld = dictionary.get( language );
        ld.put( text, translation );
    }
	
	public String get( String language, String lookup ) {
		String r = dictionary.get( language ).get( lookup );
		if( r != null ) {
			return r;
		} else {
			return lookup;
		}
	}
}
