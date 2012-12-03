package org.seventyeight.web.authentication;

/**
 * @author cwolfgang
 *         Date: 03-12-12
 *         Time: 16:49
 */
public class Authentication {
    public static enum AuthenticationType {
        NOT_AUTHENTICATED,
        AUTHENTICATED;
    }

    private AuthenticationType authType = AuthenticationType.NOT_AUTHENTICATED;

    public void setAuthenticated() {
        authType = AuthenticationType.AUTHENTICATED;
    }

    public boolean isAuthenticated() {
        return authType.equals( AuthenticationType.AUTHENTICATED );
    }
}
