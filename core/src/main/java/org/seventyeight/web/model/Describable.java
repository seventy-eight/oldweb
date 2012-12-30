package org.seventyeight.web.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

public interface Describable extends Item, DatabaseItem {
	public Descriptor<?> getDescriptor();

    /**
     * Remove this describable from the persistence layer
     */
    public void remove();

    /**
     * If associated with a {@link Hub} it should be returned otherwise null
     */
    //public Hub getHub();
}
