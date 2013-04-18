
package org.fcrepo;

import static com.google.common.base.Joiner.on;
import static com.yammer.metrics.MetricRegistry.name;
import static org.fcrepo.services.RepositoryService.metrics;
import static org.fcrepo.services.ServiceHelpers.getObjectSize;
import static org.fcrepo.utils.FedoraTypesUtils.isOwned;
import static org.fcrepo.utils.FedoraTypesUtils.map;
import static org.fcrepo.utils.FedoraTypesUtils.nodetype2name;
import static org.fcrepo.utils.FedoraTypesUtils.value2string;
import static org.modeshape.jcr.api.JcrConstants.NT_FOLDER;

import java.util.Calendar;
import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.fcrepo.utils.FedoraJcrTypes;
import org.modeshape.jcr.api.JcrTools;

import com.yammer.metrics.Timer;

/**
 * An abstraction that represents a Fedora Object backed by
 * a JCR node.
 * 
 * @author ajs6f
 *
 */
public class FedoraObject extends JcrTools implements FedoraJcrTypes {

    private Node node;

    final static Timer timer = metrics.timer(name(FedoraObject.class,
            "FedoraObject"));

    public FedoraObject(final Node n) {
        node = n;
    }

    public FedoraObject(final Session session, final String path)
            throws RepositoryException {

        final Timer.Context context = timer.time();

        try {
            node = findOrCreateNode(session, path, NT_FOLDER);
            node.addMixin(FEDORA_OBJECT);
            node.addMixin(FEDORA_OWNED);
            node.setProperty(FEDORA_OWNERID, session.getUserID());
            node.setProperty(JCR_LASTMODIFIED, Calendar.getInstance());
            node.setProperty(DC_IDENTIFIER, new String[] {node.getIdentifier(),
                    node.getName()});
        } finally {
            context.stop();
        }
    }

    /**
     * @return The JCR name of the node that backs this object.
     * @throws RepositoryException
     */
    public String getName() throws RepositoryException {
        return node.getName();
    }

    /**
     * @return The JCR node that backs this object.
     */
    public Node getNode() {
        return node;
    }

    public String getOwnerId() throws RepositoryException {
        if (isOwned.apply(node)) {
            return node.getProperty(FEDORA_OWNERID).getString();
        } else {
            return null;
        }
    }

    public void setOwnerId(final String ownerId) throws RepositoryException {
        if (isOwned.apply(node)) {
            node.setProperty(FEDORA_OWNERID, ownerId);
        } else {
            node.addMixin(FEDORA_OWNED);
            node.setProperty(FEDORA_OWNERID, ownerId);
        }
    }

    public String getLabel() throws RepositoryException {
        if (node.hasProperty(DC_TITLE)) {
            final Property dcTitle = node.getProperty(DC_TITLE);
            if (!dcTitle.isMultiple()) {
                return dcTitle.getString();
            } else {
                return on('/').join(map(dcTitle.getValues(), value2string));
            }
        }
        return null;
    }

    public void setLabel(final String label) throws RepositoryException {
        node.setProperty(DC_TITLE, label);
    }

    public String getCreated() throws RepositoryException {
        return node.getProperty(JCR_CREATED).getString();
    }

    public String getLastModified() throws RepositoryException {
        return node.getProperty(JCR_LASTMODIFIED).getString();
    }

    public long getSize() throws RepositoryException {
        return getObjectSize(node);
    }

    public Collection<String> getModels() throws RepositoryException {
        return map(node.getMixinNodeTypes(), nodetype2name);
    }

}
