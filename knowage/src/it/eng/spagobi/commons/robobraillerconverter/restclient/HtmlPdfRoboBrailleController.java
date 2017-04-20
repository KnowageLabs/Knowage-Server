package it.eng.spagobi.commons.robobraillerconverter.restclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import com.sun.research.ws.wadl.HTTPMethods;

import it.eng.spagobi.api.v2.AccessibleDocumentExportResource;
import it.eng.spagobi.commons.robobraillerconverter.jobmanager.Job;
import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobManager;
import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobState;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class HtmlPdfRoboBrailleController extends AbstractRoboBrailleController implements JobManager {
	
	private final String CONTROLLERPATH = "/api/HTMLtoPDF";
	static private Logger logger = Logger.getLogger(HtmlPdfRoboBrailleController.class);
	@Override
	public Job createJob(byte[] file, Map<String, String> params) {

		Job job = null;
		ClientRequest request = null;
		ClientResponse<String> response = null;
		String baseUrl = this.httpClient.getHostConfiguration().getHostURL();
		
		
		try {
			
			
			request = new ClientRequest(this.CONTROLLERPATH +"/Post", this.executor);
			
			request.header("Authorization",this.hawkClient.generateAuthorizationHeader(new URI(baseUrl+request.getUri()), HTTPMethods.POST.toString(), null, null, null, null) );
			MultipartFormDataOutput  mp = new MultipartFormDataOutput();
			OutputPart part =  mp.addFormData("filecontent", file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
			
			part.getHeaders().add("Content-Disposition", "form-data; name=\"filecontent\"; filename=\""+params.get("filename")+"\"");
			mp.addFormData("size", params.get("size"), MediaType.TEXT_PLAIN_TYPE);
			
			request.body(MediaType.MULTIPART_FORM_DATA_TYPE, mp);
			response =	request.post(String.class);
			String resposeString = response.getEntity().replace("\"", "");
			
			if(resposeString.contains("Message")){
				logger.error(resposeString);
				throw new SpagoBIRuntimeException(resposeString);
			}
			String jobId = resposeString;
			job = new Job(jobId);
			return job;
			
		} catch (Exception e) {
			logger.error("error while getting result from API",e);
			throw new SpagoBIRuntimeException("error while getting result from API",e);
		}
		
		
		
	}

	@Override
	public void setJobStatus(Job job) {
		
		ClientRequest request = null;
		ClientResponse<String> response = null;
		String baseUrl = this.httpClient.getHostConfiguration().getHostURL();
		
		request = new ClientRequest(this.CONTROLLERPATH +"/GetJobStatus", this.executor);
		request.queryParameter("jobId", job.getJobId());
		try {
			request.header("Authorization",this.hawkClient.generateAuthorizationHeader(new URI(baseUrl+request.getUri()), HTTPMethods.GET.toString(), null, null, null, null) );
			response =	request.get(String.class);
			job.setJobState(JobState.getEnumNamebyState(response.getEntity().replace("\"", "")));
			
			
		} catch (Exception e) {
			logger.error("error while getting job status from API",e);
			throw new SpagoBIRuntimeException("error while getting job status from API",e);
		}
		
	}

	@Override
	public File getJobResult(Job job) {
		ClientRequest request = null;
		ClientResponse<byte[]> response = null;
		String baseUrl = this.httpClient.getHostConfiguration().getHostURL();
		
		request = new ClientRequest(this.CONTROLLERPATH +"/GetJobResult", this.executor);
		request.queryParameter("jobId", job.getJobId());
		try {
			request.header("Authorization",this.hawkClient.generateAuthorizationHeader(new URI(baseUrl+request.getUri()), HTTPMethods.GET.toString(), null, null, null, null) );
			response =	request.get(byte[].class);
			File temp = File.createTempFile(getFileName(response.getHeaders()), ".pdf");
			FileOutputStream fis = new FileOutputStream(temp);
			fis.write(response.getEntity());
			fis.close();
			temp.getName();
			
			
			
			return temp;
			
		} catch (Exception e) {
			logger.error("error while getting result from API",e);
			throw new SpagoBIRuntimeException("error while getting result from API",e);
			
		}
		
	}

	@Override
	public void deleteJob(Job job) {
		ClientRequest request = null;
		ClientResponse<String> response = null;
		String baseUrl = this.httpClient.getHostConfiguration().getHostURL();
		
		request = new ClientRequest(this.CONTROLLERPATH +"/Delete", this.executor);
		request.queryParameter("jobId", job.getJobId());
		try {
			request.header("Authorization",this.hawkClient.generateAuthorizationHeader(new URI(baseUrl+request.getUri()), HTTPMethods.GET.toString(), null, null, null, null) );
			response =	request.delete(String.class);
			
			
			
		} catch (Exception e) {
			logger.error("error while deleting job on API",e);
			throw new SpagoBIRuntimeException("error while deleting job on API",e);
		}
		
		
	}
	
	private String getFileName(MultivaluedMap<String, String> headers) {
		 
		String[] contentDisposition = headers.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return null;
	}
	
	
 
}
