package org.seventyeight.web.toplevelaction.resources;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.MissingDescriptorException;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.model.*;
import org.seventyeight.web.servlet.Request;
import org.seventyeight.web.servlet.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cwolfgang
 *         Date: 17-01-13
 *         Time: 14:08
 */
public class ResourcesAction implements TopLevelAction, Actionable {

    private Logger logger = Logger.getLogger( ResourcesAction.class );

    @Override
    public String getDisplayName() {
        return "Resources";
    }

    public void doCreate( Request request, Response response, JsonObject jsonData ) throws IOException, TemplateDoesNotExistException {
        String className = request.getValue( "className" );

        if( className == null ) {
            throw new IOException( "No className given" );
        }

        /* Get the resource descriptor from the className name */
        ResourceDescriptor<?> descriptor = null;
        try {
            descriptor = (ResourceDescriptor<?>) SeventyEight.getInstance().getResourceDescriptor( className );
        } catch( ClassNotFoundException e ) {
            throw new IOException( e );
        }

        if( descriptor == null ) {
            throw new IOException( new MissingDescriptorException( "Could not find descriptor for " + className ) );
        }

        /* First of all we need to create the resource node */
        logger.debug( "Newing resource" );
        AbstractResource r = null;
        try {
            r = (AbstractResource) descriptor.newInstance( request.getDB() );
            String title = request.getValue( "title", "" );
            r.setTitle( title, null );
            r.getNode().save();
        } catch( UnableToInstantiateObjectException e ) {
            throw new IOException( e );
        }
        logger.debug( "RESOURCE IS " + r );

        r.setOwner( request.getUser() );
        r.getNode().save();

        response.sendRedirect( r.getUrl() + "configure" );
        //ResourceUtils.getConfigureResourceView( request, response, r, descriptor );
    }

    public void doType( Request request, Response response ) {

    }

    @Override
    public List<Action> getActions() {
        List<Action> actions = new ArrayList<Action>(1);
        actions.add( new ListAction() );
        return actions;
    }

    @Override
    public String getUrlName() {
        return "resources";
    }
}
