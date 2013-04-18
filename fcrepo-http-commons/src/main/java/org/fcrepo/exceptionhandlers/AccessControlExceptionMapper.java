
package org.fcrepo.exceptionhandlers;

import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import javax.jcr.security.AccessControlException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AccessControlExceptionMapper implements
        ExceptionMapper<AccessControlException> {

    @Override
    public Response toResponse(final AccessControlException arg0) {
        return status(FORBIDDEN).build();
    }

}
