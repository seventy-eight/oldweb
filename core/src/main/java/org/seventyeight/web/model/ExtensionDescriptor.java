package org.seventyeight.web.model;

import org.seventyeight.database.Database;
import org.seventyeight.database.EdgeType;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 14:50
 */
public abstract class ExtensionDescriptor<T extends AbstractExtension> extends Descriptor<T> {

    @Override
    public EdgeType getRelationType() {
        return SeventyEight.ResourceEdgeType.extension;
    }

    @Override
    public T newInstance( Database db ) throws UnableToInstantiateObjectException {
        T instance = super.newInstance( db );
        instance.getNode().set( SeventyEight.FIELD_EXTENSION_CLASS, instance.getExtensionClass() ).save();

        return instance;
    }
}
