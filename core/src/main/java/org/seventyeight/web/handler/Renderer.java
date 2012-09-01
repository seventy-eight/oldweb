package org.seventyeight.web.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.model.AbstractTheme;


public class Renderer {

	private static final long serialVersionUID = 7459581481528316339L;

	private static Logger logger = Logger.getLogger( Renderer.class );
	
	private VelocityEngine renderer = new VelocityEngine();
	private Properties velocityProperties = new Properties();
	
	private String paths = "";
	
	private List<File> templatePaths = new ArrayList<File>();
	private List<File> staticPaths = new ArrayList<File>();

	public Template getTemplate( AbstractTheme theme, String template ) throws TemplateDoesNotExistException {
		
		try {
		Template t = renderer.getTemplate( theme.getName() + "/" + template );
		
		return t;
		} catch( ResourceNotFoundException e ) {
			throw new TemplateDoesNotExistException( "The template " + template + " for " + theme.getName() + " does not exist" );
		}
	}
	
	public void addTemplatePath( File path ) {
		this.paths += path.toString() + ", ";
		templatePaths.add( path );
	}
	
	public void addStaticPath( File path ) {
		staticPaths.add( path );
	}
	
	public File getStaticFile( String filename ) throws IOException {
		for( File path : staticPaths ) {
			logger.debug( "STATIC PATH: " + path );
			File file = new File( path, filename );
			logger.debug( "STATIC FILE: " + file );
			logger.debug( "STATIC FILE: " + file.getAbsolutePath() );
			if( file.exists() ) {
				logger.debug( "Returning " + file );
				return file;
			}
		}
		
		throw new IOException( "File does not exist, " + filename );
	}
	
	public void resetPaths() {
		this.paths = "";
	}
	
	public void getTemplates( List<File> directories ) {
		for( File dir : directories ) {
			logger.debug( "Adding " + dir );
			this.paths += dir.toString() + ", ";
			templatePaths.add( dir );
		}
	}
	
	public File getThemeFile( String filename ) {
		logger.debug( "Scanning " + templatePaths );
		for( File path : templatePaths ) {
			File file = new File( path, filename );
			if( file.exists() ) {
				logger.debug( "Returning " + file );
				return file;
			}
		}
		
		return null;
	}
	
	public void initialize() {
		
		/* Generate paths */
		this.paths = "";
		for( File f : templatePaths ) {
			this.paths += f.toString() + ", ";
		}
		
		this.paths = paths.substring( 0, ( paths.length() - 2 ) );
		
		velocityProperties.setProperty( "resource.loader", "file" );
		velocityProperties.setProperty( "file.resource.loader.path", this.paths );
				
		//velocityProperties.setProperty( "file.resource.loader.modificationCheckInterval", "2" );
		
		/* Custom directives */
		velocityProperties.setProperty( "userdirective", "org.seventyeight.velocity.html.TextInputDirective,"
				                                       + "org.seventyeight.velocity.html.AdvancedFileInputDirective," 
				                                       + "org.seventyeight.velocity.html.WidgetDirective,"
				                                       + "org.seventyeight.velocity.html.GroupSelectInputDirective,"
				                                       + "org.seventyeight.velocity.html.ThemeSelectInputDirective,"
				                                       + "org.seventyeight.velocity.html.I18NDirective,"
				                                       + "org.seventyeight.velocity.html.ResourceViewDirective,"
				                                       + "org.seventyeight.velocity.html.ResourcePreviewDirective,"
				                                       + "org.seventyeight.velocity.html.ResourceSelectorDirective,"
				                                       + "org.seventyeight.velocity.html.FileInputDirective" );
		
		/* Initialize velocity */
		renderer.init( velocityProperties );
	}
	
	public VelocityEngine getRenderer() {
		return renderer;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append( "Paths: " + this.paths + "\n" );
		
		return sb.toString();
	}
}
