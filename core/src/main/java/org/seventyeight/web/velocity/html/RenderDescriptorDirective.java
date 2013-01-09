package org.seventyeight.web.velocity.html;

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.model.*;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class RenderDescriptorDirective extends Directive {

	private Logger logger = Logger.getLogger( RenderDescriptorDirective.class );
	
	@Override
	public String getName() {
		return "renderDescriptor";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render( InternalContextAdapter context, Writer writer, Node node ) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        logger.debug( "Rendering descriptor" );
		Descriptor d = null;
        AbstractItem item = null;
        Boolean expanded = false;
		
		try {
			if( node.jjtGetChild( 0 ) != null ) {
				d = (Descriptor) node.jjtGetChild( 0 ).value( context );
			} else {
				throw new IOException( "Argument not a descriptor" );
			}

            if( node.jjtGetChild( 1 ) != null ) {
                item = (AbstractItem) node.jjtGetChild( 1 ).value( context );
            } else {
                throw new IOException( "Argument not an item" );
            }

            if( node.jjtGetChild( 2 ) != null ) {
                expanded = (Boolean)node.jjtGetChild( 2 ).value( context );
            } else {
                throw new IOException( "Argument not boolean" );
            }

		} catch( Exception e ) {
            logger.debug( e );
		}

        Request request = (Request) context.get( "request" );

        /* Already configured descriptor */
        if( item != null ) {
            logger.fatal( "ITEM IS " + item );
            /* get the extension node */
            List<org.seventyeight.database.Node> nodes = item.getExtensionClassNodes( d.getClazz() );

            logger.debug( nodes );

            for( org.seventyeight.database.Node n : nodes ) {
                try {
                    writer.write( d.getConfigurationPage( request, n ) );
                } catch( TemplateDoesNotExistException e ) {
                    logger.warn( e );
                    writer.write( e.getMessage() );
                }
            }
        } else {
            logger.fatal( "ITEM IS NULL! " + context.get( "item" ) );
            logger.fatal( "EXPANDEND: " + expanded );
            try {
                writer.write( d.getConfigurationPage( request, null, expanded ) );
            } catch( TemplateDoesNotExistException e ) {
                logger.warn( e );
                writer.write( e.getMessage() );
            }
        }

        return true;
	}

}
