package org.seventyeight.web.authentication;

import java.util.List;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.AbstractItem;
import org.seventyeight.web.model.CoreRequest;
import org.seventyeight.web.model.resources.User;
import org.seventyeight.utils.Date;


public class Session extends AbstractItem {
	
	private static Logger logger = Logger.getLogger( Session.class );

	public static final String __END_DATE = "end";
	
	public enum SessionEdge implements EdgeType {
        session
	}
	
	public Session( Node node ) {
		super( node );
	}

    @Override
    public String getDisplayName() {
        return "Session";
    }

    @Override
    public EdgeType getEdgeType() {
        return SessionEdge.session;
    }

    public void bindToUser( User user ) {
		removeBindings();
		logger.debug( "Binding session to " + user );
		//user.getNode().createRelationshipTo( node, SessionEdge.session );
        user.createRelation( this, SessionEdge.session );
	}
	
	public void removeBindings() {
		logger.debug( "Removing all bindings for session" );
        List<Edge> edges = node.getEdges( SessionEdge.session, Direction.INBOUND );
		//Iterator<Relationship> i = node.getRelationships( SessionEdge.session ).iterator();

        for( Edge e : edges ) {
            e.remove();
        }
	}
	
	public User getUser() {
        List<Edge> edges = node.getEdges( SessionEdge.session, Direction.INBOUND );
        for( Edge e : edges ) {
            return new User( e.getSourceNode() );
        }

		logger.debug( "Session has no user bindings." );
		
		return null;
	}
	
	public String getHash() {
		return node.get( "hash", null );
	}
	
	public Date getEndingAsDate() {
		return new Date( (Long)node.get( __END_DATE ) );
	}
	
	public Node getNode() {
		return node;
	}
}
