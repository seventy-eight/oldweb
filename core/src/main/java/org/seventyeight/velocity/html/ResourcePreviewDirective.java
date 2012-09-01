package org.seventyeight.velocity.html;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

public class ResourcePreviewDirective extends Directive {

	private Logger logger = Logger.getLogger( ResourcePreviewDirective.class );
	
	@Override
	public String getName() {
		return "preview";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render( InternalContextAdapter context, Writer writer, Node node ) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

		AbstractResource r = null;
		logger.debug( "HERE0" );
		try {
			if( node.jjtGetChild( 0 ) != null ) {
				r = (AbstractResource) node.jjtGetChild( 0 ).value( context );
			} else {
				throw new UnknownResourceIdentifierException( "Not a resource identifier" );
			}
			
			RequestContext request = (RequestContext)context.get( "request" );
			AbstractTheme theme = request.getTheme();
			
			GraphDragon.getInstance().renderObject( writer, r, "preview.vm", theme, request.getContext() );
			
		} catch( Exception e ) {
			logger.debug( e );
			writer.write( "???" );
		}
		
		

		return true;
	}

}
