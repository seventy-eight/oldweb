package org.seventyeight.web.model;

import org.seventyeight.database.EdgeType;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 14:53
 */
public interface DatabaseItem<T> {
    public T createRelation( Item other, EdgeType type );
    public String getItemClass();
}
