package org.axisgroup.paypal.payouts.response;

import java.util.List;

import org.axisgroup.common.dto.BatchHeader;
import org.axisgroup.common.dto.Link;
import org.axisgroup.common.dto.PayoutError;

public class PaypalPayoutResponse {
	
	private BatchHeader batch_header;
	private List<Link> links;
	private PayoutError error;
	
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
	public PayoutError getError() {
		return error;
	}
	public void setError(PayoutError error) {
		this.error = error;
	}
	

}
