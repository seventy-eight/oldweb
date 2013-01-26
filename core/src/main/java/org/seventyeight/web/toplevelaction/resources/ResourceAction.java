package org.seventyeight.web.toplevelaction.resources;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.web.exceptions.CouldNotLoadItemException;
import org.seventyeight.web.model.*;
import org.seventyeight.web.util.ResourceUtils;

/**
 * @author cwolfgang
 *         Date: 17-01-13
 *         Time: 21:35
 */
public class ResourceAction implements ItemType {

    private static Logger logger = Logger.getLogger( ResourceAction.class );

    @Override
    public String getUrlName() {
        return "resource";
    }


    @Override
    public AbstractItem getItem( String name, Database db ) throws CouldNotLoadItemException {
        logger.debug( "Get RESOURCE " + name );
        try {
            AbstractResource r = ResourceUtils.getResource( db, name );
            return r;
    }
}
