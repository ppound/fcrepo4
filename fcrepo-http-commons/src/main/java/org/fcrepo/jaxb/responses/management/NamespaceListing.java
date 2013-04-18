
package org.fcrepo.jaxb.responses.management;

import java.net.URI;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "namespaceRegistry")
public class NamespaceListing {

    @XmlElement(name = "namespace")
    public Set<Namespace> namespaces;

    public NamespaceListing(final Set<Namespace> nses) {
        namespaces = nses;
    }

    public NamespaceListing() {
    }

    public static class Namespace {

        @XmlAttribute
        public String prefix;

        @XmlAttribute(name = "URI")
        public URI uri;

        public Namespace(final String prefix, final URI uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        public Namespace() {
        }
    }

}
