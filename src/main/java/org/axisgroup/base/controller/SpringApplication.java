package org.axisgroup.base.controller;

import org.axisgroup.common.dto.Account;
import org.axisgroup.confhandler.ConfigurationHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringApplication
{
    public static void main( String[] args )
    {
    	ApplicationContext context =
    	  new ClassPathXmlApplicationContext(new String[] {"dispatcher-servlet.xml"});

    	Account cust = (Account)context.getBean("AccountBean");
    	System.out.println(cust.getPersonalAccount());
    	
     
     String key=  ConfigurationHandler.getValueToConfigurationKey("secret", "WEB-INF/env.properties");
     
     System.out.println(key);

    }
}
