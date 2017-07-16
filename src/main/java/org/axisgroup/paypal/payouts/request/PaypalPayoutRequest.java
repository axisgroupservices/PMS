package org.axisgroup.paypal.payouts.request;

import java.util.List;

import org.axisgroup.common.dto.Item;
import org.axisgroup.common.dto.SendBatchHeader;

public class PaypalPayoutRequest {
	
	private SendBatchHeader sender_batch_header;
	private List<Item> items;
	public SendBatchHeader getSender_batch_header() {
		return sender_batch_header;
	}
	public void setSender_batch_header(SendBatchHeader sender_batch_header) {
		this.sender_batch_header = sender_batch_header;
	}
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}
	
}
