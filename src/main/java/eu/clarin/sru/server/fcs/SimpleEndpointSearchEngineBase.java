package eu.clarin.sru.server.fcs;

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
 * A base class for implementing a simple search engine to be used as a CLARIN
 * FCS endpoint.
 *
 */
public abstract class SimpleEndpointSearchEngineBase extends
        SRUSearchEngineBase {
    private static final String FCS_RESOURCE_INFO_NS =
            "http://clarin.eu/fcs/1.0/resource-info";
    private static final String X_CMD_RESOURCE_INFO = "x-cmd-resource-info";
    private static final String FCS_SCAN_INDEX_FCS_RESOURCE = "fcs.resource";
    private static final String FCS_SCAN_INDEX_CQL_SERVERCHOICE = "cql.serverChoice";
    private static final String FCS_SCAN_SUPPORTED_RELATION_CQL_1_1 = "scr";
    private static final String FCS_SCAN_SUPPORTED_RELATION_CQL_1_2 = "=";
    private static final String FCS_SUPPORTED_RELATION_EXACT = "exact";
    private static final Logger logger =
            LoggerFactory.getLogger(SimpleEndpointSearchEngineBase.class);
    protected ResourceInfoInventory resourceInfoInventory;


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

        logger.debug("initizalizing resource info inventory");
        this.resourceInfoInventory = createResourceInfoInventory(context, config, params);
        if (this.resourceInfoInventory == null) {
            logger.error("ClarinFCSSearchEngineBase implementation error: " +
                    "initResourceCatalog() returned null");
            throw new SRUConfigException("initResourceCatalog() returned no " +
                    "valid implementation of a ResourceCatalog");
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
        logger.debug("performing cleanup of resource info inventory");
        resourceInfoInventory.destroy();
        logger.debug("performing cleanup of search engine");
        doDestroy();
        super.destroy();
    }


    @Override
    public final SRUExplainResult explain(SRUServerConfig config,
            SRURequest request, SRUDiagnosticList diagnostics)
            throws SRUException {
        final boolean provideResourceInfo =
                parseBoolean(request.getExtraRequestData(X_CMD_RESOURCE_INFO));
        if (provideResourceInfo) {
            final List<ResourceInfo> resourceInfoList =
                    resourceInfoInventory.getResourceInfoList(
                            ResourceInfoInventory.PID_ROOT);
            return new SRUExplainResult(diagnostics) {

                @Override
                public boolean hasExtraResponseData() {
                    return provideResourceInfo;
                }

                @Override
                public void writeExtraResponseData(XMLStreamWriter writer)
                        throws XMLStreamException {
                    writeFullResourceInfo(writer, null, resourceInfoList);
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
                    return result.get(idx).getResourceCount();
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
                        writeResourceInfo(writer, null, result.get(idx));
                    }
                }
            };
        } else {
            return doScan(config, request, diagnostics);
        }
    }



    /**
     * Create the resource info inventory to be used with this endpoint.
     * Implement this method to provide an implementation of a
     * {@link ResourceInfoInventory} that is tailored towards your environment
     * and needs.
     *
     * @param context
     *            the {@link ServletContext} for the Servlet
     * @param config
     *            the {@link SRUServerConfig} object for this search engine
     * @param params
     *            additional parameters gathered from the Servlet configuration
     *            and Servlet context.
     * @return an instance of a {@link ResourceInfoInventory} used by this
     *         search engine
     * @throws SRUConfigException
     *             if an error occurred
     */
    protected abstract ResourceInfoInventory createResourceInfoInventory(
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
                results = resourceInfoInventory.getResourceInfoList(term);
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


    private static void writeFullResourceInfo(XMLStreamWriter writer,
            String prefix, List<ResourceInfo> resourceInfoList)
            throws XMLStreamException {
        if (resourceInfoList == null) {
            throw new NullPointerException("resourceInfoList == null");
        }
        if (!resourceInfoList.isEmpty()) {
            final boolean defaultNS = ((prefix == null) || prefix.isEmpty());
            if (defaultNS) {
                writer.setDefaultNamespace(FCS_RESOURCE_INFO_NS);
            } else {
                writer.setPrefix(prefix, FCS_RESOURCE_INFO_NS);
            }
            writer.writeStartElement(FCS_RESOURCE_INFO_NS, "ResourceCollection");
            if (defaultNS) {
                writer.writeDefaultNamespace(FCS_RESOURCE_INFO_NS);
            } else {
                writer.writeNamespace(prefix, FCS_RESOURCE_INFO_NS);
            }
            for (ResourceInfo resourceInfo : resourceInfoList) {
                doWriteResourceInfo(writer, prefix, resourceInfo, false, true);
            }
            writer.writeEndElement(); // "ResourceCollection" element
        }
    }


    private static void writeResourceInfo(XMLStreamWriter writer, String prefix,
            ResourceInfo resourceInfo) throws XMLStreamException {
        if (resourceInfo == null) {
            throw new NullPointerException("resourceInfo == null");
        }
        doWriteResourceInfo(writer, prefix, resourceInfo, true, false);
    }


    private static void doWriteResourceInfo(XMLStreamWriter writer,
            String prefix, ResourceInfo resourceInfo, boolean writeNS,
            boolean recursive) throws XMLStreamException {
        final boolean defaultNS = ((prefix == null) || prefix.isEmpty());
        if (writeNS) {
            if (defaultNS) {
                writer.setDefaultNamespace(FCS_RESOURCE_INFO_NS);
            } else {
                writer.setPrefix(prefix, FCS_RESOURCE_INFO_NS);
            }
        }
        writer.writeStartElement(FCS_RESOURCE_INFO_NS, "ResourceInfo");
        if (writeNS) {
            if (defaultNS) {
                writer.writeDefaultNamespace(FCS_RESOURCE_INFO_NS);
            } else {
                writer.writeNamespace(prefix, FCS_RESOURCE_INFO_NS);
            }
        }
        if (recursive) {
            /*
             * HACK: only output @pid for recursive (= explain) requests.
             * This should be revisited, if we decide to go for the explain
             * style enumeration of resources.
             */
            writer.writeAttribute("pid", resourceInfo.getPid());
        }
        if (resourceInfo.hasSubResources()) {
            writer.writeAttribute("hasSubResources", "true");
        }

        final Map<String, String> title = resourceInfo.getTitle();
        for (Map.Entry<String, String> i : title.entrySet()) {
            writer.setPrefix(XMLConstants.XML_NS_PREFIX,
                    XMLConstants.XML_NS_URI);
            writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Title");
            writer.writeAttribute(XMLConstants.XML_NS_URI, "lang", i.getKey());
            writer.writeCharacters(i.getValue());
            writer.writeEndElement(); // "title" element
        }

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

        final String landingPageURI = resourceInfo.getLandingPageURI();
        if (landingPageURI != null) {
            writer.writeStartElement(FCS_RESOURCE_INFO_NS, "LandingPageURI");
            writer.writeCharacters(landingPageURI);
            writer.writeEndElement(); // "LandingPageURI" element
        }

        final List<String> languages = resourceInfo.getLanguages();
        writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Languages");
        for (String i : languages) {
            writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Language");
            writer.writeCharacters(i);
            writer.writeEndElement(); // "Language" element

        }
        writer.writeEndElement(); // "Languages" element

        if (recursive && resourceInfo.hasSubResources()) {
            writer.writeStartElement(FCS_RESOURCE_INFO_NS,
                    "ResourceInfoCollection");
            for (ResourceInfo r : resourceInfo.getSubResources()) {
                doWriteResourceInfo(writer, prefix, r, writeNS, recursive);
            }
            writer.writeEndElement(); // "ResourceCollection" element
        }
        writer.writeEndElement(); // "ResourceInfo" element
    }

} // class SimpleEndpointSearchEngineBase
