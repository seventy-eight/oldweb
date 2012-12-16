package org.seventyeight.web.model;

import org.seventyeight.utils.Date;

/**
 * @author cwolfgang
 *         Date: 16-12-12
 *         Time: 23:18
 */
public interface Timestamped {

    /**
     * The key for a timestamp in the database
     */
    public static final String KEY_TIMESTAMP = "timestamp";

    public long getTimestamp();
    public Date getTimestampAsDate();

    /**
     * Set the timestamp
     * @param timestamp
     */
    public void setTimestamp( long timestamp );
}
