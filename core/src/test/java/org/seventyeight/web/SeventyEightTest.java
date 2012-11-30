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
        assertNotNull( se );
	}

}
