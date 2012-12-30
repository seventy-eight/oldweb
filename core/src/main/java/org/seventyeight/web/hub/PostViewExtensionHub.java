package org.seventyeight.web.hub;

import com.google.gson.JsonObject;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.AbstractExtensionHub;
import org.seventyeight.web.model.ParameterRequest;

/**
 * @author cwolfgang
 *         Date: 29-12-12
 *         Time: 22:07
 */
public class PostViewExtensionHub extends AbstractExtensionHub {

    public enum ExtensionRelation implements EdgeType {
        postViewExtension
    }

    public PostViewExtensionHub( Node node ) {
        super( node );
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void doSave( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
        /* Currently no op */
    }

    @Override
    public EdgeType getRelationType() {
        return ExtensionRelation.postViewExtension;
    }
}
