
package org.fcrepo.exceptionhandlers;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static javax.ws.rs.core.Response.serverError;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Named
@Provider
public class WildcardExceptionMapper implements ExceptionMapper<Exception> {

    Boolean showStackTrace;

    @Override
    public Response toResponse(Exception e) {
        
        return serverError().entity(
                showStackTrace ? getStackTraceAsString(e) : null).build();
    }

    public void setShowStackTrace(Boolean showStackTrace) {
        this.showStackTrace = showStackTrace;
    }
}