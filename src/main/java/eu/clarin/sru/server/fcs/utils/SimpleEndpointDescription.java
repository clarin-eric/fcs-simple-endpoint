/**
 * This software is copyright (c) 2013-2022 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.Layer;
import eu.clarin.sru.server.fcs.LexField;
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
     * @param version
     *            version of this endpoint description
     * @param capabilities
     *            a list of capabilities supported by this endpoint
     * @param supportedDataViews
     *            a list of data views that are supported by this endpoint
     * @param supportedLayers
     *            a list of layers supported for Advanced Search by this
     *            endpoint or <code>null</code>
     * @param supportedLexFields
     *            a list of lex fields supported for Lexical Search by this
     *            endpoint or <code>null</code>
     * @param resources
     *            a static list of resource info records
     * @param pidCaseSensitive
     *            <code>true</code> if comparison of persistent identifiers
     *            should be performed case-sensitive, <code>false</code>
     *            otherwise
     */
    public SimpleEndpointDescription(int version, List<URI> capabilities,
            List<DataView> supportedDataViews,
            List<Layer> supportedLayers,
            List<LexField> supportedLexFields,
            List<ResourceInfo> resources,
            boolean pidCaseSensitive) {
        super(version, capabilities, supportedDataViews, supportedLayers, supportedLexFields);

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


    @Override
    public ResourceInfo getResource(String pid) throws SRUException {
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
            throw new IllegalArgumentException("Root PID '"+ PID_ROOT +"' must not be used here!");
        }

        ResourceInfo ri = findRecursive(entries, pid);
        return ri;
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


    public List<String> getResourcePids(String pid) throws SRUException {
        if (pid == null) {
            throw new NullPointerException("pid == null");
        }
        if (pid.isEmpty()) {
            throw new IllegalArgumentException("pid is empty");
        }
        if (!pidCaseSensitive) {
            pid = pid.toLowerCase();
        }

        List<ResourceInfo> items = getResourceList(pid);
        List<String> pids = collectPidsRecursive(items);
        if (!PID_ROOT.equals(pid)) {
            pids.add(0, pid);
        }
        return pids;
    }


    private List<String> collectPidsRecursive(List<ResourceInfo> items) throws SRUException {
        if ((items == null) || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> pids = new ArrayList<>();
        for (ResourceInfo item : items) {
            pids.add(item.getPid());

            if (item.hasSubResources()) {
                List<String> subPids = collectPidsRecursive(item.getSubResources());
                pids.addAll(subPids);
            }
        }
        return pids;
    }

} // class SimpleEndpointDescription
