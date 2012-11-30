package org.seventyeight.web.model.resources;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.database.Node;
import org.seventyeight.utils.Utils;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;
import org.seventyeight.web.model.extensions.UserAvatar;
import org.seventyeight.web.util.Date;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Article extends AbstractResource {

	private static Logger logger = Logger.getLogger( Article.class );

	public static final String __TYPENAME = "article";

	public Article( Node node ) {
		super( node );
	}

	public void save( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		doSave( new ArticleSaveImpl( this, request, jsonData ) );
	}
	
	public class ArticleSaveImpl extends ResourceSaveImpl {

		public ArticleSaveImpl( AbstractResource resource, ParameterRequest request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			
			logger.debug( "Saving article" );

			String nickname = request.getValue( "text" );
			if( nickname != null ) {
				node.set( "text", nickname );
			}
		}
	}
	
	public void setText( String text ) {
		node.set( "text", text );
	}

	public String getText() {
		return getField( "text", "" );
	}

	public String getDisplayName() {
		return getTitle();
	}

    @Override
    public String getPortrait() {
        return null;
    }

    public static class ArticleDescriptor extends ResourceDescriptor<Article> {

		@Override
		public String getDisplayName() {
			return "Article";
		}
		
		@Override
		public String getType() {
			return "article";
		}

		@Override
		public Class<? extends Extension> getExtensionClass() {
			return null;
		}

		@Override
		public Article newInstance( Database db ) throws UnableToInstantiateObjectException {
			return super.newInstance( db );
		}
	}

}
