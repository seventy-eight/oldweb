package org.seventyeight.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 21:26
 */
public class RandomTest {

    @Test
    public void test1() {
        for( int i = 0 ; i < 10 ; i++ ) {
            int n = RandomCollections.random( 0, 10 );
            System.out.println( n );
        }
    }

    @Test
    public void test2() {
        List<Integer> list = new ArrayList<Integer>( 10 );
        for( int i = 0 ; i < 10 ; i++ ) {
            list.add( i );
        }

        System.out.println( list );
        RandomCollections.randomList( list );
        System.out.println( list );
    }
}
