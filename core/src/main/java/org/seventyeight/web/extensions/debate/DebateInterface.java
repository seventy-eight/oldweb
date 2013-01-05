package org.seventyeight.web.extensions.debate;

import org.seventyeight.web.model.Reply;

/**
 * @author cwolfgang
 *         Date: 05-01-13
 *         Time: 23:26
 */
public interface DebateInterface {
    public void addReply( Reply reply ) throws DebateException;
}
