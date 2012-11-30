package org.seventyeight.web.handler;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.DictionaryDoesNotExistException;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.model.AbstractTheme;
import org.seventyeight.web.model.Locale;


public class Renderer {

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
		velocityProperties.setProperty( "userdirective", "org.seventyeight.velocity.org.seventyeight.velocity.html.html.TextInputDirective,"
				                                       + "org.seventyeight.velocity.org.seventyeight.velocity.html.html.AdvancedFileInputDirective,"
				                                       + "org.seventyeight.velocity.org.seventyeight.velocity.html.html.WidgetDirective,"
				                                       + "org.seventyeight.velocity.org.seventyeight.velocity.html.html.GroupSelectInputDirective,"
				                                       + "org.seventyeight.velocity.org.seventyeight.velocity.html.html.ThemeSelectInputDirective,"
				                                       + "org.seventyeight.velocity.org.seventyeight.velocity.html.html.I18NDirective,"
				                                       + "org.seventyeight.velocity.org.seventyeight.velocity.html.html.ResourceViewDirective,"
				                                       + "org.seventyeight.velocity.org.seventyeight.velocity.html.html.ResourcePreviewDirective,"
				                                       + "org.seventyeight.velocity.org.seventyeight.velocity.html.html.ResourceSelectorDirective,"
				                                       + "org.seventyeight.velocity.org.seventyeight.velocity.html.html.FileInputDirective" );
		
		/* Initialize velocity */
		renderer.init( velocityProperties );
	}
	
	public VelocityEngine getRenderer() {
		return renderer;
	}
	
	public Render getRender( Writer writer ) {
		return new Render( writer );
	}
	
	public class Render {
		private Writer writer;
		private AbstractTheme theme;
		private Locale locale;
		
		public Render( Writer writer ) {
			this( writer, SeventyEight.getInstance().getDefaultTheme(), SeventyEight.getInstance().getDefaultLocale() );
		}
		
		public Render( Writer writer, AbstractTheme theme, Locale locale ) {
			this.writer = writer;
			this.theme = theme;
			this.locale = locale;
		}
		
		public String render() {
			return writer.toString();
		}
		

		public Render render( String template, VelocityContext context ) throws TemplateDoesNotExistException {
			/* Resolve template */
			Template t = null;
			logger.debug( "Rendering " + template );
			try {
				t = getTemplate( theme, template );
			} catch( TemplateDoesNotExistException e ) {
				/* If it goes wrong, try the default theme */
				t = getTemplate( SeventyEight.getInstance().getDefaultTheme(), template );
			}

			logger.debug( "Using the template file: " + t.getName() );
			
			/* I18N */
			context.put( "locale", locale );
			
			t.merge( context, writer );
			
			return this;
		}
		
		public Render render( Object object, String template, VelocityContext context ) throws TemplateDoesNotExistException {
			context.put( "item", object );
			return render( template, context );
		}
		
		public Render renderObject( Class<?> clazz, Object object, String method, VelocityContext context ) throws TemplateDoesNotExistException {
			/* Resolve template */
			String template = getUrlFromClass( clazz.getCanonicalName(), method );
			context.put( "item", object );
			try {
				context.put( "i18n", SeventyEight.getInstance().getI18N().getDictionary( clazz ) );
			} catch( DictionaryDoesNotExistException e ) {
				context.put( "i18n", null );
			}
			
			return render( template, context );
		}

	}
		
	
	public List<String> getTemplateFile( Object object, String method, int depth ) {
		/* Resolve template */
		List<String> list = new ArrayList<String>();
		Class<?> clazz = object.getClass();
		int cnt = 0;
		while( clazz != Object.class && clazz != null && cnt != depth ) {
			list.add( getUrlFromClass( clazz.getCanonicalName(), method ) );
			cnt++;
			clazz = clazz.getSuperclass();
		}
		
		return list;
	}
	
	public List<String> getTemplateFile( Class<?> clazz, String method, int depth ) {
		/* Resolve template */
		List<String> list = new ArrayList<String>();
		int cnt = 0;
		while( clazz != Object.class && clazz != null && cnt != depth ) {
			list.add( getUrlFromClass( clazz.getCanonicalName(), method ) );
			cnt++;
			clazz = clazz.getSuperclass();
		}
		
		return list;
	}
	
	/**
	 * Given an object and the velocity method, get the url of the file.
	 * @param object - Some object
	 * @param method - A velocity method, view.vm or configure.vm
	 * @return A relative path to the velocity file
	 */
	public String getUrlFromClass( Object object, String method ) {
		return getUrlFromClass( object.getClass().getCanonicalName(), method );
	}
	
	/**
	 * Given a string representation of a class and the velocity method, get the url of the file.
	 * @param clazz - A string representing a class
	 * @param method - A velocity method, view.vm or configure.vm
	 * @return A relative path to the velocity file
	 */
	public String getUrlFromClass( String clazz, String method ) {
		return clazz.replace( '.', '/' ).replace( '$', '/' ) + "/" + method;
	}


	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append( "Paths: " + this.paths + "\n" );
		
		return sb.toString();
	}
}
