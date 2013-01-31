package org.seventyeight.web.authentication;

import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.model.AbstractHub;
import org.seventyeight.web.model.Descriptor;

/**
 * @author cwolfgang
 *         Date: 31-01-13
 *         Time: 22:12
 */
public class SessionsHub extends AbstractHub {

    public SessionsHub( Node node ) {
        super( node );
    }

    @Override
    public EdgeType getEdgeType() {
        return Session.SessionEdge.sessions;
    }

    @Override
    public String getDisplayName() {
        return "Sessions hub";
    }

    public void addSession( Session session ) {
        this.createRelation( session, Session.SessionEdge.session );
    }

    public static class SessionsHubDescriptor extends Descriptor<AbstractHub> {

        @Override
        public String getDisplayName() {
            return "Sessions hub descriptor";
        }

        @Override
        public EdgeType getRelationType() {
            return Session.SessionEdge.sessions;
        }
    }
}
