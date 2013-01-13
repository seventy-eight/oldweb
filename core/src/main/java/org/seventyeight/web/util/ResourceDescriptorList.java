package org.seventyeight.web.util;

import org.seventyeight.web.model.ResourceDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author cwolfgang
 *         Date: 12-01-13
 *         Time: 13:41
 */
public class ResourceDescriptorList extends ArrayList<ResourceDescriptor<?>> {

    public ResourceDescriptorList( Collection<? extends ResourceDescriptor<?>> c ) {
        super( c );
    }

    public void sortAlphabetically() {
        Collections.sort( this, new DisplayNameComparator() );
    }

    private class DisplayNameComparator implements Comparator<ResourceDescriptor> {

        @Override
        public int compare( ResourceDescriptor o1, ResourceDescriptor o2 ) {
            return o1.getDisplayName().compareTo( o2.getDisplayName() );
        }
    }
}
