package org.seventyeight.web.model;

import org.seventyeight.database.Database;
import org.seventyeight.database.Edge;
import org.seventyeight.database.Node;
import org.seventyeight.web.exceptions.ErrorWhileSavingException;
import org.seventyeight.web.exceptions.InconsistentParameterException;
import org.seventyeight.web.exceptions.IncorrectTypeException;
import org.seventyeight.web.exceptions.ParameterDoesNotExistException;
import org.seventyeight.web.exceptions.ResourceDoesNotExistException;

import com.google.gson.JsonObject;


public interface Savable<NODE extends Node<NODE, EDGE>, EDGE extends Edge<EDGE, NODE>> {
	public NODE getNode();
	public void save( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException;
}
