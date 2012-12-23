package org.seventyeight.database.orientdb.impl.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.log4j.Logger;
import org.seventyeight.database.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 *
 * Taken from OrientDB man pages:<br />
 * <pre>
 * +--------------+                                       +--------------+
 * |              | out       * +------------+ in         |              |
 * |              |------------>|            |----------->|              |
 * |   V(ertex)   |             |   E(dge)   |            |   V(ertex)   |
 * |              |<------------|            |<-----------|              |
 * |              |         out +------------+         in |              |
 * +--------------+                                       +--------------+
 * </pre>
 * User: cwolfgang
 * Date: 18-11-12
 * Time: 22:28
 */
public class OrientNode implements Node {

    private static Logger logger = Logger.getLogger( OrientNode.class );

    private OrientDatabase db;
    private ODocument doc;

    public OrientNode( OrientDatabase db ) {
        this.db = db;
        this.doc = db.getInternalDatabase().createVertex();
    }

    public OrientNode( OrientDatabase db, ODocument doc ) {
        this.db = db;
        this.doc = doc;
    }

    public ODocument getDocument() {
        return doc;
    }

    @Override
    public OrientDatabase getDB() {
        return db;
    }

    @Override
    public OrientEdge createEdge( Node to, EdgeType type ) {
        logger.debug( "Creating edge(" + type + ") from " + doc.getClassName() + " to " + ((OrientNode)to).getDocument().getClassName() );

        ODocument edge = db.getInternalDatabase().createEdge( doc, ((OrientNode)to).doc ).field( OGraphDatabase.LABEL, type.toString() ).save();

        return new OrientEdge( edge, this, (OrientNode)to );
    }

    @Override
    public List<OrientEdge> getEdges( EdgeType type, Direction direction ) {
        logger.debug( "Getting " + direction + " edges for " + this + " of type " + type );

        Set<OIdentifiable> in = null;
        Set<OIdentifiable> out = null;

        switch( direction ) {
            case OUTBOUND:
                out = db.getInternalDatabase().getOutEdges( doc, ( type != null ? type.toString() : null ) );
                break;

            case INBOUND:
                in = db.getInternalDatabase().getInEdges( doc, ( type != null ? type.toString() : null ) );
                break;

            default:
                out = db.getInternalDatabase().getOutEdges( doc, ( type != null ? type.toString() : null ) );
                in = db.getInternalDatabase().getInEdges( doc, ( type != null ? type.toString() : null ) );
        }

        List<OrientEdge> es = new LinkedList<OrientEdge>();
        if( in != null ) {
            for( OIdentifiable e : in ) {
                ODocument other = db.getInternalDatabase().getOutVertex( e );

                OrientEdge edge = new OrientEdge( e, new OrientNode( db, other ), this );
                es.add( edge );
            }
        }

        if( out != null ) {
            for( OIdentifiable e : out ) {
                ODocument other = db.getInternalDatabase().getInVertex( e );

                OrientEdge edge = new OrientEdge( e, this, new OrientNode( db, other ) );
                es.add( edge );
            }
        }

        return es;
    }



    @Override
    public List<OrientEdge> getEdgesTo( Node to, EdgeType type ) {
        logger.debug( "Getting edges from " + this + " to " + to + " of type " + type );

        Set<OIdentifiable> ois = db.getInternalDatabase().getEdgesBetweenVertexes( doc, ((OrientNode)to).doc, ( type != null ? new String[]{ type.toString() } : null ) );

        logger.debug( "EDGES: " + ois );
        List<OrientEdge> es = new LinkedList<OrientEdge>();

        for( OIdentifiable e : ois ) {
            if( db.getInternalDatabase().getOutVertex( e ).equals( doc )) {
                OrientEdge edge = new OrientEdge( e, this, (OrientNode)to );
                es.add( edge );
                logger.debug( "Edge2: " + edge );
            }
        }

        return es;
    }

    @Override
    public List<Edge> getEdges( Node other, EdgeType type, Direction direction ) {
        logger.debug( "Getting edges between " + this + " and " + other + " of type " + type + " " + direction );

        Set<OIdentifiable> ois = db.getInternalDatabase().getEdgesBetweenVertexes( doc, ((OrientNode)other).doc, ( type != null ? new String[]{ type.toString() } : null ) );

        List<Edge> es = new LinkedList<Edge>();
        System.out.println( "ES: " + ois );

        for( OIdentifiable e : ois ) {
            OrientEdge edge = null;
            switch( direction ) {
                case OUTBOUND:
                    if( db.getInternalDatabase().getOutVertex( e ).equals( doc )) {
                        edge = new OrientEdge( e, this, (OrientNode)other );
                        es.add( edge );
                    }
                    break;

                case INBOUND:
                    if( db.getInternalDatabase().getInVertex( e ).equals( doc )) {
                        edge = new OrientEdge( e, this, (OrientNode)other );
                        es.add( edge );
                    }
                    break;

                default:
                    edge = new OrientEdge( e, this, (OrientNode)other );
                    es.add( edge );
            }
        }

        return es;
    }

    @Override
    public <T> T get( String key ) {
        return doc.field( key );
    }

    @Override
    public <T> T get( String key, T defaultValue ) {
        if( doc.containsField( key ) ) {
            return doc.field( key );
        } else {
            return defaultValue;
        }
    }

    @Override
    public <T> OrientNode set( String key, T value ) {
        doc.field( key, value );

        return this;
    }

    @Override
    public OrientNode save() {
        doc.save();

        return this;
    }

    @Override
    public Map<String, Object> getFieldData() {
        Map<String, Object> data = new HashMap<String, Object>();
        for( String field : doc.fieldNames() ) {
            data.put( field, doc.field( field ) );
        }

        return data;
    }

    @Override
    public OrientNode removeEdges( EdgeType type, Direction direction ) {
        logger.debug( "Removing " + direction + " edges from " + this + " of type " + type );

        switch( direction ) {
            case OUTBOUND:
                Set<OIdentifiable> out = db.getInternalDatabase().getOutEdges( doc, ( type != null ? type.toString() : null ) );
                for( OIdentifiable oi : out ) {
                    db.getInternalDatabase().removeEdge( oi );
                }
                break;

            case INBOUND:
                Set<OIdentifiable> in = db.getInternalDatabase().getInEdges( doc, ( type != null ? type.toString() : null ) );
                for( OIdentifiable oi : in ) {
                    db.getInternalDatabase().removeEdge( oi );
                }
                break;

            default:
                Set<OIdentifiable> ois1 = db.getInternalDatabase().getOutEdges( doc, ( type != null ? type.toString() : null ) );
                Set<OIdentifiable> ois2 = db.getInternalDatabase().getInEdges( doc, ( type != null ? type.toString() : null ) );
                for( OIdentifiable oi : ois1 ) {
                    db.getInternalDatabase().removeEdge( oi );
                }
                for( OIdentifiable oi : ois2 ) {
                    db.getInternalDatabase().removeEdge( oi );
                }
        }

        return this;
    }

    @Override
    public boolean equals( Object obj ) {
        if( obj == this ) {
            return true;
        }

        if( obj instanceof OrientNode ) {
            return ((OrientNode)obj).doc.equals( doc );
        }

        return false;
    }

    @Override
    public void remove() {
        doc.remove();
    }

    @Override
    public String toString() {
        return "OrientNode[" + doc + "]";
    }

    @Override
    public String getId( boolean safe ) {
        if( safe ) {
            try {
                return URLEncoder.encode( doc.getIdentity().toString(), "UTF-8" );
            } catch( UnsupportedEncodingException e ) {
                logger.warn( "Unable to encode, NOT encoding" );
                logger.warn( e );
                return doc.getIdentity().toString();
            }
        } else {
            return doc.getIdentity().toString();
        }
    }
}
