package org.seventyeight.web.debate.treelike;

import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.debate.ReplyHub;
import org.seventyeight.web.debate.ReplyHubExtension;
import org.seventyeight.web.exceptions.HubException;
import org.seventyeight.web.model.Descriptor;
import org.seventyeight.web.model.Reply;

/**
 * @author cwolfgang
 *         Date: 21-12-12
 *         Time: 13:10
 */
public class ReplyTreeHub extends ReplyHub implements ReplyHubExtension {

    private static Logger logger = Logger.getLogger( ReplyTreeHub.class );

    public ReplyTreeHub( Node node ) {
        super( node );
    }

    @Override
    public void addReply( Reply reply ) throws HubException {
        /* TODO determine what reply to add to, for now just */
        super.addReply( reply );
    }

    @Override
    public Descriptor<?> getReplyDescriptor() {
        return SeventyEight.getInstance().getDescriptor( TreeReply.class );
    }

    public static class ReplyTreeHubDescriptor extends ReplyHubDescriptor {

        @Override
        public String getDisplayName() {
            return "Reply Tree Hub";
        }

        @Override
        public String getType() {
            return "replyTreeHub";
        }

    }
}
