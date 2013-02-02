package org.seventyeight.web.servlet;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * @author cwolfgang
 *         Date: 03-02-13
 *         Time: 00:03
 */
public class Response extends HttpServletResponseWrapper {

    public Response( HttpServletResponse response ) {
        super( response );
    }


    public void getFile( HttpServletRequest request, HttpServletResponse response, GetFile getfile/*, String contentType*/, boolean content ) throws IOException {
        // Validate the requested file
        // ------------------------------------------------------------

        //File file = getFile( request, response );
        File file = getfile.getFile( request, response );

        if( file == null ) {
            return;
        }

        logger.debug( "FILE IS " + file );

        // Prepare some variables. The ETag is an unique identifier of the file.
        String fileName = file.getName();
        long length = file.length();
        long lastModified = file.lastModified();
        String eTag = fileName + "_" + length + "_" + lastModified;

        // Validate request headers for caching
        // ---------------------------------------------------

        // If-None-Match header should contain "*" or ETag. If so, then return
        // 304.
        String ifNoneMatch = request.getHeader( "If-None-Match" );
        if( ifNoneMatch != null && matches( ifNoneMatch, eTag ) ) {
            response.setHeader( "ETag", eTag ); // Required in 304.
            response.sendError( HttpServletResponse.SC_NOT_MODIFIED );
            return;
        }

        // If-Modified-Since header should be greater than LastModified. If so,
        // then return 304.
        // This header is ignored if any If-None-Match header is specified.
        long ifModifiedSince = request.getDateHeader( "If-Modified-Since" );
        if( ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified ) {
            response.setHeader( "ETag", eTag ); // Required in 304.
            response.sendError( HttpServletResponse.SC_NOT_MODIFIED );
            return;
        }

        // Validate request headers for resume
        // ----------------------------------------------------

        // If-Match header should contain "*" or ETag. If not, then return 412.
        String ifMatch = request.getHeader( "If-Match" );
        if( ifMatch != null && !matches( ifMatch, eTag ) ) {
            response.sendError( HttpServletResponse.SC_PRECONDITION_FAILED );
            return;
        }

        // If-Unmodified-Since header should be greater than LastModified. If
        // not, then return 412.
        long ifUnmodifiedSince = request.getDateHeader( "If-Unmodified-Since" );
        if( ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified ) {
            response.sendError( HttpServletResponse.SC_PRECONDITION_FAILED );
            return;
        }

        // Validate and process range
        // -------------------------------------------------------------

        // Prepare some variables. The full Range represents the complete file.
        Range full = new Range( 0, length - 1, length );
        List<Range> ranges = new ArrayList<Range>();

        // Validate and process Range and If-Range headers.
        String range = request.getHeader( "Range" );
        if( range != null ) {

            // Range header should match format "bytes=n-n,n-n,n-n...". If not,
            // then return 416.
            if( !range.matches( "^bytes=\\d*-\\d*(,\\d*-\\d*)*$" ) ) {
                response.setHeader( "Content-Range", "bytes */" + length ); // Required
                // in
                // 416.
                response.sendError( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                return;
            }

            // If-Range header should either match ETag or be greater then
            // LastModified. If not,
            // then return full file.
            String ifRange = request.getHeader( "If-Range" );
            if( ifRange != null && !ifRange.equals( eTag ) ) {
                try {
                    long ifRangeTime = request.getDateHeader( "If-Range" ); // Throws
                    // IAE
                    // if
                    // invalid.
                    if( ifRangeTime != -1 && ifRangeTime + 1000 < lastModified ) {
                        ranges.add( full );
                    }
                } catch (IllegalArgumentException ignore) {
                    ranges.add( full );
                }
            }

            // If any valid If-Range header, then process each part of byte
            // range.
            if( ranges.isEmpty() ) {
                for( String part : range.substring( 6 ).split( "," ) ) {
                    // Assuming a file with length of 100, the following
                    // examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20
                    // (length-20=80 to length=100).
                    long start = sublong( part, 0, part.indexOf( "-" ) );
                    long end = sublong( part, part.indexOf( "-" ) + 1, part.length() );

                    if( start == -1 ) {
                        start = length - end;
                        end = length - 1;
                    } else if( end == -1 || end > length - 1 ) {
                        end = length - 1;
                    }

                    // Check if Range is syntactically valid. If not, then
                    // return 416.
                    if( start > end ) {
                        response.setHeader( "Content-Range", "bytes */" + length ); // Required
                        // in
                        // 416.
                        response.sendError( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                        return;
                    }

                    // Add range.
                    ranges.add( new Range( start, end, length ) );
                }
            }
        }

        // Prepare and initialize response
        // --------------------------------------------------------

        // Get content type by file name and set default GZIP support and
        // content disposition.
        String contentType = new MimetypesFileTypeMap().getContentType(fileName);
        boolean acceptsGzip = false;
        String disposition = "inline";

        // If content type is unknown, then set the default value.
        // For all content types, see:
        // http://www.w3schools.com/media/media_mimeref.asp
        // To add new content types, add new mime-mapping entry in web.xml.
        if( contentType == null ) {
            contentType = "application/octet-stream";
        }

        // If content type is text, then determine whether GZIP content encoding
        // is supported by
        // the browser and expand content type with the one and right character
        // encoding.
        if( contentType.startsWith( "text" ) ) {
            String acceptEncoding = request.getHeader( "Accept-Encoding" );
            acceptsGzip = acceptEncoding != null && accepts( acceptEncoding, "gzip" );
            contentType += ";charset=UTF-8";
        }

        // Else, expect for images, determine content disposition. If content
        // type is supported by
        // the browser, then set to inline, else attachment which will pop a
        // 'save as' dialogue.
        else if( !contentType.startsWith( "image" ) ) {
            String accept = request.getHeader( "Accept" );
            disposition = accept != null && accepts( accept, contentType ) ? "inline" : "attachment";
        }

        // Initialize response.
        response.reset();
        response.setBufferSize( DEFAULT_BUFFER_SIZE );
        response.setHeader( "Content-Disposition", disposition + ";filename=\"" + fileName + "\"" );
        response.setHeader( "Accept-Ranges", "bytes" );
        response.setHeader( "ETag", eTag );
        response.setDateHeader( "Last-Modified", lastModified );
        response.setDateHeader( "Expires", System.currentTimeMillis() + DEFAULT_EXPIRE_TIME );

        // Send requested file (part(s)) to client
        // ------------------------------------------------

        // Prepare streams.
        RandomAccessFile input = null;
        OutputStream output = null;

        try {
            // Open streams.
            input = new RandomAccessFile( file, "r" );
            output = response.getOutputStream();

            if( ranges.isEmpty() || ranges.get( 0 ) == full ) {

                // Return full file.
                Range r = full;
                response.setContentType( contentType );
                response.setHeader( "Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total );

                if( content ) {
                    if( acceptsGzip ) {
                        // The browser accepts GZIP, so GZIP the content.
                        response.setHeader( "Content-Encoding", "gzip" );
                        output = new GZIPOutputStream( output, DEFAULT_BUFFER_SIZE );
                    } else {
                        // Content length is not directly predictable in case of
                        // GZIP.
                        // So only add it if there is no means of GZIP, else
                        // browser will hang.
                        response.setHeader( "Content-Length", String.valueOf( r.length ) );
                    }

                    // Copy full range.
                    copy( input, output, r.start, r.length );
                }

            } else if( ranges.size() == 1 ) {

                // Return single part of file.
                Range r = ranges.get( 0 );
                response.setContentType( contentType );
                response.setHeader( "Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total );
                response.setHeader( "Content-Length", String.valueOf( r.length ) );
                response.setStatus( HttpServletResponse.SC_PARTIAL_CONTENT ); // 206.

                if( content ) {
                    // Copy single part range.
                    copy( input, output, r.start, r.length );
                }

            } else {

                // Return multiple parts of file.
                response.setContentType( "multipart/byteranges; boundary=" + MULTIPART_BOUNDARY );
                response.setStatus( HttpServletResponse.SC_PARTIAL_CONTENT ); // 206.

                if( content ) {
                    // Cast back to ServletOutputStream to get the easy println
                    // methods.
                    ServletOutputStream sos = (ServletOutputStream) output;

                    // Copy multi part range.
                    for( Range r : ranges ) {
                        // Add multipart boundary and header fields for every
                        // range.
                        sos.println();
                        sos.println( "--" + MULTIPART_BOUNDARY );
                        sos.println( "Content-Type: " + contentType );
                        sos.println( "Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total );

                        // Copy single part range of multi part range.
                        copy( input, output, r.start, r.length );
                    }

                    // End with multipart boundary.
                    sos.println();
                    sos.println( "--" + MULTIPART_BOUNDARY + "--" );
                }
            }
        } finally {
            // Gently close streams.
            close( output );
            close( input );
        }
    }
}
