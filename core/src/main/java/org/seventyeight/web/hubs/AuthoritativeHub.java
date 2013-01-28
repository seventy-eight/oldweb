package org.seventyeight.web.hubs;

import org.apache.log4j.Logger;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.model.AbstractHub;
import org.seventyeight.web.model.Descriptor;

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
        return "Authoritative for " + getParent().getDisplayName();
    }

    public void setType() {

    }

    public String getType() {
        return node.get( "type", null );
    }

    public static class AuthoritativeHubDescriptor extends Descriptor<AuthoritativeHub> {

        @Override
        public String getDisplayName() {
            return "Authoritative hub";
        }

        @Override
        public EdgeType getRelationType() {
            return getEdgeType();
        }
    }
}
