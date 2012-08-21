package org.seventyeight.web.model;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.NodeType;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;

import com.orientechnologies.orient.core.record.impl.ODocument;

public abstract class Descriptor<T extends Configurable> {
	
	private static Logger logger = Logger.getLogger( Descriptor.class );
	
	protected Class<T> clazz;
	
	protected Descriptor() {
		clazz = (Class<T>) getClass().getEnclosingClass();
		logger.debug( "Descriptor class is " + clazz );
	}
	
	public abstract String getDisplayName();
	public abstract String getType();

	public T newInstance() throws UnableToInstantiateObjectException {
		logger.debug( "New instance for " + clazz );
		ODocument node = SeventyEight.getInstance().createNode( clazz, NodeType.item );
		
		T instance = null;
		try {
			Constructor<T> c = clazz.getConstructor( ODocument.class );
			instance = c.newInstance( node );
		} catch( Exception e ) {
			throw new UnableToInstantiateObjectException( "Unable to instantiate " + clazz, e );
		}
		
		return instance;
	}
	
	//public abstract T get( Node node );
	public abstract Class<? extends Extension> getExtensionClass();
	
	public Class<? extends Configurable> getClazz() {
		return clazz;
	}
}
