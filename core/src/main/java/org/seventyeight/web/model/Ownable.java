package org.seventyeight.web.model;

import javax.resource.spi.IllegalStateException;

import org.seventyeight.web.model.resources.User;

public interface Ownable {
	public void setOwner( User owner );
	public User getOwner() throws IllegalStateException;
}
