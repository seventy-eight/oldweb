package org.seventyeight.web.model;

import org.seventyeight.web.model.resources.User;

/**
 * @author cwolfgang
 *         Date: 28-01-13
 *         Time: 13:40
 */
public interface Authorizer {
    public enum Authorization {
        NONE,
        VIEW,
        MODERATE
    }

    public Authorization getAuthorization( User user );
}
