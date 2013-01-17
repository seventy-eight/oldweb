package org.seventyeight.web.toplevelaction.resources;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.web.exceptions.ActionHandlerException;
import org.seventyeight.web.model.Action;
import org.seventyeight.web.model.Actionable;
import org.seventyeight.web.model.Request;
import org.seventyeight.web.model.TopLevelAction;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 17-01-13
 *         Time: 14:08
 */
public class ResourcesAction implements TopLevelAction, Actionable {

    private Logger logger = Logger.getLogger( ResourcesAction.class );

    @Override
    public boolean execute( Request request, HttpServletResponse response ) throws ActionHandlerException {
        return false;
    }

    public void doCreate( Request request, JsonObject jsonData ) {
        logger.fatal( "YAY, resources!!!" );
    }

    @Override
    public Action getAction( String subSpace ) {
        return null;
    }

    @Override
    public String getName() {
        return "resources";
    }
}
