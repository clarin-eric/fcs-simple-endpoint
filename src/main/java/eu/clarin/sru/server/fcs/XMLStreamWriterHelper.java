/**
 * This software is copyright (c) 2013-2016 by
 *  - Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * This class provides several helper methods for writing records in the
 * CLARIN-FCS record schema. These methods <em>do not</em> cover the full
 * spectrum of all variations of records that are permitted by the CLARIN-FCS
 * specification.
 *
 * @see <a
 *      href="https://trac.clarin.eu/wiki/FCS/Specification">
 *      CLARIN FCS specification, section "Operation searchRetrieve"</a>
 */
public class XMLStreamWriterHelper {
    protected static final String FCS_NS          =
            "http://clarin.eu/fcs/resource";
    protected static final String FCS_PREFIX      =
            "fcs";
    protected static final String FCS_KWIC_NS     =
            "http://clarin.eu/fcs/1.0/kwic";
    protected static final String FCS_KWIC_PREFIX =
            "kwic";
    protected static final String FCS_KWIC_MIMETYPE =
            "application/x-clarin-fcs-kwic+xml";
    protected static final String FCS_HITS_NS =
            "http://clarin.eu/fcs/dataview/hits";
    protected static final String FCS_HITS_PREFIX =
            "hits";
    protected static final String FCS_HITS_MIMETYPE =
            "application/x-clarin-fcs-hits+xml";


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
     * @deprecated Use the HITS data view instead.
     */
    @Deprecated
    public static void writeKWICDataView(XMLStreamWriter writer, String left,
            String keyword, String right) throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }
        if (keyword == null) {
            throw new NullPointerException("keyword == null");
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
     *
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
     * @deprecated The the HITS data view instead.
     */
    @Deprecated
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



    /**
     * Convince method to write a simple HITS data view. It automatically
     * performs the calls to
     * {@link #writeStartDataView(XMLStreamWriter, String)} and
     * {@link #writeEndDataView(XMLStreamWriter)}.
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @param left
     *            the left context of the hit or <code>null</code> if not
     *            applicable
     * @param hit
     *            the actual hit, that will be highlighted
     * @param right
     *            the right context of the hit or <code>null</code> if not
     *            applicable
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeHitsDataView(XMLStreamWriter writer, String left,
            String hit, String right) throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }
        if (hit == null) {
            throw new NullPointerException("hit == null");
        }

        writeStartDataView(writer, FCS_HITS_MIMETYPE);

        // actual "hits" data view
        writer.setPrefix(FCS_HITS_PREFIX, FCS_HITS_NS);
        writer.writeStartElement(FCS_HITS_NS, "Result");
        writer.writeNamespace(FCS_HITS_PREFIX, FCS_HITS_NS);

        if ((left != null) && !left.isEmpty()) {
            writer.writeCharacters(left);
        }

        writer.writeStartElement(FCS_HITS_NS, "Hit");
        writer.writeCharacters(hit);
        writer.writeEndElement(); // "Hit" element

        if ((right != null) && !right.isEmpty()) {
            writer.writeCharacters(right);
        }

        writer.writeEndElement(); // "Result" element

        writeEndDataView(writer);
    }


    /**
     * Convince method for writing a record with a HITS data view. The following
     * code (arguments omitted) would accomplish the same result:
     *
     * <pre>
     * ...
     * writeStartResource(...);
     * writeHitsDataView(...);
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
     *            the left context of the hit or <code>null</code> if not
     *            applicable
     * @param hit
     *            the actual hit, that will be highlighted
     * @param right
     *            the right context of the hit or <code>null</code> if not
     *            applicable
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeResourceWithHitsDataView(XMLStreamWriter writer,
            String pid, String ref, String left, String hit, String right)
            throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writeStartResource(writer, pid, ref);
        writeHitsDataView(writer, left, hit, right);
        writeEndResource(writer);
    }


    /**
     * Convince method to write a simple HITS data view. It automatically
     * performs the calls to
     * {@link #writeStartDataView(XMLStreamWriter, String)} and
     * {@link #writeEndDataView(XMLStreamWriter)}.
     *
     * @param writer
     *            the {@link XMLStreamWriter} to be used
     * @param text
     *            the text content of the hit
     * @param hits
     *            an even-element array containing tuples for the hit markers in
     *            the text content
     * @param secondIsLength
     *            if <code>true</code> the second element of each tuple in this
     *            <code>hits</code> array is interpreted as an length; if
     *            <code>false</code> it is interpreted as an end-offset
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeHitsDataView(XMLStreamWriter writer, String text,
            int[] hits, boolean secondIsLength) throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }
        if (text == null) {
            throw new NullPointerException("text == null");
        }
        if (hits == null) {
            throw new NullPointerException("text == null");
        }
        if ((hits.length == 0) || ((hits.length % 2) != 0)) {
            throw new NullPointerException("length of hits array must " +
                    "contain an even number of elements");
        }

        writeStartDataView(writer, FCS_HITS_MIMETYPE);

        // actual "hits" data view
        writer.setPrefix(FCS_HITS_PREFIX, FCS_HITS_NS);
        writer.writeStartElement(FCS_HITS_NS, "Result");
        writer.writeNamespace(FCS_HITS_PREFIX, FCS_HITS_NS);

        int pos = 0;
        for (int i = 0; i < hits.length; i += 2) {
            int start  = hits[i];
            int end    = hits[i + 1];

            if ((start < 0) && (start > text.length())) {
                throw new IllegalArgumentException("start index out of " +
                        "bounds: start=" + start);
            }
            if (secondIsLength) {
                if (end < 1) {
                    throw new IllegalArgumentException(
                            "length must be larger than 0: length = " + end);
                }
                end += start;
            }
            if (start >= end) {
                throw new IllegalArgumentException("end offset must be " +
                        "larger then start offset: start=" + start +
                        ", end="+ end);
            }

            if (start > pos) {
                String s = text.substring(pos, start);
                writer.writeCharacters(s);
            }
            writer.writeStartElement(FCS_HITS_NS, "Hit");
            writer.writeCharacters(text.substring(start, end));
            writer.writeEndElement(); // "Hits" element
            pos = end;
        }
        if (pos < text.length() - 1) {
            writer.writeCharacters(text.substring(pos));
        }

        writer.writeEndElement(); // "Result" element

        writeEndDataView(writer);
    }


    /**
     * Convince method to write a simple HITS data view. It automatically
     * performs the calls to
     * {@link #writeStartDataView(XMLStreamWriter, String)} and
     * {@link #writeEndDataView(XMLStreamWriter)}.
     *
     * <pre>
     * ...
     * writeStartResource(...);
     * writeHitsDataView(...);
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
     * @param text
     *            the text content of the hit
     * @param hits
     *            an even-element array containing tuples for the hit markers in
     *            the text content
     * @param secondIsLength
     *            if <code>true</code> the second element of each tuple in this
     *            <code>hits</code> array is interpreted as an length; if
     *            <code>false</code> it is interpreted as an end-offset
     * @throws XMLStreamException
     *             if an error occurred
     */
    public static void writeResourceWithHitsDataView(XMLStreamWriter writer,
            String pid, String ref, String text,
            int[] hits, boolean secondIsLength)
            throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writeStartResource(writer, pid, ref);
        writeHitsDataView(writer, text, hits, secondIsLength);
        writeEndResource(writer);
    }


    /**
     * Convince method for writing a record with a HITS and a KWIC data view.
     * This method is intended for applications that want ensure computability
     * to legacy CLARIN-FCS clients The following code (arguments omitted) would
     * accomplish the same result:
     *
     * <pre>
     * ...
     * writeStartResource(...);
     * writeHitsDataView(...);
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
     *            the left context of the hit or <code>null</code> if not
     *            applicable
     * @param hit
     *            the actual hit, that will be highlighted
     * @param right
     *            the right context of the hit or <code>null</code> if not
     *            applicable
     * @throws XMLStreamException
     *             if an error occurred
     * @deprecated Only use, if you want compatability to legacy FCS
     *             applications.
     */
    @Deprecated
    public static void writeResourceWithHitsDataViewLegacy(XMLStreamWriter writer,
            String pid, String ref, String left, String hit, String right)
            throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        writeStartResource(writer, pid, ref);
        writeHitsDataView(writer, left, hit, right);
        writeKWICDataView(writer, left, hit, right);
        writeEndResource(writer);
    }

} // class XMLStreamWriterHelper
