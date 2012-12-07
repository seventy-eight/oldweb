package org.seventyeight.web.util;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface GetFile {
	public File getFile( HttpServletRequest request, HttpServletResponse response ) throws IOException;
}
