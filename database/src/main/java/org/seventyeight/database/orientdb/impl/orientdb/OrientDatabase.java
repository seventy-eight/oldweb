package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import org.seventyeight.database.Database;

/**
 * User: cwolfgang
 * Date: 19-11-12
 * Time: 12:29
 */
public class OrientDatabase implements Database<OGraphDatabase, OrientNode> {

    private OGraphDatabase db;

    public OrientDatabase( OGraphDatabase db ) {
        this.db = db;
    }

    public OGraphDatabase getInternalDatabase() {
        return db;
    }

    @Override
    public OrientNode createNode() {
        return new OrientNode( this );
    }
}
