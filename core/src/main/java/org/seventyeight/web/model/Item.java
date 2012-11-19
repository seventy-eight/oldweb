package org.seventyeight.web.model;

import org.seventyeight.database.Node;

public interface Item<DB> extends Savable<DB> {
	public String getDisplayName();
}
