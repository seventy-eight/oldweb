package org.seventyeight.web.model;

import org.seventyeight.web.debate.exceptions.ReplyException;
import org.seventyeight.web.exceptions.NotRepliedException;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 09:13
 */
public interface Debatable {

    public static final String HUB_DEBATE = "debateHub";

    public List<Reply> getReplies();

    /**
     * Get a subset of replies, given an offset and a number of replies.
     * @param offset
     * @param number
     * @return
     */
    public List<Reply> getReplies( int offset, int number );

    /**
     * Reply to a debate
     * @param reply
     */
    public void reply( Reply reply ) throws NotRepliedException, ReplyException;

    /**
     * Determines whether this is debatable
     * @return
     */
    public boolean isDebatable();
}
