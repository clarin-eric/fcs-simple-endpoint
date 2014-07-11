package eu.clarin.sru.server.fcs;

public class DataView {

	public enum PayloadDelivery {
		SEND_BY_DEFAULT, NEED_TO_REQUEST;
		
		@Override
		public String toString(){
			String str = super.toString().toLowerCase();
			return str.replace("_", "-");
		}
	}

	public enum PayloadDisposition {
		INLINE, REFERENCE;
		
		@Override
		public String toString(){
			return super.toString().toLowerCase();
		}
	}
	
	private String description;
	private String mimeType;
	private String payloadDisposition;
	private String payloadDelivery;
	private String shortIdentifier;
	
	public DataView() {	}
	public DataView(String description, String mimeType, 
			PayloadDisposition payloadDisposition, PayloadDelivery payloadDelivery, 
			String shortId){
		this.description = description;
		this.mimeType = mimeType;
		this.payloadDisposition = payloadDisposition.toString();
		this.payloadDelivery = payloadDelivery.toString();
		this.shortIdentifier = shortId;		
	}
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getPayloadDisposition() {
		return payloadDisposition;
	}
	public void setPayloadDisposition(String payloadDisposition) {
		this.payloadDisposition = payloadDisposition;
	}
	public String getPayloadDelivery() {
		return payloadDelivery;
	}
	public void setPayloadDelivery(String payloadDelivery) {
		this.payloadDelivery = payloadDelivery;
	}
	public String getShortIdentifier() {
		return shortIdentifier;
	}
	public void setShortIdentifier(String shortIdentifier) {
		this.shortIdentifier = shortIdentifier;
	}
}
