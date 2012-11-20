package org.seventyeight.utils;

import java.security.PublicKey;

/**
 * User: cwolfgang
 * Date: 20-11-12
 * Time: 20:58
 */
public interface Builder<T, RT> {

    /**
     * Build the {@link Builder}
     * @return
     */
    public T build();

    /**
     * Determine whether the {@link Builder} is built
     * @return
     */
    public boolean isBuilt();

    /**
     * Get the return value when built
     * @return
     */
    public RT get();
}
