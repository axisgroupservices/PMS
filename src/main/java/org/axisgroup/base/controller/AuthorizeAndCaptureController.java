package org.axisgroup.base.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.axisgroup.common.dto.PaymentAuthorizationRequest;
import org.axisgroup.confhandler.PaypalConfiguration;
import org.axisgroup.confhandler.PrettyPrinterJson;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Authorization;
import com.paypal.api.payments.Capture;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.codec.binary.Base64;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

/**
 * Ordering follow 1. Create Payment with intent authorize 2. Get Authorization
 * ID and use with show authorization details 3. Capture
 * 
 * @author deepakpokhrel
 *
 */

@RestController
@RequestMapping("/payment")
public class AuthorizeAndCaptureController {

	private static final Logger logger = Logger.getLogger(AuthorizeAndCaptureController.class);

	@RequestMapping("/create-payment")
	public ResponseEntity<String> orderConfirmationAndPayouts(@RequestBody PaymentAuthorizationRequest request,
			HttpServletResponse response) throws JsonGenerationException, JsonMappingException, IOException {

		String returnURL = null;
		PaypalConfiguration paypalConfigs = new PaypalConfiguration();

		logger.debug("print request");
		PrettyPrinterJson.printObject(request);

		try { 

			Payer payer = new Payer();
			payer.setPaymentMethod("paypal");
			
			// Redirect URLs
			RedirectUrls redirectUrls = new RedirectUrls();
			redirectUrls.setCancelUrl(paypalConfigs.getServerURL() + "/cancel");
			redirectUrls
					.setReturnUrl(paypalConfigs.getServerURL() + "pms-advertising/payment/execute-payment?amount="
							+ request.getAmount() + "&orderId=" + request.getCustom()+"&sellerEmail="+request.getOwnerEmail()+"&buyerEmail="+request.getBuyerEmail());

			// Set payment details
			Details details = new Details();
			details.setShipping("0.0");
			details.setSubtotal("0.0");
			details.setTax("0.0");

			// Payment amount
			Amount amount = new Amount();
			amount.setCurrency("USD");
			// Total must be equal to sum of shipping, tax and subtotal.
			amount.setTotal(request.getAmount());
			amount.setDetails(details);

			// Transaction information
			Transaction transaction = new Transaction();
			transaction.setAmount(amount);
			transaction.setDescription("Payment for the order!");

			// Add transaction to a list
			List<Transaction> transactions = new ArrayList<Transaction>();
			transactions.add(transaction);

			// Add payment details
			Payment createPayment = new Payment();
			createPayment.setIntent("authorize");
			createPayment.setPayer(payer);
			createPayment.setTransactions(transactions);
			createPayment.setRedirectUrls(redirectUrls);

			// Create authorization payment
			String clientId = paypalConfigs.getClientId();
			String secret = paypalConfigs.getSecret();
			String mode = paypalConfigs.getMode();

			APIContext apiContext = new APIContext(clientId, secret, mode);

			String paymentId = null;

			Payment createdPayment = createPayment.create(apiContext);

			paymentId = createdPayment.getId();

			Payment getDetailsToPayment = Payment.get(apiContext, paymentId);

			logger.debug("Request" + request);
			PrettyPrinterJson.printObject(request);

			logger.debug(getDetailsToPayment);

			// now retriving approval URL
			returnURL = retrieveReturnURL(getDetailsToPayment);

			if (returnURL != null) {
				logger.debug("#############All operation on create payment went successfull##################");
			} else {
				logger.debug("############Didn't retrieve the return URL. Throwing exceptions #########");
				throw new Exception("return URL not formed correctly ");
			}

		} catch (PayPalRESTException e) {

			logger.error("Paypal Rest Exception occured", e);
		} catch (Exception e) {
			logger.error("General Exception occured", e);
		}

		// response.sendRedirect(paypalConfigs.getServerURL()+"/"+returnURL);
		return new ResponseEntity<>(returnURL, HttpStatus.OK);
	}

	@RequestMapping("/execute-payment")
	public void executePayment(@RequestParam("paymentId") String paymentId, @RequestParam("token") String token,
			@RequestParam("PayerID") String payerID, @RequestParam("orderId") String orderId,
			@RequestParam("amount") String amount,
			@RequestParam("sellerEmail") String sellerEmail,
			@RequestParam("buyerEmail") String buyerEmail,
			
			HttpServletResponse response) throws IOException {
		PaypalConfiguration paypalConfigs = new PaypalConfiguration();
		try {
			String clientId = paypalConfigs.getClientId();
			String secret = paypalConfigs.getSecret();
			String mode = paypalConfigs.getMode();
			APIContext apiContext = new APIContext(clientId, secret, mode);
			Payment getPaymentDetails = Payment.get(apiContext, paymentId);
			PaymentExecution p = new PaymentExecution();
			p.setPayerId(payerID);
			Payment approvedPayment = getPaymentDetails.execute(apiContext, p);
			String authorizationID = approvedPayment.getTransactions().get(0).getRelatedResources().get(0)
					.getAuthorization().getId();
			logger.debug(
					"Authorization id obtained after executing the payment (should not be null) " + authorizationID);
			if (authorizationID != null && !authorizationID.isEmpty()) {
				response.sendRedirect(paypalConfigs.getRedirectURL() + "/oms-advertising/process-order?amount=" + amount
						+ "&authorizationId=" + authorizationID + "&orderId=" + orderId+"&sellerEmail="+sellerEmail+"&buyerEmail="+buyerEmail);
			}
		} catch (PayPalRESTException e) {
			logger.error("Exception occured", e);
		} catch (Exception e) {
			logger.error("Exception occured", e);
		}

	}

	@RequestMapping("/capture-payment")
	public String capturePayment(@RequestParam("authorizationId") String authorizationId,
			@RequestParam("orderId") String orderId, @RequestParam("finalAmount") String finalAmount, 
			@RequestParam("seller")String sellerPaypal,

		HttpServletResponse response) throws IOException {

		PaypalConfiguration paypalConfigs = new PaypalConfiguration();

		String paymentStatus = null;

		logger.info("decrypted spotID " + orderId);

		try {
			String clientId = paypalConfigs.getClientId();
			String secret = paypalConfigs.getSecret();
			String mode = paypalConfigs.getMode();
			APIContext apiContext = new APIContext(clientId, secret, mode);
			double value = Double.valueOf(finalAmount);
			String formattedAmount=String.format("%.2f", value);
			logger.info("Formatted Price "+ formattedAmount);
			Authorization authorization = Authorization.get(apiContext, authorizationId);

			// ###Amount
			// Let's you specify a capture amount.
			Amount amount2 = new Amount();
			amount2.setCurrency("USD");
		
			amount2.setTotal(formattedAmount);

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
			Capture responseCapture = authorization.capture(apiContext, capture);

			if (responseCapture.getState().equals("completed")) {
				paymentStatus = "Payment is successfully captured for order Id "+ orderId;
				
				//Now transfering to the webmaster mail
				
				PayoutController payoutController= new PayoutController();
				paymentStatus=payoutController.payouts(orderId, sellerPaypal,formattedAmount, "Payment made for orderId "+orderId);
			} else {
				paymentStatus = "Something went wrong. Please call us to resolve the issue !";
			}

		} catch (PayPalRESTException e) {

			logger.error("Exception Occurred", e);
		} catch (Exception e) {
			logger.error("Exception Occurred", e);
		}

		return paymentStatus;
	}

	private String retrieveReturnURL(Payment getDetailsToPayment) {
		// TODO Auto-generated method stub
		String returnURL = null;
		if (getDetailsToPayment != null) {
			if (getDetailsToPayment.getLinks() != null && getDetailsToPayment.getLinks().size() > 0) {
				for (Links link : getDetailsToPayment.getLinks()) {
					if (link != null) {
						if (link.getRel().equals("approval_url")) {
							returnURL = link.getHref();
							logger.info("Returned URL is "+ returnURL);
						}
					}
				}
			}
		}
		return returnURL;
	}

}
