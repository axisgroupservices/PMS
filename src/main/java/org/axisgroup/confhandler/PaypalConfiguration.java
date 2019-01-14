package org.axisgroup.confhandler;

import org.apache.log4j.Logger;
import org.axisgroup.base.controller.AuthorizeAndCaptureApplication;

public class PaypalConfiguration {
	
	private static final Logger logger = Logger.getLogger(AuthorizeAndCaptureApplication.class);

	private static final String CONFIG_LOCATION = "env.properties";
	private static final String PAYPAL_ENDPOINT = "oauth.endpoint.paypal";
	private static final String ROBOADPLACER_CLIENT_ID = "roboadplacer.clientId";
	private static final String ROBOADPLACER_SECRET = "roboadplacer.secret";
	private static final String PAYPAL_PAYMENT_MODE = "paypal.payment.mode";
	private static final String SERVER_URL="server.url";
	private static final String REDIRECT_URL="redirect.url";
	
	// Paypal apis
	private static final String CREATE_PAYMENT = "create.payment";
	
	private String configLocation;
	private String payPalEndPoint;
	private String clientId;
	private String secret;
	private String createPayment;
	private String mode;
	private String serverURL;
	private String redirectURL;
	

	public PaypalConfiguration() {
		
		try {
			
			this.payPalEndPoint = ConfigurationHandler.getValueToConfigurationKey(PAYPAL_ENDPOINT, CONFIG_LOCATION);
			this.clientId = ConfigurationHandler.getValueToConfigurationKey(ROBOADPLACER_CLIENT_ID, CONFIG_LOCATION);
			this.secret = ConfigurationHandler.getValueToConfigurationKey(ROBOADPLACER_SECRET, CONFIG_LOCATION);
			this.createPayment = ConfigurationHandler.getValueToConfigurationKey(CREATE_PAYMENT, CONFIG_LOCATION);
			this.mode=ConfigurationHandler.getValueToConfigurationKey(PAYPAL_PAYMENT_MODE, CONFIG_LOCATION);
			this.serverURL=ConfigurationHandler.getValueToConfigurationKey(SERVER_URL, CONFIG_LOCATION);
			this.redirectURL=ConfigurationHandler.getValueToConfigurationKey(REDIRECT_URL, CONFIG_LOCATION);

			if (!this.payPalEndPoint.isEmpty() && !this.clientId.isEmpty() && !this.secret.isEmpty() && !this.createPayment.isEmpty() && !this.mode.isEmpty() && !this.serverURL.isEmpty()) {
				logger.info("all properties successfully built");
			} else {
				logger.info(new Exception("Excetion Occured while getting configs!!"));
				throw new Exception("Excetion Occured while getting configs!!");
			}

			PrettyPrinterJson.printObject(this);
		} catch (Exception e) {
			logger.debug(e);
		}
	}
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
	public String getCreatePayment() {
		return createPayment;
	}
	public void setCreatePayment(String createPayment) {
		this.createPayment = createPayment;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getServerURL() {
		return serverURL;
	}
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}
	public String getRedirectURL() {
		return redirectURL;
	}
	public void setRedirectURL(String redirectURL) {
		this.redirectURL = redirectURL;
	}
	
	

}
