package org.seventyeight.database.orientdb;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.junit.ClassRule;
import org.junit.Test;
import org.seventyeight.database.EdgeType;
import org.seventyeight.database.Node;

/**
 * User: cwolfgang
 * Date: 17-11-12
 * Time: 23:08
 */
public class OrientDBUtilsTest {

    public enum TestType implements EdgeType {
        TEST
    }

    @ClassRule
    public static OrientDBRule orule = new OrientDBRule();

    @Test
    public void test1() {
        ODocument doc1 = orule.getDB().createVertex();
        doc1.field( "field", "f" );
        doc1.save();

        DocumentVerifier verifier = new DocumentVerifier( orule.getDB(), doc1 ).addField( "field", "f" ).verify();
    }

    @Test
    public void test2() {
        Node n1 = orule.createNode();
        n1.getNode().field( "field", "a" );
        n1.getNode().save();

        Node n2 = orule.createNode();
        n2.getNode().field( "field", "b" );
        n2.getNode().save();

        OrientDBUtils.createEdge( orule.getDB(), n1, n2, TestType.TEST );
    }
}
