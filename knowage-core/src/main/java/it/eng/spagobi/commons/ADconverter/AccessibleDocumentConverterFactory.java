package it.eng.spagobi.commons.ADconverter;

import it.eng.spagobi.api.v2.AccessibleDocumentExportResource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

import org.apache.log4j.Logger;

public class AccessibleDocumentConverterFactory {

	static private Logger logger = Logger.getLogger(AccessibleDocumentExportResource.class);
	static private EnumMap<ConversionType,Class<? extends AccessibleDocumentConverter>> conversionImpelementations = new EnumMap<>(ConversionType.class);


	public AccessibleDocumentConverterFactory(){
//		conversionImpelementations.put(ConversionType.HTMLPDF, RoboBrailleConverter.class);
//		conversionImpelementations.put(ConversionType.TXTMP3, RoboBrailleConverter.class);
//		conversionImpelementations.put(ConversionType.HTMLTXT, RoboBrailleConverter.class);
	}


	public AccessibleDocumentConverter getAccessibleDocumentConverter(ConversionType conversionType){

		try {
			return conversionImpelementations.get(conversionType).getConstructor(ConversionType.class).newInstance(conversionType);
		} catch (InstantiationException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		} catch (IllegalAccessException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		} catch (IllegalArgumentException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		} catch (InvocationTargetException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		} catch (NoSuchMethodException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		} catch (SecurityException e) {
			logger.error("Class instatiantion problem", e);
			throw new SpagoBIRuntimeException("Class instatiantion problem",e);
		}


	}



}
