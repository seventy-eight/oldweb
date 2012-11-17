package org.seventyeight.web.model.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.Extension;
import org.seventyeight.web.model.ParameterRequest;
import org.seventyeight.web.model.ResourceDescriptor;

public class Decal extends Image {

	private static Logger logger = Logger.getLogger( Decal.class );
	public static final String __TYPE = "decal";

	public Decal( ODocument node ) {
		super( node );
	}
	
	@Override
	public String getType() {
		return Decal.__TYPE;
	}
	
	public class DecalSaveImpl extends ImageSaveImpl {

		public DecalSaveImpl( AbstractResource resource, ParameterRequest request, JsonObject jsonData ) {
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
