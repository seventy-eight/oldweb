package org.seventyeight.web.debate.treelike;

import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.web.debate.ReplyHub;

/**
 * @author cwolfgang
 *         Date: 21-12-12
 *         Time: 13:10
 */
public class ReplyTreeHub extends ReplyHub {

    private static Logger logger = Logger.getLogger( ReplyTreeHub.class );

    public ReplyTreeHub( Node node ) {
        super( node );
    }


    public class ReplyTreeHubDescriptor extends ReplyHubDescriptor {

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
