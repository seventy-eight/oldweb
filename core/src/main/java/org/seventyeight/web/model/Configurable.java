package org.seventyeight.web.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public interface Configurable extends Savable, Item {
	public ODocument getNode();
	public Descriptor<?> getDescriptor();
}
