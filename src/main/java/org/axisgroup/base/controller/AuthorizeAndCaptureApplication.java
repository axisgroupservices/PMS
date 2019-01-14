/**
 * 1. change invoice number of the transactions
 * 2. Comment out the execute payment api 
 * 3. call create payment api and showpaymentdetails api all at once.
 * 4. the approval url you get from showpaymentdetails api response, the customer will use it to pay for the amount. Take this to browser and pay there before calling approve api
 * 5. now comment out create payment api and use showpaymentdetails and executeapprovedPayment.
 * 5. capture the fund.
 */

package org.axisgroup.base.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.axisgroup.confhandler.ConfigurationHandler;
import org.axisgroup.confhandler.PaypalConfiguration;
import org.axisgroup.confhandler.PrettyPrinterJson;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;



public class AuthorizeAndCaptureApplication {


	private static final Logger logger = Logger.getLogger(AuthorizeAndCaptureApplication.class);
	
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {

		PaypalConfiguration paypalConfigs=new PaypalConfiguration();

		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");
		
		// Redirect URLs
		RedirectUrls redirectUrls = new RedirectUrls();
		redirectUrls.setCancelUrl("http://localhost:8080/cancel");
		redirectUrls.setReturnUrl("http://localhost:8080/process");
		
		// Set payment details
		Details details = new Details();
		details.setShipping("0.50");
		details.setSubtotal("4");
		details.setTax("0.04");
		
		// Payment amount
		Amount amount = new Amount();
		amount.setCurrency("USD");
		// Total must be equal to sum of shipping, tax and subtotal.
		amount.setTotal("4.54");
		amount.setDetails(details);

		// Transaction information
		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setDescription("This is the payment transaction description.");

		// Add transaction to a list
		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);

		// Add payment details
		Payment payment = new Payment();
		payment.setIntent("authorize");
		payment.setPayer(payer);
		payment.setTransactions(transactions);
		payment.setRedirectUrls(redirectUrls);
		
		// Create authorization payment
		String clientId=paypalConfigs.getClientId();
		String secret=paypalConfigs.getSecret();
		String mode=paypalConfigs.getMode();
		
		APIContext apiContext = new APIContext(clientId, secret, mode);
		
		  String paymentId=null;
		try {
		Payment createdPayment = payment.create(apiContext);

		 paymentId=createdPayment.getId();
		  
		//  paymentId="PAY-8VH651325G939284WLQYAREI";
		  
		  Payment payment2 = Payment.get(apiContext,
					paymentId);
		
		  
		  logger.info(payment2);
		//approve pay 
		PaymentExecution p =new PaymentExecution();
		p.setPayerId("KCBENVYUARHGN");
		Payment approvedPayment= payment2.execute(apiContext, p);
		
		
		
		logger.info("State "+ approvedPayment.getState());
		
		//Capture
	//	String authorizationID=payment2.getTransactions().get(0).getRelatedResources().get(0).getAuthorization().getId();		
		
		//logger.debug("authorizationID "+authorizationID);
		
	//	Authorization authorization = Authorization.get(apiContext, authorizationID);

		// ###Amount
		// Let's you specify a capture amount.
		Amount amount2 = new Amount();
		amount2.setCurrency("USD");
		amount2.setTotal("4.54");

		// ###Capture
		// A capture transaction
		Capture capture = new Capture();
		capture.setAmount(amount2);
		
		// ##IsFinalCapture
		// If set to true, all remaining 
		// funds held by the authorization 
		// will be released in the funding 
		// instrument. Default is �false�.
		capture.setIsFinalCapture(true);

		// Capture by POSTing to
		// URI v1/payments/authorization/{authorization_id}/capture
		//Capture responseCapture = authorization.capture(apiContext, capture);
		
		
		
		
		
		//Get Capture
		//Capture capture2 = Capture.get(apiContext, "11T8777535119981P");
		

		} catch (PayPalRESTException e) {
		 
		  return;
		}
		
		//Show paymentDetails
		
	
		
	}
	
	
	
}
