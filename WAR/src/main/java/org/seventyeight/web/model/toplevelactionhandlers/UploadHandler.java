package org.seventyeight.web.model.toplevelactionhandlers;

import com.google.gson.JsonObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.structure.Tuple;
import org.seventyeight.utils.Date;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;
import org.seventyeight.web.model.resources.FileResource;
import org.seventyeight.web.servlet.Request;
import org.seventyeight.web.servlet.Response;
import org.seventyeight.web.util.ResourceHelper;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    private ResourceHelper helper = new ResourceHelper();
    private ResourceDescriptor descriptor = (ResourceDescriptor) SeventyEight.getInstance().getDescriptor( FileResource.class );

    @Override
    public String getDisplayName() {
        return "upload";
    }

    /*

    (0) / (1) uploadAndCreate handler / (2) type

    Types:
    GET: /info: get information
    GET: /multiple: multiple uploadAndCreate form
    POST: /: uploadAndCreate a single file


     */

    //@Override
    public void execute( Request request, Response response ) throws ActionHandlerException {

        System.out.println( request.getParameterMap() );

        if( request.isRequestPost() ) {
            /* Do the actual uploadAndCreate */
            try {
                uploadAndCreate( request, response );
            } catch( Exception e ) {
                throw new ActionHandlerException( e );
            }
        } else {
            if( request.getRequestParts()[2].equals( "info" ) ) {
                String uid = request.getRequestParts()[3];
                logger.debug( "Upload information: " + uid );
                List<Node> nodes = request.getDB().getFromIndex( FileResource.INDEX_UPLOAD_IDENTITIES, uid );
                if( nodes.size() > 0 ) {
                    logger.debug( "NODE: " + nodes.get( 0 ) );
                    FileResource f = new FileResource( nodes.get( 0 ) );
                    logger.debug( "FR: " + f );
                    long size = f.getFileSize();
                    File file = f.getFile();
                    logger.debug( "FILE: " + file + " - " + file.exists() );

                    double d1 = (double)file.length();
                    double d2 = (double)f.getFileSize();
                    double d = ( Math.round( ( d1 / d2 ) * 10000.0 ) ) / 100.0;
                    logger.debug( "DOUBLE: " + d );
                    try {
                        response.getWriter().print( d );
                    } catch( IOException e ) {
                        logger.warn( e );
                    }
                }
            } else {
                logger.debug( "Multiple uploadAndCreate form" );
                try {
                    request.getContext().put( "javascript", "ajaxupload" );
                    request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( "org/seventyeight/web/multipleupload.vm" ) );
                    response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( request.getTemplate() ) );
                } catch( Exception e ) {
                    throw new ActionHandlerException( e );
                }
            }
        }
    }

    /**
     * The actual single POST upload method, multipart
     * @param request
     * @param response
     * @param json
     */
    public void doFile( Request request, Response response, JsonObject json ) {

    }


    public static class Copier implements Runnable {
        AsyncContext ctx;
        String pathPrefix;

        public Copier( AsyncContext ctx, String pathPrefix ) {
            this.ctx = ctx;
            this.pathPrefix = pathPrefix;
        }

        public void run() {
            HttpServletRequest request = (HttpServletRequest) ctx.getRequest();
            logger.debug( "REQUEST IS " + request );

            List<FileItem> items = null;
            try {
                items = new ServletFileUpload( new DiskFileItemFactory() ).parseRequest( request );
            } catch( FileUploadException e ) {
                e.printStackTrace();
            }
            for( FileItem item : items ) {
                if( !item.isFormField() ) {
                    Tuple<File, File> files = FileResource.generateFile( item.getName(), pathPrefix );
                    //item.write();
                }
            }
        }
    }

    private void upload( Request request, Response response ) throws CouldNotLoadItemException {
        String filename = request.getValue( "filename", null );
        logger.debug( "Filename: " + filename );

        String id = request.getValue( "identifier", null );
        logger.debug( "Identifier: " + id );

        if( id == null ) {
            throw new IllegalStateException( "File resource identifier must be given" );
        }

        AbstractResource resource = helper.getResource( request, id );
        if( resource instanceof FileResource ) {

        }
    }

    private void uploadAndCreate( Request request, Response response ) throws IOException, ServletException, ResourceNotCreatedException {

        String filename = request.getValue( "filename", null );
        if( filename == null ) {
            request.getValue( "ax-file-name", null );
        }

        logger.debug( "Filename: " + filename );

        /* Get the current sessions */
        Cookie c = org.seventyeight.web.util.Util.getCookie( request, "session" );
        logger.debug( "Cookie: " + c );
        //Session session = SeventyEight.getInstance().getSessionManager().getSession( request.getDB(), c.getValue() );

        Date now = new Date();
        int mid = filename.lastIndexOf( "." );
        String fname = filename;
        String ext = null;
        if( mid > -1 ) {
            ext = filename.substring( mid + 1, filename.length() );
            fname = filename.substring( 0, mid );
        }

        //String strpath = "uploadAndCreate/" + session.getUser().getIdentifier() + "/" + formatYear.format( now ) + "/" + formatMonth.format( now ) + "/" + ext;
        String strpath = "upload/wolle/" + formatYear.format( now ) + "/" + formatMonth.format( now ) + "/" + ext;

        File path = new File( SeventyEight.getInstance().getPath(), strpath );
        File relativePath = new File( strpath, filename );
        logger.debug( "Trying to create path " + path );
        path.mkdirs();
        File file = new File( path, filename );
        int cnt = 0;
        while( file.exists() ) {
            file = new File( path, fname + "_" + cnt + ( ext != null ? "." + ext : "" ) );
            cnt++;
        }
        logger.debug( "FILE: " + file.getAbsolutePath() );

        //writeToFile( filepart, file );

        FileResource resource = (FileResource) helper.createResource( descriptor, request, response );
        resource.getNode().set( "fileSize", Long.parseLong( request.getParameter( "ax-fileSize" ) ) );
        resource.getNode().set( "uploadIdentity", request.getParameter( "uploadAndCreate-identity" ) );
        resource.getNode().set( "file", relativePath.toString() );
        resource.getNode().set( "ext", FileResource.getExtension( file ) );
        resource.setOwner( request.getUser() );
        resource.getNode().save();

        resource.addUploadIdentityToIndex();

        /* Finally, writeToFile to disk */
        //writeToFile( request.getInputStream(), file );
        AsyncContext aCtx = request.startAsync( request, response );
        Executor uploadExecutor = Executors.newCachedThreadPool();

        logger.debug( "SERVLET THREAD: " + Thread.currentThread().getId() + " - " + Thread.currentThread().getName() );
        uploadExecutor.execute( new Copier( aCtx, "" ) );

        //response.setStatus( 200 );
        response.getWriter().print( resource.getIdentifier() );
    }

    @Override
    public String getUrlName() {
        return "uploadAndCreate";
    }


    private String getValue( Part part, String encoding ) throws IOException {
        BufferedReader reader = new BufferedReader( new InputStreamReader( part.getInputStream(), encoding ) );
        StringBuilder value = new StringBuilder();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        for( int length = 0; ( length = reader.read( buffer ) ) > 0; ) {
            value.append( buffer, 0, length );
        }
        return value.toString();
    }

    private void write( InputStream is, File file ) throws IOException {
        logger.debug( "Writing " + is + " to " + file );
        try {
            InputStream input = new BufferedInputStream( is, DEFAULT_BUFFER_SIZE );
            OutputStream os = new BufferedOutputStream( new FileOutputStream( file ), DEFAULT_BUFFER_SIZE );
            int i = 0;
            try {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                for( int length = 0; ( ( length = input.read( buffer ) ) > 0 ); ) {
                    //System.out.println( "LENGTH: " + length );
                    os.write( buffer, 0, length );

                    /*
                    try {
                        System.out.println( "Waiting.... " + i );
                        Thread.sleep( 250 );
                    } catch( InterruptedException e ) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    */

                    i++;
                }
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }

    private void write2( Part part, File file ) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        logger.debug( "WRITING...." );
        try {
            input = new BufferedInputStream( part.getInputStream(), DEFAULT_BUFFER_SIZE );
            output = new BufferedOutputStream( new FileOutputStream( file ), DEFAULT_BUFFER_SIZE );
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            for( int length = 0; ( ( length = input.read( buffer ) ) > 0 ); ) {
                output.write( buffer, 0, length );
            }
        } catch( Exception e ) {
            logger.warn( "Whoops: " + e.getMessage() );
        } finally {
            if( output != null ) try {
                output.close();
            } catch( IOException logOrIgnore ) {
            }
            if( input != null ) try {
                input.close();
            } catch( IOException logOrIgnore ) {
            }
        }

        logger.debug( "File uploaded" );
    }


    /*
    private class Copier implements Runnable {
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
                write( request.getInputStream(), file );
            } catch( IOException e ) {
                logger.error( "Unable to copy file", e );
            }
        }
    }
    */

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
                String filename = generateFile( filepart );
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
                String strpath = "uploadAndCreate/" + session.getUser().getIdentifier() + "/" + formatYear.format( now ) + "/" + formatMonth.format( now ) + "/" + ext;

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

                writeToFile( filepart, file );
            } catch( Exception e ) {
                logger.fatal( "Unable to uploadAndCreate: " + e.getMessage() );
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
