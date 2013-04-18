
package org.fcrepo.services;

import static org.fcrepo.services.PathService.getDatastreamJcrNodePath;
import static org.fcrepo.services.PathService.getObjectJcrNodePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.fcrepo.Datastream;
import org.fcrepo.FedoraObject;
import org.fcrepo.utils.DatastreamIterator;
import org.fcrepo.utils.FedoraJcrTypes;
import org.fcrepo.utils.FedoraTypesUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DatastreamService.class, FedoraTypesUtils.class,
        ServiceHelpers.class})
public class DatastreamServiceTest implements FedoraJcrTypes {

    private static final String MOCK_CONTENT_TYPE = "application/test-data";

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateDatastreamNode() throws Exception {
        final Node mockNode = mock(Node.class);
        final Session mockSession = mock(Session.class);
        final InputStream mockIS = mock(InputStream.class);
        final String testPath = getDatastreamJcrNodePath("foo", "bar");
        final Datastream mockWrapper = mock(Datastream.class);
        when(mockWrapper.getNode()).thenReturn(mockNode);
        whenNew(Datastream.class).withArguments(mockSession, testPath)
                .thenReturn(mockWrapper);
        final DatastreamService testObj = new DatastreamService();
        final Node actual =
                testObj.createDatastreamNode(mockSession, testPath,
                        MOCK_CONTENT_TYPE, mockIS);
        assertEquals(mockNode, actual);
        verifyNew(Datastream.class).withArguments(mockSession, testPath);
        verify(mockWrapper).setContent(eq(mockIS), any(String.class),
                any(String.class), any(String.class));
    }

    @Test
    public void testGetDatastreamNode() throws Exception {
        final Session mockSession = mock(Session.class);
        final Node mockNode = mock(Node.class);
        final Datastream mockWrapper = mock(Datastream.class);
        when(mockWrapper.getNode()).thenReturn(mockNode);
        whenNew(Datastream.class).withArguments(mockSession, "foo", "bar")
                .thenReturn(mockWrapper);
        final DatastreamService testObj = new DatastreamService();
        testObj.readOnlySession = mockSession;
        testObj.getDatastreamNode("foo", "bar");
        verifyNew(Datastream.class).withArguments(mockSession, "foo", "bar");
        verify(mockWrapper).getNode();
    }

    @Test
    public void testGetDatastream() throws Exception {
        final Session mockSession = mock(Session.class);
        final Node mockNode = mock(Node.class);
        final Datastream mockWrapper = mock(Datastream.class);
        when(mockWrapper.getNode()).thenReturn(mockNode);
        whenNew(Datastream.class).withArguments(mockSession, "foo", "bar")
                .thenReturn(mockWrapper);
        final DatastreamService testObj = new DatastreamService();
        testObj.readOnlySession = mockSession;
        testObj.getDatastream("foo", "bar");
        verifyNew(Datastream.class).withArguments(mockSession, "foo", "bar");
    }

    @Test
    public void testPurgeDatastream() throws Exception {
        final Session mockSession = mock(Session.class);
        final Node mockNode = mock(Node.class);
        final Datastream mockWrapper = mock(Datastream.class);
        when(mockWrapper.getNode()).thenReturn(mockNode);
        whenNew(Datastream.class).withArguments(mockSession, "foo", "bar")
                .thenReturn(mockWrapper);
        final DatastreamService testObj = new DatastreamService();
        testObj.purgeDatastream(mockSession, "foo", "bar");
        verifyNew(Datastream.class).withArguments(mockSession, "foo", "bar");
        verify(mockWrapper).purge();
    }

    @Test
    public void testGetDatastreamsFor() throws Exception {
        final Session mockSession = mock(Session.class);
        final Node mockObjNode = mock(Node.class);
        final FedoraObject mockObj = mock(FedoraObject.class);
        when(mockObj.getNode()).thenReturn(mockObjNode);
        whenNew(FedoraObject.class).withArguments(mockSession,
                getObjectJcrNodePath("foo")).thenReturn(mockObj);
        final DatastreamService testObj = new DatastreamService();
        testObj.readOnlySession = mockSession;
        final DatastreamIterator actual = testObj.getDatastreamsFor("foo");
        assertNotNull(actual);
        verifyNew(FedoraObject.class).withArguments(mockSession,
                getObjectJcrNodePath("foo"));
        verify(mockObjNode).getNodes();
    }

    @Test
    public void testExists() throws RepositoryException {
        final Session mockSession = mock(Session.class);
        final DatastreamService testObj = new DatastreamService();
        testObj.readOnlySession = mockSession;
        testObj.exists("foo", "bar");
        verify(mockSession).nodeExists(getDatastreamJcrNodePath("foo", "bar"));
    }
}
