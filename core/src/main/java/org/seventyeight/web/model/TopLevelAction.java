package org.seventyeight.web.model;

import org.seventyeight.web.exceptions.ActionHandlerException;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 02-12-12
 *         Time: 16:00
 */
public interface TopLevelAction extends Action, Actionable {
    /**
     * If true is returned, default handling should not be executed.
     * @param request
     * @param response
     * @return
     * @throws ActionHandlerException
     */
    public boolean execute( Request request, HttpServletResponse response ) throws ActionHandlerException;
}
