package org.seventyeight.web.model.saving;

import org.junit.Rule;
import org.junit.Test;
import org.seventyeight.database.Direction;
import org.seventyeight.database.Edge;
import org.seventyeight.web.EnvRule;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.CoreRequest;
import org.seventyeight.web.model.resources.Group;
import org.seventyeight.web.model.util.Parameters;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 23-01-13
 *         Time: 09:57
 */
public class Testing {

    @Rule
    public EnvRule env = new EnvRule();

    @Test
    public void test1() throws UnableToInstantiateObjectException, ErrorWhileSavingException, ParameterDoesNotExistException, IncorrectTypeException, ResourceDoesNotExistException, InconsistentParameterException {
        Parameters parms = new Parameters();
        parms.put( "title", "My title" );
        parms.setUser( SeventyEight.getInstance().getAnonymousUser() );

        Group group = (Group) SeventyEight.getInstance().getDescriptor( Group.class ).newInstance( env.getDB() );
        group.save( parms,  null );

        try {
            List<Edge> edges = group.getNode().getEdges( null, Direction.OUTBOUND );
            for( Edge e : edges ) {
                System.out.println(e );
            }
        } catch ( Exception e ) {
            System.out.println( "FAILED " + e.getMessage() );
            e.printStackTrace();
        }


        //parms.put( "title", "My next title" );
        //group.save( parms,  null );
    }
}
