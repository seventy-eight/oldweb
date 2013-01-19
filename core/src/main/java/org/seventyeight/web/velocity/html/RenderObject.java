package org.seventyeight.web.velocity.html;

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.model.AbstractItem;
import org.seventyeight.web.model.Descriptor;
import org.seventyeight.web.model.Request;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class RenderObject extends Directive {

	private Logger logger = Logger.getLogger( RenderObject.class );
	
	@Override
	public String getName() {
		return "render";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render( InternalContextAdapter context, Writer writer, Node node ) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        logger.debug( "Rendering descriptor" );
		Object obj = null;
        String template = null;
        String container = null;

		try {
			if( node.jjtGetChild( 0 ) != null ) {
				obj = (Object) node.jjtGetChild( 0 ).value( context );
			} else {
				throw new IOException( "First argument is not an object" );
			}

            if( node.jjtGetChild( 1 ) != null ) {
                template = (String) node.jjtGetChild( 1 ).value( context );
            } else {
                throw new IOException( "Second argument is not a string" );
            }

            if( node.jjtGetChild( 1 ) != null ) {
                container = (String) node.jjtGetChild( 1 ).value( context );
            } else {
                throw new IOException( "Third argument is not a string" );
            }

		} catch( Exception e ) {
            logger.debug( e );
		}

        Request request = (Request) context.get( "request" );

        if( template == null ) {
            return false;
        } else {
            if( container == null ) {
                try {
                    writer.write( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( obj, template + ".vm" ) );
                } catch( TemplateDoesNotExistException e ) {
                    e.printStackTrace();
                }
            } else {
                try {
                    request.getContext().put( "content", SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( obj, template + ".vm" ) );
                    writer.write( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).render( container + ".vm" ) );
                } catch( TemplateDoesNotExistException e ) {
                    e.printStackTrace();
                }
            }
        }

        return true;
	}

}
