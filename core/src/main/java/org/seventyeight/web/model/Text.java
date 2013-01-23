package org.seventyeight.web.model;

import org.apache.log4j.Logger;
import org.seventyeight.database.Database;
import org.seventyeight.database.Edge;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;

import com.google.gson.JsonObject;
import org.seventyeight.web.exceptions.*;

public class Text extends AbstractSubItem implements Item {
	private static Logger logger = Logger.getLogger( Text.class );
	
	public Text( Node node ) {
		super( node );
	}
	
	public void setText( String text ) {
		node.set( "text", text );
		node.save();
	}
	
	public String getText() {
		return (String) node.get( "text", "" );
	}

    @Override
    public EdgeType getEdgeType() {
        return ResourceEdgeType.translation;
    }

    public static Text create( Database db, DatabaseItem item, String property, String language ) {
		//ODocument node = SeventyEight.getInstance().createNode( Text.class, NodeType.text );
		Text t = new Text( db.createNode() );

		//List<Edge> edges = t.getNode().getEdgesTo( ResourceEdgeType.translation );
        Edge edge = item.getNode().createEdge( t.getNode(), ResourceEdgeType.translation ).save();
		//ODocument edge = SeventyEight.getInstance().createEdge( item, t, ResourceEdgeType.translation );

		/* Edge data */
		edge.set( "language", language );
		edge.set( "property", property );
		edge.save();
		
		/* Node data */
		t.getNode().set( "language", language );
        t.getNode().set( "property", property );
        t.getNode().save();
		
		return t;
	}

	@Override
    public final void save( CoreRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDisplayName() {
		return node.get( "language" ) + ": " + node.get( "property" );
	}

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public Edge createRelation( DatabaseItem other, EdgeType type ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getItemClass() {
        return getClass().getSimpleName();
    }

    @Override
    public Database getDB() {
        return node.getDB();
    }
}