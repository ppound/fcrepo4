package org.fcrepo.api.legacy;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Named
@Provider
public class RestEasyExceptionMapper implements ExceptionMapper<Exception>{
	@Override
	public Response toResponse(Exception exception) {
		return Response.status(404).build();
	}

}
