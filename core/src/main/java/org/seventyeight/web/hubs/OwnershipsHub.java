package org.seventyeight.web.hubs;

import org.apache.log4j.Logger;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.model.AbstractHub;
import org.seventyeight.web.model.DatabaseItem;
import org.seventyeight.web.model.Descriptor;

/**
 * @author cwolfgang
 *         Date: 24-01-13
 *         Time: 13:46
 */
public class OwnershipsHub extends AbstractHub {

    private static Logger logger = Logger.getLogger( OwnershipsHub.class );

    public OwnershipsHub( Node node ) {
        super( node );
    }

    @Override
    public EdgeType getEdgeType() {
        return SeventyEight.ResourceEdgeType.ownerships;
    }

    @Override
    public String getDisplayName() {
        return "Ownerships for " + getParent().getDisplayName();
    }

    public void addOwnership( DatabaseItem item ) {
        item.createRelation( this, SeventyEight.ResourceEdgeType.owner );
    }

    public static class OwnershipsHubDescriptor extends Descriptor<OwnershipsHub> {

        @Override
        public String getDisplayName() {
            return "Ownerships hub";
        }

        @Override
        public EdgeType getRelationType() {
            return SeventyEight.ResourceEdgeType.ownerships;
        }
    }
}
