package org.axisgroup.base.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.axisgroup.client.response.GetContractInfoBySpotIdResponse;
import org.axisgroup.common.dto.Amount;
import org.axisgroup.confhandler.ConfigurationHandler;
import org.axisgroup.confhandler.PayOutApplication;
import org.axisgroup.confhandler.PrettyPrinterJson;
import org.axisgroup.paypal.payouts.request.PaypalPayoutRequest;
import org.axisgroup.paypal.payouts.response.PaypalPayoutResponse;
import org.axisgroup.paypal.utils.PayoutStakeHolderInfo;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.roboadplacer.request.GetContractsInfoBySpotId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * 1. Get Contract by SpotID 2. Transfer balance to PyAdvertising from
 * Roboadplacer 3. Wait for 15 seconds 4. Transfer balance to CableOperator.
 */

@RestController
@RequestMapping("/orderConfirmation")
public class OrderConfirmationAndPaymentController {

	private static final Logger logger = Logger.getLogger(OrderConfirmationAndPaymentController.class);
	private static final String PYADVERTISING_PAYPAL_ACCOUNT = "pyadvertising.paypalinfo";
	// private static final String ROBOADPLACER_PAYPAL_ACCOUNT =
	// "pyadvertising.paypalinfo";
	private static final String CONFIG_LOCATION = "env.properties";
	private static final String[] PAYEE_ENTITIES = { "ROBOADPLACER", "PYADVERTISING" };

	@RequestMapping("/payouts")
	public String orderConfirmationAndPayouts(@RequestParam("spotid") String spotId) {
		String paymentStatus = null;
		byte[] decoded = Base64.decodeBase64(spotId);
		spotId = new String(decoded);

		logger.info("decrypted spotID" + spotId);

		try {
			logger.info("Inside orderConfirmationAndPayouts");
			logger.info("SpotID from frontend " + spotId);

			String endPointForRoboAdplacer = ConfigurationHandler.getValueToConfigurationKey("server.url",
					CONFIG_LOCATION);

			GetContractsInfoBySpotId getContractsInfoBySpotIdRequest = new GetContractsInfoBySpotId();
			getContractsInfoBySpotIdRequest.setSpotId(spotId);

			RestTemplate restTemplate = new RestTemplate();
			GetContractInfoBySpotIdResponse getContractInfoBySpotIdResponse = (GetContractInfoBySpotIdResponse) restTemplate
					.postForObject(endPointForRoboAdplacer + "/RoboAdPlacer-ApiServices/getcontractinfo/email",
							getContractsInfoBySpotIdRequest, GetContractInfoBySpotIdResponse.class);

			PrettyPrinterJson.printObject(getContractInfoBySpotIdResponse);

			List<String> splitsPaymentsStakeHolders = generateSplitAmountToStakeHolders(
					getContractInfoBySpotIdResponse);

			String dollarsToTransfer = null;

			for (String payeeEntity : PAYEE_ENTITIES) {
				String note = null;
				if (payeeEntity.equals("ROBOADPLACER")) {
					note = "Balance Transfered to PyAdverting for order number " + spotId + ".";
					dollarsToTransfer = splitsPaymentsStakeHolders.get(1);

					String payeeEntityPayPalInfo = ConfigurationHandler
							.getValueToConfigurationKey(PYADVERTISING_PAYPAL_ACCOUNT, CONFIG_LOCATION);
					paymentStatus = transferPayment(note, dollarsToTransfer, spotId, payeeEntityPayPalInfo, payeeEntity,
							getContractInfoBySpotIdResponse);

					// stop thread for 10 seconds
					long start = System.currentTimeMillis();
					long end = start + 15 * 1000; // 60 seconds * 1000 ms/sec

					while (System.currentTimeMillis() < end) {

					}
					logger.info("15 secs waiting period completed!!");
				} else if (payeeEntity.equals("PYADVERTISING")) {
					note = "Balance transfered for order number " + spotId + ".";
					dollarsToTransfer = splitsPaymentsStakeHolders.get(2);
					String cableOpeartorPaypalInfo = getCableOperatorPaypalInfo(getContractInfoBySpotIdResponse);
					if (StringUtils.isNotBlank(cableOpeartorPaypalInfo)) {
						paymentStatus = transferPayment(note, dollarsToTransfer, spotId, cableOpeartorPaypalInfo,
								payeeEntity, getContractInfoBySpotIdResponse);
					}
				}

			}

			if (StringUtils.isBlank(paymentStatus)) {
				paymentStatus = "Paypal Info of cable operator is null. No balance transfered. Please, check Pyadvertising for manual transfer";
			}

		} catch (Exception e) {

			logger.debug(e.getStackTrace());
		}

		logger.info("The status of payouts api is " + paymentStatus);
		return paymentStatus;
	}

	private static String getCableOperatorPaypalInfo(GetContractInfoBySpotIdResponse getContractInfoBySpotIdResponse) {
		// TODO Auto-generated method stub
		String cableOpEmail = null;
		if (getContractInfoBySpotIdResponse != null) {
			if (getContractInfoBySpotIdResponse.getContract() != null) {
				if (getContractInfoBySpotIdResponse.getContract().isPayPalExists()) {
					cableOpEmail = getContractInfoBySpotIdResponse.getContract().getPayPalAccountInfo();
				}
			}
		}
		return cableOpEmail;
	}

	private static String transferPayment(String note, String dollarsToTransfer, String spotId,
			String payeeEntityPaypalInfo, String payeeEntity,
			GetContractInfoBySpotIdResponse getContractInfoBySpotIdResponse) {
		String paymentStatus = null;
		if (getContractInfoBySpotIdResponse != null && getContractInfoBySpotIdResponse.getContract() != null) {
			if (!getContractInfoBySpotIdResponse.getContract().isSpotPaid()) {
				try {
					logger.info("Paypal-Info of payee-entity that the balance is being transfered is "
							+ payeeEntityPaypalInfo);
					if (StringUtils.isNotBlank(payeeEntityPaypalInfo)) {
						if (dollarsToTransfer != null) {
							PaypalPayoutResponse response = transferPayPalBalanceToPayeeEntity(payeeEntity,
									payeeEntityPaypalInfo, dollarsToTransfer, note, spotId);
							if (response.getError() == null) {
								paymentStatus = "Payment was successfully completed. No action is needed";

								if (payeeEntity.equals("PYADVERTISING")) {
									boolean isPaymentSavedToDB = savePaymentConfirmationToDB(spotId);
									if (!isPaymentSavedToDB) {
										logger.error(
												"Error occured for ordernumber while processing payment. Must resolve manually. SpotID db status: isPaidTocableOperator could not be updated for order numbers! "
														+ spotId);
										paymentStatus = "Fatal error occurred!s";
									}
								}
							} else {
								paymentStatus = "Something went wrong. Please, contact PyAdvertising. No balance transfered";
							}

						}

					}

				} catch (Exception e) {
					logger.debug(e);

					paymentStatus = "Error occured no balance was transfered. Please, contact PyAdvertising";

				}
			}

			else {
				paymentStatus = "The payment to this order id is already completed.";
			}
		}

		return paymentStatus;

	}

	private static PaypalPayoutResponse transferPayPalBalanceToPayeeEntity(String payeeEntity,
			String payeeEntityPaypalInfo, String dollarsToTransfer, String note, String spotId)
			throws JsonGenerationException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		logger.info("Amount that is transfered to payee-entity " + payeeEntityPaypalInfo + " is " + dollarsToTransfer);
		String emailSubject = note;
		String reciepient_type;
		if (payeeEntityPaypalInfo.contains("@")) {
			reciepient_type = "EMAIL";

		} else {
			reciepient_type = "PHONE";
		}

		PayOutApplication apps = new PayOutApplication();

		List<PayoutStakeHolderInfo> stakeHoldersInfo = new ArrayList<>();
		PayoutStakeHolderInfo stakeHolder = new PayoutStakeHolderInfo();
		stakeHolder.setPaymentNote(note);
		Amount amount = new Amount();
		amount.setCurrency("USD");
		amount.setValue(dollarsToTransfer);

		stakeHolder.setPayoutAmount(amount);
		stakeHolder.setSenderItemId(spotId);
		stakeHolder.setRecipeintType(reciepient_type);
		stakeHolder.setPayPalAccountEmail(payeeEntityPaypalInfo);

		stakeHoldersInfo.add(stakeHolder);

		PaypalPayoutRequest request = apps.createPaypalPayoutRequest( emailSubject,
				stakeHoldersInfo,spotId);

		PrettyPrinterJson.printObject(request);

		PaypalPayoutResponse response = apps.payOut(request, payeeEntity);

		return response;

	}

	/**
	 * @param spotId
	 * @return
	 */
	private static boolean savePaymentConfirmationToDB(String spotId) {
		// TODO Auto-generated method stub
		Boolean isPaymentSaved = false;
		try {
			String url = ConfigurationHandler.getValueToConfigurationKey("server.url", "env.properties");
			String resourcePath = "/RoboAdPlacer-ApiServices/insertpayment/order/{spotId}";
			String endPoint = url + resourcePath;

			Map<String, String> uriParams = new HashMap<String, String>();
			uriParams.put("spotId", spotId);
			RestTemplate template = new RestTemplate();
			isPaymentSaved = template.getForObject(endPoint, Boolean.class, uriParams);
			logger.info(
					"Payment for OrderId " + spotId + " is saved. Is payment saved (must Be true) ? " + isPaymentSaved);
		} catch (HttpClientErrorException e) {
			logger.error(e);
		}
		return isPaymentSaved;
	}

	private static List<String> generateSplitAmountToStakeHolders(GetContractInfoBySpotIdResponse response) {
		List<String> priceSplitsHolder = null;

		if (response != null) {
			if (response.getContract() != null) {
				double amountCollected = response.getContract().getPrice();
				if (amountCollected != 0.0) {

					priceSplitsHolder = splitPayments(amountCollected);

				}
			}

		}

		return priceSplitsHolder;
	}

	private static List<String> splitPayments(double amountCollected) {
		String getCommisionPercent = ConfigurationHandler.getValueToConfigurationKey("commision.percent",
				"env.properties");
		String balanceTransferToOperator = null;
		String amountCollectedString = null;
		String amountToPyadvertisingString = null;
		String totalCashRoboAdPlacer = null;
		List<String> priceSplitsHolder = null;

		if (StringUtils.isNotBlank(getCommisionPercent)) {
			priceSplitsHolder = new ArrayList<>();
			Double chargedPercent = 1 + Double.parseDouble(getCommisionPercent) / 100;

			DecimalFormat df = new DecimalFormat("#.00");

			Double totalCash = amountCollected;
			double amountToPyadvertising = totalCash - 0.25;
			Double amountToCableOprator = (amountToPyadvertising + 0.25) / chargedPercent;

			balanceTransferToOperator = df.format(amountToCableOprator);
			amountCollectedString = df.format(amountCollected);
			amountToPyadvertisingString = df.format(amountToPyadvertising);
			totalCashRoboAdPlacer = df.format(amountCollected);

			logger.info("Balance transfering to CableOpeartor is " + balanceTransferToOperator + " out of "
					+ amountCollectedString + " and to pyAdvertising after 0.25 cent paypal charge is "
					+ amountToPyadvertisingString + " from Roboadplacer which has" + " " + totalCashRoboAdPlacer
					+ " and new balance to Pyadvertising (commisssion ) will be "
					+ (amountToPyadvertising - amountToCableOprator));

			priceSplitsHolder.add(amountCollectedString);
			priceSplitsHolder.add(amountToPyadvertisingString);
			priceSplitsHolder.add(balanceTransferToOperator);

		}
		return priceSplitsHolder;
	}

	public static void main(String[] args) {
		// This payouts by spotIDs
		// 63= NjM=

		String spotId = "62";
		String paymentStatus = null;

		try {
			logger.info("Inside orderConfirmationAndPayouts");
			logger.info("SpotID from frontend " + spotId);

			String endPointForRoboAdplacer = ConfigurationHandler.getValueToConfigurationKey("server.url",
					CONFIG_LOCATION);

			GetContractsInfoBySpotId getContractsInfoBySpotIdRequest = new GetContractsInfoBySpotId();
			getContractsInfoBySpotIdRequest.setSpotId(spotId);

			RestTemplate restTemplate = new RestTemplate();
			GetContractInfoBySpotIdResponse getContractInfoBySpotIdResponse = (GetContractInfoBySpotIdResponse) restTemplate
					.postForObject(endPointForRoboAdplacer + "/RoboAdPlacer-ApiServices/getcontractinfo/email",
							getContractsInfoBySpotIdRequest, GetContractInfoBySpotIdResponse.class);

			PrettyPrinterJson.printObject(getContractInfoBySpotIdResponse);

			List<String> splitsPaymentsStakeHolders = generateSplitAmountToStakeHolders(
					getContractInfoBySpotIdResponse);

			// manipulate samplae values
			splitsPaymentsStakeHolders.add(0, "10.00");
			splitsPaymentsStakeHolders.add(1, "9.75");
			splitsPaymentsStakeHolders.add(2, "9.10");

			String dollarsToTransfer = null;

			for (String payeeEntity : PAYEE_ENTITIES) {
				String note = null;
				if (payeeEntity.equals("ROBOADPLACER")) {
					note = "Balance Transfered to PyAdverting for order number " + spotId + ".";
					dollarsToTransfer = splitsPaymentsStakeHolders.get(1);

					String payeeEntityPayPalInfo = ConfigurationHandler
							.getValueToConfigurationKey(PYADVERTISING_PAYPAL_ACCOUNT, CONFIG_LOCATION);
					paymentStatus = transferPayment(note, dollarsToTransfer, spotId, payeeEntityPayPalInfo, payeeEntity,
							getContractInfoBySpotIdResponse);

					// stop thread for 10 seconds
					long start = System.currentTimeMillis();
					long end = start + 15 * 1000; // 60 seconds * 1000 ms/sec

					while (System.currentTimeMillis() < end) {

					}
					logger.info("15 secs waiting period completed!!");
				} else if (payeeEntity.equals("PYADVERTISING")) {
					note = "Balance transfered for order number " + spotId + ".";
					dollarsToTransfer = splitsPaymentsStakeHolders.get(2);
					String cableOpeartorPaypalInfo = getCableOperatorPaypalInfo(getContractInfoBySpotIdResponse);
					logger.info("Transfering balance to " + cableOpeartorPaypalInfo);
					cableOpeartorPaypalInfo = "payments-buyer@roboadplacer.com";
					if (StringUtils.isNotBlank(cableOpeartorPaypalInfo)) {
						paymentStatus = transferPayment(note, dollarsToTransfer, spotId, cableOpeartorPaypalInfo,
								payeeEntity, getContractInfoBySpotIdResponse);
					}
				}

			}

			if (StringUtils.isBlank(paymentStatus)) {
				paymentStatus = "Something went wrong. No balance transfered. Please, check Pyadvertising for manual transfer";
			}

		} catch (Exception e) {

			logger.debug(e.getStackTrace());
		}

		logger.info("The status of payouts api is " + paymentStatus);
	}

}
