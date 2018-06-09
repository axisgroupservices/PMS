package org.axisgroup.base.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.axisgroup.common.dto.PaymentAuthorizationRequest;
import org.axisgroup.confhandler.PaypalConfiguration;
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
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

/**
 * Ordering follow
 * 1. Create Payment with intent authorize
 * 2. Get Authorization ID and use with show authorization details
 * 3. Capture	
 * @author deepakpokhrel
 *
 */


@RestController
@RequestMapping("/payment")
public class AuthorizeAndCaptureController {
	
	
	private static final Logger logger = Logger.getLogger(AuthorizeAndCaptureController.class);


	@RequestMapping("/create-payment")
	public String orderConfirmationAndPayouts(@RequestBody PaymentAuthorizationRequest request) {
		
		String returnURL=null;
		
		try {
			
			PaypalConfiguration paypalConfigs=new PaypalConfiguration();
			
			Payer payer = new Payer();
			payer.setPaymentMethod("paypal");

			// Redirect URLs
			RedirectUrls redirectUrls = new RedirectUrls();
			redirectUrls.setCancelUrl(paypalConfigs.getServerURL()+"/cancel");
			redirectUrls.setReturnUrl(paypalConfigs.getServerURL()+"/PaymentManagement/payment/execute-payment?amount="+request.getAmount()+"&spotId="+ request.getCustom());
			
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
			String clientId=paypalConfigs.getClientId();
			String secret=paypalConfigs.getSecret();
			String mode=paypalConfigs.getMode();
			
			APIContext apiContext = new APIContext(clientId, secret, mode);
			
			String paymentId=null;
		
			 Payment createdPayment = createPayment.create(apiContext);

			 paymentId=createdPayment.getId();
		
			 Payment getDetailsToPayment = Payment.get(apiContext,
						paymentId);
			logger.debug(getDetailsToPayment);
			
			//now retriving approval URL
			returnURL=retrieveReturnURL(getDetailsToPayment);
			
			if(returnURL !=null){
				logger.debug("#############All operation on create payment went successfull##################");
			}else{
				logger.debug("############Didn't retrieve the return URL. Throwing exceptions #########");
			}
		

		} catch (PayPalRESTException e) {

			logger.error(e.getStackTrace());
		}
		
		return returnURL;

		
		}

	@RequestMapping("/execute-payment")
	public void executePayment(
			@RequestParam("paymentId")String paymentId,
			@RequestParam("token") String token,
			@RequestParam("PayerID") String payerID,
			@RequestParam("spotId") String spotId,
			@RequestParam("amount") String amount,
			HttpServletResponse response
			) throws IOException {
		
		PaypalConfiguration paypalConfigs=new PaypalConfiguration();
		
		try {
	
			String clientId=paypalConfigs.getClientId();
			String secret=paypalConfigs.getSecret();
			String mode=paypalConfigs.getMode();
			
			APIContext apiContext = new APIContext(clientId, secret, mode);
			
			Payment getPaymentDetails = Payment.get(apiContext,
						paymentId);

	 
			PaymentExecution p =new PaymentExecution();
			p.setPayerId(payerID);
			Payment approvedPayment= getPaymentDetails.execute(apiContext, p);

			
			String authorizationID=approvedPayment.getTransactions().get(0).getRelatedResources().get(0).getAuthorization().getId();		

		
	logger.debug("Authorization id obtained after executing the payment (should not be null) "+ authorizationID);
	
			if(authorizationID !=null && !authorizationID.isEmpty()){
				
				response.sendRedirect(paypalConfigs.getServerURL()+"/cable/process-order?amount="+amount+"&authorizationId="+authorizationID+"&spotId="+spotId);
			}
	
		} catch (PayPalRESTException e) {

			logger.error(e.getStackTrace());
		
	} catch (Exception e){
		logger.debug(e.getStackTrace());
	}

	      
			
		         
		}
	
	@RequestMapping("/capture-payment")
	public String capturePayment	(
			@RequestParam("authorizationId")String authorizationId,
			@RequestParam("spotId")String spotId,
			@RequestParam("finalAmount")String finalAmount,
			
			HttpServletResponse response
			) throws IOException {
		
		PaypalConfiguration paypalConfigs=new PaypalConfiguration();
		
		String paymentStatus = null;
		byte[] decoded = Base64.decodeBase64(spotId);
		spotId = new String(decoded);

		logger.info("decrypted spotID " + spotId);
		
		try {
			String clientId=paypalConfigs.getClientId();
			String secret=paypalConfigs.getSecret();
			String mode=paypalConfigs.getMode();
			APIContext apiContext = new APIContext(clientId, secret, mode);
			
				Authorization authorization = Authorization.get(apiContext, authorizationId);

				// ###Amount
				// Let's you specify a capture amount.
				Amount amount2 = new Amount();
				amount2.setCurrency("USD");
				amount2.setTotal(finalAmount);

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
				
				if(responseCapture.getState().equals("completed")){
					paymentStatus="Payment is successfully captured";
				}else{
					paymentStatus="Something went wrong. Please call us to resolve the issue !";
				}
	
		} catch (PayPalRESTException e) {

			logger.error(e.getStackTrace());
		} catch (Exception e){
			logger.debug(e.getStackTrace());
		}

	      
			
		     return paymentStatus;    
		}

	private String retrieveReturnURL(Payment getDetailsToPayment) {
		// TODO Auto-generated method stub
		String returnURL=null;
		if(getDetailsToPayment !=null){
			if(getDetailsToPayment.getLinks() !=null && getDetailsToPayment.getLinks().size() >0){
				for(Links link : getDetailsToPayment.getLinks()){
					if(link !=null){
						if(link.getRel().equals("approval_url")){
							returnURL=link.getHref();
						}
					}
				}
			}
		}
		return returnURL;
	}

	
	
	
	

}
