package org.axisgroup.base.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.axisgroup.common.dto.Amount;
import org.axisgroup.confhandler.ConfigurationHandler;
import org.axisgroup.confhandler.PrettyPrinterJson;
import org.axisgroup.paypal.payouts.request.PaypalPayoutRequest;
import org.axisgroup.paypal.payouts.response.PaypalPayoutResponse;
import org.axisgroup.paypal.utils.PayOutApplication;
import org.axisgroup.paypal.utils.PayoutStakeHolderInfo;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;



@RestController
@RequestMapping("/order")
public class PayoutController {

	private static final Logger logger = Logger.getLogger(PayoutController.class);
	private static final String SENDER_PAYPAL_EMAIL = "sender.paypalinfo";
	//private static final String RECEIVER_PAYPAL_EMAIL = "receiver.paypalinfo";
	private static final String CONFIG_LOCATION = "env.properties";
	

	@RequestMapping("/payouts")
	public String payouts(
			@RequestParam("orderId") String contractId,
			@RequestParam("trans-amount") String transAmount,
			@RequestParam("note") String note, @RequestParam("seller") String sellerPaypal) {
		String paymentStatus = null; 
		try {
					String sender = ConfigurationHandler
							.getValueToConfigurationKey(SENDER_PAYPAL_EMAIL, CONFIG_LOCATION);
				
					paymentStatus = transferPayment(note, transAmount, contractId, sender, sellerPaypal);
				
		} catch (Exception e) {

			logger.error("Exception Occured", e);
		}
		logger.info("The payment status of payouts api is " + paymentStatus);
		return paymentStatus;
	}

	private  String transferPayment(String note, String transerBalance, String contractId,
			String sender, String receiver) {
		String paymentStatus = null;

			try{
							PaypalPayoutResponse response = transfer(receiver,
									sender, transerBalance, note, contractId);
							if (response.getError() == null) {
								paymentStatus = "Payment was successfully completed. No action is needed";
								//update payment status to db
							} else {
								paymentStatus = "Something went wrong. Please, contact Administration. No balance transfered";
							}
				} catch (Exception e) {
					logger.error("Exception occured" ,e);

					paymentStatus = "Error occured no balance was transfered. Please, contact Administration";

				}
			

		return paymentStatus;

	}

	private  PaypalPayoutResponse transfer(String receiver,
			String sender, String transferBalance, String note, String contractId)
			throws JsonGenerationException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		logger.info("Amount that is transfered to payee-entity " + receiver + " is " + transferBalance);
		String emailSubject = note;
		String reciepient_type;
		if (receiver.contains("@")) {
			reciepient_type = "EMAIL";

		} else {
			reciepient_type = "PHONE";
		}

		logger.info("Recipient type is "+ reciepient_type);
		PayOutApplication apps = new PayOutApplication();

		List<PayoutStakeHolderInfo> stakeHoldersInfo = new ArrayList<>();
		PayoutStakeHolderInfo stakeHolder = new PayoutStakeHolderInfo();
		stakeHolder.setPaymentNote(note);
		Amount amount = new Amount();
		amount.setCurrency("USD");
		amount.setValue(transferBalance);

		stakeHolder.setPayoutAmount(amount);
		stakeHolder.setSenderItemId(contractId);
		stakeHolder.setRecipeintType(reciepient_type);
		stakeHolder.setPayPalAccountEmail(receiver);

		stakeHoldersInfo.add(stakeHolder);

		PaypalPayoutRequest request = apps.createPaypalPayoutRequest( emailSubject,
				stakeHoldersInfo,contractId);

		PrettyPrinterJson.printObject(request);

		PaypalPayoutResponse response = apps.payOut(request);

		return response;

	}

	/**
	 * @param spotId
	 * @return
	 */
	private static boolean savePaymentConfirmationToDB(String orderId) {
		
		Boolean isPaymentSaved = false;
		try {
			String url = ConfigurationHandler.getValueToConfigurationKey("server.url", "env.properties");
			String resourcePath = "/RoboAdPlacer-ApiServices/insertpayment/order/{spotId}";
			String endPoint = url + resourcePath;

			Map<String, String> uriParams = new HashMap<String, String>();
			uriParams.put("spotId", orderId);
			RestTemplate template = new RestTemplate();
			isPaymentSaved = template.getForObject(endPoint, Boolean.class, uriParams);
			logger.info(
					"Payment for OrderId " + orderId + " is saved. Is payment saved (must Be true) ? " + isPaymentSaved);
		} catch (HttpClientErrorException e) {
			logger.error(e);
		}
		return isPaymentSaved;
	}
}
