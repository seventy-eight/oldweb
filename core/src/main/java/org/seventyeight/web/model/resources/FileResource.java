package org.seventyeight.web.model.resources;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import org.seventyeight.database.*;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;


public class FileResource extends AbstractResource {

	private static Logger logger = Logger.getLogger( FileResource.class );
    public static final String INDEX_UPLOAD_IDENTITIES = "upload-identities";


    public enum FileResourceEdgeType implements EdgeType {
        file
    }
	
	public FileResource( Node node ) {
		super( node );
	}

	public void doSave( ParameterRequest request, JsonObject jsonData ) throws ResourceDoesNotExistException, ParameterDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		save( new FileSaveImpl( this, request, jsonData ) );
	}
	
	public class FileSaveImpl extends ResourceSaveImpl {

		public FileSaveImpl( AbstractResource resource, ParameterRequest request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			logger.debug( "Saving file" );

            String title = request.getValue( "ax-file-name", null );
            if( title != null ) {
                logger.debug( "Short-cut titling to " + title );
                node.set( "title", title );
            }

            /*
			Long nodeid = null;
			try {
				nodeid = request.getValue( "nodeid" );
			} catch( Exception e ) {
				logger.debug( "NAN: " + e.getMessage() );
			}

			logger.debug( "Setting file relation to " + nodeid );
			if( nodeid != null ) {
				DatabaseItem item = SeventyEight.getInstance().getDatabaseItem( nodeid );
				setFileRelation( item );
			}

			File file = getFile();
			node.set( "ext", FileResource.getExtension( file ) );
			*/
		}
	}
	
	/*
	public byte[] getBytes() throws FileNotFoundException {
		File file = getFile();
		
		FileReader reader = new FileReader( file );
		reader.re
	}
	*/
	
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
        List<Edge> edges = node.getEdges( FileResourceEdgeType.file, Direction.OUTBOUND );
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
		int l = SeventyEight.getInstance().getPath().toString().length();
		int l2 = rfilename.length();
		
		logger.debug( "PATH: " + l + ", " + l2 );
		
		//return new File( rfilename.substring( l, l2 ) );
        return new File( SeventyEight.getInstance().getPath(), rfilename );
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
		public Class<? extends Extension> getExtensionClass() {
			return null;
		}

		@Override
		public FileResource newInstance( Database db ) throws UnableToInstantiateObjectException {
			return super.newInstance( db );
		}

        @Override
        public List<String> getRequiredJavascripts() {
            return Collections.singletonList( "ajaxupload-min" );
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
