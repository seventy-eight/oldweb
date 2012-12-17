package org.seventyeight.web.model;

import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 17-12-12
 *         Time: 21:34
 */
public class Conversation extends AbstractDatabaseItem {

    public enum ET implements EdgeType {
        reply
    }

    public Conversation( Node node ) {
        super( node );
    }

    public void getReplies() {
        List<Edge> edges = node.getEdges( ET.reply, Direction.OUTBOUND );
    }
}
