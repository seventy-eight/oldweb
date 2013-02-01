package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.index.OIndex;
import org.seventyeight.database.Index;

/**
 * @author cwolfgang
 *         Date: 01-02-13
 *         Time: 08:35
 */
public class OrientIndex implements Index {

    private OIndex<?> index;

    public OrientIndex( OIndex<?> index ) {
        this.index = index;
    }

    public OIndex<?> getIndex() {
        return index;
    }
}
