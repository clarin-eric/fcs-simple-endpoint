package eu.clarin.sru.server.fcs.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.fcs.DataView;
import eu.clarin.sru.server.fcs.DataView.DeliveryPolicy;
import eu.clarin.sru.server.fcs.EndpointDescription;
import eu.clarin.sru.server.fcs.ResourceInfo;


/**
 * A parser, that parses an XML file and produces a endpoint description with
 * static list of resource info records. The XML file has the same format as the
 * result format defined for endpoint description of the CLARIN-FCS
 * specification. The {@link #parse(URL)} returns a
 * {@link SimpleEndpointDescription} instance.
 *
 * @see EndpointDescription
 * @see SimpleEndpointDescription
 */
public class SimpleEndpointDescriptionParser {
    private static final String NS =
            "http://clarin.eu/fcs/endpoint-description";
    private static final String NS_LEGACY =
            "http://clarin.eu/fcs/1.0/resource-info";
    private static final String CAP_BASIC_SEARCH =
            "http://clarin.eu/fcs/capability/basic-search";
    private static final String LANG_EN = "en";
    private static final String POLICY_SEND_DEFAULT = "send-by-default";
    private static final String POLICY_NEED_REQUEST = "need-to-request";
    private static final Logger logger =
            LoggerFactory.getLogger(SimpleEndpointDescriptionParser.class);


    /**
     * Parse an XML file and return a static list of resource info records.
     *
     * @param url
     *            the URI pointing to the file to be parsed
     * @return an {@link EndpointDescription} instance
     * @throws SRUConfigException
     *             if an error occurred
     */
    public static EndpointDescription parse(URL url) throws SRUConfigException {
        if (url == null) {
            throw new NullPointerException("url == null");
        }

        logger.debug("parsing endpoint description from: {}", url);

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setCoalescing(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(url.openStream());

            /*
             * Detect for deprecated resource-info catalog files and bail, if necessary
             */
            checkLegacyMode(doc, url);

            /*
             * Parse on and create endpoint description ...
             */
            return parseEndpointDescription(doc);
        } catch (ParserConfigurationException e) {
            throw new SRUConfigException("internal error", e);
        } catch (SAXException e) {
            throw new SRUConfigException("parsing error", e);
        } catch (IOException e) {
            throw new SRUConfigException("error reading file", e);
        } catch (XPathExpressionException e) {
            throw new SRUConfigException("internal error", e);
        }
    }


    private static EndpointDescription parseEndpointDescription(Document doc)
            throws SRUConfigException, XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public Iterator<?> getPrefixes(String namespaceURI) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getPrefix(String namespaceURI) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix == null) {
                    throw new NullPointerException("prefix == null");
                }
                if (prefix.equals("ed")) {
                    return NS;
                } else if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
                    return XMLConstants.XML_NS_URI;
                } else {
                    return XMLConstants.NULL_NS_URI;
                }
            }
        });

        // capabilities
        List<URI> capabilities = new ArrayList<URI>();
        XPathExpression exp1 =
                xpath.compile("//ed:Capabilities/ed:Capability");
        NodeList list1 = (NodeList) exp1.evaluate(doc, XPathConstants.NODESET);
        if ((list1 != null) && (list1.getLength() > 0)) {
            logger.debug("parsing capabilities");
            for (int i = 0; i < list1.getLength(); i++) {
                String s = list1.item(i).getTextContent().trim();
                try {
                    URI uri = new URI(s);
                    if (capabilities.contains(uri)) {
                        logger.warn("ignoring duplicate capability " +
                                "entry for '{}'", uri);
                    }
                    capabilities.add(uri);
                } catch (URISyntaxException e) {
                    throw new SRUConfigException("capability is not encoded " +
                            "as proper URI: " + s);
                }
            }
        } else {
            logger.warn("No capabilities where defined in " +
                    "endpoint configuration");
        }
        URI cap = URI.create(CAP_BASIC_SEARCH);
        if (!capabilities.contains(cap)) {
            logger.warn("capability '{}' was not defined in endpoint " +
                    "description; added it to meet specification. Please " +
                    "update your endpoint description!", CAP_BASIC_SEARCH);
            capabilities.add(cap);
        }
        logger.debug("CAPS:'{}'", capabilities);

        // supported data views
        List<DataView> supportedDataViews = new ArrayList<DataView>();
        XPathExpression exp2 =
                xpath.compile("//ed:SupportedDataViews/ed:SupportedDataView");
        NodeList list2 = (NodeList) exp2.evaluate(doc, XPathConstants.NODESET);
        if ((list2 != null) && (list2.getLength() > 0)) {
            logger.debug("parsing supported data views");
            for (int i = 0; i < list2.getLength(); i++) {
                Element item = (Element) list2.item(i);
                String id = getAttribute(item, "id");
                if (id == null) {
                    throw new SRUConfigException("Element <SupportedDataView> "
                            + "must carry a proper 'id' attribute");
                }
                String p = getAttribute(item, "delivery-policy");
                if (p == null) {
                    throw new SRUConfigException("Element <SupportedDataView> "
                            + "must carry a 'delivery-policy' attribute");
                }
                DeliveryPolicy policy = null;
                if (POLICY_SEND_DEFAULT.equals(p)) {
                    policy = DeliveryPolicy.SEND_BY_DEFAULT;
                } else if (POLICY_NEED_REQUEST.equals(p)) {
                    policy = DeliveryPolicy.NEED_TO_REQUEST;
                } else {
                    throw new SRUConfigException("Invalid value '" + p +
                            "' for attribute 'delivery-policy' on element " +
                            "<SupportedDataView>");
                }
                String mimeType = item.getTextContent();
                if (mimeType != null) {
                    mimeType = mimeType.trim();
                    if (mimeType.isEmpty()) {
                        mimeType = null;
                    }
                }
                if (mimeType == null) {
                    throw new SRUConfigException("Element <SupportedDataView> "
                            + "must contain a MIME-type as content");
                }
                // check for duplicate entries ...
                for (DataView dataView : supportedDataViews) {
                    if (id.equals(dataView.getIdentifier())) {
                        throw new SRUConfigException(
                                "A <SupportedDataView> with " + "the id '" +
                                        id + "' is already defined!");
                    }
                    if (mimeType.equals(dataView.getMimeType())) {
                        throw new SRUConfigException(
                                "A <SupportedDataView> with " +
                                        "the MIME-type '" + mimeType +
                                        "' is already defined!");
                    }
                }
                supportedDataViews.add(new DataView(id, mimeType, policy));
            }
        } else {
            logger.error("Endpoint configuration contains no valid " +
                    "information about supported data views");
            throw new SRUConfigException("Endpoint configuration contains " +
                    "no valid information about supported data views");
        }

        logger.debug("DV: {}", supportedDataViews);


        // resources
        XPathExpression x3 =
                xpath.compile("/ed:EndpointDescription/ed:Resources/ed:Resource");
        NodeList l3 = (NodeList) x3.evaluate(doc, XPathConstants.NODESET);
        final Set<String> ids = new HashSet<String>();
        List<ResourceInfo> resources =
                parseRessources(xpath, l3, ids, supportedDataViews);
        if ((resources == null) || resources.isEmpty()) {
            throw new SRUConfigException("No resources where " +
                    "defined in endpoint description");
        }

        return new SimpleEndpointDescription(capabilities,
                supportedDataViews,
                resources,
                false);
    }


    private static List<ResourceInfo> parseRessources(XPath xpath,
            NodeList nodes, Set<String> ids, List<DataView> supportedDataViews)
            throws SRUConfigException, XPathExpressionException {
      List<ResourceInfo> ris = null;
      for (int k = 0; k < nodes.getLength(); k++) {
          final Element node                = (Element) nodes.item(k);
          String pid                        = null;
          Map<String, String> titles        = null;
          Map<String, String> descrs        = null;
          String link                       = null;
          List<String> langs                = null;
          List<DataView> availableDataViews = null;
          List<ResourceInfo> sub            = null;

          pid = getAttribute(node, "pid");
          if (pid == null) {
              throw new SRUConfigException("Element <ResourceInfo> " +
                      "must carry a proper 'pid' attribute");
          }
          if (ids.contains(pid)) {
              throw new SRUConfigException("Another element <Resource> " +
                      "with pid '" + pid + "' already exists");
          }
          ids.add(pid);

          XPathExpression x1 = xpath.compile("ed:Title");
          NodeList l1 = (NodeList) x1.evaluate(node, XPathConstants.NODESET);
          if ((l1 != null) && (l1.getLength() > 0)) {
              for (int i = 0; i < l1.getLength(); i++) {
                  final Element n = (Element) l1.item(i);

                  final String lang = getLangAttribute(n);
                  if (lang == null) {
                      throw new SRUConfigException("Element <Title> must " +
                              "carry a proper 'xml:lang' attribute");
                  }

                  final String title = cleanString(n.getTextContent());
                  if (title == null) {
                      throw new SRUConfigException("Element <Title> must " +
                              "carry a non-empty 'xml:lang' attribute");
                  }

                  if (titles == null) {
                      titles = new HashMap<String, String>();
                  }
                  if (titles.containsKey(lang)) {
                      logger.warn("title with language '{}' already exists",
                              lang);
                  } else {
                      logger.debug("title: '{}' '{}'", lang, title);
                      titles.put(lang, title);
                  }
              }
              if ((titles != null) && !titles.containsKey(LANG_EN)) {
                  throw new SRUConfigException(
                          "A <Title> with language 'en' is mandatory");
              }
          }

          XPathExpression x2 = xpath.compile("ed:Description");
          NodeList l2 = (NodeList) x2.evaluate(node, XPathConstants.NODESET);
          if ((l2 != null) && (l2.getLength() > 0)) {
              for (int i = 0; i < l2.getLength(); i++) {
                  Element n = (Element) l2.item(i);

                  String lang = getLangAttribute(n);
                  if (lang == null) {
                      throw new SRUConfigException("Element <Description> " +
                              "must carry a proper 'xml:lang' attribute");

                  }
                  String desc = cleanString(n.getTextContent());

                  if (descrs == null) {
                      descrs = new HashMap<String, String>();
                  }

                  if (descrs.containsKey(lang)) {
                      logger.warn("description with language '{}' "
                              + "already exists", lang);
                  } else {
                      logger.debug("description: '{}' '{}'", lang, desc);
                      descrs.put(lang, desc);
                  }
              }
              if ((descrs != null) && !descrs.containsKey(LANG_EN)) {
                  throw new SRUConfigException(
                          "A <Description> with language 'en' is mandatory");
              }
          }

          XPathExpression x3 = xpath.compile("ed:LandingPageURI");
          NodeList l3 = (NodeList) x3.evaluate(node, XPathConstants.NODESET);
          if ((l3 != null) && (l3.getLength() > 0)) {
              for (int i = 0; i < l3.getLength(); i++) {
                  Element n = (Element) l3.item(i);
                  link = cleanString(n.getTextContent());
              }
          }

          XPathExpression x4 = xpath.compile("ed:Languages/ed:Language");
          NodeList l4 = (NodeList) x4.evaluate(node, XPathConstants.NODESET);
          if ((l4 != null) && (l4.getLength() > 0)) {
              for (int i = 0; i < l4.getLength(); i++) {
                  Element n = (Element) l4.item(i);

                  String s = n.getTextContent();
                  if (s != null) {
                      s = s.trim();
                      if (s.isEmpty()) {
                          s = null;
                      }
                  }

                  /*
                   * enforce three letter codes
                   */
                  if ((s == null) || (s.length() != 3)) {
                      throw new SRUConfigException("Element <Language> " +
                              "must use ISO-632-3 three letter " +
                              "language codes");
                  }

                  if (langs == null) {
                      langs = new ArrayList<String>();
                  }
                  langs.add(s);
              }
          }

          XPathExpression x5 = xpath.compile("ed:AvailableDataViews");
          Node n = (Node) x5.evaluate(node, XPathConstants.NODE);
          if ((n != null) && (n instanceof Element)) {
              String ref = getAttribute((Element) n, "ref");
              if (ref == null) {
                  throw new SRUConfigException("Element <AvailableDataViews> " +
                          "must carry a 'ref' attribute");
              }
              String[] refs = ref.split("\\s+");
              if ((refs == null) || (refs.length < 1)) {
                  throw new SRUConfigException("Attribute 'ref' on element " +
                          "<AvailableDataViews> must contain a whitespace " +
                          "seperated list of data view references");
              }


              for (int i = 0; i < refs.length; i++) {
                  DataView dataview = null;
                  for (DataView dv : supportedDataViews) {
                      if (refs[i].equals(dv.getIdentifier())) {
                          dataview = dv;
                          break;
                      }
                  }
                  if (dataview != null) {
                      if (availableDataViews == null) {
                          availableDataViews = new ArrayList<DataView>();
                      }
                      availableDataViews.add(dataview);
                  } else {
                      throw new SRUConfigException("A data view with " +
                              "identifier '" + refs[i] + "' was not defined " +
                              "in <SupportedDataViews>");
                  }
              }
          } else {
              throw new SRUConfigException(
                      "missing element <ed:AvailableDataViews>");
          }
          if (availableDataViews == null) {
              throw new SRUConfigException("No available data views where " +
                      "defined for resource with PID '" + pid + "'");
          }

          XPathExpression x6 = xpath.compile("ed:Resources/ed:Resource");
          NodeList l6 = (NodeList) x6.evaluate(node, XPathConstants.NODESET);
          if ((l6 != null) && (l6.getLength() > 0)) {
              sub = parseRessources(xpath, l6, ids, supportedDataViews);
          }

          if (ris == null) {
              ris = new ArrayList<ResourceInfo>();
          }
          ris.add(new ResourceInfo(pid,
                  titles,
                  descrs,
                  link,
                  langs,
                  availableDataViews,
                  sub));
      }
      return ris;
    }


    private static String getAttribute(Element el, String localName) {
        String lang = el.getAttribute(localName);
        if (lang != null) {
            lang = lang.trim();
            if (!lang.isEmpty()) {
                return lang;
            }
        }
        return null;
    }


    private static String getLangAttribute(Element el) {
        String lang = el.getAttributeNS(XMLConstants.XML_NS_URI, "lang");
        if (lang != null) {
            lang = lang.trim();
            if (!lang.isEmpty()) {
                return lang;
            }
        }
        return null;
    }


    private static String cleanString(String s) {
        if (s != null) {
            s = s.trim();
            if (!s.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String z : s.split("\\s*\\n+\\s*")) {
                    z = z.trim();
                    if (!z.isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(' ');
                        }
                        sb.append(z);
                    }
                }
                if (sb.length() > 0) {
                    return sb.toString();
                }
            }
        }
        return null;
    }


    private static void checkLegacyMode(Document doc, URL url)
            throws SRUConfigException {
        Element root = doc.getDocumentElement();
        if (root != null) {
            String ns = root.getNamespaceURI();
            if (ns != null) {
                if (ns.equals(NS_LEGACY)) {
                    logger.error("Detected out-dated " +
                            "resource info catalog file '" + url +
                            "'. Please update to the " +
                            "current version");
                    throw new SRUConfigException("unsupport file format: " + ns);
                } else if (!ns.equals(NS)) {
                    logger.error("Detected unsupported resource info " +
                            "catalog file '" + url + "' with namespace '" + ns + '"');
                    throw new SRUConfigException("unsupport file format: " + ns);
                }
            } else {
                throw new SRUConfigException("No namespace URI was detected " +
                        "for resource info catalog file '" + url +"'!");
            }
        } else {
            throw new SRUConfigException("Error retrieving root element");
        }
    }


//    private static List<ResourceInfo> parseLegacy(Document doc)
//            throws SRUConfigException, XPathExpressionException {

} // class SimpleResourceInfoInventoryParser
