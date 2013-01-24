package org.seventyeight.web;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.seventyeight.database.Edge;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.CouldNotLoadObjectException;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.extensions.debate.Debate;
import org.seventyeight.web.extensions.debate.simple.SimpleDebate;
import org.seventyeight.web.model.resources.Group;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 *         Date: 24-01-13
 *         Time: 15:53
 */
public class ExtensionsTest {

    Group group;
    Debate debate;
    SimpleDebate simpleDebate;

    @Rule
    public EnvRule env = new EnvRule();

    @Before
    public void addingExtension() throws UnableToInstantiateObjectException, CouldNotLoadObjectException {
        group = (Group) SeventyEight.getInstance().getDescriptor( Group.class ).newInstance( env.getDB() );
        group.setTitle( "YEAH" );
        group.getNode().save();

        debate = (Debate) SeventyEight.getInstance().getDescriptor( Debate.class ).newInstance( env.getDB() );
        simpleDebate = (SimpleDebate) SeventyEight.getInstance().getDescriptor( SimpleDebate.class ).newInstance( env.getDB() );

        debate.addExtension( simpleDebate );
        group.addExtension( debate );
    }

    @Test
    public void testExtension() throws CouldNotLoadObjectException {
        assertThat( group.getTitle(), is( "YEAH" ) );

        List<Node> nodes = group.getExtensionNodesByClass( Debate.class );

        assertThat( nodes.size(), is( 1 ) );
        Debate d = SeventyEight.getInstance().getDatabaseItem( nodes.get( 0 ) );
        assertThat( d, is( debate ) );
    }

    @Test
    public void testRemoval() {
        String did = debate.getNode().getId( false );
        String sdid = simpleDebate.getNode().getId( false );

        List<Edge> edges = group.getALlExtensions();

        assertThat( edges.size(), is( 1 ) );

        group.recursivelyRemoveExtensions( edges.get( 0 ) );
        Node n = env.getDB().getByIndex( did );

        System.out.println( "NODE: " + n );
    }
}
