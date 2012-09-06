package org.seventyeight.web;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.seventyeight.web.EnvRule.DummyItem;
import org.seventyeight.web.SeventyEight.NodeType;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.CouldNotLoadObjectException;
import org.seventyeight.web.graph.Edge;
import org.seventyeight.web.model.AbstractItem;
import org.seventyeight.web.model.Item;

import com.orientechnologies.orient.core.record.impl.ODocument;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class SeventyEightTest {
	private static Logger logger = Logger.getLogger( SeventyEightTest.class );
	
	@Rule
	public static EnvRule env = new EnvRule();
	
	
	@Test
	public void getInstance() {
		SeventyEight se = SeventyEight.getInstance();
	}
	
	@Test
	public void createNode() {
		SeventyEight se = SeventyEight.getInstance();
		ODocument node = se.createNode( getClass(), NodeType.item );
		assertThat( (String)node.field( "class" ), is( getClass().getName() ) );
	}
	
	@Test
	public void getItem() throws CouldNotLoadObjectException {
		SeventyEight se = SeventyEight.getInstance();
		ODocument node1 = se.createNode( DummyItem.class, NodeType.item );
		assertThat( (String)node1.field( "class" ), is( DummyItem.class.getName() ) );
		
		Item item1 = se.getItemByNode( node1 );
		
		assertThat( item1.getNode(), is( node1  ) );
	}
	
	@Test
	public void createEdge() throws CouldNotLoadObjectException {
		SeventyEight se = SeventyEight.getInstance();
		ODocument node1 = se.createNode( DummyItem.class, NodeType.item );
		ODocument node2 = se.createNode( DummyItem.class, NodeType.item );
		assertThat( (String)node1.field( "class" ), is( DummyItem.class.getName() ) );
		assertThat( (String)node2.field( "class" ), is( DummyItem.class.getName() ) );
		
		Item item1 = se.getItemByNode( node1 );
		Item item2 = se.getItemByNode( node2 );
		
		ODocument edge = se.createEdge( item1, item2, ResourceEdgeType.owner );
		System.out.println( "Edge: " + edge );
		assertThat( edge, not( nullValue()) );
		
		List<Edge> es = se.getEdges( item1, item2 );
		assertThat( es.size(), is( 1 ) );
		
		List<Edge> edges = se.getEdges2( item1, ResourceEdgeType.owner );
		assertThat( edges.size(), is( 1 ) );
		assertThat( edges.get( 0 ).getInNode(), is( node1 ) );
		assertThat( edges.get( 0 ).getOutNode(), is( node2 ) );
	}
}
