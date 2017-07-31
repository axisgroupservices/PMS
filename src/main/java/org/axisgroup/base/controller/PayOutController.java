package org.axisgroup.base.controller;



import org.apache.log4j.Logger;
import org.axisgroup.common.dto.AccountService;
import org.axisgroup.confhandler.PayOutApplication;
import org.axisgroup.paypal.payouts.request.PaypalPayoutRequest;
import org.axisgroup.paypal.payouts.response.PaypalPayoutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payout")
public class PayOutController {
	@Autowired
	 AccountService accountService;
	
	private static final Logger logger = Logger.getLogger(PayOutController.class);
	
	@RequestMapping("/stakeholders")
	public PaypalPayoutResponse payOutStakeHolders(@RequestBody PaypalPayoutRequest request) {
		logger.info("Inside payOutStakeHolders:::::::::::::"+ request);
		
		PayOutApplication payoutApp= new PayOutApplication();
		
		return null;
	}
}