package org.seventyeight.web.model;

import org.seventyeight.database.Node;
import org.seventyeight.web.model.AbstractItem;

/**
 * @author cwolfgang
 *         Date: 28-11-12
 *         Time: 13:23
 */
public abstract class AbstractSubItem extends AbstractItem {

    public AbstractSubItem( Node node ) {
        super( node );
    }
}
