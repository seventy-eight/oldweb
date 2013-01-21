package org.seventyeight.web.model;

import org.seventyeight.database.Node;

/**
 * @author cwolfgang
 *         Date: 21-01-13
 *         Time: 15:00
 */
public abstract class AbstractAction extends AbstractDatabaseItem implements Action {

    public AbstractAction( Node node ) {
        super( node );
    }
}
