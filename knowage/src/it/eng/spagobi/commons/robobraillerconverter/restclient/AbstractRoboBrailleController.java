package it.eng.spagobi.commons.robobraillerconverter.restclient;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;

import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkCredentials.Algorithm;
import com.wealdtech.hawk.HawkCredentials.Builder;

import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobManager;
import it.eng.spagobi.commons.robobraillerconverter.restclient.conf.ConfigInstance;
import it.eng.spagobi.commons.robobraillerconverter.restclient.conf.RobobrailleConfiguration;
import it.eng.spagobi.tools.dataset.ckan.utils.CKANUtils;

public abstract class AbstractRoboBrailleController implements JobManager {

	static protected Logger logger = Logger.getLogger(AbstractRoboBrailleController.class);
	protected HttpClient httpClient;
	protected HawkClient hawkClient;
	protected ClientExecutor executor;
	private RobobrailleConfiguration robobrailleConfiguration;

	public AbstractRoboBrailleController() {
		robobrailleConfiguration = ConfigInstance.getRobobrailleConfiguration();
		setHawkClient();
		setHttpClient();

	}

	private void setHttpClient() {

		String proxyHost = System.getProperty("http.robobrailleProxyHost");
		String proxyPort = System.getProperty("http.robobrailleProxyPort");
		int proxyPortInt = Integer.parseInt(proxyPort);
		String proxyUsername = System.getProperty("http.robobrailleProxyUsername");
		String proxyPassword = System.getProperty("http.robobrailleProxyPassword");

		httpClient = new HttpClient();
		
		if (proxyHost != null && proxyPortInt > 0) {
			if(proxyUsername != null && proxyPassword != null){
				logger.debug("Setting proxy with authentication");
				httpClient.getHostConfiguration().setProxy(proxyHost, proxyPortInt);
				AuthScope authscope  = new AuthScope(proxyHost, proxyPortInt);
				Credentials credentials = new UsernamePasswordCredentials(proxyUsername,proxyPassword); 
				httpClient.getState().setProxyCredentials(authscope, credentials);
				logger.debug("Proxy with authentication set");
			} else {
				logger.debug("Setting proxy without authentication");
				httpClient.getHostConfiguration().setProxy(proxyHost, proxyPortInt);
				logger.debug("Proxy without authentication set");
			}
			
		} else {
			logger.debug("No proxy configuration found");
		}

		httpClient.getHostConfiguration().setHost(robobrailleConfiguration.getHost(), robobrailleConfiguration.getPort(),
				robobrailleConfiguration.getProtocol());
		executor = new ApacheHttpClientExecutor(httpClient);
	}

	private void setHawkClient() {
		Builder builder = new HawkCredentials.Builder();

		builder.keyId(robobrailleConfiguration.getId());
		builder.key(robobrailleConfiguration.getKey());
		builder.algorithm(Algorithm.parse(robobrailleConfiguration.getAlgorithm()));

		HawkCredentials hawkCredentials = builder.build();

		hawkClient = new HawkClient.Builder().credentials(hawkCredentials).build();

	}
	
	private void setProxy(){
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
			authscope  = new AuthScope(proxyHost, proxyPortInt);
			Credentials credentials = new UsernamePasswordCredentials(proxyUsername,proxyPassword); 
			httpClient.getState().setProxyCredentials(authscope, credentials);
			logger.debug("Proxy with authentication set");
		} else {
			logger.debug("Setting proxy without authentication");
			httpClient.getHostConfiguration().setProxy(proxyHost, proxyPortInt);
			logger.debug("Proxy without authentication set");
		}
	}

}
