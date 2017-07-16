package org.axisgroup.confhandler;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class PrettyPrinterJson {
	
	private static final Logger logger = Logger.getLogger(PrettyPrinterJson.class);
	
	public static void printObject(Object generics) throws JsonGenerationException, JsonMappingException, IOException{
		
			ObjectMapper obj = new ObjectMapper();
			logger.info(obj.writerWithDefaultPrettyPrinter().writeValueAsString(generics));
			// logger.info(obj.writerWithDefaultPrettyPrinter().writeValueAsString(genrics));

		

	}

}
