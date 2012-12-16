package org.seventyeight.web;

import java.util.HashMap;
import java.util.Map;

import org.seventyeight.web.exceptions.DictionaryDoesNotExistException;
import org.seventyeight.web.handler.Dictionary;

public class I18N {
	private Map<Class<?>, Dictionary> i18n = new HashMap<Class<?>, Dictionary>();
		
	public Dictionary getDictionary( Class<?> clazz ) throws DictionaryDoesNotExistException {
		try {
			return i18n.get( clazz );
		} catch( Exception e ) {
			throw new DictionaryDoesNotExistException( "Dictionary for " + clazz.getName() + " does not exist" );
		}
	}
	
	public void addDictionary( Class<?> clazz, Dictionary dictionary ) {
		i18n.put( clazz, dictionary );
	}
}
