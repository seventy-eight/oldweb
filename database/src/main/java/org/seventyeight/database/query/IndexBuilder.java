package org.seventyeight.database.query;

import org.seventyeight.database.IndexValueType;
import org.seventyeight.database.QueryBuilder;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 21:02
 */
public interface IndexBuilder<T> extends QueryBuilder {
    public T setIndexType( IndexType type );
    public T setIndexValueType( IndexValueType valueType );
    public String getIndexName();
}
