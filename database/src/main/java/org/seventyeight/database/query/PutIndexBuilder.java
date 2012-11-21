package org.seventyeight.database.query;

import org.seventyeight.database.QueryBuilder;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 21:02
 */
public interface PutIndexBuilder<T> extends QueryBuilder {
    public T setKey( Object key );
    public String getIndexName();
}
