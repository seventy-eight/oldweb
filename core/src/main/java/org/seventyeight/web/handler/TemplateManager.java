package org.seventyeight.web.handler;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
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

    private List<String> libsList = new LinkedList<String>();

	private Template _getTemplate( AbstractTheme theme, String template ) throws TemplateDoesNotExistException {
		try {
		    return engine.getTemplate( theme.getName() + "/" + template );
		} catch( ResourceNotFoundException e ) {
			throw new TemplateDoesNotExistException( "The template " + template + " for " + theme.getName() + " does not exist" );
		}
	}

    public Template getTemplate( AbstractTheme theme, String template ) throws TemplateDoesNotExistException {
        logger.debug( "[Finding] " + template + " for " + theme.getName() );
        try {
            return _getTemplate( theme, template );
        } catch( TemplateDoesNotExistException e ) {
            /* If it goes wrong, try the default theme */
            return _getTemplate( SeventyEight.getInstance().getDefaultTheme(), template );
        }
    }

	
	public void addTemplatePath( File path ) {
		this.paths += path.toString() + ", ";
		templatePaths.add( path );
	}
	
	public void addStaticPath( File path ) {
		staticPaths.add( path );
	}

    public void addTemplateLibrary( String lib ) {
        libsList.add( lib );
    }
	
	public File getStaticFile( String filename ) throws IOException {
		for( File path : staticPaths ) {
			File file = new File( path, filename );
			if( file.exists() ) {
				return file;
			}
		}
		
		throw new IOException( "File does not exist, " + filename );
	}
	
	public void resetPaths() {
		this.paths = "";
	}
	
	public void setTemplateDirectories( List<File> directories ) {
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
				                                       + "org.seventyeight.web.velocity.html.ViewDirective,"
				                                       + "org.seventyeight.web.velocity.html.ResourcePreviewDirective,"
				                                       + "org.seventyeight.web.velocity.html.ResourceSelectorDirective,"
                                                       + "org.seventyeight.web.velocity.html.RenderDescriptorDirective,"
                                                       + "org.seventyeight.web.velocity.html.RenderObject,"
				                                       + "org.seventyeight.web.velocity.html.FileInputDirective" );

        String l = getListAsCommaString( libsList );
        logger.debug( "L: " + l );
        velocityProperties.setProperty( "velocimacro.library", l );
        velocityProperties.setProperty( "velocimacro.library.autoreload", "true" );
		
		/* Initialize velocity */
		engine.init( velocityProperties );
	}

    private <T> String getListAsCommaString( List<T> files ) {
        StringBuilder s = new StringBuilder();
        for( int i = 0 ; i < files.size() ; i++ ) {
            T f = files.get( i );

            if( i < ( files.size() - 1 ) ) {
                s.append( f );
                s.append( "," );
            } else {
                s.append( f );
            }
        }

        return s.toString();
    }
	
	public VelocityEngine getEngine() {
		return engine;
	}
	
	public Renderer getRenderer( AbstractTheme theme ) {
		return new Renderer( theme );
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

        public Renderer( AbstractTheme theme ) {
            this.theme = theme;
        }

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


		public String render( String template ) throws TemplateDoesNotExistException {
            /* Resolve template */
            Template t = null;
            try {
                t = getTemplate( theme, template );
            } catch( TemplateDoesNotExistException e ) {
                /* If it goes wrong, try the default theme */
                t = getTemplate( SeventyEight.getInstance().getDefaultTheme(), template );
            } catch( Exception e ) {
                logger.error( e );
            }

            return render( t );
        }

        public String render( Template template ) {
            StringWriter writer = new StringWriter();

			logger.debug( "[Rendering] " + template.getName() );

            context.put( "core", SeventyEight.getInstance() );
            context.put( "theme", theme );
            //context.put( "url" );

			/* I18N */
			context.put( "locale", locale );
			
			template.merge( context, writer );
			
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
            return renderObject( object, method, true );
        }

        public String renderObject( Object object, String method, boolean trySuper ) throws TemplateDoesNotExistException {
            Template template = getTemplate( theme, object, method, trySuper );
            context.put( "item", object );
            return render( template );
        }


        public String renderClass( Class clazz, String method ) throws TemplateDoesNotExistException {
            return renderClass( clazz, method, true );
        }

        public String renderClass( Class clazz, String method, boolean trySuper ) throws TemplateDoesNotExistException {
            Template template = getTemplateFile( theme, clazz, method, trySuper );
            return render( template );
        }

	}

    /**
     * Given a class, get the corresponding list of templates
     * @param object
     * @param method
     * @param trySuper If true, try objects super classes
     * @return
     */
	public Template getTemplate( AbstractTheme theme, Object object, String method, boolean trySuper ) throws TemplateDoesNotExistException {
		/* Resolve template */
		Class<?> clazz = object.getClass();
		while( clazz != Object.class && clazz != null ) {
            try {
                return getTemplate( theme, getUrlFromClass( clazz.getCanonicalName(), method ) );
            } catch( TemplateDoesNotExistException e ) {
                if( trySuper ) {
                    clazz = clazz.getSuperclass();
                } else {
                    break;
                }
            }
		}
		
		throw new TemplateDoesNotExistException( method + " for " + object.getClass().getName() + " not found" );
	}



    /**
     * Given a class, get the corresponding list of templates
     * @param clazz
     * @param method
     * @param trySuper If true, try objects super classes
     * @return
     */
	public Template getTemplateFile( AbstractTheme theme, Class<?> clazz, String method, boolean trySuper ) throws TemplateDoesNotExistException {
		/* Resolve template */
		int cnt = 0;
		while( clazz != Object.class && clazz != null ) {
            try {
                return getTemplate( theme, getUrlFromClass( clazz.getCanonicalName(), method ) );
            } catch( TemplateDoesNotExistException e ) {
                if( trySuper ) {
                    clazz = clazz.getSuperclass();
                } else {
                    break;
                }
            }
		}

        throw new TemplateDoesNotExistException( method + " for " + clazz.getName() + " not found" );
	}

    public static String getUrlFromClass( Class<?> clazz ) {
        return getUrlFromClass( clazz.getName() );
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


    public static String getUrlFromClass( String clazz ) {
        return clazz.replace( '.', '/' ).replace( '$', '/' );
    }

	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append( "Paths: " + this.paths + "\n" );
		
		return sb.toString();
	}
}
