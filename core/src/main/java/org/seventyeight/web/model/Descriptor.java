package org.seventyeight.web.model;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
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

public abstract class Descriptor<T extends Describable> {
	
	private static Logger logger = Logger.getLogger( Descriptor.class );
	
	protected Class<T> clazz;
	
	protected Descriptor() {
		clazz = (Class<T>) getClass().getEnclosingClass();
		logger.debug( "Descriptor class is " + clazz );
	}
	
	public abstract String getDisplayName();
	public abstract String getType();

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
	
	public Class<? extends Describable> getClazz() {
		return clazz;
	}

    /**
     * When instantiated the descriptor can configure an index
     */
    public void configureIndex( Database db ) {
        /* Default implementation is a no op */
    }

    public String getConfigurationPage( Request request, Node node ) throws TemplateDoesNotExistException {
        VelocityContext c = new VelocityContext();
        c.put( "class", getClazz().getName() );
        c.put( "disabled", true );

        T instance = null;
        try {
            instance = getInstance( node );
            c.put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( instance, "configure.vm" ) );
        } catch( UnableToInstantiateObjectException e ) {
            /* instance == null || node == null */
            logger.warn( e );
            request.getContext().remove( "item" );
            c.put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderClass( getClazz(), "configure.vm" ) );
        }

        logger.fatal( Arrays.asList( request.getContext().getKeys() ) );

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

}
