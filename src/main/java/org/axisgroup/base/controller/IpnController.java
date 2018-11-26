package org.axisgroup.base.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.axisgroup.confhandler.ConfigurationHandler;
import org.axisgroup.paypal.payouts.response.PaymentStatusResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IpnController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private final static Logger logger = Logger.getLogger(IpnController.class);
	private static final String PAYPAL_URL_MODE = "paypal.payment.mode";
	private static final String CONFIG_LOCATION = "env.properties";

	
	@RequestMapping("/ipn")
	public PaymentStatusResponse ipn(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		logger.debug("Get here successfully");
		PaymentStatusResponse paymentStatus= new PaymentStatusResponse();
		Map<String, String> formParams = new HashMap<String, String>();
		StringBuilder strBuffer = new StringBuilder("cmd=_notify-validate");
		String paramName;
		String paramValue;
		Enumeration<?> en = request.getParameterNames();
		while (en.hasMoreElements()) {
			paramName = (String) en.nextElement();
			paramValue = request.getParameter(paramName);
			strBuffer.append("&").append(paramName).append("=").append(URLEncoder.encode(paramValue, "UTF-8"));
			formParams.put(paramName, paramValue);
		}
		URL u = null;
		String paypalMode = ConfigurationHandler.getValueToConfigurationKey(PAYPAL_URL_MODE, CONFIG_LOCATION);
		if (paypalMode.equals("sandbox")) {
			u = new URL("https://www.sandbox.paypal.com/cgi-bin/webscr");
		} else if (paypalMode.equals("prod")) {
			u = new URL("https://www.paypal.com/cgi-bin/webscr");
		} else {
			logger.error("Incorrect paypal mode " + paypalMode);
		}

		HttpURLConnection uc = (HttpURLConnection) u.openConnection();
		uc.setDoOutput(true);
		uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		uc.setRequestProperty("Host", "www.paypal.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		String res = in.readLine();
		in.close();
		if (res.equals("VERIFIED")) {
			logger.info("Response verified");
			paymentStatus.setResponseVerified("VERIFIED");
			if (request.getParameter("payment_status").equalsIgnoreCase("COMPLETED")) {
				logger.info("Payment status completed");
				paymentStatus.setPaymentStatus("COMPLETED");
				paymentStatus.setTransactionId(request.getParameter("txn_id"));
				paymentStatus.setReceiverEmail(request.getParameter("receiver_email"));
				paymentStatus.setCurrency(request.getParameter("mc_currency"));
				paymentStatus.setOrderId(request.getParameter("custom"));	
			}
		}
		
		return paymentStatus;

	}
	
	
}
