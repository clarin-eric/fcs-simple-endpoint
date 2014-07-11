package eu.clarin.sru.server.fcs;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class SimpleFcsResourceWriter {
	private static final String FCS_NS = "http://clarin.eu/fcs/resource";
    private static final String FCS_PREFIX = "fcs";
    private static final String FCS_DATAVIEW_MIMETYPE =
            "application/x-clarin-fcs-hits+xml";
    
    private static final String HITS_NS = "http://clarin.eu/fcs/dataview/hits";
    private static final String HITS_PREFIX = "hits";
    
    public static void writeResource(XMLStreamWriter writer, String pid, String ref, 
			String leftContext, String keyword, String rightContext)
            throws XMLStreamException {
		
		if (writer == null) {
            throw new NullPointerException("writer == null");
        }
		
		writer.setPrefix(FCS_PREFIX, FCS_NS);
        writer.writeStartElement(FCS_NS, "Resource");
        writer.writeNamespace(FCS_PREFIX, FCS_NS);
        if ((pid != null) && !pid.isEmpty()) {
            writer.writeAttribute("pid", pid);
        }
        if ((ref != null) && !ref.isEmpty()) {
            writer.writeAttribute("ref", ref);
        }        
        writeDataView(writer, leftContext, keyword, rightContext);        
        writer.writeEndElement();
	}
    
    public static void writeDataView(XMLStreamWriter writer, String leftContext, 
    		String keyword, String rightContext) throws XMLStreamException { 
    	
    	if (writer == null) {
            throw new NullPointerException("writer == null");
        }
    	
    	writer.writeStartElement(FCS_NS, "DataView");
        writer.writeAttribute("type", FCS_DATAVIEW_MIMETYPE);        
        writeHits(writer, leftContext, keyword, rightContext);        
        writer.writeEndElement();
    }
    
    public static void writeHits(XMLStreamWriter writer, String leftContext, 
    		String keyword, String rightContext) throws XMLStreamException {                                    
            
    	if (writer == null) {
            throw new NullPointerException("writer == null");
        }
        writer.setPrefix(HITS_PREFIX, HITS_NS);
        writer.writeStartElement(HITS_NS, "Result");
        writer.writeNamespace(HITS_PREFIX, HITS_NS);
        
        if (leftContext != null && !leftContext.isEmpty()) {
            writer.writeCharacters(leftContext);
        }
        
        writer.writeStartElement(HITS_NS, "Hit");
        writer.writeCharacters(keyword);
        writer.writeEndElement(); // Hit

        if (rightContext != null && !rightContext.isEmpty()) {
            writer.writeCharacters(rightContext);
        }
        
        writer.writeEndElement(); // Result
    }
}
