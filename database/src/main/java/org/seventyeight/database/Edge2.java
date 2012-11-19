package org.seventyeight.database;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * <pre>
 * outNode --> edge --> inNode
 * </pre>
 * @author wolfgang
 *
 */
public class Edge2 {
	private ODocument edge;
	private ODocument outNode;
	private ODocument inNode;
	
	public Edge2( ODocument edge, ODocument outNode, ODocument inNode ) {
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
	
	public void delete( OGraphDatabase db ) {
		//SeventyEight.getInstance().removeEdge( edge );
        //OrientDBUtils.removeEdge( db, edge );
		edge = null;
	}
	
	public String toString() {
		return inNode + " -> " + edge + " -> " + outNode;
	}
}
