package org.seventyeight.web.model.resources;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import org.seventyeight.database.*;
import org.seventyeight.structure.Tuple;
import org.seventyeight.utils.Date;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;
import org.seventyeight.web.servlet.Request;
import org.seventyeight.web.servlet.Response;
import org.seventyeight.web.util.FileUploadListener;
import org.seventyeight.web.util.ServletUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class FileResource extends AbstractResource {

	private static Logger logger = Logger.getLogger( FileResource.class );
    public static final String INDEX_UPLOAD_IDENTITIES = "upload-identities";

    private static SimpleDateFormat formatYear = new SimpleDateFormat( "yyyy" );
    private static SimpleDateFormat formatMonth = new SimpleDateFormat( "MM" );


    public enum FileResourceEdgeType implements EdgeType {
        file
    }
	
	public FileResource( Node node ) {
		super( node );
	}

    @Override
    public Save getSaver( CoreRequest request, JsonObject jsonData ) {
		return new FileSaveImpl( this, request, jsonData );
	}

	public class FileSaveImpl extends ResourceSaveImpl {

		public FileSaveImpl( AbstractResource resource, CoreRequest request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			logger.debug( "Saving file" );


		}
	}

    public void doUpload( Request request, Response response, JsonObject json ) throws IOException {
        AsyncContext aCtx = request.startAsync( request, response );
        Executor uploadExecutor = Executors.newCachedThreadPool();

        logger.debug( "SERVLET THREAD: " + Thread.currentThread().getId() + " - " + Thread.currentThread().getName() );
        uploadExecutor.execute( new ServletUtils.FileUploader( aCtx, request.getUser().getIdentifier().toString(), getIdentifier() ) );

        response.getWriter().println( "done" );
    }

    public void doProgress( Request request, Response response ) throws IOException {
        HttpSession session = request.getSession( true );

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (session == null) {
            out.println("Sorry, session is null"); // just to be safe
            return;
        }

        FileUploadListener listener = (FileUploadListener) session.getAttribute( "listener" );
        if (listener == null) {
            out.println("Progress listener is null");
            return;
        }

        out.println(listener.getPercent());
    }

    public void doUploadForm( Request request, Response response ) throws TemplateDoesNotExistException, IOException {
        response.getWriter().print( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( this, "uploadForm.vm" ) );

    }

    /**
     * @param filename
     * @param pathPrefix
     * @return First is a {@link File} relative to the context path, and the second is an absolute file.
     */
    public static Tuple<File, File> generateFile( String filename, String pathPrefix ) {
        Date now = new Date();
        int mid = filename.lastIndexOf( "." );
        String fname = filename;
        String ext = null;
        if( mid > -1 ) {
            ext = filename.substring( mid + 1, filename.length() );
            fname = filename.substring( 0, mid );
        }

        String strpath = "upload/" + pathPrefix + "/" + formatYear.format( now ) + "/" + formatMonth.format( now ) + "/" + ext;

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
        logger.debug( "FILE: " + relativePath );

        return new Tuple<File, File>( relativePath, file );

    }

	public void doGet( ParameterRequest request, PrintWriter writer ) throws IOException {
		File file = getFile();
		
		FileReader reader = new FileReader( file );
		//reader.re
		//fwriter.
	}

    public void addUploadIdentityToIndex() {
        String uid = getUploadIdentity();
        logger.debug( "Adding " + uid + " to upload identity index" );
        getDB().putToIndex( INDEX_UPLOAD_IDENTITIES, node, uid );
    }

    /**
     * Set the file relation for this {@link FileResource}. Any existing file relations are removed.
     * @param item
     */
	public void setFileRelation( DatabaseItem item ) {
		removeFileRelations();
		//this.node.createRelationshipTo( node, SessionEdge.FILE );
        //SeventyEight.getInstance().createEdge( db, this, item, FileResourceEdgeType.file );
        node.createEdge( item.getNode(), FileResourceEdgeType.file );
	}
	
	public void removeFileRelations() {
		logger.debug( "Removing all file relationships for " + this );
        //SeventyEight.getInstance().removeOutEdges( db, this, FileResourceEdgeType.file );
        node.removeEdges( FileResourceEdgeType.file, Direction.OUTBOUND );
	}

    /**
     * Get local {@link File} for this {@link FileResource}
     * @return
     */
    /*
	public File getLocalFile() {
        List<Edge> edges = node.getEdgesTo( FileResourceEdgeType.file, Direction.OUTBOUND );
        if( edges.size() == 1 ) {
            return new File( (String) edges.get( 0 ).getTargetNode().get( "file" ) );
        } else if( edges.size() > 1 ) {
            throw new IllegalStateException( "Too many file items found for " + this );
        } else {
            throw new IllegalStateException( "File item not found for " + this );
        }

	}
	*/

    public String getRelativeFile() {
        return (String) getProperty( "file" );
    }
	
	public File getFile() {
		String rfilename = getRelativeFile();
        if( rfilename != null && !rfilename.isEmpty() ) {
            int l = SeventyEight.getInstance().getPath().toString().length();
            int l2 = rfilename.length();

            logger.debug( "PATH: " + l + ", " + l2 );

            //return new File( rfilename.substring( l, l2 ) );
            return new File( SeventyEight.getInstance().getPath(), rfilename );
        } else {
            return null;
        }
	}

    public void doFile( Request request, Response response ) throws ServletException, IOException {

        File file = getFile();

        if( file != null ) {

            BufferedInputStream buffer = null;
            ServletOutputStream stream = null;

            try {
                stream = response.getOutputStream();

                //set response headers
                response.setContentType( new MimetypesFileTypeMap().getContentType( file.getName() ) );
                response.addHeader( "Content-Disposition", "attachment; filename=" + file.getName() );

                response.setContentLength((int) file.getTotalSpace() );

                int readBytes = 0;

                //read from the file; write to the ServletOutputStream
                buffer = new BufferedInputStream( new FileInputStream( file ) );
                while( ( readBytes = buffer.read() ) != -1 ) {
                    stream.write(readBytes);
                }
            } catch (IOException ioe) {
                throw new ServletException( ioe.getMessage() );
            } finally {
                if( buffer != null ) {
                    buffer.close();
                }
                if( stream != null ) {
                    stream.close();
                }
            }
        } else {
            response.sendError( 404, "Could not find file" );
        }
    }
	
	public String getExtension() {
		return (String) getProperty( "ext" );
	}

    public Long getFileSize() {
        return (Long) getProperty( "fileSize" );
    }

    public String getUploadIdentity() {
        return (String) getProperty( "uploadIdentity" );
    }
	
	public static String getExtension( File file ) {
		String filename = file.getName();
		int mid = filename.lastIndexOf( "." );
		
		String ext = null;
		if( mid > -1 ) {
			ext = filename.substring( mid + 1, filename.length() );
		}
		
		return ext;
	}

	public static String getEncodingType() {
		return "multipart/form-data";
	}
	
	public String getType() {
		return "file";
	}

	public static class FileDescriptor extends ResourceDescriptor<FileResource> {

        @Override
		public String getDisplayName() {
			return "File";
		}
		
		@Override
		public String getType() {
			return "file";
		}

        @Override
        public String getEnctype() {
            return "multipart/form-data";
        }

        @Override
        public void configureIndex( Database db ) {
            logger.debug( "Configuring " + INDEX_UPLOAD_IDENTITIES );
            db.createIndex( INDEX_UPLOAD_IDENTITIES, IndexType.UNIQUE, IndexValueType.STRING );
        }
    }

	public String getPortrait() {
		return null;
	}

    @Override
    public String toString() {
        return "FileResource[" + getRelativeFile() + "]";
    }
}
