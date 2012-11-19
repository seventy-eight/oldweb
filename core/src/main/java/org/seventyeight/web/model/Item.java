package org.seventyeight.web.model;

import org.seventyeight.database.Edge;
import org.seventyeight.database.Node;

public interface Item<NODE extends Node<NODE, EDGE>, EDGE extends Edge<EDGE, NODE>> extends Savable<NODE, EDGE> {
	public String getDisplayName();
}
