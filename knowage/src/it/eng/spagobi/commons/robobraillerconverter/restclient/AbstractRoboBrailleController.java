package it.eng.spagobi.commons.robobraillerconverter.restclient;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;

import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkCredentials.Algorithm;
import com.wealdtech.hawk.HawkCredentials.Builder;

import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobManager;
import it.eng.spagobi.commons.robobraillerconverter.restclient.conf.ConfigInstance;
import it.eng.spagobi.commons.robobraillerconverter.restclient.conf.RobobrailleConfiguration;

public abstract class AbstractRoboBrailleController  implements JobManager {
	protected HttpClient httpClient;
	protected HawkClient hawkClient;
	protected ClientExecutor executor;
	private RobobrailleConfiguration robobrailleConfiguration;
	
	public AbstractRoboBrailleController() {
		robobrailleConfiguration  = ConfigInstance.getRobobrailleConfiguration();
		setHawkClient();
		setHttpClient();
		
	}
	
	
	private void setHttpClient(){
		
		
		
		httpClient = new HttpClient();
		
		httpClient.getHostConfiguration().setHost(	robobrailleConfiguration.getHost(), 
																			robobrailleConfiguration.getPort(), 
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
 
}

