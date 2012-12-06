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

public abstract class AbstractInputDirective extends Directive {

	private Logger logger = Logger.getLogger( AbstractInputDirective.class );
	
	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render( InternalContextAdapter context, Writer writer, Node node ) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

		String name = "";
		String value = "";
		int maxLength = 0;
		boolean readonly = false;
		String onClick = "";
		
		try {
			if( node.jjtGetChild( 0 ) != null ) {
				//logger.debug( "NODE[0]=" + node.jjtGetChild( 0 ) + "/" + node.jjtGetChild( 0 ).value( context ) );
				name = String.valueOf( node.jjtGetChild( 0 ).value( context ) );
			} else {
				throw new IOException( "The name is mandatory" );
			}
			
			if( node.jjtGetChild( 1 ) != null ) {
				//logger.debug( "NODE[0]=" + node.jjtGetChild( 0 ) + "/" + node.jjtGetChild( 0 ).value( context ) );
				value = String.valueOf( node.jjtGetChild( 1 ).value( context ) );
			}
	
			if( node.jjtGetChild( 2 ) != null ) {
				//logger.debug( "NODE[2]=" + node.jjtGetChild( 2 ) + "/" + node.jjtGetChild( 2 ).value( context ) );
				maxLength = (Integer) node.jjtGetChild( 2 ).value( context );
			}
			
			if( node.jjtGetChild( 3 ) != null ) {
				//logger.debug( "NODE[1]=" + node.jjtGetChild( 1 ) + "/" + node.jjtGetChild( 1 ).value( context ) );
				readonly = (Boolean) node.jjtGetChild( 3 ).value( context );
			}
	
			if( node.jjtGetChild( 4 ) != null ) {
				//logger.debug( "NODE[3]=" + node.jjtGetChild( 3 ) + "/" + node.jjtGetChild( 3 ).value( context ) );
				onClick = String.valueOf( node.jjtGetChild( 4 ).value( context ) );
			}
		} catch( Exception e ) {
			if( name.length() == 0 ) {
				throw new IOException( "The name is mandatory" );
			}
			/* ... And we're done */
		}
		
		return input( writer, name, value, maxLength, readonly, onClick );
	}
	
	protected abstract boolean input( Writer writer, String name, String value, int maxLength, boolean readonly, String onclick ) throws IOException;

}
