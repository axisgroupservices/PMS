import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.axisgroup.client.config.RestApiConfiguration;
import org.axisgroup.client.rest.enums.RestMethod;
import org.axisgroup.client.rest.restfulclient.RestClient;
import org.axisgroup.paypal.payouts.Amount;
import org.axisgroup.paypal.payouts.Item;
import org.axisgroup.paypal.payouts.PaypalPayoutRequest;
import org.axisgroup.paypal.payouts.SendBatchHeader;
import org.axisgroup.paypal.payouts.response.PaypalPayoutResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class Application {
	
	public static void main(String[] args) throws IOException{
		
		// Send Email Have the service calls
				//logger.info("Contract ID from readerfile::::: "+affidavitMetaHolders.get(0).getContractId());
				//getContractsInfo
		        PaypalPayoutRequest request = new PaypalPayoutRequest();
		        
		        //Create Batch header
		        SendBatchHeader header =new SendBatchHeader();
		        header.setSender_batch_id("2014021810");
		        header.setEmail_subject("You have a payout from Rest client!");
		        
		        //Create items
		        List<Item> items=new ArrayList<>();
		        Item item1 = new Item();
		        item1.setRecipient_type("EMAIL");
		        Amount amount = new Amount();
		        amount.setCurrency("USD");
		        amount.setValue("111");
		        item1.setAmount(amount);
		        item1.setNote("Thanks for your patronage!");
		        item1.setSender_item_id("201403140003");
		        item1.setReceiver("deepak.pokhrel132-buyer@gmail.com");
		        
		      //Create items
		        Item item2 = new Item();
		        item2.setRecipient_type("EMAIL");
		        Amount amount2 = new Amount();
		        amount2.setCurrency("USD");
		        amount2.setValue("111");
		        item2.setAmount(amount);
		        item2.setNote("Thanks for your patronage!");
		        item2.setSender_item_id("201403140003");
		        item2.setReceiver("roboad-personal@gmail.com");
		        
		        items.add(item1);
		        items.add(item2);
		        
		        
		        request.setSender_batch_header(header);
		        request.setItems(items);
		        
		        
		        
		        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		        headers.add("Authorization", "Bearer A21AAFO-ENak6MCBqk5Yb68jfgUcNQIRRNkaRRjMQfBXB4ZKC78k-jenT8kuopymLva2IcsEr3tE7n064lBOHFGeO09XXTn3A");
		        headers.add("Content-Type", "application/json");

		        RestTemplate restTemplate = new RestTemplate();
		        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

		        HttpEntity<PaypalPayoutRequest> 	entity = new HttpEntity<PaypalPayoutRequest>(request, headers);

		        PaypalPayoutResponse response= restTemplate.postForObject("https://api.sandbox.paypal.com/v1/payments/payouts", entity, PaypalPayoutResponse.class);
			
				System.out.println("Printing Request:::::::");
				prittyPrint(entity);
				System.out.println("\n\n\n\n\n\n\n\n\n\nPrinting Response:::::::");
				prittyPrint(response);
				
		
		
		
		
		
		
	}
	
	private static void prittyPrint(Object genrics) throws IOException{
		  ObjectMapper obj= new ObjectMapper();
		  System.out.println(obj.writerWithDefaultPrettyPrinter().writeValueAsString(genrics));
        //logger.info(obj.writerWithDefaultPrettyPrinter().writeValueAsString(genrics));

	}
	

}
