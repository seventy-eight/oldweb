package org.seventyeight.web.util;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.database.IndexType;
import org.seventyeight.database.IndexValueType;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.resources.Group;
import org.seventyeight.web.model.resources.User;
import org.seventyeight.web.model.util.Parameters;

/**
 * @author cwolfgang
 *         Date: 01-12-12
 *         Time: 22:29
 */
public class Installer {

    private static Logger logger = Logger.getLogger( Installer.class );

    public Database db;
    private SeventyEight se;

    public Installer( Database db ) {
        this.db = db;
        this.se = SeventyEight.getInstance();
    }

    public void install() throws ParameterDoesNotExistException, ErrorWhileSavingException, UnableToInstantiateObjectException, IncorrectTypeException, ResourceDoesNotExistException, InconsistentParameterException {

        logger.info( "Installing indexes" );
        db.createIndex( SeventyEight.INDEX_RESOURCES, IndexType.UNIQUE, IndexValueType.LONG );
        db.createIndex( SeventyEight.INDEX_RESOURCE_TYPES, IndexType.REGULAR, IndexValueType.STRING, IndexValueType.LONG );
        db.createIndex( SeventyEight.INDEX_SYSTEM_USERS, IndexType.UNIQUE, IndexValueType.STRING );

        logger.info( "Installing users" );
        User admin = installUser( "wolle", true );
        User anonymous = installUser( "anonymous", false );
        db.putToIndex( SeventyEight.INDEX_SYSTEM_USERS, anonymous.getNode(), "anonymous" );

        logger.info( "Installing groups" );
        Group admins = installGroup( "Admins", admin );
        admins.addMember( admin );


    }

    public  User installUser( String name, boolean visibile ) throws UnableToInstantiateObjectException, ErrorWhileSavingException, ParameterDoesNotExistException, IncorrectTypeException, ResourceDoesNotExistException, InconsistentParameterException {
        User user = (User) se.getDescriptorFromResourceType( "user" ).newInstance( db );

        Parameters p = new Parameters();
        p.put( "title", name );
        p.put( "username", name );

        if( visibile ) {
            user.setVisibility( true );
        }

        user.save( p, null );

        return user;
    }

    public Group installGroup( String name, User owner ) throws UnableToInstantiateObjectException, ErrorWhileSavingException, ParameterDoesNotExistException, IncorrectTypeException, ResourceDoesNotExistException, InconsistentParameterException {
        Group group = (Group) se.getDescriptorFromResourceType( "group" ).newInstance( db );

        Parameters p = new Parameters();
        p.put( "title", name );
        p.setUser( owner );

        group.save( p, null );

        return group;
    }
}
