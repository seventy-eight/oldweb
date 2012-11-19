package org.seventyeight.web.model;

import org.seventyeight.database.Node;

public interface Item extends Savable {
	public String getDisplayName();
	public Node getNode();
}
