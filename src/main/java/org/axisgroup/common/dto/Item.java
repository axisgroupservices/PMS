package org.axisgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@JsonInclude(Include.NON_DEFAULT)
public class Item {
	private String recipient_type;
	private Amount amount;
	private String note;
	private String sender_item_id;
	private String receiver;
	public String getRecipient_type() {
		return recipient_type;
	}
	public void setRecipient_type(String recipient_type) {
		this.recipient_type = recipient_type;
	}
	public Amount getAmount() {
		return amount;
	}
	public void setAmount(Amount amount) {
		this.amount = amount;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getSender_item_id() {
		return sender_item_id;
	}
	public void setSender_item_id(String sender_item_id) {
		this.sender_item_id = sender_item_id;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	

}
