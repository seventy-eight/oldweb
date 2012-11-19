package org.seventyeight.web.model;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import org.apache.log4j.Logger;
import org.seventyeight.database.Edge;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.CouldNotLoadResourceException;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.InconsistentParameterException;
import org.seventyeight.web.exceptions.ThemeDoesNotExistException;
import org.seventyeight.web.model.resources.User;
import org.seventyeight.web.util.Date;

import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;


public abstract class AbstractResource<NODE extends Node<NODE, EDGE>, EDGE extends Edge<EDGE, NODE>> extends AbstractObject<NODE, EDGE> implements Portraitable {
	
	private static Logger logger = Logger.getLogger( AbstractResource.class );

	protected AbstractResource( NODE node ) {
		super( node );
	}
	
	public AbstractResource( NODE node, Locale locale ) {
		super( node, locale );
	}
	
	public class ResourceSaveImpl extends ObjectSave {
		protected AbstractResource resource;
		public ResourceSaveImpl( AbstractResource resource, ParameterRequest configuration, JsonObject jsonData ) {
			super( resource, configuration, jsonData );
			
			this.resource = resource;
		}
		
		@Override
		public void before() {
			/* Remove resource from newly created */
			//GraphDragon.getInstance().acceptResource( (AbstractResource)item );
		}
		
		@Override
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			
			String theme = request.getParameter( "theme" );
			if( theme != null ) {
				logger.debug( "Setting theme: " + theme );
				node.set( "theme", theme );
			}
						
			Integer rev = getField( "revision", 0 );
			logger.debug( "REVISION: " + rev );
			if( rev != null ) {
				node.set( "revision", rev + 1 );
			}
			
			if( rev > 0 ) {
				node.set( "updated", new Date().getTime() );
			}
		}
		
		public void setOwner() {
			Long ownerId = request.getValue( "owner" );
			if( ownerId != null ) {
				logger.debug( "OwnerId is " + ownerId );
				try {
					AbstractResource o = SeventyEight.getInstance().getResource( ownerId );
					if( o instanceof User ) {
						logger.debug( "Owner is " + ownerId );
						AbstractResource.this.setOwner( (User)o );
						//node.createRelationshipTo( o.getNode(), Relationships.OWNER );
					} else {
						logger.error( "Not a user: " + o );
					}
				} catch (CouldNotLoadResourceException e) {
					logger.warn( "Unable to set owner: " + e.getMessage() );
				}
			} else {
				/* If no other owner has been specified, use the current user */
				AbstractResource.this.setOwner( request.getUser() );
				//node.createRelationshipTo( request.getUser().getNode(), Relationships.OWNER );
				
			}
		}

	}
	
		
	public Date getCreatedAsDate() {
		return new Date( (Long)getField( "created" ) );
	}
	
	public Long getCreated() {
		return getField( "created" );
	}

	public void setCreated( Date created ) {
		node.set( "created", created.getTime() );
	}	

	public Date getUpdatedAsDate() {
		Long l = getField( "updated", null );
		if( l != null ) {
			return new Date( l );
		} else {
			return null;
		}
	}
	
	public Long getUpdated() {
		return getField( "updated", null );
	}

	public void setUpdated( Date updated ) {
		node.set( "updated", updated.getTime() );
	}


	public Date getActivatesAsDate() {
		Long l = getField( "activates", null );
		if( l != null ) {
			return new Date( l );
		} else {
			return null;
		}
	}
	
	public Long getActivates() {
		return getField( "activates" );
	}

	public void setActivates( Date activates ) {
		node.set( "activates", activates.getTime() );
	}


	public Date getExpiresAsDate() {
		Long l = getField( "expires", null );
		if( l != null ) {
			return new Date( l );
		} else {
			return null;
		}
	}
	
	public Long getExpires() {
		return getField( "expires" );
	}

	public void setExpires( Date expires ) {
		node.set( "expires", expires.getTime() );
	}

	
	public Date getDeletedAsDate() {
		Long l = getField( "deleted", null );
		if( l != null ) {
			return new Date( l );
		} else {
			return null;
		}
	}
	
	public Long getDeleted() {
		return getField( "deleted" );
	}

	public void setDeleted( Date deleted ) {
		node.field( "deleted", deleted.getTime() );
	}
	
	public Long getViews() {
		return getField( "views", 0l );
	}
	
	public void incrementViews() {
		node.set( "views", getViews() + 1 );
	}
	
	public int getRevision() {
		return getField( "revision", 1 );
	}
	
	public AbstractTheme getTheme() throws ThemeDoesNotExistException {
		return SeventyEight.getInstance().getTheme( getField( "theme", SeventyEight.defaultThemeName ) );
	}
	
	public void setTheme( AbstractTheme theme ) {
		node.set( "theme", theme.getName() );
	}
	
	public boolean hasRatings() {
		return getField( "hasratings", true );
	}
	
	public void setRateble( boolean b ) {
		node.set( "hasratings", b );
	}
	
	public boolean hasComments() {
		return getField( "hascomments", true );
	}
	
	public void setCommentable( boolean b ) {
		node.set( "hascomments", b );
	}
	
	public String getDisplayName() {
		return getTitle();
	}

	public static List<ODocument> getResources( String type ) {
		List<ODocument> list = new ArrayList<ODocument>();
		IndexHits<Node> nodes = GraphDragon.getInstance().getResourceIndex().get( "type", type );
		
		while( nodes.hasNext() ) {
			list.add( nodes.next() );
		}
		
		return list;
	}
	
	/*
	public void updateIndexes( Index<Node> idx ) {
		super.updateIndexes( idx );
		
		logger.debug( "Adding resource indexes for " + this + "/" + getNode() );
		
		idx.add( getNode(), "created", new ValueContext( getCreated() ).indexNumeric() );
		if( getUpdated() != null ) {
			idx.add( getNode(), "updated", new ValueContext( getUpdated() ).indexNumeric() );
		}
		logger.debug( "Type from descriptor: " + this.getDescriptor().getType() );
		idx.add( getNode(), "type", this.getDescriptor().getType().toLowerCase() );
	}
	*/
	

}
