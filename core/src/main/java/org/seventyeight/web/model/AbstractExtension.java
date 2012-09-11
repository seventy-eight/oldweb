package org.seventyeight.web.model;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.graph.Edge;

public abstract class AbstractExtension extends AbstractItem implements Extension, Savable {
	
	private static Logger logger = Logger.getLogger( AbstractExtension.class );
	
	public AbstractExtension( ODocument node ) {
		super( node );
	}
	
	public ODocument getNode() {
		return node;
	}

	public void save( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		doSave( new ExtensionSave( this, request, jsonData ) );
	}
	
	public class ExtensionSave extends Save {

		public ExtensionSave( AbstractItem type, ParameterRequest request, JsonObject jsonData ) {
			super( type, request, jsonData );
		}

		@Override
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {

			/*
			Set<Entry<String, JsonElement>> js = jsonData.entrySet();
			for( Entry<String, JsonElement> jentry : js ) {
				if( jentry.getValue().isJsonObject() ) {
					node.setProperty( jentry.getKey(), jentry.getValue() );
				}
			}
			*/
			logger.debug( "No op!" );
		}
		
	}
		
	public Descriptor<?> getDescriptor() {
		return SeventyEight.getInstance().getDescriptor( getClass() );
	}
	
	/**
	 * Get an extension object. Origin ---- EXTENSION ----> Object
	 * @param rel
	 * @return
	 * @throws UnableToInstantiateObjectException
	 * @throws NoSuchObjectException
	 */
	public <T> T getObject( SeventyEight.EdgeType rel ) throws UnableToInstantiateObjectException, NoSuchObjectException {

        List<Edge> edges = SeventyEight.getInstance().getEdges2( this, rel );
		
		T a = null;
		
		/* Just get the first */
		if( edges.size() > 0 ) {
			ODocument n = edges.get( 0 ).getInNode();
			try {
				Class<? extends T> clazz = (Class<? extends T>) Class.forName( (String) n.field( "class" ) );
				Constructor<? extends T> c = clazz.getConstructor( ODocument.class );
				a = c.newInstance( n );
			} catch( Exception e ) {
				throw new UnableToInstantiateObjectException( "Unable to instantiate object with relationship type " + rel, e );
			}
		} else {
			throw new NoSuchObjectException( "Unable to find node with relation " + rel );
		}
		
		return a;
	}
}
