package eu.clarin.sru.server.fcs;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public class AdvancedDataViewWriter {
    public enum Unit {
        ITEM,
        TIMESTAMP
    }
    private static final long INITIAL_SEGMENT_ID = 1;
    public static final int NO_HIGHLIGHT = -1;
    private static final String ADV_PREFIX = "adv";
    private static final String ADV_NS = "http://clarin.eu/fcs/dataview/advanced";
    private static final String ADV_MIME_TYPE = "application/x-clarin-fcs-adv+xml";
    private final Unit unit;
    private final List<Segment> segments = new ArrayList<Segment>();
    private final Map<URI, List<Span>> layers = new HashMap<URI, List<Span>>();
    private long nextSegmentId = INITIAL_SEGMENT_ID;


    public AdvancedDataViewWriter(Unit unit) {
        if (unit == null) {
            throw new NullPointerException("unit == null");
        }
        this.unit = unit;
    }


    public void reset() {
        nextSegmentId = INITIAL_SEGMENT_ID;
    }


    private static final class Segment {
        private final String id;
        private final long start;
        private final long end;
        private final URI ref;

        private Segment(long id, long start, long end) {
            this.id = "s" + Long.toHexString(id);
            this.start = start;
            this.end = end;
            this.ref = null;
        }
    }

    private static final class Span {
        private final Segment segment;
        private final String value;
        private final String altValue;
        private final String highlight;


        private Span(Segment segment, String value, String altValue,
                int highlight) {
            this.segment = segment;
            this.value = value;
            this.altValue = altValue;
            if (highlight != NO_HIGHLIGHT) {
                this.highlight = "h" + Integer.toHexString(highlight);
            } else {
                this.highlight = null;
            }
        }
    }


    public void addSpan(URI layerId, long start, long end, String value) {
        addSpan(layerId, start, end, value, null, NO_HIGHLIGHT);
    }


    public void addSpan(URI layerId, long start, long end, String value,
            int highlight) {
        addSpan(layerId, start, end, value, null, highlight);
    }


    public void addSpan(URI layerId, long start, long end, String value,
            String altValue) {
        addSpan(layerId, start, end, value, altValue, NO_HIGHLIGHT);
    }


    public void addSpan(URI layerId, long start, long end, String value,
            String altValue, int highlight) {
        if (layerId == null) {
            throw new NullPointerException("layerId == null");
        }
        if (start < 0) {
            throw new IllegalArgumentException("start < 0");
        }
        if (end < start) {
            throw new IllegalArgumentException("end < start");
        }
        if (highlight <= 0) {
            highlight = NO_HIGHLIGHT;
        }

        // find segment or create a new one
        Segment segment = null;
        for (Segment seg : segments) {
            if ((seg.start == start) && (seg.end == end)) {
                segment = seg;
                break;
            }
        }
        if (segment == null) {
            segment = new Segment(nextSegmentId++, start, end);
            segments.add(segment);
        }

        // find layer or create a new one
        List<Span> layer = layers.get(layerId);
        if (layer == null) {
            layer = new ArrayList<Span>();
            layers.put(layerId, layer);
        }

        // sanity check (better overlap check?)
        for (Span span : layer) {
            if (segment.equals(span.segment)) {
                // FIXME: better exception!
                throw new IllegalArgumentException("segment already exists in layer");
            }
        }
        layer.add(new Span(segment, value, altValue, highlight));
    }


    public void writeAdvancedDataView(XMLStreamWriter writer)
            throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        XMLStreamWriterHelper.writeStartDataView(writer, ADV_MIME_TYPE);
        writer.setPrefix(ADV_PREFIX, ADV_NS);
        writer.writeStartElement(ADV_NS, "Advanced");
        writer.writeNamespace(ADV_PREFIX, ADV_NS);
        if (unit == Unit.ITEM) {
            writer.writeAttribute("unit", "item");
        } else if (unit == Unit.TIMESTAMP) {
            writer.writeAttribute("unit", "timestamp");
        }

        // segments
        writer.writeStartElement(ADV_NS, "Segments");
        for (Segment segment : segments) {
            // FIXME: unit translation (long -> time)
            writer.writeEmptyElement(ADV_NS, "Segment");
            writer.writeAttribute("id", segment.id);
            writer.writeAttribute("start", Long.toString(segment.start));
            writer.writeAttribute("end", Long.toString(segment.end));
            if (segment.ref != null) {
                writer.writeAttribute("ref", segment.ref.toString());
            }
        }
        writer.writeEndElement(); // "Segments" element

        // layers
        writer.writeStartElement(ADV_NS, "Layers");
        for (Map.Entry<URI, List<Span>> layer : layers.entrySet()) {
            writer.writeStartElement(ADV_NS, "Layer");
            writer.writeAttribute("id", layer.getKey().toString());
            for (Span span : layer.getValue()) {
                if ((span.value != null) && !span.value.isEmpty()) {
                    writer.writeStartElement(ADV_NS, "Span");
                    writer.writeAttribute("ref", span.segment.id);
                    if (span.highlight != null) {
                        writer.writeAttribute("highlight", span.highlight);
                    }
                    if (span.altValue != null) {
                        writer.writeAttribute("alt-value", span.altValue);
                    }
                    writer.writeCharacters(span.value);
                    writer.writeEndElement(); // "Span" element
                } else {
                    writer.writeEmptyElement(ADV_NS, "Span");
                    writer.writeAttribute("ref", span.segment.id);
                    if (span.highlight != null) {
                        writer.writeAttribute("highlight", span.highlight);
                    }
                    if (span.altValue != null) {
                        writer.writeAttribute("alt-value", span.altValue);
                    }
                }
            }
            writer.writeEndElement(); // "Layer" element
        }
        writer.writeEndElement(); // "Layers" element

        writer.writeEndElement(); // "Advanced" element
        XMLStreamWriterHelper.writeEndDataView(writer);
    }

} // class AdvancedDataViewHelper
