package org.seventyeight.web.model.toplevelactionhandlers;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.database.Node;
import org.seventyeight.utils.Date;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.authentication.Session;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.TopLevelAction;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

/**
 * @author cwolfgang
 *         Date: 08-12-12
 *         Time: 23:01
 */
public class UploadHandler implements TopLevelAction {

    private static Logger logger = Logger.getLogger( UploadHandler.class );

    private static final String CONTENT_DISPOSITION = "content-disposition";
    private static final String CONTENT_DISPOSITION_FILENAME = "filename";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

    private SimpleDateFormat formatYear = new SimpleDateFormat( "yyyy" );
    private SimpleDateFormat formatMonth = new SimpleDateFormat( "MM" );

    @Override
    public void prepare( Request request ) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return "upload";
    }


/*
    private class Upload implements Runnable {
        AsyncContext ctx;
        Database db;

        public Upload( Database db, AsyncContext ctx ) {
            this.ctx = ctx;
            this.db = db;
        }

        public void run() {
            logger.debug( "UPLOAD THREAD: " + Thread.currentThread().getId() + " - " + Thread.currentThread().getName() );
            HttpServletRequest request = (HttpServletRequest) ctx.getRequest();
            logger.debug( "Context:"  + ctx );
            ServletResponse response = ctx.getResponse();
            logger.debug( "Response:"  + response );
            PrintWriter out = null;
            try {
                Part filepart = request.getPart( "filename" );
                Part appendpart = request.getPart( "append" );
                String filename = getFilename( filepart );
                logger.debug( "Filename: " + filename );

                Cookie c = org.seventyeight.web.util.Util.getCookie( request, "session" );
                logger.debug( "Cookie: " + c );
                Session session = SeventyEight.getInstance().getSessionManager().getSession( db, c.getValue() );
                Date now = new Date();
                int mid = filename.lastIndexOf( "." );
                String fname = filename;
                String ext = null;
                if( mid > -1 ) {
                    ext = filename.substring( mid + 1, filename.length() );
                    fname = filename.substring( 0, mid );
                }
                String strpath = "upload/" + session.getUser().getIdentifier() + "/" + formatYear.format( now ) + "/" + formatMonth.format( now ) + "/" + ext;

                File path = new File( SeventyEight.getInstance().getPath(), strpath );
                logger.debug( "Trying to create path " + path );
                path.mkdirs();
                File file = new File( path, filename );
                int cnt = 0;
                while( file.exists() ) {
                    file = new File( path, fname + "_" + cnt + ( ext != null ? "." + ext : "" ) );
                    cnt++;
                }
                logger.debug( "FILE: " + file.getAbsolutePath() );

                //Transaction tx = GraphDragon.getInstance().getGraphDB().beginTx();

                Node node = null;
                Long id = 0l;
                try {
                    node = SeventyEight.getInstance().createFile( file );
                    logger.debug( "ID=" + node.getId() );
                    node.setProperty( "estimated-size", filepart.getSize() );
                    node.setProperty( "file", file.toString() );
                    tx.success();
                    id = node.getId();
                    logger.debug( "ID=" + id );
                } catch( Exception e ) {
                    logger.debug( "FAILED: " + e.getMessage() );
                    logger.debug( e.getStackTrace() );
                    tx.failure();
                } finally {
                    tx.finish();
                }

                logger.debug( "ID2=" + id );

                doit( request );
                String append = getValue( appendpart );

                response.setContentType( "text/html" );
                logger.debug( "Setting out" );
                out = response.getWriter();
                logger.debug( "out: " + out );
                out.println( "<script language=\"javascript\">top.Utils.startupload(" + id + ", \"" + append + "\");</script>" );

                write( filepart, file );
            } catch( Exception e ) {
                logger.fatal( "Unable to upload: " + e.getMessage() );
                ExceptionUtils.print( e, out, true );
            } finally {
                out.flush();
                out.close();
            }

            //ctx.dispatch();
        }

    }
*/

    private String getFilename( Part part ) {
        for( String cd : part.getHeader( CONTENT_DISPOSITION ).split( ";" ) ) {
            if( cd.trim().startsWith( CONTENT_DISPOSITION_FILENAME ) ) {
                return cd.substring( cd.indexOf( '=' ) + 1 ).trim().replace( "\"", "" );
            }
        }
        return null;
    }
}
