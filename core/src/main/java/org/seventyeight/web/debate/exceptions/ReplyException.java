package org.seventyeight.web.debate.exceptions;

import org.seventyeight.web.exceptions.SeventyEightException;

/**
 * @author cwolfgang
 *         Date: 21-12-12
 *         Time: 12:33
 */
public class ReplyException extends SeventyEightException {

    public ReplyException( String m, Exception e ) {
        super( m, e );
    }
}
