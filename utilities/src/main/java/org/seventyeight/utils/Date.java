package org.seventyeight.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Date extends java.util.Date {

	private static final SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
	public Date() {
		super();
	}
	
	public Date( long milli ) {
		super( milli );
	}

	public static Date dateFromString( String d ) {
		Date date = new Date();
		try {
			date = (Date) format.parse( d );
		} catch( ParseException e ) {
			/* date is now! */
		}

		return date;
	}
	
	public String toString() {
		return format.format( this );
	}
}
