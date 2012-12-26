package org.seventyeight.web.model;

import org.seventyeight.database.EdgeType;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 14:50
 */
public abstract class HubDescriptor extends Descriptor<Hub> {

    /**
     * A resource hub is a {@link org.seventyeight.database.Node} that extracts specific relations from the actual resource. These {@link org.seventyeight.database.Node}'s should be identified with a property called <i>type</i>.
     * resourceHubRelations points to such {@link org.seventyeight.database.Node}'s
     */
    public enum HubRelation implements EdgeType {
        hub
    }

    @Override
    public EdgeType getRelationType() {
        return HubRelation.hub;
    }
}
