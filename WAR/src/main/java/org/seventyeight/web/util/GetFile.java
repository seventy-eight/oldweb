package org.seventyeight.web.util;

import org.seventyeight.web.servlet.Response;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface GetFile {
	public File getFile( HttpServletRequest request, Response response ) throws IOException;
}
