package eu.clarin.sru.server.fcs.utils;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.Layer;
import eu.clarin.sru.server.fcs.ResourceInfo;


/**
 * A very simple implementation of an endpoint description that is initialized
 * from static information supplied at construction time. Mostly used together
 * with {@link SimpleEndpointDescriptionParser}, but it is agnostic how the
 * static list of resource info records is generated.
 *
 * @see EndpointDescription
 * @see SimpleEndpointDescriptionParser
 */
public class SimpleEndpointDescription extends AbstractEndpointDescriptionBase {
    private final boolean pidCaseSensitive;
    private final List<ResourceInfo> entries;


    /**
     * Constructor.
     *
     * @param capabilities
     *            a list of capabilities supported by this endpoint
     * @param supportedDataViews
     *            a list of data views that are supported by this endpoint
     * @param supportedLayers
     *            a list of layers supported for Advanced Search by this
     *            endpoint or <code>null</code>
     * @param resources
     *            a static list of resource info records
     * @param pidCaseSensitive
     *            <code>true</code> if comparison of persistent identifiers
     *            should be performed case-sensitive, <code>false</code>
     *            otherwise
     */
    public SimpleEndpointDescription(List<URI> capabilities,
            List<DataView> supportedDataViews,
            List<Layer> supportedLayers,
            List<ResourceInfo> resources,
            boolean pidCaseSensitive) {
        super(capabilities, supportedDataViews, supportedLayers);

        if (resources == null) {
            throw new NullPointerException("entries == null");
        }
        this.entries = Collections.unmodifiableList(resources);
        this.pidCaseSensitive = pidCaseSensitive;
    }


    @Override
    public void destroy() {
    }


    @Override
    public List<ResourceInfo> getResourceList(String pid) throws SRUException {
        if (pid == null) {
            throw new NullPointerException("pid == null");
        }
        if (pid.isEmpty()) {
            throw new IllegalArgumentException("pid is empty");
        }
        if (!pidCaseSensitive) {
            pid = pid.toLowerCase();
        }
        if (pid.equals(PID_ROOT)) {
            return entries;
        } else {
            ResourceInfo ri = findRecursive(entries, pid);
            if (ri != null) {
                return ri.getSubResources();
            }
        }
        return null;
    }


    private ResourceInfo findRecursive(List<ResourceInfo> items, String pid) {
        if ((items != null) && !items.isEmpty()) {
            for (ResourceInfo item : items) {
                if (pidCaseSensitive) {
                    if (pid.equals(item.getPid())) {
                        return item;
                    }
                } else {
                    if (pid.equalsIgnoreCase(item.getPid())) {
                        return item;
                    }
                }
                if (item.hasSubResources()) {
                    ResourceInfo ri =
                            findRecursive(item.getSubResources(), pid);
                    if (ri != null) {
                        return ri;
                    }
                }
            } // for
        }
        return null;
    }

} // class SimpleEndpointDescription
