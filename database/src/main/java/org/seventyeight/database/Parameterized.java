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
     * Get a propertu from the {@link Node}. If the property is not found, return the default value.
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
    public <T> T get( String key, T defaultValue );

    /**
     * Set a property on the {@link Node}
     * @param key
     * @param value
     * @param <T>
     */
    public <T> R set( String key, T value );

    public R save();
}
