package org.seventyeight.web.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.seventyeight.database.Node;

public interface Configurable<NODE extends Node> extends Item {
	public NODE getNode();
	public Descriptor<?> getDescriptor();
}
