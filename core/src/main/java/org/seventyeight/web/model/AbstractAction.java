package org.seventyeight.web.model;

import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;

/**
 * @author cwolfgang
 *         Date: 21-01-13
 *         Time: 15:00
 */
public abstract class AbstractAction extends AbstractItem implements Action {

    public AbstractAction( Node node ) {
        super( node );
    }

    @Override
    public EdgeType getEdgeType() {
        return SeventyEight.ResourceEdgeType.action;
    }
}
