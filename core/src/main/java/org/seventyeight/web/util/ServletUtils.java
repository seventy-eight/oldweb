package org.seventyeight.web.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.database.orientdb.impl.orientdb.OrientDBManager;
import org.seventyeight.structure.Tuple;
import org.seventyeight.utils.FileUtilities;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.CouldNotLoadItemException;
import org.seventyeight.web.model.resources.FileResource;
import org.seventyeight.web.model.util.MultipartMap;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author cwolfgang
 *         Date: 29-01-13
 *         Time: 14:39
 */
public class ServletUtils {

    private ServletUtils() {

    }

    private static Logger logger = Logger.getLogger( ServletUtils.class );

    public static class Copier implements Runnable {
        AsyncContext ctx;
        File file;

        public Copier( AsyncContext ctx, File file ) {
            this.ctx = ctx;
            this.file = file;
        }

        public void run() {
            HttpServletRequest request = (HttpServletRequest) ctx.getRequest();
            logger.debug( "REQUEST IS " + request );
            try {
                FileUtilities.writeToFile( request.getInputStream(), file );
            } catch( IOException e ) {
                logger.error( "Unable to copy file", e );
            }
        }
    }

    public static class FileUploader implements Runnable {
        AsyncContext ctx;
        String pathPrefix;
        long id;

        public FileUploader( AsyncContext ctx, String pathPrefix, long id ) {
            this.ctx = ctx;
            this.pathPrefix = pathPrefix;
            this.id = id;
        }

        public void run() {
            HttpServletRequest request = (HttpServletRequest) ctx.getRequest();
            logger.debug( "REQUEST IS " + request );

            Database db = OrientDBManager.getInstance().getDatabase();
            FileResource f = null;
            try {
                f = (FileResource) SeventyEight.getInstance().getResource( db, id );
            } catch( CouldNotLoadItemException e ) {
                e.printStackTrace();
                return;
            }

            f.getNode().set( "file", null );

            List<FileItem> items = null;
            FileItemFactory factory = new DiskFileItemFactory();
            FileUploadListener listener = new FileUploadListener();
            HttpSession session = request.getSession();
            session.setAttribute( "listener", listener );

            ServletFileUpload upload = new ServletFileUpload( factory );
            upload.setProgressListener( listener );

            try {
                items = upload.parseRequest( request );
            } catch( FileUploadException e ) {
                e.printStackTrace();
            }
            for( FileItem item : items ) {
                if( !item.isFormField() ) {
                    Tuple<File, File> files = FileResource.generateFile( item.getName(), pathPrefix );
                    try {
                        item.write( files.getSecond() );
                    } catch( Exception e ) {
                        e.printStackTrace();
                    }

                    f.getNode().set( "file", files.getFirst().toString() );
                }
            }

            f.getNode().save();
        }
    }



    public static class MultiPartCopier implements Runnable {
        AsyncContext ctx;
        File file;

        public MultiPartCopier( AsyncContext ctx, File file ) {
            this.ctx = ctx;
            this.file = file;
        }

        public void run() {
            HttpServletRequest request = (HttpServletRequest) ctx.getRequest();
            logger.debug( "REQUEST IS " + request );
            try {
                MultipartMap map = new MultipartMap( null );
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
