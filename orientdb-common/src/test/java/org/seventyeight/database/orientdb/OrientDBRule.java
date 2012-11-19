package org.seventyeight.database.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.seventyeight.database.Node;

import java.io.File;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 22:55
 */
public class OrientDBRule implements TestRule {

    private File path;
    private OGraphDatabase db;

    private void before() {
        //this.db = (OGraphDatabase) ODatabaseDocumentPool.global().acquire( "local:" + path.toString(), "admin", "admin" );
        //db = new OGraphDatabase( "local:" + path.toString() ).create().open( "admin", "admin" );
        //db = new OGraphDatabase( "memory:test" ).open( "admin", "admin" );
        db = new OGraphDatabase( "memory:test" ).create();
    }

    private void after() {
        db.close();
    }

    public OGraphDatabase getDB() {
        return db;
    }

    @Override
    public Statement apply( final Statement base, final Description description ) {

        /*
        try {
            path = File.createTempFile( "SEVENTYEIGHT", "web" );

            if( !path.delete() ) {
                System.out.println( path + " could not be deleted" );
            }

            if( !path.mkdir() ) {
                System.out.println( "DAMN!" );
            }
            System.out.println( "Path: " + path );
        } catch( IOException e ) {
            System.out.println( "Unable to create temporary path" );
        }
        */

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                System.out.println( " ===== Setting up OrientDB Environment =====" );

                try {
                    before();
                    base.evaluate();
                } catch( Exception e ) {
                    e.printStackTrace();
                } finally {
                    System.out.println( " ===== Tearing down OrientDB Environment =====" );
                    after();
                }
            }
        };
    }


}
