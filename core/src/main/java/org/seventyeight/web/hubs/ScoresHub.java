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
public class ScoresHub extends AbstractHub {

    private static Logger logger = Logger.getLogger( ScoresHub.class );

    public ScoresHub( Node node ) {
        super( node );
    }

    @Override
    public EdgeType getEdgeType() {
        return SeventyEight.ItemRelation.scores;
    }

    @Override
    public String getDisplayName() {
        return "Scores for " + getParent().getDisplayName();
    }

    @Override
    public void addScore( String name, double score ) {
        logger.debug( "[Score] " + name + ": " + score );

        double current = getNode().get( name, 0.0 );
        getNode().set( name, current + score );
        getNode().save();
    }

    public double getScore( String name ) {
        return getNode().get( name, 0.0 );
    }
}
