package org.seventyeight.web.model;

import java.util.*;

import org.apache.log4j.Logger;
import org.seventyeight.database.*;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.SeventyEight.ResourceEdgeType;
import org.seventyeight.web.exceptions.*;

import com.google.gson.JsonObject;


public abstract class AbstractItem extends AbstractDatabaseItem implements Item {

	private static Logger logger = Logger.getLogger( AbstractItem.class );

	public AbstractItem( Node node ) {
		super( node );
	}
	
	public AbstractItem( Node node, Locale locale ) {
        super( node );
	}

    @Override
	public void save( ParameterRequest request, JsonObject json ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException {
		logger.debug( "Begin saving" );

        Save save = getSaver( request, json );
		
		save.before();
		save.save();
		save.after();
		save.updateIndexes();
		
		node.save();
		
		logger.debug( "End saving item" );
	}

    public Save getSaver( ParameterRequest request, JsonObject json ) {
        return new Save( this, request, json );
    }

	protected class Save {

		protected AbstractItem item;
		protected ParameterRequest request;
		protected JsonObject jsonData;

		public Save( AbstractItem type, ParameterRequest request, JsonObject jsonData ) {
			this.item = type;
			this.request = request;
			this.jsonData = jsonData;
		}
		
		public void before() {}
		
		public void save() throws InconsistentParameterException, ErrorWhileSavingException {
            /* Base implementation is a no op */
        }
		
		public void after() {}
		
		public void updateIndexes() {}
		
		public ParameterRequest getRequest() {
			return request;
		}
		
		public JsonObject getJsonData() {
			return jsonData;
		}
	}
	
	public Object getProperty( String name ) {
		return node.get( name );
	}
	
	public void setProperty( String name, Object property ) {
		logger.debug( "Setting " + name + " to " + property );
		node.set( name, property );
	}
			
	public Long getIdentifier() {
		return (Long) node.get( "identifier" );
	}

	public void setIdentifier( Long identifier ) {
		logger.debug( "Setting id to " + identifier );
		node.set( "identifier", identifier );
	}
	
	public void updateIndexes() {
		/**/
	}


    /**
     * This used for preprocessing items before viewing.
     */
    public void prepareView( ParameterRequest request ) {
        /* Default implementation is no op */
    }

    public AbstractDataItem getDataItem( Class<? extends AbstractDataItem> dataClass ) throws CouldNotLoadObjectException {
        List<Edge> edges = node.getEdges( ResourceEdgeType.data, Direction.OUTBOUND, dataClass.getName() );

        if( edges.size() == 0 ) {
            return null;
        } else {
            return (AbstractDataItem) SeventyEight.getInstance().getDatabaseItem( edges.get( 0 ).getTargetNode() );
        }
    }
}
