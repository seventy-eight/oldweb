package org.seventyeight.web.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public interface Item extends Savable {
	public String getDisplayName();
	public ODocument getNode();
}
