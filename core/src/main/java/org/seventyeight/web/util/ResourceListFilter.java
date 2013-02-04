package org.seventyeight.web.util;

/**
 * @author cwolfgang
 *         Date: 31-01-13
 *         Time: 23:32
 */
public interface ResourceListFilter {
    public void filter( ResourceSet set );
    public String getName();
}
