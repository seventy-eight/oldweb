package org.seventyeight.structure;

/**
 * @author cwolfgang
 *         Date: 24-01-13
 *         Time: 15:29
 */
public class Tuple<T1, T2> {
    public T1 first;
    public T2 second;

    public Tuple( T1 first, T2 second ) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return first + ", " + second;
    }

    public static <ST1, ST2> Tuple<ST1, ST2> tuple( ST1 first, ST2 second ) {
        return new Tuple<ST1, ST2>( first, second );
    }
}
