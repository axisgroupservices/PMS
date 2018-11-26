package org.axisgroup.confhandler;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;


public class ConfigurationHandler {
	protected static final Logger logger=Logger.getLogger(ConfigurationHandler.class);
	
	public static String getValueToConfigurationKey(String confKey, String confLocation){
		String configurationValue=null;
 		try {
 			 ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 	 		InputStream input = classLoader.getResourceAsStream(confLocation);
 	 		Properties properties = new Properties();
 			properties.load(input);
 			configurationValue=properties.getProperty(confKey);
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			logger.debug(e);
 		}

 		 logger.info("Configuration value to Configuration Key: "+confKey+" (should not be null) : is "+ configurationValue);
 		 
 		 return configurationValue;
	}

}
