package org.axisgroup.base.controller;



import org.apache.log4j.Logger;
import org.axisgroup.common.dto.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class AccountController {
	@Autowired
	 AccountService accountService;
	
	private static final Logger logger = Logger.getLogger(AccountController.class);
	
	@RequestMapping("/person")
	public String getPersonDetail(@RequestParam(value = "id",required = false,
	                                                    defaultValue = "0") Integer id) {
		logger.info("Inside data/person:::::::::::::");
		return "Hello";
	}
}