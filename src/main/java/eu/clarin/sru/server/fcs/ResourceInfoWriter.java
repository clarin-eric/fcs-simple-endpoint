package eu.clarin.sru.server.fcs;

import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class ResourceInfoWriter {

	public static final String FCS_RESOURCE_INFO_NS =
            "http://clarin.eu/fcs/endpoint-description";
    
	public static void writeFullResourceInfo(XMLStreamWriter writer,
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
            writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Resources");
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


	public static void writeResourceInfo(XMLStreamWriter writer, String prefix,
            ResourceInfo resourceInfo) throws XMLStreamException {        
        doWriteResourceInfo(writer, prefix, resourceInfo, true, false);
    }


	private static void doWriteResourceInfo(XMLStreamWriter writer,
            String prefix, ResourceInfo resourceInfo, boolean writeNS,
            boolean recursive) throws XMLStreamException {
		
		if (writer == null) {
            throw new NullPointerException("writer == null");
        }
		if (prefix == null) {
            throw new NullPointerException("prefix == null");
        }
		if (prefix.isEmpty()) {
            throw new IllegalArgumentException("prefix is empty");
        }
		if (resourceInfo == null) {
            throw new NullPointerException("resourceInfo == null");
        }
		
        final boolean defaultNS = ((prefix == null) || prefix.isEmpty());
        if (writeNS) {
            if (defaultNS) {
                writer.setDefaultNamespace(FCS_RESOURCE_INFO_NS);
            } else {
                writer.setPrefix(prefix, FCS_RESOURCE_INFO_NS);
            }
        }
        writer.writeStartElement(FCS_RESOURCE_INFO_NS, "Resource");
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

        final List<String> availableDataViews = resourceInfo.getAvailableDataViews();
        writer.writeStartElement(FCS_RESOURCE_INFO_NS, "AvailableDataViews");
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < availableDataViews.size(); i++) {
            sb.append(availableDataViews.get(i));
            sb.append(" ");
        }        
        writer.writeAttribute("ref", sb.toString().trim());
        
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
}
