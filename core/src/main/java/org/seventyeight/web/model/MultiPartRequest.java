package org.seventyeight.web.model;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.seventyeight.web.servlet.Request;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cwolfgang
 *         Date: 30-01-13
 *         Time: 08:47
 */
public class MultiPartRequest extends Request {

    private static Logger logger = Logger.getLogger( MultiPartRequest.class );

    private Map<String, Object> valueMap = new HashMap<String, Object>();

    public MultiPartRequest( HttpServletRequest request ) {
        super( request );

        List<FileItem> items = null;
        try {
            items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
        } catch( FileUploadException e ) {
            e.printStackTrace();
        }
        for (FileItem item : items) {
            logger.debug( "ITEM: " + item );
            logger.debug( "ITEM: " + item.getFieldName() );
            logger.debug( "ITEM: " + item.getName() );


            String key = item.getFieldName();
            Object[] values = (Object[]) valueMap.get( key );

            if( item.isFormField() ) {
                logger.debug( "ITEM: " + item.getString() );
                if( values == null ) {
                    // Not in parameter map yet, so add as new value.
                    valueMap.put( key, new String[] { item.getString() } );
                } else {
                    /* Multiple field values, so add new value to existing array */
                    int length = values.length;
                    String[] newValues = new String[length + 1];
                    System.arraycopy( values, 0, newValues, 0, length );
                    newValues[length] = item.getString();
                    valueMap.put( key, newValues );
                }
            } else {
                /*
                Executor uploadExecutor = Executors.newCachedThreadPool();

                logger.debug( "SERVLET THREAD: " + Thread.currentThread().getId() + " - " + Thread.currentThread().getName() );
                Tuple<File, File> files = FileResource.generateFile( item.getName(), SeventyEight.getInstance().getAnonymousUser() );
                uploadExecutor.execute( new ServletUtils.FileUploader( item, files.getSecond() ) );

                valueMap.put( key, files.getFirst() );
                */
            }
        }
    }



    @Override
    public <T> T getValue( String key ) {
        return (T) valueMap.get( key );
    }

    @Override
    public <T> T getValue( String key, T defaultValue ) {
        if( valueMap.containsKey( key ) ) {
            return (T) valueMap.get( key );
        } else {
            return defaultValue;
        }
    }
}
