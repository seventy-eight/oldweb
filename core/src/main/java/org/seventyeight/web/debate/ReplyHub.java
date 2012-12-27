package org.seventyeight.web.debate;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.database.utils.SortingUtils;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 11:35
 */
public abstract class ReplyHub extends Hub {

    private static Logger logger = Logger.getLogger( ReplyHub.class );
    public static final String INDEX_REPLIES = "debate-replies";

    public ReplyHub( Node node ) {
        super( node );
    }

    public int getNumberOfFirstLevelReplies() {
        return node.getEdges( null, Direction.OUTBOUND ).size();
    }

    public void addReply( Reply reply ) throws HubException {
        logger.debug( "Adding " + reply );

        int id = addItem( reply, Reply.ReplyRelation.reply );
        long rid = getResourceIdentifier();
        node.getDB().putToIndex( INDEX_REPLIES, reply.getNode(), rid, id );
    }

    public List<Reply> getReplies( int offset, int number ) throws HubException {
        long rid = getResourceIdentifier();
        logger.debug( "RID " + rid );
        List<Node> nodes = node.getDB().getFromIndexAbove( INDEX_REPLIES, number, rid, offset );
        logger.debug( "NODES " + nodes );
        /* Sort 'em */
        Collections.sort( nodes, new SortingUtils.NodeSorter( "id" ) );

        List<Reply> replies = new ArrayList<Reply>( nodes.size() );
        for( Node node : nodes ) {
            try {
                replies.add( (Reply) SeventyEight.getInstance().getDatabaseItem( node ) );
            } catch( CouldNotLoadObjectException e ) {
                logger.warn( "Could not add " + node + " to list" );
            }
        }

        return replies;
    }

    public abstract Descriptor<?> getReplyDescriptor();

    @Override
    public String getDisplayName() {
        return "Reply Hub";
    }

    @Override
    public void doSave( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
        node.set( "type", Debatable.HUB_DEBATE );

        node.save();
    }

    public static abstract class ReplyHubDescriptor extends HubDescriptor {

        @Override
        public String getDisplayName() {
            return "Reply Hub";
        }

        @Override
        public String getType() {
            return "replyHub";
        }

        @Override
        public Class<? extends Extension> getExtensionClass() {
            return null;
        }

        @Override
        public void configureIndex( Database db ) {
            logger.debug( "Configuring " + INDEX_REPLIES );
            // identifier, time
            db.createIndex( INDEX_REPLIES, IndexType.UNIQUE, IndexValueType.LONG, IndexValueType.LONG );
        }


    }
}