package org.seventyeight.web.extensions;

import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.model.AbstractItem;
import org.seventyeight.web.model.Describable;
import org.seventyeight.web.model.Descriptor;
import org.seventyeight.web.model.extensions.PostViewExtension;

/**
 * @author cwolfgang
 *         Date: 09-01-13
 *         Time: 13:02
 */
public abstract class PostViewDecorator extends AbstractItem implements PostViewExtension, Describable {

    public PostViewDecorator( Node node ) {
        super( node );
    }

    @Override
    public Descriptor<?> getDescriptor() {
        return SeventyEight.getInstance().getDescriptor( getClass() );
    }
}
