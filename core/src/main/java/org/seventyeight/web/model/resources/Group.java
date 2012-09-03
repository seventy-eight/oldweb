package org.seventyeight.model.resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.seventyeight.GraphDragon;
import org.seventyeight.annotations.ResourceType;
import org.seventyeight.exceptions.ErrorWhileSavingException;
import org.seventyeight.exceptions.InconsistentParameterException;
import org.seventyeight.exceptions.IncorrectTypeException;
import org.seventyeight.exceptions.ParameterDoesNotExistException;
import org.seventyeight.exceptions.ResourceDoesNotExistException;
import org.seventyeight.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.model.AbstractObject;
import org.seventyeight.model.AbstractResource;
import org.seventyeight.model.Extension;
import org.seventyeight.model.Portrait;
import org.seventyeight.model.RequestContext;
import org.seventyeight.model.ResourceDescriptor;

import com.google.gson.JsonObject;


@ResourceType
public class Group extends AbstractResource {
	
	private static Logger logger = Logger.getLogger( Group.class );
	
	public boolean selected = false;
	
	public enum RelationShips implements RelationshipType {
		MEMBER
	}


	public Group( Node node ) {
		super( node );
	}

	public void save( RequestContext request, JsonObject jsonData ) throws ResourceDoesNotExistException, ParameterDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		doSave( new GroupSaveImpl( this, request, jsonData ) );
	}
	
	public class GroupSaveImpl extends ResourceSaveImpl {

		public GroupSaveImpl( AbstractResource resource, RequestContext request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			
			/* Save the list of members */
			String[] users = request.getKeys( "users" );
			if( users != null ) {
				GraphDragon gd = GraphDragon.getInstance();
				for( String user : users ) {
					try {
						logger.debug( "Adding " + user + " to group" );
						Long id = new Long( user );
						AbstractResource r = gd.getResource( id );
						node.createRelationshipTo( r.getNode(), RelationShips.MEMBER );
					} catch( Exception e ) {
						logger.warn( "Unable to get user resource: " + e.getMessage() );
					}
				}
			}
		}
		
	}
	
	public List<User> getMembers() {
		logger.debug( "Adding user to group " + this );
		List<User> users = new ArrayList<User>();
		
		List<Node> nodes = AbstractResource.getResources( User.__TYPENAME );
		for( Node node : nodes ) {
			User user = new User( node );
			logger.debug( "Adding " + user + " to group" );
			users.add( user );
		}
		
		return users;
	}
	
	public void addMember( User user ) {
		node.createRelationshipTo( user.getNode(), RelationShips.MEMBER );
	}
	
	public boolean removeMember( User user ) {
		logger.debug( "Removing member " + user );
		Iterator<Relationship> rls = node.getRelationships( Direction.OUTGOING, RelationShips.MEMBER ).iterator();
		
		while( rls.hasNext() ) {
			Relationship rl = rls.next();
			Node node = rl.getEndNode();
			if( node.getId() == user.getNode().getId() ) {
				logger.debug( "Deleting node " + rl );
				rl.delete();
				return true;
			}
		}
		
		return false;
	}
	
	public void removeMembers() {
		Iterator<Relationship> rls = node.getRelationships( Direction.OUTGOING, RelationShips.MEMBER ).iterator();
		
		while( rls.hasNext() ) {
			rls.next().delete();
		}
	}
	
	public boolean isMember( User user ) {
		logger.debug( "is " + user + " member of " + this );
		Iterator<Relationship> rls = node.getRelationships( Direction.OUTGOING, RelationShips.MEMBER ).iterator();
		
		while( rls.hasNext() ) {
			Relationship rl = rls.next();
			Node node = rl.getEndNode();
			if( node.getId() == user.getNode().getId() ) {
				return true;
			}
		}
		
		return false;
	}

	public String getType() {
		return "group";
	}
	
	public void updateIndexes( Index<Node> idx ) {
		super.updateIndexes( idx );
		
		logger.debug( "Adding group indexes for " + this );
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public static List<Group> getAllGroups() {
		return getAllGroups( new ArrayList<Group>() );
	}
	
	public static List<Group> getAllGroups( List<Group> selected ) {
		logger.debug( "Getting all groups" );
		List<Group> groups = new ArrayList<Group>();
		
		Index<Node> idx = GraphDragon.getInstance().getResourceIndex();
		IndexHits<Node> hits = idx.get( "class", Group.class );
		for( Node node : hits ) {
			Group grp = new Group( node );
			logger.debug( "Adding " + grp + " to list" );
			if( selected.contains( grp ) ) {
				grp.selected = true;
			}
			groups.add( grp );
		}
		
		return groups;
	}

	public static class GroupDescriptor extends ResourceDescriptor<Group> {

		@Override
		public String getDisplayName() {
			return "Group";
		}
		
		@Override
		public String getType() {
			return "group";
		}
		
		@Override
		public Class<? extends Extension> getExtensionClass() {
			return null;
		}

		@Override
		public Group newInstance() throws UnableToInstantiateObjectException {
			return super.newInstance();
		}

		/*
		@Override
		public Group get( Node node ) {
			return new Group( node );
		}
		*/
	}

	public String getPortrait() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
