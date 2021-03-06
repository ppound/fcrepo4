
package org.fcrepo.session;

import static org.slf4j.LoggerFactory.getLogger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.modeshape.jcr.api.ServletCredentials;
import org.slf4j.Logger;

public class SessionFactory {

    private static final Logger logger = getLogger(SessionFactory.class);

    @Inject
    private Repository repo;

    public SessionFactory() {

    }

    @PostConstruct
    public void init() {
        if (repo == null) {
            logger.error("SessionFactory requires a Repository instance!");
            throw new IllegalStateException();
        }
    }

    public void setRepository(final Repository repo) {
        this.repo = repo;
    }

    public Session getSession() throws RepositoryException {
        return repo.login();
    }

    public Session getSession(final SecurityContext securityContext,
            final HttpServletRequest servletRequest) {
        Session session = null;

        try {
            if (securityContext.getUserPrincipal() != null) {
                logger.debug("Authenticated user: " +
                        securityContext.getUserPrincipal().getName());
                final ServletCredentials credentials =
                        new ServletCredentials(servletRequest);

                session = repo.login(credentials);
            } else {
                logger.debug("No authenticated user found!");
                session = repo.login();
            }
        } catch (final RepositoryException e) {
            throw new IllegalStateException(e);
        }
        return session;
    }
}
