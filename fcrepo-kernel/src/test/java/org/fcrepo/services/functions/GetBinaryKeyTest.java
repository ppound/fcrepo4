
package org.fcrepo.services.functions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.modeshape.jcr.api.JcrConstants.JCR_CONTENT;
import static org.modeshape.jcr.api.JcrConstants.JCR_DATA;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.junit.Test;
import org.modeshape.jcr.value.BinaryValue;

public class GetBinaryKeyTest {

    @Test
    public void testApply() throws LoginException, RepositoryException {
        final Node mockNode = mock(Node.class);
        final Node mockContent = mock(Node.class);
        final Property mockProp = mock(Property.class);
        final BinaryValue mockBin = mock(BinaryValue.class);
        when(mockProp.getBinary()).thenReturn(mockBin);
        when(mockContent.getProperty(JCR_DATA)).thenReturn(mockProp);
        when(mockNode.getNode(JCR_CONTENT)).thenReturn(mockContent);
        final GetBinaryKey testObj = new GetBinaryKey();
        testObj.apply(mockNode);
    }

}
