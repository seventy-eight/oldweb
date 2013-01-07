package org.seventyeight.web.model.toplevelactionhandlers;

import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.TopLevelAction;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 07-01-13
 *         Time: 22:03
 */
public class InfoHandler implements TopLevelAction {

    @Override
    public void prepare( Request request ) {
    }

    @Override
    public void execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
    }

    @Override
    public String getName() {
        return "info";
    }
}
