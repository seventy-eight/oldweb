package org.seventyeight.database.orientdb;

import org.junit.ClassRule;
import org.junit.Test;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.IndexBuilder;
import org.seventyeight.database.IndexType;
import org.seventyeight.database.orientdb.impl.orientdb.OrientIndexer;
import org.seventyeight.database.orientdb.impl.orientdb.OrientNode;

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

        System.out.println( "DB1: " + orule.getDB().getInternalDatabase() );
        IndexBuilder i = new OrientIndexer( orule.getDB().getInternalDatabase(), "letter" ).setIndexType( IndexType.STRING ).build();

        OrientNode n1 = orule.CreateNode( "letter", "a" );
        OrientNode n2 = orule.CreateNode( "letter", "b" );
        OrientNode n3 = orule.CreateNode( "letter", "c" );
    }
}
