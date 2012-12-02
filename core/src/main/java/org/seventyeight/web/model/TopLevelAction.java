package org.seventyeight.web.model;

import java.lang.reflect.Method;

/**
 * @author cwolfgang
 *         Date: 02-12-12
 *         Time: 16:00
 */
public interface TopLevelAction extends Action {
    public Method getMethod( String[] parts, Class<? extends TopLevelAction> clazz ) throws NoSuchMethodException;
    public String getName();
}
