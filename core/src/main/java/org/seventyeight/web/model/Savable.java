package org.seventyeight.web.model;

import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;


public interface Savable {
	public ODocument getNode();
	public void save( Request request, JsonObject jsonData );
}
