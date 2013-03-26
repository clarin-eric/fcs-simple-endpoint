package eu.clarin.sru.server.fcs.utils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.clarin.sru.server.SRUConfigException;
import eu.clarin.sru.server.fcs.ResourceInfoInventory;
import eu.clarin.sru.server.fcs.ResourceInfo;


/**
 * A parser, that parses an XML file and produces a static list of resource info
 * records. The resulting list can be used to construct a
 * {@link SimpleResourceInfoInventory} instance.
 * 
 * @see ResourceInfo
 * @see SimpleResourceInfoInventory
 */
public class SimpleResourceInfoInventoryParser {
    private static final String NS = "http://clarin.eu/fcs/1.0/resource-info";
    private static final String LANG_EN = "en";
    private static final Logger logger =
            LoggerFactory.getLogger(SimpleResourceInfoInventoryParser.class);


    /**
     * Parse an XML file and return a static list of resource info records.
     * 
     * @param url
     *            the URI pointing to the file to be parsed
     * @return a list of resource info records represented as
     *         {@link ResourceInfo} instances
     * @throws SRUConfigException
     *             if an error occurred
     */
    public static ResourceInfoInventory parse(URL url) throws SRUConfigException {
        if (url == null) {
            throw new NullPointerException("url == null");
        }

        logger.debug("parsing resource-info from: {}", url);

        final Set<String> ids = new HashSet<String>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setCoalescing(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(url.openStream());

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
                    if (prefix.equals("ri")) {
                        return NS;
                    } else if (prefix.equals(XMLConstants.XML_NS_PREFIX)) {
                        return XMLConstants.XML_NS_URI;
                    } else {
                        return XMLConstants.NULL_NS_URI;
                    }
                }
            });
            XPathExpression expression =
                    xpath.compile("/ri:ResourceCollection/ri:ResourceInfo");
            NodeList list =
                    (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

            List<ResourceInfo> entries = parseResourceInfo(xpath, list, ids);
            return new SimpleResourceInfoInventory(entries, false);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static List<ResourceInfo> parseResourceInfo(XPath xpath,
            NodeList nodes, Set<String> ids) throws SRUConfigException,
            XPathExpressionException {
        logger.debug("parsing 'ResourceInfo' ({} nodes) ...",
                nodes.getLength());

        List<ResourceInfo> ris = null;
        for (int k = 0; k < nodes.getLength(); k++) {
            final Element node = (Element) nodes.item(k);
            String pid = null;
            int resourceCount = -1;
            Map<String, String> titles = null;
            Map<String, String> descrs = null;
            String link = null;
            List<String> langs = null;
            List<ResourceInfo> sub = null;

            pid = node.getAttribute("pid");
            if (pid != null) {
                pid = pid.trim();
                if (pid.isEmpty()) {
                    pid = null;
                }
            }
            if (pid == null) {
                throw new SRUConfigException("Element <ResourceInfo> " +
                        "must carry a proper 'pid' attribute");
            }
            if (ids.contains(pid)) {
                throw new SRUConfigException("Another element <ResourceInfo> " +
                        "with pid '" + pid + "' already exists");
            }
            ids.add(pid);

            XPathExpression x1 = xpath.compile("ri:Title");
            NodeList l1 = (NodeList) x1.evaluate(node, XPathConstants.NODESET);
            if (l1 != null) {
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
            XPathExpression x2 = xpath.compile("ri:Description");
            NodeList l2 = (NodeList) x2.evaluate(node, XPathConstants.NODESET);
            if (l2 != null) {
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

            XPathExpression x3 = xpath.compile("ri:LandingPageURI");
            NodeList l3 = (NodeList) x3.evaluate(node, XPathConstants.NODESET);
            if (l3 != null) {
                for (int i = 0; i < l3.getLength(); i++) {
                    Element n = (Element) l3.item(i);
                    link = cleanString(n.getTextContent());
                    logger.debug("link: \"{}\"", n.getTextContent());
                }
            }

            XPathExpression x4 = xpath.compile("ri:Languages/ri:Language");
            NodeList l4 = (NodeList) x4.evaluate(node, XPathConstants.NODESET);
            if (l4 != null) {
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
                    logger.debug("language: '{}'", n.getTextContent());
                    langs.add(s);
                }
            }

            XPathExpression x5 =
                    xpath.compile("ri:ResourceCollection/ri:ResourceInfo");
            NodeList l5 = (NodeList) x5.evaluate(node, XPathConstants.NODESET);
            if ((l5 != null) && (l5.getLength() > 0)) {
                sub = parseResourceInfo(xpath, l5, ids);
            }

            if (ris == null) {
                ris = new LinkedList<ResourceInfo>();
            }
            ris.add(new ResourceInfo(pid, resourceCount, titles, descrs, link,
                    langs, sub));
        }
        return ris;
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

} // class SimpleResourceInfoInventoryParser
