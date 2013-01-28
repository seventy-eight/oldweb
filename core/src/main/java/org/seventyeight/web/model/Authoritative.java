package org.seventyeight.web.model;

import org.seventyeight.web.model.resources.User;

/**
 * @author cwolfgang
 *         Date: 27-01-13
 *         Time: 21:09
 */
public interface Authoritative {
    public boolean isAuthoritative( User user );
}
