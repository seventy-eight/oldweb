package org.seventyeight.model;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.index.lucene.ValueContext;
import org.seventyeight.GraphDragon;
import org.seventyeight.exceptions.CouldNotLoadResourceException;
import org.seventyeight.exceptions.ErrorWhileSavingException;
import org.seventyeight.exceptions.InconsistentParameterException;
import org.seventyeight.exceptions.TemplateDoesNotExistException;
import org.seventyeight.exceptions.ThemeDoesNotExistException;
import org.seventyeight.model.resources.User;
import org.seventyeight.util.Date;

import com.google.gson.JsonObject;


public abstract class AbstractResource extends AbstractObject implements Portraitable {
	
	private static Logger logger = Logger.getLogger( AbstractResource.class );

	protected AbstractResource( Node node ) {
		super( node );
	}
	
	public AbstractResource( Node node, Locale locale ) {
		super( node, locale );
	}
	
	public class ResourceSaveImpl extends ObjectSave {
		public ResourceSaveImpl( AbstractResource resource, RequestContext configuration, JsonObject jsonData ) {
			super( resource, configuration, jsonData );
		}
		
		@Override
		public void before() {
			/* Remove resource from newly created */
			//GraphDragon.getInstance().acceptResource( (AbstractResource)item );
		}
		
		@Override
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			
			String theme = request.getKey( "theme" );
			if( theme != null ) {
				logger.debug( "Setting theme: " + theme );
				node.setProperty( "theme", theme );
			}
						
			Integer rev = (Integer) node.getProperty( "revision", 0 );
			logger.debug( "REVISION: " + rev );
			if( rev != null ) {
				node.setProperty( "revision", rev + 1 );
			}
			
			if( rev > 0 ) {
				node.setProperty( "updated", new Date().getDate().getTime() );
			}
		}
		
		public void setOwner() {
			Long ownerId = request.getIntegerKey( "owner" );
			if( ownerId != null ) {
				logger.debug( "OwnerId is " + ownerId );
				try {
					AbstractResource o = GraphDragon.getInstance().getResource( ownerId );
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
		return new Date( (Long)node.getProperty( "created" ) );
	}
	
	public Long getCreated() {
		return (Long)node.getProperty( "created" );
	}

	public void setCreated( Date created ) {
		node.setProperty( "created", created.getDate().getTime() );
	}
	

	public Date getUpdatedAsDate() {
		Long l = (Long)node.getProperty( "updated", null );
		if( l != null ) {
			return new Date( l );
		} else {
			return null;
		}
	}
	
	public Long getUpdated() {
		return (Long)node.getProperty( "updated", null );
	}

	public void setUpdated( Date updated ) {
		node.setProperty( "updated", updated.getDate().getTime() );
	}


	public Date getActivatesAsDate() {
		return new Date( (Long)node.getProperty( "activates" ) );
	}
	
	public Long getActivates() {
		return (Long)node.getProperty( "activates" );
	}

	public void setActivates( Date activates ) {
		node.setProperty( "activates", activates.getDate().getTime() );
	}


	public Date getExpiresAsDate() {
		return new Date( (Long)node.getProperty( "expires" ) );
	}
	
	public Long getExpires() {
		return (Long)node.getProperty( "expires" );
	}

	public void setExpires( Date expires ) {
		node.setProperty( "expires", expires.getDate().getTime() );
	}

	public Date getDeletedAsDate() {
		return new Date( (Long)node.getProperty( "deleted" ) );
	}
	
	public Long getDeleted() {
		return (Long)node.getProperty( "deleted" );
	}

	public void setDeleted( Date deleted ) {
		node.setProperty( "deleted", deleted.getDate().getTime() );
	}
	
	public Long getViews() {
		return (Long)node.getProperty( "views", 0l );
	}
	
	public void incrementViews() {
		node.setProperty( "views", getViews() + 1 );
	}
	
	public int getRevision() {
		return (Integer) node.getProperty( "revision", 1 );
	}
	
	public AbstractTheme getTheme() throws ThemeDoesNotExistException {
		return GraphDragon.getInstance().getTheme( (String)node.getProperty( "theme", GraphDragon.defaultThemeName ) );
	}
	
	public void setTheme( AbstractTheme theme ) {
		node.setProperty( "theme", theme.getName() );
	}
	
	public boolean hasRatings() {
		return (Boolean)node.getProperty( "hasratings", true );
	}
	
	public void setRateble( boolean b ) {
		node.setProperty( "hasratings", b );
	}
	
	public boolean hasComments() {
		return (Boolean)node.getProperty( "hascomments", true );
	}
	
	public void setCommentable( boolean b ) {
		node.setProperty( "hascomments", b );
	}
	
	public String getDisplayName() {
		return getTitle();
	}

	public static List<Node> getResources( String type ) {
		List<Node> list = new ArrayList<Node>();
		IndexHits<Node> nodes = GraphDragon.getInstance().getResourceIndex().get( "type", type );
		
		while( nodes.hasNext() ) {
			list.add( nodes.next() );
		}
		
		return list;
	}
	
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
	

}
