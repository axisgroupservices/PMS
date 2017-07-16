package org.axisgroup.base.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
import org.roboadplacer.request.GetContractsInfoBySpotId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/orderConfirmation")
public class OrderConfirmationAndPaymentController {

	private static final Logger logger = Logger.getLogger(OrderConfirmationAndPaymentController.class);
	private static final String PYADVERTISING_PAYPAL_ACCOUNT = "pyadvertising.paypalinfo";
	private static final String CONFIG_LOCATION = "env.properties";

	@RequestMapping("/payouts")
	public String orderConfirmationAndPayouts(@RequestParam("spotid") String spotId) {
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

			paymentStatus = splitPayments(spotId, getContractInfoBySpotIdResponse);

		}
		catch (Exception e) {

			logger.debug(e);
		}
		
		logger.info("The status of payouts api is " +paymentStatus);
		return paymentStatus;
	}

	private String splitPayments(String spotId, GetContractInfoBySpotIdResponse getContractInfoBySpotIdResponse) {
		String paymentStatus = null;
		if (getContractInfoBySpotIdResponse != null && getContractInfoBySpotIdResponse.getContract() != null) {
			if (getContractInfoBySpotIdResponse.getContract().isPayPalExists()==false) {
				try {
					String cableOperatorPaypalInfo = getContractInfoBySpotIdResponse.getContract()
							.getPayPalAccountInfo();
					logger.info("Paypal Info of cable operator is "+cableOperatorPaypalInfo);

					String pyAdvertisingPaypalInfo = ConfigurationHandler
							.getValueToConfigurationKey(PYADVERTISING_PAYPAL_ACCOUNT, CONFIG_LOCATION);

					List<String> splitsPaymentsStakeHolders = generateSplitAmountToStakeHolders(getContractInfoBySpotIdResponse);

					if (splitsPaymentsStakeHolders != null && splitsPaymentsStakeHolders.size() > 1) {
						logger.info("Pyadvertising paypalinfo from config " + pyAdvertisingPaypalInfo);

						logger.info(
								"Amount that is transfered to cable operator is " + splitsPaymentsStakeHolders.get(0));
						logger.info(
								"Amount that is transfered to pyAdvertising is " + splitsPaymentsStakeHolders.get(1));

						String amountToCableOperator = splitsPaymentsStakeHolders.get(0);
						String amountToPyAdvertising = splitsPaymentsStakeHolders.get(1);

						String uniqueBatchIdperMonth = spotId;
						String emailSubject = "PyAdvertising Invoice Payment for Order Number " + spotId;

						String reciepient_type;
						if (cableOperatorPaypalInfo.contains("@")) {
							reciepient_type = "EMAIL";

						} else {
							reciepient_type = "PHONE";
						}

						PayOutApplication apps = new PayOutApplication();

						List<PayoutStakeHolderInfo> stakeHoldersInfo = new ArrayList<>();
						PayoutStakeHolderInfo stakeHolder = new PayoutStakeHolderInfo();
						stakeHolder.setPaymentNote("This is a payment for Order number " + spotId);
						Amount amount = new Amount();
						amount.setCurrency("USD");
						amount.setValue(amountToCableOperator);

						stakeHolder.setPayoutAmount(amount);
						stakeHolder.setSenderItemId(spotId);
						stakeHolder.setRecipeintType(reciepient_type);
						stakeHolder.setPayPalAccountEmail(cableOperatorPaypalInfo);

						PayoutStakeHolderInfo stakeHolder2 = new PayoutStakeHolderInfo();
						stakeHolder2.setPaymentNote("This is a comission for Order number" + spotId);
						Amount amount2 = new Amount();
						amount2.setCurrency("USD");
						amount2.setValue(amountToPyAdvertising);

						stakeHolder2.setPayoutAmount(amount2);
						stakeHolder2.setSenderItemId(spotId);
						stakeHolder2.setRecipeintType("EMAIL");
						stakeHolder2.setPayPalAccountEmail(pyAdvertisingPaypalInfo);

						stakeHoldersInfo.add(stakeHolder);
						stakeHoldersInfo.add(stakeHolder2);

						// Create request
						PaypalPayoutRequest request = apps.createPaypalPayoutRequest(uniqueBatchIdperMonth,
								emailSubject, stakeHoldersInfo);

						PrettyPrinterJson.printObject(request);
						// generate response sends amount to multiple user.
						PaypalPayoutResponse response = apps.payOut(request);

						PrettyPrinterJson.printObject(response);

						if (response.getError() == null) {
							paymentStatus = "Payment was successfully completed. No action is needed";
						} else {
							paymentStatus = "Something went wrong. Please, contact PyAdvertising. No balance transfered";
						}

					}

				} catch (Exception e) {
					logger.debug(e);

					paymentStatus = "Error occured no balance was transfered. Please, contact PyAdvertising";

				}

			}
		}

		return paymentStatus;

	}

	private static List<String> generateSplitAmountToStakeHolders(GetContractInfoBySpotIdResponse response) {
		List<String> priceSplitsHolder = null;
		String getCommisionPercent = ConfigurationHandler.getValueToConfigurationKey("commision.percent",
				"env.properties");
		String balanceTransferToOperator = null;
		String amountCollectedString = null;
		String amountToPyadvertisingString = null;

		if (response != null) {
			if (response.getContract() != null) {
				double amountCollected = response.getContract().getPrice();
				if (amountCollected != 0.0) {
					if (StringUtils.isNotBlank(getCommisionPercent)) {

						priceSplitsHolder = new ArrayList<>();
						Double chargedPercent = 1 + Double.parseDouble(getCommisionPercent) / 100;

						DecimalFormat df = new DecimalFormat("#.00");

						Double amountToCableOprator = (amountCollected / chargedPercent);
						double amountToPyadvertising = amountCollected - amountToCableOprator;

						balanceTransferToOperator = df.format(amountToCableOprator);
						amountCollectedString = df.format(amountCollected);
						amountToPyadvertisingString = df.format(amountToPyadvertising);

						logger.info("Balance transfering to CableOpeartor is " + balanceTransferToOperator + " out of "
								+ amountCollectedString + " and commision of " + getCommisionPercent
								+ "% to pyAdvertising is " + amountToPyadvertisingString);

						priceSplitsHolder.add(balanceTransferToOperator);
						priceSplitsHolder.add(amountToPyadvertisingString);
					}
				}

			}
		}
		return priceSplitsHolder;
	}

	public static void main(String[] args) {
		OrderConfirmationAndPaymentController controller = new OrderConfirmationAndPaymentController();
		controller.orderConfirmationAndPayouts("49");
	}

}
