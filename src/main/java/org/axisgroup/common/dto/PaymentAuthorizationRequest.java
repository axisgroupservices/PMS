package org.axisgroup.common.dto;

public class PaymentAuthorizationRequest {	
	private String business ;
	private String currency_code; 
	private String item_name ;
	private String amount ;
	private String contractId ;
	private String ownerEmail;
	private String sellerPaypal;
	private String buyerEmail;
	private String intent;
	
	public String getBusiness() {
		return business;
	}
	public void setBusiness(String business) {
		this.business = business;
	}
	public String getCurrency_code() {
		return currency_code;
	}
	public void setCurrency_code(String currency_code) {
		this.currency_code = currency_code;
	}
	public String getItem_name() {
		return item_name;
	}
	public void setItem_name(String item_name) {
		this.item_name = item_name;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public String getContractId() {
		return contractId;
	}
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}
	public String getOwnerEmail() {
		return ownerEmail;
	}
	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}
	public String getBuyerEmail() {
		return buyerEmail;
	}
	public void setBuyerEmail(String buyerEmail) {
		this.buyerEmail = buyerEmail;
	}
	public String getIntent() {
		return intent;
	}
	public void setIntent(String intent) {
		this.intent = intent;
	}
	public String getSellerPaypal() {
		return sellerPaypal;
	}
	public void setSellerPaypal(String sellerPaypal) {
		this.sellerPaypal = sellerPaypal;
	}
	
	
}
