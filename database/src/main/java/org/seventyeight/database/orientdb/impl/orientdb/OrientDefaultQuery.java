package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.apache.log4j.Logger;
import org.seventyeight.database.Query;

/**
 * @author cwolfgang
 *         Date: 21-11-12
 *         Time: 13:02
 */
public class OrientDefaultQuery implements Query<Boolean> {

    private static Logger logger = Logger.getLogger( OrientDefaultQuery.class );

    private String query;
    private OGraphDatabase db;

    public OrientDefaultQuery( OGraphDatabase db, String query ) {
        this.query = query;
        this.db = db;
    }

    @Override
    public void execute() {
        System.out.println( "DB: " + db );
        logger.debug( "Executing query: " + query );

        OCommandSQL sql = new OCommandSQL( query );
        db.command( sql ).execute();
    }

    @Override
    public Boolean get() {
        return true;
    }

    @Override
    public String toString() {
        return "OrientQuery[" + query + "]";
    }
}
