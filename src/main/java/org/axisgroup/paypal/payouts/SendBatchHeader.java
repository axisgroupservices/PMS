package org.axisgroup.paypal.payouts;

public class SendBatchHeader {
	private String sender_batch_id;
	private String email_subject;
	public String getSender_batch_id() {
		return sender_batch_id;
	}
	public void setSender_batch_id(String sender_batch_id) {
		this.sender_batch_id = sender_batch_id;
	}
	public String getEmail_subject() {
		return email_subject;
	}
	public void setEmail_subject(String email_subject) {
		this.email_subject = email_subject;
	}
	
	

}
