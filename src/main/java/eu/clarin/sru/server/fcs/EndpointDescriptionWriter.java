package eu.clarin.sru.server.fcs;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class EndpointDescriptionWriter {
		
	public static final String NAMESPACE = "http://clarin.eu/fcs/endpoint-description";
	public static final String PREFIX = "ed";
	public static final int VERSION = 1;
		
	public static void writeEndpointDescription(XMLStreamWriter writer, 
			List<String> capabilities, List<DataView> supportedDataViews, 
			List<ResourceInfo> resourceInfoList) 
			throws XMLStreamException{
		
		if (writer == null) {
            throw new NullPointerException("writer == null");
        }
		
        writer.writeStartElement(PREFIX, "EndpointDescription", NAMESPACE);
        writer.writeNamespace(PREFIX, NAMESPACE);
        writer.writeAttribute("version", String.valueOf(VERSION));
        writeCapabilities(writer,capabilities);
        writeSupportedDataViews(writer,supportedDataViews);
        ResourceInfoWriter.writeFullResourceInfo(writer,PREFIX, resourceInfoList);
        writer.writeEndElement();
	}
	
	private static void writeCapabilities(XMLStreamWriter writer, 
			List<String> capabilities) throws XMLStreamException{
		
		if (writer == null) {
            throw new NullPointerException("writer == null");
        }
		
        writer.writeStartElement(PREFIX, "Capabilities", NAMESPACE);
		
		for (String c : capabilities){
	        writer.writeStartElement(PREFIX, "Capability", NAMESPACE);
	        writer.writeCharacters(c);
		    writer.writeEndElement();
		}
		writer.writeEndElement();		
	}
	
	private static void writeSupportedDataViews(XMLStreamWriter writer,
			List<DataView> supportedDataViews) throws XMLStreamException {
		
		if (writer == null) {
            throw new NullPointerException("writer == null");
        }
		
        writer.writeStartElement(PREFIX, "SupportedDataViews", NAMESPACE);
		
		for (DataView dv : supportedDataViews) {
	        writer.writeStartElement(PREFIX, "SupportedDataView", NAMESPACE);
	        writer.writeAttribute("id", dv.getShortIdentifier());
	        writer.writeAttribute("delivery-policy", dv.getPayloadDelivery());
	        writer.writeCharacters(dv.getMimeType());
	        writer.writeEndElement();
		}
		writer.writeEndElement();
	}	
}
