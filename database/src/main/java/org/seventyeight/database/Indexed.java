package org.seventyeight.database;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 01-02-13
 *         Time: 13:07
 */
public interface Indexed {
    public List<String> getIndexNames();

    public void updateIndexes();
}
