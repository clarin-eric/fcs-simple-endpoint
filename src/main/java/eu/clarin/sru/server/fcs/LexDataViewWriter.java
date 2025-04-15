/**
 * This software is copyright (c) 2013-2025 by
 *  - Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class LexDataViewWriter {
    private static final String LEX_PREFIX = "lex";
    private static final String LEX_NS = Constants.NS_LEX;
    private static final String LEX_MIME_TYPE = Constants.MIMETYPE_LEX;

    private final String xmlLang;
    private final String langUri;
    private final Map<String, List<Value>> fieldsAndValues = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param xmlLang ISO code for Lex Entry or <code>null</code> if not required
     * @param langUri Language URI for more detailed/additional language information
     *                not covered by <code>xmlLang</code> or <code>null</code> if
     *                not required
     */
    public LexDataViewWriter(String xmlLang, String langUri) {
        if (xmlLang != null && !xmlLang.trim().isEmpty()) {
            this.xmlLang = xmlLang;
        } else {
            this.xmlLang = null;
        }
        if (langUri != null && !langUri.trim().isEmpty()) {
            this.langUri = langUri;
        } else {
            this.langUri = null;
        }
    }

    /**
     * Constructor.
     */
    public LexDataViewWriter() {
        this(null, null);
    }

    /**
     * Add a single field value with optional attributes map.
     * 
     * @param fieldType  the Field to which the value belongs
     * @param value      the value
     * @param attributes optional attributes or <code>null</code> if not used
     */
    public void addValue(String fieldType, String value, Map<String, String> attributes) {
        // TODO: reject unsupported fields?
        if (!fieldsAndValues.containsKey(fieldType)) {
            fieldsAndValues.put(fieldType, new ArrayList<>());
        }
        List<Value> values = fieldsAndValues.get(fieldType);
        values.add(new Value(value, attributes));
    }

    public void addValue(String fieldType, String value) {
        addValue(fieldType, value, null);
    }

    @SuppressWarnings("serial")
    public void addValue(String fieldType, String value, String attrName1, String attrValue1) {
        addValue(fieldType, value, new HashMap<String, String>(1) {
            {
                put(attrName1, attrValue1);
            }
        });
    }

    @SuppressWarnings("serial")
    public void addValue(String fieldType, String value, String attrName1, String attrValue1, String attrName2,
            String attrValue2) {
        addValue(fieldType, value, new HashMap<String, String>(2) {
            {
                put(attrName1, attrValue1);
                put(attrName2, attrValue2);
            }
        });
    }

    @SuppressWarnings("serial")
    public void addValue(String fieldType, String value, String attrName1, String attrValue1, String attrName2,
            String attrValue2, String attrName3, String attrValue3) {
        addValue(fieldType, value, new HashMap<String, String>(3) {
            {
                put(attrName1, attrValue1);
                put(attrName2, attrValue2);
                put(attrName3, attrValue3);
            }
        });
    }

    /**
     * Add a multiple field values with optional attributes map.
     * 
     * @param fieldType  the Field to which the values belong to
     * @param values     the list of values
     * @param attributes optional attributes or <code>null</code> if not used
     */
    public void addValues(String fieldType, List<String> values, Map<String, String> attributes) {
        // TODO: reject unsupported fields?
        if (!fieldsAndValues.containsKey(fieldType)) {
            fieldsAndValues.put(fieldType, new ArrayList<>());
        }
        List<Value> fieldValues = fieldsAndValues.get(fieldType);
        for (String value : values) {
            fieldValues.add(new Value(value, attributes));
        }
    }

    /**
     * Write the Lex Data View to the output stream.
     *
     * @param writer the writer to write to
     * @throws XMLStreamException if an error occurred
     */
    public void writeLexDataView(XMLStreamWriter writer)
            throws XMLStreamException {
        if (writer == null) {
            throw new NullPointerException("writer == null");
        }

        XMLStreamWriterHelper.writeStartDataView(writer, LEX_MIME_TYPE);
        writer.setPrefix(LEX_PREFIX, LEX_NS);
        writer.writeStartElement(LEX_NS, "Entry");
        writer.writeNamespace(LEX_PREFIX, LEX_NS);

        // attributes on Entry
        if (xmlLang != null) {
            writer.writeAttribute(XMLConstants.XML_NS_URI, "lang", xmlLang);
        }
        if (langUri != null) {
            writer.writeAttribute("langUri", langUri);
        }

        // Fields (unordered)
        for (Map.Entry<String, List<Value>> field : fieldsAndValues.entrySet()) {
            writer.writeStartElement(LEX_NS, "Field");
            writer.writeAttribute("type", field.getKey());

            // Values
            for (Value value : field.getValue()) {
                writer.writeStartElement(LEX_NS, "Value");

                // @attributes
                if (value.attributes != null) {
                    for (Map.Entry<String, String> attribute : value.attributes.entrySet()) {
                        if (attribute.getKey().startsWith(XMLConstants.XML_NS_PREFIX + ":")) {
                            writer.writeAttribute(XMLConstants.XML_NS_URI,
                                    attribute.getKey().substring(XMLConstants.XML_NS_PREFIX.length() + 1),
                                    attribute.getValue());
                        } else {
                            writer.writeAttribute(attribute.getKey(), attribute.getValue());
                        }
                    }
                }

                writer.writeCharacters(value.value);

                writer.writeEndElement(); // "Value" element
            }

            writer.writeEndElement(); // "Field" element
        }

        writer.writeEndElement(); // "Entry" element
        XMLStreamWriterHelper.writeEndDataView(writer);
    }

    private static final class Value {
        private final String value;
        private final Map<String, String> attributes;

        private Value(String value, Map<String, String> attributes) {
            this.value = value;
            this.attributes = sanitizeAttributes(attributes);
        }

        private static Map<String, String> sanitizeAttributes(Map<String, String> attributes) {
            if (attributes == null || attributes.isEmpty()) {
                return null;
            }

            Map<String, String> sanitized = new HashMap<>();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                // check for valid attribute names
                String name = getName(entry.getKey());
                if (name == null) {
                    continue;
                }
                // check if not empty attribute value
                String value = entry.getValue();
                if (value == null || value.isEmpty()) {
                    continue;
                }
                sanitized.put(name, value);
            }

            if (sanitized.isEmpty()) {
                return null;
            }
            return Collections.unmodifiableMap(sanitized);
        }

        private static String getName(String name) {
            switch (name) {
                // add missing prefix
                case "id":
                case "lang":
                    return XMLConstants.XML_NS_PREFIX + ":" + name;

                // valid keys
                case "xml:id":
                case "xml:lang":
                case "langUri":
                case "preferred":
                case "ref":
                case "idRefs":
                case "vocabRef":
                case "vocabValueRef":
                case "type":
                case "source":
                case "sourceRef":
                case "date":
                    return name;

                // unknown, do not keep
                default:
                    return null;
            }
        }
    }

}
