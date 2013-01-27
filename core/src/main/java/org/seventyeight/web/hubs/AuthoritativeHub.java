package org.seventyeight.web.hubs;

import org.apache.log4j.Logger;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.model.AbstractHub;

/**
 * @author cwolfgang
 *         Date: 24-01-13
 *         Time: 13:46
 */
public class AuthoritativeHub extends AbstractHub {

    public enum Type {
        viewers,
        moderators
    }

    private static Logger logger = Logger.getLogger( AuthoritativeHub.class );

    public AuthoritativeHub( Node node ) {
        super( node );
    }

    @Override
    public EdgeType getEdgeType() {
        return SeventyEight.AuthoritativeEdgeType.authoritative;
    }

    @Override
    public String getDisplayName() {
        return "Scores for " + getParent().getDisplayName();
    }

    public void setType() {

    }

    public String getType() {
        return node.get( "type", null );
    }
}
