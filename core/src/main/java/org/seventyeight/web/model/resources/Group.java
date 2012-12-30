package org.seventyeight.web.model.resources;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.InconsistentParameterException;
import org.seventyeight.web.exceptions.IncorrectTypeException;
import org.seventyeight.web.exceptions.ParameterDoesNotExistException;
import org.seventyeight.web.exceptions.ResourceDoesNotExistException;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.Extension;
import org.seventyeight.web.model.ParameterRequest;
import org.seventyeight.web.model.ResourceDescriptor;

import com.google.gson.JsonObject;


public class Group extends AbstractResource {
	
	private static Logger logger = Logger.getLogger( Group.class );
	
	public enum GroupMember implements EdgeType {
		member
	}
	
	public boolean selected = false;
	
	public Group( Node node ) {
		super( node );
	}

	public void doSave( ParameterRequest request, JsonObject jsonData ) throws ResourceDoesNotExistException, ParameterDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		save( new GroupSaveImpl( this, request, jsonData ) );
	}
	
	public class GroupSaveImpl extends ResourceSaveImpl {

		public GroupSaveImpl( AbstractResource resource, ParameterRequest request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			
			/* Save the list of members */
			String[] users = request.getParameterValues( "users" );
			if( users != null ) {
				for( String user : users ) {
					try {
						logger.debug( "Adding " + user + " to group" );
						Long id = new Long( user );
						AbstractResource r = SeventyEight.getInstance().getResource( getDB(), id );
						//SeventyEight.getInstance().createEdge( db, resource, r, GroupMember.member );
                        //resource.getNode().createEdge( r.getNode(), GroupMember.member );
                        resource.createRelation( r, GroupMember.member );
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
		
		//List<Node> nodes = AbstractResource.getResources( User.__TYPENAME );
        List<Edge> edges = this.getNode().getEdges( GroupMember.member, Direction.OUTBOUND );
		for( Edge edge : edges ) {
			User user = new User( edge.getTargetNode() );
			logger.debug( "Adding " + user + " to group" );
			users.add( user );
		}
		
		return users;
	}
	
	public void addMember( User user ) {
		//SeventyEight.getInstance().createEdge( db, this, user, GroupMember.member );
        this.createRelation( user, GroupMember.member );
	}
	
	public boolean removeMember( User user ) {
		logger.debug( "Removing member " + user );
		//Iterator<Relationship> rls = node.getRelationships( Direction.OUTGOING, RelationShips.MEMBER ).iterator();
		//List<Edge> edges = SeventyEight.getInstance().getEdges2( db, this, GroupMember.member );
        List<Edge> edges = node.getEdges( GroupMember.member, Direction.OUTBOUND );
		
		//while( rls.hasNext() ) {
		for( Edge edge : edges ) {
			if( edge.getTargetNode().equals( user.getNode() ) ) {
				logger.debug( "Deleting node " + edge );
				edge.delete();
				return true;
			}
		}
		
		return false;
	}
	
	public void removeMembers() {
		//SeventyEight.getInstance().removeOutEdges( db, this, GroupMember.member );
        node.removeEdges( GroupMember.member, Direction.OUTBOUND );
	}
	
	public boolean isMember( User user ) {
		logger.debug( "is " + user + " member of " + this );
		//List<Edge> edges = SeventyEight.getInstance().getEdges2( db, this, GroupMember.member );
        List<Edge> edges = node.getEdges( user.getNode(), GroupMember.member, Direction.OUTBOUND );
		
		if( edges.size() > 0 ) {
            return true;
        } else {
            return false;
        }
	}

	public String getType() {
		return "group";
	}
	
	/*
	public void updateIndexes( Index<Node> idx ) {
		super.updateIndexes( idx );
		
		logger.debug( "Adding group indexes for " + this );
	}
	*/
	
	public boolean isSelected() {
		return selected;
	}
	
	public static List<Group> getAllGroups( Database db ) {
		return getAllGroups( db, new ArrayList<Group>() );
	}
	
	public static List<Group> getAllGroups( Database db, List<Group> selected ) {
		logger.debug( "Getting all groups" );
		List<Group> groups = new ArrayList<Group>();

        List<Node> nodes = db.getFromIndex( SeventyEight.INDEX_RESOURCE_TYPES, "group" );
		for( Node node : nodes ) {
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

	}

	public String getPortrait() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
