package org.seventyeight.web.model;

import org.apache.log4j.Logger;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;

public abstract class ResourceDescriptor<T extends AbstractResource> extends Descriptor<T> {

	private static Logger logger = Logger.getLogger( ResourceDescriptor.class );

	public T newInstance() throws UnableToInstantiateObjectException {
		logger.debug( "New instance of resource" );
		T instance = super.newInstance();
		GraphDragon.getInstance().GetIdentifier( (AbstractResource) instance );
		((AbstractResource) instance).setCreated( new Date() );
		return instance;
	}
}
