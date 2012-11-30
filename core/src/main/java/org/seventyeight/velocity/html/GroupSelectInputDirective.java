package org.seventyeight.velocity.html;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.seventyeight.database.Database;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.model.AbstractResource;
import org.seventyeight.web.model.resources.Group;

public class GroupSelectInputDirective extends Directive {

	private Logger logger = Logger.getLogger( GroupSelectInputDirective.class );
	
	@Override
	public String getName() {
		return "groupsforresource";
	}

	@Override
	public int getType() {
		return LINE;
	}

	@Override
	public boolean render( InternalContextAdapter context, Writer writer, Node node ) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

		String name = "";
		String namedRelation = "";
		long id = -1;

        /* Get the database in context */
        Database db = (Database) context.get( "database" );
		
		try {
			try {
				if( node.jjtGetChild( 0 ) != null ) {
					id = (Long) node.jjtGetChild( 0 ).value( context );
					//logger.debug( "Resource id: " + id );
				} else {
					//logger.debug( "Id is not set" );
				}
			} catch( Exception e) {
				//logger.debug( "Id will not be set" );
			}
			
			if( node.jjtGetChild( 1 ) != null ) {
				name = String.valueOf( node.jjtGetChild( 1 ).value( context ) );
			} else {
				throw new IOException( "The name is mandatory" );
			}
			
			if( node.jjtGetChild( 2 ) != null ) {
				namedRelation = String.valueOf( node.jjtGetChild( 2 ).value( context ) );
			} else {
				throw new IOException( "The group relation is mandatory" );
			}

			
		} catch( Exception e ) {
			
			if( name.length() == 0 ) {
				throw new IOException( "The name is mandatory" );
			}
			
			if( namedRelation.length() == 0 ) {
				throw new IOException( "The group relation is mandatory" );
			}

			/* ... And we're done */
		}
		
		//logger.debug( "---- " + id + " ----" );
		
		List<Group> selected = null;
		if( id > 0 ) {
			AbstractResource r = null;
			try {
				r = SeventyEight.getInstance().getResource( db, id );
			} catch( Exception e ) {
				logger.error( "Unable to load resource " + id + ": " + e.getMessage() );
			}
			
			//List<Group> selected = Group.getAllGroups( r.getGroups( GroupRelation.GROUP_HAS_ACCESS ) );
			selected = Group.getAllGroups( db, r.getGroups( namedRelation ) );
		} else {
			selected = Group.getAllGroups( db );
		}
		
		writer.write( "<select name=\"" + name + "\" multiple>" );
		
		for( Group g : selected ) {
			if( g.isSelected() ) {
				writer.write( "<option value=\"" + g.getIdentifier() + "\" selected>" + g + "</option>\n" );
			} else {
				writer.write( "<option value=\"" + g.getIdentifier() + "\">" + g + "</option>\n" );
			}
		}
		
		writer.write( "</select>\n" );

		return true;
	}

}
