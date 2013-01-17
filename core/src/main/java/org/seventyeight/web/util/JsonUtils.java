package org.seventyeight.web.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.seventyeight.web.exceptions.NoSuchJsonElementException;
import org.seventyeight.web.model.Request;

/**
 * @author cwolfgang
 *         Date: 17-01-13
 *         Time: 14:24
 */
public class JsonUtils {
    private JsonUtils() {

    }

    public static JsonObject getJsonFromRequest( Request request ) throws NoSuchJsonElementException {
        String json = request.getParameter( "json" );
        if( json == null ) {
            throw new NoSuchJsonElementException( "Null" );
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println( gson.toJson( json ) );

        JsonParser parser = new JsonParser();
        JsonObject jo = (JsonObject) parser.parse( json );
        return jo;
    }
}
