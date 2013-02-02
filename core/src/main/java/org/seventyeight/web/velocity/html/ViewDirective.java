package org.seventyeight.web.velocity.html;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.UnknownResourceIdentifierException;
import org.seventyeight.web.servlet.Request;

public class ViewDirective extends Directive {

	private Logger logger = Logger.getLogger( ViewDirective.class );
	
	@Override
	public String getName() {
		return "view";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render( InternalContextAdapter context, Writer writer, Node node ) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

		Object obj = null;
		
		try {
			if( node.jjtGetChild( 0 ) != null ) {
				obj = node.jjtGetChild( 0 ).value( context );
			} else {
				throw new UnknownResourceIdentifierException( "Not a resource identifier" );
			}
			
			Request request = (Request) context.get( "request" );
            writer.write( SeventyEight.getInstance().getTemplateManager().getRenderer( request ).renderObject( obj, "index.vm" ) );
			
		} catch( Exception e ) {
            writer.write( e.getMessage() );
            logger.warn( e );
		}
		
		

		return true;
	}

}
