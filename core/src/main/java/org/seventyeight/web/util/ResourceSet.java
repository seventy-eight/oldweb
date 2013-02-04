package org.seventyeight.web.util;

import org.seventyeight.web.model.AbstractResource;

import java.util.HashSet;

/**
 * @author cwolfgang
 *         Date: 31-01-13
 *         Time: 23:31
 */
public class ResourceSet extends HashSet<AbstractResource> {

    public ResourceSet applyFilter( ResourceListFilter filter ) {
        filter.filter( this );

        return this;
    }
}
