package org.seventyeight.web.util;

import org.seventyeight.database.Database;
import org.seventyeight.web.SeventyEight;
import org.seventyeight.web.exceptions.UnableToInstantiateObjectException;
import org.seventyeight.web.model.resources.Group;
import org.seventyeight.web.model.resources.User;

/**
 * @author cwolfgang
 *         Date: 01-12-12
 *         Time: 22:29
 */
public class Installer {
    public Database db;
    private SeventyEight se;

    public Installer( Database db ) {
        this.db = db;
        this.se = SeventyEight.getInstance();
    }

    public void install() {
    }

    public  User installUser() throws UnableToInstantiateObjectException {
        User user = (User) se.getDescriptorFromResourceType( "user" ).newInstance( db );

        return user;
    }

    public Group installGroup() throws UnableToInstantiateObjectException {
        Group group = (Group) se.getDescriptorFromResourceType( "group" ).newInstance( db );

        return group;
    }
}
