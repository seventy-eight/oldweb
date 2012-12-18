package org.seventyeight.web.model;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.utils.Date;

import java.util.Collections;
import java.util.List;

public abstract class ResourceDescriptor<T extends AbstractResource> extends Descriptor<T> {

	private static Logger logger = Logger.getLogger( ResourceDescriptor.class );

    public List<String> getRequiredJavascripts() {
        return Collections.EMPTY_LIST;
    }

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

    public boolean inputTitle() {
        return true;
    }
}
