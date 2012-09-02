package org.seventyeight.web.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.InconsistentParameterException;


import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;


public abstract class AbstractObject extends AbstractItem implements Ownable, Configurable {

	private static Logger logger = Logger.getLogger( AbstractObject.class );

	public static final String __ACCESS_GROUP_NAME = "access-group";
	public static final String __EDITOR_GROUP_NAME = "editor-group";
	public static final String __REVIEW_GROUP_NAME = "review-group";
	
	
	public AbstractObject( ODocument node ) {
		super( node );
	}
	
	public AbstractObject( ODocument node, Locale locale ) {
		super( node, locale );
	}
	
	protected abstract class ObjectSave extends Save {
		protected AbstractObject object;
		protected String language;
		
		public ObjectSave( AbstractObject object, Request configuration, JsonObject jsonData ) {
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
					language = GraphDragon.getInstance().getDefaultLocale().getLanguage();
				}
			}
			logger.debug( "Language is " + language );
			
			/* Setting title */
			String title = request.getKey( "title" );
			logger.debug( "Title is " + title );
			if( title != null ) {
				setText( "title", title, language );
				//node.setProperty( "title", title );
			}
			
			String subtitle = request.getKey( "subtitle" );
			if( subtitle != null ) {
				node.setProperty( "subtitle", subtitle );
			}
			
			/* Setting description */
			String description = request.getKey( "description" );
			if( description != null ) {
				setText( "description", description, language );
				//node.setProperty( "description", description );
			}
			
			/* Access groups */
			String[] accessGroupIds = request.getKeys( __ACCESS_GROUP_NAME );
			if( accessGroupIds != null ) {
				addGroupsById( accessGroupIds, GroupRelation.GROUP_HAS_ACCESS );				
			}
			
			/* Editor groups */
			String[] editorGroupIds = request.getKeys( __EDITOR_GROUP_NAME );
			if( editorGroupIds != null ) {
				addGroupsById( editorGroupIds, GroupRelation.GROUP_CAN_EDIT );				
			}
			
			/* Review groups */
			String[] reviewGroupIds = request.getKeys( __REVIEW_GROUP_NAME );
			if( reviewGroupIds != null ) {
				addGroupsById( reviewGroupIds, GroupRelation.GROUP_CAN_REVIEW );				
			}
		}
		
		@Override
		public void after() {
			setOwner();
		}
		
		public void setOwner() {
			AbstractObject.this.setOwner( request.getUser() );
		}
		
		@Override
		public void updateIndexes() {
			logger.debug( "UPDATING INDEXES: " + getIdentifier() );
			object.updateIndexes( GraphDragon.getInstance().getResourceIndex() );
		}
		
	}
	
	public void setText( String property, String value, String language ) {
		logger.debug( "Setting " + property + " text node for " + language + " to " + value );
		
		Text tt = null;
		try {
			tt = getText( language, property );
		} catch( TextNodeDoesNotExistException e ) {
			logger.debug( "The " + property + " text node for " + language + " does not exist, creating it" );
			tt = Text.create( AbstractObject.this, property, language );
		}
		tt.setText( language, value );
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
		Iterator<Relationship> it = node.getRelationships( Direction.OUTGOING, I18N.TRANSLATION ).iterator();
		
		Node d = null;

		while( it.hasNext() ) {
			Relationship r = it.next();

			if( r.getProperty( "property", "" ).equals( property ) ) {
				if( r.getProperty( "language", "" ).equals( language ) ) {
					return r.getEndNode();
				} else {
					d = r.getEndNode();
				}
			}
		}
		
		return d;
	}
	
	public User getOwner() {
		return new User( node.getSingleRelationship( Relationships.OWNER, Direction.OUTGOING ).getEndNode() );
	}

	public void setOwner( User owner ) {
		/* Removing all owners */
		removeAllOwners();
		/* Adding new owner */
		node.createRelationshipTo( owner.getNode(), Relationships.OWNER );
	}
	
	protected void removeAllOwners() {
		logger.debug( "Removing all owners for " + this );
		Iterator<Relationship> i = node.getRelationships( Relationships.OWNER ).iterator();
		while( i.hasNext() ) {
			i.next().delete();
		}
	}
	
	public List<Group> getGroups( GroupRelation rel ) {
		logger.debug( "Getting all groups for  " + rel.name() );
		
		List<Group> groups = new ArrayList<Group>();
		
		Iterator<Relationship> i = node.getRelationships( rel ).iterator();
		while( i.hasNext() ) {
			Group grp = new Group( i.next().getEndNode() );
			groups.add( grp );
		}
		
		return groups;
	}
	
	public List<Group> getGroups( String namedEnum ) {
		return getGroups( GroupRelation.valueOf( namedEnum ) );
	}
	
	public List<Group> getAccessGroups() {
		return getGroups( GroupRelation.GROUP_HAS_ACCESS );
	}
	
	public void addGroupsById( String[] accessGroupIds, GroupRelation rel ) {
		logger.debug( "Adding " + rel.name() );
		removeGroups( rel );
		
		for( String agid : accessGroupIds ) {
			try {
				long id = new Long( agid );
				Group grp = (Group) GraphDragon.getInstance().getResource( id );
				addGroup( grp, rel );
			} catch( Exception e ) {
				logger.warn( "Unable to add group: " + e.getMessage() );
			}
		}
	}
	
	public void addGroup( Group group, GroupRelation rel ) {
		node.createRelationshipTo( group.getNode(), rel );
	}
	
	protected void removeGroups( GroupRelation rel ) {
		logger.debug( "Removing all " + rel.name() + " for " + this );
		Iterator<Relationship> i = node.getRelationships( rel ).iterator();
		while( i.hasNext() ) {
			i.next().delete();
		}
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
		node.setProperty( "subtitle", subTitle );
	}

	public String getSubTitle() {
		return (String) node.getProperty( "subtitle", "" );
	}
	
	public void setDescription( String description, Locale locale ) {
		setText( "description", description, locale.getLanguage() );
	}

	public String getDescription() {
		return (String) node.getProperty( "description", "" );
	}
	
	public Descriptor<?> getDescriptor() {
		return GraphDragon.getInstance().getDescriptor( getClass() );
	}
		
	public void updateIndexes( Index<Node> idx ) {
		super.updateIndexes( idx );
		
		logger.debug( "Adding object indexes for " + this );
		idx.add( getNode(), "title", getTitle().toLowerCase() );
		idx.add( getNode(), "class", getClass() );
		
		/**/
		List<Group> gs = getAccessGroups();
		logger.debug( "Storing accessible groups" );
		for( Group g : gs ) {
			logger.debug( "GROUP: " + g.getIdentifier() );
			idx.add( getNode(), "group", g.getIdentifier() );
		}
		
		logger.debug( "Store owner " );
		idx.add( getNode(), "owner", getOwner().getIdentifier() );
	}
	
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
