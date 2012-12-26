package org.seventyeight.web.model;

import org.apache.log4j.Logger;

/**
 * 
 * @author wolfgang
 *
 */
public abstract class AbstractTheme {
	private Logger logger = Logger.getLogger( AbstractTheme.class );
	public AbstractTheme() {
		logger.debug( "Creating theme" );
	}
	
	public abstract String getName();

    @Override
    public String toString() {
        return getName();
    }
}
