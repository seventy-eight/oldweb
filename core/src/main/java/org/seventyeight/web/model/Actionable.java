package org.seventyeight.web.model;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 03-12-12
 *         Time: 09:56
 */
public interface Actionable {
    public List<Action> getActions();
    //public Action getAction( Request request, String urlName );
    //public void addAction( String subSpace );
}
