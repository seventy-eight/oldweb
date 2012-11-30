package org.seventyeight.web.model;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;

public abstract class Descriptor<T extends Configurable> {
	
	private static Logger logger = Logger.getLogger( Descriptor.class );
	
	protected Class<T> clazz;
	
	protected Descriptor() {
		clazz = (Class<T>) getClass().getEnclosingClass();
		logger.debug( "Descriptor class is " + clazz );
	}
	
	public abstract String getDisplayName();
	public abstract String getType();

	public T newInstance( Database db ) throws UnableToInstantiateObjectException {
		logger.debug( "New instance for " + clazz );
		//ODocument node = SeventyEight.getInstance().createNode( clazz, NodeType.item );
        Node node = db.createNode();
		
		T instance = null;
		try {
			Constructor<T> c = clazz.getConstructor( Node.class );
			instance = c.newInstance( node );
		} catch( Exception e ) {
			throw new UnableToInstantiateObjectException( "Unable to instantiate " + clazz, e );
		}

        node.set( "class", getClazz().getName() );
		
		return instance;
	}
	
	//public abstract T get( Node node );
	public abstract Class<? extends Extension> getExtensionClass();
	
	public Class<? extends Configurable> getClazz() {
		return clazz;
	}

    /**
     * When instantiated the descriptor can configure an index
     */
    public void configureIndex() {
        /* Default implementation is a no op */
    }
}
