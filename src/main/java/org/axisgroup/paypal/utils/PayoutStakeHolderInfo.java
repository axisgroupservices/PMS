package org.axisgroup.paypal.utils;

import org.axisgroup.common.dto.Amount;

public class PayoutStakeHolderInfo {
	
	private String payPalAccountEmail;
	private Amount payoutAmount;
	private String paymentNote;
	private String senderItemId;
	private String recipeintType;//EMAIL and PHONE are valid values
	public String getPayPalAccountEmail() {
		return payPalAccountEmail;
	}
	public void setPayPalAccountEmail(String payPalAccountEmail) {
		this.payPalAccountEmail = payPalAccountEmail;
	}
	public Amount getPayoutAmount() {
		return payoutAmount;
	}
	public void setPayoutAmount(Amount payoutAmount) {
		this.payoutAmount = payoutAmount;
	}
	public String getPaymentNote() {
		return paymentNote;
	}
	public void setPaymentNote(String paymentNote) {
		this.paymentNote = paymentNote;
	}
	public String getSenderItemId() {
		return senderItemId;
	}
	public void setSenderItemId(String senderItemId) {
		this.senderItemId = senderItemId;
	}
	public String getRecipeintType() {
		return recipeintType;
	}
	public void setRecipeintType(String recipeintType) {
		this.recipeintType = recipeintType;
	}
	
	
	

}


