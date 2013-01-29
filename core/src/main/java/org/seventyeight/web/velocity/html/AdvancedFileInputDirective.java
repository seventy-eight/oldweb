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

public class AdvancedFileInputDirective extends Directive {

	private Logger logger = Logger.getLogger( AdvancedFileInputDirective.class );
	
	@Override
	public String getName() {
		return "upload";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render( InternalContextAdapter context, Writer writer, Node node ) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

		String appendname = "";
		
		try {
			if( node.jjtGetChild( 0 ) != null ) {
				appendname = String.valueOf( node.jjtGetChild( 0 ).value( context ) );
			}
			
		} catch( Exception e ) {
			/* ... And we're done */
		}

		writer.write( "<div style=\"float: left\">" );
		writer.write( "<iframe id=\"uploadframe\" src=\"/static/fileupload.org.seventyeight.velocity.html.html\" style=\"display:inline;border-style:solid; width:250px;height:50px\"></iframe></div>" );
		writer.write( "<div style=\"float: left;border-style:solid;width:250px;height:25px\">" );
		writer.write( "<div style=\"background-color:#345676;width:0px;height:25px\" id=\"status\"></div></div>" );
		writer.write( "<input type=\"text\" id=\"nodeid\" name=\"nodeid\" value=\"\" readonly>" );
		//writer.writeToFile( "<input type=\"text\" name=\"nodeid\" value=\"\">" );

		return true;
	}

}
