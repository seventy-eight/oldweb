package org.seventyeight.web.model;

import org.seventyeight.database.Database;

/**
 * @author cwolfgang
 *         Date: 26-01-13
 *         Time: 21:05
 */
public interface ItemType extends TopLevelGizmo {
    public AbstractItem getItem( String name, Database db );
}
