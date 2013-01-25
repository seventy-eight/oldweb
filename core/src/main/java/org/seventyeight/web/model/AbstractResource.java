package org.seventyeight.web.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.utils.Date;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.extensions.ResourceExtension;
import org.seventyeight.web.model.resources.User;

import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletResponse;


public abstract class AbstractResource extends AbstractObject implements Portraitable, Actionable {
	
	private static Logger logger = Logger.getLogger( AbstractResource.class );

	protected AbstractResource( Node node ) {
		super( node );
	}
	
	public AbstractResource( Node node, Locale locale ) {
		super( node, locale );
	}
	
	public class ResourceSaveImpl extends ObjectSave {
		protected AbstractResource resource;
		public ResourceSaveImpl( AbstractResource resource, CoreRequest configuration, JsonObject jsonData ) {
			super( resource, configuration, jsonData );
			
			this.resource = resource;
		}
		
		@Override
		public void before() {
			/* Remove resource from newly created */
			//GraphDragon.getInstance().acceptResource( (AbstractResource)item );

            /* Check id */
            if( node.get( "identifier" ) == null ) {
                logger.debug( "Setting identifier for " + resource );
                SeventyEight.getInstance().setIdentifier( resource );
            }
		}
		
		@Override
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();

            logger.debug( "[[SAVING RESOURCE]]" );
			
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

            node.set( "debatable", true );
		}
		
		public void setOwner() {
			Long ownerId = request.getValue( "owner" );
			if( ownerId != null ) {
				logger.debug( "OwnerId is " + ownerId );
				try {
					AbstractResource o = SeventyEight.getInstance().getResource( getDB(), ownerId );
					if( o instanceof User ) {
						logger.debug( "Owner is " + ownerId );
						AbstractResource.this.setOwner( (User)o );
						//node.createRelationshipTo( o.getNode(), SessionEdge.OWNER );
					} else {
						logger.error( "Not a user: " + o );
					}
				} catch( Exception e ) {
					logger.warn( "Unable to set owner: " + e.getMessage() );
				}
			} else {
				/* If no other owner has been specified, use the current user */
				AbstractResource.this.setOwner( request.getUser() );
				//node.createRelationshipTo( request.getUser().getNode(), SessionEdge.OWNER );
				
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
		node.set( "deleted", deleted.getTime() );
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

	public static List<Node> getResourcesNodes( Database db, String type ) {
		List<Node> list = new ArrayList<Node>();
		//IndexHits<Node> nodes = GraphDragon.getInstance().getResourceIndex().get( "type", type );
        List<Edge> edges = db.getFromIndex( SeventyEight.INDEX_RESOURCE_TYPES, type );
		
		for( Edge edge : edges ) {
            list.add( edge.getTargetNode() );
        }
		
		return list;
	}

	public void updateIndexes() {
		super.updateIndexes();
		
		//logger.debug( "Updating resource index for " + this + "/" + getNode() );

        getDB().removeNodeFromIndex( SeventyEight.INDEX_RESOURCE_TYPES, node );

        /* Update the type index */
        getDB().putToIndex( SeventyEight.INDEX_RESOURCE_TYPES, node, ((ResourceDescriptor)getDescriptor()).getType(), getCreated() );

        getDB().putToIndex( SeventyEight.INDEX_RESOURCES, node, getIdentifier() );
	}

    @Override
    public String getUrl() {
        return "/resource/" + getIdentifier();
    }

    @Override
    public List<Item> getContributingViews( String view, AbstractTheme theme ) {
        logger.debug( "Getting contributing views for " + view );

        List<Node> nodes = getExtensionNodesByExtensionClass( ResourceExtension.class );

        List<Item> items = new ArrayList<Item>( nodes.size() );
        for( Node n : nodes ) {
            try {
                DatabaseItem item = SeventyEight.getInstance().getDatabaseItem( n );
                try {
                    SeventyEight.getInstance().getTemplateManager().getTemplate( theme, item, view, false );
                    items.add( item );
                } catch( TemplateDoesNotExistException e ) {
                    logger.debug( view + " does not exist for " + item );
                }

            } catch( CouldNotLoadObjectException e ) {
                logger.warn( e.getMessage() );
            }
        }

        return items;
    }

    @Override
    public EdgeType getEdgeType() {
        return null;
    }

    @Override
    public Action getAction( Request request, String urlName ) {
        List<Edge> edges = node.getEdges( SeventyEight.ResourceEdgeType.action, Direction.OUTBOUND, "action", urlName );

        if( edges.size() == 0 ) {
            return null;
        } else {
            try {
                return (Action) SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getTargetNode() );
            } catch( CouldNotLoadObjectException e ) {
                logger.warn( e.getMessage() );
                return null;
            }
        }
    }

    public void doIndex( Request request, HttpServletResponse response ) {
        logger.debug( "[VIEWING] " + this );
        try {
            request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( this, "index.vm" ) );
            response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
        } catch( TemplateDoesNotExistException e ) {
            e.printStackTrace();
        } catch( IOException e ) {
            e.printStackTrace();
        }


    }
}
