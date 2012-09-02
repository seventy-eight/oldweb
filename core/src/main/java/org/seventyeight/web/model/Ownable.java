package org.seventyeight.web.model;

public interface Ownable {
	public void setOwner( User owner );
	public User getOwner();
}
