package org.axisgroup.paypal.payouts.response;

import java.util.List;

public class PaypalPayoutResponse {
	
	private BatchHeader batch_header;
	private List<Link> links;
	public BatchHeader getBatch_header() {
		return batch_header;
	}
	public void setBatch_header(BatchHeader batch_header) {
		this.batch_header = batch_header;
	}
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}	

}
