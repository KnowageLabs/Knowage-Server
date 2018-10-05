package it.eng.spagobi.commons.ADconverter;

import java.io.File;
import java.util.Map;

public interface AccessibleDocumentConverter {
	
	
	public String startConversion(byte[] file,Map<String, String> params);
	public File getConversionResult(String jobId);
	

}
