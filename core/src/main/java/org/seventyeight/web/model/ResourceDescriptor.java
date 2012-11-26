package org.seventyeight.web.model;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.util.Date;

public abstract class ResourceDescriptor<T extends AbstractResource> extends Descriptor<T> {

	private static Logger logger = Logger.getLogger( ResourceDescriptor.class );

    protected ResourceDescriptor( Database db ) {
        super( db );
    }

	public T newInstance() throws UnableToInstantiateObjectException {
		logger.debug( "New instance of resource" );
		T instance = super.newInstance();
		//SeventyEight.getInstance().GetIdentifier( (AbstractResource) instance );
        SeventyEight.getInstance().setIdentifier( instance );
		((AbstractResource) instance).setCreated( new Date() );
		return instance;
	}
}
