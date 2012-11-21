package org.seventyeight.database.orientdb;

import org.junit.ClassRule;
import org.junit.Test;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.IndexValueType;
import org.seventyeight.database.Query;
import org.seventyeight.database.orientdb.impl.orientdb.query.OrientIndexBuilder;
import org.seventyeight.database.orientdb.impl.orientdb.OrientNode;
import org.seventyeight.database.query.IndexType;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 *         Date: 20-11-12
 *         Time: 22:47
 */
public class OrientIndexerTest {

    public enum TestType implements EdgeType {
        TEST
    }

    @ClassRule
    public static OrientDBRule orule = new OrientDBRule();

    @Test
    public void test1() {
        /*
        Query q = new OrientIndexBuilder( orule.getDB().getInternalDatabase(), "letter" ).setIndexValueType( IndexValueType.STRING ).build();
        q.execute();
        System.out.println( "q: " + q );
        */

        orule.getDB().createIndex( "letter", IndexType.REGULAR, IndexValueType.STRING );

        OrientNode n1 = orule.CreateNode( "letter", "a" );
        OrientNode n2 = orule.CreateNode( "letter", "b" );
        OrientNode n3 = orule.CreateNode( "letter", "c" );

        orule.getDB().putIndex( "letter", n1, "a" );
        orule.getDB().putIndex( "letter", n2, "b" );
        orule.getDB().putIndex( "letter", n3, "c" );

        List<OrientNode> nodes = orule.getDB().getFromIndex( "letter", "b" );
        assertThat( nodes.size(), is( 1 ) );

        System.out.println( nodes );
    }


}
