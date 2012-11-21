package org.seventyeight.database;

import org.seventyeight.utils.Builder;

/**
 * @author cwolfgang
 *         Date: 21-11-12
 *         Time: 13:19
 */
public abstract class AbstractBuilder<T> implements Builder<T> {

    protected boolean built = false;

    @Override
    public boolean isBuilt() {
        return built;
    }
}
