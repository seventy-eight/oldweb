package org.seventyeight.web.util;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.seventyeight.utils.FileUtilities;
import org.seventyeight.web.model.util.MultipartMap;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

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

        private File file;
        private FileItem item;

        public FileUploader( FileItem item, File file ) {
            this.item = item;
            this.file = file;
        }

        @Override
        public void run() {
            try {
                item.write( file );
            } catch( Exception e ) {
                e.printStackTrace();
            }
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
