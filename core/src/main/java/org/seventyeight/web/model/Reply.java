package org.seventyeight.web.model;

import org.seventyeight.database.EdgeType;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 09:14
 */
public interface Reply extends DatabaseItem, Savable {

    public enum ReplyRelation implements EdgeType {
        reply
    }

    public int getOrder();

    /**
     * Get the {@link List} of immediate(adjacent) replies to this {@link Reply}
     * @return
     */
    public List<Reply> getReplies();

    /**
     * Get the number of replies to this {@link Reply}(Possibly recursively down the paths)
     * @return
     */
    public int getNumberOfReplies();
}
