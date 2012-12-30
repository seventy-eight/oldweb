package org.seventyeight.web.model.extensions;

import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.web.model.*;

//@ExtensionType
public class UserDecal extends AbstractExtension implements Portrait, Describable, UserAvatarExtension {

	private static Logger logger = Logger.getLogger( UserDecal.class );
	
	public UserDecal( Node node ) {
		super( node );
	}
	
	public void save( Request request ) {
		logger.debug( "Saving user decal!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
		String id = request.getValue( "decalId" );
		if( id != null ) {
			setDecalId( Long.parseLong( id ) );
		}
	}
	
	public void setDecalId( long id ) {
		node.set( "decalId", id );
	}
	
	public Long getDecalId() {
		return (Long)node.get( "decalId", null );
	}
	
	public static class UserDecalDescriptor extends Descriptor<UserDecal> {

		@Override
		public String getDisplayName() {
			return "User decal";
		}
		
		@Override
		public String getType() {
			return "userDecal";
		}
	}

	@Override
	public String getDisplayName() {
		return "User decal?!";
	}

	public String getProtraitUrl() {
		return "MY URL";
	}

}
