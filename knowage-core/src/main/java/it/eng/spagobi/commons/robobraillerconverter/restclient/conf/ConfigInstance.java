package it.eng.spagobi.commons.robobraillerconverter.restclient.conf;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class ConfigInstance {
		static private Logger logger = Logger.getLogger(ConfigInstance.class);
		private final static String CONF_FILE = "robobraille_conf.xml";
		private static RobobrailleConfiguration robobrailleConfiguration = null;
		
		public static RobobrailleConfiguration getRobobrailleConfiguration(){
			
			InputStream input = ConfigInstance.class.getResourceAsStream(CONF_FILE);
			JAXBContext jcon;
			try {
				jcon = JAXBContext.newInstance( RobobrailleConfiguration.class );
				Unmarshaller umar = jcon.createUnmarshaller();
				robobrailleConfiguration = (RobobrailleConfiguration) umar.unmarshal( input );
				return robobrailleConfiguration;
			} catch (JAXBException e) {
				logger.error("Unmarhalling problem", e);
				throw new SpagoBIRuntimeException("Unmarhalling problem",e);
			}
			
			
		}
		
}
