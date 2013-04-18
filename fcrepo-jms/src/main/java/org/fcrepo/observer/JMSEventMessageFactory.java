
package org.fcrepo.observer;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jms.JMSException;
import javax.jms.Message;

public interface JMSEventMessageFactory {

    public Message getMessage(final Event jcrEvent,
            final javax.jcr.Session jcrSession,
            final javax.jms.Session jmsSession) throws RepositoryException,
            IOException, JMSException;
}
