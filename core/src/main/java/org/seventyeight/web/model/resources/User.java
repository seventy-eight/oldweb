package org.seventyeight.web.model.resources;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class User extends AbstractResource {
	
	private static Logger logger = Logger.getLogger( User.class );
	
	public static final String __TYPENAME = "user";
	
	public static final String __USERNAME = "username";

	public User( ODocument node ) {
		super( node );
	}

	public void save( Request request, JsonObject jsonData ) throws ResourceDoesNotExistException, ParameterDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		doSave( new UserSaveImpl( this, request, jsonData ) );
	}
	
	public class UserSaveImpl extends ResourceSaveImpl {

		public UserSaveImpl( AbstractResource resource, RequestContext request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			
			logger.debug( "Saving user" );

			/* Saving basic properties */
			String password1 = request.getKey( "password" );
			String password2 = request.getKey( "password_again" );
			if( password1 != null ) {
				//String oldPassword = (String) node.getProperty( "password" );
				if( password2 == null || !password1.equals( password2 ) ) {
					logger.warn( "Passwords didn't match" );
					throw new InconsistentParameterException( "The passwords does not match" );
				}
				try {
					node.setProperty( "password", Util.md5( password1 ) );
				} catch( NoSuchAlgorithmException e ) {
					logger.error( "Unable to hash password." );
					throw new ErrorWhileSavingException( "Unable to hash password: " + e.getMessage() );
				}
			}
			
			String username = request.getKey( "username" );
			logger.debug( "USERNAME: " + username );
			if( username != null ) {
				node.setProperty( "username", username );
			}
			
			String nickname = request.getKey( "nickname" );
			if( nickname != null ) {
				node.setProperty( "nickname", nickname );
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
		node.setProperty( "username", username );
	}

	public String getUsername() {
		return (String) node.getProperty( "username", "" );
	}
	
	
	public void setNickname( String nickname ) {
		node.setProperty( "nickname", nickname );
	}

	public String getNickname() {
		return (String) node.getProperty( "nickname", "" );
	}
	
	
	public void setPassword( String password ) {
		node.setProperty( "password", password );
	}
	
	public void setUnEncryptedPassword( String password ) throws UnableToSavePasswordException {
		try {
			node.setProperty( "password", Util.md5( password ) );
		} catch( NoSuchAlgorithmException e ) {
			logger.warn( "Could not set password: " + e.getMessage() );
			throw new UnableToSavePasswordException( e.getMessage() );
		}
	}

	public String getPassword() {
		return (String) node.getProperty( "password", null );
	}
	
	public String getDisplayName() {
		return getUsername();
	}

	public Date getSeenAsDate() {
		return new Date( (Long)node.getProperty( "seen", 0l ) );
	}
	
	public Long getSeen() {
		return (Long)node.getProperty( "seen", 0l );
	}
	
	public void setVisibility( boolean visible ) {
		node.setProperty( "visible", visible );
	}
	
	public boolean isVisible() {
		return (Boolean)node.getProperty( "visible", false );
	}
	
	public String getLanguage() {
		return (String)node.getProperty( "language", "danish" );
	}

	public void setSeen() {
		Long now = new Date().getDate().getTime();
		node.setProperty( "seen", now );
		logger.debug( "Setting seen to " + now );
		/* Fast track index */
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
	}
	

	public void updateIndexes( Index<Node> idx ) {
		super.updateIndexes( idx );
		
		logger.debug( "Adding user indexes for " + this );
		idx.add( getNode(), "username", getUsername().toLowerCase() );
		idx.add( getNode(), "nickname", getNickname().toLowerCase() );
		
		/* Setting seen index again */
		long seen = getSeen();
		if( seen > 0 ) {
			idx.add( node, "seen", new ValueContext( seen ).indexNumeric() );
		}
	}
	
	/**
	 *  Get user resource by username 
	 * @return
	 */
	public static User getUserByUsername( String username ) {
		
		Index<Node> idx = GraphDragon.getInstance().getResourceIndex();
		Node userNode = idx.get( User.__USERNAME, username ).getSingle();
		
		if( userNode != null ) {
			return new User( userNode );
		} else {
			return null;
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
		public User newInstance() throws UnableToInstantiateObjectException {
			return super.newInstance();
		}

		/*
		@Override
		public User get( Node node ) {
			return new User( node );
		}
		*/
	}

	public String getPortrait() {
		List<AbstractExtension> list = GraphDragon.getInstance().getExtensions( this, UserAvatar.class );
		
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