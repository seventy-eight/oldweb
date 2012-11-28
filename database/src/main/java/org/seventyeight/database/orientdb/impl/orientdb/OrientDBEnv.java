package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import org.seventyeight.database.EdgeType;

/**
 * @author cwolfgang
 *         Date: 27-11-12
 *         Time: 13:37
 */
public class OrientDBEnv {
    private OrientDatabase db;

    public OrientDBEnv() {
        db = new OrientDatabase( (OGraphDatabase) new OGraphDatabase( "memory:test" ).create() );
        System.out.println( "DB: " + db.getInternalDatabase() );
    }

    public OrientDatabase getDB() {
        return db;
    }

    public OrientNode createNode( String key, Object value ) {
        OrientNode n = new OrientNode( db );
        n.set( key, value );
        n.save();

        return n;
    }

    private void terminate() {
        db.getInternalDatabase().close();
    }
}
