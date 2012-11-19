package org.seventyeight.web.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.resource.spi.IllegalStateException;

import org.apache.log4j.Logger;
import org.seventyeight.database.Edge;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.GroupEdgeType;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.InconsistentParameterException;
import org.seventyeight.web.exceptions.TextNodeDoesNotExistException;
import org.seventyeight.web.model.resources.Group;
import org.seventyeight.web.model.resources.User;


import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;


public abstract class AbstractObject<DB> extends AbstractItem<DB> implements Ownable, Configurable {

	private static Logger logger = Logger.getLogger( AbstractObject.class );

	public static final String __ACCESS_GROUP_NAME = "access-group";
	public static final String __EDITOR_GROUP_NAME = "editor-group";
	public static final String __REVIEW_GROUP_NAME = "review-group";
	
	
	public AbstractObject( Node node ) {
		super( node );
	}
	
	public AbstractObject( Node node, Locale locale ) {
		super( node, locale );
	}
	
	protected abstract class ObjectSave extends Save {
		protected AbstractObject object;
		protected String language;
		
		public ObjectSave( AbstractObject object, ParameterRequest configuration, JsonObject jsonData ) {
			super( object, configuration, jsonData );
			
			this.object = object;
		}
		
		@Override
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			logger.debug( "Saving Object" );
			
			/* Try to get language */
			String lang = request.getParameter( "language" );
			logger.debug( "LANG: " + lang );
			if( lang != null ) {
				logger.debug( "SETTING" );
				this.language = lang;
			} else {
				if( request.getUser() != null ) {
					logger.debug( "USER" );
					language = request.getUser().getLanguage();
				} else {
					logger.debug( "DEFAULT" );
					language = SeventyEight.getInstance().getDefaultLocale().getLanguage();
				}
			}
			logger.debug( "Language is " + language );
			
			/* Setting title */
			String title = request.getParameter( "title" );
			logger.debug( "Title is " + title );
			if( title != null ) {
				setText( "title", title, language );
			}
			
			String subtitle = request.getParameter( "subtitle" );
			if( subtitle != null ) {
				node.set( "subtitle", subtitle );
			}
			
			/* Setting description */
			String description = request.getParameter( "description" );
			if( description != null ) {
				setText( "description", description, language );
			}
			
			/* Access groups */
			String[] accessGroupIds = request.getParameterValues( __ACCESS_GROUP_NAME );
			if( accessGroupIds != null ) {
				addGroupsById( accessGroupIds, GroupEdgeType.readAccess );				
			}
			
			/* Editor groups */
			String[] editorGroupIds = request.getParameterValues( __EDITOR_GROUP_NAME );
			if( editorGroupIds != null ) {
				addGroupsById( editorGroupIds, GroupEdgeType.writeAccess );
			}
			
			/* Review groups */
			String[] reviewGroupIds = request.getParameterValues( __REVIEW_GROUP_NAME );
			if( reviewGroupIds != null ) {
				addGroupsById( reviewGroupIds, GroupEdgeType.reviewAccess );				
			}
		}
		
		@Override
		public void after() {
			setOwner();
		}
		
		public void setOwner() {
			AbstractObject.this.setOwner( request.getUser() );
		}
		
		/*
		@Override
		public void updateIndexes() {
			logger.debug( "UPDATING INDEXES: " + getIdentifier() );
			object.updateIndexes( GraphDragon.getInstance().getResourceIndex() );
		}
		*/
		
	}
	
	public void setText( String property, String value, String language ) {
		logger.debug( "Setting " + property + " text node for " + language + " to " + value );
		
		Text tt = null;
		try {
			tt = getText( language, property );
		} catch( TextNodeDoesNotExistException e ) {
			logger.debug( "The " + property + " text node for " + language + " does not exist, creating it" );
			tt = Text.create( node.getDB(), AbstractObject.this, property, language );
		}
		tt.setText( value );
	}
	
	public Text getText( String language, String property ) throws TextNodeDoesNotExistException {
		Node n = getTextNode( language, property );
		if( n != null ) {
			return new Text( n );
		} else {
			throw new TextNodeDoesNotExistException( property + " for " + language + " does not exist" );
		}
	}
	
	/**
	 * Return a node for a text for a given language or null
	 * @param language
	 * @param property
	 * @return
	 */
	public Node getTextNode( String language, String property ) {
		//List<ODocument> edges = SeventyEight.getInstance().getEdges( db, this, ResourceEdgeType.translation );
        List<Edge> edges = node.getEdges( ResourceEdgeType.translation );
		Node d = null;

		for( Edge e : edges ) {

			String prop = e.get( "property" );
			if( prop != null && prop.equals( property ) ) {
				String lang  = e.get( "language" );
				if( lang != null && lang.equals( language ) ) {
					//return SeventyEight.getInstance().getOutNode( db, e );
                    return e.getInNode();
				} else {
					d = e.getInNode();
				}
			}
		}
		
		return d;
	}
	
	public User getOwner() throws IllegalStateException {
        List<Edge> edges = node.getEdges( ResourceEdgeType.owner );
		if( edges.size() == 1 ) {
            return new User( edges.get( 0 ).getInNode() );
		} else {
			if( edges.size() > 1 ) {
				throw new IllegalStateException( "Too many owners" );
			} else {
				throw new IllegalStateException( "No owner" );
			}
		}
	}

	public void setOwner( User owner ) {
		/* Removing all owners */
		removeAllOwners();
		/* Adding new owner */
		//SeventyEight.getInstance().createEdge( this, owner, EdgeType.owner );
	}
	
	protected void removeAllOwners() {
		logger.debug( "Removing all owners for " + this );
		SeventyEight.getInstance().removeOutEdges( db, this, ResourceEdgeType.owner );
	}
	
	public List<Group> getGroups( GroupEdgeType rel ) {
		logger.debug( "Getting all groups for  " + rel.toString() );
		
		List<Group> groups = new ArrayList<Group>();

		List<ODocument> nodes = SeventyEight.getInstance().getNodes( db, this, rel );
		for( ODocument node : nodes ) {
			Group grp = new Group( node );
			groups.add( grp );
		}
		
		return groups;
	}
	
	public List<Group> getGroups( String namedEnum ) {
		return getGroups( GroupEdgeType.valueOf( namedEnum ) );
	}
	
	public List<Group> getAccessGroups() {
		return getGroups( GroupEdgeType.readAccess );
	}
	
	public void addGroupsById( String[] accessGroupIds, GroupEdgeType type ) {
		logger.debug( "Adding " + type.toString() );
		removeGroups( type );
		
		for( String agid : accessGroupIds ) {
			try {
				long id = new Long( agid );
				Group grp = (Group) SeventyEight.getInstance().getResource( id );
				addGroup( grp, type );
			} catch( Exception e ) {
				logger.warn( "Unable to add group: " + e.getMessage() );
			}
		}
	}
	
	public void addGroup( Group group, GroupEdgeType type ) {
		//SeventyEight.getInstance().createEdge( db, this, group, type );
        node.createEdge( group.getNode(), type );
	}
	
	protected void removeGroups( GroupEdgeType type ) {
		logger.debug( "Removing all " + type.toString() + " for " + this );
		SeventyEight.getInstance().removeOutEdges( db, this, type );
	}
	
	public void setTitle( String title, Locale locale ) {
		setText( "title", title, locale.getLanguage() );
	}

	public String getTitle() {
		try {
			Text t = getText( locale.getLanguage(), "title" );
			return t.getText();
		} catch( TextNodeDoesNotExistException e ) {
			logger.warn( "Could not get title for " + locale.getLanguage() );
			return "???";
		}
	}
	
	public void setSubTitle( String subTitle ) {
		node.set( "subtitle", subTitle );
		node.save();
	}

	public String getSubTitle() {
		return getField( "subtitle", "" );
	}
	
	public void setDescription( String description, Locale locale ) {
		setText( "description", description, locale.getLanguage() );
	}

	/**
	 * Should be text?
	 * @return
	 */
	public String getDescription() {
		return getField( "description", "" );
	}
	
	public Descriptor<?> getDescriptor() {
		return SeventyEight.getInstance().getDescriptor( getClass() );
	}
	
	/*
	public void updateIndexes( Index<Node> idx ) {
		super.updateIndexes( idx );
		
		logger.debug( "Adding object indexes for " + this );
		idx.add( getNode(), "title", getTitle().toLowerCase() );
		idx.add( getNode(), "class", getClass() );
		
		List<Group> gs = getAccessGroups();
		logger.debug( "Storing accessible groups" );
		for( Group g : gs ) {
			logger.debug( "GROUP: " + g.getIdentifier() );
			idx.add( getNode(), "group", g.getIdentifier() );
		}
		
		logger.debug( "Store owner " );
		idx.add( getNode(), "owner", getOwner().getIdentifier() );
	}
	*/
	
	@Override
	public String toString() {
		return "[" + getIdentifier() + "]" + getDisplayName();
	}
	
	@Override
	public boolean equals( Object other ) {
		if( other instanceof AbstractObject ) {
			return this.getIdentifier().equals( ((AbstractObject)other).getIdentifier() );
		} else {
			return false;
		}
	}
}
