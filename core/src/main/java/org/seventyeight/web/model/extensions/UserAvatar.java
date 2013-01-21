package org.seventyeight.web.model.extensions;

import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.NoSuchObjectException;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.model.*;

public class UserAvatar extends AbstractExtension implements UserExtension, Describable {
	
	private static Logger logger = Logger.getLogger( UserAvatar.class );

	public UserAvatar( Node node ) {
		super( node );
	}
	
	public Portrait getPortrait() throws UnableToInstantiateObjectException, NoSuchObjectException {
		return getObject( SeventyEight.ResourceEdgeType.extension );
	}
	
	public String getUrl2() throws UnableToInstantiateObjectException, NoSuchObjectException {
		return getPortrait().getProtraitUrl();
	}

	public static class UserAvatarDescriptor extends Descriptor<UserAvatar> {

		@Override
		public String getDisplayName() {
			return "User avatar";
		}
	}

	@Override
	public String getDisplayName() {
		return "User avatar?!";
	}

}
