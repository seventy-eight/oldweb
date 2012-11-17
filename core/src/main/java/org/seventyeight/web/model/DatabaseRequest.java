package org.seventyeight.web.model;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;

/**
 * User: cwolfgang
 * Date: 16-11-12
 * Time: 21:42
 */
public interface DatabaseRequest {
    public OGraphDatabase getDB();
}
