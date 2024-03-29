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

        OGraphDatabase db = new OGraphDatabase( qualifiedPath );
        if( !db.exists() ) {
            new OrientDatabase( (OGraphDatabase) db.create() );
        }

        instance = this;
    }

    public static OrientDBManager getInstance() {
        return instance;
    }

    public Database getDatabase() {
        OrientDatabase db = new OrientDatabase( OGraphDatabasePool.global().acquire( qualifiedPath, "admin", "admin" ) );
        db.getInternalDatabase().setUseCustomTypes( false );
        return db;
    }

    public static void createDatabase( String type, String path ) {
        new OrientDatabase( (OGraphDatabase) new OGraphDatabase( type + ":" + path ).create() );
    }
}
