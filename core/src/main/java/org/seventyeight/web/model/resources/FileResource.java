package org.seventyeight.model.resources;

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

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.seventyeight.GraphDragon;
import org.seventyeight.exceptions.ErrorWhileSavingException;
import org.seventyeight.exceptions.InconsistentParameterException;
import org.seventyeight.exceptions.IncorrectTypeException;
import org.seventyeight.exceptions.ParameterDoesNotExistException;
import org.seventyeight.exceptions.ResourceDoesNotExistException;
import org.seventyeight.exceptions.TemplateDoesNotExistException;
import org.seventyeight.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.model.AbstractObject;
import org.seventyeight.model.AbstractResource;
import org.seventyeight.model.Extension;
import org.seventyeight.model.Parameters;
import org.seventyeight.model.Portrait;
import org.seventyeight.model.RequestContext;
import org.seventyeight.model.ResourceDescriptor;
import org.seventyeight.model.UserExtension;

import com.google.gson.JsonObject;


public class FileResource extends AbstractResource {

	private static Logger logger = Logger.getLogger( FileResource.class );
	
	public enum Relationships implements RelationshipType {
		FILE
	}
	
	public FileResource( Node node ) {
		super( node );
	}

	public void save( RequestContext request, JsonObject jsonData ) throws ResourceDoesNotExistException, ParameterDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		doSave( new FileSaveImpl( this, request, jsonData ) );
	}
	
	public class FileSaveImpl extends ResourceSaveImpl {

		public FileSaveImpl( AbstractResource resource, RequestContext request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();
			logger.debug( "Saving file" );

			Long nodeid = null;
			try {
				nodeid = request.getIntegerKey( "nodeid" );
			} catch( Exception e ) {
				logger.debug( "NAN: " + e.getMessage() );
			}

			logger.debug( "Setting file relation to " + nodeid );
			if( nodeid != null ) {
				Node node = GraphDragon.getInstance().getNode( nodeid );
				setFileRelation( node );
			}
			
			File file = getLocalFile();
			node.setProperty( "ext", FileResource.getExtension( file ) );
			
		}
	}
	
	/*
	public byte[] getBytes() throws FileNotFoundException {
		File file = getFile();
		
		FileReader reader = new FileReader( file );
		reader.re
	}
	*/
	
	public void doGet( Parameters request, PrintWriter writer ) throws IOException {
		File file = getFile();
		
		FileReader reader = new FileReader( file );
		//reader.re
		//fwriter.
	}
	
	public void setFileRelation( Node node ) {
		removeFileRelations();
		this.node.createRelationshipTo( node, Relationships.FILE );
	}
	
	public void removeFileRelations() {
		logger.debug( "Removing all file relationships for " + this );
		Iterator<Relationship> i = node.getRelationships( Relationships.FILE ).iterator();
		while( i.hasNext() ) {
			i.next().delete();
		}
	}
	
	public File getLocalFile() {
		Node fnode = node.getRelationships( Direction.OUTGOING, Relationships.FILE ).iterator().next().getEndNode();
		return new File( (String) fnode.getProperty( "file" ) );
	}
	
	public File getFile() {
		File f = getLocalFile();
		int l = GraphDragon.getInstance().getPath().toString().length();
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
		public FileResource newInstance() throws UnableToInstantiateObjectException {
			return super.newInstance();
		}

		/*
		@Override
		public FileResource get( Node node ) {
			return new FileResource( node );
		}
		*/
	}

	public String getPortrait() {
		return null;
	}
}
