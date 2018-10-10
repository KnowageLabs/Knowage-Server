package it.eng.spagobi.commons.robobraillerconverter.restclient;

import java.util.EnumMap;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.ADconverter.ConversionType;
import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class RoboBrailleControllerFactory {
	static private Logger logger = Logger.getLogger(RoboBrailleControllerFactory.class);
	static private EnumMap<ConversionType,Class<? extends JobManager>> conversionImpelementations = new EnumMap<>(ConversionType.class);
	

	public RoboBrailleControllerFactory(){
		conversionImpelementations.put(ConversionType.HTMLPDF, HtmlPdfRoboBrailleController.class);
		conversionImpelementations.put(ConversionType.TXTMP3, AudioRoboBrailleController.class);
		conversionImpelementations.put(ConversionType.HTMLTXT, HtmlTxtRobobrailleController.class);
	}
	
	
	public JobManager getJobManager(ConversionType conversionType){
		
		try {
			return conversionImpelementations.get(conversionType).newInstance();
		} catch (InstantiationException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		} catch (IllegalAccessException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		}
		
	
	}
	
}
