package org.seventyeight.web.handler;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.seventyeight.database.Database;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.DictionaryDoesNotExistException;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.model.AbstractTheme;
import org.seventyeight.web.model.Locale;
import org.seventyeight.web.model.Request;


public class TemplateManager {

	private static Logger logger = Logger.getLogger( TemplateManager.class );
	
	private VelocityEngine engine = new VelocityEngine();
	private Properties velocityProperties = new Properties();
	
	private String paths = "";
	
	private List<File> templatePaths = new ArrayList<File>();
	private List<File> staticPaths = new ArrayList<File>();

	public Template getTemplate( AbstractTheme theme, String template ) throws TemplateDoesNotExistException {
		
		try {
		    Template t = engine.getTemplate( theme.getName() + "/" + template );
		
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
			logger.debug( "[Template directory] " + dir );
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
				
		velocityProperties.setProperty( "file.resource.loader.modificationCheckInterval", "2" );
        velocityProperties.setProperty( "eventhandler.include.class", "org.apache.velocity.app.event.implement.IncludeRelativePath" );
		
		/* Custom directives */
		velocityProperties.setProperty( "userdirective", "org.seventyeight.web.velocity.html.TextInputDirective,"
				                                       + "org.seventyeight.web.velocity.html.AdvancedFileInputDirective,"
				                                       + "org.seventyeight.web.velocity.html.WidgetDirective,"
				                                       + "org.seventyeight.web.velocity.html.GroupSelectInputDirective,"
				                                       + "org.seventyeight.web.velocity.html.ThemeSelectInputDirective,"
				                                       + "org.seventyeight.web.velocity.html.I18NDirective,"
				                                       + "org.seventyeight.web.velocity.html.ResourceViewDirective,"
				                                       + "org.seventyeight.web.velocity.html.ResourcePreviewDirective,"
				                                       + "org.seventyeight.web.velocity.html.ResourceSelectorDirective,"
                                                       + "org.seventyeight.web.velocity.html.RenderDescriptorDirective,"
				                                       + "org.seventyeight.web.velocity.html.FileInputDirective" );
		
		/* Initialize velocity */
		engine.init( velocityProperties );
	}
	
	public VelocityEngine getEngine() {
		return engine;
	}
	
	public Renderer getRenderer() {
		return new Renderer();
	}

    public Renderer getRenderer( Request request ) {
        return new Renderer( request );
    }
	
	public class Renderer {
		//private Writer writer = new StringWriter();
		private AbstractTheme theme;
		private Locale locale;
        private VelocityContext context;
        private Database db;

        public Renderer() {}
        public Renderer( Request request ) {
            this.theme = request.getTheme();
            //this.locale = request.getLocale();
            this.context = request.getContext();
            this.db = request.getDB();
        }

        public Renderer setContext( VelocityContext context ) {
            this.context = context;
            return this;
        }

        /*
		public Renderer setWriter( Writer writer ) {
			this.writer = writer;
            return this;
		}
		*/

        public Renderer setTheme( AbstractTheme theme ) {
            this.theme = theme;
            return this;
        }

        public Renderer setLocale( Locale locale ) {
            this.locale = locale;
            return this;
        }

        /*
		public String get() {
			return writer.toString();
		}
		*/

		public String render( String template ) throws TemplateDoesNotExistException {
            StringWriter writer = new StringWriter();

            if( theme == null ) {
                theme  = SeventyEight.getInstance().getDefaultTheme();
            }
			/* Resolve template */
			Template t = null;
			logger.debug( "Rendering " + template + " - " + theme );
			try {
				t = getTemplate( theme, template );
			} catch( TemplateDoesNotExistException e ) {
				/* If it goes wrong, try the default theme */
				t = getTemplate( SeventyEight.getInstance().getDefaultTheme(), template );
			} catch( Exception e ) {
                logger.error( e );
            }

			logger.debug( "Using the template file: " + t.getName() );

            context.put( "core", SeventyEight.getInstance() );

			/* I18N */
			context.put( "locale", locale );
			
			t.merge( context, writer );
			
			return writer.toString();
		}

        /**
         * Render a specific object, given as "item" in the context. Given a concrete template
         * @param object
         * @param template An exact template
         * @return
         * @throws TemplateDoesNotExistException
         */
		public String render( Object object, String template ) throws TemplateDoesNotExistException {
			context.put( "item", object );
			return render( template );
		}

        /**
         * Render a specific object, given as "item" in the context. <br/>
         * This will render each existing view for this class.
         * @param object
         * @param method
         * @return
         * @throws TemplateDoesNotExistException
         */
        public String renderObject( Object object, String method ) throws TemplateDoesNotExistException {
            List<String> list = getTemplateFile( object, method, -1 );

            context.put( "item", object );

            int c = 0;
            for( String t : list ) {
                try {
                    context.put( "subchildcontent", render( t ) );
                } catch( TemplateDoesNotExistException e ) {
                    /* No op, we just bail */
                    break;
                }

                c++;
            }

            if( c == 0 ) {
                throw new TemplateDoesNotExistException( "No \"" + method + "\" template found for " + object.getClass() );
            }

            return context.get( "subchildcontent" ).toString();
        }

        public String renderClass( Class clazz, String method ) throws TemplateDoesNotExistException {
            List<String> list = getTemplateFile( clazz, method, -1 );

            int c = 0;
            for( String t : list ) {
                try {
                    context.put( "subchildcontent", render( t ) );
                } catch( TemplateDoesNotExistException e ) {
                    /* No op, we just bail */
                    break;
                }

                c++;
            }

            if( c == 0 ) {
                throw new TemplateDoesNotExistException( "No \"" + method + "\" template found for " + clazz );
            }

            return context.get( "subchildcontent" ).toString();
        }

        public String renderClassNoRecursive( Class clazz, String method ) throws TemplateDoesNotExistException {
            String template = getUrlFromClass( clazz.getCanonicalName(), method );

            context.put( "subchildcontent", render( template ) );

            return context.get( "subchildcontent" ).toString();
        }

        /**
         * Render a specific object, given as "item" in the context
         * @param object
         * @param method
         * @return
         * @throws TemplateDoesNotExistException
         */
		public String renderObjectNoRecursive( Object object, String method ) throws TemplateDoesNotExistException {
			String template = getUrlFromClass( object.getClass().getCanonicalName(), method );
			context.put( "item", object );
			return render( template );
		}

        /**
         * Render a specific object given the class, given as "item" in the context
         * @param clazz
         * @param object
         * @param method
         * @return
         * @throws TemplateDoesNotExistException
         */
        public String renderObject( Class<?> clazz, Object object, String method ) throws TemplateDoesNotExistException {
            String template = getUrlFromClass( clazz.getCanonicalName(), method );
            context.put( "item", object );
            return render( template );
        }

	}

    /**
     * Given a class, get the corresponding list of templates
     * @param object
     * @param method
     * @param depth
     * @return
     */
	public static List<String> getTemplateFile( Object object, String method, int depth ) {
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

    /**
     * Given a class, get the corresponding list of templates
     * @param clazz
     * @param method
     * @param depth
     * @return
     */
	public static List<String> getTemplateFile( Class<?> clazz, String method, int depth ) {
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
	public static String getUrlFromObject( Object object, String method ) {
		return getUrlFromClass( object.getClass().getCanonicalName(), method );
	}
	
	/**
	 * Given a string representation of a class and the velocity method, get the url of the file.
	 * @param clazz - A string representing a class
	 * @param method - A velocity method, view.vm or configure.vm
	 * @return A relative path to the velocity file
	 */
	public static String getUrlFromClass( String clazz, String method ) {
		return clazz.replace( '.', '/' ).replace( '$', '/' ) + "/" + method;
	}


	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append( "Paths: " + this.paths + "\n" );
		
		return sb.toString();
	}
}
