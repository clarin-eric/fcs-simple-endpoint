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
import java.util.Collections;
import java.util.List;

import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.Layer;
import eu.clarin.sru.server.fcs.LexField;


/**
 * An abstract base class for implementing endpoint descriptions. It already
 * implements the methods required for capabilities and supported data views.
 *
 * @see EndpointDescription
 *
 */
public abstract class AbstractEndpointDescriptionBase
        implements EndpointDescription {
    protected final int version;
    protected final List<URI> capabilities;
    protected final List<DataView> supportedDataViews;
    protected final List<Layer> supportedLayers;
    protected final List<LexField> supportedLexFields;


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
     *            a list of layers that are supported by this endpoint
     * @param supportedLexFields
     *            a list of lex fields that are supported by this endpoint
     */
    protected AbstractEndpointDescriptionBase(int version,
            List<URI> capabilities, List<DataView> supportedDataViews,
            List<Layer> supportedLayers, List<LexField> supportedLexFields) {
        if ((version != 1) && (version != 2)) {
            throw new IllegalArgumentException("version must be either 1 or 2");
        }
        this.version = version;

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

        if ((supportedLexFields != null) && !supportedLexFields.isEmpty()) {
            for (LexField field : supportedLexFields) {
                if (field == null) {
                    throw new IllegalArgumentException(
                            "supportedLexFields must not contain a 'null' item");
                }
            }
            this.supportedLexFields =
                    Collections.unmodifiableList(supportedLexFields);
        } else {
            this.supportedLexFields = null;
        }
    }


    @Override
    public int getVersion() {
        return version;
    }


    @Override
    public boolean isVersion(int version) {
        return this.version == version;
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


    @Override
    public List<LexField> getSupportedLexFields() {
        return supportedLexFields;
    }

} // abstract class EndpointDescriptionBase
