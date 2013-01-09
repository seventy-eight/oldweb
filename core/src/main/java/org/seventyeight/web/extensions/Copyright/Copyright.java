package org.seventyeight.web.extensions.Copyright;

import org.seventyeight.database.Node;
import org.seventyeight.web.extensions.PostViewDecorator;
import org.seventyeight.web.model.Descriptor;

/**
 * @author cwolfgang
 *         Date: 09-01-13
 *         Time: 12:53
 */
public class Copyright extends PostViewDecorator {

    public Copyright( Node node ) {
        super( node );
    }

    @Override
    public String getDisplayName() {
        return "Copyrigth";
    }

    public static class CopyrightDescriptor extends Descriptor<Copyright> {

        @Override
        public String getDisplayName() {
            return "Copyright";
        }

    }
}
