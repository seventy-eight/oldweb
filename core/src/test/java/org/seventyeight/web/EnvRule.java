package org.seventyeight.web;

import java.io.File;
import java.io.IOException;

import javax.management.openmbean.InvalidOpenTypeException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBManager;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.InconsistentParameterException;
import org.seventyeight.web.exceptions.IncorrectTypeException;
import org.seventyeight.web.exceptions.ParameterDoesNotExistException;
import org.seventyeight.web.exceptions.ResourceDoesNotExistException;
import org.seventyeight.web.model.AbstractItem;
import org.seventyeight.web.model.ParameterRequest;

import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class EnvRule implements TestRule {
	
	protected Description testDescription;
	protected File path;
    protected String type = "local";
    protected File odbPath;
	
	protected void before( File path ) {
        odbPath = new File( path, "odb" );

        System.out.println( "PATH: " + path );
        System.out.println( "ODBPATH: " + odbPath );

        new OrientDBManager( type, odbPath.getAbsolutePath() );
		new SeventyEight( path, OrientDBManager.getInstance().getDatabase() );
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
