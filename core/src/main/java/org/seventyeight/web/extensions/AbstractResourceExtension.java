package org.seventyeight.web.extensions;

import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.model.AbstractItem;
import org.seventyeight.web.model.Describable;
import org.seventyeight.web.model.Descriptor;
import org.seventyeight.web.model.extensions.ResourceExtension;

/**
 * @author cwolfgang
 *         Date: 09-01-13
 *         Time: 13:02
 */
public abstract class AbstractResourceExtension extends AbstractItem implements ResourceExtension, Describable {

    public AbstractResourceExtension( Node node ) {
        super( node );
    }

    @Override
    public Descriptor<?> getDescriptor() {
        return SeventyEight.getInstance().getDescriptor( getClass() );
    }
}
