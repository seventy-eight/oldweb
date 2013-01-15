package org.seventyeight.web.model;

import org.seventyeight.database.EdgeType;
import org.seventyeight.web.SeventyEight;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 14:50
 */
public abstract class ExtensionDescriptor extends Descriptor<Hub> {

    @Override
    public EdgeType getRelationType() {
        return SeventyEight.ResourceEdgeType.extension;
    }
}
