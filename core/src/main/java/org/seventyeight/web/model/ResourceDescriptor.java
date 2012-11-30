package org.seventyeight.web.model;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.util.Date;

public abstract class ResourceDescriptor<T extends AbstractResource> extends Descriptor<T> {

	private static Logger logger = Logger.getLogger( ResourceDescriptor.class );

	public T newInstance( Database db ) throws UnableToInstantiateObjectException {
		logger.debug( "New instance of resource" );
		T instance = super.newInstance( db );
		//SeventyEight.getInstance().GetIdentifier( (AbstractResource) instance );
        SeventyEight.getInstance().setIdentifier( instance );
		instance.setCreated( new Date() );
        instance.getNode().set( "type", getType() );
        instance.getNode().save();

        /* Update index */
        instance.updateIndexes();

		return instance;
	}
}
