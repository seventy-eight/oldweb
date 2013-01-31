package org.seventyeight.web.authentication;

import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.database.IndexType;
import org.seventyeight.database.IndexValueType;
import org.seventyeight.database.Node;
import org.seventyeight.utils.NetworkUtils;
import org.seventyeight.utils.Utils;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.authentication.exceptions.UnableToCreateSessionException;
import org.seventyeight.web.exceptions.PersistenceException;
import org.seventyeight.web.model.resources.User;


public class SessionManager {
	
	private static Logger logger = Logger.getLogger( SessionManager.class );
	
    public static final String INDEX_SESSIONS = "sessions";
	
	public SessionManager( Database db ) {
        db.createIndex( INDEX_SESSIONS, IndexType.UNIQUE, IndexValueType.STRING );
		logger.debug( "Initializing session index" );
	}
	
	private Node createSessionNode( Database db, String hash, Date endDate ) {
		logger.debug( "Creating new session" );
		
		Node node = null;
	
		node = SeventyEight.getInstance().createNode( db, Session.class );
		node.set( "hash", hash );
		node.set( "created", new Date().getTime() );
		node.set( Session.__END_DATE, endDate.getTime() );

        node.set( "identity", NetworkUtils.getNetworkIdentity() );

        node.save();
		
		return node;
	}
	
	public Session createSession( Database db, User user, Date date, int ttl ) throws UnableToCreateSessionException, PersistenceException {
		logger.debug( "Creating session for " + user + ", " + ttl );
		String hash = "";
		try {
			hash = Utils.md5( user.getUsername() + date.getTime() );
		} catch( NoSuchAlgorithmException e ) {
			logger.warn( "Unable to create session: " + e.getMessage() );
			throw new UnableToCreateSessionException( e );
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( date );
		calendar.add( Calendar.HOUR_OF_DAY, ttl );
		
		Node node = createSessionNode( db, hash, calendar.getTime() );
		//node.setProperty( "userid", user.getIdentifier() );
		
		/* Add to index */
		//sessionsIndexes.add( node, "hash", hash );
        db.putToIndex( INDEX_SESSIONS, node, hash );
		
		Session session = new Session( node );

        SessionsHub hub = user.getSessionsHub();
        hub.addSession( session );
		
		return session;
	}
	
	public Session getSession( Database db, String hash ) {
		logger.debug( "Getting session for " + hash );

        List<Node> nodes = db.getFromIndex( INDEX_SESSIONS, hash );

        Session actual = null;
        for( Node node : nodes ) {
            Session session = new Session( node );
            //logger.debug( "Comparing " + session.getEndingAsDate().getTime() + " with " + new Date().getTime() + ", ident: " + session.getIdentity() );
            logger.debug( "Comparing " + session.getEndingAsDate().getTime() + " with " + new Date().getTime() );
            //if( session.getEndingAsDate().after( new Date() ) && ( identity == null || identity.equals( session.getIdentity() )) ) {
            if( session.getEndingAsDate().after( new Date() ) ) {
                logger.debug( "A valid session found" );
                actual = session;
            } else {
                logger.debug( "Session has expired" );
                /* TODO remove session??? */
            }
        }

		return actual;
	}
	
	public void removeSession( Database db, String hash ) {
        logger.debug( "[Removing sessions] " + hash );
        List<Node> nodes = db.getFromIndex( INDEX_SESSIONS, hash );
		
		for( Node node : nodes ) {
			node.remove();
		}
	}
}
