package org.seventyeight.web.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class Text {
	ODocument node;
	
	public Text( ODocument node ) {
		this.node = node;
	}
	
	public void setText( String language, String text ) {
		node.field( "language", language ).field( "text", text );
	}
	
	public String getText() {
		return node.field("text");
	}
	
	/*
	public static Text create( Object item, String property, String language ) {
		//Node node = GraphDragon.getInstance().getGraphDB().createNode();
		//Relationship rel = item.node.createRelationshipTo( node, I18N.TRANSLATION );
		rel.setProperty( "language", language );
		rel.setProperty( "property", property );
		
		return new Text( node );
	}
	*/
}