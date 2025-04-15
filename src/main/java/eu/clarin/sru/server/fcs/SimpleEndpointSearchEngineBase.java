/**
 * This software is copyright (c) 2013-2025 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *  - Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * @copyright Saxon Academy of Sciences and Humanities in Leipzig (https://www.saw-leipzig.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server.fcs;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLTermNode;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;

import eu.clarin.sru.server.SRUAuthenticationInfoProvider;
import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUExplainResult;
import eu.clarin.sru.server.SRUQueryParserRegistry;
import eu.clarin.sru.server.SRURequest;
import eu.clarin.sru.server.SRUScanResultSet;
import eu.clarin.sru.server.SRUSearchEngine;
import eu.clarin.sru.server.SRUServer;
import eu.clarin.sru.server.SRUServerConfig;
import eu.clarin.sru.server.fcs.Font.DownloadUrl;
import eu.clarin.sru.server.fcs.ResourceInfo.AvailabilityRestriction;
import eu.clarin.sru.server.fcs.utils.AuthenticationProvider;
import eu.clarin.sru.server.utils.SRUAuthenticationInfoProviderFactory;
import eu.clarin.sru.server.utils.SRUSearchEngineBase;


/**
 * A base class for implementing a simple search engine to be used as a
 * CLARIN-FCS endpoint.
 *
 */
public abstract class SimpleEndpointSearchEngineBase extends
        SRUSearchEngineBase implements SRUAuthenticationInfoProviderFactory {
    public static final String FCS_AUTHENTICATION_ENABLE_PARAM =
            "eu.clarin.sru.server.fcs.authentication.enable";
    public static final String FCS_AUTHENTICATION_AUDIENCE_PARAM =
            "eu.clarin.sru.server.fcs.authentication.audience";
    public static final String FCS_AUTHENTICATION_IGNORE_ISSUEDAT_PARAM =
            "eu.clarin.sru.server.fcs.authentication.ignoreIssuedAt";
    public static final String FCS_AUTHENTICATION_ACCEPT_ISSUEDAT_PARAM =
            "eu.clarin.sru.server.fcs.authentication.acceptIssuedAt";
    public static final String FCS_AUTHENTICATION_ACCEPT_EXPIRESAT_PARAM =
            "eu.clarin.sru.server.fcs.authentication.acceptExpiresAt";
    public static final String FCS_AUTHENTICATION_ACCEPT_NOTBEFORE_PARAM =
            "eu.clarin.sru.server.fcs.authentication.acceptNotBefore";
    public static final String FCS_AUTHENTICATION_PUBLIC_KEY_PARAM_PREFIX =
            "eu.clarin.sru.server.fcs.authentication.key.";
    public static final String FCS_AUTHENTICATION_PUBLIC_JWKS_PARAM_PREFIX =
            "eu.clarin.sru.server.fcs.authentication.jwks.";
    public static final String FCS_AUTHENTICATION_PUBLIC_ISSUER_PARAM_PREFIX =
            "eu.clarin.sru.server.fcs.authentication.issuer.";
    private static final String RESOURCE_URI_PREFIX = "resource:";
    private static final String X_FCS_ENDPOINT_DESCRIPTION =
            "x-fcs-endpoint-description";
    private static final String ED_NS =
            "http://clarin.eu/fcs/endpoint-description";
    private static final String ED_PREFIX = "ed";
    private static final Logger logger =
            LoggerFactory.getLogger(SimpleEndpointSearchEngineBase.class);
    protected EndpointDescription endpointDescription;


    /**
     * This method should not be overridden. Perform your custom initialization
     * in the
     * {@link #doInit(ServletContext, SRUServerConfig, eu.clarin.sru.server.SRUQueryParserRegistry.Builder, Map)}
     * method instead.
     *
     * @see #doInit(ServletContext, SRUServerConfig,
     *      eu.clarin.sru.server.SRUQueryParserRegistry.Builder, Map)
     */
    @Override
    public final void init(ServletContext context,
            SRUServerConfig config,
            SRUQueryParserRegistry.Builder parserReqistryBuilder,
            Map<String, String> params) throws SRUConfigException {
        logger.debug("initializing");
        super.init(context, config, parserReqistryBuilder, params);

        parserReqistryBuilder.register(new FCSQueryParser());
        parserReqistryBuilder.register(new LexCQLQueryParser());

        logger.debug("initializing search engine implementation");
        doInit(context, config, parserReqistryBuilder, params);

        logger.debug("initizalizing endpoint description");
        this.endpointDescription =
                createEndpointDescription(context, config, params);
        if (this.endpointDescription == null) {
            logger.error("SimpleEndpointSearchEngineBase implementation " +
                    "error: createEndpointDescription() returned null");
            throw new SRUConfigException("createEndpointDescription() " +
                    "returned no valid implementation of an EndpointDescription");
        }
    }


    /**
     * This method should not be overridden. Perform you custom cleanup in the
     * {@link #doDestroy()} method.
     *
     * @see #doDestroy()
     */
    @Override
    public final void destroy() {
        logger.debug("performing cleanup of endpoint description");
        endpointDescription.destroy();
        logger.debug("performing cleanup of search engine");
        doDestroy();
        super.destroy();
    }


    @Override
    public SRUAuthenticationInfoProvider createAuthenticationInfoProvider(
            ServletContext context, Map<String, String> params)
            throws SRUConfigException {
        String enableAuthentication = params.get(FCS_AUTHENTICATION_ENABLE_PARAM);
        if (enableAuthentication != null) {
            if (parseBoolean(enableAuthentication)) {
                logger.debug("enabling authentication");
                AuthenticationProvider.Builder builder =
                        AuthenticationProvider.Builder.create();

                String audience = params.get(FCS_AUTHENTICATION_AUDIENCE_PARAM);
                if (audience != null) {
                    String[] values = audience.split("\\s*,\\s*");
                    if (values != null) {
                        for (String value : values) {
                            logger.debug("adding audience: {}", value);
                            builder.withAudience(value);
                        }
                    } else {
                        logger.debug("adding audience: {}", audience);
                        builder.withAudience(audience);
                    }
                }

                boolean ignoreIssuedAt = parseBoolean(
                        params.get(FCS_AUTHENTICATION_IGNORE_ISSUEDAT_PARAM));
                if (ignoreIssuedAt) {
                    logger.debug("will not verify 'iat' claim");
                    builder.withIgnoreIssuedAt();
                } else {
                    long issuedAtLeeway = parseLong(
                            params.get(FCS_AUTHENTICATION_ACCEPT_ISSUEDAT_PARAM), -1);
                    if (issuedAtLeeway > 0) {
                        logger.debug("allowing {} seconds leeway for 'iat' claim", issuedAtLeeway);
                        builder.withIssuedAt(issuedAtLeeway);
                    }
                }
                long expiresAtLeeway = parseLong(
                        params.get(FCS_AUTHENTICATION_ACCEPT_EXPIRESAT_PARAM), -1);
                if (expiresAtLeeway > 0) {
                    logger.debug("allowing {} seconds leeway for 'exp' claim", expiresAtLeeway);
                    builder.withExpiresAt(expiresAtLeeway);
                }

                long notBeforeLeeway = parseLong(
                        params.get(FCS_AUTHENTICATION_ACCEPT_NOTBEFORE_PARAM), -1);
                if (notBeforeLeeway > 0) {
                    logger.debug("allowing {} seconds leeway for 'nbf' claim", expiresAtLeeway);
                    builder.withNotBefore(notBeforeLeeway);
                }

                // load keys
                for (Entry<String, String> entry : params.entrySet()) {
                    if (entry.getKey().startsWith(FCS_AUTHENTICATION_PUBLIC_KEY_PARAM_PREFIX)) {
                        // PEM public key to be loaded from a file (either embedded or on local file system)
                        String keyId = entry.getKey().substring(FCS_AUTHENTICATION_PUBLIC_KEY_PARAM_PREFIX.length()).trim();
                        if (keyId.isEmpty()) {
                            throw new SRUConfigException("init-parameter: '" + entry.getKey() + "' is invalid: keyId is empty!");
                        }
                        String keyFileName = entry.getValue();
                        logger.debug("keyId = {}, keyFile = {}", keyId, keyFileName);
                        String issuer = params.get(FCS_AUTHENTICATION_PUBLIC_ISSUER_PARAM_PREFIX + keyId);
                        if (issuer != null) {
                            logger.debug("keyId = {} with issuer = {}", keyId, issuer);
                        }
                        if (keyFileName.regionMatches(0, RESOURCE_URI_PREFIX, 0, RESOURCE_URI_PREFIX.length())) {
                            String path = keyFileName.substring(RESOURCE_URI_PREFIX.length());
                            logger.debug("loading key '{}' from resource '{}'", keyId, keyFileName);
                            InputStream in = context.getResourceAsStream(path);
                            builder.withPublicKey(keyId, in, issuer);
                        } else {
                            logger.debug("loading key '{}' from file '{}'", keyId, keyFileName);
                            builder.withPublicKey(keyId, new File(keyFileName), issuer);
                        }
                    } else if (entry.getKey().startsWith(FCS_AUTHENTICATION_PUBLIC_JWKS_PARAM_PREFIX)) {
                        // JWK(S) to be loaded from a URL/domain
                        // TODO: encode keyId (for JWKS) in parameter name or as separate related init-param?
                        String keyId = entry.getKey().substring(FCS_AUTHENTICATION_PUBLIC_JWKS_PARAM_PREFIX.length()).trim();
                        if (keyId.isEmpty()) {
                            throw new SRUConfigException("init-parameter: '" + entry.getKey() + "' is invalid: keyId is empty!");
                        }
                        String keyUrl = entry.getValue();
                        logger.debug("keyId = {}, url = {}", keyId, keyUrl);
                        String issuer = params.get(FCS_AUTHENTICATION_PUBLIC_ISSUER_PARAM_PREFIX + keyId);
                        if (issuer != null) {
                            logger.debug("keyId = {} with issuer = {}", keyId, issuer);
                        }
                        UrlJwkProvider provider = new UrlJwkProvider(keyUrl);
                        try {
                            Jwk jwk = provider.get(null); // assume only a single key at this JWKS endpoint
                            PublicKey publicKey = jwk.getPublicKey();
                            if (!(publicKey instanceof RSAPublicKey)) {
                                throw new SRUConfigException("JWK is not a RSA public key!");
                            }
                            builder.withPublicKey(keyId, (RSAPublicKey) publicKey, issuer);
                        } catch (JwkException e) {
                            throw new SRUConfigException("Failed to load JWK from JWKS endpoint.", e);
                        }
                    }
                }
                AuthenticationProvider authenticationProvider = builder.build();
                if (authenticationProvider.getKeyCount() == 0) {
                    logger.warn("No keys configured, all well-formed tokens will be accepted. Make sure, youn know what you are doing!");
                }
                return authenticationProvider;
            } else {
                logger.debug("explictly disable authentication");
            }
        }
        return null;
    }


    @Override
    public final SRUExplainResult explain(SRUServerConfig config,
            SRURequest request, SRUDiagnosticList diagnostics)
            throws SRUException {

        final boolean provideEndpointDescription =
                parseBoolean(request.getExtraRequestData(
                        X_FCS_ENDPOINT_DESCRIPTION));

        if (provideEndpointDescription) {
            return new SRUExplainResult(diagnostics) {
                @Override
                public boolean hasExtraResponseData() {
                    return provideEndpointDescription;
                }


                @Override
                public void writeExtraResponseData(XMLStreamWriter writer)
                        throws XMLStreamException {
                    writeEndpointDescription(writer);
                }
            };
        } else {
            return null;
        }
    }


    /**
     * Handle a <em>scan</em> operation. This implementation provides support to
     * CLARIN FCS resource enumeration. If you want to provide custom scan
     * behavior for a different index, override the
     * {@link #doScan(SRUServerConfig, SRURequest, SRUDiagnosticList)} method.
     *
     * @see #doScan(SRUServerConfig, SRURequest, SRUDiagnosticList)
     */
    @Override
    public final SRUScanResultSet scan(SRUServerConfig config,
            SRURequest request, SRUDiagnosticList diagnostics)
            throws SRUException {
        return doScan(config, request, diagnostics);
    }


    protected abstract EndpointDescription createEndpointDescription(
            ServletContext context, SRUServerConfig config,
            Map<String, String> params) throws SRUConfigException;


    /**
     * Initialize the search engine. This initialization should be tailed
     * towards your environment and needs.
     *
     * @param context
     *            the {@link ServletContext} for the Servlet
     * @param config
     *            the {@link SRUServerConfig} object for this search engine
     * @param queryParsersBuilder
     *            the {@link SRUQueryParserRegistry.Builder} object to be used
     *            for this search engine. Use to register additional query
     *            parsers with the {@link SRUServer}.
     * @param params
     *            additional parameters gathered from the Servlet configuration
     *            and Servlet context.
     * @throws SRUConfigException
     *             if an error occurred
     */
    protected abstract void doInit(ServletContext context,
            SRUServerConfig config,
            SRUQueryParserRegistry.Builder queryParsersBuilder,
            Map<String, String> params) throws SRUConfigException;


    /**
     * Destroy the search engine. Override this method for any cleanup the
     * search engine needs to perform upon termination.
     */
    protected void doDestroy() {
    }


    /**
     * Handle a <em>scan</em> operation. The default implementation is a no-op.
     * Override this method, if you want to provide a custom behavior.
     *
     * @param config
     *            the <code>SRUEndpointConfig</code> object that contains the
     *            endpoint configuration
     * @param request
     *            the <code>SRURequest</code> object that contains the request
     *            made to the endpoint
     * @param diagnostics
     *            the <code>SRUDiagnosticList</code> object for storing
     *            non-fatal diagnostics
     * @return a <code>SRUScanResultSet</code> object or <code>null</code> if
     *         this operation is not supported by this search engine
     * @throws SRUException
     *             if an fatal error occurred
     * @see SRUSearchEngine#scan(SRUServerConfig, SRURequest, SRUDiagnosticList)
     * @deprecated override
     *             {@link #scan(SRUServerConfig, SRURequest, SRUDiagnosticList)}
     */
    @Deprecated
    protected SRUScanResultSet doScan(SRUServerConfig config,
            SRURequest request, SRUDiagnosticList diagnostics)
            throws SRUException {
        final CQLNode scanClause = request.getScanClause();
        if (scanClause instanceof CQLTermNode) {
            final CQLTermNode root = (CQLTermNode) scanClause;
            final String index = root.getIndex();
            throw new SRUException(SRUConstants.SRU_UNSUPPORTED_INDEX, index,
                    "scan operation on index '" + index + "' is not supported");
        } else {
            throw new SRUException(SRUConstants.SRU_QUERY_FEATURE_UNSUPPORTED,
                    "Scan clause too complex.");
        }
    }


    /**
     * Convince method for parsing a string to boolean. Values <code>1</code>,
     * <code>true</code>, <code>yes</code> yield a <em>true</em> boolean value
     * as a result, all others (including <code>null</code>) a <em>false</em>
     * boolean value.
     *
     * @param value
     *            the string to parse
     * @return <code>true</code> if the supplied string was considered something
     *         representing a <em>true</em> boolean value, <code>false</code>
     *         otherwise
     */
    protected static boolean parseBoolean(String value) {
        if (value != null) {
            return value.equals("1") || Boolean.parseBoolean(value);
        }
        return false;
    }


    private long parseLong(String value, long defaultValue) throws SRUConfigException {
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new SRUConfigException("invalid long value");
            }
        }
        return defaultValue;
    }


    private void writeEndpointDescription(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.setPrefix(ED_PREFIX, ED_NS);
        writer.writeStartElement(ED_NS, "EndpointDescription");
        writer.writeNamespace(ED_PREFIX, ED_NS);
        writer.writeAttribute("version",
                Integer.toString(endpointDescription.getVersion()));

        // Capabilities
        writer.writeStartElement(ED_NS, "Capabilities");
        for (URI capability : endpointDescription.getCapabilities()) {
            writer.writeStartElement(ED_NS, "Capability");
            writer.writeCharacters(capability.toString());
            writer.writeEndElement(); // "Capability" element
        }
        writer.writeEndElement(); // "Capabilities" element

        // SupportedDataViews
        writer.writeStartElement(ED_NS, "SupportedDataViews");
        for (DataView dataView : endpointDescription.getSupportedDataViews()) {
            writer.writeStartElement(ED_NS, "SupportedDataView");
            writer.writeAttribute("id", dataView.getIdentifier());
            String s;
            switch (dataView.getDeliveryPolicy()) {
            case SEND_BY_DEFAULT:
                s = "send-by-default";
                break;
            case NEED_TO_REQUEST:
                s = "need-to-request";
                break;
            default:
                throw new XMLStreamException(
                        "invalid value for payload delivery policy: " +
                                dataView.getDeliveryPolicy());
            } // switch
            writer.writeAttribute("delivery-policy", s);
            writer.writeCharacters(dataView.getMimeType());
            writer.writeEndElement(); // "SupportedDataView" element
        }
        writer.writeEndElement(); // "SupportedDataViews" element

        if (endpointDescription.isVersion(EndpointDescription.VERSION_2)) {
            // SupportedLayers
            final List<Layer> layers = endpointDescription.getSupportedLayers();
            if (layers != null) {
                writer.writeStartElement(ED_NS, "SupportedLayers");
                for (Layer layer : layers) {
                    writer.writeStartElement(ED_NS, "SupportedLayer");
                    writer.writeAttribute("id", layer.getId());
                    writer.writeAttribute("result-id",
                            layer.getResultId().toString());
                    if (layer.getContentEncoding() ==
                            Layer.ContentEncoding.EMPTY) {
                        writer.writeAttribute("type", "empty");
                    }
                    if (layer.getQualifier() != null) {
                        writer.writeAttribute("qualifier",
                                layer.getQualifier());
                    }
                    if (layer.getAltValueInfo() != null) {
                        writer.writeAttribute("alt-value-info",
                                layer.getAltValueInfo());
                        if (layer.getAltValueInfoURI() != null) {
                            writer.writeAttribute("alt-value-info-uri",
                                    layer.getAltValueInfoURI().toString());
                        }
                    }
                    writer.writeCharacters(layer.getType());
                    writer.writeEndElement(); // "SupportedLayer" element
                }
                writer.writeEndElement(); // "SupportedLayers" element
            }

            // SupportedLexFields
            final List<LexField> fields = endpointDescription.getSupportedLexFields();
            if (fields != null) {
                writer.writeStartElement(ED_NS, "SupportedLexFields");
                for (LexField field : fields) {
                    writer.writeStartElement(ED_NS, "SupportedLexField");
                    writer.writeAttribute("id", field.getId());
                    writer.writeCharacters(field.getType());
                    writer.writeEndElement(); // "SupportedLexField" element
                }
                writer.writeEndElement(); // "SupportedLexFields" element
            }

            // RequiredFonts
            final List<Font> fonts = endpointDescription.getRequiredFonts();
            if (fonts != null) {
                writer.writeStartElement(ED_NS, "RequiredFonts");
                for (Font font : fonts) {
                    writer.writeStartElement(ED_NS, "RequiredFont");
                    writer.writeAttribute("id", font.getId());
                    writer.writeAttribute("name", font.getName());
                    if (font.getDescriptionUrl() != null) {
                        writer.writeAttribute("description-url", font.getDescriptionUrl().toString());
                    }
                    writer.writeAttribute("license", font.getLicense());
                    if (font.getLicenseURLs() != null && !font.getLicenseURLs().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (URI licenseUri : font.getLicenseURLs()) {
                            if (sb.length() > 0) {
                                sb.append(" ");
                            }
                            sb.append(licenseUri.toString()); // TODO: do we need to check for encoding spaces?!
                        }
                        writer.writeAttribute("license-urls", sb.toString());
                    }
                    for (DownloadUrl url : font.getDownloadURLs()) {
                        writer.writeStartElement(ED_NS, "DownloadURL");
                        if (url.getVariant() != null) {
                            writer.writeAttribute("variant", url.getVariant());
                        }
                        if (url.getFontFamily() != null) {
                            writer.writeAttribute("font-family", url.getFontFamily());
                        }
                        writer.writeCharacters(url.getURL().toString());
                        writer.writeEndElement(); // "DownloadURL" element
                    }
                    writer.writeEndElement(); // "RequiredFont" element
                }
                writer.writeEndElement(); // "RequiredFonts" element
            }
        }

        // Resources
        try {
            List<ResourceInfo> resources =
                    endpointDescription.getResourceList(
                            EndpointDescription.PID_ROOT);
            writeResourceInfos(writer, resources);
        } catch (SRUException e) {
            throw new XMLStreamException(
                    "error retriving top-level resources", e);
        }
        writer.writeEndElement(); // "EndpointDescription" element
    }


    private void writeResourceInfos(XMLStreamWriter writer,
            List<ResourceInfo> resources) throws XMLStreamException {
        if (resources == null) {
            throw new NullPointerException("resources == null");
        }
        if (!resources.isEmpty()) {
            writer.writeStartElement(ED_NS, "Resources");

            for (ResourceInfo resource : resources) {
                writer.writeStartElement(ED_NS, "Resource");
                writer.writeAttribute("pid", resource.getPid());

                // title
                final Map<String, String> title = resource.getTitle();
                for (Map.Entry<String, String> i : title.entrySet()) {
                    writer.setPrefix(XMLConstants.XML_NS_PREFIX,
                            XMLConstants.XML_NS_URI);
                    writer.writeStartElement(ED_NS, "Title");
                    writer.writeAttribute(XMLConstants.XML_NS_URI, "lang", i.getKey());
                    writer.writeCharacters(i.getValue());
                    writer.writeEndElement(); // "title" element
                }

                // description
                final Map<String, String> description = resource.getDescription();
                if (description != null) {
                    for (Map.Entry<String, String> i : description.entrySet()) {
                        writer.writeStartElement(ED_NS, "Description");
                        writer.writeAttribute(XMLConstants.XML_NS_URI, "lang",
                                i.getKey());
                        writer.writeCharacters(i.getValue());
                        writer.writeEndElement(); // "Description" element
                    }
                }

                // institution
                final Map<String, String> institution = resource.getInstitution();
                if (institution != null) {
                    for (Map.Entry<String, String> i : institution.entrySet()) {
                        writer.writeStartElement(ED_NS, "Institution");
                        writer.writeAttribute(XMLConstants.XML_NS_URI, "lang",
                                i.getKey());
                        writer.writeCharacters(i.getValue());
                        writer.writeEndElement(); // "Institution" element
                    }
                }

                // landing page
                final String landingPageURI = resource.getLandingPageURI();
                if (landingPageURI != null) {
                    writer.writeStartElement(ED_NS, "LandingPageURI");
                    writer.writeCharacters(landingPageURI);
                    writer.writeEndElement(); // "LandingPageURI" element
                }

                // languages
                final List<String> languages = resource.getLanguages();
                writer.writeStartElement(ED_NS, "Languages");
                for (String i : languages) {
                    writer.writeStartElement(ED_NS, "Language");
                    writer.writeCharacters(i);
                    writer.writeEndElement(); // "Language" element

                }
                writer.writeEndElement(); // "Languages" element

                if (resource.hasAvailabilityRestriction()) {
                    final AvailabilityRestriction availabilityRestriction = resource.getAvailabilityRestriction();
                    String s;
                    switch (availabilityRestriction) {
                        case AUTH_ONLY:
                            s = "authOnly";
                            break;
                        case PERSONAL_IDENTIFIER:
                            s = "personalIdentifier";
                            break;
                        default:
                            throw new XMLStreamException(
                                    "invalid value for payload availability restriction: " +
                                            availabilityRestriction);
                    } // switch
                    writer.writeStartElement(ED_NS, "AvailabilityRestriction");
                    writer.writeCharacters(s);
                    writer.writeEndElement(); // "AvailabilityRestriction" element
                }

                // available data views
                StringBuilder sb = new StringBuilder();
                for (DataView dataview : resource.getAvailableDataViews()) {
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(dataview.getIdentifier());
                }
                writer.writeEmptyElement(ED_NS, "AvailableDataViews");
                writer.writeAttribute("ref", sb.toString());

                if (endpointDescription.isVersion(
                        EndpointDescription.VERSION_2)) {
                    final List<Layer> layers = resource.getAvailableLayers();
                    if (layers != null) {
                        sb = new StringBuilder();
                        for (Layer layer : layers) {
                            if (sb.length() > 0) {
                                sb.append(" ");
                            }
                            sb.append(layer.getId());
                        }
                        writer.writeEmptyElement(ED_NS, "AvailableLayers");
                        writer.writeAttribute("ref", sb.toString());
                    }

                    final List<LexField> fields = resource.getAvailableLexFields();
                    if (fields != null) {
                        sb = new StringBuilder();
                        for (LexField field : fields) {
                            if (sb.length() > 0) {
                                sb.append(" ");
                            }
                            sb.append(field.getId());
                        }
                        writer.writeEmptyElement(ED_NS, "AvailableLexFields");
                        writer.writeAttribute("ref", sb.toString());
                    }

                    final List<Font> fonts = resource.getRequiredFonts();
                    if (fonts != null) {
                        sb = new StringBuilder();
                        for (Font font : fonts) {
                            if (sb.length() > 0) {
                                sb.append(" ");
                            }
                            sb.append(font.getId());
                        }
                        writer.writeEmptyElement(ED_NS, "RequiredFonts");
                        writer.writeAttribute("ref", sb.toString());
                    }
                }

                // child resources
                List<ResourceInfo> subs = resource.getSubResources();
                if ((subs != null) && !subs.isEmpty()) {
                    writeResourceInfos(writer, subs);
                }

                writer.writeEndElement(); // "Resource" element
            }
            writer.writeEndElement(); // "Resources" element
        }
    }

} // class SimpleEndpointSearchEngineBase
