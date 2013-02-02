package org.seventyeight.web.extensions.debate;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.Node;
import org.seventyeight.web.model.AbstractAction;
import org.seventyeight.web.servlet.Request;

import javax.servlet.http.HttpServletResponse;

/**
 * @author cwolfgang
 *         Date: 21-01-13
 *         Time: 15:06
 */
public class DebateAction extends AbstractAction {

    private static Logger logger = Logger.getLogger( DebateAction.class );

    public DebateAction( Node node ) {
        super( node );
    }

    @Override
    public String getUrlName() {
        return "debate";
    }

    @Override
    public String getDisplayName() {
        return "Debate!";
    }

    public void doIndex( Request request, HttpServletResponse response, JsonObject jsonObject ) {
        logger.debug( "I AM HERE!!!!!!!!!!!!!!!!" );
    }
}
