package org.seventyeight.web.debate.treelike;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 11:35
 */
public class ReplyHub extends Hub {

    private static Logger logger = Logger.getLogger( ReplyHub.class );
    public static final String INDEX_REPLIES = "debate-replies";

    public ReplyHub( Node node ) {
        super( node );
    }

    public int getNumberOfFirstLevelReplies() {
        return node.getEdges( null, Direction.OUTBOUND ).size();
    }

    public void addReply( Reply reply ) {
        int id = addItem( reply, Reply.ReplyRelation.reply );
        AbstractResource resource = getResource();
        node.getDB().putToIndex( INDEX_REPLIES, reply.getNode(), resource.getIdentifier(), id );
    }

    public List<Reply> getReplies( int offset, int number ) {
        List<Edge> edges = node.getEdges( null, Direction.OUTBOUND );
        node.getDB().getFromIndex()
    }

    @Override
    public String getDisplayName() {
        return "Reply Hub";
    }

    @Override
    public void doSave( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
        /* No op */
    }

    public class ReplyHubDescriptor extends HubDescriptor {

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
