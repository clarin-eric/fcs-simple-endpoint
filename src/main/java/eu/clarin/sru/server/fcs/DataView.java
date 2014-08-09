package eu.clarin.sru.server.fcs;

/**
 * This class is used to hold information about a data view that is implemented
 * by the endpoint.
 */
public class DataView {
    /**
     * Enumeration to indicate the delivery policy of a data view.
     */
    public enum DeliveryPolicy {
        /**
         * The data view is sent automatically  by the endpoint.
         */
        SEND_BY_DEFAULT,
        /**
         * A client must explicitly request the endpoint.
         */
        NEED_TO_REQUEST;
    } // enum PayloadDelivery
    private final String identifier;
    private final String mimeType;
    private final DeliveryPolicy deliveryPolicy;


    /**
     * Constructor.
     *
     * @param identifier
     *            a unique short identifier for the data view
     * @param mimeType
     *            the MIME type of the data view
     * @param deliveryPolicy
     *            the delivery policy for this data view
     */
    public DataView(String identifier, String mimeType,
            DeliveryPolicy deliveryPolicy) {
        if (identifier == null) {
            throw new NullPointerException("identifier == null");
        }
        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("identifier is empty");
        }
        this.identifier = identifier;

        if (mimeType == null) {
            throw new NullPointerException("mimeType == null");
        }
        if (mimeType.isEmpty()) {
            throw new IllegalArgumentException("mimeType is empty");
        }
        this.mimeType = mimeType;

        if (deliveryPolicy == null) {
            throw new NullPointerException("deliveryPolicy == null");
        }
        this.deliveryPolicy = deliveryPolicy;
    }


    public String getIdentifier() {
        return identifier;
    }


    public String getMimeType() {
        return mimeType;
    }


    public DeliveryPolicy getDeliveryPolicy() {
        return deliveryPolicy;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[");
        sb.append("identifier=").append(identifier);
        sb.append(", mimeType=").append(mimeType);
        sb.append("]");
        return sb.toString();
    }
} // class DataView
