package eu.clarin.sru.server.fcs.utils;

import java.util.Collections;
import java.util.List;

import eu.clarin.sru.server.fcs.ResourceInfoInventory;
import eu.clarin.sru.server.fcs.ResourceInfo;


/**
 * A very simple resource info inventory that is initialized with a static list
 * of resource info records. Mostly used together with
 * {@link SimpleResourceInfoInventoryParser}, but it is agnostic how the static
 * list of resource info records is generated.
 * 
 * @see ResourceInfoInventory
 * @see SimpleResourceInfoInventoryParser
 */
public class SimpleResourceInfoInventory implements ResourceInfoInventory {
    private final boolean pidCaseSensitive;
    private final List<ResourceInfo> entries;


    /**
     * Constructor.
     *
     * @param entries
     *            a static list of resource info records
     * @param pidCaseSensitive
     *            <code>true</code> if comparison of persistent identifiers
     *            should be performed case-sensitive, <code>false</code> otherwise
     */
    public SimpleResourceInfoInventory(List<ResourceInfo> entries,
            boolean pidCaseSensitive) {
        if (entries == null) {
            throw new NullPointerException("entries == null");
        }
        this.entries = Collections.unmodifiableList(entries);
        this.pidCaseSensitive = pidCaseSensitive;
    }


    @Override
    public void destroy() {
    }


    @Override
    public List<ResourceInfo> getResourceInfoList(String id) {
        if (id == null) {
            throw new NullPointerException("id == null");
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("id is empty");
        }
        if (!pidCaseSensitive) {
            id = id.toLowerCase();
        }
        if (id.equals(PID_ROOT)) {
            return entries;
        } else {
            ResourceInfo ri = findRecursive(entries, id);
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

} // class SimpleResourceInfoInventory
