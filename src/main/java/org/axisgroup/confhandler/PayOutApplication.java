package org.axisgroup.confhandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.axisgroup.common.dto.Item;
import org.axisgroup.common.dto.PayoutError;
import org.axisgroup.common.dto.SendBatchHeader;
import org.axisgroup.paypal.payouts.request.PaypalPayoutRequest;
import org.axisgroup.paypal.payouts.response.PaypalPayoutResponse;
import org.axisgroup.paypal.utils.PayoutStakeHolderInfo;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;

public class PayOutApplication {

	private static final Logger logger = Logger.getLogger(PayOutApplication.class);
	private static final String CONFIG_LOCATION = "env.properties";
	private static final String PAYPAL_ENDPOINT = "oauth.endpoint.paypal";
	private static final String ROBOADPLACER_CLIENT_ID = "roboadplacer.clientId";
	private static final String ROBOADPLACER_SECRET = "roboadplacer.secret";
	private static final String PYADVERTISING_CLIENT_ID = "pyadvertising.clientId";
	private static final String PYADVERTISING_SECRET = "pyadvertising.secret";

	
	public static void main(String[] args) throws IOException {
		// For web, this location should be Meta-Inf/env.properties and that
		// file must be there as well.
		//getConfigurations();
		
		ConfigurationHandler.getValueToConfigurationKey("pyadvertising.paypalinfo", CONFIG_LOCATION);

		PayOutApplication p= new PayOutApplication();
		String id=p.generateUniqueBatchId("45");
		
		
		System.out.println(id);
		
		
		
		
		
		
	/*
		List<PayoutStakeHolderInfo> stakeHoldersInfo = new ArrayList<>();
		PayoutStakeHolderInfo stakeHolder = new PayoutStakeHolderInfo();
		stakeHolder.setPaymentNote("This is payment for Month June");
		Amount amount = new Amount();
		amount.setCurrency("USD");
		amount.setValue("1");

		stakeHolder.setPayoutAmount(amount);
		stakeHolder.setSenderItemId("201403140003");
		stakeHolder.setRecipeintType("EMAIL");
		stakeHolder.setPayPalAccountEmail("deepak.pokhrel132-buyer@gmail.com");

		PayoutStakeHolderInfo stakeHolder2 = new PayoutStakeHolderInfo();
		stakeHolder2.setPaymentNote("This is payment for Month June");
		Amount amount2 = new Amount();
		amount2.setCurrency("USD");
		amount2.setValue("1");

		stakeHolder2.setPayoutAmount(amount2);
		stakeHolder2.setSenderItemId("201403140003");
		stakeHolder2.setRecipeintType("EMAIL");
		stakeHolder2.setPayPalAccountEmail("roboad-personal@gmail.com");

		stakeHoldersInfo.add(stakeHolder);
		stakeHoldersInfo.add(stakeHolder2);

		String uniqueBatchIdperMonth = "2014021825";
		String emailSubject = "You have a payout from Rest client!";

		// Create request
		PaypalPayoutRequest request = apps.createPaypalPayoutRequest(uniqueBatchIdperMonth, emailSubject,
				stakeHoldersInfo);

		// generate response sends amount to multiple user.
		apps.payOut(request);
		
		
		*/

	}

	public PaypalPayoutRequest createPaypalPayoutRequest(String emailSubject,
			List<PayoutStakeHolderInfo> stakeHoldersInfo,String spotId) {
		// TODO Auto-generated method stub
		String uniqueBatchIdperMonth= generateUniqueBatchId(spotId);
		logger.info("unique batch id "+uniqueBatchIdperMonth);
		PaypalPayoutRequest request = new PaypalPayoutRequest();
		// Create Batch header
		SendBatchHeader header = new SendBatchHeader();
		header.setSender_batch_id(uniqueBatchIdperMonth);
		header.setEmail_subject(emailSubject);
		header.setSender_batch_id(uniqueBatchIdperMonth);

		// Create items
		List<Item> items = generatePayoutItems(stakeHoldersInfo);

		request.setSender_batch_header(header);
		request.setItems(items);

		return request;

	}

	private String generateUniqueBatchId(String spotId) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				String isPaymentSaved = null;
				try {
					String url = ConfigurationHandler.getValueToConfigurationKey("server.url", "env.properties");
					String resourcePath = "/RoboAdPlacer-ApiServices/get-unique-batch-id/paypal?spotId="+spotId;
					String endPoint = url + resourcePath;

					
					RestTemplate template = new RestTemplate();
					isPaymentSaved = template.getForObject(endPoint, String.class);
					logger.info(
							"Payment for OrderId " + spotId + " is saved. Is payment saved (must Be true) ? " + isPaymentSaved);
				} catch (HttpClientErrorException e) {
					logger.error(e);
				}
				return isPaymentSaved;
		
	}

	public PaypalPayoutResponse payOut(PaypalPayoutRequest request, String payeeEntity ) {
		// TODO Auto-generated method stub
		// gets configuration
		PaypalConfiguration configs = getConfigurations(payeeEntity);

		PaypalPayoutResponse response = new PaypalPayoutResponse();
		try {
			String accessToken = getAccessToken(configs);

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
			headers.add("Authorization", accessToken);
			headers.add("Content-Type", "application/json");

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

			HttpEntity<PaypalPayoutRequest> entity = new HttpEntity<PaypalPayoutRequest>(request, headers);

			response = restTemplate.postForObject(configs.getPayPalEndPoint() + "/v1/payments/payouts", entity,
					PaypalPayoutResponse.class);

		} catch (HttpClientErrorException exception) {
			logger.info("HttpClientErrorException occured");
			response = new PaypalPayoutResponse();
			if (exception != null) {
				PayoutError error = new PayoutError();

				error.setMessage(exception.getResponseBodyAsString());

				error.setInformation_link(exception.getStatusText());

				response.setError(error);
			}

		} catch (Exception e) {
			PayoutError error = new PayoutError();
			error.setMessage(e.toString());
			response.setError(error);
		}

		try {
			prittyPrint(response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;

	}

	private List<Item> generatePayoutItems(List<PayoutStakeHolderInfo> stakeHoldersInfo) {
		// TODO Auto-generated method stub
		List<Item> items = new ArrayList<>();
		for (PayoutStakeHolderInfo stakeHolderInfo : stakeHoldersInfo) {
			Item item = new Item();
			item.setRecipient_type(stakeHolderInfo.getRecipeintType());
			item.setAmount(stakeHolderInfo.getPayoutAmount());
			item.setNote(stakeHolderInfo.getPaymentNote());
			item.setSender_item_id(stakeHolderInfo.getSenderItemId());
			item.setReceiver(stakeHolderInfo.getPayPalAccountEmail());

			items.add(item);
		}
		return items;
	}

	public String getAccessToken(PaypalConfiguration configs) throws PayPalRESTException, JsonGenerationException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		Map<String, String> configurationMap = new HashMap<>();
		configurationMap.put("oauth.EndPoint", configs.getPayPalEndPoint());
		OAuthTokenCredential aAuthTokenCredential = new OAuthTokenCredential(configs.getClientId(), configs.getSecret(),
				configurationMap);

		String accessToken = null;
		logger.info("Token expires in" +aAuthTokenCredential.expiresIn());
	
		PrettyPrinterJson.printObject(aAuthTokenCredential);

		accessToken = aAuthTokenCredential.getAccessToken();
		logger.info("Access Token Created successfully " + accessToken);

		return accessToken;
	}
	
	private static void prittyPrint(Object genrics) throws IOException {
		ObjectMapper obj = new ObjectMapper();
		logger.info(obj.writerWithDefaultPrettyPrinter().writeValueAsString(genrics));
		// logger.info(obj.writerWithDefaultPrettyPrinter().writeValueAsString(genrics));

	}

	private static PaypalConfiguration getConfigurations(String payeeEntity) {
		PaypalConfiguration configs = null;
		String payPalEndpoint=null;
		String clientId=null;
		String secret=null;
		try{
		if(payeeEntity.equals("ROBOADPLACER")){
			payPalEndpoint = ConfigurationHandler.getValueToConfigurationKey(PAYPAL_ENDPOINT, CONFIG_LOCATION);
			clientId = ConfigurationHandler.getValueToConfigurationKey(ROBOADPLACER_CLIENT_ID, CONFIG_LOCATION);
			secret = ConfigurationHandler.getValueToConfigurationKey(ROBOADPLACER_SECRET, CONFIG_LOCATION);
		}
		
		else if(payeeEntity.equals("PYADVERTISING")){
			payPalEndpoint = ConfigurationHandler.getValueToConfigurationKey(PAYPAL_ENDPOINT, CONFIG_LOCATION);
			clientId = ConfigurationHandler.getValueToConfigurationKey(PYADVERTISING_CLIENT_ID, CONFIG_LOCATION);
			secret = ConfigurationHandler.getValueToConfigurationKey(PYADVERTISING_SECRET, CONFIG_LOCATION);
		}
		

		if (!payPalEndpoint.isEmpty() && !clientId.isEmpty() && !secret.isEmpty()) {
			configs = new PaypalConfiguration();
			configs.setClientId(clientId);
			configs.setConfigLocation(CONFIG_LOCATION);
			configs.setPayPalEndPoint(payPalEndpoint);
			configs.setSecret(secret);
		}
		else{
			logger.info(new Exception("Excetion Occured while getting configs!!"));
			throw new Exception("Excetion Occured while getting configs!!");
		}
		
		PrettyPrinterJson.printObject(configs);
		}
		catch(Exception e){
			logger.debug(e);
		}
		return configs;
	}
	
	

}
