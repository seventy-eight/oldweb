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

public class ResourceSelectorDirective extends Directive {

	private Logger logger = Logger.getLogger( ResourceSelectorDirective.class );
	
	@Override
	public String getName() {
		return "resourceSelector";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render( InternalContextAdapter context, Writer writer, Node node ) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

		String resourceType = "decal";
		String name = "id";
		String url = "/resources/list/";
		String formName = "configure";
		
		try {
			if( node.jjtGetChild( 0 ) != null ) {
				resourceType = String.valueOf( node.jjtGetChild( 0 ).value( context ) );
			}
			
			if( node.jjtGetChild( 1 ) != null ) {
				name = String.valueOf( node.jjtGetChild( 1 ).value( context ) );
			}
			
			if( node.jjtGetChild( 2 ) != null ) {
				url = String.valueOf( node.jjtGetChild( 2 ).value( context ) );
			}
			
			if( node.jjtGetChild( 3 ) != null ) {
				formName = String.valueOf( node.jjtGetChild( 3 ).value( context ) );
			}
			
		} catch( Exception e ) {
			/* ... And we're done */
		}
		
		writer.write( "<input name=\"" + name + "\" onclick=\"Utils.popupselect('" + url + resourceType + "?formName=" + formName + "&amp;formElement=" + resourceType + "Id', '500px');\" value=\"\">" );

		return true;
	}

}
