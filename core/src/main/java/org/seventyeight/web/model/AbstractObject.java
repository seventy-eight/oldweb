package org.seventyeight.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.Indexed;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.GroupEdgeType;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.InconsistentParameterException;
import org.seventyeight.web.exceptions.PersistenceException;
import org.seventyeight.web.exceptions.TextNodeDoesNotExistException;
import org.seventyeight.web.hubs.OwnershipsHub;
import org.seventyeight.web.model.resources.Group;
import org.seventyeight.web.model.resources.User;


import com.google.gson.JsonObject;


public abstract class AbstractObject extends AbstractItem implements Ownable, Describable, Authorizable, Indexed {

	private static Logger logger = Logger.getLogger( AbstractObject.class );

	public static final String __ACCESS_GROUP_NAME = "access-group";
	public static final String __EDITOR_GROUP_NAME = "editor-group";

    protected Locale locale;
	
	public AbstractObject( Node node ) {
		super( node );
        this.locale = SeventyEight.getInstance().getDefaultLocale();
	}
	
	public AbstractObject( Node node, Locale locale ) {
		super( node );
        this.locale = locale;
	}
	
	protected abstract class ObjectSave extends Save {
		protected AbstractObject object;
		protected String language;
		
		public ObjectSave( AbstractObject object, CoreRequest configuration, JsonObject jsonData ) {
			super( object, configuration, jsonData );
			
			this.object = object;
		}
		
		@Override
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
            logger.debug( "[[SAVING OBJECT]]" );
			
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
                node.set( "title", title );
			}
			
			String subtitle = request.getParameter( "subtitle" );
			if( subtitle != null ) {
				//node.set( "subtitle", subtitle );
                node.set( "subtitle", subtitle );
			}
			
			/* Setting description */
			String description = request.getParameter( "description" );
			if( description != null ) {
				//setText( "description", description, language );
                node.set( "description", description );
			}
			
			/* Access groups */
			String[] accessGroupIds = request.getParameterValues( __ACCESS_GROUP_NAME );
			if( accessGroupIds != null ) {
                logger.debug( "GS: " + accessGroupIds );
                logger.debug( "GROUPS: " + Arrays.asList( accessGroupIds ) );
				addGroupsById( accessGroupIds, GroupEdgeType.readAccess );				
			}
			
			/* Editor groups */
			String[] editorGroupIds = request.getParameterValues( __EDITOR_GROUP_NAME );
			if( editorGroupIds != null ) {
				addGroupsById( editorGroupIds, GroupEdgeType.writeAccess );
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

    public void setLocale( Locale locale ) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
	
	public void setText( String property, String value, String language ) {
		logger.debug( "Setting " + property + " text node for " + language + " to " + value );
		
		Text tt = null;
		try {
			tt = getText( language, property );
		} catch( Exception e ) {
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
		//List<ODocument> edges = SeventyEight.getInstance().getEdgesTo( db, this, ResourceEdgeType.translation );
        logger.debug( "The node is " + node );
        List<Edge> edges = node.getEdges( ResourceEdgeType.translation, Direction.OUTBOUND );
		Node d = null;

		for( Edge e : edges ) {

			String prop = e.get( "property" );
			if( prop != null && prop.equals( property ) ) {
				String lang  = e.get( "language" );
				if( lang != null && lang.equals( language ) ) {
					//return SeventyEight.getInstance().getSourceNode( db, e );
                    return e.getTargetNode();
				} else {
					d = e.getTargetNode();
				}
			}
		}
		
		return d;
	}
	
	public User getOwner() throws IllegalStateException, PersistenceException {
        List<Edge> edges = node.getEdges( ResourceEdgeType.owner, Direction.OUTBOUND );
		if( edges.size() == 1 ) {
            //return new User( edges.get( 0 ).getTargetNode() );
            //OwnershipsHub hub = getHub( (Descriptor<? extends AbstractHub>) SeventyEight.getInstance().getDescriptor( OwnershipsHub.class ) );
            OwnershipsHub hub = SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getTargetNode() );
            return hub.getParent();
		} else {
			if( edges.size() > 1 ) {
				throw new IllegalStateException( "Too many owners" );
			} else {
				throw new IllegalStateException( "No owner" );
			}
		}
	}

	public void setOwner( User owner ) {
        logger.debug( "OWNER IS "+ owner + " and node is " + owner.getNode() );
		/* Removing all owners */
		removeAllOwners();
        owner.getNode().update();
        logger.debug( "--------------------> " + this.getNode() );
		/* Adding new ownerships */
        //createRelation( ownerships, ResourceEdgeType.ownerships );
        try {
            OwnershipsHub hub = owner.getHub( (Descriptor<? extends AbstractHub>)SeventyEight.getInstance().getDescriptor( OwnershipsHub.class ) );
            hub.addOwnership( this );
        } catch( PersistenceException e ) {
            logger.warn( e );
        }
        //SeventyEight.getInstance().createEdge( this, ownerships, EdgeType.ownerships );
	}
	
	protected void removeAllOwners() {
		logger.debug( "Removing all owners for " + this );
		try {
            node.removeEdges( ResourceEdgeType.owner, Direction.OUTBOUND );
            logger.debug( "--------------------> " + this.getNode() );
        } catch( Exception e ) {
            logger.warn( e );
        }
	}
	
	public List<Group> getGroups( GroupEdgeType rel ) {
		logger.debug( "Getting all groups for  " + rel.toString() );
		
		List<Group> groups = new ArrayList<Group>();

		//List<ODocument> nodes = SeventyEight.getInstance().getNodes( db, this, rel );
        List<Edge> edges = node.getEdges( rel, Direction.OUTBOUND );
		for( Edge edge : edges ) {
			Group grp = new Group( edge.getTargetNode() );
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

    public void addModerator( Authoritative auth ) {
        List<Edge> edges = node.getEdges( SeventyEight.AuthoritativeEdgeType.authoritative, Direction.OUTBOUND );
    }
	
	public void addGroupsById( String[] accessGroupIds, GroupEdgeType type ) {
		logger.debug( "Adding " + type.toString() );
        try {
		    removeGroups( type );
        } catch( Exception e ) {
            logger.error( e );
        }
        logger.debug( "REMOVINED " );
		
		for( String agid : accessGroupIds ) {
			try {
				long id = new Long( agid );
				Group grp = (Group) SeventyEight.getInstance().getResource( this.getDB(), id );
				addGroup( grp, type );
			} catch( Exception e ) {
				logger.warn( "Unable to add group: " + e.getMessage() );
			}
		}
	}
	
	public void addGroup( Group group, GroupEdgeType type ) {
		//SeventyEight.getInstance().createEdge( db, this, group, type );
        node.createEdge( group.getNode(), type ).save();
	}
	
	protected void removeGroups( GroupEdgeType type ) {
		logger.debug( "Removing all " + type.toString() + " for " + this );
		//SeventyEight.getInstance().removeOutEdges( db, this, type );
        node.removeEdges( type, Direction.OUTBOUND );
	}

    public void setTitle( String title ) {
        setTitle( title, null );
    }

	public void setTitle( String title, Locale locale ) {
        if( locale != null ) {
		    setText( "title", title, locale.getLanguage() );
        }
        node.set( "title", title );
	}

	public String getTitle() {
		try {
			Text t = getText( locale.getLanguage(), "title" );
			return t.getText();
		} catch( Exception e ) {
			logger.debug( "Could not get title for " + locale.getLanguage() );
			return node.get( "title", "" );
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
		
		logger.debug( "Store ownerships " );
		idx.add( getNode(), "ownerships", getOwner().getIdentifier() );
	}
	*/
	
	@Override
	public String toString() {
		return "[" + getIdentifier() + "]" + getDisplayName();
	}

    /*
	@Override
	public boolean equals( Object other ) {
		if( other instanceof AbstractObject ) {
			return this.getIdentifier().equals( ((AbstractObject)other).getIdentifier() );
		} else {
			return false;
		}
	}
	*/

    @Override
    public boolean isOwner( User owner ) throws PersistenceException {
        return getOwner().equals( owner );
    }

    @Override
    public Authorizer getAuthorizer() {
        return this;
    }



}
