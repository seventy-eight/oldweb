package org.seventyeight.web.util;

import java.lang.reflect.Method;

/**
 * @author cwolfgang
 *         Date: 14-01-13
 *         Time: 22:26
 */
public class ClassUtils {
    private ClassUtils() {}

    public static Method getEnheritedMethod( Class<?> clazz, String method, Class<?>... args ) throws NoSuchMethodException {
        while( clazz != null ) {
            try {
                return clazz.getDeclaredMethod( method, args );
            } catch( NoSuchMethodException e ) {

            }

            clazz = clazz.getSuperclass();
        }

        throw new NoSuchMethodException( method );
    }
}
