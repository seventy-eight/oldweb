package org.seventyeight.web;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.seventyeight.database.Database;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.resources.User;
import org.seventyeight.web.model.util.Parameters;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SeventyEightTest {
	private static Logger logger = Logger.getLogger( SeventyEightTest.class );
	
	@Rule
	public static EnvRule env = new EnvRule();
	
	
	@Test
	public void getInstance() {
		SeventyEight se = SeventyEight.getInstance();
        assertNotNull( se );
	}

    @Test
    public void createUser() throws UnableToInstantiateObjectException {
        SeventyEight se = SeventyEight.getInstance();
        assertNotNull( se );

        User user = (User) se.getDescriptorFromResourceType( "user" ).newInstance( env.getDB() );
        assertNotNull( user );
        assertNotNull( user.getNode() );
        assertThat( (String) user.getNode().get( "type" ), is( "user" ) );
        assertThat( user.getIdentifier(), is( 1l ) );
    }

    @Test
    public void createUserSaved() throws UnableToInstantiateObjectException, ErrorWhileSavingException, ParameterDoesNotExistException, IncorrectTypeException, ResourceDoesNotExistException, InconsistentParameterException, TooManyException, NotFoundException, CouldNotLoadResourceException {
        SeventyEight se = SeventyEight.getInstance();
        assertNotNull( se );

        User user = (User) se.getDescriptorFromResourceType( "user" ).newInstance( env.getDB() );
        assertNotNull( user );
        assertNotNull( user.getNode() );
        assertThat( (String) user.getNode().get( "type" ), is( "user" ) );
        assertThat( user.getIdentifier(), is( 1l ) );

        Parameters parms = new Parameters();
        parms.put( "username", "wolle" );
        parms.put( "nickname", "wolle" );
        parms.put( "password", "p" );
        parms.put( "password_again", "p" );

        user.save( parms, null );

        assertThat( user.getUsername(), is( "wolle" ) );

        Database db = env.getAnotherDB();
        User user1 = (User) SeventyEight.getInstance().getResource( db, 1l );
        assertThat( user1, is( user ) );
        assertThat( user1.getUsername(), is( user.getUsername() ));
    }

}
