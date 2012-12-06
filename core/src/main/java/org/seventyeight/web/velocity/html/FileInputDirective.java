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
import org.seventyeight.web.velocity.html.AbstractInputDirective;

public class FileInputDirective extends AbstractInputDirective {

	private Logger logger = Logger.getLogger( FileInputDirective.class );
	
	@Override
	public String getName() {
		return "inputfile";
	}

	@Override
	public int getType() {
		return LINE;
	}


	@Override
	protected boolean input( Writer writer, String name, String value, int maxLength, boolean readonly, String onclick ) throws IOException {
		writer.write( "<input type=\"file\" name=\"" + name + "\" value=\"" + value + "\" " + ( maxLength > 0 ? "maxlength=\"" + maxLength + "\"" : "" ) + " onclick=\"" + onclick + "\"" + ( readonly ? " readonly" : "" ) + ">" );
		return true;
	}

}
