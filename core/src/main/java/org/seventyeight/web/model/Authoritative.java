package org.seventyeight.web.model;

import org.seventyeight.web.model.resources.User;

/**
 * @author cwolfgang
 *         Date: 27-01-13
 *         Time: 21:09
 */
public interface Authoritative {
    public enum Authorization {
        NONE,
        VIEW,
        MODERATE
    }

    public Authorization getAuthorization( User user );
}
