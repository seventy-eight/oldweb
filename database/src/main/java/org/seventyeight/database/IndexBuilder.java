package org.seventyeight.database;

import org.seventyeight.utils.Builder;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 21:02
 */
public interface IndexBuilder<T> extends Builder<T, Boolean> {
    public T setUniqueness( boolean unique );
    public T setFulltextIndexed( boolean fulltextIndexed );
    public T setIndexType( IndexType type );
    public String getIndexName();
}
