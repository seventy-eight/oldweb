package org.seventyeight.web.model;

public interface Describable extends Item, DatabaseItem, Savable {
	public Descriptor<?> getDescriptor();

    /**
     * Remove this describable from the persistence layer
     */
    public void remove();

    /**
     * If associated with a {@link AbstractHub} it should be returned otherwise null
     */
    //public AbstractHub getHub();
}
