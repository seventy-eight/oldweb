package org.seventyeight.web.model.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import org.seventyeight.database.*;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;


public class FileResource extends AbstractResource {

	private static Logger logger = Logger.getLogger( FileResource.class );

    public enum FileResourceEdgeType implements EdgeType {
        file
    }
	
	public FileResource( Node node ) {
		super( node );
	}

	public void save( ParameterRequest request, JsonObject jsonData ) throws ResourceDoesNotExistException, ParameterDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		doSave( new FileSaveImpl( this, request, jsonData ) );
	}
	
	public class FileSaveImpl extends ResourceSaveImpl {

		public FileSaveImpl( AbstractResource resource, ParameterRequest request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			logger.debug( "Saving file" );

			Long nodeid = null;
			try {
				nodeid = request.getValue( "nodeid" );
			} catch( Exception e ) {
				logger.debug( "NAN: " + e.getMessage() );
			}

			logger.debug( "Setting file relation to " + nodeid );
			if( nodeid != null ) {
				Item item = SeventyEight.getInstance().getItem( nodeid );
				setFileRelation( item );
			}
			
			File file = getLocalFile();
			node.set( "ext", FileResource.getExtension( file ) );
			
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

    /**
     * Set the file relation for this {@link FileResource}. Any existing file relations are removed.
     * @param item
     */
	public void setFileRelation( Item item ) {
		removeFileRelations();
		//this.node.createRelationshipTo( node, Relationships.FILE );
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
	
	public File getFile() {
		File f = getLocalFile();
		int l = SeventyEight.getInstance().getPath().toString().length();
		int l2 = f.getAbsoluteFile().toString().length();
		
		logger.debug( "PATH: " + l + ", " + l2 );
		
		return new File( f.getAbsoluteFile().toString().substring( l, l2 ) );
	}
	
	public String getExtension() {
		return (String) getProperty( "ext" );
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
	}

	public String getPortrait() {
		return null;
	}
}
