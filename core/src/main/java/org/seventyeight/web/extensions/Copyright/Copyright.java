package org.seventyeight.web.extensions.Copyright;

import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.extensions.AbstractResourceExtension;
import org.seventyeight.web.model.Descriptor;

/**
 * @author cwolfgang
 *         Date: 09-01-13
 *         Time: 12:53
 */
public class Copyright extends AbstractResourceExtension {

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

    @Override
    public EdgeType getEdgeType() {
        return SeventyEight.ResourceEdgeType.extension;
    }
}
