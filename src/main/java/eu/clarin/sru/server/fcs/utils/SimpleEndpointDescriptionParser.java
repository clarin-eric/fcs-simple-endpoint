/**
 * This software is copyright (c) 2013-2016 by
 *  - Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
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
import eu.clarin.sru.server.fcs.Layer;
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
    private static final URI CAP_BASIC_SEARCH =
            URI.create("http://clarin.eu/fcs/capability/basic-search");
    private static final URI CAP_ADVANCED_SEARCH =
            URI.create("http://clarin.eu/fcs/capability/advanced-search");
    private static final String MINETYPE_HITS = "application/x-clarin-fcs-hits+xml";
    private static final String MINETYPE_ADV = "application/x-clarin-fcs-adv+xml";
    private static final String LANG_EN = "en";
    private static final String POLICY_SEND_DEFAULT = "send-by-default";
    private static final String POLICY_NEED_REQUEST = "need-to-request";
    private static final String LAYER_ENCODING_VALUE = "value";
    private static final String LAYER_ENCODING_EMPTY = "empty";
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
        XPathExpression exp =
                xpath.compile("//ed:Capabilities/ed:Capability");
        NodeList list =
                (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
        if ((list != null) && (list.getLength() > 0)) {
            logger.debug("parsing capabilities");
            for (int i = 0; i < list.getLength(); i++) {
                String s = list.item(i).getTextContent().trim();
                try {
                    URI uri = new URI(s);
                    if (!capabilities.contains(uri)) {
                        capabilities.add(uri);
                    } else {
                        logger.warn("ignoring duplicate capability " +
                                "entry for '{}'", uri);
                    }
                } catch (URISyntaxException e) {
                    throw new SRUConfigException("capability is not encoded " +
                            "as proper URI: " + s);
                }
            }
        } else {
            logger.warn("No capabilities where defined in " +
                    "endpoint configuration");
        }
        if (!capabilities.contains(CAP_BASIC_SEARCH)) {
            logger.warn("capability '{}' was not defined in endpoint " +
                    "description; added it to meet specification. Please " +
                    "update your endpoint description!", CAP_BASIC_SEARCH);
            capabilities.add(CAP_BASIC_SEARCH);
        }
        logger.debug("CAPS:'{}'", capabilities);

        // used to check for id attribute uniqueness
        final Set<String> xml_ids = new HashSet<String>();

        // supported data views
        List<DataView> supportedDataViews = new ArrayList<DataView>();
        exp = xpath.compile("//ed:SupportedDataViews/ed:SupportedDataView");
        list = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
        if ((list != null) && (list.getLength() > 0)) {
            logger.debug("parsing supported data views");
            for (int i = 0; i < list.getLength(); i++) {
                Element item = (Element) list.item(i);
                String id = getAttribute(item, "id");
                if (id == null) {
                    throw new SRUConfigException("Element <SupportedDataView> "
                            + "must carry a proper 'id' attribute");
                }

                if (xml_ids.contains(id)) {
                    throw new SRUConfigException("The value of attribute " +
                            "'id' of element <SupportedDataView> must be unique: " + id);
                }
                xml_ids.add(id);

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

        // sanity check on data views
        boolean hasHitsView = false;
        boolean hasAdvView = false;

        for (DataView dataView : supportedDataViews) {
            if (dataView.getMimeType().equals(MINETYPE_HITS)) {
                hasHitsView = true;
            } else if (dataView.getMimeType().equals(MINETYPE_ADV)) {
                hasAdvView = true;
            }
        }
        if (!hasHitsView) {
            throw new SRUConfigException("Generic Hits Data View (" +
                    MINETYPE_HITS + ") was not declared in <SupportedDataViews>");
        }
        if (capabilities.contains(CAP_ADVANCED_SEARCH) && !hasAdvView) {
            throw new SRUConfigException("Endpoint claimes to support " +
                    "Advanced FCS but does not declare Advanced Data View (" +
                    MINETYPE_ADV + ") in <SupportedDataViews>");

        }


        // supported layers
        List<Layer> supportedLayers = null;
        exp = xpath.compile("//ed:SupportedLayers/ed:SupportedLayer");
        list = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
        if ((list != null) && (list.getLength() > 0)) {
            logger.debug("parsing supported layers");
            for (int i = 0; i < list.getLength(); i++) {
                Element item = (Element) list.item(i);
                String id = getAttribute(item, "id");
                if (id == null) {
                    throw new SRUConfigException("Element <SupportedLayer> "
                            + "must carry a proper 'id' attribute");
                }

                if (xml_ids.contains(id)) {
                    throw new SRUConfigException("The value of attribute " +
                            "'id' of element <SupportedLayer> must be unique: " + id);
                }
                xml_ids.add(id);

                String s = getAttribute(item, "result-id");
                if (s == null) {
                    throw new SRUConfigException("Element <SupportedLayer> "
                            + "must carry a proper 'result-id' attribute");
                }
                URI resultId = null;
                try {
                    resultId = new URI(s);
                } catch (URISyntaxException e) {
                    throw new SRUConfigException("Attribute 'result-id' on " +
                            "Element <SupportedLayer> is not encoded " +
                            "as proper URI: " + s);
                }

                String type = cleanString(item.getTextContent());
                if ((type != null) && !type.isEmpty()) {
                    // sanity check on layer types
                    if (!(type.equals("text") ||
                            type.equals("lemma") ||
                            type.equals("pos") ||
                            type.equals("orth") ||
                            type.equals("norm") ||
                            type.equals("phonetic") ||
                            type.startsWith("x-"))) {
                        logger.warn("layer type '{}' is not defined by specification", type);
                    }
                } else {
                    throw new SRUConfigException("Element <SupportedLayer> " +
                            "does not define a proper layer type");
                }

                String qualifier = getAttribute(item, "qualifier");

                Layer.ContentEncoding encoding =
                        Layer.ContentEncoding.VALUE;
                s = getAttribute(item, "type");
                if (s != null) {
                    if (LAYER_ENCODING_VALUE.equals(s)) {
                        encoding = Layer.ContentEncoding.VALUE;
                    } else if (LAYER_ENCODING_EMPTY.equals(s)) {
                        encoding = Layer.ContentEncoding.EMPTY;
                    } else {
                        throw new SRUConfigException("invalid layer encoding: " + s);
                    }
                }


                String altValueInfo = getAttribute(item, "alt-value-info");
                URI altValueInfoURI = null;
                if (altValueInfo != null) {
                    s = getAttribute(item, "alt-value-info-uri");
                    if (s != null) {
                        try {
                          altValueInfoURI = new URI(s);
                        } catch (URISyntaxException e) {
                            throw new SRUConfigException("Attribute 'alt-value-info-uri' on " +
                                    "Element <SupportedLayer> is not encoded " +
                                    "as proper URI: " + s);
                        }
                    } else {

                    }
                }



                if (supportedLayers == null) {
                    supportedLayers = new ArrayList<Layer>(list.getLength());
                }
                supportedLayers.add(new Layer(id, resultId, type, encoding,
                        qualifier, altValueInfo, altValueInfoURI));
            }
        }

        if ((supportedLayers != null) &&
                !capabilities.contains(CAP_ADVANCED_SEARCH)) {
                logger.warn("capability '{}' was not defined in endpoint " +
                        "description; added it to meet specification. Please " +
                        "update your endpoint description!", CAP_ADVANCED_SEARCH);
                capabilities.add(CAP_ADVANCED_SEARCH);
        }
        logger.debug("L: {}", supportedLayers);

        // resources
        exp = xpath.compile("/ed:EndpointDescription/ed:Resources/ed:Resource");
        list = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
        final Set<String> pids = new HashSet<String>();
        List<ResourceInfo> resources = parseRessources(xpath, list, pids,
                supportedDataViews, supportedLayers, hasAdvView);
        if ((resources == null) || resources.isEmpty()) {
            throw new SRUConfigException("No resources where " +
                    "defined in endpoint description");
        }

        return new SimpleEndpointDescription(capabilities,
                supportedDataViews,
                supportedLayers,
                resources,
                false);
    }


    private static List<ResourceInfo> parseRessources(XPath xpath,
            NodeList nodes, Set<String> pids, List<DataView> supportedDataViews,
            List<Layer> supportedLayers, boolean hasAdv)
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
          List<Layer> availableLayers       = null;
          List<ResourceInfo> sub            = null;

          pid = getAttribute(node, "pid");
          if (pid == null) {
              throw new SRUConfigException("Element <ResourceInfo> " +
                      "must carry a proper 'pid' attribute");
          }
          if (pids.contains(pid)) {
              throw new SRUConfigException("Another element <Resource> " +
                      "with pid '" + pid + "' already exists");
          }
          pids.add(pid);

          XPathExpression exp = xpath.compile("ed:Title");
          NodeList list = (NodeList) exp.evaluate(node, XPathConstants.NODESET);
          if ((list != null) && (list.getLength() > 0)) {
              for (int i = 0; i < list.getLength(); i++) {
                  final Element n = (Element) list.item(i);

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

          exp = xpath.compile("ed:Description");
          list = (NodeList) exp.evaluate(node, XPathConstants.NODESET);
          if ((list != null) && (list.getLength() > 0)) {
              for (int i = 0; i < list.getLength(); i++) {
                  Element n = (Element) list.item(i);

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

          exp = xpath.compile("ed:LandingPageURI");
          list = (NodeList) exp.evaluate(node, XPathConstants.NODESET);
          if ((list != null) && (list.getLength() > 0)) {
              for (int i = 0; i < list.getLength(); i++) {
                  Element n = (Element) list.item(i);
                  link = cleanString(n.getTextContent());
              }
          }

          exp = xpath.compile("ed:Languages/ed:Language");
          list = (NodeList) exp.evaluate(node, XPathConstants.NODESET);
          if ((list != null) && (list.getLength() > 0)) {
              for (int i = 0; i < list.getLength(); i++) {
                  Element n = (Element) list.item(i);

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

          exp = xpath.compile("ed:AvailableDataViews");
          Node n = (Node) exp.evaluate(node, XPathConstants.NODE);
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
                      "missing element <AvailableDataViews>");
          }
          if (availableDataViews == null) {
              throw new SRUConfigException("No available data views where " +
                      "defined for resource with PID '" + pid + "'");
          }

          exp = xpath.compile("ed:AvailableLayers");
          n = (Node) exp.evaluate(node, XPathConstants.NODE);
          if ((n != null) && (n instanceof Element)) {
              String ref = getAttribute((Element) n, "ref");
              if (ref == null) {
                  throw new SRUConfigException("Element <AvailableLayers> " +
                          "must carry a 'ref' attribute");
              }
              String[] refs = ref.split("\\s+");
              if ((refs == null) || (refs.length < 1)) {
                  throw new SRUConfigException("Attribute 'ref' on element " +
                          "<AvailableLayers> must contain a whitespace " +
                          "seperated list of data view references");
              }


              for (int i = 0; i < refs.length; i++) {
                  Layer layer = null;
                  for (Layer l : supportedLayers) {
                      if (refs[i].equals(l.getId())) {
                          layer = l;
                          break;
                      }
                  }
                  if (layer != null) {
                      if (availableLayers == null) {
                          availableLayers = new ArrayList<Layer>();
                      }
                      availableLayers.add(layer);
                  } else {
                      throw new SRUConfigException("A layer with " +
                              "identifier '" + refs[i] + "' was not defined " +
                              "in <SupportedLayers>");
                  }
              }
          } else {
              if (hasAdv) {
                  logger.debug("no <SupportedLayers> for ressource '{}'", pid);
              }
          }

          exp = xpath.compile("ed:Resources/ed:Resource");
          list = (NodeList) exp.evaluate(node, XPathConstants.NODESET);
          if ((list != null) && (list.getLength() > 0)) {
              sub = parseRessources(xpath, list, pids,
                      supportedDataViews, supportedLayers, hasAdv);
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
                  availableLayers,
                  sub));
      }
      return ris;
    }


    private static String getAttribute(Element el, String localName) {
        String value = el.getAttribute(localName);
        if (value != null) {
            value = value.trim();
            if (!value.isEmpty()) {
                return value;
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

} // class SimpleResourceInfoInventoryParser
