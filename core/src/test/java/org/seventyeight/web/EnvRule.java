package org.seventyeight.web;

import java.io.File;
import java.io.IOException;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.seventyeight.database.Database;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBManager;

public class EnvRule implements TestRule {
	
	protected Description testDescription;
	protected File path;
    protected String type = "local";
    protected File odbPath;
    protected Database db;
	
	protected void before( File path ) {
        odbPath = new File( path, "odb" );

        System.out.println( "PATH: " + path );
        System.out.println( "ODBPATH: " + odbPath );

        new OrientDBManager( type, odbPath.getAbsolutePath() );
        db = OrientDBManager.getInstance().getDatabase();
		new SeventyEight( path, db );
	}

    public Database getDB() {
        return db;
    }

    public Database getAnotherDB() {
        return OrientDBManager.getInstance().getDatabase();
    }
	
	protected void after() throws IOException {
		SeventyEight.getInstance().shutdown();
		//FileUtils.deleteDirectory( path );
	}
	


	@Override
	public Statement apply( final Statement base, final Description description ) {

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
		
		return new Statement() {
						
			@Override
			public void evaluate() throws Throwable {
				testDescription = description;
				Thread t = Thread.currentThread();
				String o = t.getName();
				t.setName( "Executing " + testDescription.getDisplayName() );
				System.out.println( " ===== Setting up Seventy Eight Web Environment =====" );
				
				try {
					before( path );
					System.out.println( " ===== Running test: " + testDescription.getDisplayName() + " =====" );
					base.evaluate();
				} catch( Exception e ) {
					//System.out.println( "Caught exception: " + e.getMessage() );
					e.printStackTrace();
				} finally {
					System.out.println( " ===== Tearing down Seventy Eight Web Environment =====" );
					after();
					testDescription = null;
					t.setName( o );
				}
			}
		};
	}


}
