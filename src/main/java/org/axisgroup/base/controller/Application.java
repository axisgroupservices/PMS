
/*package org.axisgroup.base.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.axisgroup.client.response.GetContractInfoBySpotIdResponse;
import org.axisgroup.confhandler.ConfigurationHandler;
import org.axisgroup.confhandler.PrettyPrinterJson;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.roboadplacer.request.GetContractsInfoBySpotId;
import org.springframework.web.client.RestTemplate;

public class Application {
	private static final Logger logger = Logger.getLogger(AccountController.class);
/*
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		
		GetContractsInfoBySpotId getContractsInfoBySpotIdRequest = new GetContractsInfoBySpotId();
		getContractsInfoBySpotIdRequest.setSpotId("45");

		RestTemplate restTemplate = new RestTemplate();
		GetContractInfoBySpotIdResponse response = (GetContractInfoBySpotIdResponse) restTemplate.postForObject(
				"http://kite.infinite-data.com:8080/RoboAdPlacer-ApiServices/getcontractinfo/email",
				getContractsInfoBySpotIdRequest, GetContractInfoBySpotIdResponse.class);

		PrettyPrinterJson.printObject(response);

		List<String> paymentsplits = getAmountToCableOperator(response);

		if (paymentsplits != null && response !=null) {
			

		}

	}


	private static List<String> getAmountToCableOperator(GetContractInfoBySpotIdResponse response) {
		// TODO Auto-generated method stub

		List<String> priceSplitsHolder = null;
		String getCommisionPercent = ConfigurationHandler.getValueToConfigurationKey("commision.percent",
				"env.properties");
		String balanceTransferToOperator = null;
		String amountCollectedString = null;
		String amountToPyadvertisingString = null;

		if (response != null) {
			if (response.getContract() != null) {
				double amountCollected = response.getContract().getPrice();
				amountCollected = 450;
				if (amountCollected != 0.0) {
					if (StringUtils.isNotBlank(getCommisionPercent)) {
						
						priceSplitsHolder=new ArrayList<>();
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

}

	*/
