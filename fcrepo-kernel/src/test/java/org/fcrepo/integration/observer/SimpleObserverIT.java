
package org.fcrepo.integration.observer;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.fcrepo.integration.AbstractIT;
import org.fcrepo.observer.FedoraEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@ContextConfiguration({"/spring-test/eventing.xml", "/spring-test/repo.xml"})
public class SimpleObserverIT extends AbstractIT {

    private Integer eventBusMessageCount;

    @Inject
    private Repository repository;

    @Inject
    private EventBus eventBus;

    @Test
    public void TestEventBusPublishing() throws RepositoryException {

        final Session se = repository.login();
        se.getRootNode().addNode("/object1").addMixin("fedora:object");
        se.getRootNode().addNode("/object2").addMixin("fedora:object");
        se.save();
        se.logout();

        try {
            Thread.sleep(500);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        // Should be two messages, for each time
        // each node becomes a Fedora object

        assertEquals("Where are my messages!?", (Integer) 2,
                eventBusMessageCount);

    }

    @Subscribe
    public void countMessages(final FedoraEvent e) {
        eventBusMessageCount++;
    }

    @Before
    public void acquireConnections() {
        eventBusMessageCount = 0;
        eventBus.register(this);
    }

    @After
    public void releaseConnections() {
        eventBus.unregister(this);
    }
}
