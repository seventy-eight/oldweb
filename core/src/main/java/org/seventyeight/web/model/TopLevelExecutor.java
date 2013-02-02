package org.seventyeight.web.model;

import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.servlet.Request;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 02-12-12
 *         Time: 16:00
 */
public interface TopLevelExecutor extends TopLevelGizmo {

    public void execute( Request request, HttpServletResponse response ) throws ActionHandlerException;
}
