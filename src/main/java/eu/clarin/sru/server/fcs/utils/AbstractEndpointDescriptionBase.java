package eu.clarin.sru.server.fcs.utils;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.Layer;


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
    protected final List<Layer> supportedLayers;


    /**
     * Constructor.
     *
     * @param capabilities
     *            a list of capabilities supported by this endpoint
     * @param supportedDataViews
     *            a list of data views that are supported by this endpoint
     */
    protected AbstractEndpointDescriptionBase(List<URI> capabilities,
            List<DataView> supportedDataViews, List<Layer> supportedLayers) {
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

        if ((supportedLayers != null) && !supportedLayers.isEmpty()) {
            for (Layer layer : supportedLayers) {
                if (layer == null) {
                    throw new IllegalArgumentException(
                            "supportedLayers must not contain a 'null' item");
                }
            }
            this.supportedLayers =
                    Collections.unmodifiableList(supportedLayers);
        } else {
            this.supportedLayers = null;
        }
    }


    @Override
    public List<URI> getCapabilities() {
        return capabilities;
    }


    @Override
    public List<DataView> getSupportedDataViews() {
        return supportedDataViews;
    }


    @Override
    public List<Layer> getSupportedLayers() {
        return supportedLayers;
    }

} // abstract class EndpointDescriptionBase
