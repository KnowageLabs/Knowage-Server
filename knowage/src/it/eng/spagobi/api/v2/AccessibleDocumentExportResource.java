package it.eng.spagobi.api.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.GenericType;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.ADconverter.AccessibleDocumentConverter;
import it.eng.spagobi.commons.ADconverter.AccessibleDocumentConverterFactory;
import it.eng.spagobi.commons.ADconverter.ConversionType;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/exportAccessibleDocument")
@ManageAuthorization
public class AccessibleDocumentExportResource extends AbstractSpagoBIResource {
	static private String SERVICENAME = "exportAccessibleDocument";
	static private Logger logger = Logger.getLogger(AccessibleDocumentExportResource.class);
	
	@GET
	@Path("/{conversionType}/getResult/{jobId}")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public Response getResult(@PathParam("jobId")String jobId,@PathParam("conversionType") String conversionType){
		
		ConversionType convesionType;
		AccessibleDocumentConverterFactory ADCFactory;
		AccessibleDocumentConverter dc;
		File outputFile;
		ResponseBuilder responseBuilder;
		
		convesionType = ConversionType.valueOf(conversionType);
		ADCFactory = new AccessibleDocumentConverterFactory();
		dc = ADCFactory.getAccessibleDocumentConverter(convesionType);
		
		outputFile = dc.getConversionResult(jobId);

		responseBuilder = Response.ok(outputFile)
		.header("Content-Disposition", "attachment; filename="+ outputFile.getName())
		.header("filename", outputFile.getName());
		
		return responseBuilder.build();
	}
	@POST
	@Path("/{conversionType}/startconversion")
	@Consumes({ MediaType.MULTIPART_FORM_DATA})
	@Produces({ MediaType.TEXT_PLAIN })
	public Response startConversion(@MultipartForm MultipartFormDataInput multipartFormDataInput, @PathParam("conversionType") String conversionType) {
		
		byte[] file =null;
		ConversionType convesionType = null;
		Map<String,String> params = new HashMap<>();
		
		
		file = getFileBytes(multipartFormDataInput);
		params = getParams(multipartFormDataInput);
		try {
		convesionType = ConversionType.valueOf(conversionType);
		

		AccessibleDocumentConverterFactory ADCFactory = new AccessibleDocumentConverterFactory();
		AccessibleDocumentConverter dc = ADCFactory.getAccessibleDocumentConverter(convesionType);
		
		String jobId = dc.startConversion(file,params);
		
		return Response.ok(jobId).build() ;
		} 
		catch (Exception e) {
			logger.error("error in service",e);
			throw new SpagoBIEngineServiceException(SERVICENAME, "ERROR",e);
		}
	}
	
	

	private byte[] getFileBytes( MultipartFormDataInput multipartFormDataInput){
		
		byte[] file = null;
		Set<Entry<String, List<InputPart>>>    multiPartParams = multipartFormDataInput.getFormDataMap().entrySet();
		
		for (Iterator<Map.Entry<String, List<InputPart>>> iterator = multiPartParams.iterator(); iterator.hasNext();) {
			 Map.Entry<String, List<InputPart>> part =  iterator.next();
			if(part.getKey().equals("file")){
				try {
				file =	multipartFormDataInput.getFormDataPart(part.getKey(), new GenericType<byte[]>(){});
				return file;
				} catch (IOException e) {
					logger.error("error getting a file from multipart form",e);
					throw new SpagoBIEngineServiceException(SERVICENAME, "error getting a file from multipart form",e);
				}
			}
		
		}
		logger.error("paramter with name file is mandatory");
		throw new SpagoBIEngineServiceException(SERVICENAME, "paramter with name file is mandatory");
	}
	
	private Map<String,String> getParams(MultipartFormDataInput multipartFormDataInput){
		
		Map<String,String> params = new HashMap<String, String>();
		Set<Entry<String, List<InputPart>>>    multiPartParams = multipartFormDataInput.getFormDataMap().entrySet();
		
		for (Iterator<Map.Entry<String, List<InputPart>>> iterator = multiPartParams.iterator(); iterator.hasNext();) {
			 Map.Entry<String, List<InputPart>> part =  iterator.next();
			 if(!part.getKey().equals("file")){
				try {
					params.put(part.getKey(), multipartFormDataInput.getFormDataPart(part.getKey(), new GenericType<String>(){}));
					
				} catch (IOException e) {
					logger.error("error getting a param "+part.getKey()+" from multipart form",e);
					throw new SpagoBIEngineServiceException(SERVICENAME, "error getting a  "+part.getKey()+" from multipart form",e);
				}
			 }
		}
		
		return params;
	}
	
	
	
	
	
}
