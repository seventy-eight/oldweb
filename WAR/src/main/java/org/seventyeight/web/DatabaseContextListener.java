package org.seventyeight.web;

import org.apache.log4j.Logger;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBManager;
import org.seventyeight.web.model.toplevelactionhandlers.*;
import org.seventyeight.web.services.InformationService;
import org.seventyeight.web.toplevelaction.resources.ResourcesAction;

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
                List<File> plugins = gd.extractPlugins( gd.getPath() );

                /* Paths added first is served first */
                //gd.getTemplateManager().addStaticPath( new File( "C:/projects/graph-dragon/war/src/main/webapp/static" ) );
                //gd.getTemplateManager().addStaticPath( new File( gd.getPath(), "static" ) );
                gd.getTemplateManager().addStaticPath( new File( "C:/Users/Christian/projects/seventy-eight/WAR/src/main/webapp/static" ) );


                //paths.add( new File( "/home/wolfgang/projects/graph-dragon/system/target/classes/templates" ) );
                //paths.add( new File( "C:/projects/graph-dragon/system/target/classes/templates" ) );



                /*
                for( File plugin : plugins ) {
                    logger.debug( "Adding " + plugin );
                    paths.add( new File( plugin, "themes" ) );
                }
                */

                paths.add( new File( "C:/Users/Christian/projects/seventy-eight/system/src/main/resources/templates" ) );

                /* LIB */
                paths.add( new File( "C:/Users/Christian/projects/seventy-eight/system/src/main/resources/lib" ) );
                SeventyEight.getInstance().getTemplateManager().addTemplateLibrary( "form.vm" );

                //paths.add( new File( "C:/projects/graph-dragon/system/target/classes/templates" ) );

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
            //GraphDragon.getInstance().addActionHandler( "system", new SystemHandler() );
            //SeventyEight.getInstance().addTopLevelAction( "resource", new ResourceHandler() );
            SeventyEight.getInstance().addTopLevelAction( "resources", new ResourcesAction() );
            SeventyEight.getInstance().addTopLevelAction( "static", new StaticFileHandler() );
            SeventyEight.getInstance().addTopLevelAction( "upload", new UploadHandler() );

            //SeventyEight.getInstance().addTopLevelAction( "debate", new DebateHandler() );

            SeventyEight.getInstance().addTopLevelAction( "db", new DatabaseBrowser() );

            SeventyEight.getInstance().addTopLevelAction( "theme", new ThemeFileHandler() );

            /* Post actions */
            SeventyEight.getInstance().setThemesPath( new File( "C:/Users/Christian/projects/seventy-eight/system/src/main/resources/themes" ) );
        }
}
