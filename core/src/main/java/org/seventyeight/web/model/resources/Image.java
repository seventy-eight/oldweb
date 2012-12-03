package org.seventyeight.web.model.resources;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import org.seventyeight.database.Database;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.Extension;
import org.seventyeight.web.model.ParameterRequest;
import org.seventyeight.web.model.ResourceDescriptor;

public class Image extends FileResource {

	private static Logger logger = Logger.getLogger( Image.class );

	public Image( Node node ) {
		super( node );
	}

	public enum ImageType {
		jpeg, png, gif
	}

	public void doSave( ParameterRequest request, JsonObject jsonData ) throws ResourceDoesNotExistException, ParameterDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		save( new ImageSaveImpl( this, request, jsonData ) );
	}

	public class ImageSaveImpl extends FileSaveImpl {

		public ImageSaveImpl( AbstractResource resource, ParameterRequest request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}

		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
			super.save();

			logger.debug( "Getting file" );
			File file = getLocalFile();
			logger.debug( "FILE: " + file );
			
			createImages( file );
		}
		
		public void createImages( File imageFile ) throws ErrorWhileSavingException {
			createThumbnail( imageFile );
		}
		
		public void createThumbnail( File imageFile ) throws ErrorWhileSavingException {
			File thumbPath = new File( imageFile.getParentFile(), "thumb" );
			logger.debug( "THUMBPATH: " + thumbPath );
			thumbPath.mkdirs();
			File thumbFile = new File( thumbPath, imageFile.getName() );
			logger.debug( "Creating thumbnail: " + thumbFile );
			
			/* Make thumb */
			try {
				Image.doit( imageFile, thumbFile, 80, 80, true, 0.75f );
			} catch( Exception e ) {
				logger.debug( "Unable to doSave file: " + e.getMessage() );
				throw new ErrorWhileSavingException( e );
			}
		}
		
		public void createIcon( File imageFile ) throws ErrorWhileSavingException {
			File iconPath = new File( imageFile.getParentFile(), "icon" );
			logger.debug( "ICON: " + iconPath );
			iconPath.mkdirs();
			File thumbFile = new File( iconPath, imageFile.getName() );
			logger.debug( "Creating icon: " + thumbFile );
			
			/* Make thumb */
			try {
				Image.doit( imageFile, thumbFile, 40, 40, true, 0.6f );
			} catch( Exception e ) {
				logger.debug( "Unable to doSave file: " + e.getMessage() );
				throw new ErrorWhileSavingException( e );
			}
		}
	}
	
	public File getFile( String type ) {
		File f = getLocalFile();
		int l = SeventyEight.getInstance().getPath().toString().length();
		int l2 = f.getParentFile().getAbsoluteFile().toString().length();
		
		logger.debug( "PATH: " + l + ", " + l2 );
		
		return new File( f.getAbsoluteFile().toString().substring( l, l2 ), type + "/" + f.getName() );
	}

	public static BufferedImage loadImage( File imageFile ) throws UnableToLoadImageException {
		BufferedImage image = null;
		try {
			image = ImageIO.read( imageFile );
		} catch( Exception e ) {
			throw new UnableToLoadImageException( e.getMessage() );
		}

		return image;
	}

	public static void saveImage( BufferedImage image, ImageType type, File file, float quality ) throws UnableToSaveImageException {
		ImageOutputStream out = null;
		try {
			ImageWriter writer = ImageIO.getImageWritersByFormatName( type.name() ).next();
			
			ImageWriteParam parms = writer.getDefaultWriteParam();
			out = ImageIO.createImageOutputStream( file );
			writer.setOutput( out );
			
			/* Only for jpegs */
			if( type.equals( ImageType.jpeg ) ) {
				parms.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
			}
			
			if( !type.equals( ImageType.png ) ) {
				parms.setCompressionQuality( quality );
			}
			
			writer.write( null, new IIOImage( image, null, null ), parms );
			
		} catch( Exception e ) {
			throw new UnableToSaveImageException( e.getMessage() );
		} finally {
			try {
				out.close();
			} catch( IOException e ) {
				throw new UnableToSaveImageException( e.getMessage() );
			}
		}
	}

	public static BufferedImage resizeImage( BufferedImage image, int width, int height ) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage resized = new BufferedImage( width, height, image.getType() );
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		g.drawImage( image, 0, 0, width, height, 0, 0, w, h, null );
		g.dispose();
		
		return resized;
	}
	
	public static ImageType extensionToType( String extension ) {
		ImageType type = null;
		if( extension.equalsIgnoreCase( "jpg" ) || extension.equalsIgnoreCase( "jpeg" ) ) {
			type = ImageType.jpeg;
		} else if( extension.equalsIgnoreCase( "png" ) ) {
			type = ImageType.png;
		} else if( extension.equalsIgnoreCase( "gif" ) ) {
			type = ImageType.gif;
		} else {
			logger.debug( "Unkown image type: " + extension );
		}
		
		logger.debug( "Image type is " + type );
		return type;
	}
	
	public static void doit( File imageSource, File imageDestination, int width, int height, boolean uniformScale, float quality ) throws UnableToLoadImageException, UnableToSaveImageException {
		BufferedImage image = Image.loadImage( imageSource );
		
		if( image.getWidth() > width && image.getHeight() > height ) {
			if( uniformScale ) {
				float ratio = 1f;
				if( image.getWidth() >= image.getHeight() ) {
					ratio = (float)width / image.getWidth();
				} else {
					ratio = (float)height / image.getHeight();
				}
				image = resizeImage( image, (int)( image.getWidth() * ratio ), (int)( image.getHeight() * ratio ) );
			} else {
				image = resizeImage( image, width, height );
			}
			
		}
		
		saveImage( image, Image.extensionToType( getExtension( imageSource ) ), imageDestination, quality );
	}
	
	
	public static class ImageDescriptor extends ResourceDescriptor<Image> {

		@Override
		public String getDisplayName() {
			return "Image";
		}
		
		@Override
		public String getType() {
			return "image";
		}
		
		@Override
		public Class<? extends Extension> getExtensionClass() {
			return null;
		}

		@Override
		public Image newInstance( Database db ) throws UnableToInstantiateObjectException {
			return super.newInstance( db );
		}
	}

}
