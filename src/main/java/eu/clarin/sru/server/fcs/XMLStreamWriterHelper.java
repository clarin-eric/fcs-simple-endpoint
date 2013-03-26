package eu.clarin.sru.server.fcs;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * This class provides several helper methods for writing records in the
 * CLARIN-FCS record schema. These methods <em>do not</em> cover the full
 * spectrum of all variations of records that are permitted by the CLARIN FCS
 * specification.
 *
 * @see <a
 *      href="https://trac.clarin.eu/wiki/FCS-specification#SearchRetrieveOperation">
 *      CLARIN FCS specification, section "SearchRetrieve Operation"</a>
 */
public final class XMLStreamWriterHelper {
    private static final String FCS_NS          = "http://clarin.eu/fcs/1.0";
    private static final String FCS_PREFIX      = "fcs";
    private static final String FCS_KWIC_NS     =
            "http://clarin.eu/fcs/1.0/kwic";
    private static final String FCS_KWIC_PREFIX = "kwic";
    private static final String FCS_KWIC_MIMETYPE =
            "application/x-clarin-fcs-kwic+xml";


    /**
     * Write the start of a resource (i.e. the <code>&lt;Resource&gt;</code>
     * element). Calls to this method need to be balanced with calls to the
     * {@link #writeEndResource(XMLStreamWriter)} method.
     *
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @param pid
     *            the persistent identifier of this resource or
     *            <code>null</code>, if not applicable
     * @param ref
     *            the reference of this resource or <code>null</code>, if not
     *            applicable
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeStartResource(XMLStreamWriter writer, String pid,
            String ref) throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writer.setPrefix(FCS_PREFIX, FCS_NS);
        writer.writeStartElement(FCS_NS, "Resource");
        writer.writeNamespace(FCS_PREFIX, FCS_NS);
        if ((pid != null) && !pid.isEmpty()) {
            writer.writeAttribute("pid", pid);
        }
        if ((ref != null) && !ref.isEmpty()) {
            writer.writeAttribute("ref", ref);
        }
    }


    /**
     * Write the end of a resource (i.e. the <code>&lt;/Resource&gt;</code>
     * element). Calls to this method need to be balanced with calls to the
     * {@link #writeStartResource(XMLStreamWriter, String, String)} method.
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeEndResource(XMLStreamWriter writer)
            throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writer.writeEndElement(); // "Resource" element
    }


    /**
     * Write the start of a resource fragment (i.e. the
     * <code>&lt;ResourceFragment&gt;</code> element). Calls to this method need
     * to be balanced with calls to the
     * {@link #writeEndResourceFragment(XMLStreamWriter)} method.
     *
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @param pid
     *            the persistent identifier of this resource or
     *            <code>null</code>, if not applicable
     * @param ref
     *            the reference of this resource or <code>null</code>, if not
     *            applicable
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeStartResourceFragment(XMLStreamWriter writer,
            String pid, String ref) throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writer.writeStartElement(FCS_NS, "ResourceFragment");
        if ((pid != null) && !pid.isEmpty()) {
            writer.writeAttribute("pid", pid);
        }
        if ((ref != null) && !ref.isEmpty()) {
            writer.writeAttribute("ref", ref);
        }
    }


    /**
     * Write the end of a resource fragment (i.e. the
     * <code>&lt;/ResourceFragment&gt;</code> element). Calls to this method
     * need to be balanced with calls to the
     * {@link #writeStartResourceFragment(XMLStreamWriter, String, String)}
     * method.
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeEndResourceFragment(XMLStreamWriter writer)
            throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writer.writeEndElement(); // "ResourceFragment" element
    }


    /**
     * Write the start of a data view (i.e. the
     * <code>&lt;DataView&gt;</code> element). Calls to this method need
     * to be balanced with calls to the
     * {@link #writeEndResource(XMLStreamWriter)} method.
     *
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @param mimetype
     *            the MIME type of this data view
     *            applicable
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeStartDataView(XMLStreamWriter writer,
            String mimetype) throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }
        if (mimetype == null) {
            throw new NullPointerException("mimetype == null");
        }
        if (mimetype.isEmpty()) {
            throw new IllegalArgumentException("mimetype is empty");
        }

        writer.writeStartElement(FCS_NS, "DataView");
        writer.writeAttribute("type", mimetype);
    }


    /**
     * Write the end of a data view (i.e. the <code>&lt;/DataView&gt;</code>
     * element). Calls to this method need to be balanced with calls to the
     * {@link #writeStartDataView(XMLStreamWriter, String)} method.
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeEndDataView(XMLStreamWriter writer)
            throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writer.writeEndElement(); // "DataView" element
    }


    /**
     * Convince method to write a KWIC data view. It automatically performs the
     * calls to {@link #writeStartDataView(XMLStreamWriter, String)} and
     * {@link #writeEndDataView(XMLStreamWriter)}.
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @param left
     *            the left context of the KWIC or <code>null</code> if not
     *            applicable
     * @param keyword
     *            the keyword of the KWIC
     * @param right
     *            the right context of the KWIC or <code>null</code> if not
     *            applicable
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeKWICDataView(XMLStreamWriter writer, String left,
            String keyword, String right) throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writeStartDataView(writer, FCS_KWIC_MIMETYPE);

        // actual "kwic" data view
        writer.setPrefix(FCS_KWIC_PREFIX, FCS_KWIC_NS);
        writer.writeStartElement(FCS_KWIC_NS, "kwic");
        writer.writeNamespace(FCS_KWIC_PREFIX, FCS_KWIC_NS);

        writer.writeStartElement(FCS_KWIC_NS, "c");
        writer.writeAttribute("type", "left");
        if (left != null) {
            writer.writeCharacters(left);
        }
        writer.writeEndElement(); // "c" element

        writer.writeStartElement(FCS_KWIC_NS, "kw");
        writer.writeCharacters(keyword);
        writer.writeEndElement(); // "kw" element

        writer.writeStartElement(FCS_KWIC_NS, "c");
        writer.writeAttribute("type", "right");
        if (right != null) {
            writer.writeCharacters(right);
        }
        writer.writeEndElement(); // "c" element

        writer.writeEndElement(); // "kwic" element

        writeEndDataView(writer);
    }


    /**
     * Convince method for writing a record with a KWIC data view. The following
     * code (arguments omitted) would accomplish the same result:
     * <pre>
     * ...
     * writeStartResource(...);
     * writeKWICDataView(...);
     * writeEndResource(...);
     * ...
     * </pre>
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @param pid
     *            the persistent identifier of this resource or
     *            <code>null</code>, if not applicable
     * @param ref
     *            the reference of this resource or <code>null</code>, if not
     *            applicable
     * @param left
     *            the left context of the KWIC or <code>null</code> if not
     *            applicable
     * @param keyword
     *            the keyword of the KWIC
     * @param right
     *            the right context of the KWIC or <code>null</code> if not
     *            applicable
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeResourceWithKWICDataView(XMLStreamWriter writer,
            String pid, String ref, String left, String keyword, String right)
            throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writeStartResource(writer, pid, ref);
        writeKWICDataView(writer, left, keyword, right);
        writeEndResource(writer);
    }

} // class XMLStreamWriterHelper
