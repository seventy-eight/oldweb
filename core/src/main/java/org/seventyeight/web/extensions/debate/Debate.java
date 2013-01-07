package org.seventyeight.web.extensions.debate;

import com.google.gson.JsonObject;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;
import org.seventyeight.web.model.extensions.PostViewExtension;

import java.util.Collections;
import java.util.List;

/**
 * @author cwolfgang
 *         Date: 30-12-12
 *         Time: 21:54
 */
public class Debate extends AbstractExtensibleItem implements PostViewExtension, Describable {

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
    public final void save( ParameterRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
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

        public List<Descriptor> getDebates() {
            return AbstractDebate.all();
        }

        public List<Descriptor> getDebates2() {
            return Collections.EMPTY_LIST;
        }
    }
}
