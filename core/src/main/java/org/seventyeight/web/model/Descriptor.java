package org.seventyeight.web.model;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.seventyeight.database.Database;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.servlet.Request;

public abstract class Descriptor<T extends Describable> {
	
	private static Logger logger = Logger.getLogger( Descriptor.class );
	
	protected Class<T> clazz;
	
	protected Descriptor() {
		clazz = (Class<T>) getClass().getEnclosingClass();
		logger.debug( "Descriptor class is " + clazz );
	}
	
	public abstract String getDisplayName();

	public T newInstance( Database db ) throws UnableToInstantiateObjectException {
		logger.debug( "New instance for " + clazz );
		//ODocument node = SeventyEight.getInstance().createNode( clazz, NodeType.item );
        Node node = db.createNode();
		
		T instance = null;
		try {
			Constructor<T> c = clazz.getConstructor( Node.class );
			instance = c.newInstance( node );
		} catch( Exception e ) {
			throw new UnableToInstantiateObjectException( "Unable to instantiate " + clazz, e );
		}

        node.set( "class", getClazz().getName() );
		
		return instance;
	}

    public T getInstance( Node node ) throws UnableToInstantiateObjectException {
        T instance = null;
        try {
            Constructor<T> c = clazz.getConstructor( Node.class );
            instance = c.newInstance( node );
        } catch( Exception e ) {
            throw new UnableToInstantiateObjectException( "Unable to instantiate " + clazz, e );
        }

        return instance;
    }

    /**
     * Get the descriptors for
     * @return
     */
    public List<Class> getExtensionClasses() {
        return Collections.emptyList();
    }

    /**
     * Get the class of the {@link Descriptor}s {@link Describable}.
     * @return
     */
	public Class<? extends Describable> getClazz() {
		return clazz;
	}

    public String getId() {
        return getClazz().getName();
    }

    public String getJsonId() {
        return getId().replace( '.', '-' );
    }

    /**
     * When instantiated the descriptor can configure an index
     */
    public void configureIndex( Database db ) {
        /* Default implementation is a no op */
    }

    public boolean enabledByDefault() {
        return false;
    }

    public String getConfigurationPage( Request request, AbstractExtension extension ) throws TemplateDoesNotExistException {
        VelocityContext c = new VelocityContext();
        c.put( "class", getClazz().getName() );
        c.put( "descriptor", this );

        if( extension != null ) {
            //Descriptor d = extension.getDescriptor();
            logger.debug( "Extension is " + extension );
            c.put( "enabled", true );
            c.put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( extension, "config.vm" ) );
        } else {
            logger.debug( "Preparing EMPTY " + getClazz() );
            c.put( "enabled", false );
            c.put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderClass( getClazz(), "config.vm" ) );
        }

        return SeventyEight.getInstance().getTemplateManager().getRenderer( request ).setContext( c ).render( "org/seventyeight/web/model/descriptorpage.vm" );
    }

    public EdgeType getRelationType() {
        return SeventyEight.ResourceEdgeType.extension;
    }

    /**
     * Determine whether to remove data items on configure.
     * @return
     */
    public boolean doRemoveDataItemOnConfigure() {
        return false;
    }

    public String getEnctype() {
        return "application/x-www-form-urlencoded";
    }

}
