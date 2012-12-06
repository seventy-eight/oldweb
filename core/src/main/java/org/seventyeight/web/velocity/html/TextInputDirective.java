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

public class TextInputDirective extends Directive {

	private Logger logger = Logger.getLogger( TextInputDirective.class );
	
	@Override
	public String getName() {
		return "inputtext";
	}

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
		boolean area = false;
		
		logger.debug( "LINE: " + node.getLine() );
		
		try {
			if( node.jjtGetChild( 0 ) != null ) {
				name = String.valueOf( node.jjtGetChild( 0 ).value( context ) );
			} else {
				throw new IOException( "The name is mandatory" );
			}
			
			if( node.jjtGetChild( 1 ) != null ) {
				value = String.valueOf( node.jjtGetChild( 1 ).value( context ) );
			}
	
			if( node.jjtGetChild( 2 ) != null ) {
				maxLength = (Integer) node.jjtGetChild( 2 ).value( context );
			}
			
			if( node.jjtGetChild( 3 ) != null ) {
				logger.debug( "NODE[3]=" + node.jjtGetChild( 3 ) + "/" + node.jjtGetChild( 3 ).value( context ) );
				area = (Boolean) node.jjtGetChild( 3 ).value( context );
			}
			
			if( node.jjtGetChild( 4 ) != null ) {
				readonly = (Boolean) node.jjtGetChild( 4 ).value( context );
			}
	
			if( node.jjtGetChild( 5 ) != null ) {
				onClick = String.valueOf( node.jjtGetChild( 5 ).value( context ) );
			}
		} catch( Exception e ) {
			if( name.length() == 0 ) {
				throw new IOException( "The name is mandatory" );
			}
			/* ... And we're done */
		}
		
		if( area ) {
			writer.write( "<textarea name=\"" + name + "\" onclick=\"" + onClick + "\">" + value + "</textarea>" );
		} else {
			writer.write( "<input type=\"text\" name=\"" + name + "\" value=\"" + value + "\" " + ( maxLength > 0 ? "maxlength=\"" + maxLength + "\"" : "" ) + " onclick=\"" + onClick + "\"" + ( readonly ? " readonly" : "" ) + ">" );
		}

		return true;
	}

}
