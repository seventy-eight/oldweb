package org.seventyeight.web.graph;

import org.seventyeight.web.SeventyEight;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class Edge {
	private ODocument edge;
	private ODocument outNode;
	private ODocument inNode;
	
	public Edge( ODocument edge, ODocument inNode, ODocument outNode ) {
		this.edge = edge;
		this.inNode = inNode;
		this.outNode = outNode;
	}

	public ODocument getEdge() {
		return edge;
	}

	public ODocument getOutNode() {
		return outNode;
	}

	public ODocument getInNode() {
		return inNode;
	}
	
	public void delete() {
		SeventyEight.getInstance().removeEdge( edge );
		edge = null;
	}
}
