package org.seventyeight.database;

/**
 *
 * v1 ---(e)---> v2 <br />
 * e is outbound for v1<br />
 * e is inbound for v2<br />
 *
 * @author cwolfgang
 * Date: 19-11-12
 * Time: 17:24
 */
public enum Direction {
    /**
     * An inbound {@link Edge} points towards the given {@link Node}.<br />
     */
    INBOUND,

    /**
     * An inbound {@link Edge} points away from the given {@link Node}
     * The {@link Edge} originates from the {@link Node}. <br />
     * v1 --(e)--> v2<br />
     * Where v1 is the given {@link Node}
     */
    OUTBOUND,

    /**
     * Both inbound and outbound
     */
    BOTH
}
