package org.axisgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonInclude(Include.NON_DEFAULT)
public class BatchHeader {
	private String payout_batch_id;
	private String batch_status;
	private SendBatchHeader sender_batch_header;
	public String getPayout_batch_id() {
		return payout_batch_id;
	}
	public void setPayout_batch_id(String payout_batch_id) {
		this.payout_batch_id = payout_batch_id;
	}
	public String getBatch_status() {
		return batch_status;
	}
	public void setBatch_status(String batch_status) {
		this.batch_status = batch_status;
	}
	public SendBatchHeader getSender_batch_header() {
		return sender_batch_header;
	}
	public void setSender_batch_header(SendBatchHeader sender_batch_header) {
		this.sender_batch_header = sender_batch_header;
	}
	
	

}
