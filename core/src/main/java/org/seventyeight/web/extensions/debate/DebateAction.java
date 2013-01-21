package org.seventyeight.web.extensions.debate;

import org.seventyeight.database.Node;
import org.seventyeight.web.model.AbstractAction;

/**
 * @author cwolfgang
 *         Date: 21-01-13
 *         Time: 15:06
 */
public class DebateAction extends AbstractAction {

    public DebateAction( Node node ) {
        super( node );
    }

    @Override
    public String getUrlName() {
        return "debate";
    }

    @Override
    public String getDisplayName() {
        return "Debate!";
    }
}
