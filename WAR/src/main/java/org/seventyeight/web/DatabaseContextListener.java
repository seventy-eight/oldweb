package org.seventyeight.web;

import org.apache.log4j.Logger;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cwolfgang
 *         Date: 02-12-12
 *         Time: 15:39
 */
@WebListener
public class DatabaseContextListener implements ServletContextListener {
        private static Logger logger = Logger.getLogger( DatabaseContextListener.class );

        public void contextDestroyed( ServletContextEvent arg0 ) {
            synchronized( DatabaseContextListener.class ) {
                System.out.println( "Shutting down" );
            }
        }

        public void contextInitialized( ServletContextEvent sce ) {

            String spath = sce.getServletContext().getRealPath( "" );
            logger.info( "Path: " + spath );

            //logger.setLevel( Level.WARN );
            //LogManager.shutdown();

            /* Debugging system */
            List<File> paths = new ArrayList<File>();

            File path = new File( spath );
            File odbPath = new File( path, "odb" );
            OrientDBManager dbm = new OrientDBManager( "local", odbPath.getAbsolutePath() );
            SeventyEight gd = new SeventyEight( path, dbm.getDatabase() );
            try {
                List<File> plugins = gd.extractPlugins( gd.getPluginsPath() );

                /* Paths added first is served first */
                gd.getTemplateManager().addStaticPath( new File( "C:/projects/graph-dragon/war/src/main/webapp/static" ) );
                gd.getTemplateManager().addStaticPath( new File( gd.getPath(), "static" ) );


                paths.add( new File( "/home/wolfgang/projects/graph-dragon/system/target/classes/themes" ) );
                paths.add( new File( "C:/projects/graph-dragon/system/target/classes/themes" ) );

                for( File plugin : plugins ) {
                    logger.debug( "Adding " + plugin );
                    paths.add( new File( plugin, "themes" ) );
                }

                //paths.add( new File( "C:/projects/graph-dragon/system/target/classes/themes" ) );

                logger.info( "Loading plugins" );
                gd.getClassLoader().addUrls( new URL[] { new File( spath, "WEB-INF/lib/seventy-eight.jar" ).toURI().toURL() } );
                gd.getPlugins( plugins );

                logger.info( "Loading templates" );
                gd.getTemplateManager().getTemplates( paths );
                logger.debug( gd.getTemplateManager().toString() );
                gd.getTemplateManager().initialize();
            } catch( IOException e ) {
                e.printStackTrace();
            }

            /* Adding action handlers */
            GraphDragon.getInstance().addActionHandler( "system", new SystemHandler() );
            GraphDragon.getInstance().addActionHandler( "login", new LoginHandler() );
            GraphDragon.getInstance().addActionHandler( "resource", new ResourceHandler() );
            GraphDragon.getInstance().addActionHandler( "resources", new ResourcesHandler() );
            GraphDragon.getInstance().addActionHandler( "widget", new WidgetActionHandler() );
}