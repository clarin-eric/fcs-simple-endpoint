package eu.clarin.sru.server.fcs.utils;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.EndpointDescription;


/**
 * An abstract base class for implementing endpoint descriptions. It already
 * implements the methods required for capabilities and supported dataviews.
 *
 * @see EndpointDescription
 *
 */
public abstract class AbstractEndpointDescriptionBase implements EndpointDescription {
    protected final List<URI> capabilities;
    protected final List<DataView> supportedDataViews;

    /**
     * Constructor.
     *
     * @param capabilities
     *            a list of capabilities supported by this endpoint
     * @param supportedDataViews
     *            a list of data views that are supported by this endpoint
     */
    protected AbstractEndpointDescriptionBase(List<URI> capabilities,
            List<DataView> supportedDataViews) {
        if (capabilities == null) {
            throw new NullPointerException("capabilities == null");
        }
        if (capabilities.isEmpty()) {
            throw new IllegalArgumentException("capabilities are empty");
        }
        for (URI capability : capabilities) {
            if (capability == null) {
                throw new IllegalArgumentException(
                        "capabilities must not contain a 'null' item");
            }
        }
        this.capabilities = Collections.unmodifiableList(capabilities);

        if (supportedDataViews == null) {
            throw new NullPointerException("supportedDataViews == null");
        }
        if (supportedDataViews.isEmpty()) {
            throw new IllegalArgumentException("supportedDataViews are empty");
        }
        for (DataView supportedDataView : supportedDataViews) {
            if (supportedDataView == null) {
                throw new IllegalArgumentException(
                        "supportedDataViews must not contain a 'null' item");
            }
        }
        this.supportedDataViews =
                Collections.unmodifiableList(supportedDataViews);
    }


    @Override
    public List<URI> getCapabilities() {
        return capabilities;
    }


    @Override
    public List<DataView> getSupportedDataViews() {
        return supportedDataViews;
    }

} // abstract class EndpointDescriptionBase
