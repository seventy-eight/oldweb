package org.seventyeight.web.model.extensions;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.seventyeight.GraphDragon;
import org.seventyeight.annotations.ExtensionType;
import org.seventyeight.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.model.AbstractExtension;
import org.seventyeight.model.Configurable;
import org.seventyeight.model.Descriptor;
import org.seventyeight.model.AbstractObject;
import org.seventyeight.model.Extension;
import org.seventyeight.model.Portrait;
import org.seventyeight.model.RequestContext;

@ExtensionType
public class UserDecal extends AbstractExtension implements Portrait, Configurable, UserAvatarExtension {

	private static Logger logger = Logger.getLogger( UserDecal.class );
	
	public UserDecal( Node node ) {
		super( node );
	}
	
	public void save( RequestContext request ) {
		logger.debug( "Saving user decal!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
		String id = request.getKey( "decalId" );
		if( id != null ) {
			setDecalId( Long.parseLong( id ) );
		}
	}
	
	public void setDecalId( long id ) {
		node.setProperty( "decalId", id );
	}
	
	public Long getDecalId() {
		return (Long)node.getProperty( "decalId", null );
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
		
		@Override
		public Class<? extends Extension> getExtensionClass() {
			return null;
		}

		@Override
		public UserDecal newInstance() throws UnableToInstantiateObjectException {
			return super.newInstance();
		}

		/*
		@Override
		public UserDecal get( Node node ) {
			return new UserDecal( node );
		}
		*/
	}

	@Override
	public String getDisplayName() {
		return "User decal?!";
	}

	public String getProtraitUrl() {
		return "MY URL";
	}

}
