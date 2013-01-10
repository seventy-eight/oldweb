package org.seventyeight.web.extensions.debate;

import com.google.gson.JsonObject;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.AbstractItem;
import org.seventyeight.web.model.Describable;
import org.seventyeight.web.model.Descriptor;
import org.seventyeight.web.model.ParameterRequest;
import org.seventyeight.web.model.extensions.PostViewExtension;

/**
 * @author cwolfgang
 *         Date: 30-12-12
 *         Time: 21:54
 */
public class Debate2 extends AbstractItem implements PostViewExtension, Describable {

    public Debate2( Node node ) {
        super( node );
    }

    @Override
    public Descriptor<?> getDescriptor() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public final void save( ParameterRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
    }

    public static class DebateDescriptor extends Descriptor<Debate2> {

        @Override
        public String getDisplayName() {
            return "Debate";
        }
    }
}