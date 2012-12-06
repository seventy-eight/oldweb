package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.graph.OGraphDatabasePool;
import com.orientechnologies.orient.core.db.object.ODatabaseObject;
import org.seventyeight.database.Database;

/**
 * @author cwolfgang
 *         Date: 30-11-12
 *         Time: 14:20
 */
public class OrientDBManager {

    private static OrientDBManager instance;
    private String qualifiedPath;

    private OrientDBManager() {}

    public OrientDBManager( String type, String path ) {
        this.qualifiedPath = type + ":" + path;
        try {
            new OrientDatabase( (OGraphDatabase) new OGraphDatabase( qualifiedPath ).create() );
        } catch( Exception e ) {

        }

        instance = this;
    }

    public static OrientDBManager getInstance() {
        return instance;
    }

    public Database getDatabase() {
        return new OrientDatabase( OGraphDatabasePool.global().acquire( qualifiedPath, "admin", "admin" ) );
    }

    public static void createDatabase( String type, String path ) {
        new OrientDatabase( (OGraphDatabase) new OGraphDatabase( type + ":" + path ).create() );
    }
}
