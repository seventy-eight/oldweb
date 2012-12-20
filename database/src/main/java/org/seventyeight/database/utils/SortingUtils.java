package org.seventyeight.database.utils;

import org.seventyeight.database.Node;

import java.util.Comparator;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 22:52
 */
public class SortingUtils {
    private SortingUtils() {}

    public static class NodeSorter implements Comparator<Node> {

        private String field;

        public NodeSorter( String field ) {
            this.field = field;
        }

        @Override
        public int compare( Node o1, Node o2 ) {
            int id1 = o1.get( field );
            int id2 = o2.get( field );

            if( id1 > id2 ) {
                return 1;
            } else if( id1 < id2 ) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
