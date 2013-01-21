package org.seventyeight.web.model.resources;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

public class Article extends AbstractResource {

	private static Logger logger = Logger.getLogger( Article.class );

	public static final String __TYPENAME = "article";

	public Article( Node node ) {
		super( node );
	}

    @Override
    public Save getSaver( CoreRequest request, JsonObject jsonData ) {
		return new ArticleSaveImpl( this, request, jsonData );
	}
	
	public class ArticleSaveImpl extends ResourceSaveImpl {

		public ArticleSaveImpl( AbstractResource resource, CoreRequest request, JsonObject jsonData ) {
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
	}

}
