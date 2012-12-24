package org.seventyeight.web.model;

import org.seventyeight.database.EdgeType;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 14:50
 */
public abstract class HubDescriptor extends Descriptor<Hub> {

    public enum HubRelation implements EdgeType {
        hub
    }

    @Override
    public EdgeType getRelationType() {
        return HubRelation.hub;
    }
}
