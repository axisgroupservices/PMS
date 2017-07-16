package org.axisgroup.confhandler;

public class PaypalConfiguration {
	
	private String configLocation;
	private String payPalEndPoint;
	private String clientId;
	private String secret;
	public String getConfigLocation() {
		return configLocation;
	}
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}
	public String getPayPalEndPoint() {
		return payPalEndPoint;
	}
	public void setPayPalEndPoint(String payPalEndPoint) {
		this.payPalEndPoint = payPalEndPoint;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	

}
