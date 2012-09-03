package org.seventyeight.model.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.seventyeight.GraphDragon;
import org.seventyeight.annotations.ResourceType;
import org.seventyeight.exceptions.ErrorWhileSavingException;
import org.seventyeight.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.model.AbstractResource;
import org.seventyeight.model.Extension;
import org.seventyeight.model.RequestContext;
import org.seventyeight.model.ResourceDescriptor;

import com.google.gson.JsonObject;

@ResourceType
public class Decal extends Image {

	private static Logger logger = Logger.getLogger( Decal.class );
	public static final String __TYPE = "decal";

	public Decal( Node node ) {
		super( node );
	}
	
	@Override
	public String getType() {
		return Decal.__TYPE;
	}
	
	public class DecalSaveImpl extends ImageSaveImpl {

		public DecalSaveImpl( AbstractResource resource, RequestContext request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}

		@Override
		public void createImages( File imageFile ) throws ErrorWhileSavingException {
			createThumbnail( imageFile );
			createIcon( imageFile );
		}
	}

	public static List<Decal> getDecals() {
		
		Index<Node> idx = GraphDragon.getInstance().getResourceIndex();
		IndexHits<Node> nodes = idx.get( "type", Decal.__TYPE );
		
		List<Decal> deals = new ArrayList<Decal>();
		
		while( nodes.hasNext() ) {
			deals.add( new Decal( nodes.next() ) );
		}
		
		return deals;
	}
	
	
	public static class DecalDescriptor extends ResourceDescriptor<Decal> {

		@Override
		public String getDisplayName() {
			return "Decal";
		}
		
		@Override
		public String getType() {
			return "decal";
		}
		
		@Override
		public Class<? extends Extension> getExtensionClass() {
			return null;
		}

		@Override
		public Decal newInstance() throws UnableToInstantiateObjectException {
			return super.newInstance();
		}

	}

}
