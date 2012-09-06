package org.seventyeight.web.model;

import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.EdgeType;
import org.seventyeight.web.SeventyEight.NodeType;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;

import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class Text implements Item {
	ODocument node;
	
	public Text( ODocument node ) {
		this.node = node;
	}
	
	public void setText( String text ) {
		node.field( "text", text );
		node.save();
	}
	
	public String getText() {
		return node.field("text");
	}
	
	public static Text create( Item item, String property, String language ) {
		ODocument node = SeventyEight.getInstance().createNode( Text.class, NodeType.text );
		Text t = new Text( node );
		
		ODocument edge = SeventyEight.getInstance().createEdge( item, t, ResourceEdgeType.translation );

		/* Edge data */
		edge.field( "language", language );
		edge.field( "property", property );
		edge.save();
		
		/* Node data */
		node.field( "language", language );
		node.field( "property", property );
		node.save();
		
		return new Text( node );
	}

	@Override
	public void save( ParameterRequest request, JsonObject jsonData ) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDisplayName() {
		return node.field( "language" ) + ": " + node.field( "property" );
	}

	@Override
	public ODocument getNode() {
		return node;
	}
}