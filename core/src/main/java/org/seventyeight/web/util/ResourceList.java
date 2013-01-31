package org.seventyeight.web.util;

import org.seventyeight.web.model.AbstractResource;

import java.util.LinkedList;

/**
 * @author cwolfgang
 *         Date: 31-01-13
 *         Time: 23:31
 */
public class ResourceList extends LinkedList<AbstractResource> {

    public ResourceList applyFilter( ResourceListFilter filter ) {
        filter.filter( this );

        return this;
    }
}
