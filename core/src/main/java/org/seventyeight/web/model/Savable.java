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


public interface Savable {
	public Node<?, ?> getNode();
	public void save( ParameterRequest request, JsonObject jsonData ) throws ParameterDoesNotExistException, ResourceDoesNotExistException, IncorrectTypeException, InconsistentParameterException, ErrorWhileSavingException;
}
