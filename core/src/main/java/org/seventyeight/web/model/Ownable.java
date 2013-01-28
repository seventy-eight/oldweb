package org.seventyeight.web.model;

import org.seventyeight.web.exceptions.PersistenceException;
import org.seventyeight.web.model.resources.User;

public interface Ownable {
	public void setOwner( User owner );
	public User getOwner() throws IllegalStateException, PersistenceException;
}
