
package org.fcrepo.messaging.legacy;

import static javax.jcr.observation.Event.NODE_ADDED;
import static javax.jcr.observation.Event.NODE_REMOVED;
import static javax.jcr.observation.Event.PROPERTY_ADDED;
import static javax.jcr.observation.Event.PROPERTY_CHANGED;
import static javax.jcr.observation.Event.PROPERTY_REMOVED;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.Parser;
import org.fcrepo.utils.FedoraTypesUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

public class LegacyMethod {

    private static final Abdera abdera = new Abdera();

    private static final Parser abderaParser = abdera.getParser();

    private static final String BASE_URL = "http://localhost:8080/rest"; //TODO Figure out where to get the base url

    private static final Properties FEDORA_TYPES = new Properties();

    public static final String FORMAT =
            "info:fedora/fedora-system:ATOM-APIM-1.0";

    private static final String FORMAT_PREDICATE =
            "http://www.fedora.info/definitions/1/0/types/formatURI";

    //TODO get this out of the build properties
    public static final String SERVER_VERSION = "4.0.0-SNAPSHOT";

    private final static String TYPES_NS =
            "http://www.fedora.info/definitions/1/0/types/";

    private final static String VERSION_PREDICATE =
            "info:fedora/fedora-system:def/view#version";

    private final static String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private static String[] METHODS = new String[] {"ingest", "modifyObject",
            "purgeObject", "addDatastream", "modifyDatastream",
            "purgeDatastream"};

    private final static List<String> METHOD_NAMES = Arrays.asList(METHODS);

    private static final Logger logger = getLogger(LegacyMethod.class);

    private static final String MAP_PROPERTIES =
            "/org/fcrepo/messaging/legacy/map.properties";

    static {
        try (final InputStream is =
                LegacyMethod.class.getResourceAsStream(MAP_PROPERTIES)) {
            FEDORA_TYPES.load(is);
        } catch (final IOException e) {
            throw new RuntimeException(e);
            // it's in the jar.
        }
    }

    private final Entry delegate;

    public LegacyMethod(final Event jcrEvent, final Node resource)
            throws RepositoryException {
        this(newEntry());

        final boolean isDatastreamNode =
                FedoraTypesUtils.isFedoraDatastream.apply(resource);
        final boolean isObjectNode =
                FedoraTypesUtils.isFedoraObject.apply(resource) &&
                        !isDatastreamNode;

        if (isDatastreamNode || isObjectNode) {
            setMethodName(mapMethodName(jcrEvent, isObjectNode));
            final String returnValue = getReturnValue(jcrEvent, resource);
            setContent(getEntryContent(getMethodName(), returnValue));
            if (isDatastreamNode) {
                setPid(resource.getParent().getName());
                setDsId(resource.getName());
            } else {
                setPid(resource.getName());
            }
        } else {
            setMethodName(null);
        }
        final String userID =
                jcrEvent.getUserID() == null ? "unknown" : jcrEvent.getUserID();
        setUserId(userID);
        setModified(new Date(jcrEvent.getDate()));
    }

    public LegacyMethod(final Entry atomEntry) {
        delegate = atomEntry;
    }

    public LegacyMethod(final String atomEntry) {
        delegate =
                (Entry) abderaParser.parse(new StringReader(atomEntry))
                        .getRoot();
    }

    public Entry getEntry() {
        return delegate;
    }

    public void setContent(final String content) {
        delegate.setContent(content);
    }

    public void setUserId(String val) {
        if (val == null) {
            val = "unknown";
        }
        delegate.addAuthor(val, null, BASE_URL);
    }

    public String getUserID() {
        return delegate.getAuthor().getName();
    }

    public void setModified(final Date date) {
        delegate.setUpdated(date);
    }

    public Date getModified() {
        return delegate.getUpdated();
    }

    public void setMethodName(final String val) {
        delegate.setTitle(val).setBaseUri(BASE_URL);
    }

    public String getMethodName() {
        return delegate.getTitle();
    }

    public void setPid(final String val) {
        final List<Category> vals = delegate.getCategories("fedora-types:pid");
        if (vals == null || vals.isEmpty()) {
            delegate.addCategory("xsd:string", val, "fedora-types:pid");
        } else {
            vals.get(0).setTerm(val);
        }
        delegate.setSummary(val);
    }

    public String getPid() {
        final List<Category> categories = delegate.getCategories("xsd:string");
        for (final Category c : categories) {
            if ("fedora-types:pid".equals(c.getLabel())) {
                return c.getTerm();
            }
        }
        return null;
    }

    public void setDsId(final String val) {
        final List<Category> vals = delegate.getCategories("fedora-types:dsID");
        if (vals == null || vals.isEmpty()) {
            delegate.addCategory("xsd:string", val, "fedora-types:dsID");
        } else {
            vals.get(0).setTerm(val);
        }
    }

    public String getDsId() {
        final List<Category> categories = delegate.getCategories("xsd:string");
        for (final Category c : categories) {
            if ("fedora-types:dsID".equals(c.getLabel())) {
                return c.getTerm();
            }
        }
        return null;
    }

    public void writeTo(final Writer writer) throws IOException {
        delegate.writeTo(writer);
    }

    private static Entry newEntry() {
        final Entry entry = abdera.newEntry();
        entry.declareNS(XSD_NS, "xsd");
        entry.declareNS(TYPES_NS, "fedora-types");
        entry.setId("urn:uuid:" + UUID.randomUUID().toString());
        entry.addCategory(FORMAT_PREDICATE, FORMAT, "format");
        entry.addCategory(VERSION_PREDICATE, SERVER_VERSION, "version");
        return entry;
    }

    private static String getEntryContent(final String methodName,
            final String returnVal) {
        //String parm = (String)FEDORA_TYPES.get(methodName + ".parm");
        final String datatype =
                (String) FEDORA_TYPES.get(methodName + ".datatype");
        return objectToString(returnVal, datatype);
    }

    private static String
            objectToString(final String obj, final String xsdType) {
        if (obj == null) {
            return "null";
        }
        final String javaType = obj.getClass().getCanonicalName();
        String term;
        //TODO Most of these types are not yet relevant to FCR4, but we can borrow their serializations as necessary
        if (javaType != null && javaType.equals("java.util.Date")) { // several circumstances yield null canonical names
            //term = convertDateToXSDString((Date) obj);
            term = "[UNSUPPORTED" + xsdType + "]";
        } else if (xsdType.equals("fedora-types:ArrayOfString")) {
            //term = array2string(obj);
            term = "[UNSUPPORTED" + xsdType + "]";
        } else if (xsdType.equals("xsd:boolean")) {
            term = obj;
        } else if (xsdType.equals("xsd:nonNegativeInteger")) {
            term = obj;
        } else if (xsdType.equals("fedora-types:RelationshipTuple")) {
            //RelationshipTuple[] tuples = (RelationshipTuple[]) obj;
            //TupleArrayTripleIterator iter =
            //		new TupleArrayTripleIterator(new ArrayList<RelationshipTuple>(Arrays
            //				.asList(tuples)));
            //ByteArrayOutputStream os = new ByteArrayOutputStream();
            //try {
            //	iter.toStream(os, RDFFormat.NOTATION_3, false);
            //} catch (TrippiException e) {
            //	e.printStackTrace();
            //}
            //term = new String(os.toByteArray());
            term = "[UNSUPPORTED" + xsdType + "]";
        } else if (javaType != null && javaType.equals("java.lang.String")) {
            term = obj;
            term = term.replaceAll("\"", "'");
        } else {
            term = "[OMITTED]";
        }
        return term;
    }

    private static String getReturnValue(final Event jcrEvent,
            final Node jcrNode) throws RepositoryException {
        switch (jcrEvent.getType()) {
            case NODE_ADDED:
                return jcrNode.getName();
            case NODE_REMOVED:
                return convertDateToXSDString(jcrEvent.getDate());
            case PROPERTY_ADDED:
                return convertDateToXSDString(jcrEvent.getDate());
            case PROPERTY_CHANGED:
                return convertDateToXSDString(jcrEvent.getDate());
            case PROPERTY_REMOVED:
                return convertDateToXSDString(jcrEvent.getDate());
        }
        return null;
    }

    private static String mapMethodName(final Event jcrEvent,
            final boolean isObjectNode) {
        switch (jcrEvent.getType()) {
            case NODE_ADDED:
                return isObjectNode ? "ingest" : "addDatastream";
            case NODE_REMOVED:
                return isObjectNode ? "purgeObject" : "purgeDatastream";
            case PROPERTY_ADDED:
                return isObjectNode ? "modifyObject" : "modifyDatastream";
            case PROPERTY_CHANGED:
                return isObjectNode ? "modifyObject" : "modifyDatastream";
            case PROPERTY_REMOVED:
                return isObjectNode ? "modifyObject" : "modifyDatastream";
        }
        return null;
    }

    /**
     * @param date Instance of java.util.Date.
     * @return the lexical form of the XSD dateTime value, e.g.
     *         "2006-11-13T09:40:55.001Z".
     */
    private static String convertDateToXSDString(final long date) {
        final DateTime dt = new DateTime(date);
        final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        return fmt.print(dt);
    }

    public static boolean canParse(final Message jmsMessage) {
        try {
            return FORMAT.equals(jmsMessage.getJMSType()) &&
                    METHOD_NAMES.contains(jmsMessage
                            .getStringProperty("methodName"));
        } catch (final JMSException e) {
            logger.info("Could not parse message: {}", jmsMessage);
            return false;
        }
    }

}
