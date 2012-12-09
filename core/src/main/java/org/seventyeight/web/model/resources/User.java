package org.seventyeight.web.model.resources;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.database.Node;
import org.seventyeight.utils.Utils;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.authentication.exceptions.NoSuchUserException;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;
import org.seventyeight.web.model.extensions.UserAvatar;
import org.seventyeight.web.util.Date;

import com.google.gson.JsonObject;

public class User extends AbstractResource {
	
	private static Logger logger = Logger.getLogger( User.class );
	
	public static final String __TYPENAME = "user";
	
	public static final String __USERNAME = "username";

    public static final String INDEX_USERNAMES = "usernames";

	public User( Node node ) {
		super( node );
	}

	public void doSave( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		save( new UserSaveImpl( this, request, jsonData ) );
	}
	
	public class UserSaveImpl extends ResourceSaveImpl {

		public UserSaveImpl( AbstractResource resource, ParameterRequest request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			
			logger.debug( "Saving user" );

			/* Saving basic properties */
			String password1 = request.getValue( "password" );
			String password2 = request.getValue( "password_again" );
			if( password1 != null ) {
				//String oldPassword = (String) node.getProperty( "password" );
				if( password2 == null || !password1.equals( password2 ) ) {
					logger.warn( "Passwords didn't match" );
					throw new InconsistentParameterException( "The passwords does not match" );
				}
				try {
					node.set( "password", Utils.md5( password1 ) );
				} catch( NoSuchAlgorithmException e ) {
					logger.error( "Unable to hash password." );
					throw new ErrorWhileSavingException( "Unable to hash password: " + e.getMessage() );
				}
			}
			
			String username = request.getValue( "username" );
			logger.debug( "USERNAME: " + username );
			if( username != null ) {
				node.set( "username", username );
			}
			
			String nickname = request.getValue( "nickname" );
			if( nickname != null ) {
				node.set( "nickname", nickname );
			}
		}
		
		@Override
		public void setOwner() {
			/* Set the user it self as owner(only for user resources) */
			User.this.setOwner( User.this );
			logger.debug( "Owner is now " + User.this );
		}
		
	}
	
	public void setUsername( String username ) {
		node.set( "username", username );
	}

	public String getUsername() {
		return getField( "username", "" );
	}
	
	
	public void setNickname( String nickname ) {
		node.set( "nickname", nickname );
	}

	public String getNickname() {
		return getField( "nickname", "" );
	}
	
	
	public void setPassword( String password ) {
		node.set( "password", password );
	}
	
	public void setUnEncryptedPassword( String password ) throws UnableToSavePasswordException {
		try {
			node.set( "password", Utils.md5( password ) );
		} catch( NoSuchAlgorithmException e ) {
			logger.warn( "Could not set password: " + e.getMessage() );
			throw new UnableToSavePasswordException( e.getMessage() );
		}
	}

	public String getPassword() {
		return getField( "password", null );
	}
	
	public String getDisplayName() {
		return getUsername();
	}

	public Date getSeenAsDate() {
		return new Date( getField( "seen", 0l ) );
	}
	
	public Long getSeen() {
		return getField( "seen", 0l );
	}
	
	public void setVisibility( boolean visible ) {
		node.set( "visible", visible );
	}
	
	public boolean isVisible() {
		return getField( "visible", false );
	}
	
	public String getLanguage() {
		return getField( "language", "danish" );
	}

	public void setSeen() {
		Long now = new Date().getTime();
		node.set( "seen", now );
		logger.debug( "Setting seen to " + now );
		/* Fast track index */
		/*
		Index<Node> idx = GraphDragon.getInstance().getResourceIndex();
		try {
			idx.remove( node, "seen" );
			logger.debug( "Seen index removed" );
		} catch( Exception e ) {
			logger.warn( e );
		}
		
		try {
			idx.add( node, "seen", new ValueContext( now ).indexNumeric() );
			logger.debug( "Seen index added" );
		} catch( Exception e ) {
			logger.warn( e );
		}
		
		logger.debug( "Seen index DONE" );
		*/
	}
	
	/*
	public void updateIndexes( Index<Node> idx ) {
		super.updateIndexes( idx );
		
		logger.debug( "Adding user indexes for " + this );
		idx.add( getNode(), "username", getUsername().toLowerCase() );
		idx.add( getNode(), "nickname", getNickname().toLowerCase() );
		
		// Setting seen index again 
		long seen = getSeen();
		if( seen > 0 ) {
			idx.add( node, "seen", new ValueContext( seen ).indexNumeric() );
		}
	}
	*/
	
	/**
	 *  Get user resource by username 
	 * @return
	 */
	public static User getUserByUsername( Database db, String username ) throws NoSuchUserException {

        List<Node> nodes = db.getFromIndex( INDEX_USERNAMES, username );
		
		if( nodes.size() == 1 ) {
			return new User( nodes.get( 0 ) );
		} else {
			throw new NoSuchUserException( "No single user found. Found " + nodes.size() + " users" );
		}
	}
	
	public static class UserDescriptor extends ResourceDescriptor<User> {

		@Override
		public String getDisplayName() {
			return "User";
		}
		
		@Override
		public String getType() {
			return "user";
		}
		
		@Override
		public Class<? extends Extension> getExtensionClass() {
			return UserExtension.class;
		}

		@Override
		public User newInstance( Database db ) throws UnableToInstantiateObjectException {
			return super.newInstance( db );
		}

		/*
		@Override
		public User get( Node node ) {
			return new User( node );
		}
		*/
	}

	public String getPortrait() {
		List<AbstractExtension> list = SeventyEight.getInstance().getExtensions( this, UserAvatar.class );
		
		logger.debug( "I found " + list );
		
		if( list.size() > 0 ) {
			UserAvatar avatar = (UserAvatar) list.get( 0 );
			try {
				return avatar.getUrl();
			} catch( Exception e ) {
				logger.debug( "I could not find any object: " + e.getMessage() );
				/* Return default portrait? */
			}
		} else {
			logger.debug( "I did not find any?!" );
			/* Return a default portrait? */
		}
		
		return "No portrait";
	}
	
}
