package org.seventyeight.utils;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author cwolfgang
 *         Date: 30-11-12
 *         Time: 12:57
 */
public class ExceptionUtils {
    public static Throwable print( Throwable e, PrintWriter writer, boolean stack ) {

        if( e.getCause() != null ) {
            writer.println( e.getMessage() );
            return print( e.getCause(), writer, stack );
        } else {
            if( stack ) {
                e.printStackTrace( writer );
            } else {
                writer.println( e.getMessage() );
            }

            return e;
        }
    }

    public static Throwable getRootCause( Throwable e ) {
        if( e.getCause() != null ) {
            return getRootCause( e.getCause() );
        } else {
            return e;
        }
    }

    public static Throwable print( Throwable e, PrintStream writer, boolean stack ) {

        if( e.getCause() != null ) {
            writer.println( e.getMessage() );
            return print( e.getCause(), writer, stack );
        } else {
            if( stack ) {
                e.printStackTrace( writer );
            } else {
                writer.println( e.getMessage() );
            }

            return e;
        }
    }

}
