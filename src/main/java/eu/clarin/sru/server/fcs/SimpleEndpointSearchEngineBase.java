package eu.clarin.sru.server.fcs;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLRelation;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.Modifier;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.SRUConstants;
import eu.clarin.sru.server.SRUDiagnosticList;
import eu.clarin.sru.server.SRUException;
import eu.clarin.sru.server.SRUExplainResult;
import eu.clarin.sru.server.SRURequest;
import eu.clarin.sru.server.SRUScanResultSet;
import eu.clarin.sru.server.SRUSearchEngine;
import eu.clarin.sru.server.SRUServerConfig;
import eu.clarin.sru.server.utils.SRUSearchEngineBase;


/**
 * A base class for implementing a simple search engine to be used as a
 * CLARIN-FCS endpoint.
 *
 */
public abstract class SimpleEndpointSearchEngineBase extends
        SRUSearchEngineBase {
    private static final String X_FCS_ENDPOINT_DESCRIPTION = "x-fcs-endpoint-description";
    private static final String X_CMD_RESOURCE_INFO = "x-cmd-resource-info";
    private static final String ED_NS = "http://clarin.eu/fcs/endpoint-description";
    private static final String ED_PREFIX = "ed";
    private static final int ED_VERSION = 1;
    private static final String FCS_RESOURCE_INFO_NS = "http://clarin.eu/fcs/1.0/resource-info";
    private static final String FCS_SCAN_INDEX_FCS_RESOURCE = "fcs.resource";
    private static final String FCS_SCAN_INDEX_CQL_SERVERCHOICE = "cql.serverChoice";
    private static final String FCS_SCAN_SUPPORTED_RELATION_CQL_1_1 = "scr";
    private static final String FCS_SCAN_SUPPORTED_RELATION_CQL_1_2 = "=";
    private static final String FCS_SUPPORTED_RELATION_EXACT = "exact";
    private static final Logger logger =
            LoggerFactory.getLogger(SimpleEndpointSearchEngineBase.class);
    protected EndpointDescription endpointDescription;


    /**
     * This method should not be overridden. Perform your custom initialization
     * in the {@link #doInit(ServletContext, SRUServerConfig, Map)} method
     * Instead.
     *
     * @see #doInit(ServletContext, SRUServerConfig, Map)
     */
    @Override
    public final void init(ServletContext context, SRUServerConfig config,
            Map<String, String> params) throws SRUConfigException {
        logger.debug("initializing");
        super.init(context, config, params);

        logger.debug("initializing search engine implementation");
        doInit(context, config, params);

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
        /*
         * Check if we got a scan on fcs.resource. If yes, handle it
         * accordingly, otherwise delegate to user-provided implementation.
         */
        final List<ResourceInfo> result =
                translateFcsScanResource(request.getScanClause());
        if (result != null) {
            /*
             * Make sure, we honor the maximumTerms limit, of the client
             * requests it ...
             */
            final int maxTerms
                = ((result.size() > 0) && (request.getMaximumTerms() > 0))
                ? Math.min(result.size(), request.getMaximumTerms())
                : result.size();

            /*
             * Shall we provide extended resource information ... ?
             */
            final boolean provideResourceInfo = parseBoolean(
                    request.getExtraRequestData(X_CMD_RESOURCE_INFO));

            return new SRUScanResultSet(diagnostics) {
                private int idx = -1;

                @Override
                public boolean nextTerm() {
                    return (result != null) && (++idx < maxTerms);
                }


                @Override
                public String getValue() {
                    return result.get(idx).getPid();
                }


                @Override
                public int getNumberOfRecords() {
                    return -1;
                }


                @Override
                public String getDisplayTerm() {
                    return result.get(idx).getTitle("en");
                }


                @Override
                public WhereInList getWhereInList() {
                    return null;
                }


                @Override
                public boolean hasExtraTermData() {
                    return provideResourceInfo;
                }


                @Override
                public void writeExtraTermData(XMLStreamWriter writer)
                        throws XMLStreamException {
                    if (provideResourceInfo) {
                        writeLegacyResourceInfo(writer, result.get(idx));
                    }
                }
            };
        } else {
            return doScan(config, request, diagnostics);
        }
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
     * @param params
     *            additional parameters gathered from the Servlet configuration
     *            and Servlet context.
     * @throws SRUConfigException
     *             if an error occurred
     */
    protected abstract void doInit(ServletContext context,
            SRUServerConfig config, Map<String, String> params)
            throws SRUConfigException;


    /**
     * Destroy the search engine. Override this method for any cleanup the
     * search engine needs to perform upon termination.
     */
    protected void doDestroy() {
    }


    /**
     * Handle a <em>explain</em> operation. The default implementation is a
     * no-op. Override this method, if you want to provide a custom behavior.
     *
     * @see SRUSearchEngine#explain(SRUServerConfig, SRURequest,
     *      SRUDiagnosticList)
     */
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


    private List<ResourceInfo> translateFcsScanResource(CQLNode scanClause)
            throws SRUException {
        if (scanClause instanceof CQLTermNode) {
            final CQLTermNode root = (CQLTermNode) scanClause;
            logger.debug("index = '{}', relation = '{}', term = '{}'",
                    new Object[] { root.getIndex(),
                            root.getRelation().getBase(), root.getTerm() });

            String index = root.getIndex();
            if (FCS_SCAN_INDEX_CQL_SERVERCHOICE.equals(index) &&
                    FCS_SCAN_INDEX_FCS_RESOURCE.equals(root.getTerm())) {
                throw new SRUException(SRUConstants.SRU_UNSUPPORTED_INDEX,
                        "scan operation with 'scanClause' with value " +
                        "'fcs.resource' is deprecated within CLARIN-FCS");
            }
            if (!(FCS_SCAN_INDEX_FCS_RESOURCE.equals(index))) {
                logger.debug("got scan operation on index '{}', bailing ...",
                        index);
                return null;
            }

            logger.warn("scan on 'fcs.resource' for endpoint resource " +
                    "enumeration is deprecated.");

            // only allow "=" relation without any modifiers
            final CQLRelation relationNode = root.getRelation();
            String relation = relationNode.getBase();
            if (!(FCS_SCAN_SUPPORTED_RELATION_CQL_1_1.equals(relation) ||
                    FCS_SCAN_SUPPORTED_RELATION_CQL_1_2.equals(relation) ||
                    FCS_SUPPORTED_RELATION_EXACT.equals(relation))) {
                throw new SRUException(SRUConstants.SRU_UNSUPPORTED_RELATION,
                        relationNode.getBase(), "Relation \"" +
                                relationNode.getBase() +
                                "\" is not supported in scan operation.");
            }
            final List<Modifier> modifiers = relationNode.getModifiers();
            if ((modifiers != null) && !modifiers.isEmpty()) {
                Modifier modifier = modifiers.get(0);
                throw new SRUException(
                        SRUConstants.SRU_UNSUPPORTED_RELATION_MODIFIER,
                        modifier.getValue(), "Relation modifier \"" +
                                modifier.getValue() +
                                "\" is not supported in scan operation.");
            }

            final String term = root.getTerm();
            if ((term == null) || term.isEmpty()) {
                throw new SRUException(SRUConstants.SRU_EMPTY_TERM_UNSUPPORTED,
                        "An empty term is not supported in scan operation.");
            }

            /*
             * generate result: currently we only have a flat hierarchy, so
             * return an empty result on any attempt to do a recursive scan ...
             */
            List<ResourceInfo> results = null;
            if ((FCS_SCAN_INDEX_CQL_SERVERCHOICE.equals(index) &&
                    FCS_SCAN_INDEX_FCS_RESOURCE.equals(term)) ||
                    (FCS_SCAN_INDEX_FCS_RESOURCE.equals(index))) {
                results = endpointDescription.getResourceList(term);
            }
            if ((results == null) || results.isEmpty()) {
                return Collections.emptyList();
            } else {
                return results;
            }
        } else {
            throw new SRUException(SRUConstants.SRU_QUERY_FEATURE_UNSUPPORTED,
                    "Scan clause too complex.");
        }
    }


    private void writeEndpointDescription(XMLStreamWriter writer)
            throws XMLStreamException {
        writer.setPrefix(ED_PREFIX, ED_NS);
        writer.writeStartElement(ED_NS, "EndpointDescription");
        writer.writeNamespace(ED_PREFIX, ED_NS);
        writer.writeAttribute("version", Integer.toString(ED_VERSION));

        // capabilities
        writer.writeStartElement(ED_NS, "Capabilities");
        for (URI capability : endpointDescription.getCapabilities()) {
            writer.writeStartElement(ED_NS, "Capability");
            writer.writeCharacters(capability.toString());
            writer.writeEndElement(); // "Capability" element
        }
        writer.writeEndElement(); // "Capabilities" element

        // supported data views
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

        try {
            // resources
            List<ResourceInfo> resources =
                    endpointDescription.getResourceList(
                            EndpointDescription.PID_ROOT);
            writeResourceInfos(writer, resources);
        } catch (SRUException e) {
            throw new XMLStreamException("error retriving top-level resources",
                    e);
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


    private void writeLegacyResourceInfo(XMLStreamWriter writer,
            ResourceInfo resourceInfo) throws XMLStreamException {
        writer.setDefaultNamespace(FCS_RESOURCE_INFO_NS);
        writer.writeStartElement(FCS_RESOURCE_INFO_NS, "ResourceInfo");
        writer.writeDefaultNamespace(FCS_RESOURCE_INFO_NS);

        // title
        final Map<String, String> title = resourceInfo.getTitle();
        for (Map.Entry<String, String> i : title.entrySet()) {
            writer.setPrefix(XMLConstants.XML_NS_PREFIX,
                    XMLConstants.XML_NS_URI);
            writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Title");
            writer.writeAttribute(XMLConstants.XML_NS_URI, "lang", i.getKey());
            writer.writeCharacters(i.getValue());
            writer.writeEndElement(); // "title" element
        }

        // description
        final Map<String, String> description = resourceInfo.getDescription();
        if (description != null) {
            for (Map.Entry<String, String> i : description.entrySet()) {
                writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Description");
                writer.writeAttribute(XMLConstants.XML_NS_URI, "lang",
                        i.getKey());
                writer.writeCharacters(i.getValue());
                writer.writeEndElement(); // "Description" element
            }
        }

        // landing page
        final String landingPageURI = resourceInfo.getLandingPageURI();
        if (landingPageURI != null) {
            writer.writeStartElement(FCS_RESOURCE_INFO_NS, "LandingPageURI");
            writer.writeCharacters(landingPageURI);
            writer.writeEndElement(); // "LandingPageURI" element
        }

        // languages
        final List<String> languages = resourceInfo.getLanguages();
        writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Languages");
        for (String i : languages) {
            writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Language");
            writer.writeCharacters(i);
            writer.writeEndElement(); // "Language" element

        }
        writer.writeEndElement(); // "Languages" element
        writer.writeEndElement(); // "ResourceInfo" element
    }


//             final boolean defaultNS = ((prefix == null) || prefix.isEmpty());
//            if (writeNS) {
//                 if (defaultNS) {
//                     writer.setDefaultNamespace(FCS_RESOURCE_INFO_NS);
//                } else {
//                    writer.setPrefix(prefix, FCS_RESOURCE_INFO_NS);
//                }
//            }
//            writer.writeStartElement(FCS_RESOURCE_INFO_NS, "ResourceInfo");
//            if (writeNS) {
//                if (defaultNS) {
//                    writer.writeDefaultNamespace(FCS_RESOURCE_INFO_NS);
//                 } else {
//                    writer.writeNamespace(prefix, FCS_RESOURCE_INFO_NS);
//                 }
//          } )
//}

//    public static void XwriteResourceInfo(XMLStreamWriter writer, String prefix,
//            ResourceInfo resourceInfo) throws XMLStreamException {
//        doWriteResourceInfo(writer, prefix, resourceInfo, true, false);
//    }
//
//
//    private static void XdoWriteResourceInfo(XMLStreamWriter writer,
//            String prefix, ResourceInfo resourceInfo, boolean writeNS,
//            boolean recursive) throws XMLStreamException {
//
//        if (writer == null) {
//            throw new NullPointerException("writer == null");
//        }
//        if (resourceInfo == null) {
//            throw new NullPointerException("resourceInfo == null");
//        }
//
//        final boolean defaultNS = ((prefix == null) || prefix.isEmpty());
//        if (writeNS) {
//            if (defaultNS) {
//                writer.setDefaultNamespace(FCS_RESOURCE_INFO_NS);
//            } else {
//                writer.setPrefix(prefix, FCS_RESOURCE_INFO_NS);
//            }
//        }
//        writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Resource");
//        if (writeNS) {
//            if (defaultNS) {
//                writer.writeDefaultNamespace(FCS_RESOURCE_INFO_NS);
//            } else {
//                writer.writeNamespace(prefix, FCS_RESOURCE_INFO_NS);
//            }
//        }
//        if (recursive) {
//            /*
//             * HACK: only output @pid for recursive (= explain) requests.
//             * This should be revisited, if we decide to go for the explain
//             * style enumeration of resources.
//             */
//            writer.writeAttribute("pid", resourceInfo.getPid());
//        }
//        if (resourceInfo.hasSubResources()) {
//            writer.writeAttribute("hasSubResources", "true");
//        }
//
//
//        if (recursive && resourceInfo.hasSubResources()) {
//            writer.writeStartElement(FCS_RESOURCE_INFO_NS,
//                    "ResourceInfoCollection");
//            for (ResourceInfo r : resourceInfo.getSubResources()) {
//                doWriteResourceInfo(writer, prefix, r, writeNS, recursive);
//            }
//            writer.writeEndElement(); // "ResourceCollection" element
//        }
//        writer.writeEndElement(); // "ResourceInfo" element
//    }

} // class SimpleEndpointSearchEngineBase
