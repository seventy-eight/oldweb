package org.seventyeight.web.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

public interface Describable extends Item, DatabaseItem {
	public Descriptor<?> getDescriptor();
}
