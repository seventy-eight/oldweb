package org.seventyeight.utils;

import java.util.List;

import static java.lang.Math.random;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 21:09
 */
public class RandomCollections {

    private RandomCollections() {}

    public static <T> List<T> randomList( List<T> list ) {

        int s = list.size();
        int sm1 = s - 1;
        for( int i = 0 ; i < s ; i++ ) {
            int idx = random( 0, sm1 );

            if( i != idx ) {
                T temp = list.get( i );
                list.set( i, list.get( idx ) );
                list.set( idx, temp );
            }
        }

        return list;
    }

    public static int random( int from, int to ) {
        int diff = to - from;
        double r = Math.random();
        return (int) (from + r * diff);
    }
}
