package org.seventyeight.utils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtilities {

	private static Logger logger = Logger.getLogger( FileUtilities.class );
	
	public static void extractArchive( File archive, File outputDir ) {
		extractArchive( archive, outputDir, null );
	}
	
	public static void extractArchive( File archive, File outputDir, String subdir ) {
		try {
			ZipFile zipfile = new ZipFile( archive );
			for( Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements(); ) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				if( subdir == null || ( subdir != null && entry.toString().startsWith( subdir ) ) ) {
					unzipEntry( zipfile, entry, outputDir );
				}
			}
		} catch( Exception e ) {
			logger.debug( e.getMessage() );
		}
	}
	
	public static void extractFile( File archive, File outputDir, String filename ) {
		try {
			ZipFile zipfile = new ZipFile( archive );
			for( Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements(); ) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				if( entry.toString().equals( filename ) ) {
					unzipEntry( zipfile, entry, outputDir );
					return;
				}
			}
		} catch( Exception e ) {
			logger.debug( e.getMessage() );
		}
	}

	private static void unzipEntry( ZipFile zipfile, ZipEntry entry, File outputDir ) throws IOException {

		//logger.debug( "Entry: " + entry.getName() );
		if( entry.isDirectory() ) {
			new File( outputDir, entry.getName() ).mkdirs();
			return;
		}

		File outputFile = new File( outputDir, entry.getName() );
		if( !outputFile.getParentFile().exists() ) {
			outputFile.getParentFile().mkdirs();
		}

		BufferedInputStream inputStream = new BufferedInputStream( zipfile.getInputStream( entry ) );
		BufferedOutputStream outputStream = new BufferedOutputStream( new FileOutputStream( outputFile ) );

		try {
			IOUtils.copy( inputStream, outputStream );
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}
	
	public static List<String> splitPath( File file, String terminator ) {
		List<String> paths = new ArrayList<String>();
		//String template  = file.getName();
		File parent = file;
		do {
			parent = parent.getParentFile();
			paths.add( parent.getName() );
			//template = parent.getName() + "/" + template;
		} while( parent != null && !parent.getName().equals( terminator ) );
		
		//paths.add( template );
		
		return paths;
	}
	
	public static FilenameFilter getExtension( String ext ) {
		return new OnlyExtension( ext );
	}
	
	public static FileFilter getDirectories() {
		return new DirectoriesOnly();
	}
	
	public static class OnlyExtension implements FilenameFilter {
		String ext;

		public OnlyExtension( String ext ) {
			this.ext = "." + ext;
		}

		public boolean accept( File dir, String name ) {
			System.out.println( dir + " - " + name + "\"" + ext + "\"" );
			return name.endsWith( ext );
		}
	}
	
	public static class DirectoriesOnly implements FileFilter {

		public boolean accept( File arg0 ) {
			return arg0.isDirectory();
		}
		
	}

    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

    public static void writeToFile( InputStream is, File file ) throws IOException {
        logger.debug( "Writing " + is + " to " + file );
        try {
            InputStream input = new BufferedInputStream( is, DEFAULT_BUFFER_SIZE );
            OutputStream os = new BufferedOutputStream( new FileOutputStream( file ), DEFAULT_BUFFER_SIZE );
            logger.debug( "----INPUT: " + input );
            logger.debug( "----INPUT: " + input.read() );
            int i = 0;
            try {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                for( int length = 0; ( ( length = input.read( buffer ) ) > 0 ); ) {
                    System.out.println( "-------LENGTH: " + length );
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
}
