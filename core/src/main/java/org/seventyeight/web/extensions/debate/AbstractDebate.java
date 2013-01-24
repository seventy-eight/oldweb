package org.seventyeight.web.extensions.debate;

import com.google.gson.JsonObject;
import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.database.utils.SortingUtils;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.*;
import org.seventyeight.web.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cwolfgang
 *         Date: 20-12-12
 *         Time: 11:35
 */
public abstract class AbstractDebate extends AbstractExtension implements DebateInterface {

    private static Logger logger = Logger.getLogger( AbstractDebate.class );
    public static final String INDEX_REPLIES = "debate-replies";

    public AbstractDebate( Node node ) {
        super( node );
    }

    @Override
    public Class<?> getExtensionClass() {
        return DebateInterface.class;
    }

    public abstract Descriptor<?> getReplyDescriptor();

    public static List<Descriptor> all() {
        return SeventyEight.getInstance().getExtensionDescriptors( DebateInterface.class );
    }

    @Override
    public String getDisplayName() {
        return "Debate";
    }

    @Override
    public void save( CoreRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
        //node.set( "type", Debatable.HUB_DEBATE );
        super.save( request, json );

        //node.save();
    }

    public static abstract class DebateImplDescriptor extends ExtensionDescriptor<AbstractDebate> {

        @Override
        public void configureIndex( Database db ) {
            logger.debug( "Configuring " + INDEX_REPLIES );
            // identifier, time
            db.createIndex( INDEX_REPLIES, IndexType.UNIQUE, IndexValueType.LONG, IndexValueType.LONG );
        }


    }
}
