package org.seventyeight.web.extensions.debate.simple;

import org.seventyeight.database.Node;
import org.seventyeight.web.extensions.debate.AbstractDebate;
import org.seventyeight.web.extensions.debate.DebateInterface;
import org.seventyeight.web.model.Descriptor;

/**
 * @author cwolfgang
 *         Date: 05-01-13
 *         Time: 23:25
 */
public class SimpleDebate extends AbstractDebate {

    public SimpleDebate( Node node ) {
        super( node );
    }

    @Override
    public Descriptor<?> getReplyDescriptor() {
        return null;
    }

    public static class SimpleDebateDescriptor extends DebateDescriptor {

    }
}
