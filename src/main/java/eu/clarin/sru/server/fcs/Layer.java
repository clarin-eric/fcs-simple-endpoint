package eu.clarin.sru.server.fcs;

import java.net.URI;

/**
 * This class is used to information about a Layers that is available by the
 * Endpoint.
 */
public class Layer {
    /**
     * The content encoding policy for a Layer
     */
    public enum ContentEncoding {
        /**
         * Value information is encoded as element content in this layer.
         */
        VALUE,
        /**
         * No additional value information is encoded for this layer.
         */
        EMPTY
    }
    private final String id;
    private final URI resultId;
    private final String type;
    private final ContentEncoding encoding;
    private final String qualifier;
    private final String altValueInfo;
    private final URI altValueInfoURI;


    /**
     * Constructor.
     *
     * @param id
     *            the identifier of the layer
     * @param resultId
     *            the unique URI that used in the Advanced Data View to refer to
     *            this layer
     * @param type
     *            the type identifier for the layer
     * @param qualifier
     *            an optional layer qualifier to be used in FCS-QL to refer to
     *            this layer or <code>null</code>
     * @param altValueInfo
     *            an additional information about the layer or <code>null</code>
     * @param altValueInfoURI
     *            an additional URI for pointing to more information about the
     *            layer or <code>null</code>
     */
    public Layer(String id, URI resultId, String type,
            ContentEncoding encoding, String qualifier, String altValueInfo,
            URI altValueInfoURI) {
        if (id == null) {
            throw new NullPointerException("id == null");
        }
        this.id = id;

        if (resultId == null) {
            throw new NullPointerException("resultId == null");
        }
        this.resultId = resultId;

        if (type == null) {
            throw new NullPointerException("type == null");
        }
        this.type = type;

        if (encoding == null) {
            throw new NullPointerException("encoding == null");
        }
        this.encoding = encoding;

        this.qualifier       = qualifier;
        this.altValueInfo    = altValueInfo;
        this.altValueInfoURI = altValueInfoURI;
    }


    /**
     * Constructor.
     *
     * @param id
     *            the identifier of the layer
     * @param resultId
     *            the unique URI that used in the Advanced Data View to refer to
     *            this layer
     * @param type
     *            the type identifier for the layer
     */
    public Layer(String id, URI resultId, String type) {
        this(id, resultId, type, ContentEncoding.VALUE, null, null, null);
    }


    /**
     * Get the identifier for the layer.
     *
     * @return the identifier for the layer
     */
    public String getId() {
        return id;
    }


    /**
     * Get the unique URI that used in the Advanced Data View to refer to this
     * layer
     *
     * @return the URI for referring to this layer in the Advanced Data View
     */
    public URI getResultId() {
        return resultId;
    }


    /**
     * Get the type identifier for this layer.
     *
     * @return the type identifier of the layer
     */
    public String getType() {
        return type;
    }


    /**
     * Get the content encoding mode for this layer.
     *
     * @return the content encoding mode
     */
    public ContentEncoding getContentEncoding() {
        return encoding;
    }


    /**
     * Get the optional layer qualifier to be used in FCS-QL
     *
     * @return the layer qualifier or <code>null</code>
     */
    public String getQualifier() {
        return qualifier;
    }


    /**
     * Get the additional information about this layer.
     *
     * @return an additional information about the layer or <code>null</code>
     */
    public String getAltValueInfo() {
        return altValueInfo;
    }


    /**
     * Get an additional URI for pointing to more information about this layer.
     *
     * @return an additional URI for pointing to more information about the
     *         layer or <code>null</code>
     */
    public URI getAltValueInfoURI() {
        return altValueInfoURI;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[");
        sb.append("id=").append(id);
        sb.append(", result-id=").append(resultId);
        sb.append(", type=").append(type);
        if (qualifier != null) {
            sb.append(", qualifier=").append(qualifier);
        }
        sb.append("]");
        return sb.toString();
    }

} // class Layer
