package it.eng.spagobi.commons.robobraillerconverter.restclient;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;

import com.wealdtech.hawk.HawkClient;
import com.wealdtech.hawk.HawkCredentials;
import com.wealdtech.hawk.HawkCredentials.Algorithm;
import com.wealdtech.hawk.HawkCredentials.Builder;

import it.eng.spagobi.commons.robobraillerconverter.jobmanager.JobManager;

public abstract class AbstractRoboBrailleController  implements JobManager {
	protected HttpClient httpClient;
	protected HawkClient hawkClient;
	protected ClientExecutor executor;
	
	public AbstractRoboBrailleController() {
		setHawkClient();
		setHttpClient();
	}
	
	
	private void setHttpClient(){
		
		
		
		httpClient = new HttpClient();
		
		httpClient.getHostConfiguration().setHost("2.109.50.18", 5150, "http");
		executor = new ApacheHttpClientExecutor(httpClient);
		}

	private void setHawkClient() {
		Builder builder = new HawkCredentials.Builder();
	
		builder.keyId("9d3a2c9d-9714-e711-88c6-1c6f65d84158");
		builder.key("f4b9be89-f819-45f9-8a50-10d21445ff22");
		builder.algorithm(Algorithm.SHA256);
	
		HawkCredentials hawkCredentials = builder.build();
	
		hawkClient = new HawkClient.Builder().credentials(hawkCredentials).build();
	
	}
 
}

