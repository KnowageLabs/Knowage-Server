package it.eng.spagobi.commons.robobraillerconverter.restclient;

import java.io.File;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;

import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkCredentials.Algorithm;
import com.wealdtech.hawk.HawkCredentials.Builder;

import it.eng.spagobi.commons.robobraillerconverter.jobmanager.Job;
import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobManager;
import it.eng.spagobi.commons.robobraillerconverter.restclient.conf.ConfigInstance;
import it.eng.spagobi.commons.robobraillerconverter.restclient.conf.RobobrailleConfiguration;

public abstract class AbstractRoboBrailleController implements JobManager {

	static protected Logger logger = Logger.getLogger(AbstractRoboBrailleController.class);
	protected HttpClient httpClient;
	protected HawkClient hawkClient;
	// protected ClientExecutor executor;
	protected String controllerPath;
	protected String fileExtension;
	private RobobrailleConfiguration robobrailleConfiguration;

	public AbstractRoboBrailleController() {
		robobrailleConfiguration = ConfigInstance.getRobobrailleConfiguration();
		setHawkClient();
		setHttpClient();

	}

	@Override
	public Job createJob(byte[] file, Map<String, String> params) {

//		Job job = null;
//		ClientRequest request = null;
//		ClientResponse<String> response = null;
//		String baseUrl = this.httpClient.getHostConfiguration().getHostURL();
//
//
//		try {
//
//
//			request = new ClientRequest(this.controllerPath +"/Post", this.executor);
//
//			request.header("Authorization",this.hawkClient.generateAuthorizationHeader(new URI(baseUrl+request.getUri()), HTTPMethods.POST.toString(), null, null, null, null) );
//			MultipartFormDataOutput  mp = new MultipartFormDataOutput();
//			OutputPart part =  mp.addFormData("filecontent", file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
//
//			part.getHeaders().add("Content-Disposition", "form-data; name=\"filecontent\"; filename=\""+params.get("fileName")+"\"");
//			params.remove("fileName");
//
//			for (Iterator<Entry<String,String>> iterator = params.entrySet().iterator(); iterator.hasNext();) {
//				 Map.Entry<String, String> param =  iterator.next();
//						mp.addFormData(param.getKey(), param.getValue(), MediaType.TEXT_PLAIN_TYPE);
//
//			}
//
//			request.body(MediaType.MULTIPART_FORM_DATA_TYPE, mp);
//			response =	request.post(String.class);
//			String resposeString = response.getEntity().replace("\"", "");
//
//			if(resposeString.contains("error")){
//				logger.error(resposeString);
//				throw new SpagoBIRuntimeException(resposeString);
//			}
//			String jobId = resposeString;
//			job = new Job(jobId);
//			return job;
//
//		} catch (Exception e) {
//			logger.error("error while getting result from API",e);
//			throw new SpagoBIRuntimeException("error while getting result from API",e);
//		}
//
		return null;

	}

	@Override
	public void setJobStatus(Job job) {
//
//		ClientRequest request = null;
//		ClientResponse<String> response = null;
//		String baseUrl = this.httpClient.getHostConfiguration().getHostURL();
//
//		request = new ClientRequest(this.controllerPath +"/GetJobStatus", this.executor);
//		request.queryParameter("jobId", job.getJobId());
//		try {
//			request.header("Authorization",this.hawkClient.generateAuthorizationHeader(new URI(baseUrl+request.getUri()), HTTPMethods.GET.toString(), null, null, null, null) );
//			response =	request.get(String.class);
//			job.setJobState(JobState.getEnumNamebyState(response.getEntity().replace("\"", "")));
//
//
//		} catch (Exception e) {
//			logger.error("error while getting job status from API",e);
//			throw new SpagoBIRuntimeException("error while getting job status from API",e);
//		}

	}

	@Override
	public File getJobResult(Job job) {
//		ClientRequest request = null;
//		ClientResponse<byte[]> response = null;
//		String baseUrl = this.httpClient.getHostConfiguration().getHostURL();
//
//		request = new ClientRequest(this.controllerPath +"/GetJobResult", this.executor);
//		request.queryParameter("jobId", job.getJobId());
//		try {
//			request.header("Authorization",this.hawkClient.generateAuthorizationHeader(new URI(baseUrl+request.getUri()), HTTPMethods.GET.toString(), null, null, null, null) );
//			response =	request.get(byte[].class);
//			File temp = File.createTempFile(getFileName(response.getHeaders()), this.fileExtension);
//			FileOutputStream fis = new FileOutputStream(temp);
//			fis.write(response.getEntity());
//			fis.close();
//			temp.getName();
//
//
//
//			return temp;
//
//		} catch (Exception e) {
//			logger.error("error while getting result from API",e);
//			throw new SpagoBIRuntimeException("error while getting result from API",e);
//
//		}
		return null;
	}

	@Override
	public void deleteJob(Job job) {
//		ClientRequest request = null;
//		ClientResponse<String> response = null;
//		String baseUrl = this.httpClient.getHostConfiguration().getHostURL();
//
//		request = new ClientRequest(this.controllerPath +"/Delete", this.executor);
//		request.queryParameter("jobId", job.getJobId());
//		try {
//			request.header("Authorization",this.hawkClient.generateAuthorizationHeader(new URI(baseUrl+request.getUri()), HTTPMethods.GET.toString(), null, null, null, null) );
//			response =	request.delete(String.class);
//
//
//
//		} catch (Exception e) {
//			logger.error("error while deleting job on API",e);
//			throw new SpagoBIRuntimeException("error while deleting job on API",e);
//		}

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

	private void setHttpClient() {

//		String proxyHost = System.getProperty("http.robobrailleProxyHost");
//		String proxyPort = System.getProperty("http.robobrailleProxyPort");
//
//		String proxyUsername = System.getProperty("http.robobrailleProxyUsername");
//		String proxyPassword = System.getProperty("http.robobrailleProxyPassword");
//
//		httpClient = new HttpClient();
//
//		if (proxyHost != null && proxyPort!=null) {
//			int proxyPortInt = Integer.parseInt(proxyPort);
//			if(proxyUsername != null && proxyPassword != null){
//				logger.debug("Setting proxy with authentication");
//				httpClient.getHostConfiguration().setProxy(proxyHost, proxyPortInt);
//				AuthScope authscope  = new AuthScope(proxyHost, proxyPortInt);
//				Credentials credentials = new UsernamePasswordCredentials(proxyUsername,proxyPassword);
//				httpClient.getState().setProxyCredentials(authscope, credentials);
//				logger.debug("Proxy with authentication set");
//			} else {
//				logger.debug("Setting proxy without authentication");
//				httpClient.getHostConfiguration().setProxy(proxyHost, proxyPortInt);
//				logger.debug("Proxy without authentication set");
//			}
//
//		} else {
//			logger.debug("No proxy configuration found");
//		}
//
//		httpClient.getHostConfiguration().setHost(robobrailleConfiguration.getHost(), robobrailleConfiguration.getPort(),
//				robobrailleConfiguration.getProtocol());
//		executor = new ApacheHttpClientExecutor(httpClient);
	}

	private void setHawkClient() {
		Builder builder = new HawkCredentials.Builder();

		builder.keyId(robobrailleConfiguration.getHawkId());
		builder.key(robobrailleConfiguration.getHawkKey());
		builder.algorithm(Algorithm.parse(robobrailleConfiguration.getAlgorithm()));

		HawkCredentials hawkCredentials = builder.build();

		hawkClient = new HawkClient.Builder().credentials(hawkCredentials).build();

	}

	private void setProxy() {
		String proxyHost = System.getProperty("http.robobrailleProxyHost");
		String proxyPort = System.getProperty("http.robobrailleProxyPort");
		int proxyPortInt = Integer.parseInt(proxyPort);
		String proxyUsername = System.getProperty("http.proxyUsername");
		String proxyPassword = System.getProperty("http.proxyPassword");
		AuthScope authscope = null;
		httpClient = new HttpClient();

		if (proxyHost != null && proxyPortInt > 0 && proxyUsername != null && !proxyUsername.isEmpty()) {

			logger.debug("Setting proxy with authentication");
			httpClient.getHostConfiguration().setProxy(proxyHost, proxyPortInt);
			authscope = new AuthScope(proxyHost, proxyPortInt);
			Credentials credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
			httpClient.getState().setProxyCredentials(authscope, credentials);
			logger.debug("Proxy with authentication set");
		} else {
			logger.debug("Setting proxy without authentication");
			httpClient.getHostConfiguration().setProxy(proxyHost, proxyPortInt);
			logger.debug("Proxy without authentication set");
		}
	}

}
