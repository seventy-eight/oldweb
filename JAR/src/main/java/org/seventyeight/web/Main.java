package org.seventyeight.web;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.eclipse.jetty.annotations.AnnotationConfiguration;

public class Main {

    private static int port = 8080;
    private static Logger logger = Logger.getLogger( Main.class );

    public static void main( String[] args ) throws Exception {

        System.out.println( "Seventy Eight 0.1.1" );

        boolean clean = false;

        if( args.length > 0 ) {
            for( int i = 0 ; i < args.length ; ++i ) {
                //System.out.println( "[" + i + "] " + args[i] );
                if( args[i].equals( "--clean" ) ) {
                    clean = true;
                } else if( args[i].equals( "--httpPort" ) ) {
                    i++;
                    port = Integer.parseInt( args[i] );
                }
            }
        }

        System.out.println( "Cleaning : " + clean );
        System.out.println( "Http port: " + port );

        File path = getHome();
        logger.debug( "Path: " + path.getAbsolutePath() );

        if( clean ) {
            System.out.println( "Cleaning " + path );
            FileUtils.deleteDirectory( path );
        }

        path.mkdirs();

        File warfile = new File( path, "seventyeight.war" );

        extractWar( path );
        FileUtilities.extractArchive( warfile, path );
        Server server = new Server();

        SelectChannelConnector connector0 = new SelectChannelConnector();
        connector0.setPort( port );
        connector0.setMaxIdleTime( 30000 );
        connector0.setRequestHeaderSize( 8192 );

        server.setConnectors( new Connector[] { connector0 } );

        WebAppContext context = new WebAppContext();
        context.setDescriptor( path + "/WEB-INF/web.xml" );
        context.setResourceBase( path.toString() );

        context.setContextPath( "/" );
        context.setParentLoaderPriority( true );

        context.setConfigurations( new Configuration[] {
                new AnnotationConfiguration(),
                new WebInfConfiguration(),
                new WebXmlConfiguration(),
                new MetaInfConfiguration(),
                new FragmentConfiguration(),
                new EnvConfiguration(),
                new PlusConfiguration(),
                new JettyWebXmlConfiguration()
        } );

        server.setHandler( context );

        server.start();
        server.join();
    }

    private static void extractWar( File path ) {
        File jar = new File( Main.class.getProtectionDomain().getCodeSource().getLocation().getPath() );
        logger.debug( "Jar location: " + jar );

        FileUtilities.extractFile( jar, path, "seventyeight.war" );
    }

    private static File getHome() {
        File def = new File( System.getProperty( "user.home" ), ".seventyeight" );
        try {
            String path = System.getenv( "seventy_eight" );
            if( path != null && path.length() > 0 ) {
                return new File( path );
            } else {
                return def;
            }
        } catch( Exception e ) {
            return def;
        }
    }

}
