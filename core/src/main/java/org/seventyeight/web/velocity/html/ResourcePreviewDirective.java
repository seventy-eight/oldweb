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
import org.seventyeight.database.Database;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.UnknownResourceIdentifierException;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.AbstractTheme;
import org.seventyeight.web.model.Request;

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

        /* Get the database in context */
        Database db = (Database) context.get( "database" );

		AbstractResource r = null;
		logger.debug( "HERE0" );
		try {
			if( node.jjtGetChild( 0 ) != null ) {
				r = (AbstractResource) node.jjtGetChild( 0 ).value( context );
			} else {
				throw new UnknownResourceIdentifierException( "Not a resource identifier" );
			}

            Request request = (Request) context.get( "request" );
            SeventyEight.getInstance().getTemplateManager().getRenderer( request ).setWriter( writer ).renderObject( r, "preview.vm" );
			
		} catch( Exception e ) {
			logger.debug( e );
			writer.write( "???" );
		}
		
		

		return true;
	}

}
