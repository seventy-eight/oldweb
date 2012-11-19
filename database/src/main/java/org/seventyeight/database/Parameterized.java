package org.seventyeight.database;

/**
 * User: cwolfgang
 * Date: 19-11-12
 * Time: 16:52
 */
public interface Parameterized<R> {
    /**
     * Get a property from the {@link Node}
     * @param key
     * @param <T>
     * @return
     */
    public <T> T get( String key );

    /**
     * Set a property on the {@link Node}
     * @param key
     * @param value
     * @param <T>
     */
    public <T> R set( String key, T value );

    public R save();
}
