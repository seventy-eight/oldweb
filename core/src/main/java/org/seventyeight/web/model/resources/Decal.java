package org.seventyeight.web.model.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import org.seventyeight.database.Database;
import org.seventyeight.database.Edge;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.model.*;

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

		public DecalSaveImpl( AbstractResource resource, CoreRequest request, JsonObject jsonData ) {
			super( resource, request, jsonData );
		}

		@Override
		public void createImages( File imageFile ) throws ErrorWhileSavingException {
			createThumbnail( imageFile );
			createIcon( imageFile );
		}
	}

	public static List<Decal> getDecals( Database db ) {
		
		//Index<Node> idx = GraphDragon.getInstance().getResourceIndex();
		//IndexHits<Node> nodes = idx.get( "type", Decal.__TYPE );

        List<Edge> edges = db.getFromIndex( SeventyEight.INDEX_RESOURCE_TYPES, __TYPE );
		
		List<Decal> deals = new ArrayList<Decal>();
		
		for( Edge edge : edges ) {
			deals.add( new Decal( edge.getTargetNode() ) );
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
	}

}
