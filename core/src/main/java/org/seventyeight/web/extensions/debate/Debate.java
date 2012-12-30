package org.seventyeight.web.extensions.debate;

import com.google.gson.JsonObject;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;
import org.seventyeight.web.model.extensions.PostViewExtension;

/**
 * @author cwolfgang
 *         Date: 30-12-12
 *         Time: 21:54
 */
public class Debate extends AbstractItem implements PostViewExtension, Describable {

    public Debate( Node node ) {
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
    public void doSave( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
    }

    public static class DebateDescriptor extends Descriptor<Debate> {

        @Override
        public String getDisplayName() {
            return "Debate";
        }

        @Override
        public String getType() {
            return null;
        }
    }
}
